package com.spectratech.sp530demo;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.spectratech.lib.LocalPersistentStore;
import com.spectratech.lib.MediaHelper;
import com.spectratech.sp530demo.conf.PathClass;
import com.spectratech.sp530demo.controller.UrlSchemeHelper;

/**
 * OpeningActivity - opening activity for SP530 demo
 */
public class OpeningActivity extends SP530DemoBaseActivity {

    private final String m_className="OpeningActivity";

    private final int WAIT_TIME_INMS=1000;

    private Handler m_handler;

    private final int MSG_HANDLER_ENDACTIVITY=2000;

    private RelativeLayout m_mainrl;
    private ImageView m_iv;

    private Intent m_intentActionView;
    private String m_intentActionViewURLString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_context=this;

        m_mainrl=(RelativeLayout)RelativeLayout.inflate(m_context, R.layout.activity_opening, null);
        setContentView(m_mainrl);

        ActionBar actionBar = getActionBar();
        if (actionBar!=null) {
            actionBar.hide();
        }

        createRootFolder(this);

        m_iv=(ImageView)m_mainrl.findViewById(R.id.iv);

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }

        initHandler();

        Application.hashUrlSchemeForDemoMain=null;
        m_intentActionView=null;
        m_intentActionViewURLString="";
        fetchIntentActionView();

        m_handler.sendEmptyMessageDelayed(MSG_HANDLER_ENDACTIVITY, WAIT_TIME_INMS);
    }

    private void fetchIntentActionView() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            m_intentActionView=intent;
            Uri uri = m_intentActionView.getData();
            m_intentActionViewURLString=uri.toString();
        }
    }

    /**
     * Create folders in external storage for SP530 demo
     * @param context context of application
     */
    public static void createRootFolder(Context context) {
        LocalPersistentStore.makeDirectory(PathClass.ROOTDIR);
        MediaHelper.createRootPathNoMediaFile(PathClass.EXTERNAL_ROOTPATH);
        LocalPersistentStore.makeDirectory(PathClass.ROOTDIR + "/" + PathClass.IMGDIR);
        LocalPersistentStore.makeDirectory(PathClass.ROOTDIR + "/" + PathClass.ICONIMGDIR);
        LocalPersistentStore.makeDirectory(PathClass.ROOTDIR + "/" + PathClass.IMGCAPTUREDIR);

        LocalPersistentStore.makeDirectoryInsideCacheDir(context, PathClass.SIGNATUREDIR);
    }

    @Override
    protected void onDestroy() {
        m_handler.removeMessages(MSG_HANDLER_ENDACTIVITY);
        super.onDestroy();
    }

    private void initHandler() {
        m_handler=new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_HANDLER_ENDACTIVITY: {

                        if (m_intentActionView != null) {
                            UrlSchemeHelper instUrlSchemeHelper = UrlSchemeHelper.getInstance();
                            instUrlSchemeHelper.process(m_context, m_intentActionViewURLString);
                            finish();
                        } else {
                            endActivity();
                        }

                    }
                    break;
                }
            }
        };
    }

    /**
     * End activity function
     */
    public void endActivity() {
        finish();

        Application.startActivity(m_context, Application.ACTIVITY_DEMO_MAIN);
        overridePendingTransition(0, 0);
    }

    private void onOrientationPortrait() {
        // not full screen mode
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void onOrientationLandscape() {
        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
