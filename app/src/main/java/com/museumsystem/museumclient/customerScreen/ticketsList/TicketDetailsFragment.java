package com.museumsystem.museumclient.customerScreen.ticketsList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
import com.android.volley.toolbox.StringRequest;
import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.AccessTokenManager;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.customerScreen.CustomerActivity;
import com.museumsystem.museumclient.customerScreen.PaymentOptionsFragment;
import com.museumsystem.museumclient.dto.SuccessResponseDto;
import com.museumsystem.museumclient.dto.TicketDto;

import net.glxn.qrgen.android.QRCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class TicketDetailsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    TextView validDateValueTextView;
    TextView orderDateValueTextView;
    TextView adultsValueTextView;
    TextView childrenValueTextView;
    TextView priceValueTextView;
    TextView statusValueTextView;
    Button payButton;
    Button getQrButton;
    TicketDto ticket;
    ProgressBar progressBar;

    private OnFragmentInteractionListener mListener;

    public TicketDetailsFragment() {
        // Required empty public constructor
    }

    public static TicketDetailsFragment newInstance(String param1, String param2) {
        TicketDetailsFragment fragment = new TicketDetailsFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_ticket_details, container, false);

        progressBar = rootView.findViewById(R.id.ticketdetails_progressbar);
        validDateValueTextView = rootView.findViewById(R.id.ticketdetails_validdate_value_textview);
        orderDateValueTextView = rootView.findViewById(R.id.ticketdetails_orderdate_value_textview);
        adultsValueTextView = rootView.findViewById(R.id.ticketdetails_adults_value_textview);
        childrenValueTextView = rootView.findViewById(R.id.ticketdetails_children_value_textview);
        priceValueTextView = rootView.findViewById(R.id.ticketdetails_price_value_textview);
        statusValueTextView = rootView.findViewById(R.id.ticketdetails_status_value_textview);
        payButton = rootView.findViewById(R.id.ticketdetails_pay_button);
        getQrButton = rootView.findViewById(R.id.ticketdetails_getqr_button);

        if(ticket != null) {
            validDateValueTextView.setText(ticket.getDate());
            orderDateValueTextView.setText(ticket.getOrderDate());
            adultsValueTextView.setText(String.valueOf(ticket.getAdultsNr()));
            childrenValueTextView.setText(String.valueOf(ticket.getChildrenNr()));
            DecimalFormat df = new DecimalFormat("#0.00");
            priceValueTextView.setText(String.valueOf(df.format(ticket.getPrice())) + " PLN");

            String status = ticket.getStatus();
            switch (status){
                case "valid":
                    statusValueTextView.setText(getResources().getString(R.string.ticket_status_valid));
                    break;
                case "unpaid":
                    statusValueTextView.setText(getResources().getString(R.string.ticket_status_unpaid));
                    break;
                default:
                    statusValueTextView.setText(getResources().getString(R.string.ticket_status_expired));
                    break;
            }

            if(!ticket.getStatus().equals("unpaid"))
                payButton.setVisibility(View.GONE);

            if(!ticket.getStatus().equals("valid"))
                getQrButton.setVisibility(View.GONE);
        }
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        getQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessToken(new listCallback() {
                    @Override
                    public void onFinish(boolean success) {
                        if(success) {
                            getTicketCode();
                            showProgress(true);
                        }
                    }
                });
            }
        });
        showProgress(false);
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

    public void getTicketCode(){
        String userEmail = null;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        userEmail = jwt.getClaim("user_name").asString();

        String url = ServerValues.SERVER_TICKET_PURCHASE_URL + "/qr/" + ticket.getId().toString() + "/" + userEmail;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    SuccessResponseDto successResponse = objectMapper.readValue(response, SuccessResponseDto.class);
                    showQrCodeDialog(successResponse.getMessages().get("token"));
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
                    if(networkResponse == null){
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

    void showQrCodeDialog(final String token) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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
                ByteArrayOutputStream stream = QRCode.from(token).withSize(1000, 1000).stream();
                final ImageView imageView = (ImageView) dialog.findViewById(R.id.qrdialog_imageview);
                byte[] img = stream.toByteArray();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);

                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });

        dialog.show();
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
