package com.spectratech.spectratechlib_ftp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.CustomBaseActivity;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.spectratechlib_ftp.data.Data_ftpdlObject;

import java.util.ArrayList;


/**
 * Show file path string activity
 */
public class ShowFilePathStringListListViewActivity extends CustomBaseActivity {

    private static final String m_className = "ShowFilePathStringListListViewActivity";

    public static String KEY_INPUTSTRING_COMMON_PREFIX="KEY_INPUTSTRING_COMMON_PREFIX";

    public static String KEY_INPUTSTRINGLIST="KEY_INPUTSTRINGLIST";

    private LinearLayout m_mainll;
    private ListView m_lv;

    private String m_strPrefixCommon;

    private static ArrayList<Data_ftpdlObject> m_strDataArrayList=null;

    private FilePathStringListListArrayAdapter m_strListLVAdapter;

    private static Toast m_toast;

    public static void setList(ArrayList<Data_ftpdlObject> strDataArrayList) {
        m_strDataArrayList=strDataArrayList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String strNoContenCanBeShown=ResourcesHelper.getBaseContextString(m_context, R.string.no_content_can_be_shown);

        //Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_ftpshowstringlistlistview, null);
        setContentView(m_mainll);

        m_lv=(ListView)m_mainll.findViewById(R.id.lv);
        m_lv.setDividerHeight(0);
        m_lv.setFastScrollAlwaysVisible(true);

        fetchInputString();
        if ( (m_strDataArrayList==null)||(m_strDataArrayList.size()==0) ) {
            Logger.i(m_className, "String array is NULL or empty");
            ToastMessage(strNoContenCanBeShown);
            finish();
            return;
        }

        initListView();
    }

    @Override
    protected void onDestroy() {
        m_strDataArrayList=null;
        super.onDestroy();
    }

    private void ToastMessage(String msg) {
        if (m_toast!=null) {
            m_toast.cancel();
            m_toast=null;
        }
        m_toast= Toast.makeText(m_context, msg, Toast.LENGTH_SHORT);
        m_toast.show();
    }

    private void fetchInputString() {
        Intent intent=getIntent();
        Bundle b=intent.getExtras();

        m_strPrefixCommon=b.getString(KEY_INPUTSTRING_COMMON_PREFIX);
        if (m_strPrefixCommon==null) {
            m_strPrefixCommon="";
        }

        //m_strDataArrayList=(ArrayList<Data_ftpdlObject>)b.getSerializable(KEY_INPUTSTRINGLIST);
    }

    private void initListView() {
        m_strListLVAdapter = new FilePathStringListListArrayAdapter(m_context, -1);
        m_lv.setAdapter(m_strListLVAdapter);
    }

    @Override
    public void onBackPressed() {
        endActivityWithoutAnimation();
    }

    private void endActivityWithoutAnimation() {
        endActivity();
        ((Activity)m_context).overridePendingTransition(0, 0);
    }

    private void endActivity() {
        finish();
    }

    public void onClick_translucent(View v) {
        endActivityWithoutAnimation();
    }

    public void onClick_noeffect(View v) {
    }

    protected class FilePathStringListListArrayAdapter extends ArrayAdapter<String> {

        private static final String m_className="StringListListArrayAdapter";

        public FilePathStringListListArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            if (m_strDataArrayList==null) {
                return 0;
            }
            return m_strDataArrayList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v=convertView;
            int idView= R.id.showstringlistlistview_cell;
            if (v == null || v.getId() !=idView ) {
                v = (LinearLayout) LinearLayout.inflate(m_context, R.layout.view_ftpshowstringlistlistview_cell, null);
            }
            else {
                // reset view to default setting
            }

            TextView tv_id=(TextView)v.findViewById(R.id.tv_id);
            TextView tv_msg=(TextView)v.findViewById(R.id.tv_msg);

            String strId=""+(position+1);

            Data_ftpdlObject dataFtpdlObject=m_strDataArrayList.get(position);
            final String strMsg=m_strPrefixCommon+dataFtpdlObject.m_relative_pathlist;
            tv_id.setText(strId);
            tv_msg.setText(strMsg);

            View.OnClickListener listener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Data_uri_file dataUriFile=getData_uri_file(strMsg);
//                    if ( (dataUriFile==null)||(dataUriFile.m_uri_file==null) ) {
//                        return;
//                    }
//                    String inputString=dataUriFile.getDataString(m_context);
//                    Intent intent=new Intent(m_context, ShowStringActivity.class);
//                    intent.putExtra(ShowStringActivity.KEY_INPUTSTRING, inputString);
//                    m_context.startActivity(intent);
                }
            };
            v.setOnClickListener(listener);

            return v;
        }

    }

//    private Data_uri_file getData_uri_file(String path) {
//        Data_uri_file data=new Data_uri_file();
//        File f=new File(path);
//        data.m_uri_file=Uri.fromFile(f);
//        return data;
//    }

}
