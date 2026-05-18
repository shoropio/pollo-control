package com.pollocontrol.app.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pollocontrol.app.R
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?
)

class AuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val prefs = context.getSharedPreferences("auth_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    suspend fun signIn(activity: Activity): Result<AuthUser> = runCatching {
        val webClientId = context.getString(R.string.google_web_client_id)
        check(webClientId.isNotBlank() && !webClientId.startsWith("REPLACE_WITH")) {
            "Configura google_web_client_id con el OAuth Web Client ID de Firebase."
        }

        try {
            requestGoogleCredential(activity, webClientId, filterAuthorizedAccounts = true)
        } catch (e: GetCredentialException) {
            requestGoogleCredential(activity, webClientId, filterAuthorizedAccounts = false)
        }.let { credential ->
            signInWithFirebaseIfAvailable(credential.idToken)

            AuthUser(
                id = currentFirebaseUid() ?: credential.id,
                email = credential.email,
                displayName = credential.displayName,
                photoUrl = credential.photoUrl
            )
        }.also { user ->
            saveUser(user)
            _currentUser.value = user
        }
    }

    suspend fun signOut() {
        prefs.edit().clear().apply()
        _currentUser.value = null
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseAuth.getInstance().signOut()
        }
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private suspend fun requestGoogleCredential(
        activity: Activity,
        webClientId: String,
        filterAuthorizedAccounts: Boolean
    ): GoogleCredentialData {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(filterAuthorizedAccounts)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credential = credentialManager.getCredential(
            context = activity,
            request = request
        ).credential

        if (
            credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("La credencial seleccionada no es de Google.")
        }

        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw IllegalStateException("No se pudo leer la cuenta de Google.", e)
        }

        return GoogleCredentialData(
            id = googleCredential.id,
            email = googleCredential.email ?: googleCredential.id,
            displayName = googleCredential.displayName ?: googleCredential.id,
            photoUrl = googleCredential.profilePictureUri?.toString(),
            idToken = googleCredential.idToken
        )
    }

    private suspend fun signInWithFirebaseIfAvailable(idToken: String) {
        if (FirebaseApp.getApps(context).isEmpty()) return

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
    }

    private fun currentFirebaseUid(): String? {
        if (FirebaseApp.getApps(context).isEmpty()) return null
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun loadUser(): AuthUser? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_NAME, null) ?: email
        val photo = prefs.getString(KEY_PHOTO, null)
        return AuthUser(id, email, name, photo)
    }

    private fun saveUser(user: AuthUser) {
        prefs.edit()
            .putString(KEY_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_NAME, user.displayName)
            .putString(KEY_PHOTO, user.photoUrl)
            .apply()
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PHOTO = "photo"
    }
}

private data class GoogleCredentialData(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val idToken: String
)
