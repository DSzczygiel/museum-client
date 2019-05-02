package com.museumsystem.museumclient.employeeScreen.artworks;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.AccessTokenManager;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.dto.ArtistDto;
import com.museumsystem.museumclient.dto.ArtworkDto;
import com.museumsystem.museumclient.dto.ErrorResponseDto;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;
import com.museumsystem.museumclient.employeeScreen.artworks.operations.ArtworkOperationFragment;
import com.museumsystem.museumclient.employeeScreen.artworks.operations.OperationsListFragment;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class ArtworkDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NEW_ARTWORK = "newArtwork";
    private static final String ARG_LANG = "lang";
    ImageView artworkImageView;
    Button changeImageButton;
    Button addArtworkButton;
    Button changeArtworkButton;
    Button deleteArtworkButton;
    Button addOperationButton;
    Button operationsListButton;
    Spinner artistsSpinner;
    EditText nameEditText;
    EditText creationYearEditText;
    EditText descriptionEditText;
    Button getQrButton;
    ProgressBar progressBar;

    boolean newArtwork;
    String lang;
    ArtworkDto artwork;
    List<ArtistDto> artists;
    ArtistDto currentArtist;
    ArtistSpinnerAdapter artistSpinnerAdapter;
    private OnFragmentInteractionListener mListener;

    public static final int RESULT_LOAD_IMG = 777;

    public ArtworkDetailsFragment() {
        // Required empty public constructor
    }

    public static ArtworkDetailsFragment newInstance(boolean newArtwork, String lang) {
        ArtworkDetailsFragment fragment = new ArtworkDetailsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NEW_ARTWORK, newArtwork);
        args.putString(ARG_LANG, lang);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newArtwork = getArguments().getBoolean(ARG_NEW_ARTWORK);
            lang = getArguments().getString(ARG_LANG);
        }
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            artwork = (ArtworkDto) bundle.getSerializable("artwork_dto");
            bundle.remove("artwork_dto");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artwork_details, container, false);
        artworkImageView = rootView.findViewById(R.id.artwork_details_imageview);
        nameEditText = rootView.findViewById(R.id.artwork_details_name_edittext);
        creationYearEditText = rootView.findViewById(R.id.artwork_details_creationyear_edittext);
        descriptionEditText = rootView.findViewById(R.id.artwork_details_description_edittext);
        addArtworkButton = rootView.findViewById(R.id.artwork_details_addartwork_button);
        addOperationButton = rootView.findViewById(R.id.artwork_details_addoperation_button);
        changeArtworkButton = rootView.findViewById(R.id.artwork_details_changedata_button);
        deleteArtworkButton = rootView.findViewById(R.id.artwork_details_deleteartwork_button);
        operationsListButton = rootView.findViewById(R.id.artwork_details_operationslist_button);
        changeImageButton = rootView.findViewById(R.id.artwork_details_editimage_button);
        getQrButton = rootView.findViewById(R.id.artwork_details_generateqr_button);
        progressBar = rootView.findViewById(R.id.artwork_details_progressbar);
        artistsSpinner = rootView.findViewById(R.id.artwork_details_artists_spinner);
        artists = new ArrayList<>();

        getQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQrCodeDialog(artwork.getId());
                showProgress(true);
            }
        });

        changeArtworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new Callback() {
                    @Override
                    public void onFinish(boolean success) {
                        if(success)
                            if(validateForm()){
                                updateArtwork();
                                showProgress(true);
                            }
                    }
                });
            }
        });

        addArtworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new Callback() {
                    @Override
                    public void onFinish(boolean success) {
                        if(success)
                            if(validateForm()){
                                addArtwork();
                                showProgress(true);
                            }
                    }
                });
            }
        });


        deleteArtworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new Callback() {
                    @Override
                    public void onFinish(boolean success) {
                        if(success)
                            if(validateForm()){
                                deleteArtwork();
                                showProgress(true);
                            }
                    }
                });
            }
        });

        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                }
            }
        });

        addOperationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = new ArtworkOperationFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("artwork_dto", artwork);
                fragment.setArguments(bundle);

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.employee_framelayout, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        operationsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = new OperationsListFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("artwork_dto", artwork);
                fragment.setArguments(bundle);

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.employee_framelayout, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        if(!newArtwork){
            byte[] photo = artwork.getPhoto();
            artworkImageView.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.length));
            nameEditText.setText(artwork.getTitle());
            creationYearEditText.setText(artwork.getCreationYear().toString());
            descriptionEditText.setText(artwork.getDescription());
            addArtworkButton.setVisibility(View.GONE);
            addOperationButton.setVisibility(View.VISIBLE);
            changeArtworkButton.setVisibility(View.VISIBLE);
            deleteArtworkButton.setVisibility(View.VISIBLE);
            getQrButton.setVisibility(View.VISIBLE);
            operationsListButton.setVisibility(View.VISIBLE);
        }else{
            addArtworkButton.setVisibility(View.VISIBLE);
            addOperationButton.setVisibility(View.GONE);
            changeArtworkButton.setVisibility(View.GONE);
            deleteArtworkButton.setVisibility(View.GONE);
            getQrButton.setVisibility(View.GONE);
            operationsListButton.setVisibility(View.GONE);
        }

        checkAccessToken(new Callback() {
            @Override
            public void onFinish(boolean success) {
                if(success){
                    getArtists();
                    showProgress(true);
                }
            }
        });

        artistSpinnerAdapter = new ArtistSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_item, artists);
        artistsSpinner.setAdapter(artistSpinnerAdapter);
        artistsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentArtist = artists.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootView;
    }

    public interface Callback {
        void onFinish(boolean success);
    }

    void checkAccessToken(final Callback callback) {
        AccessTokenManager.validateTokens(getActivity(), new AccessTokenManager.Callback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("true")) {
                    callback.onFinish(true);
                } else if (result.equals("error_no_token")) {
                    callback.onFinish(false);
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_sign_in_again), Toast.LENGTH_SHORT).show();
                    ((EmployeeActivity) getActivity()).signOut();
                } else {
                    callback.onFinish(false);
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getArtists(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        String url = ServerValues.SERVER_ARTIST + "/" + lang;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                try {
                    List<ArtistDto> mArtists = objectMapper.readValue(response, new TypeReference<List<ArtistDto>>() {});
                    artists = mArtists;
                    artistSpinnerAdapter.update(artists);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                showProgress(false);
                if ((error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                } else {
                    if (networkResponse == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);

                return headers;
            }
        };
        RequestManager.getInstance(getActivity()).addToRequestQueue(request);
    }

    public void updateArtwork(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        JSONObject jsonObject = new JSONObject();
        String url = ServerValues.SERVER_ARTWORK + "/" + lang + "/" + artwork.getId().toString();
        ArtworkDto artworkDto = new ArtworkDto();

        artworkDto.setCreationYear(Integer.valueOf(creationYearEditText.getText().toString()));
        artworkDto.setDescription(descriptionEditText.getText().toString());
        artworkDto.setTitle(nameEditText.getText().toString());
        artworkDto.setArtist(currentArtist);

        Bitmap bitmap = ((BitmapDrawable) artworkImageView.getDrawable()).getBitmap();
        if(bitmap.getWidth() > 1024 || bitmap.getHeight() > 1024){
            bitmap = resize(bitmap, 800, 800);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInByte = baos.toByteArray();
        artworkDto.setPhoto(imageInByte);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(artworkDto);
            jsonObject = new JSONObject(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getActivity(), getResources().getString(R.string.ui_data_changed), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                // TODO: Handle error
                showProgress(false);
                if ((error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                } else {
                    if (networkResponse == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        RequestManager.getInstance(getActivity()).addToRequestQueue(request);
    }

    public void addArtwork(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        JSONObject jsonObject = new JSONObject();
        String url = ServerValues.SERVER_ARTWORK + "/" + lang;
        ArtworkDto artworkDto = new ArtworkDto();

        artworkDto.setCreationYear(Integer.valueOf(creationYearEditText.getText().toString()));
        artworkDto.setDescription(descriptionEditText.getText().toString());
        artworkDto.setTitle(nameEditText.getText().toString());
        artworkDto.setArtist(currentArtist);

        Bitmap bitmap = ((BitmapDrawable) artworkImageView.getDrawable()).getBitmap();
        if(bitmap.getWidth() > 1024 || bitmap.getHeight() > 1024){
            bitmap = resize(bitmap, 800, 800);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInByte = baos.toByteArray();
        artworkDto.setPhoto(imageInByte);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(artworkDto);
            jsonObject = new JSONObject(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getActivity(), getResources().getString(R.string.ui_artwork_added), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                // TODO: Handle error
                showProgress(false);
                if ((error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                } else {
                    if (networkResponse == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        RequestManager.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void deleteArtwork(){
        showProgress(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);
        final String refreshToken = prefs.getString("refresh_token", null);
        JWT jwt = new JWT(refreshToken);
        String url = ServerValues.SERVER_ARTWORK + "/" + artwork.getId().toString();

        StringRequest stringRequest = new StringRequest
                (Request.Method.DELETE, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.ui_artwork_deleted), Toast.LENGTH_SHORT).show();
                        showProgress(false);
                        FragmentManager fm = getFragmentManager();
                        fm.popBackStack();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        // TODO: Handle error
                        showProgress(false);
                        if ((error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                        } else {
                            if (networkResponse == null) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int status = error.networkResponse.statusCode;

                            if (status == 400) {
                                if (error.networkResponse.data != null) {
                                    String json = new String(error.networkResponse.data);

                                    if (json != null) {
                                        //TODO error handling
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        try {
                                            ErrorResponseDto response = objectMapper.readValue(json, ErrorResponseDto.class);

                                            if (response.getCode().equals("error")) {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                    }

                                    Log.e("httpserr", error.toString() + " " + status + " " + json);
                                }
                            }
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };
        RequestManager.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private boolean validateForm(){
        String name = nameEditText.getText().toString();
        String year = creationYearEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        nameEditText.setError(null);
        creationYearEditText.setError(null);
        descriptionEditText.setError(null);

        // Validate fields
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            validForm = false;
        }
        if (TextUtils.isEmpty(year)) {
            creationYearEditText.setError(getString(R.string.error_field_required));
            focusView = creationYearEditText;
            validForm = false;
        }
        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError(getString(R.string.error_field_required));
            focusView = descriptionEditText;
            validForm = false;
        }

        if(artworkImageView.getDrawable() == null){
            Toast.makeText(getActivity(), getResources().getString(R.string.error_image_required), Toast.LENGTH_SHORT).show();
            validForm = false;
        }

        if (!validForm) {
            focusView.requestFocus();
        }
        return validForm;
    }

    void showQrCodeDialog(final Long id) {
        showProgress(false);

        ByteArrayOutputStream stream = QRCode.from(id.toString()).withSize(1000, 1000).stream();
        byte[] img = stream.toByteArray();
        final Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.ui_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                } else {
                    saveImage(bitmap);
                }
            }
        });

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.qr_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);



        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                final ImageView imageView = (ImageView) dialog.findViewById(R.id.qrdialog_imageview);

                final float imageWidthInPX = (float)imageView.getWidth();

                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        // LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Math.round(imageWidthInPX),
                        //      Math.round(imageWidthInPX * (float)bitmap.getHeight() / (float)bitmap.getWidth()));
                        // imageView.setLayoutParams(layoutParams);
                        imageView.setImageBitmap(bitmap);
                        showProgress(false);

                    }
                });
            }
        });

        dialog.show();
    }

    private void saveImage(Bitmap bitmap){
        FileOutputStream outStream = null;
        File museumSystem = Environment.getExternalStorageDirectory();
        File dir = new File(museumSystem.getAbsolutePath());
        //dir.mkdirs();
        String fileName = String.format("%s.jpg", artwork.getTitle());
        File outFile = new File(dir, fileName);
        try {
            outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        try {
            outStream.flush();
            outStream.close();
            Toast.makeText(getActivity(), getResources().getString(R.string.ui_qr_saved), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Perms", "granted read");
                } else {
                    Log.d("Perms", "denied read");
                    Toast.makeText(getActivity(), "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                artworkImageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getActivity(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
