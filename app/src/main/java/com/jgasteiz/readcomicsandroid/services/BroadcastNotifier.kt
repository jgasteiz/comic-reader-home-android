package com.jgasteiz.readcomicsandroid.services

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.jgasteiz.readcomicsandroid.helpers.Constants


/**
 * Creates a BroadcastNotifier containing an instance of LocalBroadcastManager.
 * LocalBroadcastManager is more efficient than BroadcastManager; because it only
 * broadcasts to components within the app, it doesn't have to do parceling and so forth.
 *
 * @param context a Context from which to get the LocalBroadcastManager
 */
class BroadcastNotifier (context: Context) {

    private val mBroadcaster: LocalBroadcastManager

    init {

        // Gets an instance of the support library local broadcastmanager
        mBroadcaster = LocalBroadcastManager.getInstance(context)

    }

    /**
     *
     * Uses LocalBroadcastManager to send an [Intent] containing `status`. The
     * [Intent] has the action `BROADCAST_ACTION` and the category `DEFAULT`.
     *
     * @param status [Integer] denoting a work request status
     */
    fun broadcastIntentWithState(status: Int) {

        val localIntent = Intent()

        // The Intent contains the custom broadcast action for this app
        localIntent.action = Constants.BROADCAST_ACTION

        // Puts the status into the Intent
        localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, status)
        localIntent.addCategory(Intent.CATEGORY_DEFAULT)

        // Broadcasts the Intent
        mBroadcaster.sendBroadcast(localIntent)

    }

    /**
     * Uses LocalBroadcastManager to send an [String] containing a logcat message.
     * [Intent] has the action `BROADCAST_ACTION` and the category `DEFAULT`.
     *
     * @param logData a [String] to insert into the log.
     */
    fun notifyProgress(logData: String) {

        val localIntent = Intent()

        // The Intent contains the custom broadcast action for this app
        localIntent.action = Constants.BROADCAST_ACTION

        localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, -1)

        // Puts log data into the Intent
        localIntent.putExtra(Constants.EXTENDED_STATUS_LOG, logData)
        localIntent.addCategory(Intent.CATEGORY_DEFAULT)

        // Broadcasts the Intent
        mBroadcaster.sendBroadcast(localIntent)

    }
}
