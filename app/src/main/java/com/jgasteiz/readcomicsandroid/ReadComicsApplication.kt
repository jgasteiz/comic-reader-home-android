package com.jgasteiz.readcomicsandroid

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class ReadComicsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this);
    }
}