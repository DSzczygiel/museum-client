package com.museumsystem.museumclient.customerScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.ServerValues;
import com.museumsystem.museumclient.customerScreen.menu.CustomerMenuFragment;
import com.museumsystem.museumclient.customerScreen.ticketsList.MyTicketsFragment;
import com.museumsystem.museumclient.customerScreen.ticketsList.TicketDetailsFragment;
import com.museumsystem.museumclient.loginScreen.LoginActivity;
import com.museumsystem.museumclient.loginScreen.fragments.ScannerFragment;

public class CustomerActivity extends AppCompatActivity implements
        CustomerMenuFragment.OnFragmentInteractionListener,
        TicketPurchaseFragment.OnFragmentInteractionListener,
        PaymentOptionsFragment.OnFragmentInteractionListener,
        MyTicketsFragment.OnFragmentInteractionListener,
        TicketDetailsFragment.OnFragmentInteractionListener,
        CustomerProfileFragment.OnFragmentInteractionListener,
        ScannerFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        Intent intent = getIntent();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.customer_framelayout, new CustomerMenuFragment());
        transaction.commit();
    }


    void showExitDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(CustomerActivity.this).create();
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
