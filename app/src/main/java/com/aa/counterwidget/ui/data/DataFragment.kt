package com.aa.counterwidget.ui.data

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aa.counterwidget.R
import com.aa.counterwidget.TsDataUtil
import com.aa.counterwidget.ui.Util

import com.aa.counterwidget.CounterWidget

class DataFragment : Fragment() {

    private lateinit var adapter: DataViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_data_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                val data = TsDataUtil.getTsColorData(context)
                layoutManager = LinearLayoutManager(context)
                adapter = DataViewAdapter(data, Util.getDefaultBackground(context))
            }
            this.adapter = view.adapter as DataViewAdapter
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.setData(TsDataUtil.getTsColorData(requireContext()))
        CounterWidget.updateWidgets(requireContext())
    }
}