package com.museumsystem.museumclient.customerScreen.menu;

import android.content.Context;
import android.support.annotation.NonNull;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.customerScreen.CustomerProfileFragment;
import com.museumsystem.museumclient.customerScreen.TicketPurchaseFragment;
import com.museumsystem.museumclient.customerScreen.CustomerActivity;
import com.museumsystem.museumclient.customerScreen.ticketsList.MyTicketsFragment;

import java.util.List;

public class CustomerMenuItemAdapter extends RecyclerView.Adapter<CustomerMenuItemAdapter.MenuItemViewHolder>{
    private Context context;
    private List<String> titles;

    public CustomerMenuItemAdapter(Context context, List<String> titles){
        this.context = context;
        this.titles = titles;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menuitem_card, parent, false);

        return new MenuItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        String title = titles.get(position);
        holder.title.setText(title);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class MenuItemViewHolder extends RecyclerView.ViewHolder{
        public TextView title;

        public MenuItemViewHolder(final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.menuitem_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomerActivity customerActivity = (CustomerActivity) context;
                    FragmentManager manager = customerActivity.getSupportFragmentManager();

                    if(getAdapterPosition() == 0){
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.customer_framelayout, new TicketPurchaseFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }else if(getAdapterPosition() == 1){
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.customer_framelayout, new MyTicketsFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }else if(getAdapterPosition() == 2){
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.customer_framelayout, new CustomerProfileFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
        }
    }
}
