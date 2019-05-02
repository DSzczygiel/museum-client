package com.museumsystem.museumclient.employeeScreen.artworks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.dto.ArtistDto;

import java.util.List;

public class ArtistSpinnerAdapter extends ArrayAdapter<ArtistDto> {
    List<ArtistDto> artists;
    Context context;
    public ArtistSpinnerAdapter(Context context, int resId, List<ArtistDto> artists) {
        super(context, resId, artists);
        this.artists = artists;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        return createItemView(position, convertView, parent);
    }

    public View createItemView(int position, View convertView, ViewGroup parent){
        // Get the data item for this position
        ArtistDto artist = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_spinner_item, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.artist_spinneritem_textview);
        // Populate the data into the template view using the data object
        name.setText(artist.getName());
        // Return the completed view to render on screen
        return convertView;
    }

    public void update(List<ArtistDto> artists){
        this.artists.clear();
        this.artists.addAll(artists);
        this.notifyDataSetChanged();
    }
}