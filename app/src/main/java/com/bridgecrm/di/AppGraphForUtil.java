package com.bridgecrm.di;

import com.bridgecrm.helper.InstallReceiver;
import com.bridgecrm.ui.ActivityMediator;

public interface AppGraphForUtil {
    void inject(InstallReceiver installReceiver);
    void inject(ActivityMediator activityMediator);
}
