package com.aa.counterwidget.ui.history

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aa.counterwidget.PeriodSummary
import com.aa.counterwidget.databinding.FragmentHistoryBinding
import com.aa.counterwidget.ui.Util

data class HistoryColumn(val widgetId: Int, val color: Int)

class HistoryViewAdapter(
    private var values: List<PeriodSummary>,
    private var columns: List<HistoryColumn>
) :
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

    fun setColumns(newColumns: List<HistoryColumn>) {
        columns = newColumns
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.dateView.text = Util.formatDate(item.date)
        val countByWidgetId = item.counts.associate { it.widgetId to it }

        holder.columnsContainer.removeAllViews()
        for (column in columns) {
            val cell = TextView(holder.itemView.context)
            cell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            cell.gravity = Gravity.END
            cell.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_END
            cell.includeFontPadding = false

            val count = countByWidgetId[column.widgetId]?.count
            cell.text = if (count == null) "" else count.toString()
            cell.setTextColor(column.color)
            holder.columnsContainer.addView(cell)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val dateView: TextView = binding.date
        val columnsContainer: LinearLayout = binding.columns
    }
}
