package com.museumsystem.museumclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.dto.ErrorResponseDto;
import com.museumsystem.museumclient.loginScreen.LoginActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {// implements LoaderCallbacks<Cursor> {

    public static final String URL = ServerValues.SERVER_REGISTER_URL;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    // UI references.
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText rPasswordEditText;
    private View mProgressView;
    private View registerFormView;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameEditText = (EditText) findViewById(R.id.firstNameTextInput);
        lastNameEditText = (EditText) findViewById(R.id.lastNameTextInput);
        emailEditText = (EditText) findViewById(R.id.emailTextInput);

        //populateAutoComplete();
        passwordEditText = (EditText) findViewById(R.id.passwordTextInput);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                return true;
            }
        });

        rPasswordEditText = (EditText) findViewById(R.id.rPasswordTextInput);

        signUpButton = (Button) findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFormValid())
                    attemptRegister();
            }
        });

        registerFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }


    /**
     * Sends request to the server and reads the response code and response body
     */
    private void attemptRegister() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("firstName", firstNameEditText.getText().toString());
        params.put("lastName", lastNameEditText.getText().toString());
        params.put("email", emailEditText.getText().toString());
        params.put("password", passwordEditText.getText().toString());
        params.put("language", getResources().getConfiguration().locale.toString());

            //TODO bad encoding polish chars in database after request
        Log.d("json", params.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, URL, new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("volley", "onResponse");

                        showProgress(false);
                        showAlertDialog();
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("volley", "onErrorResponse");
                        NetworkResponse networkResponse = error.networkResponse;
                        showProgress(false);
                        if ((error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
                            Toast.makeText(getApplication(), getResources().getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                        } else {
                            if(networkResponse == null){
                                Toast.makeText(getApplication(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
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
                                            if (response.getCode().equals("email_already_used")) {
                                                Toast.makeText(getApplication(), getResources().getString(R.string.error_email_already_used), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplication(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(getApplication(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                    }

                                    Log.e("httpserr", error.toString() + " " + status + " " + json);
                                }
                            }
                        }
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getInstance(getApplication()).addToRequestQueue(jsonObjectRequest);
        showProgress(true);
    }

    /**
     * Validates the register form
     * @return true, if form is valid, otherwise, false
     */
    private boolean isFormValid() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String rPassword = rPasswordEditText.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        emailEditText.setError(null);
        passwordEditText.setError(null);
        rPasswordEditText.setError(null);

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

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_field_required));
            focusView = passwordEditText;
            validForm = false;
        } else if (!isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_password_length));
            focusView = passwordEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(rPassword)) {
            rPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = rPasswordEditText;
            validForm = false;
        } else if (!comparePasswords(password, rPassword)) {
            rPasswordEditText.setError(getString(R.string.error_different_passwords));
            focusView = rPasswordEditText;
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

    private boolean isPasswordValid(String password) {
        return (password.length() >= 4 && password.length() <= 16);
    }

    private boolean comparePasswords(String pass1, String pass2) {
        return pass1.equals(pass2);
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

            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            registerFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    void showAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        alertDialog.setMessage(getApplication().getResources().getString(R.string.message_account_created));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showLoginActivity();
                    }
                });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                showLoginActivity();
            }
        });
        alertDialog.show();
    }

    void showLoginActivity() {
        Intent i = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(i);
    }

}

