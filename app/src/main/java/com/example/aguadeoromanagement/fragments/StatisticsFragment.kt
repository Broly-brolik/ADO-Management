package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.navArgs
import com.example.aguadeoromanagement.screens.ProductDetailsScreen
import com.example.aguadeoromanagement.screens.StatisticsScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.InvoiceListViewModelFactory
import com.example.aguadeoromanagement.viewmodels.ProductDetailsViewModel
import com.example.aguadeoromanagement.viewmodels.ProductDetailsViewModelFactory
import com.example.aguadeoromanagement.viewmodels.StatisticsViewModel
import kotlinx.coroutines.launch


class StatisticsFragment : Fragment() {

    private val args by navArgs<ProductDetailsFragmentArgs>()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
//        inflater.inflate(R.menu.menu_inventory, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        }
        return super.onOptionsItemSelected(item)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
//        Log.e("args man", args.inventoryCode)


        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    val viewModel: StatisticsViewModel = viewModel()
                    val scope = viewModel.viewModelScope

                    StatisticsScreen(
                        loading = viewModel.loading.value,
//                        statistics = viewModel.statisticsData.value,
//                        inventoryData = viewModel.inventoryData.value,
                        inventoryChecks = viewModel.inventoryChecks.value,
                        retrieveCodeInfo = { codes ->
                            scope.launch {
                                viewModel.retrieveCodeInfo(codes)
                            }
                        },
                        images = viewModel.images.value
                    )
                }
            }
        }
    }
}

