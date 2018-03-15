package com.jgasteiz.readcomicsandroid.helpers

import android.os.AsyncTask
import com.jgasteiz.readcomicsandroid.interfaces.OnResponseFetched
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException


class GetStringResponseAsyncTask(private val mOnResponseFetched: OnResponseFetched) : AsyncTask<String, Void, String>() {

    private val client = OkHttpClient()

    private val _timeout = "timeout"
    private val _noResponseBody = "noResponseBody"
    private val _otherError = "otherError"

    override fun doInBackground(vararg params: String): String? {

        val builder = Request.Builder()
        builder.url(params[0])
        val request = builder.build()
        try {
            val response = client.newCall(request).execute()
            if (response?.body() != null) {
                return response.body()!!.string()
            }
            return this._noResponseBody
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            return this._timeout
        } catch (e: Exception) {
            e.printStackTrace()
            return this._otherError
        }
    }

    override fun onPostExecute(response: String) {
        mOnResponseFetched.callback(response)
    }
}