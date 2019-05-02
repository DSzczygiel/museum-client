package com.museumsystem.museumclient.employeeScreen.artists;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.AccessTokenManager;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.dto.ArtistDto;
import com.museumsystem.museumclient.dto.ErrorResponseDto;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class ArtistDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NEW_ARTIST = "newArtist";
    private static final String ARG_LANG = "lang";

    // TODO: Rename and change types of parameters
    private boolean newArtist;
    private String lang;
    private ArtistDto artist;
    private EditText nameEditText;
    private EditText birthDateEditText;
    private EditText deathDateEditText;
    private EditText descriptionEditText;
    private Button changeDataButton;
    private Button deleteArtistButton;
    private Button addArtistButton;
    private ProgressBar progressBar;

    private OnFragmentInteractionListener mListener;

    public ArtistDetailsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ArtistDetailsFragment newInstance(Boolean newArtist, String lang) {
        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NEW_ARTIST, newArtist);
        args.putString(ARG_LANG, lang);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newArtist = getArguments().getBoolean(ARG_NEW_ARTIST);
            lang = getArguments().getString(ARG_LANG);
        }

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            artist = (ArtistDto) bundle.getSerializable("artist_dto");
            bundle.remove("artist_dto");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_details, container, false);

        progressBar = rootView.findViewById(R.id.artist_details_progressBar);
        nameEditText = rootView.findViewById(R.id.artist_details_name_edittext);
        birthDateEditText = rootView.findViewById(R.id.artist_details_bdate_edittext);
        deathDateEditText = rootView.findViewById(R.id.artist_details_ddate_edittext);
        descriptionEditText = rootView.findViewById(R.id.artist_details_description_edittext);
        changeDataButton = rootView.findViewById(R.id.artist_details_change_button);
        deleteArtistButton = rootView.findViewById(R.id.artist_details_delete_button);
        addArtistButton = rootView.findViewById(R.id.artist_details_add_button);

        changeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new Callback() {
                    @Override
                    public void onFinish(boolean success) {
                        if(success)
                            if(validateForm()){
                                showProgress(true);
                                updateArtist();
                        }
                    }
                });
            }
        });

        addArtistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new Callback() {
                    @Override
                    public void onFinish(boolean success) {
                        if(success)
                            if(validateForm()){
                                showProgress(true);
                                addArtist();
                            }
                    }
                });
            }
        });

        deleteArtistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });

        if(!newArtist) {
            nameEditText.setText(artist.getName());
            birthDateEditText.setText(artist.getBirthDate());
            deathDateEditText.setText(artist.getDeathDate());
            descriptionEditText.setText(artist.getDescription());
            addArtistButton.setVisibility(View.GONE);
            deleteArtistButton.setVisibility(View.VISIBLE);
            changeDataButton.setVisibility(View.VISIBLE);
        }else{
            addArtistButton.setVisibility(View.VISIBLE);
            deleteArtistButton.setVisibility(View.GONE);
            changeDataButton.setVisibility(View.GONE);
        }
        return rootView;
    }

    public interface Callback {
        void onFinish(boolean success);
    }

    void checkAccessToken(final Callback callback) {
        AccessTokenManager.validateTokens(getActivity(), new AccessTokenManager.Callback() {
            @Override
            public void onSuccess(String result) {
                Log.d("check on success", result);
                if (result.equals("true")) {
                    callback.onFinish(true);
                    // attemptPurchase();
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

    private boolean validateForm(){
        String name = nameEditText.getText().toString();
        String bDate = birthDateEditText.getText().toString();
        String dDate = deathDateEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        nameEditText.setError(null);
        birthDateEditText.setError(null);
        deathDateEditText.setError(null);
        descriptionEditText.setError(null);

        // Validate fields
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(bDate)) {
            birthDateEditText.setError(getString(R.string.error_field_required));
            focusView = birthDateEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(dDate)) {
            deathDateEditText.setError(getString(R.string.error_field_required));
            focusView = deathDateEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError(getString(R.string.error_field_required));
            focusView = descriptionEditText;
            validForm = false;
        }

        if (!validForm) {
            focusView.requestFocus();
        }
        return validForm;
    }

    private void updateArtist() {
        showProgress(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);
        final String refreshToken = prefs.getString("refresh_token", null);
        JSONObject jsonObject = new JSONObject();
        JWT jwt = new JWT(refreshToken);
        String email = jwt.getClaim("user_name").asString();
        String url = ServerValues.SERVER_ARTIST + "/" + lang + "/" + artist.getId().toString();

        ArtistDto artistDto = new ArtistDto();
        artistDto.setBirthDate(birthDateEditText.getText().toString());
        artistDto.setDeathDate(deathDateEditText.getText().toString());
        artistDto.setDescription(descriptionEditText.getText().toString());
        artistDto.setName(nameEditText.getText().toString());


        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(artistDto);
            jsonObject = new JSONObject(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
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
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
    }

    public void addArtist(){
        showProgress(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);
        final String refreshToken = prefs.getString("refresh_token", null);
        JSONObject jsonObject = new JSONObject();
        JWT jwt = new JWT(refreshToken);
        String email = jwt.getClaim("user_name").asString();
        String url = ServerValues.SERVER_ARTIST + "/" + lang;

        ArtistDto artistDto = new ArtistDto();
        artistDto.setBirthDate(birthDateEditText.getText().toString());
        artistDto.setDeathDate(deathDateEditText.getText().toString());
        artistDto.setDescription(descriptionEditText.getText().toString());
        artistDto.setName(nameEditText.getText().toString());


        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(artistDto);
            jsonObject = new JSONObject(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.ui_artist_added), Toast.LENGTH_SHORT).show();
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
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
    }

    private void deleteArtist(){
        showProgress(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);
        final String refreshToken = prefs.getString("refresh_token", null);
        JWT jwt = new JWT(refreshToken);
        String url = ServerValues.SERVER_ARTIST + "/" + artist.getId().toString();

        StringRequest stringRequest = new StringRequest
                (Request.Method.DELETE, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.ui_artist_deleted), Toast.LENGTH_SHORT).show();
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

    private void showProgress(final boolean show) {

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
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    void showConfirmDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setMessage(getActivity().getResources().getString(R.string.prompt_delete_confirm));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ui_text_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        checkAccessToken(new Callback() {
                            @Override
                            public void onFinish(boolean success) {
                                if(success) {
                                    deleteArtist();
                                    showProgress(true);
                                }
                            }
                        });
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.ui_text_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        alertDialog.show();
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
