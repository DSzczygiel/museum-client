package com.museumsystem.museumclient.employeeScreen.news;



import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;

import java.util.HashMap;
import java.util.List;

public class EmpNewsItemAdapter extends RecyclerView.Adapter<EmpNewsItemAdapter.NewsViewHolder> {

    Context context;
    List<HashMap<String, String>> news;

    public EmpNewsItemAdapter(Context context, List<HashMap<String, String>> news){
        this.context = context;
        this.news = news;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.emp_newsitem, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, final int position) {
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
            holder.message.setText(R.string.ui_content);
            holder.message.setTypeface(null, Typeface.BOLD);
            holder.message.setTextSize(18);
        }else {
            final HashMap<String, String> newsItem = news.get(position - 1);
            holder.nr.setText(String.valueOf(position));
            holder.date.setText(newsItem.get("date"));
            String message = newsItem.get("content");
            holder.message.setText(message.substring(0, Math.min(message.length(), 30)));
        }
    }

    @Override
    public int getItemCount() {
        return news.size() + 1;
    }

    public void updateNewsList(List<HashMap<String, String>> newList) {
        news.clear();
        news.addAll(newList);
        this.notifyDataSetChanged();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder{
        public TextView nr;
        public TextView date;
        public TextView message;

        public NewsViewHolder(View itemView) {
            super(itemView);
            nr = itemView.findViewById(R.id.emp_newsitem_nr_textview);
            date = itemView.findViewById(R.id.emp_newslist_date_textview);
            message = itemView.findViewById(R.id.emp_newslist_message_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition() > 0){
                        EmployeeActivity employeeActivity = (EmployeeActivity) context;
                        FragmentManager manager = employeeActivity.getSupportFragmentManager();
                        Fragment fragment = new NewsDetailsFragment();
                        HashMap<String, String> info = news.get(getAdapterPosition() - 1);
                        Bundle bundle = new Bundle();

                        bundle.putSerializable("news_dto", info);
                        bundle.putBoolean("new", false);
                        fragment.setArguments(bundle);

                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.employee_framelayout, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
        }
    }
}
