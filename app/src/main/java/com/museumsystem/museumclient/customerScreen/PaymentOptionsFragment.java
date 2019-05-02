package com.museumsystem.museumclient.customerScreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.museumsystem.museumclient.dto.ErrorResponseDto;
import com.museumsystem.museumclient.dto.TicketDto;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class PaymentOptionsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private TicketDto ticket;
    private TextView amountTextView;
    private TextView paymentNrTextView;
    private View paymentMethodProgressBar;
    private View paymentMethodLayout;
    private Button creditCardButton;
    private Button transferButton;
    private Button payPalButton;

    private OnFragmentInteractionListener mListener;

    public PaymentOptionsFragment() {
        // Required empty public constructor
    }

    public static PaymentOptionsFragment newInstance(String param1, String param2) {
        PaymentOptionsFragment fragment = new PaymentOptionsFragment();
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
            ticket = (TicketDto) bundle.getSerializable("ticket_dto");
            bundle.remove("ticket_dto");
            Log.d("ticket_details", ticket.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_payment_options, container, false);
        amountTextView = rootView.findViewById(R.id.payment_amount_textview);
        paymentNrTextView = rootView.findViewById(R.id.payment_paymentnr_textview);
        paymentMethodLayout = rootView.findViewById(R.id.payment_method_layout);
        paymentMethodProgressBar = rootView.findViewById(R.id.payment_method_progressbar);
        creditCardButton = rootView.findViewById(R.id.payment_creditcard_button);
        transferButton = rootView.findViewById(R.id.payment_banktransfer_button);
        payPalButton = rootView.findViewById(R.id.payment_paypal_button);

        creditCardButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new paymentCallback() {
                    @Override
                    public void onFinish(boolean success) {
                        if (success) {
                            attemptPayment("Credit card");
                        }
                    }
                });
            }
        });

        transferButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                checkAccessToken(new paymentCallback() {
                    @Override
                    public void onFinish(boolean success) {
                        if (success) {
                            attemptPayment("Bank transfer");
                        }
                    }
                });
            }
        });

        payPalButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                checkAccessToken(new paymentCallback() {
                    @Override
                    public void onFinish(boolean success) {
                        if (success) {
                            attemptPayment("PayPal");
                        }
                    }
                });
            }
        });
        showProgress(false);



        if(ticket != null) {
            DecimalFormat df = new DecimalFormat("#0.00");
            amountTextView.setText(String.valueOf(df.format(ticket.getPrice())) + " PLN");
            paymentNrTextView.setText(ticket.getId().toString());
        }
        // Inflate the layout for this fragment
        return rootView;
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

    public interface paymentCallback {
        void onFinish(boolean success);
    }

    void checkAccessToken(final paymentCallback callback) {
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

    private void attemptPayment(String method) {
        showProgress(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);
        final String refreshToken = prefs.getString("refresh_token", null);
        JWT jwt = new JWT(refreshToken);
        String name = jwt.getClaim("user").asString();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("amount", String.valueOf(ticket.getPrice()));
        params.put("method", method);
        params.put("payer", name);
        params.put("description", ticket.getId().toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ServerValues.SERVER_MAKE_PAYMENT, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        showProgress(false);
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


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            paymentMethodLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            paymentMethodLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    paymentMethodLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            paymentMethodProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            paymentMethodProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    paymentMethodProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            paymentMethodProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            paymentMethodLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
