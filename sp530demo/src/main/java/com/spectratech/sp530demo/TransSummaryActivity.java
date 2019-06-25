package com.spectratech.sp530demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import com.spectratech.lib.Logger;
import com.spectratech.lib.bluetooth.BluetoothConnectActivity;
import com.spectratech.sp530demo.view.TransSummaryListView;

/**
 * TransSummaryActivity - transaction summary list activity
 */
public class TransSummaryActivity extends SP530DemoBaseActivity {
    private static final String m_className="TransSummaryActivity";

    private Handler m_handler;

    private LinearLayout m_mainll;

    private TransSummaryListView m_tsListView;

    private int m_n_tryHidenSoftKeyboardAtStartup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)LinearLayout.inflate(m_context, R.layout.activity_transsummary, null);
        setContentView(m_mainll);

        m_tsListView=(TransSummaryListView)m_mainll.findViewById(R.id.ts_lv);

        initHandler();

        m_n_tryHidenSoftKeyboardAtStartup=1;
        m_handler.sendEmptyMessage(BluetoothConnectActivity.MSG_HANDLER_HIDE_SOFTKEYBOARD);
    }

    @Override
    protected void onDestroy() {
        if (Application.dbTransSummaryDetailList!=null) {
            Application.dbTransSummaryDetailList.clear();
            Application.dbTransSummaryDetailList=null;
        }
        super.onDestroy();
    }

    private void initHandler() {
        m_handler=new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BluetoothConnectActivity.MSG_HANDLER_HIDE_SOFTKEYBOARD:
                        View v=getCurrentFocus();
                        if (v==null) {
                            if (m_n_tryHidenSoftKeyboardAtStartup>10) {
                                return;
                            }
                            m_n_tryHidenSoftKeyboardAtStartup++;
                            Logger.i(m_className, "handler, try to hide soft keyboard, try: "+m_n_tryHidenSoftKeyboardAtStartup);
                            m_handler.sendEmptyMessageDelayed(BluetoothConnectActivity.MSG_HANDLER_HIDE_SOFTKEYBOARD, 500);
                            return;
                        }
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm!=null) {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        break;
                }
            }
        };
    }
}
