package com.lychee.onepay

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var textPaymentMessage: TextView
    private lateinit var buttonDone: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        textPaymentMessage = findViewById(R.id.text_payment_message)
        buttonDone = findViewById(R.id.button_done)

        setupStatusBar()

        val paymentAmount = intent.getStringExtra("PAYMENT_AMOUNT")
        textPaymentMessage.text = getString(R.string.your_payment_of_has_been_successfully_done, paymentAmount)

        buttonDone.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}