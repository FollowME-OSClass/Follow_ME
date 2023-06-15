package com.oss.followMe

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.database.*
import com.oss.followMe.databinding.ActivityHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : ComponentActivity(), View.OnClickListener
{
    private lateinit var _binding: ActivityHomeBinding
    private val binding get() = _binding
    private var theme = ApiObject.Theme
    private var btnGravity = true
    private var id: Int = 1

    private var pressTime: Long = 0
    private val finishedTime: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.themeTravel.setOnClickListener(this)
        binding.searchTravel.setOnClickListener(this)
        binding.myInfoTravel.setOnClickListener(this)
    }

    override fun onClick(v: View?)
    {
        if(v != null)
        {
            when(v)
            {
                binding.searchTravel ->
                {
                    val moveSearchActivity = Intent(this, SearchActivity::class.java)
                    startActivity(moveSearchActivity)
                }

                binding.themeTravel ->
                {
                    binding.themeTravel.isClickable = false
                    // 주소
                    val database = FirebaseDatabase.getInstance().reference.child("Travel")
                    database.addListenerForSingleValueEvent(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            val date = Date(System.currentTimeMillis() - (System.currentTimeMillis() / 1000 / 60 / 60 * 3))
                            val dateFormatDay = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(date)
                            val dbTime = snapshot.child("DailyTime").value.toString()
                            val dataSize = snapshot.childrenCount - 2
                            var cId = ""

                            if(dbTime.toInt() < dateFormatDay.toInt()) { database.child("DailyTime").setValue(dateFormatDay) }

                            val dataNum = snapshot.child("DailyTime").value.toString()

                            for(data in snapshot.children)
                            {
                                if(dataNum.toInt() % dataSize == data.child("Number").value) { database.child("DailyTheme").setValue(data.key) }
                            }

                            cId = snapshot.child("DailyTheme").value.toString()
                            val snapShot = snapshot.child(cId)

                            theme.intro = snapShot.child("Intro").value.toString()
                            theme.themeName = snapShot.child("ThemeName").value.toString()
                            theme.warning = snapShot.child("Warning").value.toString()

                            createView("theme", "null", cId, 0)

                            for(data in snapShot.children)
                            {
                                if(data.value!!.javaClass.name != String::class.java.name && data.key != "Number")
                                {
                                    createView("leafBtn", data.key, cId, id)
                                    id++
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) { Log.e("Firebase Error", "Firebase data read error ${error.toException()}") }
                    })
                }

                binding.myInfoTravel ->
                {
                    val moveMyInfoActivity = Intent(this, MyInfoActivity::class.java)
                    startActivity(moveMyInfoActivity)
                }
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

    private fun createView(viewName: String, themeName: String?, themeCode: String?, id: Int)
    {
        when(viewName)
        {
            "theme" ->
            {
                Log.i("themeTitleView", "테마 뷰 생성")

                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val intro = TextView(applicationContext)
                var text  = theme.themeName  + "\n\n" +
                            theme.intro      + "\n\n"

                param.topMargin = 20
                param.bottomMargin = 20

                if(theme.warning != "Null") { text += theme.warning + "\n" }

                intro.gravity = Gravity.CENTER

                intro.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
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

                param.topMargin = 50
                param.bottomMargin = 50

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