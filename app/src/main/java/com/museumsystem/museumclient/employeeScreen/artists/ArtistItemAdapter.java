package com.museumsystem.museumclient.employeeScreen.artists;

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
import com.museumsystem.museumclient.dto.ArtistDto;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;

import java.util.List;

public class ArtistItemAdapter extends RecyclerView.Adapter<ArtistItemAdapter.ArtistItemViewHolder> {

    Context context;
    List<ArtistDto> artists;
    String lang;

    public ArtistItemAdapter(Context context, List<ArtistDto> artists, String lang){
        this.context = context;
        this.artists = artists;
        this.lang = lang;
    }

    @NonNull
    @Override
    public ArtistItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artistitem, parent, false);

        return new ArtistItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistItemViewHolder holder, int position) {
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
            holder.name.setText(R.string.ui_artwork_name);
            holder.name.setTypeface(null, Typeface.BOLD);
            holder.name.setTextSize(18);
            holder.birthDate.setText(R.string.ui_artist_birthdate);
            holder.birthDate.setTypeface(null, Typeface.BOLD);
            holder.birthDate.setTextSize(18);
        }else {
            ArtistDto artist = artists.get(position - 1);
            holder.nr.setText(String.valueOf(position));
            holder.name.setText(artist.getName());
            holder.birthDate.setText(artist.getBirthDate());

            if(artist.getDescription().equals(" ")) {
                holder.itemView.setBackgroundColor(Color.RED);
                holder.nr.setTextColor(Color.WHITE);
                holder.name.setTextColor(Color.WHITE);
                holder.birthDate.setTextColor(Color.WHITE);

                Log.d("ArtistAdapter", artist.toString() + "EE");
            }else{
                holder.nr.setTextColor(Color.BLACK);
                holder.name.setTextColor(Color.BLACK);
                holder.birthDate.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public int getItemCount() {
        return artists.size() + 1;
    }

    public void updateArtistsList(List<ArtistDto> newList, String lang) {
        artists.clear();
        artists.addAll(newList);
        this.notifyDataSetChanged();
        this.lang = lang;
    }

    public class ArtistItemViewHolder extends RecyclerView.ViewHolder{
        public TextView nr;
        public TextView name;
        public TextView birthDate;

        public ArtistItemViewHolder(View itemView) {
            super(itemView);
            nr = itemView.findViewById(R.id.artistlist_nr_textview);
            name = itemView.findViewById(R.id.artistlist_name_textview);
            birthDate = itemView.findViewById(R.id.artistlist_birthdate_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition() > 0){
                        EmployeeActivity employeeActivity = (EmployeeActivity) context;
                        FragmentManager manager = employeeActivity.getSupportFragmentManager();
                        Fragment fragment = new ArtistDetailsFragment();
                        ArtistDto artist = artists.get(getAdapterPosition() - 1);
                        Bundle bundle = new Bundle();

                        bundle.putSerializable("artist_dto", artist);
                        bundle.putBoolean("newArtist", false);
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
