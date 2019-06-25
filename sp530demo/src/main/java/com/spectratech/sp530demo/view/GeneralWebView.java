package com.spectratech.sp530demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

/**
 * GeneralWebView - General web view
 */
public class GeneralWebView extends WebView {
	private static final String m_className="GeneralWebView";
	
	private Context m_context;
	
	private WebSettings m_webSettings;
	private GeneralWebViewClient m_webViewClient;
	
	private String m_URL;
	
	private OnTouchListener m_onTouchListener;
	
	public GeneralWebView(Context context) {
		super(context);
		init(context);
	}
	
	public GeneralWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public GeneralWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		m_context=context;
		
		m_webSettings = getSettings();
		m_webSettings.setJavaScriptEnabled(true);
		
		m_webSettings.setBuiltInZoomControls(true);
		
		m_webSettings.setLoadWithOverviewMode(true);
		m_webSettings.setUseWideViewPort(true);

        setWebChromeClient(new WebChromeClient());

		setViewLayout();
		
		initOnTouchListener();
		this.setOnTouchListener(m_onTouchListener);
		
		m_webViewClient=new GeneralWebViewClient(m_context);
		setWebViewClient(m_webViewClient);
	}
	
	private void initOnTouchListener() {
		m_onTouchListener=new OnTouchListener()	{
			@Override
			public boolean onTouch(android.view.View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_UP:
						if (!v.hasFocus()) {
							v.requestFocus();
						}
						break;
				}
				return false;
			}
		};
	}
	
	public void setViewLayout() {
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(p);
	}
	
	public void enableShowDialog(boolean flag) {
		if (m_webViewClient!=null) {
			m_webViewClient.enableShowDialog(flag);
		}
	}
	
	@Override
	public void loadUrl(String url) {
		m_URL=url;
		super.loadUrl(m_URL);
	}
	
	public void reset() {
		this.loadUrl("");
	}
	
}
