package com.bridgecrm.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;
import com.bridgecrm.R;

import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;
import static com.balysv.materialmenu.MaterialMenuDrawable.IconState;

public class BaseDrawerActivity extends BaseSessionActivity {

    protected Toolbar toolbar;
    private MaterialMenuIconToolbar menuIcon;

    private DrawerLayout drawerLayout;
    private boolean isDrawerOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_base);
        ButterKnife.inject(this);
        //
        initUi();
    }

    private void initUi() {
        setupToolbar();
        setupDrawerLayout();
    }

    private void setupToolbar() {
        toolbar = findById(this, R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
                if (isDrawerOpened) {
                    menuIcon.animatePressedState(IconState.BURGER);
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    menuIcon.animatePressedState(IconState.ARROW);
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        );
        menuIcon = new MaterialMenuIconToolbar(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN) {
            @Override
            public int getToolbarViewId() {
                return R.id.main_toolbar;
            }
        };
    }

    private void setupDrawerLayout() {// Now retrieve the DrawerLayout so that we can set the status bar color.
        drawerLayout = findById(this, R.id.drawer_layout);
        // This only takes effect on Lollipop, or when using translucentStatusBar on KitKat.
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.highlighted_text_material_light));
        //
        drawerLayout.setDrawerListener(
            new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    menuIcon.setTransformationOffset(
                        MaterialMenuDrawable.AnimationState.BURGER_ARROW,
                        isDrawerOpened ? 2 - slideOffset : slideOffset
                    );
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    isDrawerOpened = true;
                    // TODO add onDrawerOpened event?
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    isDrawerOpened = false;
                    // TODO add onDrawerClosed event?
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    if (newState == DrawerLayout.STATE_IDLE) {
                        if (isDrawerOpened) menuIcon.setState(IconState.ARROW);
                        else menuIcon.setState(IconState.BURGER);
                    }
                }
            }
        );
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        menuIcon.syncState(savedInstanceState);
        isDrawerOpened = drawerLayout.isDrawerOpen(Gravity.START); // or END, LEFT, RIGHT
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        menuIcon.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}
