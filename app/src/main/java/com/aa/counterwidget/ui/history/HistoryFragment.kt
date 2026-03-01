package com.aa.counterwidget.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aa.counterwidget.HistoryDataUtil
import com.aa.counterwidget.PeriodSummary
import com.aa.counterwidget.R

class HistoryFragment : Fragment() {

    private lateinit var adapter: HistoryViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_history_list, container, false)
        val list = root.findViewById<RecyclerView>(R.id.list)

        val history = HistoryDataUtil.getHistory(requireContext())
        val columns = buildColumns(history)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = HistoryViewAdapter(history, columns)
        this.adapter = list.adapter as HistoryViewAdapter

        return root
    }

    override fun onResume() {
        super.onResume()
        val history = HistoryDataUtil.getHistory(requireContext())
        val columns = buildColumns(history)
        adapter.setColumns(columns)
        adapter.setData(history)
    }

    private fun buildColumns(history: List<PeriodSummary>): List<HistoryColumn> {
        val byWidget = linkedMapOf<Int, Int>()
        for (row in history) {
            for (count in row.counts) {
                if (!byWidget.containsKey(count.widgetId)) {
                    byWidget[count.widgetId] = count.color
                }
            }
        }
        return byWidget.entries
            .sortedBy { it.key }
            .map { HistoryColumn(it.key, it.value) }
    }
}
