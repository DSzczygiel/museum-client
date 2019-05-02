package com.museumsystem.museumclient.employeeScreen.artworks.operations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.AccessTokenManager;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.customerScreen.CustomerActivity;
import com.museumsystem.museumclient.dto.ArtworkDto;
import com.museumsystem.museumclient.dto.ErrorResponseDto;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ArtworkOperationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView artworkTextView;
    private EditText dateEditText;
    private EditText descriptionEditText;
    private Button addOperationButton;
    private ProgressBar progressBar;

    ArtworkDto artwork;

    public ArtworkOperationFragment() {
        // Required empty public constructor
    }

    public static ArtworkOperationFragment newInstance(String param1, String param2) {
        ArtworkOperationFragment fragment = new ArtworkOperationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
        View rootView = inflater.inflate(R.layout.fragment_artwork_operation, container, false);

        artworkTextView = rootView.findViewById(R.id.artwork_operation_name_textview);
        dateEditText = rootView.findViewById(R.id.artwork_operation_date_edittext);
        descriptionEditText = rootView.findViewById(R.id.artwork_operation_description_edittext);
        addOperationButton = rootView.findViewById(R.id.artwork_operation_add_button);
        progressBar = rootView.findViewById(R.id.artwork_operation_progressbar);

        artworkTextView.setText(artwork.getTitle() + " - " + artwork.getArtist().getName());

        addOperationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new Callback() {
                    @Override
                    public void onFinish(boolean success) {
                        if(success)
                            if (validateForm()) {
                                addOperation();
                                showProgress(true);
                            }
                    }
                });
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
                Log.d("check on success", result);
                if (result.equals("true")) {
                    callback.onFinish(true);
                    // attemptPurchase();
                } else if (result.equals("error_no_token")) {
                    callback.onFinish(false);
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_sign_in_again), Toast.LENGTH_SHORT).show();
                    ((CustomerActivity) getActivity()).signOut();
                } else {
                    callback.onFinish(false);
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addOperation() {
        showProgress(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);
        final String refreshToken = prefs.getString("refresh_token", null);
        JWT jwt = new JWT(refreshToken);
        String name = jwt.getClaim("username").asString();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("date", dateEditText.getText().toString());
        params.put("description", descriptionEditText.getText().toString());

        String url = ServerValues.SERVER_ARTWORK + "/operation/" + artwork.getId();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        showProgress(false);
                        Toast.makeText(getActivity(), getResources().getString(R.string.ui_operation_added), Toast.LENGTH_SHORT).show();
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

    /**
     * Shows the progress UI and hides the login form.
     */
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
        String date = dateEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        dateEditText.setError(null);
        descriptionEditText.setError(null);

        // Validate fields
        if (TextUtils.isEmpty(date)) {
            dateEditText.setError(getString(R.string.error_field_required));
            focusView = dateEditText;
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
