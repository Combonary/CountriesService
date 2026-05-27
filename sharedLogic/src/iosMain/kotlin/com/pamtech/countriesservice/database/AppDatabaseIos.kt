package com.pamtech.countriesservice.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/countries.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}
