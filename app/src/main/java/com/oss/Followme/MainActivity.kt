package com.oss.followMe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.oss.followMe.databinding.ActivityMainBinding

// 해쉬 키 확인에 사용하는 LIB
// import com.kakao.sdk.common.util.Utility

const val KTAG = "KakaoLog"
const val GTAG = "GoogleLog"

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), View.OnClickListener
{
    private lateinit var _mainBinding: ActivityMainBinding
    private val mainBinding get() = _mainBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var startGoogleLoginForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        if(it.resultCode == RESULT_OK)
        {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

            try
            {
                val account = task.getResult(ApiException::class.java)
                Log.d(GTAG, "firebaseAutoWithGoogle: ${account.id}")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) { Log.w(GTAG, "구글 로그인 실패: ${e.localizedMessage}") }
        }
        else { Log.e(GTAG, "호출 ID 오류") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        //setContentView(R.layout.activity_main)

        //        해시 키 확인 용도
        //        val keyHash = Utility.getKeyHash(this)
        //        Log.i("KeyHash", keyHash)

        mainBinding.KakaoLogin.setOnClickListener(this)
        mainBinding.GoogleLogin.setOnClickListener(this)
    }

    private fun homeActivityResult()
    {
        val moveHomeActivity = Intent(this, HomeActivity::class.java)
        startActivity(moveHomeActivity)
    }

    private fun firebaseAuthWithGoogle(idToken: String)
    {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth = Firebase.auth
        auth.signInWithCredential(credential).addOnCompleteListener(this)
        {
            if(it.isSuccessful)
            {
                Log.d(GTAG, "signInWithCredential: 성공")
                val user = auth.currentUser
                updateUI(user)
                homeActivityResult()
            }
            else
            {
                Log.w(GTAG, "signInWithCredential: 실패", it.exception)
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?)
    {

    }

    override fun onClick(v: View?) {

        if(v != null)
        {
            when(v.id)
            {
                // 카카오 로그인 Activity
                R.id.KakaoLogin ->
                {
                    val callback: (OAuthToken?, Throwable?) -> Unit =
                        {
                            token, error ->
                            if(error != null) Log.e(KTAG, "카카오 계정 로그인 실패", error)
                            else if(token != null)
                            {
                                Log.i(KTAG, "카카오 계정 로그인 성공")
                                homeActivityResult()
                            }
                        }

                    if (UserApiClient.instance.isKakaoTalkLoginAvailable(this))
                    {
                        UserApiClient.instance.loginWithKakaoTalk(this)
                        {
                            token, error ->
                            if(error != null)
                            {
                                Log.e(KTAG, "카카오톡 로그인 실패", error)

                                if(error is ClientError && error.reason == ClientErrorCause.Cancelled) return@loginWithKakaoTalk
                            }
                            else if(token != null)
                            {
                                Log.i(KTAG, "카카오톡 로그인 성공 ${token.accessToken}")
                                homeActivityResult()
                            }
                        }
                    }
                    else UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                }

                // 구글 로그인 Activity One tap
                R.id.GoogleLogin ->
                {
                    Log.i(GTAG, "구글 로그인 버튼 확인")
                    val webClientId = "776783503426-6kj38qdt39quh29esu3udust8j8igg6t.apps.googleusercontent.com"
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId).requestEmail().build()

                    mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

                    val signInIntent = mGoogleSignInClient.signInIntent
                    startGoogleLoginForResult.launch(signInIntent)
                }
            }
       }
    }
}