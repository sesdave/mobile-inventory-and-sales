package com.spectratech.sp530demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * CustomLinearLayoutWithMeasureWH - custom linear layout with measure width and height on layout called
 */
public class CustomLinearLayoutWithMeasureWH extends LinearLayout {
	private static final String m_className="CustomLinearLayoutWithMeasureWH";
	
	private Context m_context;
	
	private int m_width;
	private int m_height;
	
	private boolean m_bSetLayoutParameter;
	
	public CustomLinearLayoutWithMeasureWH(Context context) {
		super(context);
		init(context);
	}
	
	public CustomLinearLayoutWithMeasureWH(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public CustomLinearLayoutWithMeasureWH(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		m_context=context;
		
		m_width=-1;
		m_height=-1;
		m_bSetLayoutParameter=false;
	}
	
	public boolean isSetLayout() {
		return m_bSetLayoutParameter;
	}
	
	public int getCustomMeasuredWidth() {
		return m_width;
	}
	
	public int getCustomMeasuredHeight() {
		return m_height;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		super.onLayout(changed, l, t, r, b);
		if (!m_bSetLayoutParameter) {
			m_width = r - l;
			m_height = b - t;
			if ( (m_width>-1)&&(m_height>-1) ) {
				LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(m_width, m_height);
				//RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(m_width, m_height);
				this.setLayoutParams(lp);
				m_bSetLayoutParameter=true;
			}
		}
	}
}
