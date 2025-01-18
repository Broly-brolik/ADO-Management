package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.example.aguadeoromanagement.screens.AdoStonesScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme

class AdoStonesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                ManagementTheme {
                    AdoStonesScreen("0")
                }
            }
        }
    }
}
