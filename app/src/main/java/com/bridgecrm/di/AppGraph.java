package com.bridgecrm.di;

import com.bridgecrm.App;

/**
 * A common interface implemented by both the Release and Debug flavored components.
 */
public interface AppGraph extends AppGraphForActivity, AppGraphForFragment {
    void inject(App app);
}
