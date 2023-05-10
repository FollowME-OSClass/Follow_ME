package com.FollowMe.project

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application()
{
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "652680317a3f2a98bb6ab7132a8bd4c2")
    }
}