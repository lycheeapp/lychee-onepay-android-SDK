# Keep the PaymentUtils class and its public methods in consumer apps
# Keep the PaymentUtils class and all its methods
-keep class com.lychee.onepay.PaymentUtils {
    public *;
}

# Keep the PaymentUtils class and all its methods
-keep class com.lychee.onepay.LycheeAppUtils {
    public *;
}

# Keep the PaymentUtils class and all its methods
-keep class com.lychee.onepay.LycheeOnePay {
    public *;
}

-keep class com.lychee.onepay.PaymentUtils{
    public *;
}

-keep class **.R$style { *; }
-keep class **.R$attr { *; }
