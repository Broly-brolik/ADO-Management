package com.example.aguadeoromanagement.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aguadeoromanagement.R;
import com.example.aguadeoromanagement.fragments.SuppliersListDirections;
import com.example.aguadeoromanagement.models.Contact;
import com.example.aguadeoromanagement.models.SupplierOrderMain;
import com.example.aguadeoromanagement.networking.APIFetcher;
import java.util.List;

public class SuppliersListAdapter extends RecyclerView.Adapter<SuppliersListAdapter.ListItemHolder> {

    private final List<Contact> contactList;
    private final Activity activity;

    public SuppliersListAdapter(List<Contact> contactList, Activity activity) {
        this.contactList = contactList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SuppliersListAdapter.ListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.line_supplier, parent, false);
        return new ListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SuppliersListAdapter.ListItemHolder holder, int position) {
        Contact contact = contactList.get(position);

        holder.name.setText(contact.getName());
        holder.date_created.setText("");
        holder.email.setText(contact.getEmail());
        holder.address.setText(contact.getAddress());
        holder.phone.setText(contact.getPhone());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public class ListItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView name;
        TextView date_created;
        TextView email;
        TextView phone;
        TextView address;

        public ListItemHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.supplierItemName);
            date_created = itemView.findViewById(R.id.supplierItemDateCreated);
            email = itemView.findViewById(R.id.supplierItemEmail);
            address = itemView.findViewById(R.id.supplierItemAddress);
            phone = itemView.findViewById(R.id.supplierItemPhone);


            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {
            Log.e("hey", "click");

            Contact contact = contactList.get(getBindingAdapterPosition());
//            Toast.makeText(activity.getApplicationContext(), contact.getContacts(), Toast.LENGTH_SHORT).show();

            Bundle bundle = new Bundle();
            bundle.putInt("contact_id", contact.getId());
            bundle.putString("contact_name", contact.getName());
//            NavDirections action = SuppliersListDirections.actionSuppliersListToSuppliersManagement();
            String contactName = contact.getName();

            Navigation.findNavController(view).navigate(R.id.action_suppliersList_to_suppliersManagement, bundle);
        }

        @Override
        public boolean onLongClick(View view) {
//            Log.e("hey", "long click");
            Contact contact = contactList.get(getBindingAdapterPosition());
            Bundle bundle = new Bundle();
            APIFetcher apiFetcher = new APIFetcher(activity);
            apiFetcher.getSupplier(String.valueOf(contact.getId()), false, false, (data, s) -> {
                bundle.putParcelable("contact", data);
                bundle.putParcelableArray("supplierOrderMain", new SupplierOrderMain[0]);
//                Log.e("contact long clikc", contact.toString());
//            bundle.putString("contact_name", contact.getName());

//          com.example.aguadeoromanagement.fragments.SuppliersListDirections.ActionSuppliersListToCreateInvoice action =
//                  new SuppliersListDirections.ActionSuppliersListToCreateInvoice(new SupplierOrderMain[0], contact);
//            Navigation.findNavController(view).navigate(action);
//            Navigation.findNavController(view).navigate(R.id.action_suppliersList_to_createInvoice, bundle);
                com.example.aguadeoromanagement.fragments.SuppliersListDirections.ActionSuppliersListToCreateInvoice action =
                        SuppliersListDirections.actionSuppliersListToCreateInvoice(new SupplierOrderMain[0], data);
                Navigation.findNavController(view).navigate(action);
//                return true;
                return null;
            });
            return true;
        }
//        @Override
//        public boolean onLongClick(View view) {
//            Log.e("hey", "long click");
//            return true;
//        }
    }


}
