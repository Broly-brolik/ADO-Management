package com.example.aguadeoromanagement.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.databinding.FragmentHomeBinding
import com.example.aguadeoromanagement.networking.api.getSupplier
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    var back_button_action = {
//        Log.e("hey", "bien")
    }
    lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                displayURLDialog()
                true
            }

            R.id.action_login -> {
                navController.navigate(R.id.action_HomeFragment_to_settingsFragment)
                true
            }

            android.R.id.home -> {
                back_button_action()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        navController = view.findNavController()
        binding.orderManagementButton.setOnClickListener {
            val action = HomeDirections.actionFirstFragmentToOrdersManagement()
            view.findNavController().navigate(action)
        }
        binding.ReportsHome.setOnClickListener {
            val action = HomeDirections.actionFirstFragmentToReportsHome()
            view.findNavController().navigate(action)
        }
        binding.suppliersBtn.setOnClickListener {
            val action = HomeDirections.actionFirstFragmentToSuppliersList()
            view.findNavController().navigate(action)
        }
        binding.paymentsBtn.setOnClickListener {
//            val action = HomeDirections.actionFirstFragmentToOrdersManagement()
//            view.findNavController().navigate(action
            //                Toast.makeText(getActivity(), "Add contact", Toast.LENGTH_LONG).show();
            val action = HomeDirections.actionFirstFragmentToPaymentsList()
            view.findNavController().navigate(action)
//            findNavController(requireView()).navigate(R.id.action_FirstFragment_to_paymentsList)
        }

        binding.inventoryBtn.setOnClickListener {
            findNavController(requireView()).navigate(R.id.action_FirstFragment_to_inventory)
        }

        binding.productBtn.setOnClickListener {
            findNavController(requireView()).navigate(R.id.action_FirstFragment_to_productsFragment)
        }

        binding.singleProductBtn.setOnClickListener {
//
            val action =
                HomeDirections.actionHomeFragmentToSingleInventory("")
            findNavController(
                requireView()
            ).navigate(action)
        }

        binding.statisticsBtn.setOnClickListener {
            findNavController(requireView()).navigate(R.id.action_HomeFragment_to_statisticsFragment)
        }
        binding.invoiceListBtn.setOnClickListener {
            findNavController(requireView()).navigate(R.id.action_HomeFragment_to_invoiceList)
        }

        binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayURLDialog() {
        val url = requireActivity().getPreferences(Context.MODE_PRIVATE).getString(
            "url",
            "http://195.15.223.234/aguadeoro/connect.php"
        )
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.settings_dialog, null)
        val input = view.findViewById<TextInputEditText>(R.id.urlSetting)
        input.setText(url)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Settings")
            .setView(view)
            .setPositiveButton("SAVE") { dialog, which ->
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("url", input.text.toString())
                    apply()
                }
            }
            .show()
    }
}