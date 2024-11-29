package com.lychee.onepaydemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.lychee.onepay.LycheeOnePay
import com.lychee.onepay.LycheePaymentActivity

class MainActivity : AppCompatActivity() {
    private lateinit var paymentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Payment was successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No Payment", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the SDK with your keys
        LycheeOnePay.initialize(
            apiKey = BuildConfig.apiKey,
            secretKey = BuildConfig.apiSecret,
            storeName = "Mart",
            baseUrl = "http://test.lycheecoin.org"
        )

        // Find the button and EditText
        val payButton = findViewById<Button>(R.id.pay_button)
        val amountInput = findViewById<EditText>(R.id.amount_input)

        // Set up a click listener to open the payment activity
        payButton.setOnClickListener {
            // Get the amount from the EditText
            val amountText = amountInput.text.toString()

            // Check if the amount input is valid (not empty or zero)
            if (amountText.isNotEmpty()) {
                val amount = amountText.toDoubleOrNull()

                if (amount != null && amount > 0) {
                    val intent = Intent(this, LycheePaymentActivity::class.java)
                    intent.putExtra("AMOUNT", amount)
                    paymentLauncher.launch(intent)
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Amount cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

}