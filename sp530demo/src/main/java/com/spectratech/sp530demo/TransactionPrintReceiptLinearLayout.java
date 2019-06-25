// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TransactionPrintReceiptLinearLayout.java

package com.spectratech.sp530demo;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
//import com.spectratech.lib.BuildConfig;
import com.spectratech.lib.Logger;
import com.spectratech.sp530demo.R;
import com.spectratech.lib.data.Data_trans_summary;
//import com.spectratech.lib.printer.bp80.BP80_PrintTemplateClass;
import com.spectratech.lib.printer.bp80.BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS;

import com.spectratech.lib.view.DrawableLinearLayout;
import com.spectratech.lib.view.TransactionReceiptLinearLayout;
import com.spectratech.sp530demo.domain.inventory.LineItem;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Referenced classes of package com.spectratech.lib.view:
//            DrawableLinearLayout, TransactionReceiptLinearLayout

public class TransactionPrintReceiptLinearLayout extends android.widget.LinearLayout {
    private final java.lang.String m_className = "TransactionPrintReceiptLinearLayout";
    private ArrayList<Map<String, String>> saleList;
    private ListView saleListView;
    private final String m_MONEYSYMBOL = "₦";
    protected android.content.Context m_context;
    private android.widget.TextView m_val_mid;
    private android.widget.TextView m_val_tid;
    private android.widget.TextView m_val_cardnumber;
    private android.widget.TextView m_val_cardname;
    private android.widget.TextView m_val_cardtype;
    private android.widget.TextView m_val_expiry;
    private android.widget.TextView m_val_txBig;
    private android.widget.TextView m_val_datetime;
    private android.widget.TextView m_val_batchno;
    private android.widget.TextView m_val_rrn;
    private android.widget.TextView m_val_traceno;
    private android.widget.TextView m_val_ecrref;
    private android.widget.LinearLayout m_container_training;
    private android.widget.TextView m_val_amount_base;
    private android.widget.TextView m_val_amount_tip;
    private android.widget.TextView m_val_amount_total;
    private android.widget.LinearLayout m_container_offline;
    public android.widget.LinearLayout m_container_cancel;
    public android.widget.LinearLayout m_container_decline;
    public android.widget.LinearLayout m_container_nosign;
    public android.widget.LinearLayout m_container_signature;
    public com.spectratech.lib.view.DrawableLinearLayout m_drawable_signature;
    private android.widget.TextView m_val_appcode;
    private android.widget.TextView m_val_app;
    private android.widget.TextView m_val_aid;
    private android.widget.TextView m_val_tc;
    private BP80_PrintTemplateClass m_bp80PrintTemplateClass;
    private Register register;

    public TransactionPrintReceiptLinearLayout(final Context context) {
        super(context);
        this.m_context = context;
        this.init();
    }

    public TransactionPrintReceiptLinearLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.m_context = context;
        this.init();
    }

    public TransactionPrintReceiptLinearLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.m_context = context;
        this.init();
    }

    private void init() {
        this.m_bp80PrintTemplateClass = new BP80_PrintTemplateClass(this.m_context);
    }

    public byte[] getPrintBytes() {
        byte[] buf = null;
        if (this.m_bp80PrintTemplateClass != null) {
            buf = this.m_bp80PrintTemplateClass.getTemplateBytes();
        }
        return buf;
    }

    public void initUIParams() {
        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        this.m_val_mid = (TextView) findViewById(R.id.val_mid);
        this.m_val_tid = (TextView) findViewById(R.id.val_tid);
        this.m_val_cardnumber = (TextView) findViewById(R.id.val_cardnumber);
        this.m_val_cardname = (TextView) findViewById(R.id.val_cardname);
        this.m_val_cardtype = (TextView) findViewById(R.id.val_cardtype);
        this.m_val_expiry = (TextView) findViewById(R.id.val_expiry);
        this.m_val_txBig = (TextView) findViewById(R.id.val_txBig);
        this.m_val_datetime = (TextView) findViewById(R.id.val_datetime);
        this.m_val_batchno = (TextView) findViewById(R.id.val_batchno);
        this.m_val_rrn = (TextView) findViewById(R.id.val_rrn);
        this.m_val_traceno = (TextView) findViewById(R.id.val_traceno);
        this.m_val_ecrref = (TextView) findViewById(R.id.val_ecrref);
        this.m_container_training = (LinearLayout) findViewById(R.id.training_container);
        this.m_val_amount_base = (TextView) findViewById(R.id.val_amount_base);
        this.m_val_amount_tip = (TextView) findViewById(R.id.val_amount_tip);
        this.m_val_amount_total = (TextView) findViewById(R.id.val_amount_total);
        this.m_container_offline = (LinearLayout) findViewById(R.id.offline_container);
        this.m_container_cancel = (LinearLayout) findViewById(R.id.cancel_container);
        this.m_container_decline = (LinearLayout) findViewById(R.id.decline_container);
        this.m_container_nosign = (LinearLayout) findViewById(R.id.nosign_container);
        this.m_container_signature = (LinearLayout) findViewById(R.id.container_signature);
        this.m_drawable_signature = (DrawableLinearLayout) findViewById(R.id.transaction_printreceipt_signature);
        this.m_val_appcode = (TextView) findViewById(R.id.val_appcode);
        this.m_val_app = (TextView) findViewById(R.id.val_app);
        this.m_val_aid = (TextView) findViewById(R.id.val_aid);
        this.m_val_tc = (TextView) findViewById(R.id.val_tc);
        this.saleListView = (ListView)findViewById(R.id.sale_ListPrint1);
    }
    public void setUIForDemo() {
        this.set_tid("7700013");
        this.set_cardnumber("NIL");
        this.set_cardname("NIL");
        this.set_cardtype("NIL");
        this.set_txBig("SALE");
        this.set_expiry("NIL");
        this.set_datetime("NIL");
        this.set_batchnumber("000001");
        this.set_rrn("000000002777");
        this.set_tracenumber("002776");
        this.set_ecrref("1");
        String strAmountTotal =register.getTotal() + "";
        strAmountTotal = "NGR #" + strAmountTotal;
        this.set_amount_base(strAmountTotal);
        this.m_container_offline.setVisibility(0);
        this.set_appcode("000000");
        this.set_app("VISA CREDIT");
        this.set_aid("A0000000031010");
        this.set_tc("F20035C0A59351A0");
    }
    public void showList(List<LineItem> list) {

        this.saleList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            this.saleList.add(line.toMap());
        }

        SimpleAdapter sAdap;
        sAdap = new SimpleAdapter(m_context, this.saleList,
                R.layout.listview_lineitem, new String[]{"name","quantity","price"}, new int[] {R.id.name,R.id.quantity,R.id.price});
        this.saleListView.setAdapter(sAdap);
    }

    public String getTotalAmountUnderlineString(final int length_string) {
        String s = "";
        for (int i = 0; i < length_string; ++i) {
            s += "=";
        }
        return s;
    }

    public void setUI(final Data_trans_summary dataTransSummary) {
        String msg = dataTransSummary.getStringFromByteBuf(40726);
        this.set_mid(msg);
        msg = dataTransSummary.getStringFromByteBuf(40732);
        this.set_tid(msg);
        msg = dataTransSummary.getStringFromByteBuf(90);
        if (msg != null) {
            final char[] myNameChars = msg.toCharArray();
            for (int i = 0; i < myNameChars.length - 4; ++i) {
                myNameChars[i] = 'X';
            }
            msg = new String(myNameChars);
        }
        this.set_cardnumber(msg);
        this.set_cardname("TEST CARD 8");
        this.set_cardtype("VISA");
        this.set_txBig("SALE");
        this.set_expiry("03/17");
        msg = TransactionReceiptLinearLayout.getTransactionTime(dataTransSummary);
        this.set_datetime(msg);
        this.set_batchnumber("000001");
        this.set_rrn("000000002777");
        msg = dataTransSummary.getStringFromByteBuf(40769);
        this.set_tracenumber(msg);
        this.set_ecrref("1");
        this.set_appcode("164234");
        this.set_app("VISA CREDIT");
        msg = dataTransSummary.getStringFromByteBuf(40710);
        this.set_aid(msg);
        this.set_tc("F20035C0A59351A0");
        //String strAmountTotal = dataTransSummary.getAmountTotalString();
        String strAmountTotal =register.getTotal() + "";
        strAmountTotal = "NGR #" + strAmountTotal;
        this.set_amount_base(strAmountTotal);
        boolean bShowOffline = false;
        if (dataTransSummary.isTransactionComplete() && dataTransSummary.isTransactionApproved() && !dataTransSummary.isHostApp()) {
            bShowOffline = true;
        }
        if (!bShowOffline) {
            this.m_container_offline.setVisibility(8);
        }
        else {
            this.set_offline();
            this.m_container_offline.setVisibility(0);
        }
        BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS signLineStatus = BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.EMPTY;
        if (dataTransSummary.isTransactionComplete()) {
            if (!dataTransSummary.isTransactionApproved()) {
                this.m_container_decline.setVisibility(0);
                signLineStatus = BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.DECLINE;
            }
        }
        else {
            this.m_container_cancel.setVisibility(0);
            signLineStatus = BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.CANCEL;
        }
        if (dataTransSummary.isTransactionComplete()) {
            if (dataTransSummary.isTransactionApproved()) {
                if (dataTransSummary.isSignatureRequired()) {
                    this.m_container_signature.setVisibility(0);
                    this.m_container_nosign.setVisibility(8);
                    signLineStatus = BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.SIGN;
                }
                else {
                    this.m_container_signature.setVisibility(8);
                    this.m_container_nosign.setVisibility(0);
                    signLineStatus = BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.NO_SIGN;
                }
            }
            else {
                this.m_container_signature.setVisibility(8);
            }
        }
        else {
            this.m_container_signature.setVisibility(8);
        }
        this.set_signatureLinesClassStatus(signLineStatus);
    }

    public void set_BarCodeDataLineClassData(final String strNumber) {
        if (this.m_bp80PrintTemplateClass != null) {
            this.m_bp80PrintTemplateClass.set_BarCodeDataLineClassData(strNumber);
        }
        else {
            Logger.w("TransactionPrintReceiptLinearLayout", "set_BarCodeDataLineClassData, m_bp80PrintTemplateClass is NULL");
        }
    }

    public void set_signatureLineClassData(final byte[] buf) {
        if (this.m_bp80PrintTemplateClass != null) {
            this.m_bp80PrintTemplateClass.set_signatureLinesClassData(buf);
        }
        else {
            Logger.w("TransactionPrintReceiptLinearLayout", "set_signatureLinesClassStatus, m_bp80PrintTemplateClass is NULL");
        }
    }

    public void set_signatureLinesClassStatus(final BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS signLineStatus) {
        if (this.m_bp80PrintTemplateClass != null) {
            this.m_bp80PrintTemplateClass.set_signatureLinesClassStatus(signLineStatus);
        }
        else {
            Logger.w("TransactionPrintReceiptLinearLayout", "set_signatureLinesClassStatus, m_bp80PrintTemplateClass is NULL");
        }
    }

    public BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS get_signatureLineClassStatus() {
        if (this.m_bp80PrintTemplateClass != null) {
            final BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS signLineStatus = this.m_bp80PrintTemplateClass.get_signatureLinesClassStatus();
            return signLineStatus;
        }
        Logger.w("TransactionPrintReceiptLinearLayout", "set_signatureLinesClassStatus, m_bp80PrintTemplateClass is NULL");
        return BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.EMPTY;
    }

    private void setText(final TextView tv, final String msg) {
        if (tv != null) {
            tv.setText((CharSequence)msg);
        }
    }

    public void set_mid(final String msg) {
        this.setText(this.m_val_mid, msg);
        this.m_bp80PrintTemplateClass.set_mid(msg);
    }

    public void set_tid(final String msg) {
        this.setText(this.m_val_tid, msg);
        this.m_bp80PrintTemplateClass.set_tid(msg);
    }

    public void set_cardnumber(final String msg) {
        this.setText(this.m_val_cardnumber, msg);
        this.m_bp80PrintTemplateClass.set_cardnumber(msg);
    }

    public void set_cardname(final String msg) {
        this.setText(this.m_val_cardname, msg);
        this.m_bp80PrintTemplateClass.set_cardname(msg);
    }

    public void set_cardtype(final String msg) {
        this.setText(this.m_val_cardtype, msg);
        this.m_bp80PrintTemplateClass.set_cardtype(msg);
    }

    public void set_expiry(final String msg) {
        this.setText(this.m_val_expiry, msg);
        this.m_bp80PrintTemplateClass.set_expiry(msg);
    }

    public void set_txBig(final String msg) {
        this.setText(this.m_val_txBig, msg);
        this.m_bp80PrintTemplateClass.set_txBig(msg);
    }

    public void set_datetime(final String msg) {
        this.setText(this.m_val_datetime, msg);
        this.m_bp80PrintTemplateClass.set_datetime(msg);
    }

    public void set_batchnumber(final String msg) {
        this.setText(this.m_val_batchno, msg);
        this.m_bp80PrintTemplateClass.set_batchnumber(msg);
    }

    public void set_rrn(final String msg) {
        this.setText(this.m_val_rrn, msg);
        this.m_bp80PrintTemplateClass.set_rrn(msg);
    }

    public void set_tracenumber(final String msg) {
        this.setText(this.m_val_traceno, msg);
        this.m_bp80PrintTemplateClass.set_tracenumber(msg);
    }

    public void set_ecrref(final String msg) {
        this.setText(this.m_val_ecrref, msg);
        this.m_bp80PrintTemplateClass.set_ecrref(msg);
    }

    public void set_amount_base(final String msg) {
        this.setText(this.m_val_amount_base, msg);
        this.m_bp80PrintTemplateClass.set_amount_base(msg);
    }

    public void set_amount_tip(final String msg) {
        this.setText(this.m_val_amount_tip, msg);
        this.m_bp80PrintTemplateClass.set_amount_tip(msg);
    }

    public void set_amount_total(final String msg) {
        this.setText(this.m_val_amount_total, msg);
        this.m_bp80PrintTemplateClass.set_amount_total(msg);
    }

    public void set_appcode(final String msg) {
        this.setText(this.m_val_appcode, msg);
        this.m_bp80PrintTemplateClass.set_approvlcode(msg);
    }

    public void set_app(final String msg) {
        this.setText(this.m_val_app, msg);
        this.m_bp80PrintTemplateClass.set_app(msg);
    }

    public void set_aid(final String msg) {
        this.setText(this.m_val_aid, msg);
        this.m_bp80PrintTemplateClass.set_aid(msg);
    }

    public void set_tc(final String msg) {
        this.setText(this.m_val_tc, msg);
        this.m_bp80PrintTemplateClass.set_tc(msg);
    }

    public void set_offline() {
        this.m_bp80PrintTemplateClass.set_offline();
    }
}