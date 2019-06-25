package com.spectratech.sp530demo;

import android.os.Bundle;
import com.spectratech.lib.FilesDownloadActivity;

/**
 * SP530 download files activity
 */
public class SP530DemoFilesDownloadActivity extends FilesDownloadActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_activitycommmon = new SP530DemoBaseActivityCommonClass(m_context);
        m_activitycommmon.onCreate(savedInstanceState, this);
    }
}
