package com.bridgecrm.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.bridgecrm.App;
import com.bridgecrm.R;
import com.bridgecrm.manager.SessionManager;
import com.bridgecrm.ui.ActivityMediator;
import com.bridgecrm.ui.fragment.SignUpFragment;
import com.bridgecrm.ui.fragment.SignInFragment;
import com.bridgecrm.ui.fragment.TourFragmentBuilder;
import com.viewpagerindicator.CirclePageIndicator;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AuthActivity extends BaseActivity {

    @Inject
    SessionManager sessionManager;
    @Inject
    ActivityMediator activityMediator;

    @InjectView(R.id.auth_tour)
    ViewPager tourPager;
    @InjectView(R.id.auth_tour_titles)
    CirclePageIndicator tourTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).component().inject(this);
        setContentView(R.layout.activity_auth);
        ButterKnife.inject(this);

        tourPager.setAdapter(new TourPager(getSupportFragmentManager()));
        tourTitles.setViewPager(tourPager);
    }


    @OnClick(R.id.auth_sign_in)
    public void onSignIn() {
        new SignInFragment().show(getSupportFragmentManager(), SignInFragment.class.getName());
    }

    @OnClick(R.id.auth_sign_up)
    public void onSignUp() {
        new SignUpFragment().show(getSupportFragmentManager(), SignUpFragment.class.getName());
    }

    static class TourPager extends FragmentPagerAdapter {

        public TourPager(FragmentManager fm) { super(fm); }

        int[] res = new int[] {
            R.drawable.im_auth_tour_1,
            R.drawable.im_auth_tour_2
        };

        @Override
        public Fragment getItem(int position) {
            return TourFragmentBuilder.newTourFragment(res[position]);
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

}



