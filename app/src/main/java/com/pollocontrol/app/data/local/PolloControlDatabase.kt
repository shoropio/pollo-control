/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pollocontrol.app.data.local.converter.DateConverter
import com.pollocontrol.app.data.local.dao.*
import com.pollocontrol.app.data.local.entity.*

@Database(
    entities = [
        BatchEntity::class,
        MortalityEntity::class,
        SupplyEntity::class,
        SupplyMovementEntity::class,
        FeedingEntity::class,
        ExpenseEntity::class,
        TreatmentEntity::class,
        WeighingEntity::class,
        SlaughterEntity::class,
        SaleEntity::class,
        ClientEntity::class,
        SyncTombstoneEntity::class,
        EggProductionEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class PolloControlDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun mortalityDao(): MortalityDao
    abstract fun supplyDao(): SupplyDao
    abstract fun supplyMovementDao(): SupplyMovementDao
    abstract fun feedingDao(): FeedingDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun weighingDao(): WeighingDao
    abstract fun slaughterDao(): SlaughterDao
    abstract fun saleDao(): SaleDao
    abstract fun clientDao(): ClientDao
    abstract fun syncTombstoneDao(): SyncTombstoneDao
    abstract fun eggProductionDao(): EggProductionDao

    companion object {
        @Volatile
        private var INSTANCE: PolloControlDatabase? = null

        fun getDatabase(context: Context): PolloControlDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PolloControlDatabase::class.java,
                    "pollocontrol_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_tombstones (
                        collectionName TEXT NOT NULL,
                        documentId INTEGER NOT NULL,
                        deletedAt INTEGER NOT NULL,
                        PRIMARY KEY(collectionName, documentId)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS lotes_codornices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        fechaIngreso INTEGER NOT NULL,
                        cantidadInicial INTEGER NOT NULL,
                        precioUnitario REAL NOT NULL,
                        raza TEXT NOT NULL,
                        galpon TEXT NOT NULL,
                        proveedor TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        observaciones TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lotes_codornices_estado ON lotes_codornices (estado)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lotes_codornices_fechaIngreso ON lotes_codornices (fechaIngreso)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_quail_estado_fecha ON lotes_codornices (estado, fechaIngreso)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS produccion_huevos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        loteCodonizId INTEGER NOT NULL,
                        fecha INTEGER NOT NULL,
                        cantidadHuevos INTEGER NOT NULL,
                        pesoPromedio REAL NOT NULL,
                        calidadA INTEGER NOT NULL,
                        calidadB INTEGER NOT NULL,
                        rechazos INTEGER NOT NULL,
                        observaciones TEXT NOT NULL,
                        FOREIGN KEY(loteCodonizId) REFERENCES lotes_codornices(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_produccion_huevos_loteCodonizId ON produccion_huevos (loteCodonizId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_produccion_huevos_fecha ON produccion_huevos (fecha)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_eggs_fecha_lote ON produccion_huevos (fecha, loteCodonizId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lotes ADD COLUMN especie TEXT NOT NULL DEFAULT 'POLLO'")
                db.execSQL("ALTER TABLE lotes ADD COLUMN proposito TEXT NOT NULL DEFAULT 'CARNE'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO lotes (
                        id, nombre, fechaIngreso, cantidadInicial, especie, proposito,
                        precioPorPollito, raza, galpon, proveedor, estado, observaciones
                    )
                    SELECT
                        id, nombre, fechaIngreso, cantidadInicial, 'CODORNIZ', 'HUEVOS',
                        precioUnitario, raza, galpon, proveedor, estado, observaciones
                    FROM lotes_codornices
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS produccion_huevos_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        loteId INTEGER NOT NULL,
                        fecha INTEGER NOT NULL,
                        cantidadHuevos INTEGER NOT NULL,
                        pesoPromedio REAL NOT NULL,
                        calidadA INTEGER NOT NULL,
                        calidadB INTEGER NOT NULL,
                        rechazos INTEGER NOT NULL,
                        observaciones TEXT NOT NULL,
                        FOREIGN KEY(loteId) REFERENCES lotes(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO produccion_huevos_new (
                        id, loteId, fecha, cantidadHuevos, pesoPromedio, calidadA, calidadB, rechazos, observaciones
                    )
                    SELECT
                        id, loteCodonizId, fecha, cantidadHuevos, pesoPromedio, calidadA, calidadB, rechazos, observaciones
                    FROM produccion_huevos
                    WHERE EXISTS (SELECT 1 FROM lotes WHERE lotes.id = produccion_huevos.loteCodonizId)
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE produccion_huevos")
                db.execSQL("ALTER TABLE produccion_huevos_new RENAME TO produccion_huevos")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_produccion_huevos_loteId ON produccion_huevos (loteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_produccion_huevos_fecha ON produccion_huevos (fecha)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_eggs_fecha_lote ON produccion_huevos (fecha, loteId)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS lotes_codornices")
                db.execSQL("DELETE FROM sync_tombstones WHERE collectionName = 'lotes_codornices'")
            }
        }
    }
}
