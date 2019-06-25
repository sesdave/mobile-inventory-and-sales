package com.spectratech.sp530demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.spectratech.sp530demo.view.GeneralWebView;

/**
 * WebViewActivity - web view activity
 */
public class WebViewActivity extends SP530DemoBaseActivity {
	private static final String m_className="WebViewActivity";
	
	private LinearLayout m_mainll;
	private GeneralWebView m_mainwv;
	
	private String m_url;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		m_url="";
		Intent intent=getIntent();
		if (intent!=null) {
			m_url=intent.getStringExtra("url");
		}
		
		m_mainll=(LinearLayout) LinearLayout.inflate(m_context, R.layout.activity_generalwebview, null);

		m_mainwv=(GeneralWebView)m_mainll.findViewById(R.id.generalwebview);
		m_mainwv.enableShowDialog(true);

		if ( (m_url!=null)&&(!m_url.equals("")) ) {
			m_mainwv.loadUrl(m_url);
		}
		
		setContentView(m_mainll);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    @Override
    protected void onResume() {
        super.onResume();
    }
}
