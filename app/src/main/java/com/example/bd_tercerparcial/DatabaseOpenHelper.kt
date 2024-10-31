package com.example.bd_tercerparcial

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "appointment.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"

        private const val CREATE_TABLE="""
        CREATE TABLE $TABLE_NAME(
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT,
            $COLUMN_PHONE TEXT NOT NULL,
            $COLUMN_DATE TEXT NOT NULL,
            $COLUMN_TIME TEXT NOT NULL
            )    
        """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertAppointment(name: String, phone: String, date: String, time: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_DATE, date)
            put(COLUMN_TIME, time)
        }

        try {
            val result = db.insert(TABLE_NAME, null, values)
            db.close()
            return result != -1L
        } catch (e: Exception) {
            db.close()
            return false
        }
    }

    fun getAllAppointments(): List<Map<String, String>> {
        val appointmentsList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_DATE, COLUMN_TIME), null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val appointment = mapOf(
                    COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString(),
                    COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    COLUMN_PHONE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    COLUMN_DATE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    COLUMN_TIME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                )
                appointmentsList.add(appointment)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return appointmentsList
    }

    fun getAppointmentById(id: Int): Map<String, String> {
        val appointmentsList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_DATE, COLUMN_TIME), "$COLUMN_ID=?", arrayOf(id.toString()), null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val appointment = mapOf(
                    COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString(),
                    COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    COLUMN_PHONE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    COLUMN_DATE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    COLUMN_TIME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                )
                appointmentsList.add(appointment)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return appointmentsList.getOrNull(0) ?: emptyMap()
    }

    fun getByDateAndTime(date: String, time: String): Map<String, String> {
        val appointmentsList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_DATE, COLUMN_TIME), "$COLUMN_DATE=? AND $COLUMN_TIME=?", arrayOf(date, time), null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val appointment = mapOf(
                    COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString(),
                    COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    COLUMN_PHONE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    COLUMN_DATE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    COLUMN_TIME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                )
                appointmentsList.add(appointment)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return appointmentsList.getOrNull(0) ?: emptyMap()
    }

    fun deleteAppointment(id: Int): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
            db.close()
            result > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun updateAppointment(id: Int, name: String, phone: String, date: String, time: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_DATE, date)
            put(COLUMN_TIME, time)
        }
        return try {
            val result = db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(id.toString()))
            db.close()
            result > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }
}
