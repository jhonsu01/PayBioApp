package com.local.paybio

import android.app.Application
import net.sqlcipher.database.SQLiteDatabase

class PayBioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load SQLCipher native libraries once before any DB access.
        SQLiteDatabase.loadLibs(this)
    }
}
