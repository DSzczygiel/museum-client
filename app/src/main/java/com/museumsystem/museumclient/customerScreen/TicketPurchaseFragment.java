package com.museumsystem.museumclient.customerScreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.AccessTokenManager;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.dto.ErrorResponseDto;
import com.museumsystem.museumclient.dto.TicketDto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class TicketPurchaseFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ImageButton calendarButton;
    private Button orderAndPayButton;
    private Button orderAndPayLaterButton;
    private EditText adultsNrEditText;
    private EditText childrenNrEditText;
    private TextView pickedDateTextView;
    private Calendar calendar = Calendar.getInstance();
    private View ticketPurchaseProgressBar;
    private View ticketPurchaseScrollView;

    public TicketPurchaseFragment() {
        // Required empty public constructor
    }

    public static TicketPurchaseFragment newInstance(String param1, String param2) {
        TicketPurchaseFragment fragment = new TicketPurchaseFragment();
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_ticket_purchase, container, false);

        pickedDateTextView = (TextView) rootView.findViewById(R.id.ticket_select_date_textview);
        childrenNrEditText = (EditText) rootView.findViewById(R.id.children_nr_edittext);
        childrenNrEditText.setText("0");
        adultsNrEditText = (EditText) rootView.findViewById(R.id.adults_nr_edittext);
        adultsNrEditText.setText("1");
        orderAndPayButton = (Button) rootView.findViewById(R.id.ticket_order_and_pay_button);
        orderAndPayButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFormValid()) {
                    checkAccessToken(new purchaseCallback() {
                        @Override
                        public void onFinish(boolean success) {
                            if (success) {
                                attemptPurchase(true);
                            }
                        }
                    });
                }
            }
        });

        orderAndPayLaterButton = (Button) rootView.findViewById(R.id.ticket_order_pay_later_button);
        orderAndPayLaterButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isFormValid()) {
                    checkAccessToken(new purchaseCallback() {
                        @Override
                        public void onFinish(boolean success) {
                            if (success) {
                                attemptPurchase(false);
                            }
                        }
                    });
                }
            }
        });
        calendarButton = (ImageButton) rootView.findViewById(R.id.ticket_calendar_imagebutton);
        calendarButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        pickedDateTextView.setText(String.format("%02d", dayOfMonth) + "." + String.format("%02d", month + 1) + "." + year);
                    }
                };

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.show();
            }
        });
        ticketPurchaseProgressBar = (View) rootView.findViewById(R.id.ticket_purchase_progressbar);
        ticketPurchaseScrollView = (View) rootView.findViewById(R.id.ticket_purchase_scrollview);

        return rootView;
    }


    private boolean isFormValid() {
        String children = childrenNrEditText.getText().toString();
        String adults = adultsNrEditText.getText().toString();
        String date = pickedDateTextView.getText().toString();
        View focusView = null;
        boolean validForm = true;

        // Reset errors.
        childrenNrEditText.setError(null);
        adultsNrEditText.setError(null);


        if (adults != null && !adults.isEmpty()) {
            if (Integer.parseInt(adults) == 0) {
                adultsNrEditText.setError(getString(R.string.error_person_amount));
                focusView = adultsNrEditText;
                validForm = false;
            }
        }

        if (TextUtils.isEmpty(children)) {
            childrenNrEditText.setError(getString(R.string.error_field_required));
            focusView = childrenNrEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(adults)) {
            adultsNrEditText.setError(getString(R.string.error_field_required));
            focusView = adultsNrEditText;
            validForm = false;
        }

        if (TextUtils.isEmpty(date)) {
            pickedDateTextView.setError(getString(R.string.error_field_required));
            validForm = false;
        }

        if (!isDateValid(date)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_date), Toast.LENGTH_SHORT).show();
            validForm = false;
        }

        if (isDateWeekend(date)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_day), Toast.LENGTH_SHORT).show();
            validForm = false;
        }

        if (!validForm && focusView != null) {
            focusView.requestFocus();
        }
        return validForm;
    }

    boolean isDateValid(String date) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyy");
        try {
            Date tmp = format.parse(date);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean isDateWeekend(String date) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyy");
        try {
            Date tmp = format.parse(date);
            cal.setTime(tmp);
            int day = cal.get(Calendar.DAY_OF_WEEK);
            Log.d("Calendar dayofweek", String.valueOf(day));
            Log.d("Calendar dayofmonth", String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            Log.d("Calendar month", String.valueOf(cal.get(Calendar.MONTH)));
            Log.d("Calendar year", String.valueOf(cal.get(Calendar.YEAR)));

            return day == Calendar.SUNDAY || day == Calendar.SATURDAY;
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    public interface purchaseCallback {
        void onFinish(boolean success);
    }

    void checkAccessToken(final purchaseCallback callback) {
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

    private void attemptPurchase(final boolean payNow) {
        showProgress(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);
        final String refreshToken = prefs.getString("refresh_token", null);
        JSONObject jsonObject = new JSONObject();
        JWT jwt = new JWT(refreshToken);
        String email = jwt.getClaim("user_name").asString();

        TicketDto ticketDto = new TicketDto();
        ticketDto.setAdultsNr(Integer.valueOf(adultsNrEditText.getText().toString()));
        ticketDto.setChildrenNr(Integer.valueOf(childrenNrEditText.getText().toString()));
        ticketDto.setDate(pickedDateTextView.getText().toString());
        ticketDto.setCustomerEmail(email);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ticketDto);
            jsonObject = new JSONObject(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ServerValues.SERVER_TICKET_PURCHASE_URL, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        showProgress(false);
                        if (!payNow) {
                            showSuccessDialog();
                        } else {
                            ObjectMapper objectMapper = new ObjectMapper();
                            try {
                                TicketDto ticket = objectMapper.readValue(response.toString(), TicketDto.class);
                                if (ticket != null) {
                                    CustomerActivity customerActivity = (CustomerActivity) getActivity();
                                    FragmentManager manager = customerActivity.getSupportFragmentManager();
                                    Fragment fragment = new PaymentOptionsFragment();
                                    Bundle bundle = new Bundle();

                                    bundle.putSerializable("ticket_dto", ticket);
                                    fragment.setArguments(bundle);

                                    FragmentTransaction transaction = manager.beginTransaction();
                                    transaction.replace(R.id.customer_framelayout, fragment);
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                } else {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

                            int status = error.networkResponse.statusCode;

                            if (status == 400) {
                                if (error.networkResponse.data != null) {
                                    String json = new String(error.networkResponse.data);

                                    if (json != null) {
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

            ticketPurchaseScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
            ticketPurchaseScrollView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ticketPurchaseScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            ticketPurchaseProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            ticketPurchaseProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ticketPurchaseProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            ticketPurchaseProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            ticketPurchaseScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.ui_dialog_ticket_ordered)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentManager fm = getFragmentManager();
                        fm.popBackStack();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
