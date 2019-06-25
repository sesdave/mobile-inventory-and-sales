package com.spectratech.sp530demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.StringHelper;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.data.Data_logdatapacket;

/**
 * ViewLogListView - list view for view log
 */
public class ViewLogListView extends ListView implements AbsListView.OnScrollListener {

    private static final String m_className="ViewLogListView";

    private OnScrollListener m_listener = null;

    private Context m_context;

    private ViewLogListViewAdapter m_viewLoglv_adapter;

    public ViewLogListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ViewLogListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        m_context=context;
        m_viewLoglv_adapter=null;
        super.setOnScrollListener(this);

        initListViewAdapter();
    }

    private void initListViewAdapter() {
        m_viewLoglv_adapter = new ViewLogListViewAdapter(m_context, -1);
        setAdapter(m_viewLoglv_adapter);

        if (Application.logPacketList.size()>50) {
            setFastScrollAlwaysVisible(true);
        }
        else {
            setFastScrollAlwaysVisible(false);
        }
    }

    public void notifyDataSetChanged() {
        if (m_viewLoglv_adapter!=null) {
            m_viewLoglv_adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (this.m_listener != null) {
			this.m_listener.onScrollStateChanged(view, scrollState);
		}
    }

    public class ViewLogListViewAdapter extends ArrayAdapter<String> {
        public ViewLogListViewAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return Application.logPacketList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = null;

            Data_logdatapacket data=Application.logPacketList.get(position);
            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
            String strMsg = "";
            if ( (convertView!=null)&&(convertView.getId()==R.id.viewlog_cell) ) {
                v=convertView;
            }
            else {
                v=LinearLayout.inflate(m_context, R.layout.view_viewlog_cell, null);
            }

            TextView tv_time=(TextView)v.findViewById(R.id.tv_time);
            long tInMs=data.m_logDatetime;
            strMsg=TransSummaryListView.getFullDateStringWithMicroSecond(tInMs);
            tv_time.setText(strMsg);

            TextView tv_large=(TextView)v.findViewById(R.id.tv_large);
            if (data.m_bFromSend) {
                strMsg="SEND";
            }
            else {
                strMsg="RECEIVE";
            }
            tv_large.setText(strMsg);

            TextView tv_general=(TextView)v.findViewById(R.id.tv_general);
            strMsg=instByteHexHelper.bytesArrayToHexString(data.m_dataBuf);
            StringHelper instStringHelper=StringHelper.getInstance();
            strMsg=instStringHelper.addPaddingStartingFromHead(" ", strMsg, 4);
            tv_general.setText(strMsg);

            return v;
        }
    }
}
