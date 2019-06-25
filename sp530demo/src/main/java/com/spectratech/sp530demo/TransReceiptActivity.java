package com.spectratech.sp530demo;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
//import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.data.Data_dbTransSummaryDetail;
import com.spectratech.lib.data.Data_trans_summary;
import com.spectratech.lib.sp530.data.Data_AP_ONE;
import com.spectratech.lib.view.TransactionReceiptLinearLayout;
import com.spectratech.sp530demo.adapters.TransReceiptPagerAdapter;
import com.spectratech.sp530demo.constant.ActivityRequestCodeEnum;
import com.spectratech.sp530demo.domain.DateTimeStrategy;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;
import com.spectratech.sp530demo.view.ViewTransreceiptLinearLayout;

import java.util.HashMap;

/**
 * TransReceiptActivity - transaction receipt activity
 */
public class TransReceiptActivity extends SP530DemoBaseFragmentActivity {
    private static final String m_className="TransReceiptActivity";
    private Register regis;

    /**
     * Variable to store key of signature validation for intent
     */
    public static final String KEY_SIGNATURE_VALIDATION="signatureValidation";
    /**
     * Variable to store key of print receipt for intent
     */
    public static final String KEY_PRINTRECEIPT="printreceipt";

    private LinearLayout m_mainll;

    private ViewPager m_viewPager;

    private int m_posStart;

    private TransReceiptPagerAdapter m_adapter;

    private boolean m_bDemoTransReceipt;

    /**
     * Flag to indicate the need of signature validation
     */
    public boolean m_bSignatureValidation;
    /**
     * Flag to indicate the need of signature
     */
    private boolean m_bReceiptNeedSignature;

    /**
     * Flag to indicate the need of printing receipt
     */
    public boolean m_bPrintReceipt;
    private int m_countPrintReceipt;

    /**
     * Flag to indicate getting the printing receipt request from onActivityResult
     */
    public boolean m_bPrintCalledFromOnActivityResult;

    private Callback<Object> m_cb_success_request_btprinter_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            regis = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)LinearLayout.inflate(m_context, R.layout.activity_transreceipt, null);
        setContentView(m_mainll);

        m_bDemoTransReceipt=false;
        m_bSignatureValidation=false;
        m_bReceiptNeedSignature=true;

        m_bPrintReceipt=false;
        m_countPrintReceipt=0;
        m_bPrintCalledFromOnActivityResult=false;

        getIntentParams();

        m_viewPager = (ViewPager)m_mainll.findViewById(R.id.pager);
        m_adapter=new TransReceiptPagerAdapter(m_context, m_bDemoTransReceipt);
        m_viewPager.setAdapter(m_adapter);

        m_viewPager.setCurrentItem(m_posStart);

        m_cb_success_request_btprinter_connect=null;

        boolean bPrint=m_bPrintReceipt;
        if (m_bSignatureValidation) {
            if (m_bReceiptNeedSignature) {
                bPrint=false;
                startTransReceiptSignatureActivityForResult();
            }
        }

        if ( (m_bDemoTransReceipt)||(bPrint) ) {
            m_adapter.setPrintReceiptAtPosition(m_posStart);
            m_countPrintReceipt++;
            Logger.i(m_className, "m_countPrintReceipt: "+m_countPrintReceipt);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (m_bPrintCalledFromOnActivityResult) {
            int pos_current = m_viewPager.getCurrentItem();
            printReceipt(pos_current);
            m_bPrintCalledFromOnActivityResult=false;
        }
    }

    /**
     * Get intent parameters
     */
    public void getIntentParams() {
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

        key="demo";
        tmp=intent.getStringExtra(key);
        try {
            m_bDemoTransReceipt = Boolean.parseBoolean(tmp);
        }
        catch (Exception ex) {
            m_bDemoTransReceipt=false;
        }

        if (!m_bDemoTransReceipt) {
            key=TransReceiptActivity.KEY_SIGNATURE_VALIDATION;
            tmp=intent.getStringExtra(key);
            try {
                m_bSignatureValidation=Boolean.parseBoolean(tmp);
            }
            catch (Exception ex) {
                m_bSignatureValidation=false;
            }

            if (m_bSignatureValidation) {
                if (Application.dbTransSummaryDetailListForTransReceipt.size()!=1) {
                    Logger.e(m_className, "getIntentParams, Application.dbTransSummaryDetailListForTransReceipt.size is not equal to 1, val: "+Application.dbTransSummaryDetailListForTransReceipt.size());
                    m_bSignatureValidation=false;
                }
                else {
                    Data_dbTransSummaryDetail dbTransSummaryDetail=Application.dbTransSummaryDetailListForTransReceipt.get(0);
                    Data_AP_ONE dataAP = new Data_AP_ONE(dbTransSummaryDetail.m_dataBuf, Application.SkeyForMutuAuth);
                    Data_trans_summary dataTransSummary=new Data_trans_summary(dataAP);
                    boolean bSuccessLoadData=dataTransSummary.isSuccessLoadData();
                    if (bSuccessLoadData) {
                        boolean bValidTrans = dataTransSummary.isTransactionCompleteAndApproved();
                        if (bValidTrans) {
                            m_bReceiptNeedSignature = dataTransSummary.isSignatureRequired();
                        } else {
                            m_bSignatureValidation = false;
                        }
                    }
                    else {
                        Logger.w(m_className, "getIntentParams, Data_trans_summary loading is NOT SUCCESS");
                        m_bSignatureValidation=false;
                    }
                }
            }

            key=TransReceiptActivity.KEY_PRINTRECEIPT;
            tmp=intent.getStringExtra(key);
            try {
                m_bPrintReceipt=Boolean.parseBoolean(tmp);
            }
            catch (Exception ex) {
                m_bPrintReceipt=false;
            }

        }
    }

    @Override
    protected void onDestroy() {
        Application.dbTransSummaryDetailListForTransReceipt=null;
        super.onDestroy();
    }

    private void printReceipt(int pos) {
        if (!m_bDemoTransReceipt) {
            if (m_countPrintReceipt>0) {
                Logger.w(m_className, "printReceipt, m_countPrintReceipt>0, m_countPrintReceipt: "+m_countPrintReceipt);
                return;
            }
        }
        String strTagView = m_adapter.getTag_viewAt(pos);
        View v = (View) m_viewPager.findViewWithTag(strTagView);
        if (v==null) {
            Logger.w(m_className, "printReceipt, pos: "+pos+", view v is NULL");

            m_adapter.setPrintReceiptAtPosition(m_posStart);
            m_countPrintReceipt++;

            return;
        }
        if (!(v instanceof ViewTransreceiptLinearLayout)) {
            Logger.w(m_className, "printReceipt, pos: "+pos+", view v is NOT ViewTransreceiptLinearLayout");

            m_adapter.setPrintReceiptAtPosition(m_posStart);
            m_countPrintReceipt++;

            return;
        }
        ViewTransreceiptLinearLayout layout=(ViewTransreceiptLinearLayout)v;
        layout.printTransactionReceipt();

        m_countPrintReceipt++;
        Logger.i(m_className, "printReceipt, print at position: "+pos+", m_countPrintReceipt: " + m_countPrintReceipt);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String strYouNeedSignName= ResourcesHelper.getBaseContextString(m_context, R.string.you_need_sign_name);

        if (requestCode == ActivityRequestCodeEnum.RequestCode.REQUEST_TRANSACTION_SIGNATURE.toInt()) {
            if (resultCode == RESULT_OK) {

                if (m_bDemoTransReceipt) {
                    int pos_current = m_viewPager.getCurrentItem();
                    String strTagView = m_adapter.getTag_viewAt(pos_current);
                    View v = (View) m_viewPager.findViewWithTag(strTagView);
                    Bundle res = data.getExtras();
                    String fullpathSave = res.getString(TransReceiptSignatureActivity.FULLPATH_SAVE_TRANSRECEIPTSIGNATURE);
                    if (v != null) {
                        TransactionReceiptLinearLayout transactionReceiptll = (TransactionReceiptLinearLayout) v.findViewById(R.id.transaction_receipt_blue_container);
                        if (transactionReceiptll != null) {
                            transactionReceiptll.m_drawable_signature.load(fullpathSave);
                        }
                    }
                }
                else {
                    m_bSignatureValidation = false;
                    if (m_adapter != null) {
                        m_adapter.notifyDataSetChanged();
                    }
                }
            }
            else {
                if (m_bSignatureValidation) {
                    Toast.makeText(m_context, strYouNeedSignName, Toast.LENGTH_SHORT).show();
                    startTransReceiptSignatureActivityForResult();
                }
            }

            m_bPrintCalledFromOnActivityResult=true;
        }
        else if (requestCode == ActivityRequestCodeEnum.RequestCode.REQUEST_BTPRINTER_CONNECT.toInt()) {
            if (resultCode == RESULT_OK) {
                //Toast.makeText(m_context, "ActivityRequestCodeEnum.RequestCode.REQUEST_BTPRINTER_CONNECT", Toast.LENGTH_SHORT).show();
                if (m_cb_success_request_btprinter_connect!=null) {
                    try {
                        m_cb_success_request_btprinter_connect.call();
                    }
                    catch (Exception ex) {
                    }
                    m_cb_success_request_btprinter_connect=null;
                }
            }
        }

    }

    /**
     * Set callback for success of connection of Bluetooth printer
     * @param cb_success Callback for success of connection of Bluetooth printer
     */
    public void set_callback_success_request_btprinter_connect(Callback<Object> cb_success) {
        m_cb_success_request_btprinter_connect=cb_success;
    }

    /**
     * Start transaction receipt signature for activity result
     */
    public void startTransReceiptSignatureActivityForResult() {
        Data_dbTransSummaryDetail dbTransSummaryDetail=Application.dbTransSummaryDetailListForTransReceipt.get(0);
        Data_AP_ONE dataAP=new Data_AP_ONE(dbTransSummaryDetail.m_dataBuf, Application.SkeyForMutuAuth);
        Data_trans_summary dataTransSummary=new Data_trans_summary(dataAP);

        if (dataTransSummary==null) {
            Logger.w(m_className, "startTransReceiptSignatureActivityForResult, dataTransSummary is NULL");
            return;
        }
        if ( (dataTransSummary.m_fullpathSaveSignature==null)||(dataTransSummary.m_fullpathSaveSignature.equals("")) ) {
            Logger.w(m_className, "startTransReceiptSignatureActivityForResult, dataTransSummary.m_fullpathSaveSignature is NULL or empty");
            return;
        }

        int id=Application.ACTIVITY_TRANSRECEIPT_SIGNATURE;
        HashMap<String, String> hash=new HashMap<String, String>();
        hash.put(TransReceiptSignatureActivity.FULLPATH_SAVE_TRANSRECEIPTSIGNATURE, dataTransSummary.m_fullpathSaveSignature);
        boolean bWithSeparator=false;
        String strAmountTotal=dataTransSummary.getAmountTotalString(bWithSeparator);
        hash.put("totalAmountString", strAmountTotal);
        Application.startActivity(m_context, id, hash, ActivityRequestCodeEnum.RequestCode.REQUEST_TRANSACTION_SIGNATURE.toInt());
    }

    /**
     * Function to end of this activity
     */
    public void endActivity() {
        regis.endSale(DateTimeStrategy.getCurrentTime());
        this.finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void onOrientationPortrait() {

    }

    private void onOrientationLandscape() {

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
