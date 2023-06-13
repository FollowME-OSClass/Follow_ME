package com.oss.followMe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oss.followMe.databinding.SearchItemBinding

class SearchAdapter: RecyclerView.Adapter<SearchAdapter.ViewHolder>()
{
    private lateinit var _itemBinding: SearchItemBinding
    private val itemBinding get() = _itemBinding

    var data = mutableListOf<ThemeData>()
    private lateinit var adapterParent: ViewGroup
    init
    {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        adapterParent = parent
        _itemBinding = SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding.root)
    }

    override fun getItemCount(): Int
    {
        return data.size
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int
    {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.dataBinding(data[position])
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        fun dataBinding(data: ThemeData)
        {
            var titleText:      String = "관광지: " + data.title
            var introText:      String = "소개: " + data.intro
            var addressText:    String = "주소: " + data.address
            var tagText:        String = "태그: " + data.tag

            if (data.title.length > 18) { titleText = "관광지: " + data.title.substring(0 until 18) + "..."}
            if (data.intro.length > 18) { introText = "소개: " + data.intro.substring(0 until 18) + "..."}
            if (data.address.length > 18) { addressText = "주소: " + data.address.substring(0 until 18) + "..."}
            if (data.tag.length > 18) { tagText = "태그: " + data.tag.substring(0 until 18) + "..."}

            itemBinding.contentsTitle.text = titleText
            itemBinding.contentsIntro.text = introText
            itemBinding.contentsAddress.text = addressText
            itemBinding.contentsTag.text = tagText
            Glide.with(itemBinding.root).load(data.img).into(itemBinding.contentsImg)

            itemBinding.searchItemBtn.setOnClickListener{
                val showSearchPopup = SearchPopupActivity(adapterParent.context, data)
                showSearchPopup.popupDialog()
            }
        }

    }
}