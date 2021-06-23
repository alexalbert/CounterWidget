package com.aa.counterwidget.ui.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aa.counterwidget.databinding.FragmentChartBinding

class ChartFragment : Fragment() {

    private lateinit var chartViewModel: ChartViewModel
    private var _binding: FragmentChartBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chartViewModel =
            ViewModelProvider(this).get(ChartViewModel::class.java)

        _binding = FragmentChartBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textChart
        chartViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}