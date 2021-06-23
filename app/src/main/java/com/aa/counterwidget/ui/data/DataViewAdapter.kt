package com.aa.counterwidget.ui.data

import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aa.counterwidget.TsColorItem
import com.aa.counterwidget.databinding.FragmentDataBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat


class DataViewAdapter(
    private var values: List<TsColorItem>, private val bgColor: Int
) : RecyclerView.Adapter<DataViewAdapter.ViewHolder>() {

    private var dateFormat = SimpleDateFormat("dd hh:mm")
    private val defaultBackground = bgColor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentDataBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun setData(data: List<TsColorItem>) {
        values = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = String.format("%1$2s", position + 1)
        holder.contentView.text = dateFormat.format(item.date)
        holder.colorsView.text = ""

        for (color in item.colors) {
            val word: Spannable = SpannableString(" ")

            word.setSpan(BackgroundColorSpan(color), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.colorsView.append(word)

            word.setSpan(defaultBackground, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.colorsView.append(word)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentDataBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        val colorsView: TextView = binding.colors

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}