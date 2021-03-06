package com.july.viewlibrary.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.july.viewlibrary.R

/**
 * author : Hyt
 * time : 2020/07/30
 * version : 1.0
 *
 */
class RecyclerAdapter(layoutResId:Int,data: MutableList<String>):
    BaseQuickAdapter<String,BaseViewHolder>(layoutResId,data) {

    override fun convert(holder: BaseViewHolder, item: String) {


        holder.setText(R.id.searchText,data[holder.layoutPosition])
    }
}