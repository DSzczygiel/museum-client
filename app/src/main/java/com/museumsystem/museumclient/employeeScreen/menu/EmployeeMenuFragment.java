package com.museumsystem.museumclient.employeeScreen.menu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.employeeScreen.EmployeeActivity;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMenuFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView roleTextView;
    TextView usernameTextView;
    RecyclerView recyclerView;
    List<String> menuTitles;
    EmployeeMenuItemAdapter employeeMenuItemAdapter;
    TextView signOutTextView;

    private OnFragmentInteractionListener mListener;

    public EmployeeMenuFragment() {
        // Required empty public constructor
    }

    public static EmployeeMenuFragment newInstance(String param1, String param2) {
        EmployeeMenuFragment fragment = new EmployeeMenuFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_employee_menu, container, false);
        Intent intent = getActivity().getIntent();
        roleTextView = (TextView) rootView.findViewById(R.id.role_textview);
        usernameTextView = (TextView) rootView.findViewById(R.id.username_textview);
        roleTextView.setText(getResources().getString(R.string.ui_role_employee));
        usernameTextView.setText(intent.getStringExtra("username"));
        signOutTextView = (TextView) rootView.findViewById(R.id.sign_out_textview);
        signOutTextView.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
            }
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.employee_menu_recyclerview);
        menuTitles = new ArrayList<>();
        employeeMenuItemAdapter = new EmployeeMenuItemAdapter(getActivity(), menuTitles);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        // recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        // recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(employeeMenuItemAdapter);

        prepareMenu();

        return rootView;
    }

    private void prepareMenu(){
        menuTitles.add(getResources().getString(R.string.emp_menu_scanner));
        menuTitles.add(getResources().getString(R.string.emp_menu_manage_artists));
        menuTitles.add(getResources().getString(R.string.emp_menu_manage_artworks));
        menuTitles.add(getResources().getString(R.string.emp_menu_maintenances));
        menuTitles.add(getResources().getString(R.string.ui_news));
    }

    void showSignOutDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(getActivity().getResources().getString(R.string.prompt_signout));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ui_text_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((EmployeeActivity)getActivity()).signOut();
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
