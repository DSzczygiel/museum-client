package com.museumsystem.museumclient.employeeScreen.artworks;

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
import com.museumsystem.museumclient.dto.ArtworkDto;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;

import java.util.List;

public class ArtworkItemAdapter extends RecyclerView.Adapter<ArtworkItemAdapter.ArtworkItemViewHolder> {
    Context context;
    List<ArtworkDto> artworks;
    String lang;

    public ArtworkItemAdapter(Context context, List<ArtworkDto> artworks, String lang){
        this.context = context;
        this.artworks = artworks;
        this.lang = lang;
    }

    @NonNull
    @Override
    public ArtworkItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artworkitem, parent, false);

        return new ArtworkItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtworkItemViewHolder holder, int position) {
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
            holder.name.setText(R.string.ui_ticketlist_date);
            holder.name.setTypeface(null, Typeface.BOLD);
            holder.name.setTextSize(18);
            holder.author.setText(R.string.ui_artwork_author);
            holder.author.setTypeface(null, Typeface.BOLD);
            holder.author.setTextSize(18);
            holder.year.setText(R.string.ui_artwork_creationyear);
            holder.year.setTypeface(null, Typeface.BOLD);
            holder.year.setTextSize(18);
        }else {
            ArtworkDto artwork = artworks.get(position - 1);
            holder.nr.setText(String.valueOf(position));
            holder.name.setText(artwork.getTitle());
            holder.author.setText(artwork.getArtist().getName());
            holder.year.setText(artwork.getCreationYear().toString());

            if(artwork.getDescription().equals(" ")){
                holder.itemView.setBackgroundColor(Color.RED);
                holder.nr.setTextColor(Color.WHITE);
                holder.name.setTextColor(Color.WHITE);
                holder.author.setTextColor(Color.WHITE);
                holder.year.setTextColor(Color.WHITE);
            }else{
                holder.nr.setTextColor(Color.BLACK);
                holder.name.setTextColor(Color.BLACK);
                holder.author.setTextColor(Color.BLACK);
                holder.year.setTextColor(Color.BLACK);
            }

        }

    }

    @Override
    public int getItemCount() {
        return artworks.size() + 1;
    }

    public void updateArtworksList(List<ArtworkDto> newList, String lang) {
        artworks.clear();
        artworks.addAll(newList);
        this.notifyDataSetChanged();
        this.lang = lang;
    }

    public class ArtworkItemViewHolder extends RecyclerView.ViewHolder{
        public TextView nr;
        public TextView name;
        public TextView author;
        public TextView year;

        public ArtworkItemViewHolder(View itemView) {
            super(itemView);
            nr = itemView.findViewById(R.id.artworklist_nr_textview);
            name = itemView.findViewById(R.id.artworklist_name_textview);
            author = itemView.findViewById(R.id.artworklist_author_textview);
            year = itemView.findViewById(R.id.artworklist_year_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition() > 0){
                        EmployeeActivity employeeActivity = (EmployeeActivity) context;
                        FragmentManager manager = employeeActivity.getSupportFragmentManager();
                        Fragment fragment = new ArtworkDetailsFragment();
                        ArtworkDto artwork = artworks.get(getAdapterPosition() - 1);
                        Bundle bundle = new Bundle();

                        bundle.putSerializable("artwork_dto", artwork);
                        bundle.putBoolean("newArtwork", false);
                        bundle.putString("lang", lang);
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
