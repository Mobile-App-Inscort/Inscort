package com.example.inscort.util

import android.os.Build

object DeviceUtils {

    fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        val model = Build.MODEL
        val product = Build.PRODUCT
        val brand = Build.BRAND
        val manufacturer = Build.MANUFACTURER

        return fingerprint.startsWith("generic")
                || fingerprint.lowercase().contains("emulator")
                || model.contains("Android SDK built for x86")
                || product.contains("sdk_google")
                || product.contains("sdk_gphone")
                || product.contains("emulator")
                || (brand.startsWith("generic") && manufacturer.startsWith("generic"))
    }
}
