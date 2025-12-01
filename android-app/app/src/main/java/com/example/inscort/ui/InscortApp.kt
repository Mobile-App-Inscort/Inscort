package com.example.inscort

import android.app.Application
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import com.example.inscort.BuildConfig
import com.kakao.vectormap.KakaoMapSdk
import java.security.MessageDigest // [해결] 이게 없어서 에러난 겁니다!

class InscortApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. 지도 SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // 2. 키 해시 확인
        val keyHash = getKeyHash()
        Log.d("KeyHash", "KeyHash: $keyHash")
    }

    private fun getKeyHash(): String? {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            // [해결] for문 대신 forEach를 써서 iterator 모호함 에러 해결
            info.signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                return hash
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "키 해시 에러: ${e.message}")
        }
        return null
    }
}