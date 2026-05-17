package com.pollocontrol.app.data.local

import android.content.Context
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

object BackupManager {

    private const val DB_NAME = "pollocontrol_database"

    fun backup(context: Context, outputStream: OutputStream) {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) throw FileNotFoundException("Base de datos no encontrada")

        dbFile.inputStream().use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    fun restore(context: Context, inputStream: InputStream) {
        PolloControlDatabase.closeDatabase()

        val dbPath = context.getDatabasePath(DB_NAME)
        dbPath.parentFile?.mkdirs()

        inputStream.use { input ->
            dbPath.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val walFile = context.getDatabasePath("$DB_NAME-wal")
        val shmFile = context.getDatabasePath("$DB_NAME-shm")
        if (walFile.exists()) walFile.delete()
        if (shmFile.exists()) shmFile.delete()
    }
}
