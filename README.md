
# Lychee OnePay Android SDK

Lychee OnePay SDK is an Android SDK that provides an easy way to integrate payment services into your application. This SDK is distributed as an `.aar` file and supports both traditional Android development with `AppCompatActivity` and Jetpack Compose for modern UI development.

---

## Installation

To add the Lychee OnePay SDK to your Android project, follow these steps:

1. Download the `LycheeOnePaySDK-release.aar` file and place it in your project's `libs/` folder.

2. Add the following dependency to your app's `build.gradle` file:

```groovy
implementation(files("libs/LycheeOnePaySDK-release.aar"))
```

3. Make sure you have the following dependency for `AppCompat`:

```groovy
implementation 'androidx.appcompat:appcompat:<version>' // Replace <version> with the appropriate version
```

4. Set your `minSdk` version to 26 or higher in your app's `build.gradle` file:

```groovy
android {
    defaultConfig {
        minSdkVersion 26
    }
}
```

---

## Configuration

Your API keys must be stored securely in the `local.properties` file of your project. The SDK requires an API key and a secret key for initialization.

### Step 1: Store API Keys

Add the following keys to your `local.properties` file:

```
apiKey="<Your API Key>"
apiSecret="<Your Secret Key>"
```

### Step 2: Access Keys in Build Config

Modify your `build.gradle` (app module) to expose these keys in `BuildConfig`:

```groovy
android {
    ...
    defaultConfig {
        ...
        buildConfigField("String", "apiKey", localProperties['apiKey'])
        buildConfigField("String", "apiSecret", localProperties['apiSecret'])
    }
}

def localProperties = new Properties()
def localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.withInputStream { stream ->
        localProperties.load(stream)
    }
}
```

---

## Usage

### 1. Using with `AppCompatActivity`

Here's an example of how to use the SDK in a traditional Android activity:

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var paymentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the payment launcher
        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Payment was successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No Payment", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the SDK with your keys and store details
        LycheeOnePay.initialize(
            apiKey = BuildConfig.apiKey,
            secretKey = BuildConfig.apiSecret,
            storeName = "<Your Store Name>", // Pass the store name
            baseUrl = "<Your Base URL>"
        )

        // Set up a click listener for the payment button
        val payButton = findViewById<Button>(R.id.pay_button)
        val amountInput = findViewById<EditText>(R.id.amount_input)

        payButton.setOnClickListener {
            val amountText = amountInput.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toDoubleOrNull()

                if (amount != null && amount > 0) {
                    val intent = Intent(this, LycheePaymentActivity::class.java)

                    // Pass the amount as a Double using the "AMOUNT" extra key
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
```

> **Note:** The value of the payment amount **must** be passed to `LycheePaymentActivity` as a `Double` extra using the key `"AMOUNT"`. Ensure that the amount is properly validated as a `Double` before passing it, as this is required for the payment processing.

---

### 2. Using with Jetpack Compose

If you are using Jetpack Compose, here's how you can integrate the SDK:

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var paymentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the payment launcher
        paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Payment was successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No Payment", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the SDK with your keys and store details
        LycheeOnePay.initialize(
            apiKey = BuildConfig.apiKey,
            secretKey = BuildConfig.apiSecret,
            storeName = "<Your Store Name>", // Pass the store name
            baseUrl = "<Your Base URL>"
        )

        setContent {
            MyApplicationTheme {
                PaymentScreen(
                    onPayClick = { amount ->
                        val intent = Intent(this, LycheePaymentActivity::class.java)

                        // Pass the amount as a Double using the "AMOUNT" extra key
                        intent.putExtra("AMOUNT", amount)
                        paymentLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentScreen(onPayClick: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Enter Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage.isNotEmpty()
        )
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val parsedAmount = amount.toDoubleOrNull()

            if (amount.isNotEmpty() && parsedAmount != null && parsedAmount > 0) {
                errorMessage = ""
                onPayClick(parsedAmount)
            } else {
                errorMessage = "Please enter a valid amount"
            }
        }) {
            Text("Pay Now")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    PaymentScreen(onPayClick = {})
}
```

---

## Additional Parameters

- **Store Name**: The `storeName` parameter is **required** and must be passed when initializing the SDK. This will identify the store during payment transactions.

- **Store Logo** (Optional): You can optionally pass a store logo to display during the payment process. This can be set using the `storeLogoUrl` parameter during SDK initialization. Example:

```kotlin
LycheeOnePay.initialize(
    apiKey = BuildConfig.apiKey,
    secretKey = BuildConfig.apiSecret,
    storeName = "<Your Store Name>",
    storeLogoUrl = "<Your Logo URL>",  // Optional logo URL
    baseUrl = "<Your Base URL>"
)
```

---

## License

Lychee OnePay Android SDK is licensed under the [License Name]. Please refer to the `LICENSE` file for more details.
