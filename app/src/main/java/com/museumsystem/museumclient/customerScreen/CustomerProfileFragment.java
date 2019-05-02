package com.museumsystem.museumclient.customerScreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import com.android.volley.toolbox.StringRequest;
import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.AccessTokenManager;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.dto.CustomerInfoDto;
import com.museumsystem.museumclient.dto.ErrorResponseDto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerProfileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText rNewPasswordEditText;
    private TextView registrationDate;
    private TextView daysFromRegister;
    private TextView orderedTickets;
    private Button changeDataButton;
    private Button changePasswordButton;
    private Button deleteAccountButton;
    private ProgressBar progressBar;

    public CustomerProfileFragment() {
        // Required empty public constructor
    }

    public static CustomerProfileFragment newInstance(String param1, String param2) {
        CustomerProfileFragment fragment = new CustomerProfileFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_customer_profile, container, false);

        firstNameEditText = rootView.findViewById(R.id.cust_details_fname_textinput);
        lastNameEditText = rootView.findViewById(R.id.cust_details_lname_textinput);
        emailEditText = rootView.findViewById(R.id.cust_details_email_textinput);
        registrationDate = rootView.findViewById(R.id.cust_details_regdate_val_textview);
        daysFromRegister = rootView.findViewById(R.id.cust_details_daysfromreg_val_textview);
        orderedTickets = rootView.findViewById(R.id.cust_details_ticketsnr_val_textview);
        progressBar = rootView.findViewById(R.id.cust_details_progressbar);
        oldPasswordEditText = rootView.findViewById(R.id.cust_details_oldpass_edittext);
        newPasswordEditText = rootView.findViewById(R.id.cust_details_newpass_edittext);
        rNewPasswordEditText = rootView.findViewById(R.id.cust_details_rnewpass_edittext);

        changeDataButton = rootView.findViewById(R.id.cust_details_changedata_button);
        changePasswordButton = rootView.findViewById(R.id.cust_details_changepass_button);
        deleteAccountButton = rootView.findViewById(R.id.cust_details_deleteacc_button);

        changeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new listCallback() {
                    @Override
                    public void onFinish(boolean success) {
                        if (success)
                            if (validateData()) {
                                changeUserData();
                                showProgress(true);
                            }
                    }
                });
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new listCallback() {
                    @Override
                    public void onFinish(boolean success) {
                        if (success)
                            if (validatePasswords()) {
                                changeUserPassword();
                                showProgress(true);
                            }
                    }
                });
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });

        showProgress(true);
        checkAccessToken(new listCallback() {
            @Override
            public void onFinish(boolean success) {
                if (success) {
                    getUserInfo();
                }
            }
        });

        return rootView;
    }

    public interface listCallback {
        void onFinish(boolean success);
    }

    void checkAccessToken(final listCallback callback) {
        AccessTokenManager.validateTokens(getActivity(), new AccessTokenManager.Callback() {
            @Override
            public void onSuccess(String result) {
                Log.d("check on success", result);
                if (result.equals("true")) {
                    callback.onFinish(true);
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

    boolean validateData() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        emailEditText.setError(null);

        // Validate fields
        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError(getString(R.string.error_field_required));
            focusView = firstNameEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError(getString(R.string.error_field_required));
            focusView = lastNameEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_field_required));
            focusView = emailEditText;
            validForm = false;
        } else if (!isEmailValid(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            focusView = emailEditText;
            validForm = false;
        }

        if (!validForm) {
            focusView.requestFocus();
        }
        return validForm;
    }

    private boolean isEmailValid(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }


    private boolean validatePasswords() {
        String oldPassword = oldPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String rNewPassword = rNewPasswordEditText.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        oldPasswordEditText.setError(null);
        newPasswordEditText.setError(null);
        rNewPasswordEditText.setError(null);

        if (TextUtils.isEmpty(oldPassword)) {
            oldPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = oldPasswordEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = newPasswordEditText;
            validForm = false;
        } else if (!isPasswordValid(newPassword)) {
            newPasswordEditText.setError(getString(R.string.error_password_length));
            focusView = newPasswordEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(rNewPassword)) {
            rNewPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = rNewPasswordEditText;
            validForm = false;
        } else if (!comparePasswords(newPassword, rNewPassword)) {
            rNewPasswordEditText.setError(getString(R.string.error_different_passwords));
            focusView = rNewPasswordEditText;
            validForm = false;
        }

        if (!validForm) {
            focusView.requestFocus();
        }
        return validForm;
    }

    private boolean isPasswordValid(String password) {
        return (password.length() >= 4 && password.length() <= 16);
    }

    private boolean comparePasswords(String pass1, String pass2) {
        return pass1.equals(pass2);
    }

    public void getUserInfo() {
        String userEmail = null;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        userEmail = jwt.getClaim("user_name").asString();

        String url = ServerValues.SERVER_CUSTOMER + userEmail;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    CustomerInfoDto custInfo = objectMapper.readValue(response, CustomerInfoDto.class);
                    firstNameEditText.setText(custInfo.getFirstName());
                    lastNameEditText.setText(custInfo.getLastName());
                    emailEditText.setText(custInfo.getEmail());
                    daysFromRegister.setText(custInfo.getDaysFromRegister().toString());
                    registrationDate.setText(custInfo.getRegisterDate());
                    orderedTickets.setText(custInfo.getOrderedTickets().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        RequestManager.getInstance(getActivity()).addToRequestQueue(request);
    }

    public void changeUserData() {
        String userEmail = null;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        userEmail = jwt.getClaim("user_name").asString();
        String url = ServerValues.SERVER_CUSTOMER + userEmail + "/changedata";
        JSONObject jsonObject = new JSONObject();

        CustomerInfoDto custInfo = new CustomerInfoDto();
        custInfo.setFirstName(firstNameEditText.getText().toString());
        custInfo.setLastName(lastNameEditText.getText().toString());
        custInfo.setEmail(emailEditText.getText().toString());
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(custInfo);
            jsonObject = new JSONObject(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showProgress(false);
                Toast.makeText(getActivity(), getResources().getString(R.string.ui_data_changed), Toast.LENGTH_SHORT).show();
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
                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                    ErrorResponseDto response = objectMapper.readValue(json, ErrorResponseDto.class);
                                    Log.d("Error resp", response.getCode());

                                    if (response.getCode().equals("err_email_used")) {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.error_email_already_used), Toast.LENGTH_SHORT).show();
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

        RequestManager.getInstance(getActivity()).addToRequestQueue(request);
    }

    public void changeUserPassword() {
        String userEmail = null;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        userEmail = jwt.getClaim("user_name").asString();
        String url = ServerValues.SERVER_CUSTOMER + userEmail + "/changepassword";
        JSONObject jsonObject = new JSONObject();

        HashMap<String, String> passwords = new HashMap<>();
        passwords.put("old_password", oldPasswordEditText.getText().toString());
        passwords.put("new_password", newPasswordEditText.getText().toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(passwords);
            jsonObject = new JSONObject(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showProgress(false);
                Toast.makeText(getActivity(), getResources().getString(R.string.ui_password_changed), Toast.LENGTH_SHORT).show();
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
                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                    ErrorResponseDto response = objectMapper.readValue(json, ErrorResponseDto.class);
                                    Log.d("Error resp", response.getCode());

                                    if (response.getCode().equals("err_wrong_pass")) {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.error_current_password_incorrect), Toast.LENGTH_SHORT).show();
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

        RequestManager.getInstance(getActivity()).addToRequestQueue(request);
    }

    public void deleteUser() {
        String userEmail = null;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        userEmail = jwt.getClaim("user_name").asString();
        String url = ServerValues.SERVER_CUSTOMER + userEmail;

        StringRequest request = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false);
                Toast.makeText(getActivity(), getResources().getString(R.string.ui_account_deleted), Toast.LENGTH_SHORT).show();
                ((CustomerActivity)getActivity()).signOut();
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
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        RequestManager.getInstance(getActivity()).addToRequestQueue(request);
    }

    void showConfirmDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setMessage(getActivity().getResources().getString(R.string.prompt_delete_confirm));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ui_text_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       checkAccessToken(new listCallback() {
                           @Override
                           public void onFinish(boolean success) {
                               if(success) {
                                   deleteUser();
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
