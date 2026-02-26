package com.aa.counterwidget.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.aa.counterwidget.PeriodSummary
import com.aa.counterwidget.databinding.FragmentHistoryBinding
import com.aa.counterwidget.ui.Util

class HistoryViewAdapter(private var values: List<PeriodSummary>) :
    RecyclerView.Adapter<HistoryViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun setData(data: List<PeriodSummary>) {
        values = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.dateView.text = Util.formatDate(item.date)
        holder.countsView.text = ""
        for (cc in item.counts.sortedBy { it.widgetId }) {
            val text = "${cc.count} "
            val span: Spannable = SpannableString(text)
            span.setSpan(
                ForegroundColorSpan(cc.color),
                0,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.countsView.append(span)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val dateView: TextView = binding.date
        val countsView: TextView = binding.counts
    }
}
