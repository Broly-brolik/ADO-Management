package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.aguadeoromanagement.databinding.FragmentReportsHomeBinding

class ReportsHome : Fragment() {
    private val binding: FragmentReportsHomeBinding by lazy {
        FragmentReportsHomeBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cashReport.setOnClickListener {
            val action = ReportsHomeDirections.actionReportsHomeToCashReportFragment()
            view.findNavController().navigate(action)
        }
    }
}