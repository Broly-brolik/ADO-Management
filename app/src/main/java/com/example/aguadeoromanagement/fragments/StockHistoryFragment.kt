package com.example.aguadeoromanagement.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.databinding.FragmentHomeBinding
import com.example.aguadeoromanagement.screens.InOutContainerV2
import com.example.aguadeoromanagement.screens.ProductDetailsScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.ProductDetailsViewModel
import com.example.aguadeoromanagement.viewmodels.ProductDetailsViewModelFactory
import com.example.aguadeoromanagement.viewmodels.StockHistoryViewModel
import com.example.aguadeoromanagement.viewmodels.StockHistoryViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StockHistoryFragment : Fragment() {

    private val args by navArgs<StockHistoryFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        Log.e("args group", args.grouping)
        if (args.orderNumber.isNullOrEmpty()){
            Log.e("null", "comme un vers")
        }
        Log.e("args order number", args.orderNumber.orEmpty())
        Log.e("args order number", args.supplier.orEmpty())


        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    val viewModel: StockHistoryViewModel = viewModel(factory = StockHistoryViewModelFactory(args.grouping, args.orderNumber.orEmpty(), args.supplier.orEmpty()))
                    InOutContainerV2(
                        inOutList = viewModel.orderNumbers.value,
                        loadingItems = false,
                        modifier = Modifier,
                        navController = findNavController()
                    )
                }
            }

        }
    }


}