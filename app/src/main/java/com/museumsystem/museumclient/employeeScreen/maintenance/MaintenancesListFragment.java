package com.museumsystem.museumclient.employeeScreen.maintenance;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.AccessTokenManager;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaintenancesListFragment extends Fragment implements MaintenanceAdapterCallback{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaintenanceItemAdapter maintenanceItemAdapter;
    private List<HashMap<String, String>> maintenances;

    private OnFragmentInteractionListener mListener;

    public MaintenancesListFragment() {
        // Required empty public constructor
    }

    public static MaintenancesListFragment newInstance(String param1, String param2) {
        MaintenancesListFragment fragment = new MaintenancesListFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_maintenances_list, container, false);
        recyclerView = rootView.findViewById(R.id.maintenances_recyclerView);
        progressBar = rootView.findViewById(R.id.maintenances_progressBar);
        maintenances = new ArrayList<>();
        maintenanceItemAdapter = new MaintenanceItemAdapter(getActivity(), maintenances);
        maintenanceItemAdapter.setCallback(this);
        recyclerView.setAdapter(maintenanceItemAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        checkAccessToken(new Callback() {
            @Override
            public void onFinish(boolean success) {
                if(success){
                    showProgress(true);
                    getMaintenances();
                }
            }
        });

        return rootView;
    }

    @Override
    public void buttonPressed(final Long id) {
        Log.d("MainList", "Button " + id.toString());
        checkAccessToken(new Callback() {
            @Override
            public void onFinish(boolean success) {
                if(success){
                    showProgress(true);
                    addMaintenance(id);
                }
            }
        });
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

    public void getMaintenances(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        String userEmail = jwt.getClaim("user_name").asString();
        String url = ServerValues.SERVER_ARTWORK + "/maintenance/" + userEmail;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                try {
                    List<HashMap<String, String>> mMaintenances = objectMapper.readValue(response, new TypeReference<List<HashMap<String, String>>>() {});
                    maintenances = mMaintenances;
                    maintenanceItemAdapter.updateMaintenancesList(maintenances);
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

    public void addMaintenance(Long id){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ServerValues.TOKEN_PREFS, Context.MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);
        JWT jwt = new JWT(accessToken);
        String url = ServerValues.SERVER_ARTWORK + "/maintenance/" + id.toString();
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false);

                    checkAccessToken(new Callback() {
                        @Override
                        public void onFinish(boolean success) {
                            if(success){
                                showProgress(true);
                                getMaintenances();
                            }
                        }
                    });
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            recyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

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
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
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
