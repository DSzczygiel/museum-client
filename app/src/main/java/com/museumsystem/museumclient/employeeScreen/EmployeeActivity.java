package com.museumsystem.museumclient.employeeScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.employeeScreen.artists.ArtistDetailsFragment;
import com.museumsystem.museumclient.employeeScreen.artists.ArtistListFragment;
import com.museumsystem.museumclient.employeeScreen.artworks.ArtworkDetailsFragment;
import com.museumsystem.museumclient.employeeScreen.artworks.ArtworkListFragment;
import com.museumsystem.museumclient.employeeScreen.artworks.operations.ArtworkOperationFragment;
import com.museumsystem.museumclient.employeeScreen.artworks.operations.OperationsListFragment;
import com.museumsystem.museumclient.employeeScreen.maintenance.MaintenancesListFragment;
import com.museumsystem.museumclient.employeeScreen.menu.EmployeeMenuFragment;
import com.museumsystem.museumclient.employeeScreen.news.NewsDetailsFragment;
import com.museumsystem.museumclient.employeeScreen.news.NewsListFragment;
import com.museumsystem.museumclient.loginScreen.LoginActivity;

public class EmployeeActivity extends AppCompatActivity implements
        EmployeeMenuFragment.OnFragmentInteractionListener,
        ArtworkListFragment.OnFragmentInteractionListener,
        ArtworkDetailsFragment.OnFragmentInteractionListener,
        ArtistListFragment.OnFragmentInteractionListener,
        ArtistDetailsFragment.OnFragmentInteractionListener,
        ArtworkOperationFragment.OnFragmentInteractionListener,
        OperationsListFragment.OnFragmentInteractionListener,
        MaintenancesListFragment.OnFragmentInteractionListener,
        NewsListFragment.OnFragmentInteractionListener,
        NewsDetailsFragment.OnFragmentInteractionListener,
        TicketValidatorFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        Intent intent = getIntent();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.employee_framelayout, new EmployeeMenuFragment());
        transaction.commit();
    }

    void showExitDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(EmployeeActivity.this).create();
        alertDialog.setMessage(getApplication().getResources().getString(R.string.prompt_exit));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ui_text_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory( Intent.CATEGORY_HOME );
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
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

    public void signOut(){
        SharedPreferences sharedPreferences = getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        if(sharedPreferences.contains("refresh_token")){
            sharedPreferences.edit().remove("refresh_token").apply();
        }
        if(sharedPreferences.contains("access_token")){
            sharedPreferences.edit().remove("access_token").apply();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        for(int i=0; i<fragmentManager.getBackStackEntryCount(); i++)
            fragmentManager.popBackStack();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    @Override
    public void onBackPressed(){
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            showExitDialog();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
