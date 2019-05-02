package com.museumsystem.museumclient.customerScreen.menu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.customerScreen.CustomerActivity;
import com.museumsystem.museumclient.loginScreen.fragments.ScannerFragment;

import java.util.ArrayList;
import java.util.List;

public class CustomerMenuFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    TextView roleTextView;
    TextView usernameTextView;
    RecyclerView recyclerView;
    List<String> menuTitles;
    CustomerMenuItemAdapter customerMenuItemAdapter;
    TextView signOutTextView;
    FloatingActionButton floatingActionButton;

    private OnFragmentInteractionListener mListener;

    public CustomerMenuFragment() {
        // Required empty public constructor
    }

    public static CustomerMenuFragment newInstance(String param1, String param2) {
        CustomerMenuFragment fragment = new CustomerMenuFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_customer_menu, container, false);
        Intent intent = getActivity().getIntent();
        roleTextView = (TextView) rootView.findViewById(R.id.role_textview);
        usernameTextView = (TextView) rootView.findViewById(R.id.username_textview);
        usernameTextView.setText(intent.getStringExtra("username"));
        signOutTextView = (TextView) rootView.findViewById(R.id.sign_out_textview);
        signOutTextView.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
            }
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.customer_menu_recyclerview);
        menuTitles = new ArrayList<>();
        customerMenuItemAdapter = new CustomerMenuItemAdapter(getActivity(), menuTitles);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(customerMenuItemAdapter);
        floatingActionButton = rootView.findViewById(R.id.cust_menu_floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.customer_framelayout, new ScannerFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        prepareMenu();

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

    private void prepareMenu(){
        menuTitles.add(getResources().getString(R.string.ui_menu_buyticket));
        menuTitles.add(getResources().getString(R.string.ui_menu_mytickets));
        menuTitles.add(getResources().getString(R.string.ui_menu_customerprofile));
    }

    void showSignOutDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(getActivity().getResources().getString(R.string.prompt_signout));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ui_text_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((CustomerActivity)getActivity()).signOut();
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
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
