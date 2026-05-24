package com.pollocontrol.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Eliminar",
    dismissText: String = "Cancelar"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = onConfirm) {
                Text(confirmText, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
