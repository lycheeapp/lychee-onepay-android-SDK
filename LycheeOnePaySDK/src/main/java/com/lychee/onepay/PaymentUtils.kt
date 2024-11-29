package com.lychee.onepay

import android.content.Context
import android.provider.Settings
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Base64
import java.util.Locale
import kotlin.concurrent.thread

object PaymentUtils {
    private const val TAG = "PaymentUtils"

    fun submitPayment(
        context: Context,
        voucherCode: String,
        amount: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        thread {
            try {
                val payload = constructPayload(context, voucherCode, amount)
                val secretKey = LycheeOnePay.getSecretKey() ?: return@thread
                val signature = createSignature(payload, secretKey)
                val apiKey = LycheeOnePay.getApiKey() ?: ""
                val baseUrl = LycheeOnePay.getBaseUrl() ?: return@thread

                logDebug("Payload: $payload")
                logDebug("Signature: $signature")

                val response = createPaymentRequest(
                    baseUrl,
                    apiKey,
                    signature,
                    voucherCode,
                    amount,
                    getDeviceUdid(context)
                )

                logDebug("Response Code: ${response.first}")
                logDebug("Response Body: ${response.second}")

                if (response.first == HttpURLConnection.HTTP_OK) {
                    onSuccess()
                } else {
                    val errorMessage = handleErrorResponse(response.second, context)
                    onFailure(errorMessage)
                }
            } catch (e: Exception) {
                logDebug("Exception: ${e.message}")
                onFailure("Network failure: ${e.message}")
            }
        }
    }

    private fun handleErrorResponse(errorBody: String?, context: Context): String {
        return if (errorBody != null) {
            try {
                val jsonResponse = JSONObject(errorBody)
                val message = jsonResponse.optString("message", "Unknown error")
                return  message
            } catch (e: JSONException) {
                logDebug("JSONException: ${e.message}")
                "Invalid response format"
            }
        } else {
            "Unknown error"
        }
    }

    private fun constructPayload(context: Context, voucherCode: String, amount: Double): String {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        return "amount:$formattedAmount\$\$\$code:$voucherCode\$\$\$deviceUDID:${getDeviceUdid(context)}"
    }

    private fun getDeviceUdid(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun createPaymentRequest(
        baseUrl: String,
        apiKey: String,
        signature: String?,
        code: String,
        amount: Double,
        udid: String
    ): Pair<Int, String?> {
        val url = URL("$baseUrl/rest/lychee-v2/public/one-pay/pay")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-API-Key", apiKey)
        connection.setRequestProperty("X-Signature", signature ?: "")
        connection.setRequestProperty("X-SDK-Version", LycheeOnePay.VERSION_NAME)
        connection.setRequestProperty("Accept-Language", getCurrentAppLanguage())

        logDebug("Request Headers: ")
        connection.requestProperties.forEach { (key, value) ->
            logDebug("$key: $value")
        }

        val requestBody = """
            {
                "amount": $amount,
                "code": "$code",
                "deviceUDID": "$udid"
            }
        """.trimIndent()

        logDebug("Request Body: $requestBody")

        // Send the request
        connection.outputStream.use { os ->
            val input = requestBody.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }

        // Read the response
        val responseCode = connection.responseCode
        val response = if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use(BufferedReader::readText)
        } else {
            connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
        }

        return responseCode to response
    }

    private fun createSignature(payload: String, secretKey: String): String {
        return try {
            val data = secretKey + payload
            val sha256Digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = sha256Digest.digest(data.toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(hashBytes)
        } catch (e: NoSuchAlgorithmException) {
            logDebug("NoSuchAlgorithmException: ${e.message}")
            throw RuntimeException("Error creating signature", e)
        }
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    private fun getCurrentAppLanguage(): String {
        val currentLanguage = Locale.getDefault().language
        return when (currentLanguage) {
            "en" -> "en-US"
            "ar" -> "ar-EG"
            else -> "ar-EG"
        }
    }
}