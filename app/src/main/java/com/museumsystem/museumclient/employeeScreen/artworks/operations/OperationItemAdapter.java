package com.museumsystem.museumclient.employeeScreen.artworks.operations;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.museumsystem.museumclient.R;

import java.util.HashMap;
import java.util.List;

public class OperationItemAdapter extends RecyclerView.Adapter<OperationItemAdapter.OperationItemViewHolder> {

    Context context;
    List<HashMap<String, String>> operations;

    public OperationItemAdapter(Context context, List<HashMap<String, String>> operations){
        this.context = context;
        this.operations = operations;
    }

    @NonNull
    @Override
    public OperationItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.operationitem, parent, false);

        return new OperationItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OperationItemViewHolder holder, int position) {
        if(position %2 == 1)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#EEEEEE"));
        }

        if(position == 0){
            holder.nr.setText(R.string.ui_ticketlist_nr);
            holder.nr.setTypeface(null, Typeface.BOLD);
            holder.nr.setTextSize(18);
            holder.date.setText(R.string.ui_ticketlist_date);
            holder.date.setTypeface(null, Typeface.BOLD);
            holder.date.setTextSize(18);
            holder.emp.setText(R.string.ui_role_employee);
            holder.emp.setTypeface(null, Typeface.BOLD);
            holder.emp.setTextSize(18);
            holder.description.setText(R.string.ui_description);
            holder.description.setTypeface(null, Typeface.BOLD);
            holder.description.setTextSize(18);
        }else {
            HashMap<String, String> operation = operations.get(position - 1);
            holder.nr.setText(String.valueOf(position));
            holder.date.setText(operation.get("date"));
            holder.emp.setText(operation.get("employee"));
            holder.description.setText(operation.get("description"));
        }
    }

    @Override
    public int getItemCount() {
        return operations.size() + 1;
    }

    public void updateOperationsList(List<HashMap<String, String>> newList) {
        operations.clear();
        operations.addAll(newList);
        this.notifyDataSetChanged();
    }

    public class OperationItemViewHolder extends RecyclerView.ViewHolder{
        public TextView nr;
        public TextView date;
        public TextView emp;
        public TextView description;

        public OperationItemViewHolder(View itemView) {
            super(itemView);
            nr = itemView.findViewById(R.id.operationitem_nr_textview);
            date = itemView.findViewById(R.id.operationitem_date_textview);
            emp = itemView.findViewById(R.id.operationitem_emp_textview);
            description = itemView.findViewById(R.id.operationitem_description_textview);
        }
    }
}
