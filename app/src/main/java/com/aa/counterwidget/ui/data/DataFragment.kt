package com.aa.counterwidget.ui.data

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.aa.counterwidget.R
import com.aa.counterwidget.SelectedDateStore
import com.aa.counterwidget.TsDataUtil
import com.aa.counterwidget.ui.DateSwipeItemTouchListener
import com.aa.counterwidget.ui.DateSwipeTouchListener
import com.aa.counterwidget.ui.Util

import com.aa.counterwidget.CounterWidget

class DataFragment : Fragment() {

    private lateinit var adapter: DataViewAdapter
    private lateinit var dateView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_data_list, container, false)
        dateView = view.findViewById(R.id.date)
        val list = view.findViewById<RecyclerView>(R.id.list)

        val selectedDate = SelectedDateStore.currentDate()
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = DataViewAdapter(
            TsDataUtil.getTsColorData(requireContext(), selectedDate),
            Util.getDefaultBackground(requireContext()),
            { SelectedDateStore.currentDate() }
        )
        this.adapter = list.adapter as DataViewAdapter

        val swipeListener = DateSwipeTouchListener(
            onSwipeLeft = { SelectedDateStore.move(1) },
            onSwipeRight = { SelectedDateStore.move(-1) }
        )
        dateView.setOnTouchListener(swipeListener)
        list.addOnItemTouchListener(
            DateSwipeItemTouchListener(
                onSwipeLeft = { SelectedDateStore.move(1) },
                onSwipeRight = { SelectedDateStore.move(-1) }
            )
        )

        SelectedDateStore.selectedDate.observe(viewLifecycleOwner) { date ->
            dateView.text = Util.formatDate(date)
            adapter.setData(TsDataUtil.getTsColorData(requireContext(), date))
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val date = SelectedDateStore.currentDate()
        dateView.text = Util.formatDate(date)
        adapter.setData(TsDataUtil.getTsColorData(requireContext(), date))
        CounterWidget.updateWidgets(requireContext())
    }
}
