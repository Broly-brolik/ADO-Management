package com.example.aguadeoromanagement.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.models.Inventory
import com.example.aguadeoromanagement.screens.AddProductScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.ProductViewModel

class ProductsFragment : Fragment() {

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
//        inflater.inflate(R.menu.menu_payments_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.action_connect_reader -> {
//                showPopup.value = true
//            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)


        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    val productViewModel: ProductViewModel = viewModel()
                    AddProductScreen(
                        optionValues = productViewModel.optionValues.value,
                        loading = productViewModel.loading.value,
                        insertInventory = { inv: Inventory ->
                            val s = productViewModel.insertInventory(inv)
                            if (s) {
                                Toast.makeText(
                                    ManagementApplication.getAppContext(),
                                    "Insertion successful",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    ManagementApplication.getAppContext(),
                                    "Insertion failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        getLastInventory = { category: String ->
                            productViewModel.getLastItemOfCategory(category)
                        },
                    )
                }
            }
        }
    }
}