package com.spectratech.sp530demo.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.R;

public class GeneralWebViewClient extends WebViewClient {
	private static final String m_className="GeneralWebViewClient";
	private Context m_context;
	private ProgressDialog m_d=null;
	
	private boolean m_bShowDialog;
	
	public GeneralWebViewClient(Context context) {
		super();
		m_context=context;
		m_d=new ProgressDialog(m_context);
		m_bShowDialog=false;
	}
	
	public void enableShowDialog(boolean flag) {
		m_bShowDialog=flag;
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url){
		return super.shouldOverrideUrlLoading(view, url);
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		final String strPleaseWait= ResourcesHelper.getBaseContextString(m_context, R.string.prompt_please_wait_message);
        final String strLoading=ResourcesHelper.getBaseContextString(m_context, R.string.loading);
		if (m_bShowDialog) {
			if (m_d!=null) {
				m_d.setTitle(strPleaseWait);
				m_d.setMessage(strLoading);
				m_d.show();
			}
			m_bShowDialog=false;
		}
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		if (m_d!=null) {
			if (m_d.isShowing()) {
				try {
					m_d.dismiss();
				}
				catch (Exception ex) {
				}
			}
		}
	}
}
