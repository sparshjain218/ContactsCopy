package com.example.contacts;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    ArrayList<ContactDetails> conlist=new ArrayList<>();
    Context context;

    public ContactsAdapter(ArrayList<ContactDetails> conlist, Context context) {
        this.conlist=conlist;
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater lay=LayoutInflater.from(parent.getContext());
        View view=lay.inflate(R.layout.contact_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactDetails con=conlist.get(position);
        holder.name.setText(con.name);
        holder.number.setText(con.number);
        holder.account.setText(con.Account);
    }

    @Override
    public int getItemCount() {
        return conlist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView name,number,account;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.textView);
            number=itemView.findViewById(R.id.textView2);
            account=itemView.findViewById(R.id.textView3);
        }
    }
}
