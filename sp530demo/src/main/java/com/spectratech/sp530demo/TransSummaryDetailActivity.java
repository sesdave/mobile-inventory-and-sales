package com.spectratech.sp530demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import com.spectratech.sp530demo.adapters.TransSummaryDetailPagerAdapter;

/**
 * TransSummaryDetailActivity - transaction summary detail activity
 */
public class TransSummaryDetailActivity extends SP530DemoBaseFragmentActivity {
    private static final String m_className="TransSummaryDetailActivity";

    private LinearLayout m_mainll;

    private ViewPager m_viewPager;

    private int m_posStart;

    private TransSummaryDetailPagerAdapter m_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)LinearLayout.inflate(m_context, R.layout.activity_transsummarydetail, null);
        setContentView(m_mainll);

        String tmp="";
        Intent intent = getIntent();
        // id type
        String key="pos";
        tmp=intent.getStringExtra(key);
        try {
            m_posStart = Integer.parseInt(tmp);
        }
        catch (Exception ex) {
            m_posStart=0;
        }

        m_viewPager = (ViewPager)m_mainll.findViewById(R.id.pager);
        m_adapter=new TransSummaryDetailPagerAdapter(m_context);
        m_viewPager.setAdapter(m_adapter);

        m_viewPager.setCurrentItem(m_posStart);
    }
}
