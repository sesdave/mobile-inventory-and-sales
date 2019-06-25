package com.spectratech.sp530demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import com.spectratech.lib.CustomBaseActivityCommonClass;
import com.spectratech.sp530demo.controller.ActivityHelper;

/**
 * SP530DemoBaseActivityCommonClass used by SP530DemoBaseActivity and SP530DemoBaseFragmentActivity
 * Common functions are implemented here.
 */
public class SP530DemoBaseActivityCommonClass extends CustomBaseActivityCommonClass {

    public SP530DemoBaseActivityCommonClass(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, Activity act) {
        super.onCreate(savedInstanceState, act);
        ActivityHelper instActivityHelper= ActivityHelper.getInstance();
        instActivityHelper.insertWithoutDemoMainActivity(act);
    }

    @Override
    public void onDestroy(Activity act) {
        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        instActivityHelper.remove(act);
        super.onDestroy(act);
    }

    public void menuOnClick_setting(MenuItem item) {
        if (!m_bOnPause) {
            int id=Application.ACTIVITY_SETTING;
            Application.startActivity(m_context, id);
            ((Activity) m_context).overridePendingTransition(0, 0);
        }
    }
}
