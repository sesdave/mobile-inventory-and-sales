package com.spectratech.sp530demo.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.SP530DemoBaseActivity;

/**
 * Test read QR code activity
 */
public class TestQRReadActivity extends SP530DemoBaseActivity {

    private static final int QR_CODE_REQUEST = 0x8002;

    private LinearLayout m_mainll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_mainll = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_testqrread, null);
        setContentView(m_mainll);
    }


    public void onClick_qrScan(View v) {
//        final Intent intent = new Intent(this, com.google.zxing.client.android.CaptureActivity.class);
//        intent.setAction(com.google.zxing.client.android.Intents.Scan.ACTION);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        startActivityForResult(intent, QR_CODE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case QR_CODE_REQUEST: {
                if (resultCode == RESULT_OK) {
//                    String barcode = data.getStringExtra(com.google.zxing.client.android.Intents.Scan.RESULT);
//                    Logger.i("QRCode", "got barcode: " + barcode);
                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
