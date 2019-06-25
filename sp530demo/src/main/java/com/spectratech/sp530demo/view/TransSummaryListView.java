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

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.data.Data_trans_summary;
import com.spectratech.lib.DisplayHelper;
import com.spectratech.sp530demo.Application;
import com.spectratech.lib.data.Data_dbTransSummaryDetail;
import com.spectratech.lib.sp530.db.DBTransSummaryDetail;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.SP530DemoBaseActivity;
import com.spectratech.sp530demo.SP530DemoBaseFragmentActivity;
import com.spectratech.sp530demo.adapters.TransSummaryDetailPagerAdapter;
import com.spectratech.sp530demo.data.Data_setting_general;

/**
 * TransSummaryListView - list view for transaction summary
 */
public class TransSummaryListView extends ListView implements AbsListView.OnScrollListener {

    private static final String m_className="TransSummaryListView";

    private OnScrollListener m_listener = null;

    private Context m_context;

    private TransSummaryListViewAdapter m_transsummarylv_adapter;

    public TransSummaryListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public TransSummaryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        m_context=context;
        m_transsummarylv_adapter=null;
        super.setOnScrollListener(this);

        initListViewAdapter();
    }

    private void initListViewAdapter() {
        Application.dbTransSummaryDetailList= DBTransSummaryDetail.getLatest(m_context);
        m_transsummarylv_adapter = new TransSummaryListViewAdapter(m_context, -1);
        setAdapter(m_transsummarylv_adapter);

        if (Application.dbTransSummaryDetailList.size()>50) {
            setFastScrollAlwaysVisible(true);
        }
        else {
            setFastScrollAlwaysVisible(false);
        }
    }

    public void notifyDataSetChanged() {
        if (m_transsummarylv_adapter!=null) {
            m_transsummarylv_adapter.notifyDataSetChanged();
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

    public class TransSummaryListViewAdapter extends ArrayAdapter<Data_trans_summary> {
        public TransSummaryListViewAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            if (Application.dbTransSummaryDetailList==null) {
                return 0;
            }
            return Application.dbTransSummaryDetailList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final String strRecord= ResourcesHelper.getBaseContextString(m_context, R.string.record);
            final String strCreatedAt=ResourcesHelper.getBaseContextString(m_context, R.string.created_at);

            View v = null;

            Data_dbTransSummaryDetail data = Application.dbTransSummaryDetailList.get(position);
            if ( (convertView!=null)&&(convertView.getId()==R.id.transsummary_cell) ) {
                v=convertView;
            }
            else {
                v=LinearLayout.inflate(m_context, R.layout.view_transummary_cell, null);
            }

            DisplayHelper instDisplayHelper= DisplayHelper.getInstance();
            {
                int height=100;
                height=instDisplayHelper.convertPixelsToDp(m_context, height);
                //LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
                AbsListView.LayoutParams lp=new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, height);
                v.setLayoutParams(lp);
            }

            TextView tv_large=(TextView)v.findViewById(R.id.tv_large);
            tv_large.setText(strRecord+" "+(position+1));

            long tInMs=data.m_importDate;

            TextView tv_general=(TextView)v.findViewById(R.id.tv_general);
            String strDateOnly=getDateOnlyString(tInMs);
            tv_general.setText(strCreatedAt+" "+strDateOnly);

            TextView tv_time=(TextView)v.findViewById(R.id.tv_time);
            String strTimeOnly=getTimeOnlyString(tInMs);
            tv_time.setText(strTimeOnly);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bDebug = Data_setting_general.isDebugMode(m_context);
                    boolean bPause = false;
                    if (bDebug) {
                        if (m_context instanceof SP530DemoBaseActivity) {
                            bPause = ((SP530DemoBaseActivity) m_context).isOnPause();
                        } else if (m_context instanceof SP530DemoBaseFragmentActivity) {
                            bPause = ((SP530DemoBaseFragmentActivity) m_context).isOnPause();
                        }
                        if (!bPause) {
                            HashMap<String, String> hash = new HashMap<String, String>();
                            hash.put("pos", "" + position);
                            int id = Application.ACTIVITY_TRANSSUMMARYDETAIL;
                            Application.startActivity(m_context, id, hash);
                        }
                    } else {
                        TransSummaryDetailPagerAdapter.startTransactionReceipt(m_context, position);
                    }
                }
            });

            return v;
        }
    }

    public static String getDateOnlyString(long milliSeconds) {
        String dateFormat = "yyyy-MM-dd";
        return getDateString(milliSeconds, dateFormat);
    }
    public static String getTimeOnlyString(long milliSeconds) {
        String dateFormat = "hh:mm:ssZ a";
        return getDateString(milliSeconds, dateFormat);
    }
    public static String getFullDateString(long milliSeconds) {
        String dateFormat = "yyyy-MM-dd hh:mm:ss";
        return getDateString(milliSeconds, dateFormat);
    }
    public static String getFullDateStringWithMicroSecond(long milliSeconds) {
        String dateFormat = "yyyy-MM-dd hh:mm:ss.SSSZ";
        return getDateString(milliSeconds, dateFormat);
    }
    public static String getDateString(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
