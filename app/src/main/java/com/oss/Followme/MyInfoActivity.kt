package com.oss.followMe

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.oss.followMe.databinding.ActivityMyInfoBinding

class MyInfoActivity : ComponentActivity(), View.OnClickListener
{

    private lateinit var _myInfoBinding: ActivityMyInfoBinding
    private val myInfoBinding get() = _myInfoBinding

    private val userInfo = ApiObject.UserInfo

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