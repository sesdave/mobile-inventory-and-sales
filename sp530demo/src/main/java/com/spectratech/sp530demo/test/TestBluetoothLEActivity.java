package com.spectratech.sp530demo.test;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import com.spectratech.lib.Logger;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.SP530DemoBaseActivity;

/**
 * TestLoopbackActivity - test Bluetooth Low Energy activity
 */
public class TestBluetoothLEActivity extends SP530DemoBaseActivity {
    private static final String m_className="TestBluetoothLEActivity";

    private LinearLayout m_mainll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_mainll = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_testbluetoothle, null);
        setContentView(m_mainll);
    }

    public void onClick_connectBLE(View v) {
        Logger.i(m_className, "onClick_connectBLE");
    }
}
