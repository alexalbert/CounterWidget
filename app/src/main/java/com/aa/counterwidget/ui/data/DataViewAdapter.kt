package com.aa.counterwidget.ui.data

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aa.counterwidget.CounterWidget
import com.aa.counterwidget.TsColorItem
import com.aa.counterwidget.TsDataUtil
import com.aa.counterwidget.databinding.FragmentDataBinding
import com.aa.counterwidget.getWidgetIds
import com.aa.counterwidget.ui.Util

class DataViewAdapter(private var values: List<TsColorItem>, bgColor: Int):
    RecyclerView.Adapter<DataViewAdapter.ViewHolder>() {

    private val defaultBackground = bgColor
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        context = parent.context

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
        holder.contentView.text = Util.formatTime(item.date)
        holder.colorsView.text = ""

        for (color in item.colors) {
            val word: Spannable = SpannableString(" ")

            word.setSpan(BackgroundColorSpan(color), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.colorsView.append(word)

            word.setSpan(defaultBackground, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.colorsView.append(word)
        }

        holder.itemView.setOnClickListener {
            if (Util.isDoubleClick()) {
                TsDataUtil.removeTs(context, holder.contentView.text.toString())
                values = TsDataUtil.getTsColorData(context)
                notifyDataSetChanged()
                CounterWidget.updateWidgets(context)
                val ids = getWidgetIds(context)

            }
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