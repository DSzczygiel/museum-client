package com.museumsystem.museumclient.employeeScreen.maintenance;



import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.museumsystem.museumclient.R;

import java.util.HashMap;
import java.util.List;

public class MaintenanceItemAdapter extends RecyclerView.Adapter<MaintenanceItemAdapter.MaintenanceViewHolder> {

    Context context;
    List<HashMap<String, String>> maintenances;
    MaintenanceAdapterCallback maintenanceAdapterCallback;

    public MaintenanceItemAdapter(Context context, List<HashMap<String, String>> maintenances){
        this.context = context;
        this.maintenances = maintenances;
    }

    @NonNull
    @Override
    public MaintenanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.maintenanceitem, parent, false);

        return new MaintenanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MaintenanceViewHolder holder, final int position) {
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
            holder.author.setText(R.string.ui_artwork_author);
            holder.author.setTypeface(null, Typeface.BOLD);
            holder.author.setTextSize(18);
            holder.name.setText(R.string.ui_artwork_name);
            holder.name.setTypeface(null, Typeface.BOLD);
            holder.name.setTextSize(18);
            holder.confirmButton.setVisibility(View.GONE);
        }else {
            final HashMap<String, String> maintenance = maintenances.get(position - 1);
            holder.nr.setText(String.valueOf(position));
            holder.date.setText(maintenance.get("date"));
            holder.author.setText(maintenance.get("author"));
            holder.name.setText(maintenance.get("name"));

            if(maintenance.get("need_main").equals("true")){
                holder.confirmButton.setVisibility(View.VISIBLE);
                holder.confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(maintenanceAdapterCallback != null)
                            maintenanceAdapterCallback.buttonPressed(Long.valueOf(maintenance.get("art_id")));
                    }
                });
            }else{
                holder.confirmButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return maintenances.size() + 1;
    }

    public void setCallback(MaintenanceAdapterCallback callback){
        this.maintenanceAdapterCallback = callback;
    }

    public void updateMaintenancesList(List<HashMap<String, String>> newList) {
        maintenances.clear();
        maintenances.addAll(newList);
        this.notifyDataSetChanged();
    }

    public class MaintenanceViewHolder extends RecyclerView.ViewHolder{
        public TextView nr;
        public TextView date;
        public TextView author;
        public TextView name;
        public Button confirmButton;

        public MaintenanceViewHolder(View itemView) {
            super(itemView);
            nr = itemView.findViewById(R.id.mainlist_nr_textview);
            date = itemView.findViewById(R.id.mainlist_date_textview);
            author = itemView.findViewById(R.id.mainlist_artistname_textview);
            name = itemView.findViewById(R.id.mainlist_artwotkname_textview);
            confirmButton = itemView.findViewById(R.id.mainlist_confirm_button);
        }
    }
}
