package com.spectratech.sp530demo;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.spectratech.lib.CustomBaseActivity;
import com.spectratech.lib.LanguageHelper;
import com.spectratech.lib.ResourcesHelper;

import java.util.Locale;

import static com.spectratech.sp530demo.SP530DemoBaseApplication.KEY_SP_LANGUAGEPREFERENCE;

/**
 * SP530DemoBaseActivity used by other activities to inherit from.
 */
public class SP530DemoBaseActivity extends CustomBaseActivity {

    private static final String m_className="SP530DemoBaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set language
        LanguageHelper.setFromPreference(this, KEY_SP_LANGUAGEPREFERENCE, true);

        super.onCreate(savedInstanceState);

        m_activitycommmon=new SP530DemoBaseActivityCommonClass(m_context);
        m_activitycommmon.onCreate(savedInstanceState, this);

        if (BuildConfig.DEBUG) {
            ActionBar ab = getActionBar();
            if (ab!=null) {
                String strTitle = ResourcesHelper.getBaseContextString(m_context, R.string.app_name);
                String strDebug = ResourcesHelper.getBaseContextString(m_context, R.string.debug);
                ab.setTitle(strDebug+" - " + strTitle);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Locale locale=LanguageHelper.getLocaleFromPreference(this, KEY_SP_LANGUAGEPREFERENCE);
        if (locale!=null) {
            newConfig.locale = locale;
        }
        // set language
        LanguageHelper.setFromPreference(this, KEY_SP_LANGUAGEPREFERENCE, true);
    }

    public void setActionBarTitle(String title) {
        ActionBar ab = getActionBar();
        if (ab!=null) {
            ab.setTitle(title);
        }
    }

    // onclick functions

    public void onClick_noeffect(View v) {

    }

    public void menuOnClick_setting(MenuItem item) {
        ((SP530DemoBaseActivityCommonClass)m_activitycommmon).menuOnClick_setting(item);
    }

}
