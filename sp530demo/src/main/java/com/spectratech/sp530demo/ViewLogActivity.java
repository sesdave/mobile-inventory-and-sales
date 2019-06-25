package com.spectratech.sp530demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.spectratech.sp530demo.view.ViewLogListView;

/**
 * ViewLogActivity - view logging activity
 */
public class ViewLogActivity extends SP530DemoBaseActivity {
    private static final String m_className="ViewLogActivity";

    private LinearLayout m_mainll;

    private ViewLogListView m_vlListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)LinearLayout.inflate(m_context, R.layout.activity_viewlog, null);
        setContentView(m_mainll);

        m_vlListView=(ViewLogListView)m_mainll.findViewById(R.id.vl_lv);

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void onOrientationPortrait() {
        // not full screen mode
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void onOrientationLandscape() {
        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                onOrientationPortrait();
            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE: {
                onOrientationLandscape();
            }
            break;
        }
    }
}
