package com.museumsystem.museumclient.loginScreen.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RegisterActivity;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.customerScreen.CustomerActivity;
import com.museumsystem.museumclient.dto.TokenResponseDto;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class LoginFormFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView registerTextView;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private View mProgressView;
    private View loginFormView;

    public LoginFormFragment() {
        // Required empty public constructor
    }

    public static LoginFormFragment newInstance(String param1, String param2) {
        LoginFormFragment fragment = new LoginFormFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //TODO check if user is already logged in
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        registerTextView = (TextView) rootView.findViewById(R.id.registerTextView);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        emailEditText = (EditText) rootView.findViewById(R.id.loginEmalTextInput);
        passwordEditText = (EditText) rootView.findViewById(R.id.loginPasswordTextInput);
        signInButton = (Button) rootView.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFormValid())
                    attemptLogin();

            }
        });
        loginFormView = (View) rootView.findViewById(R.id.login_form_scrollview);
        mProgressView = (View) rootView.findViewById(R.id.login_progress);

        return rootView;
    }

    private boolean isEmailValid(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    private boolean isFormValid() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        emailEditText.setError(null);
        passwordEditText.setError(null);

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_field_required));
            focusView = emailEditText;
            validForm = false;
        } else if (!isEmailValid(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            focusView = emailEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_field_required));
            focusView = passwordEditText;
            validForm = false;
        }

        if (!validForm) {
            focusView.requestFocus();
        }

        return validForm;
    }

    public void attemptLogin() {
        final HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "password");
        requestBody.put("username", emailEditText.getText().toString());
        requestBody.put("password", passwordEditText.getText().toString());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerValues.SERVER_LOGIN_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                showProgress(false);
                ObjectMapper objectMapper = new ObjectMapper();
                TokenResponseDto responseDto;
                try {
                    responseDto = objectMapper.readValue(response, TokenResponseDto.class);
                    String refreshToken = responseDto.getRefreshToken();
                    JWT jwtRefreshToken = new JWT(refreshToken);
                    List<String> authorities = new ArrayList<>();
                    authorities = jwtRefreshToken.getClaim("authorities").asList(String.class);

                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE).edit();
                    editor.putString("access_token", responseDto.getAccessToken());
                    editor.putString("refresh_token", responseDto.getRefreshToken());
                    editor.apply();

                    Log.d("responseDto", responseDto.getAccessToken());
                    Log.d("JWT",authorities.get(0) );

                    if(authorities.get(0).equals("ROLE_CUSTOMER")){
                        Intent myIntent = new Intent(getActivity(), CustomerActivity.class);
                        myIntent.putExtra("username", jwtRefreshToken.getClaim("user").asString());
                        myIntent.putExtra("role", "customer");
                        getActivity().startActivity(myIntent);
                    }else if(authorities.get(0).equals("ROLE_EMPLOYEE")){
                        Intent myIntent = new Intent(getActivity(), EmployeeActivity.class);
                        myIntent.putExtra("username", jwtRefreshToken.getClaim("user").asString());
                        myIntent.putExtra("role", "employee");
                        getActivity().startActivity(myIntent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("https", "Response: " + response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                if ((error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                }else{
                    int status = error.networkResponse.statusCode;
                    if(status == 400){
                        if (error.networkResponse.data != null) {
                            String response = new String(error.networkResponse.data);
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                String errorDescription = jsonResponse.get("error_description").toString();
                                Log.d("httpserr", "Response: " + errorDescription);
                                if(errorDescription == null)
                                    Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                else if(errorDescription.equals("Bad credentials"))
                                    Toast.makeText(getActivity(), getResources().getString(R.string.error_bad_credentials), Toast.LENGTH_SHORT).show();
                                else if(errorDescription.equals("User is disabled"))
                                    Toast.makeText(getActivity(), getResources().getString(R.string.error_inactive_account), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("httpserr", "Response: " + response );
                        }
                    }
                }
            }
        }) {
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();

                String creds = String.format("%s:%s", ServerValues.CLIENT_USERNAME, ServerValues.CLIENT_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
                params.put("Authorization", auth);

                return params;
            }

            @Override
            protected HashMap<String, String> getParams() throws AuthFailureError {
                return requestBody;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };

        RequestManager.getInstance(getActivity()).addToRequestQueue(stringRequest);
        showProgress(true);
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

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

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
        void onFragmentInteraction(Uri uri);
    }
}
