package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.screens.ProductDetailsScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.InvoiceListViewModelFactory
import com.example.aguadeoromanagement.viewmodels.ProductDetailsViewModel
import com.example.aguadeoromanagement.viewmodels.ProductDetailsViewModelFactory
import kotlinx.coroutines.launch


class ProductDetailsFragment : Fragment() {

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

        val inventoryCode = args.inventoryCode ?: ""

        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    val viewModel: ProductDetailsViewModel = viewModel(factory = ProductDetailsViewModelFactory(inventoryCode))
                    val scope = viewModel.viewModelScope

                    ProductDetailsScreen(
                        currentCode = viewModel.currentCode.value,
                        changeCurrentCode = { newVal -> viewModel.currentCode.value = newVal },
                        allCodes = viewModel.allCodes.value,
                        onCodeSelected = {
                            scope.launch {
                                viewModel.updateProduct()
                            }
                        },
                        inventory = viewModel.currentInventory.value,
                        productHistory = viewModel.productHistory.value,
                        onNavigate = { findNavController().navigate(R.id.action_singleInventory_to_inventory2) }
                    )

                }
            }
        }
    }
}

