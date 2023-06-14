package com.oss.followMe

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.oss.followMe.databinding.ActivityMyInfoBinding

class MyInfoActivity : ComponentActivity(), View.OnClickListener
{

    private lateinit var _myInfoBinding: ActivityMyInfoBinding
    private val myInfoBinding get() = _myInfoBinding

    private val userInfo = ApiObject.UserInfo

    private var pressTime: Long = 0
    private val finishedTime: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _myInfoBinding = ActivityMyInfoBinding.inflate(layoutInflater)
        setContentView(myInfoBinding.root)

        myInfoBinding.searchTravel.setOnClickListener(this)
        myInfoBinding.themeTravel.setOnClickListener(this)
        myInfoBinding.myInfoTravel.isClickable = false

        infoInit()
    }

    override fun onClick(v: View?)
    {
        when(v)
        {
            myInfoBinding.searchTravel ->
            {
                val moveSearchActivity = Intent(this, SearchActivity::class.java)
                startActivity(moveSearchActivity)
            }

            myInfoBinding.themeTravel ->
            {
                val moveHomeActivity = Intent(this, HomeActivity::class.java)
                startActivity(moveHomeActivity)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed()
    {
        val checkTime = System.currentTimeMillis()
        val intervalTime = checkTime - pressTime

        if(intervalTime in 0..finishedTime)
        {
            moveTaskToBack(true)
            finish()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        else
        {
            pressTime = checkTime
            Toast.makeText(applicationContext, "한번 더 누르면 앱이 종료됩니다", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun infoInit()
    {
        Glide.with(myInfoBinding.root).load(userInfo.profileImg).into(myInfoBinding.profileImg)
        myInfoBinding.profileImg.clipToOutline = true
        myInfoBinding.profileNickname.text = "이름: " + userInfo.nickname
        myInfoBinding.profileNickname.setTextColor(Color.argb(255, 0, 0, 0))
        myInfoBinding.profileEmail.text = "이메일: " + userInfo.email
        myInfoBinding.profileEmail.setTextColor(Color.argb(255, 0, 0, 0))
    }
}