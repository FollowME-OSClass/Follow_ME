package com.oss.followMe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.oss.followMe.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

// 해쉬 키 확인에 사용하는 LIB
// import com.kakao.sdk.common.util.Utility

const val KTAG = "KakaoLog"
const val GTAG = "GoogleLog"

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity(), View.OnClickListener
{
    private lateinit var _mainBinding: ActivityMainBinding
    private val mainBinding get() = _mainBinding

    private var userInfo = ApiObject.UserInfo

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        _mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        //        해시 키 확인 용도
        //        val keyHash = Utility.getKeyHash(this)
        //        Log.i("KeyHash", keyHash)

        if (TokenManagerProvider.instance.manager.getToken() != null)
        {
            firebaseAuthWithKakao()
        }

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

    private fun firebaseAuthWithKakao()
    {
        val callback: (OAuthToken?, Throwable?) -> Unit =
            {
                    token, error ->
                if(error != null) Log.e(KTAG, "카카오 계정 로그인 실패", error)
                else if(token != null)
                {
                    UserApiClient.instance.me { user, errorAccount ->
                        val kakaoAccount = user?.kakaoAccount
                        val kakaoId = "kakao" + user?.id
                        databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child("KakaoLogin").child(kakaoId)

                        try
                        {
                            databaseReference.child("Email").setValue(kakaoAccount?.email)
                            databaseReference.child("Id").setValue(user?.id)
                            databaseReference.child("Nickname").setValue(kakaoAccount?.profile?.nickname)

                            Log.i(KTAG, "카카오톡 로그인 성공 ${token.accessToken}")
                            Log.i(KTAG, "Instance: ${UserApiClient.instance}")

                            userInfo.id = user?.id.toString()
                            userInfo.email = kakaoAccount?.email.toString()
                            userInfo.profileImg = kakaoAccount?.profile?.profileImageUrl.toString()
                            userInfo.nickname = kakaoAccount?.profile?.nickname.toString()

                            homeActivityResult()
                        }
                        catch (e: KakaoSdkError) {Log.e(KTAG, "${e.localizedMessage} / ${errorAccount?.localizedMessage}")}
                    }
                }
            }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this))
        {
            UserApiClient.instance.loginWithKakaoTalk(this, 1001, null, null, null, callback)
        }
        else UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
    }

    private fun updateUI(user: FirebaseUser?)
    {
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = Firebase.database.reference

        if (user != null) { databaseReference.child("Users").child("GoogleLogin").child(user.uid).setValue(user.email) }
    }

    override fun onClick(v: View?)
    {

        if(v != null)
        {
            when(v)
            {
                // 카카오 로그인 Activity
                mainBinding.KakaoLogin ->
                {
                    firebaseAuthWithKakao()
                }

                // 구글 로그인 Activity One tap
                mainBinding.GoogleLogin ->
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