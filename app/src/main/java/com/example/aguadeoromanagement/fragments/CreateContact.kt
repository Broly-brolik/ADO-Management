package com.example.aguadeoromanagement.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aguadeoromanagement.databinding.FragmentCreateContactBinding
import com.example.aguadeoromanagement.models.Contact
import com.example.aguadeoromanagement.models.OptionValue
import com.example.aguadeoromanagement.networking.APIFetcher

class CreateContact : Fragment() {
    private var _binding: FragmentCreateContactBinding? = null
    private val binding get() = _binding!!

    private var optionValues: List<OptionValue> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val apiFetcher = APIFetcher(requireActivity())
        apiFetcher.getOptionValues { data, s ->
            if (s) {
                optionValues = data

            } else {
                Toast.makeText(requireContext(), "Error fetching option values", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contactTypes: MutableList<String> = mutableListOf()
        optionValues.forEach { optionValue ->
            if (optionValue.type == "ContactType") {
                contactTypes.add(optionValue.optionValue)
            }
        }
//        Log.e("ContactType", contactTypes.toString())

        val typeAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, contactTypes)
        typeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerSingleContactType.adapter = typeAdapter

        binding.saveContact.setOnClickListener {
            saveContact()
        }
    }

    private fun saveContact() {
        val newContact = Contact(
            name = binding.editTextSingleContactContacts.text.toString(),
            remark = binding.editTextSingleContactRemarks.text.toString(),
            address = binding.editTextSingleContactAddress.text.toString(),
            phone = binding.editTextSingleContactPhone.text.toString(),
            type = binding.spinnerSingleContactType.selectedItem.toString(),
            active = binding.checkBoxSingleContactActive.isChecked,
            resp1 = binding.editTextSingleContactResp1.text.toString(),
            resp2 = binding.editTextSingleContactResp2.text.toString(),
            email = binding.editTextSingleContactEmail1.text.toString(),
            email2 = binding.editTextSingleContactEmail2.text.toString(),
        )
//        Log.e("new contact", newContact.toString())
        APIFetcher(requireActivity()).createContact(newContact) { s ->
            if (s) {
                Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "fail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateContactBinding.inflate(inflater, container, false)

        return binding.root
    }
}