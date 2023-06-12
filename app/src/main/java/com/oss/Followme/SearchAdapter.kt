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

    init
    {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
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
            itemBinding.contentsTitle.text = data.title
            itemBinding.contentsIntro.text = data.intro
            itemBinding.contentsAddress.text = data.address
            itemBinding.contentsTag.text = data.tag
            Glide.with(itemBinding.root).load(data.img).into(itemBinding.contentsImg)
        }
    }
}