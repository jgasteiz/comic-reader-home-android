package com.jgasteiz.readcomicsandroid.helpers

import android.os.AsyncTask
import com.jgasteiz.readcomicsandroid.interfaces.OnResponseFetched
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException


class GetStringResponseAsyncTask(private val mOnResponseFetched: OnResponseFetched) : AsyncTask<String, Void, String>() {

    companion object {
        val TIMEOUT = "TIMEOUT"
        val NO_RESPONSE_BODY = "NO_RESPONSE_BODY"
        val OTHER_ERROR = "OTHER_ERROR"
    }

    private val client = OkHttpClient()


    override fun doInBackground(vararg params: String): String? {

        val builder = Request.Builder()
        builder.url(params[0])
        val request = builder.build()
        try {
            val response = client.newCall(request).execute()
            if (response?.body() != null) {
                return response.body()!!.string()
            }
            return NO_RESPONSE_BODY
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            return TIMEOUT
        } catch (e: Exception) {
            e.printStackTrace()
            return OTHER_ERROR
        }
    }

    override fun onPostExecute(response: String) {
        mOnResponseFetched.callback(response)
    }
}