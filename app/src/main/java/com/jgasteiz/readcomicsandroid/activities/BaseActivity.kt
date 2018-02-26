package com.jgasteiz.readcomicsandroid.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.jgasteiz.readcomicsandroid.services.DownloadsService

abstract class BaseActivity : AppCompatActivity() {

    abstract val hasRemovableItems: Boolean

    var mService: DownloadsService? = null
    private var mBound = false

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, DownloadsService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as DownloadsService.LocalBinder
            mService = binder.getService
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

}