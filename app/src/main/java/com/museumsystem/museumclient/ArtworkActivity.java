package com.museumsystem.museumclient;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.museumsystem.museumclient.dto.ArtworkDto;

public class ArtworkActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private FloatingActionButton button;
    private boolean speak = false;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView yearTextView;
    private TextView descriptionTextView;
    private ImageView photoImageView;
    ArtworkDto artwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork);


        artwork = (ArtworkDto) getIntent().getSerializableExtra("artwork");
        getIntent().removeExtra("artwork");

        titleTextView = findViewById(R.id.artwork_title_textview);
        authorTextView = findViewById(R.id.artwork_author_textview);
        yearTextView = findViewById(R.id.artwork_year_textview);
        descriptionTextView = findViewById(R.id.artwork_description_textview);
        photoImageView = findViewById(R.id.artwork_photo_imageview);

        titleTextView.setText(artwork.getTitle());
        authorTextView.setText(artwork.getArtist().getName());
        yearTextView.setText(artwork.getCreationYear().toString());
        descriptionTextView.setText(artwork.getDescription());

        final Bitmap bmp = BitmapFactory.decodeByteArray(artwork.getPhoto(), 0, artwork.getPhoto().length);

        photoImageView.post(new Runnable() {
            @Override
            public void run() {
                int height = (photoImageView.getWidth()*bmp.getHeight())/bmp.getWidth();
                photoImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, photoImageView.getWidth(),
                        height, true));
            }
        });

        button = findViewById(R.id.artwork_speech_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(false);
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(getResources().getConfiguration().locale);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        button.setEnabled(false);
                    }else{
                        button.setEnabled(true);
                    }
                }else{
                    button.setEnabled(false);
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(final String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(utteranceId.equals("LAST"))
                            speak(true);
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        authorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showArtistDialog();
                Log.d("ARTIST", "Touch");
            }
        });

        Toast.makeText(getApplication(), getResources().getString(R.string.ui_get_artist_info), Toast.LENGTH_LONG).show();
    }

    private void speak(boolean finish){
        final String ID = "A";
        if(finish){
            button.setImageDrawable(getResources().getDrawable(R.drawable.icon_speaker, this.getTheme()));
            return;
        }
        if(!tts.isSpeaking()){
            tts.speak(titleTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, ID);
            tts.speak(authorTextView.getText().toString(), TextToSpeech.QUEUE_ADD, null, ID);
            tts.speak(yearTextView.getText().toString(), TextToSpeech.QUEUE_ADD, null, ID);
            tts.speak(descriptionTextView.getText().toString(), TextToSpeech.QUEUE_ADD, null, "LAST");
            button.setImageDrawable(getResources().getDrawable(R.drawable.icon_stop, this.getTheme()));
        }else {
            tts.stop();
            button.setImageDrawable(getResources().getDrawable(R.drawable.icon_speaker, this.getTheme()));
        }
    }

    void showArtistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.artist_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                TextView name = dialog.findViewById(R.id.artist_dialog_name_textview);
                TextView years = dialog.findViewById(R.id.artist_dialog_years_textview);
                TextView description = dialog.findViewById(R.id.artist_dialog_description_textview);
                String birthDeath = artwork.getArtist().getBirthDate() + " - " + artwork.getArtist().getDeathDate();
                name.setText(artwork.getArtist().getName());
                years.setText(birthDeath);
                description.setText(artwork.getArtist().getDescription());
            }
        });

        dialog.show();

    }

    public void onDestroy(){
        tts.shutdown();
        super.onDestroy();
    }
}
