package com.museumsystem.museumclient.loginScreen.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.Result;
import com.museumsystem.museumclient.ArtworkActivity;
import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.RequestManager;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.dto.ArtworkDto;

import java.io.IOException;

public class ScannerFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    CodeScannerView codeScannerView;
    CodeScanner codeScanner;
    private static final int CAMERA_REQUEST_CODE = 100;
    private Boolean cameraAccess = false;
    private ProgressBar progressBar;

    public ScannerFragment() {
        // Required empty public constructor
    }

    public static ScannerFragment newInstance(String param1, String param2) {
        ScannerFragment fragment = new ScannerFragment();
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

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        else
            cameraAccess = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scanner, container, false);
        progressBar = rootView.findViewById(R.id.scanner_progressBar);
        Log.d("cam", "oncreateview");
        if(cameraAccess){
            initScanner(rootView);
        }

        //Hide keyboard
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        return rootView;
    }

    public void initScanner(View rootView){
        codeScannerView = (CodeScannerView) rootView.findViewById(R.id.scannerView);

        codeScanner = new CodeScanner(getActivity(), codeScannerView);
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), result.getText(), Toast.LENGTH_SHORT).show();
                        getArtwork(Long.valueOf(result.getText()));
                        showProgress(true);
                    }
                });
            }
        });
        codeScanner.startPreview();

        codeScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScanner.startPreview();
            }
        });
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
        Log.d("cam", "detach");

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(cameraAccess)
            codeScanner.startPreview();
        Log.d("cam", "resume");

    }

    @Override
    public void onPause() {
        super.onPause();
        if(cameraAccess)
            codeScanner.releaseResources();
        Log.d("cam", "pause");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if(requestCode == CAMERA_REQUEST_CODE){
            /**
             *
             * Restart activity to make scanner works
             */
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);

               // cameraAccess = true;
            } else {
               // cameraAccess = false;
            }
            return;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("cam", "ondesrtroyview");

    }

    public void getArtwork(Long id) {
        String url = ServerValues.SERVER_ARTWORK + "/" + id.toString() + "/" + getResources().getConfiguration().locale.getLanguage();

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

                try {
                    ArtworkDto artwork = objectMapper.readValue(response, ArtworkDto.class);
                    Intent intent = new Intent(getActivity(), ArtworkActivity.class);
                    intent.putExtra("artwork", artwork);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                // TODO: Handle error & progress bar
                showProgress(false);
                codeScanner.startPreview();
                if ((error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                } else {
                    if (networkResponse == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
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
}
