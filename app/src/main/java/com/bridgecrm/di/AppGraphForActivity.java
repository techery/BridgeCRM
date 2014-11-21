package com.bridgecrm.di;

import com.bridgecrm.ui.activity.BaseActivity;
import com.bridgecrm.ui.activity.MainActivity;

public interface AppGraphForActivity {
    void inject(BaseActivity activity);
    void inject(MainActivity mainActivity);
}
