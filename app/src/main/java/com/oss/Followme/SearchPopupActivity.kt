package com.oss.followMe

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.Glide
import com.oss.followMe.databinding.ActivitySearchPopupBinding

class SearchPopupActivity(context: Context, _data: ThemeData) : Dialog(context)
{

    private val _searchPopupBinding:ActivitySearchPopupBinding = ActivitySearchPopupBinding.inflate(layoutInflater)
    private val searchPopupBinding get() = _searchPopupBinding

    private val dialog: Dialog = this
    private val dialogParams = dialog.window?.attributes
    private val data: ThemeData = _data

    init
    {
        setContentView(searchPopupBinding.root)
    }

    fun popupDialog()
    {
        dataInit()

        searchPopupBinding.closePopup.setOnClickListener{ dialog.dismiss() }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogParams?.width = 990
        dialogParams?.height = 1440

        dialog.window?.attributes

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun dataInit()
    {
        var tagText = "태그: "
        var phoneNumber = "연락처: "

        tagText += if(data.tag != "null") { "#" + data.tag.replace(","," #") }
        else { "X" }
        phoneNumber += if(data.phoneNumber == "--" || data.phoneNumber == "null") { "X" }
        else { data.phoneNumber.replace("-", ".") }

        Glide.with(searchPopupBinding.root).load(data.img).into(searchPopupBinding.themeImg)

        searchPopupBinding.infoName.text = data.title
        searchPopupBinding.infoIntro.text = data.intro + "\n"
        searchPopupBinding.addressText.text = "주소: " + data.address
        searchPopupBinding.tagText.text = tagText
        searchPopupBinding.phoneNumber.text = phoneNumber
    }
}