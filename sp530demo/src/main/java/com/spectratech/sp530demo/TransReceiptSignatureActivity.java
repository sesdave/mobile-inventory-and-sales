package com.spectratech.sp530demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.StringHelper;
import com.spectratech.lib.data.Data_drawable_lineset;
import com.spectratech.lib.view.DrawableLinearLayout;
import com.spectratech.sp530demo.conf.PathClass;

/**
 * TransReceiptSignatureActivity - transaction receipt activity for SP530
 */
public class TransReceiptSignatureActivity extends SP530DemoBaseActivity {
    private static final String m_className="TransReceiptSignatureActivity";

    public static final String FULLPATH_SAVE_TRANSRECEIPTSIGNATURE="FULLPATH_SAVE_TRANSRECEIPTSIGNATURE";

    private LinearLayout m_mainll;

    private Button m_btnCancel;

    private LinearLayout m_drawable_signature_container;
    private DrawableLinearLayout m_drawable_signature;

    private TextView m_transaction_signame_caption;

    private LinearLayout m_transaction_amount_value_containerll;
    private TextView m_transaction_amount_valuetv;

    private float m_factor=1.0f;

    private boolean m_bDemoTransReceipt;
    private boolean m_bCancelable;

    private String m_fullSavePath;
    private String m_totalAmountString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        m_context=this;

        final String strDemoCap=ResourcesHelper.getBaseContextString(m_context, R.string.demo_capital);

        m_mainll=(LinearLayout)LinearLayout.inflate(m_context, R.layout.activity_transreceiptsignature, null);
        setContentView(m_mainll);

        m_drawable_signature_container=(LinearLayout)m_mainll.findViewById(R.id.transaction_signature_container);
        m_drawable_signature=(DrawableLinearLayout)m_drawable_signature_container.findViewById(R.id.transaction_signature_drawable_signature);
        LinearLayout.LayoutParams lp= (LinearLayout.LayoutParams)m_drawable_signature.getLayoutParams();
        DisplayHelper instDisplayHelper=DisplayHelper.getInstance();
        m_factor=instDisplayHelper.getFitScreenScaleFactor(m_context, lp.width, lp.height);
        Logger.i(m_className, "m_factor: "+String.format("%0,2f", m_factor));
        m_drawable_signature.setScalingFactor(m_factor);
        setSignatureFitScreen();

        m_transaction_signame_caption=(TextView)m_mainll.findViewById(R.id.text_signname_caption);

        m_transaction_amount_value_containerll=(LinearLayout)m_mainll.findViewById(R.id.transaction_amount_value_container);
        m_transaction_amount_valuetv=(TextView)m_mainll.findViewById(R.id.transaction_amount_value);

        m_btnCancel=(Button)m_mainll.findViewById(R.id.btn_cancel);

        m_fullSavePath="";
        getIntentParams();

        if (m_bDemoTransReceipt) {
            m_transaction_signame_caption.setText(strDemoCap);
        }
        StringHelper instStringHelper=StringHelper.getInstance();
        String strAmount=m_totalAmountString;
        strAmount=instStringHelper.parseAmountWithSeparator(strAmount);
        m_transaction_amount_valuetv.setText(strAmount);
        m_transaction_amount_value_containerll.setVisibility(View.VISIBLE);
    }

    public static final String getFullSavePathForDemo(Context context) {
        return PathClass.getSignatureDir(context)+"/"+"demo_signature.bin";
    }

    private void getIntentParams() {
        String tmp="";
        Intent intent = getIntent();

        String key="demo";
        tmp=intent.getStringExtra(key);
        try {
            m_bDemoTransReceipt = Boolean.parseBoolean(tmp);
        }
        catch (Exception ex) {
            m_bDemoTransReceipt=false;
        }

        if (m_bDemoTransReceipt) {
            m_fullSavePath=TransReceiptSignatureActivity.getFullSavePathForDemo(m_context);

            m_totalAmountString="1888.88";
        }
        else {
            key=FULLPATH_SAVE_TRANSRECEIPTSIGNATURE;
            tmp=intent.getStringExtra(key);
            m_fullSavePath=tmp;

            key="totalAmountString";
            m_totalAmountString=intent.getStringExtra(key);

            m_btnCancel.setVisibility(View.GONE);
        }

        if (m_totalAmountString==null) {
            m_totalAmountString="";
        }
    }

    private void setSignatureFitScreen() {
        int wActual=m_drawable_signature.getActualWidth();
        int hActual=m_drawable_signature.getActualHeight();
        RelativeLayout.LayoutParams tmpLp=new RelativeLayout.LayoutParams(wActual, hActual);
        tmpLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        m_drawable_signature_container.setLayoutParams(tmpLp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // onclick functions

    public void onProcess(View v) {
        Logger.i(m_className, "onProcess called");

        int size_lineset=m_drawable_signature.getLineSetSize();
        if (size_lineset<1) {
            String strTmp=ResourcesHelper.getBaseContextString(m_context, R.string.transaction_receipt_text_signname);
            Toast.makeText(m_context, strTmp, Toast.LENGTH_SHORT).show();
            return;
        }

        Data_drawable_lineset dataDrawableLineset=m_drawable_signature.exportLineset2DrawableLineset();
        String drawableLinesetPath= m_fullSavePath;
        dataDrawableLineset.write2file(drawableLinesetPath);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(FULLPATH_SAVE_TRANSRECEIPTSIGNATURE, m_fullSavePath);
        setResult(RESULT_OK, returnIntent);
        endActivity();
    }

    public void onCancel(View v) {
        if (!isOnPause()) {
            Logger.i(m_className, "onCancel called");
            finish();
        }
    }

    public void onClear(View v) {
        Logger.i(m_className, "onClear called");
        m_drawable_signature.clear();
    }

    private void endActivity() {
        this.finish();
        //overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (m_bDemoTransReceipt) {
            super.onBackPressed();
        }
        else {
            final String strYouNeedSignName=ResourcesHelper.getBaseContextString(m_context, R.string.you_need_sign_name);

            Toast.makeText(m_context, strYouNeedSignName, Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
