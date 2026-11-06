package com.friday.assistant

import android.app.Application
import com.friday.assistant.di.AppContainer

/**
 * Application entry point. Owns the single [AppContainer] used for manual dependency
 * injection across the app.
 */
class FridayApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
