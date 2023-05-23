package com.oss.followMe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.oss.followMe.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity()
{
    private lateinit var _binding: ActivityHomeBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}