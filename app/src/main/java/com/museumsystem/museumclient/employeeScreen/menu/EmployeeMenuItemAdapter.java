package com.museumsystem.museumclient.employeeScreen.menu;

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
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;
import com.museumsystem.museumclient.employeeScreen.TicketValidatorFragment;
import com.museumsystem.museumclient.employeeScreen.artists.ArtistListFragment;
import com.museumsystem.museumclient.employeeScreen.artworks.ArtworkListFragment;
import com.museumsystem.museumclient.employeeScreen.maintenance.MaintenancesListFragment;
import com.museumsystem.museumclient.employeeScreen.news.NewsListFragment;

import java.util.List;

public class EmployeeMenuItemAdapter extends RecyclerView.Adapter<EmployeeMenuItemAdapter.MenuItemViewHolder>{
    private Context context;
    private List<String> titles;

    public EmployeeMenuItemAdapter(Context context, List<String> titles){
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

        public MenuItemViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.menuitem_textview);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EmployeeActivity employeeActivity = (EmployeeActivity) context;
                    FragmentManager manager = employeeActivity.getSupportFragmentManager();

                    if(getAdapterPosition() == 0){
                       FragmentTransaction transaction = manager.beginTransaction();
                       transaction.replace(R.id.employee_framelayout, new TicketValidatorFragment());
                       transaction.addToBackStack(null);
                       transaction.commit();
                    }else if(getAdapterPosition() == 1){
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.employee_framelayout, new ArtistListFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }else if(getAdapterPosition() == 2){
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.employee_framelayout, new ArtworkListFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }else if(getAdapterPosition() == 3){
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.employee_framelayout, new MaintenancesListFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }else if(getAdapterPosition() == 4){
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.employee_framelayout, new NewsListFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
        }
    }
}
