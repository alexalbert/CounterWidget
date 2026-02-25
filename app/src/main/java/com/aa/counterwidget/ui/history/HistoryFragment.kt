package com.aa.counterwidget.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aa.counterwidget.HistoryDataUtil
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
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = HistoryViewAdapter(history)
        this.adapter = list.adapter as HistoryViewAdapter

        return root
    }

    override fun onResume() {
        super.onResume()
        adapter.setData(HistoryDataUtil.getHistory(requireContext()))
    }
}
