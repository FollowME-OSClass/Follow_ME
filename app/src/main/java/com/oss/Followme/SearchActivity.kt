package com.oss.followMe

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.oss.followMe.databinding.ActivitySearchBinding

class SearchActivity : ComponentActivity(), View.OnClickListener
{

    private lateinit var _searchBinding: ActivitySearchBinding
    private val searchBinding get() = _searchBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        _searchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(searchBinding.root)

        searchBinding.searchTravel.isClickable = false
        searchBinding.themeTravel.setOnClickListener(this)
        searchBinding.myInfoTravel.setOnClickListener(this)

    }

    override fun onClick(v: View?)
    {
        if(v != null)
        {
            when(v.id)
            {
                R.id.themeTravel ->
                {
                    val moveHomeActivity = Intent(this, HomeActivity::class.java)
                    startActivity(moveHomeActivity)
                }

                R.id.myInfoTravel ->
                {

                }
            }
        }
    }

}