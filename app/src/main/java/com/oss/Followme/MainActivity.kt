package com.oss.Followme

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.util.Utility

const val KTAG = "KakaoLog"
public class MainActivity : AppCompatActivity(), View.OnClickListener
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val kakaoLogin = findViewById<ImageButton>(R.id.KakaoLogin)
        kakaoLogin.setOnClickListener(this)


        val keyHash = Utility.getKeyHash(this)

        Log.i("KeyHash", keyHash)
    }

    override fun onClick(v: View?) {

        if(v != null)
        {
            when(v.id)
            {
                R.id.KakaoLogin->
                {
                    Log.i(KTAG, "버튼 테스트")
                    val callback: (OAuthToken?, Throwable?) -> Unit =
                        {
                            token, error ->
                            if(error != null) Log.e(KTAG, "카카오 계정으로 로그인 실패", error)
                            else if(token != null) Log.i(KTAG, "카카오 계정으로 로그인 성공")
                        }

                    if(UserApiClient.instance.isKakaoTalkLoginAvailable(this))
                    {
                        UserApiClient.instance.loginWithKakaoTalk(this)
                        {
                            token, error ->
                            if(error != null)
                            {
                                Log.e(KTAG, "카카오톡으로 로그인 실패", error)

                                if(error is ClientError && error.reason == ClientErrorCause.Cancelled) return@loginWithKakaoTalk
                            }
                            else if(token != null) Log.i(KTAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                        }
                    }
                    else UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                }
            }
        }

    }
}