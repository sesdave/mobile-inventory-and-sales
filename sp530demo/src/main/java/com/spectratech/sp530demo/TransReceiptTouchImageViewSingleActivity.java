package com.spectratech.sp530demo;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.spectratech.sp530demo.conf.PathClass;

/**
 * TransReceiptTouchImageViewSingleActivity - transaction receipt zoomable image view activity
 */
public class TransReceiptTouchImageViewSingleActivity extends SP530DemoBaseFragmentActivity {
    private static final String m_className="TransReceiptTouchImageViewSingleActivity";

    private LinearLayout m_mainll;

    private ImageView m_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)LinearLayout.inflate(m_context, R.layout.activity_transreceipttouchimagesingle, null);
        setContentView(m_mainll);

        m_iv=(ImageView)m_mainll.findViewById(R.id.transaction_receipt_touchiv);

        Bitmap bmp=getBitmap();
        if (bmp==null) {
            Toast.makeText(m_context, "View transaction receipt ERROR, please try again later", Toast.LENGTH_SHORT).show();
            return;
        }
        m_iv.setImageBitmap(bmp);

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    private Bitmap getBitmap(){
        Bitmap output=null;
        String fullPath= PathClass.CAPTURE_TRANSRECEIPT_FULLPATH;
        output = BitmapFactory.decodeFile(fullPath);
        return output;
    }

    private void onOrientationPortrait() {
        // not full screen mode
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void onOrientationLandscape() {
        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                onOrientationPortrait();
            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE: {
                onOrientationLandscape();
            }
            break;
        }
    }

}
