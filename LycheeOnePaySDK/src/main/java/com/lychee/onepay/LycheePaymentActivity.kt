package com.lychee.onepay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsController
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import java.util.Locale

class LycheePaymentActivity : AppCompatActivity() {
    private lateinit var toolbarCustom: Toolbar
    private lateinit var textViewToolbarTitle: TextView
    private lateinit var editTextVoucherCode: EditText
    private lateinit var textViewVoucherError: TextView
    private lateinit var buttonPayNow: Button
    private lateinit var textViewPaymentAmount: TextView
    private lateinit var textViewStoreName: TextView
    private lateinit var imageViewStoreLogo: ImageView
    private lateinit var textViewNoVoucher: TextView
    private lateinit var paymentSuccessLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lychee_payment)

        toolbarCustom = findViewById(R.id.toolbar_custom)
        textViewToolbarTitle = findViewById(R.id.text_view_toolbar_title)
        editTextVoucherCode = findViewById(R.id.edit_text_voucher_code)
        textViewVoucherError = findViewById(R.id.text_view_voucher_error)
        buttonPayNow = findViewById(R.id.button_pay_now)
        textViewPaymentAmount = findViewById(R.id.text_view_payment_amount)
        textViewStoreName = findViewById(R.id.text_view_store_name)
        imageViewStoreLogo = findViewById(R.id.image_view_store_logo)
        textViewNoVoucher = findViewById(R.id.text_view_no_voucher)

        setupStatusBar()
        setupToolbar()
        setupVoucherCodeListener(this)
        displayStoreInfo()
        setupNoVoucherAction()
        setupPaymentSuccessLauncher()
        setupPayButton()
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

    private fun setupToolbar() {
        setSupportActionBar(toolbarCustom)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarCustom.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupVoucherCodeListener(context: Context) {
        editTextVoucherCode.filters += InputFilter.AllCaps()
        editTextVoucherCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val voucherCode = s.toString()

                editTextVoucherCode.setBackgroundResource(R.drawable.edittext_border)
                textViewVoucherError.visibility = View.GONE

                if (voucherCode.length >= 6) {
                    editTextVoucherCode.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, ContextCompat.getDrawable(context, R.drawable.onepay_2), null
                    )
                    buttonPayNow.isEnabled = true
                    buttonPayNow.setBackgroundResource(R.drawable.button_rounded)
                } else {
                    editTextVoucherCode.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(context, R.drawable.one_pay_disabled),
                        null
                    )
                    buttonPayNow.isEnabled = false
                    buttonPayNow.setBackgroundResource(R.drawable.button_rounded_disabled)
                }
            }
        })
    }

    private fun displayStoreInfo() {
        val amount = intent.getDoubleExtra("AMOUNT", Double.NaN)
        if (!amount.isNaN()) {
            val formattedAmount = String.format(Locale.US, "%.2f", amount)
            val amountWithCurrency = "ILS $formattedAmount"

            textViewPaymentAmount.text = amountWithCurrency
        } else {
            throw IllegalArgumentException("Invalid or missing payment amount")
        }

        val storeLogo = LycheeOnePay.getStoreLogo()
        if (storeLogo != null) {
            imageViewStoreLogo.setImageDrawable(storeLogo)
        }
        textViewStoreName.text = LycheeOnePay.getStoreName()
    }

    private fun setupNoVoucherAction() {
        textViewNoVoucher.setOnClickListener {
            LycheeAppUtils.openLycheeAppOrWebsite(this)
        }
    }

    private fun setupPaymentSuccessLauncher() {
        paymentSuccessLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setupPayButton() {
        buttonPayNow.setOnClickListener {
            val voucherCode = editTextVoucherCode.text.toString()
            val amount = intent.getDoubleExtra("AMOUNT", 0.0)
            LycheeAppUtils.showLycheeLoader(this)
            PaymentUtils.submitPayment(
                context = this,
                voucherCode = voucherCode,
                amount = amount,
                onSuccess = {
                    runOnUiThread {
                        LycheeAppUtils.hideLycheeLoader()
                        val formattedAmount = "ILS %.2f".format(amount)
                        val intent = Intent(this, PaymentSuccessActivity::class.java)
                        intent.putExtra("PAYMENT_AMOUNT", formattedAmount)
                        paymentSuccessLauncher.launch(intent)
                    }
                },
                onFailure = { errorMessage ->
                    runOnUiThread {
                        LycheeAppUtils.hideLycheeLoader()
                        showErrorState(errorMessage)
                    }
                }
            )
        }
    }

    private fun showErrorState(error: String?) {
        editTextVoucherCode.setBackgroundResource(R.drawable.edittext_border_error)
        textViewVoucherError.visibility = View.VISIBLE
        textViewVoucherError.text = error
    }
}