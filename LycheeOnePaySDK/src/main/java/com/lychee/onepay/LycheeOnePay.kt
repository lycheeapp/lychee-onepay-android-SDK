package com.lychee.onepay

import android.graphics.drawable.Drawable

object LycheeOnePay {
    const val VERSION_NAME = "1.0.0"
    private var apiKey: String? = null
    private var secretKey: String? = null
    private var storeName: String? = null
    private var storeLogo: Drawable? = null
    private var baseUrl: String? = null
    private var isInitialized = false

    fun initialize(apiKey: String, secretKey: String, storeName: String, baseUrl: String, storeLogo: Drawable? = null) {
        this.apiKey = apiKey
        this.secretKey = secretKey
        this.storeName = storeName
        this.storeLogo = storeLogo
        this.baseUrl = baseUrl
        isInitialized = true
    }

    private fun checkInitialization() {
        if (!isInitialized) {
            throw IllegalStateException("LycheeOnePay is not initialized. Call initialize() first.")
        }
    }

    fun getApiKey(): String? {
        checkInitialization()
        return apiKey
    }

    fun getSecretKey(): String? {
        checkInitialization()
        return secretKey
    }

    fun getStoreName(): String? {
        checkInitialization()
        return storeName
    }

    fun getStoreLogo(): Drawable? {
        checkInitialization()
        return storeLogo
    }

    fun getBaseUrl(): String? {
        checkInitialization()
        return baseUrl
    }
}