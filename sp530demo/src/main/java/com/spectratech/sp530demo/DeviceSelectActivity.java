package com.spectratech.sp530demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.ResourcesHelper;

/**
 * DeviceSelectActivity - activity to select device for transaction
 * devices include sp530, t1000
 */
public class DeviceSelectActivity extends SP530DemoBaseActivity {

    private static final String m_className="DeviceSelectActivity";

    private static String PREFERENCE_DEVICESELECTED="DEVICESELECTED";
    private static final int DEFAULT_IDX_SELECT=0;

    private static final String KEY_DEVICE_SELECTED_INDEX="KEY_DEVICE_SELECTED_INDEX";

    public static final String[] LISTVIEW_VALUES=new String[] { "SP530", "T1000" };

    private LinearLayout m_mainll;
    private ListView m_lv;
    private Button m_btn_ok;
    private Button m_btn_cancel;

    private DeviceSelectionArrayAdapter m_deviceselectionAdapter;

    private int m_idx_select;

    /**
     * OnCreate function
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //initBTEventsForActivityInstance(this);
        super.onCreate(savedInstanceState);

        m_activitycommmon = new SP530DemoBaseActivityCommonClass(m_context);
        m_activitycommmon.onCreate(savedInstanceState, this);

        m_mainll = (LinearLayout) LinearLayout.inflate(m_context, R.layout.activity_deviceselect, null);
        setContentView(m_mainll);
        initUIs();

        m_deviceselectionAdapter=new DeviceSelectionArrayAdapter(m_context, -1);
        m_lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        m_lv.setAdapter(m_deviceselectionAdapter);
        m_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                m_idx_select = position;
            }
        });

        m_idx_select=getSelectedIndex(m_context);
        m_lv.setItemChecked(m_idx_select, true);
    }

    public static int getSelectedIndex(Context context) {
        SharedPreferences preference = context.getSharedPreferences(PREFERENCE_DEVICESELECTED, Context.MODE_PRIVATE);
        int idx = preference.getInt(KEY_DEVICE_SELECTED_INDEX, DEFAULT_IDX_SELECT);
        return idx;
    }

    public static void setSelectedIndex(Context context, int idx) {
        SharedPreferences preference=context.getSharedPreferences(PREFERENCE_DEVICESELECTED, Context.MODE_PRIVATE);
        preference.edit().putInt(KEY_DEVICE_SELECTED_INDEX, idx).commit();
    }

    private void initUIs() {
        final String strSave=ResourcesHelper.getBaseContextString(m_context, R.string.save);
        final String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        m_lv=(ListView)m_mainll.findViewById(R.id.device_lv);
        m_btn_ok=(Button)m_mainll.findViewById(R.id.btn_ok);
        m_btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedIndex(m_context, m_idx_select);
                endActivityWithResultOK();
                Toast.makeText(m_context, strSave, Toast.LENGTH_SHORT).show();
            }
        });
        m_btn_cancel=(Button)m_mainll.findViewById(R.id.btn_cancel);
        m_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endActivity();
                Toast.makeText(m_context, strCancel, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Function used for onclick of Translucent view
     * @param v Corresponding onclick view
     */
    public void onClick_translucent(View v) {
        endActivity();
    }

    private void ToastRunOnUiThread(final String msg) {
        ((Activity)m_context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(m_context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
        endActivity();
    }

    private void endActivityWithResultOK() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(KEY_DEVICE_SELECTED_INDEX, m_idx_select);
        setResult(RESULT_OK, returnIntent);
        endActivity();
    }
    private void endActivity() {
        this.finish();
        overridePendingTransition(0, 0);
    }

    private class DeviceSelectionArrayAdapter extends ArrayAdapter<String> {
        private static final String m_className = "DeviceSelectionArrayAdapter";

        public DeviceSelectionArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return LISTVIEW_VALUES.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if ((v == null) || (v.getId() != android.R.id.text1)) {
                v = (CheckedTextView) CheckedTextView.inflate(m_context, R.layout.view_simple_list_item_checked, null);
                int height=50;
                DisplayHelper instDisplayHelper=DisplayHelper.getInstance();
                height=instDisplayHelper.convertPixelsToDp(m_context, height);
                AbsListView.LayoutParams lp=new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, height);
                v.setLayoutParams(lp);
            }

            String strMsg = LISTVIEW_VALUES[position];
            ((CheckedTextView) v).setText(strMsg);

            return v;
        }

    }

}
