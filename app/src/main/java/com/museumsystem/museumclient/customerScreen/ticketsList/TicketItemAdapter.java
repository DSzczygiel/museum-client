package com.museumsystem.museumclient.customerScreen.ticketsList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.customerScreen.CustomerActivity;
import com.museumsystem.museumclient.dto.TicketDto;

import java.util.List;

public class TicketItemAdapter extends RecyclerView.Adapter<TicketItemAdapter.TicketItemViewHolder>{
    private Context context;
    private List<TicketDto> tickets;

    public TicketItemAdapter(Context context, List<TicketDto> tickets){
        this.context = context;
        this.tickets =  tickets;
    }

    @NonNull
    @Override
    public TicketItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticketitem, parent, false);

        return new TicketItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketItemViewHolder holder, int position) {
        if(position %2 == 1) {
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
            holder.cost.setText(R.string.ui_ticketlist_cost);
            holder.cost.setTypeface(null, Typeface.BOLD);
            holder.cost.setTextSize(18);
            holder.status.setText(R.string.ui_ticketlist_status);
            holder.status.setTypeface(null, Typeface.BOLD);
            holder.status.setTextSize(18);

        }else {
            TicketDto ticket = tickets.get(position - 1);
            holder.nr.setText(String.valueOf(position));
            holder.date.setText(ticket.getDate());
            holder.cost.setText(String.valueOf(ticket.getPrice()));

            if(ticket.getStatus().equals("unpaid")) {
                holder.status.setText(R.string.ticket_status_unpaid);
                holder.status.setTextColor(Color.RED);
            }else if (ticket.getStatus().equals("valid")){
                holder.status.setText(R.string.ticket_status_valid);
                holder.status.setTextColor(Color.GREEN);
            }else {
                holder.status.setText(R.string.ticket_status_expired);
                holder.status.setTextColor(Color.BLACK);
            }
        }

        Log.d("TicketAdapter", position + " " + getItemCount());
    }

    @Override
    public int getItemCount() {
        return tickets.size() + 1;
    }

    public void updateTicketList(List<TicketDto> newList) {
        tickets.clear();
        tickets.addAll(newList);
        this.notifyDataSetChanged();
    }

    public class TicketItemViewHolder extends RecyclerView.ViewHolder{
        public TextView nr;
        public TextView date;
        public TextView cost;
        public TextView status;

        public TicketItemViewHolder(View itemView) {
            super(itemView);
            nr = (TextView) itemView.findViewById(R.id.ticketlist_nr_textview);
            date = (TextView) itemView.findViewById(R.id.ticketlist_date_textview);
            cost = (TextView) itemView.findViewById(R.id.ticketlist_price_textview);
            status = (TextView) itemView.findViewById(R.id.ticketlist_status_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition() > 0){
                        CustomerActivity customerActivity = (CustomerActivity) context;
                        FragmentManager manager = customerActivity.getSupportFragmentManager();
                        Fragment fragment = new TicketDetailsFragment();
                        TicketDto ticket = tickets.get(getAdapterPosition() - 1);
                        Bundle bundle = new Bundle();

                        bundle.putSerializable("ticket_dto", ticket);
                        fragment.setArguments(bundle);

                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.customer_framelayout, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
        }
    }
}
