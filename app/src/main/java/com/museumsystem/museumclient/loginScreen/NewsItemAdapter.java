package com.museumsystem.museumclient.loginScreen;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.museumsystem.museumclient.R;

import java.util.HashMap;
import java.util.List;

import at.blogc.android.views.ExpandableTextView;

public class NewsItemAdapter extends RecyclerView.Adapter<NewsItemAdapter.NewsViewHolder> {

    Context context;
    List<HashMap<String, String>> news;

    public NewsItemAdapter(Context context, List<HashMap<String, String>> news){
        this.context = context;
        this.news = news;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, final int position) {
        if(position %2 == 0)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#EEEEEE"));
        }

            final HashMap<String, String> newsItem = news.get(position);
            holder.date.setText(newsItem.get("date"));
            String message = newsItem.get("content");
            holder.message.setText(message);

    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public void updateNewsList(List<HashMap<String, String>> newList) {
        news.clear();
        news.addAll(newList);
        this.notifyDataSetChanged();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder{
        public TextView date;
        public ExpandableTextView message;
        public Button toggleButton;

        public NewsViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.news_date_textview);
            message = itemView.findViewById(R.id.news_content_expandabletextview);
            toggleButton = itemView.findViewById(R.id.news_toogle_button);
            toggleButton.setText(R.string.ui_expand);

            message.setInterpolator(new OvershootInterpolator());
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleButton.setText(message.isExpanded() ? R.string.ui_expand : R.string.ui_collapse);
                    message.toggle();
                }
            });

        }
    }
}

