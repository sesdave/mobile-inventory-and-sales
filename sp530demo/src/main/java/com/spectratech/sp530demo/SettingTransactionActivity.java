package com.spectratech.sp530demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.data.Data_setting_s3trans;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * SettingTransactionActivity - settting transaction activity
 */
public class SettingTransactionActivity extends SP530DemoBaseActivity {

    private static final String m_className="SettingTransactionActivity";

    private LinearLayout m_mainll;

    private SeekBar m_max_iter_seekbar;
    private TextView m_max_iter_tv;
    private SeekBar.OnSeekBarChangeListener m_listenMaxIterSeek;

    private SeekBar m_comto_seekbar;
    private TextView m_comto_tv;
    private SeekBar.OnSeekBarChangeListener m_listenComToSeek;

    private TextView m_tvTransSimulateTCPHost;

    private EditText m_etTransExtraTlv;

    private Data_setting_s3trans m_dataS3transConfig;

    private int m_uiIdx;
    private int m_uiValMaxIter;
    private int m_uiValComTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_settingtransaction, null);
        setContentView(m_mainll);

        initListeners();
        m_max_iter_seekbar=(SeekBar) m_mainll.findViewById(R.id.conf_trans_max_iteration);
        m_max_iter_seekbar.setOnSeekBarChangeListener(m_listenMaxIterSeek);
        m_max_iter_tv=(TextView)m_mainll.findViewById(R.id.conf_trans_max_iteration_tv);
        m_comto_seekbar=(SeekBar) m_mainll.findViewById(R.id.conf_trans_com_to);
        m_comto_seekbar.setOnSeekBarChangeListener(m_listenComToSeek);
        m_comto_tv=(TextView)m_mainll.findViewById(R.id.conf_trans_com_to_tv);

        m_dataS3transConfig=new Data_setting_s3trans(m_context);
        m_uiIdx=m_dataS3transConfig.m_idxTransResult;
        m_uiValMaxIter= map2seekbar_max_iter_value(m_dataS3transConfig.m_maxBatch);
        setMaxIterSeekBar(m_uiValMaxIter);
        m_uiValComTO=map2seekbar_comto_value(m_dataS3transConfig.m_waitTimeNormalInS);
        setComTOSeekBar(m_uiValComTO);

        initDropDownUIs();

        m_etTransExtraTlv=(EditText)m_mainll.findViewById(R.id.settings_transaction_extratlv);
        m_etTransExtraTlv.setText(m_dataS3transConfig.m_strTransactionExtraTlv);

        m_tvTransSimulateTCPHost=(TextView)m_mainll.findViewById(R.id.settings_transaction_simulatetcphost);
        if (m_tvTransSimulateTCPHost!=null) {
            boolean bTransactionUIShowMore=m_dataS3transConfig.m_bTransactionSimulateTCPHost;
            int checkbox = bTransactionUIShowMore ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvTransSimulateTCPHost.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bTransactionUIShowMore) {
                m_tvTransSimulateTCPHost.setTag(true);
            }
            else {
                m_tvTransSimulateTCPHost.setTag(false);
            }
        }

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }

        // focus me please
        m_mainll.setFocusable(true);
        m_mainll.setFocusableInTouchMode(true);
        m_mainll.requestFocus();
    }

    private void setMaxIterSeekBar(int val) {
        m_max_iter_seekbar.setProgress(val);
        int valReal=map2real_max_iter_value(val);
        m_max_iter_tv.setText(""+valReal);
    }
    private void setComTOSeekBar(int val) {
        m_comto_seekbar.setProgress(val);
        int valReal = map2real_comto_value(val);
        m_comto_tv.setText(""+valReal);
    }

    private int map2seekbar_max_iter_value(int val) {
        int result= val-Data_setting_s3trans.DEFAULT_MAX_BATCH;
        return result;
    }
    private int map2real_max_iter_value(int val) {
        int result= val+Data_setting_s3trans.DEFAULT_MAX_BATCH;
        return result;
    }

    private int map2seekbar_comto_value(int val) {
        int result= val-Data_setting_s3trans.DEFAULT_WAITTIMENORMALINS;
        return result;
    }
    private int map2real_comto_value(int val) {
        int result= val+Data_setting_s3trans.DEFAULT_WAITTIMENORMALINS;
        return result;
    }


    private void initDropDownUIs() {
        final String strTransResult= ResourcesHelper.getBaseContextString(m_context, R.string.transaction_result);
        TextView tv = (TextView) m_mainll.findViewById(R.id.conf_trans_result_tv);
        {
            final TextView ftv=tv;
            final String title=strTransResult;
            ArrayList listTitleArray=new ArrayList<String>(
                    Arrays.asList("Accept", "Decline", "Toggle", "Random")
            );
            final ArrayAdapter<String> adapter=new ArrayAdapter<String>(m_context, android.R.layout.simple_spinner_dropdown_item, listTitleArray);
            if ( (tv!=null)&&(adapter!=null) ) {
                setDropDownTextView(tv, adapter, m_uiIdx);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialog=new AlertDialog.Builder(m_context);
                        dialog.setTitle(title);
                        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setDropDownTextView(ftv, adapter, which);
                                m_uiIdx=which;
                                dialog.dismiss();
                            }
                        };

                        if (m_uiIdx<0) {
                            dialog.setAdapter(adapter, listener).show();
                        }
                        else {
                            dialog.setSingleChoiceItems(adapter, m_uiIdx, listener).show();
                        }
                    }
                });
            }
        }
    }

    private void setDropDownTextView(TextView tv, ArrayAdapter<String> adapter, int which) {
        if (which>-1) {
            String val = adapter.getItem(which);
            tv.setText(val);
            int idx = which;
            tv.setTag("" + idx);
        }
    }

    private void initListeners() {
        m_listenMaxIterSeek=new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_uiValMaxIter=progress;
                int valReal=map2real_max_iter_value(m_uiValMaxIter);
                m_max_iter_tv.setText(""+valReal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        };
        m_listenComToSeek=new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_uiValComTO=progress;
                int valReal=map2real_comto_value(progress);
                m_comto_tv.setText(""+valReal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }

    public void onProcess(View v) {
        final String strParamSaved=ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);

        m_dataS3transConfig.setIdxTransResult(m_uiIdx);
        int val=map2real_max_iter_value(m_uiValMaxIter);
        m_dataS3transConfig.setMaxBatch(val);
        val=map2real_comto_value(m_uiValComTO);
        m_dataS3transConfig.setWaitTimeNormalInS(val);

        String strExtraTlv=m_etTransExtraTlv.getText().toString();
        m_dataS3transConfig.setTransactionExtraTlv(strExtraTlv);

        boolean flag=(boolean)m_tvTransSimulateTCPHost.getTag();
        m_dataS3transConfig.setTransactionSimulateTCPHost(flag);

        Application.FLAG_UPDATE_DATA_SETTING_TRANSACTION_PARAMS=true;

        Toast.makeText(m_context, strParamSaved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        Toast.makeText(m_context, strCancel, Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    public void onTransactionSimulateTCPHost(View v) {
        if (m_tvTransSimulateTCPHost!=null) {
            boolean flag=(boolean)m_tvTransSimulateTCPHost.getTag();
            flag=!flag;
            m_tvTransSimulateTCPHost.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvTransSimulateTCPHost.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
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
