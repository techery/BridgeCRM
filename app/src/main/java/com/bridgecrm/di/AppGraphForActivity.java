package com.bridgecrm.di;

import com.bridgecrm.ui.activity.AuthActivity;
import com.bridgecrm.ui.activity.BaseActivity;
import com.bridgecrm.ui.activity.BaseSessionActivity;
import com.bridgecrm.ui.activity.LaunchActivity;
import com.bridgecrm.ui.activity.MainActivity;

public interface AppGraphForActivity {
    void inject(BaseActivity activity);
    void inject(BaseSessionActivity activity);
    void inject(LaunchActivity launchActivity);
    void inject(AuthActivity authActivity);
    void inject(MainActivity mainActivity);
}
