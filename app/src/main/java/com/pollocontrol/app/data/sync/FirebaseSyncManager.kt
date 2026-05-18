package com.pollocontrol.app.data.sync

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.MortalityEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.data.local.entity.SlaughterEntity
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.local.entity.TreatmentEntity
import com.pollocontrol.app.data.local.entity.WeighingEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SyncStatus {
    PENDING, SYNCING, SYNCED, ERROR, NOT_CONFIGURED
}

data class SyncResult(
    val success: Boolean,
    val message: String = ""
)

class FirebaseSyncManager(private val context: Context) {

    private val app: PolloControlApp
        get() = context.applicationContext as PolloControlApp

    private val gson = Gson()

    private val _syncStatus = MutableStateFlow(SyncStatus.PENDING)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    init {
        _syncStatus.value = if (isConfigured()) SyncStatus.PENDING else SyncStatus.NOT_CONFIGURED
    }

    fun isFirebaseConfigured(): Boolean = isConfigured()

    suspend fun syncAll(): SyncResult = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext publishResult(
                SyncStatus.NOT_CONFIGURED,
                SyncResult(
                    success = false,
                    message = "Firebase no configurado. Agrega app/google-services.json, habilita Google Sign-In y Firestore en Firebase Console, y vuelve a compilar la app."
                )
            )
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@withContext publishResult(
                SyncStatus.ERROR,
                SyncResult(false, "Inicia sesion con Google/Firebase antes de sincronizar.")
            )

        _syncStatus.value = SyncStatus.SYNCING

        return@withContext try {
            val firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(userId)

            var uploaded = 0
            var downloaded = 0

            val remoteDeletes = downloadTombstones(userDoc)
            remoteDeletes.forEach { tombstone ->
                app.database.syncTombstoneDao().insert(tombstone)
                applyLocalDelete(tombstone)
            }

            val localDeletes = app.database.syncTombstoneDao().getAll().first()
            uploaded += uploadTombstones(userDoc, localDeletes)
            localDeletes.forEach { tombstone ->
                userDoc.collection(tombstone.collectionName)
                    .document(tombstone.documentId.toString())
                    .delete()
                    .await()
                deleteRelatedRemoteDocuments(userDoc, tombstone)
            }

            uploaded += uploadCollection(userDoc, SyncCollections.BATCHES, app.database.batchDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.CLIENTS, app.database.clientDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.SUPPLIES, app.database.supplyDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.SUPPLY_MOVEMENTS, app.database.supplyMovementDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.EXPENSES, app.database.expenseDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.FEEDING, app.database.feedingDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.MORTALITY, app.database.mortalityDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.TREATMENTS, app.database.treatmentDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.WEIGHING, app.database.weighingDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.SLAUGHTER, app.database.slaughterDao().getAll().first())
            uploaded += uploadCollection(userDoc, SyncCollections.SALES, app.database.saleDao().getAll().first())

            downloaded += downloadCollection(userDoc, SyncCollections.BATCHES, BatchEntity::class.java, app.database.batchDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.CLIENTS, ClientEntity::class.java, app.database.clientDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.SUPPLIES, SupplyEntity::class.java, app.database.supplyDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.SUPPLY_MOVEMENTS, SupplyMovementEntity::class.java, app.database.supplyMovementDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.EXPENSES, ExpenseEntity::class.java, app.database.expenseDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.FEEDING, FeedingEntity::class.java, app.database.feedingDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.MORTALITY, MortalityEntity::class.java, app.database.mortalityDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.TREATMENTS, TreatmentEntity::class.java, app.database.treatmentDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.WEIGHING, WeighingEntity::class.java, app.database.weighingDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.SLAUGHTER, SlaughterEntity::class.java, app.database.slaughterDao()::insert)
            downloaded += downloadCollection(userDoc, SyncCollections.SALES, SaleEntity::class.java, app.database.saleDao()::insert)

            publishResult(
                SyncStatus.SYNCED,
                SyncResult(true, "Sincronizacion completa: $uploaded registros enviados, $downloaded registros recibidos.")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error de sincronizacion", e)
            publishResult(SyncStatus.ERROR, SyncResult(false, "Error de sincronizacion: ${e.message}"))
        }
    }

    private suspend fun uploadCollection(
        userDoc: com.google.firebase.firestore.DocumentReference,
        collectionName: String,
        records: List<Any>
    ): Int {
        if (records.isEmpty()) return 0

        val payloads = records.mapNotNull { record ->
            val data = record.toFirestoreMap()
            val id = data["id"]?.asLongString() ?: return@mapNotNull null
            id to data
        }

        payloads.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
            val batch = FirebaseFirestore.getInstance().batch()
            chunk.forEach { (id, data) ->
                batch.set(userDoc.collection(collectionName).document(id), data, SetOptions.merge())
            }
            batch.commit().await()
        }
        return payloads.size
    }

    private suspend fun uploadTombstones(
        userDoc: com.google.firebase.firestore.DocumentReference,
        tombstones: List<SyncTombstoneEntity>
    ): Int {
        if (tombstones.isEmpty()) return 0

        tombstones.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
            val batch = FirebaseFirestore.getInstance().batch()
            chunk.forEach { tombstone ->
                batch.set(
                    userDoc.collection(SyncCollections.TOMBSTONES).document(tombstone.documentKey()),
                    mapOf(
                        "collectionName" to tombstone.collectionName,
                        "documentId" to tombstone.documentId,
                        "deletedAt" to tombstone.deletedAt
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
        }
        return tombstones.size
    }

    private suspend fun downloadTombstones(
        userDoc: com.google.firebase.firestore.DocumentReference
    ): List<SyncTombstoneEntity> =
        userDoc.collection(SyncCollections.TOMBSTONES).get().await().documents.mapNotNull { document ->
            val collectionName = document.getString("collectionName") ?: return@mapNotNull null
            val documentId = document.getLong("documentId") ?: return@mapNotNull null
            val deletedAt = document.getLong("deletedAt") ?: 0L
            SyncTombstoneEntity(collectionName, documentId, deletedAt)
        }

    private suspend fun applyLocalDelete(tombstone: SyncTombstoneEntity) {
        when (tombstone.collectionName) {
            SyncCollections.BATCHES -> app.database.batchDao().deleteById(tombstone.documentId)
            SyncCollections.CLIENTS -> app.database.clientDao().deleteById(tombstone.documentId)
            SyncCollections.SUPPLIES -> app.database.supplyDao().deleteById(tombstone.documentId)
            SyncCollections.SUPPLY_MOVEMENTS -> app.database.supplyMovementDao().deleteById(tombstone.documentId)
            SyncCollections.EXPENSES -> app.database.expenseDao().deleteById(tombstone.documentId)
            SyncCollections.FEEDING -> app.database.feedingDao().deleteById(tombstone.documentId)
            SyncCollections.MORTALITY -> app.database.mortalityDao().deleteById(tombstone.documentId)
            SyncCollections.TREATMENTS -> app.database.treatmentDao().deleteById(tombstone.documentId)
            SyncCollections.WEIGHING -> app.database.weighingDao().deleteById(tombstone.documentId)
            SyncCollections.SLAUGHTER -> app.database.slaughterDao().deleteById(tombstone.documentId)
            SyncCollections.SALES -> app.database.saleDao().deleteById(tombstone.documentId)
        }
    }

    private suspend fun deleteRelatedRemoteDocuments(
        userDoc: com.google.firebase.firestore.DocumentReference,
        tombstone: SyncTombstoneEntity
    ) {
        when (tombstone.collectionName) {
            SyncCollections.BATCHES -> {
                deleteRemoteWhere(userDoc, SyncCollections.FEEDING, "loteId", tombstone.documentId)
                deleteRemoteWhere(userDoc, SyncCollections.MORTALITY, "loteId", tombstone.documentId)
                deleteRemoteWhere(userDoc, SyncCollections.TREATMENTS, "loteId", tombstone.documentId)
                deleteRemoteWhere(userDoc, SyncCollections.WEIGHING, "loteId", tombstone.documentId)
                deleteRemoteWhere(userDoc, SyncCollections.SLAUGHTER, "loteId", tombstone.documentId)
            }
            SyncCollections.SUPPLIES -> {
                deleteRemoteWhere(userDoc, SyncCollections.SUPPLY_MOVEMENTS, "insumoId", tombstone.documentId)
            }
        }
    }

    private suspend fun deleteRemoteWhere(
        userDoc: com.google.firebase.firestore.DocumentReference,
        collectionName: String,
        field: String,
        value: Long
    ) {
        val snapshot = userDoc.collection(collectionName).whereEqualTo(field, value).get().await()
        snapshot.documents.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
            val batch = FirebaseFirestore.getInstance().batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    private suspend fun <T : Any> downloadCollection(
        userDoc: com.google.firebase.firestore.DocumentReference,
        collectionName: String,
        clazz: Class<T>,
        insert: suspend (T) -> Long
    ): Int {
        val snapshot = userDoc.collection(collectionName).get().await()
        snapshot.documents.forEach { document ->
            val data = document.data.orEmpty().toMutableMap()
            document.id.toLongOrNull()?.let { data["id"] = it }
            insert(gson.fromJson(gson.toJson(data), clazz))
        }
        return snapshot.size()
    }

    private fun Any.toFirestoreMap(): Map<String, Any?> = when (this) {
        is BatchEntity -> mapOf(
            "id" to id,
            "nombre" to nombre,
            "fechaIngreso" to fechaIngreso,
            "cantidadInicial" to cantidadInicial,
            "precioPorPollito" to precioPorPollito,
            "raza" to raza,
            "galpon" to galpon,
            "proveedor" to proveedor,
            "estado" to estado,
            "observaciones" to observaciones
        )
        is ClientEntity -> mapOf(
            "id" to id,
            "nombre" to nombre,
            "telefono" to telefono,
            "direccion" to direccion,
            "saldoPendiente" to saldoPendiente,
            "observaciones" to observaciones
        )
        is SupplyEntity -> mapOf(
            "id" to id,
            "nombre" to nombre,
            "tipo" to tipo,
            "stockActual" to stockActual,
            "stockMinimo" to stockMinimo,
            "unidad" to unidad,
            "observaciones" to observaciones
        )
        is SupplyMovementEntity -> mapOf(
            "id" to id,
            "insumoId" to insumoId,
            "tipo" to tipo,
            "cantidad" to cantidad,
            "fecha" to fecha,
            "costo" to costo,
            "loteId" to loteId,
            "observaciones" to observaciones
        )
        is ExpenseEntity -> mapOf(
            "id" to id,
            "loteId" to loteId,
            "tipo" to tipo,
            "descripcion" to descripcion,
            "monto" to monto,
            "fecha" to fecha,
            "observaciones" to observaciones
        )
        is FeedingEntity -> mapOf(
            "id" to id,
            "loteId" to loteId,
            "fecha" to fecha,
            "tipo" to tipo,
            "cantidadKg" to cantidadKg,
            "cantidadSacos" to cantidadSacos,
            "costo" to costo,
            "observaciones" to observaciones
        )
        is MortalityEntity -> mapOf(
            "id" to id,
            "loteId" to loteId,
            "fecha" to fecha,
            "cantidad" to cantidad,
            "motivo" to motivo,
            "observaciones" to observaciones
        )
        is TreatmentEntity -> mapOf(
            "id" to id,
            "loteId" to loteId,
            "tipo" to tipo,
            "nombre" to nombre,
            "fechaAplicacion" to fechaAplicacion,
            "dosis" to dosis,
            "responsable" to responsable,
            "costo" to costo,
            "periodoRetiro" to periodoRetiro,
            "observaciones" to observaciones
        )
        is WeighingEntity -> mapOf(
            "id" to id,
            "loteId" to loteId,
            "fecha" to fecha,
            "edadLote" to edadLote,
            "cantidadAves" to cantidadAves,
            "pesoPromedio" to pesoPromedio,
            "observaciones" to observaciones
        )
        is SlaughterEntity -> mapOf(
            "id" to id,
            "loteId" to loteId,
            "fecha" to fecha,
            "cantidad" to cantidad,
            "pesoVivoPromedio" to pesoVivoPromedio,
            "pesoCanalPromedio" to pesoCanalPromedio,
            "merma" to merma,
            "costoSacrificio" to costoSacrificio,
            "observaciones" to observaciones
        )
        is SaleEntity -> mapOf(
            "id" to id,
            "loteId" to loteId,
            "clienteId" to clienteId,
            "fecha" to fecha,
            "tipo" to tipo,
            "modalidad" to modalidad,
            "cantidad" to cantidad,
            "precioUnitario" to precioUnitario,
            "total" to total,
            "metodoPago" to metodoPago,
            "estadoPago" to estadoPago,
            "observaciones" to observaciones
        )
        else -> emptyMap()
    }

    private fun Any.asLongString(): String? = when (this) {
        is Number -> toLong().takeIf { it > 0L }?.toString()
        is String -> toLongOrNull()?.takeIf { it > 0L }?.toString()
        else -> null
    }

    private fun SyncTombstoneEntity.documentKey(): String =
        "${collectionName}_${documentId}"

    private fun publishResult(status: SyncStatus, result: SyncResult): SyncResult {
        _syncStatus.value = status
        _lastSyncResult.value = result
        return result
    }

    private fun isConfigured(): Boolean = try {
        FirebaseApp.getApps(context).isNotEmpty()
    } catch (e: IllegalStateException) {
        false
    }

    companion object {
        private const val TAG = "FirebaseSync"
        private const val FIRESTORE_BATCH_LIMIT = 450
    }
}
