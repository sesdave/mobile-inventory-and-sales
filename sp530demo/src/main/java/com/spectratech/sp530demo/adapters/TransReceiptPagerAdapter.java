package com.spectratech.sp530demo.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.view.ViewTransreceiptLinearLayout;

/**
 * TransReceiptPagerAdapter - transaction receipt pager adapter
 */
public class TransReceiptPagerAdapter extends PagerAdapter {

    private final String m_className="TransSummaryDetailPagerAdapter";

    /**
     * Prefix for fragment tag
     */
    public static final String TAG_PREFIX_VIEW="transreceiptpage";

    private Context m_context;

    private boolean m_bDemoPurpose;

    private int m_posPrint;

    /**
     * Constructor for TransReceiptPagerAdapter
     * @param context context of application
     * @param bDemoPurpose flag to enable/disable demo mode
     */
    public TransReceiptPagerAdapter(Context context, boolean bDemoPurpose) {
        m_context=context;
        init();
        m_bDemoPurpose=bDemoPurpose;
        m_posPrint=-1;
    }

    private void init() {
        m_bDemoPurpose=false;
    }

    /**
     * Set printing at a position
     * @param pos position for printing
     */
    public void setPrintReceiptAtPosition(int pos) {
        m_posPrint=pos;
    }

    /**
     * Get tag of fragment
     * @param position position of fragment
     * @return tag of fragment in string format
     */
    public String getTag_viewAt(int position) {
        return TAG_PREFIX_VIEW + position;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final ViewTransreceiptLinearLayout layout = (ViewTransreceiptLinearLayout) LinearLayout.inflate(m_context, R.layout.view_transreceipt, null);
        layout.setTag(TAG_PREFIX_VIEW + position);

        layout.initUIs(position, m_bDemoPurpose);

        container.addView(layout);

        // need to print?
        if (position==m_posPrint) {
            layout.printTransactionReceipt();
            m_posPrint=-1;
        }

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getCount() {
        if (m_bDemoPurpose) {
            return 1;
        }

        if (Application.dbTransSummaryDetailListForTransReceipt==null) {
            return 0;
        }
        return Application.dbTransSummaryDetailListForTransReceipt.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
