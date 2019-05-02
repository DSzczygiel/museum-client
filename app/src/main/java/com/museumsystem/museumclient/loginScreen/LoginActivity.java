package com.museumsystem.museumclient.loginScreen;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.museumsystem.museumclient.R;
import com.museumsystem.museumclient.loginScreen.fragments.InfoFragment;
import com.museumsystem.museumclient.loginScreen.fragments.LoginFormFragment;
import com.museumsystem.museumclient.loginScreen.fragments.ScannerFragment;

public class LoginActivity extends AppCompatActivity implements LoginFormFragment.OnFragmentInteractionListener,
        ScannerFragment.OnFragmentInteractionListener,
        InfoFragment.OnFragmentInteractionListener {
    //TODO restore password

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(position == 0)
                return  LoginFormFragment.newInstance(" ", " ");
            else if(position == 1)
                return InfoFragment.newInstance(" ", " ");
            else
                return ScannerFragment.newInstance(" ", " ");
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
