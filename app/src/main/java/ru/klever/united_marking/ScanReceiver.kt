package ru.klever.united_marking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.d(TAG,intent.toString())

    }
}