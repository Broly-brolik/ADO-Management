package com.example.aguadeoromanagement.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aguadeoromanagement.Constants;
import com.example.aguadeoromanagement.MainActivity;
import com.example.aguadeoromanagement.R;
import com.example.aguadeoromanagement.adapters.suppliersFragment.SupplierHistoryAdapter;
import com.example.aguadeoromanagement.adapters.suppliersFragment.SupplierOrdersHistoryAdapter;
import com.example.aguadeoromanagement.adapters.suppliersFragment.SuppliersInvoicesHistory;
import com.example.aguadeoromanagement.databinding.FragmentSuppliersManagementBinding;
import com.example.aguadeoromanagement.models.Contact;
import com.example.aguadeoromanagement.models.ContactHistory;
import com.example.aguadeoromanagement.models.Invoice;
import com.example.aguadeoromanagement.models.Payment;
import com.example.aguadeoromanagement.models.SupplierOrderMain;
import com.example.aguadeoromanagement.utils.Functions;
import com.example.aguadeoromanagement.utils.FunctionsKt.*;
import com.example.aguadeoromanagement.networking.APIFetcher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SuppliersManagement extends Fragment {
    private FragmentSuppliersManagementBinding binding;
    private Contact contact = new Contact();
    private ArrayList<ContactHistory> contactHistory = new ArrayList<>();
    private final ArrayList<SupplierOrderMain> supplierOrderMainHistory = new ArrayList<>();
    private ArrayList<Payment> payments = new ArrayList<>();
    private ArrayList<Invoice> invoices = new ArrayList<>();

    private EditText contacts;
    private EditText remarks;
    private EditText phone;
    private CheckBox active;
    private EditText resp1;
    private EditText resp2;
    private EditText email1;
    private EditText email2;
    private EditText address;

    private Button saveEditContact;
    private int contact_id;
    private String contact_name;


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_supplier_management, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SupplierOrderMain[] supplierOrderMain = new SupplierOrderMain[supplierOrderMainHistory.size()];
        supplierOrderMainHistory.toArray(supplierOrderMain);
        if (item.getItemId()==R.id.action_new_invoice){
            Bundle bundle = new Bundle();
            bundle.putParcelableArray("SupplierOrderMain", supplierOrderMain);
            bundle.putParcelable("Supplier", contact);
            Navigation.findNavController(getView()).navigate(R.id.action_suppliersManagement_to_createInvoice, bundle);
        }
        else if (item.getItemId() == R.id.action_edit_contact){
                setWriteMode(true);
        }
        else if (item.getItemId() == R.id.action_all_invoices){
            Navigation.findNavController(getView()).navigate(R.id.action_suppliersManagement_to_allInvoices);
        }
//        switch (item.getItemId()) {
//            case R.id.action_new_invoice:
//
//
//                break;
//            case R.id.action_edit_contact:
//                break;
//            case R.id.action_all_invoices:
//
//
//                break;
//
//        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        contact_id = getArguments().getInt("contact_id");
        contact_name = getArguments().getString("contact_name");
        binding = FragmentSuppliersManagementBinding.inflate(getLayoutInflater());
        binding.infoLayout.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(contact_name);


        new Thread(new Runnable() {
            @Override
            public void run() {
                refresh(contact_name);
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saveEditContact = binding.editContact.saveContact;
        saveEditContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWriteMode(false);
                Contact newContact = new Contact();
                newContact.setId(contact_id);
                newContact.setName(binding.editContact.editTextSingleContactContacts.getText().toString());
                newContact.setAddress(binding.editContact.editTextSingleContactAddress.getText().toString());
                newContact.setRemark(binding.editContact.editTextSingleContactRemarks.getText().toString());
                newContact.setPhone(binding.editContact.editTextSingleContactPhone.getText().toString());
                newContact.setResp1(binding.editContact.editTextSingleContactResp1.getText().toString());
                newContact.setResp2(binding.editContact.editTextSingleContactResp2.getText().toString());
                newContact.setEmail(binding.editContact.editTextSingleContactEmail1.getText().toString());
                newContact.setEmail2(binding.editContact.editTextSingleContactEmail2.getText().toString());
                newContact.setActive(binding.editContact.checkBoxSingleContactActive.isChecked());
            }
        });


    }


    private void unlockFields(boolean f) {
        focus(contacts, f);
        focus(remarks, f);
        focus(address, f);
        focus(phone, f);
        focus(active, f);
        focus(resp1, f);
        focus(resp2, f);
        focus(email1, f);
        focus(email2, f);


    }

    private void focus(View v, boolean f) {
        v.setFocusable(f);
        v.setClickable(f);
        v.setFocusableInTouchMode(f);
        v.setFocusedByDefault(f);
        v.setEnabled(f);
        if (v instanceof EditText) {
            ((EditText) v).setTextColor(Color.BLACK);
            if (!f) {
                v.setBackgroundResource(android.R.color.transparent);
            } else {
                v.setBackgroundResource(android.R.drawable.edit_text);
            }
        }
    }

    private void setWriteMode(boolean r) {
        unlockFields(r);
        if (r) {
            saveEditContact.setVisibility(View.VISIBLE);
        } else {
            saveEditContact.setVisibility(View.GONE);
        }
    }

    private void refresh(String contact_name) {

        final long[] start = {System.currentTimeMillis()};
        APIFetcher apiFetcher = new APIFetcher(getActivity());

        apiFetcher.getSupplier(contact_name, false, true, (data, s) -> {

            Log.e("time get Supplier", String.valueOf(System.currentTimeMillis() - start[0]));

            contact = data;
//            apiFetcher.getSupplierHistory(contact, (data1, s1) -> {
//                contactHistory = (ArrayList<ContactHistory>) data1;
            start[0] = System.currentTimeMillis();
            apiFetcher.getSupplierOrderMainHistory(contact.getName(), (data2, s2) -> {
                Log.e("time get Supplier main history", String.valueOf(System.currentTimeMillis() - start[0]));

                for (SupplierOrderMain order : data2) {
                    if (!order.getStatus().equals("Status 3")) {
                        supplierOrderMainHistory.add(order);
                    }
                }

                start[0] = System.currentTimeMillis();
                apiFetcher.getPaymentsForSupplier(contact, (data3, s3) -> {
                    Log.e("time get payments", String.valueOf(System.currentTimeMillis() - start[0]));

                    payments = (ArrayList<Payment>) data3;

                    start[0] = System.currentTimeMillis();
                    apiFetcher.getSupplierInvoices(contact, (data4, s4) -> {
                        Log.e("time get invoices", String.valueOf(System.currentTimeMillis() - start[0]));

                        requireActivity().runOnUiThread(() -> {
                            invoices = (ArrayList<Invoice>) data4;
                            contacts = binding.editContact.editTextSingleContactContacts;
                            remarks = binding.editContact.editTextSingleContactRemarks;
                            phone = binding.editContact.editTextSingleContactPhone;
                            active = binding.editContact.checkBoxSingleContactActive;
                            resp1 = binding.editContact.editTextSingleContactResp1;
                            resp2 = binding.editContact.editTextSingleContactResp2;
                            email1 = binding.editContact.editTextSingleContactEmail1;
                            email2 = binding.editContact.editTextSingleContactEmail2;
                            address = binding.editContact.editTextSingleContactAddress;

                            setWriteMode(false);

                            contacts.setText(contact.getName());
                            remarks.setText(contact.getRemark());
//        binding.editContact.spinnerSingleContactType.setSelection( contact.getType());
                            phone.setText(contact.getPhone());
                            active.setChecked(contact.getActive());
                            resp1.setText(contact.getResp1());
                            resp2.setText(contact.getResp2());
                            email1.setText(contact.getEmail());
                            email2.setText(contact.getEmail2());
                            address.setText(contact.getAddress());
                            RecyclerView contactHistoryList = binding.contactHistoryList;
                            contactHistoryList.setLayoutManager(new LinearLayoutManager(getActivity()));
                            contactHistoryList.setAdapter(new SupplierHistoryAdapter(contactHistory));

                            RecyclerView openOrdersList = binding.openOrdersList;
                            openOrdersList.setLayoutManager(new LinearLayoutManager(getActivity()));
                            openOrdersList.setAdapter(new SupplierOrdersHistoryAdapter(supplierOrderMainHistory, requireActivity()));

                            RecyclerView invoicesList = binding.openInvoicesList;
                            invoicesList.setLayoutManager(new LinearLayoutManager(getActivity()));
                            invoicesList.setAdapter(new SuppliersInvoicesHistory(invoices, contact.getName()));
                            binding.progressBar.setVisibility(View.GONE);
                            binding.infoLayout.setVisibility(View.VISIBLE);


                            TextView toPay = binding.textViewToPay;
                            TextView inDelay = binding.textViewInDelay;

                            List<Double> lessThan30Days = new ArrayList<>();
                            List<Double> lessThan60Days = new ArrayList<>();
                            List<Double> lessThan90Days = new ArrayList<>();
                            List<Double> moreThan90Days = new ArrayList<>();
                            List<Double> inDelayInvoice = new ArrayList<>();


                            for (Invoice invoice : invoices) {
                                String dueOn = invoice.getDueOn();
                                if (dueOn.isEmpty()) {
                                    continue;
                                }
                                LocalDateTime dueOnDate = LocalDateTime.parse(dueOn, Constants.Companion.getFromAccessFormatter());
                                LocalDateTime now = LocalDateTime.now();
                                Period period = Period.between(now.toLocalDate(), dueOnDate.toLocalDate());
                                System.out.println(period.getDays());
                                int days = period.getDays();
                                if (days < 0) {
                                    inDelayInvoice.add(invoice.getRemain());
                                } else if (days < 30) {
                                    lessThan30Days.add(invoice.getRemain());
                                } else if (days < 60) {
                                    lessThan60Days.add(invoice.getRemain());
                                } else if (days < 90) {
                                    lessThan90Days.add(invoice.getRemain());
                                } else {
                                    moreThan90Days.add(invoice.getRemain());
                                }
                            }

                            String toPayString = "";
                            if (lessThan30Days.size() > 0) {
                                toPayString += "< 30 days: " + Functions.toPrice(lessThan30Days.stream().reduce(0.0, Double::sum)) + " (" + lessThan30Days.size() + " invoice) \n";
                            }
                            if (lessThan60Days.size() > 0) {
                                toPayString += "< 60 days: " + Functions.toPrice(lessThan60Days.stream().reduce(0.0, Double::sum)) + " (" + lessThan60Days.size() + " invoice) \n";
                            }
                            if (lessThan90Days.size() > 0) {
                                toPayString += "< 90 days: " + Functions.toPrice(lessThan90Days.stream().reduce(0.0, Double::sum)) + " (" + lessThan90Days.size() + " invoice) \n";
                            }
                            if (moreThan90Days.size() > 0) {
                                toPayString += "> 90 days: " + Functions.toPrice(moreThan90Days.stream().reduce(0.0, Double::sum)) + " (" + moreThan90Days.size() + " invoice) \n";
                            }

                            String inDelayString = "";
                            if (inDelayInvoice.size() > 0) {
                                inDelayString += Functions.toPrice(inDelayInvoice.stream().reduce(0.0, Double::sum)) + " (" + inDelayInvoice.size() + " invoice) \n";

                            }

                            toPay.setText(toPayString);
                            inDelay.setText(inDelayString);

                            TextView textViewTitleOpenInvoice = binding.textViewOpenInvoices;
                            textViewTitleOpenInvoice.setText(textViewTitleOpenInvoice.getText().toString() + " (" + invoices.size() + " invoices)");

                            TextView textViewTitleOpenOrders = binding.textViewOpenOrders;
                            textViewTitleOpenOrders.setText(textViewTitleOpenOrders.getText().toString() + " (" + supplierOrderMainHistory.size() + " orders)");


                        });

                        return null;
                    });
                    return null;
                });
                return null;
            });
//                return null;
//            });
            return null;
        });
    }
}
