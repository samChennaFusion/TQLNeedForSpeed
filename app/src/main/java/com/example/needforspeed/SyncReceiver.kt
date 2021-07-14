package com.example.needforspeed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
            val stepsCount = StepsCount(steps = 12,date = "12 / 04")
            AppDatabase.getInstance(context).userDao().insertToRoomDatabase(stepsCount)
        }
    }
}