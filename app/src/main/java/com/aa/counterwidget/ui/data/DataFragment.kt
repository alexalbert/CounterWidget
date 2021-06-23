package com.aa.counterwidget.ui.data

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aa.counterwidget.R
import com.aa.counterwidget.TsDataUtil

class DataFragment : Fragment() {

    private var columnCount = 1
    private lateinit var adapter: DataViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                val data = TsDataUtil.getTsColorData(context)

                adapter = DataViewAdapter(data, getDefaultBackground())
            }
            this.adapter = view.adapter as DataViewAdapter
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        adapter?.setData(TsDataUtil.getTsColorData(requireContext()))
    }

    fun getDefaultBackground() : Int {
        var color =  Color.WHITE
        val a = TypedValue()
        context?.theme?.resolveAttribute(android.R.attr.windowBackground, a, true)
        if (a.isColorType) {
            color = a.data
        }
        return color
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            DataFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}