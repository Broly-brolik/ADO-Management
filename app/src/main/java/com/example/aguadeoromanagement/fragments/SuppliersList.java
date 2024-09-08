package com.example.aguadeoromanagement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aguadeoromanagement.R;
import com.example.aguadeoromanagement.adapters.SuppliersListAdapter;
import com.example.aguadeoromanagement.databinding.FragmentSuppliersListBinding;
import com.example.aguadeoromanagement.models.Contact;
import com.example.aguadeoromanagement.networking.APIFetcher;

import java.util.ArrayList;
import java.util.List;

public class SuppliersList extends Fragment {
    private FragmentSuppliersListBinding binding;

    private SuppliersListAdapter adapter;
    private final List<Contact> contactList = new ArrayList<>();


    public SuppliersList() {
        // Required empty public constructor
    }


//    @Override
//    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater menuInflater = getActivity().getMenuInflater();
//        menuInflater.inflate(R.menu.menu_suppliers_list, menu);
//    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_supplier_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()== R.id.action_add_contact){

                Navigation.findNavController(getView()).navigate(R.id.action_suppliersList_to_createContact);
        }
//        switch (item.getItemId()) {
//            case R.id.action_add_contact:
////                Toast.makeText(getActivity(), "Add contact", Toast.LENGTH_LONG).show();
//                break;
//        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);


        binding = FragmentSuppliersListBinding.inflate(getLayoutInflater());

        APIFetcher apiFetcher = new APIFetcher(getActivity());

        apiFetcher.getSuppliers((data, s) -> {
            contactList.addAll(data);
            return null;
        });


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = new SuppliersListAdapter(contactList, getActivity());

        RecyclerView recyclerView = binding.recyclerSupplierList;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        return binding.getRoot();
    }
}
