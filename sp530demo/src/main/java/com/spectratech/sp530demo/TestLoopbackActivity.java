package com.spectratech.sp530demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Test loop back activity
 */
public class TestLoopbackActivity extends SP530DemoBaseActivity  {
    private static final String m_className="TestLoopbackActivity";

    private LinearLayout m_mainll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_mainll = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_testloopback, null);
        setContentView(m_mainll);
    }

    public void onClick_server(View v) {
        Intent intent = new Intent(m_context, TestLoopbackServerActivity.class);
        m_context.startActivity(intent);
    }

    public void onClick_client(View v) {
        Intent intent = new Intent(m_context, TestLoopbackClientActivity.class);
        m_context.startActivity(intent);
    }
}
