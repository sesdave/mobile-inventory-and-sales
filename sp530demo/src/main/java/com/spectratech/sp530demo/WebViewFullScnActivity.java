package com.spectratech.sp530demo;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * WebViewFullScnActivity - full screen web view activity
 */
public class WebViewFullScnActivity extends WebViewActivity {
	private static final String m_className="WebViewFullScnActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// full screen mode
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);
	}

    @Override
    protected void onResume() {
        super.onResume();
    }
}
