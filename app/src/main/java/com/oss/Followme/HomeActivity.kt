package com.oss.followMe

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.database.*
import com.oss.followMe.databinding.ActivityHomeBinding

class HomeActivity : ComponentActivity(), View.OnClickListener
{
    private lateinit var _binding: ActivityHomeBinding
    private val binding get() = _binding
    private var theme = ApiObject.Theme
    private var btnGravity = true
    private var id: Int = 1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testThemeTravel.setOnClickListener(this)
    }
    override fun onClick(v: View?)
    {
        if(v != null)
        {
            when(v.id)
            {
                R.id.testThemeTravel ->
                {
                    // 주소
                    val database = FirebaseDatabase.getInstance().reference.child("Travel").child("CNTS_000000000022466")

                    database.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            theme.intro = snapshot.child("Intro").value.toString()
                            theme.themeName = snapshot.child("ThemeName").value.toString()
                            theme.warning = snapshot.child("Warning").value.toString()

                            createView("theme", "null", "CNTS_000000000022466", 0)

                            for(data in snapshot.children)
                            {
                                if(data.value!!.javaClass.name != String::class.java.name)
                                {
                                    createView("leafBtn", data.key, "CNTS_000000000022466", id)
                                    id++
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) { Log.e("Firebase Error", "Firebase data read error ${error.toException()}") }
                    })
                }
            }
        }

    }

    private fun createView(viewName: String, themeName: String?, themeCode: String?, id: Int)
    {
        when(viewName)
        {
            "theme" ->
            {
                Log.i("themeTitleView", "테마 뷰 생성")

                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val intro = TextView(applicationContext)
                val text  = theme.intro      + "\n\n" +
                            theme.themeName  + "\n\n" +
                            theme.warning    + "\n"

                intro.gravity = Gravity.CENTER

                intro.text = text
                intro.textSize = 11f
                intro.setTypeface(null, Typeface.BOLD)
                intro.id = 0
                intro.layoutParams = param
                intro.setTextColor(Color.argb(255, 0, 0, 0))
                binding.homeLinear.addView(intro)
            }

            "leafBtn" ->
            {
                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val leafBtn  = Button(applicationContext)

                if(btnGravity)
                {
                    param.gravity = Gravity.START
                    btnGravity = false
                }
                else
                {
                    param.gravity = Gravity.END
                    btnGravity = true
                }

                leafBtn.text = themeName
                leafBtn.textSize = 11f
                leafBtn.id = id
                leafBtn.layoutParams = param

                leafBtn.setOnClickListener{

                    val showPopupDialog = PopupActivity(this, themeName, themeCode)
                    showPopupDialog.popupDialog()
                }

                binding.homeLinear.addView(leafBtn)
            }
        }
    }
}