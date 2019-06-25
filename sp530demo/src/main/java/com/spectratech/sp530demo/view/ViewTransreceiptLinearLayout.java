package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Bytes;
import com.spectratech.lib.Callback;
import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.ImageHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.ViewCaptureHelper;
import com.spectratech.lib.bluetooth.BluetoothConnectActivity;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.data.Data_binaryImageBitPadded;
import com.spectratech.lib.data.Data_dbTransSummaryDetail;
import com.spectratech.lib.data.Data_drawable_lineset;
import com.spectratech.lib.data.Data_trans_summary;
//import com.spectratech.lib.printer.bp80.BP80_PrintTemplateClass;
import com.spectratech.lib.printer.bp80.Lpt_funcCHelper;
import com.spectratech.lib.printer.bp80.constant.BP80_Constant;
import com.spectratech.lib.printer.bp80.constant.BTPrinterProtocolConstant;
import com.spectratech.lib.sp530.data.Data_AP_ONE;
import com.spectratech.lib.view.DrawableLinearLayout;
//import com.spectratech.lib.view.TransactionPrintReceiptLinearLayout;
import com.spectratech.lib.view.TransactionReceiptLinearLayout;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.BP80_PrintTemplateClass;
import com.spectratech.sp530demo.BTPrinterBluetoothConnectActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.SP530DemoBaseActivity;
import com.spectratech.sp530demo.SP530DemoBaseFragmentActivity;
import com.spectratech.sp530demo.TransReceiptActivity;
import com.spectratech.sp530demo.TransReceiptSignatureActivity;
import com.spectratech.sp530demo.TransactionPrintReceiptLinearLayout;
import com.spectratech.sp530demo.conf.PathClass;
import com.spectratech.sp530demo.constant.ActivityRequestCodeEnum;
import com.spectratech.sp530demo.data.Data_setting_devicebluetoothprinter;
import com.spectratech.sp530demo.domain.inventory.LineItem;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ViewTransreceiptLinearLayout - transaction receipt of SP530
 */
public class ViewTransreceiptLinearLayout extends LinearLayout {
    private static final String m_className="ViewTransreceiptLinearLayout";
    private Register register;

    private static final DateFormat timeStampFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS  ", Locale.US);

    protected Context m_context;

    private LinearLayout m_receiptContainerParent;

    // view set one
    private TransactionReceiptLinearLayout m_transaction_receipt_container;
    private LinearLayout m_transaction_receipt;

    // view set two
    private TransactionPrintReceiptLinearLayout m_transaction_printreceipt_container;
    private LinearLayout m_transaction_printreceipt;

    private Button m_btnOK;
    private Button m_btnZoom;
    private Button m_btnToggleView;

    private Button m_btnPrintView;

    private LinearLayout m_btnShare;

    private float m_DEFAULTTransReceiptScaleFactor=0.95f;

    private boolean m_bDemoPurpose;

    private boolean m_bUseLowResolution;

    private boolean m_bCapturingImage;

    private int m_countCaptureViewTransactionReceipt;

    private int m_countToggleView;

    private Data_trans_summary m_dataTransSummary;

    private boolean m_bSignatureValidation;

    private String m_fullSavePath_signature;


    public ViewTransreceiptLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        m_context=context;
        init();
    }

    public ViewTransreceiptLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context=context;
        init();
    }

    private void init() {
        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        m_bDemoPurpose=false;
        m_bCapturingImage=false;
        m_countCaptureViewTransactionReceipt=0;

        m_bUseLowResolution=determineNeed2UseLowResolution();

        m_transaction_printreceipt_container=null;

        m_dataTransSummary=null;

        m_bSignatureValidation=false;

        m_fullSavePath_signature="";
    }

    private void toggleView() {
        m_receiptContainerParent.removeAllViews();
        m_countToggleView++;
        m_countToggleView=(m_countToggleView%2);
        if (m_countToggleView==0) {
            m_receiptContainerParent.addView(m_transaction_receipt_container);
        }
        else {
            initPrintReceipt();
            m_receiptContainerParent.addView(m_transaction_printreceipt_container);
//                    setTransactionReceiptFitScreen(m_transaction_printreceipt_container, m_transaction_printreceipt);
//                    m_transaction_printreceipt_container.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }


    private synchronized void initPrintReceipt() {
        if (m_transaction_printreceipt_container==null) {
            //showList(register.getCurrentSale().getAllLineItem());
            if (m_bDemoPurpose) {
                m_transaction_printreceipt_container = (TransactionPrintReceiptLinearLayout) LinearLayout.inflate(m_context, R.layout.transaction_printreceipt_cash, null);
                m_transaction_printreceipt_container.initUIParams();
                m_transaction_printreceipt_container.setUIForDemo();
                m_transaction_printreceipt_container. showList(register.getCurrentSale().getAllLineItem());
               // m_transaction_printreceipt_container.setUIForDemo();
            } else {
                m_transaction_printreceipt_container = (TransactionPrintReceiptLinearLayout) LinearLayout.inflate(m_context, R.layout.transaction_printreceipt_doubl, null);
                m_transaction_printreceipt_container.initUIParams();
                m_transaction_printreceipt_container.setUI(m_dataTransSummary);
                m_transaction_printreceipt_container. showList(register.getCurrentSale().getAllLineItem());

            }
            m_transaction_printreceipt = (LinearLayout) m_transaction_printreceipt_container.findViewById(R.id.transaction_printreceipt);
        }
        // load signature if file exist
        if (!m_bSignatureValidation) {
            if ((m_fullSavePath_signature != null) && (!m_fullSavePath_signature.equals(""))) {
                m_transaction_printreceipt_container.m_drawable_signature.load(m_fullSavePath_signature);
            }
        }
    }
   /* public void showList(List<LineItem> list) {
        check=(TextView)findViewById(R.id.check);
        saleListView = (ListView)findViewById(R.id.sale_ListPrint);

        //check.setText("Hello");

        saleList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            saleList.add(line.toMap());
        }

        SimpleAdapter sAdap;
        sAdap = new SimpleAdapter(m_context, saleList,R.layout.listview_lineitem, new String[]{"price"}, new int[] {R.id.price});
       // saleListView.setAdapter(sAdap);
    }
*/

    public void printTransactionReceipt() {
        if (!Data_setting_devicebluetoothprinter.isEnableBTPrinter(m_context)) {
            Logger.i(m_className, "printTransactionReceipt, enable bt printer is FALSE");
            return;
        }

        final String strPleaseConnectBTPrinter=ResourcesHelper.getBaseContextString(m_context, R.string.please_connect_bt_printer);

        boolean bReadyPrinter=BTPrinterBluetoothConnectActivity.isReadyPrinter();
        if (!bReadyPrinter) {
            Toast.makeText(m_context, strPleaseConnectBTPrinter, Toast.LENGTH_SHORT).show();

            Callback<Object> cb_success=new Callback<Object>() {
                @Override
                public Object call() throws Exception {
                    printTransactionReceiptByLpt_funcCHelper();
                    return null;
                }
            };
            ((TransReceiptActivity)m_context).set_callback_success_request_btprinter_connect(cb_success);

            int reqCode= ActivityRequestCodeEnum.RequestCode.REQUEST_BTPRINTER_CONNECT.toInt();
            HashMap<String, String> data=new HashMap<String, String>();
            data.put(BluetoothConnectActivity.RESPONSE_ENDACTITYWITHRESULT_WHENCONNECTED, "1");
            BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context, data, reqCode);
            return;
        }
        printTransactionReceiptByLpt_funcCHelper();
    }
    public void printTransactionReceiptByLpt_funcCHelper() {
//        if (m_countToggleView%2!=1) {
//            toggleView();
//        }
        initPrintReceipt();

        Lpt_funcCHelper instLpt_funcCHelper=Lpt_funcCHelper.getInstance();
        Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
        instLpt_funcCHelper.setPacketEncapsulateLevel(dataBTPrinterConf.m_packetEncapsulateLevel);
        BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
        InputStream is=btSocketClass.getInputStream();
        OutputStream os=btSocketClass.getOutputStream();
        instLpt_funcCHelper.setIO(is, os);
        instLpt_funcCHelper.setContext(m_context);

        if ( (m_bDemoPurpose) || ( m_dataTransSummary.isTransactionComplete()&&m_dataTransSummary.isTransactionApproved()&& m_dataTransSummary.isSignatureRequired() ) ) {
            byte[] bufSignature=getSignatureData();
            if (bufSignature!=null) {
                m_transaction_printreceipt_container.set_signatureLineClassData(bufSignature);
            }
            else {
                BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS signLineStatus=m_transaction_printreceipt_container.get_signatureLineClassStatus();
                if (signLineStatus== BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.SIGN) {
                    m_transaction_printreceipt_container.set_signatureLinesClassStatus(BP80_PrintTemplateClass.SIGNATURE_LINE_STATUS.NO_SIGN);
                }
            }
        }

        // handle print template
        byte[] dataBuf=m_transaction_printreceipt_container.getPrintBytes();
        if (dataBuf==null) {
            Logger.w(m_className, "printTransactionReceipt, dataBuf is NULL");
            return;
        }
        if (dataBuf!=null) {
            byte[] printBuf=new byte[dataBuf.length+4];
            instLpt_funcCHelper.page_pack_start(printBuf, printBuf.length);
            instLpt_funcCHelper.page_pack_mem(dataBuf, dataBuf.length);
            instLpt_funcCHelper.page_pack_end();
        }
    }

    private byte[] getSignatureData() {
        Logger.i(m_className, "getSignatureData called");
        byte[] signBmpBuf=null;
        Bitmap bmpSign = null;
        Data_drawable_lineset data = m_transaction_printreceipt_container.m_drawable_signature.exportLineset2DrawableLineset();
        if (data.m_lines.size() > 0) {
            int width = BP80_Constant.MAX_PIXEL_WIDTH;
            int height = BP80_Constant.MAX_BUFFER_HEIGHT_FORFULLPIXELWIDTH;
            DrawableLinearLayout drawablell = new DrawableLinearLayout(m_context);
            LayoutParams lp = new LayoutParams(width, height);
            drawablell.setLayoutParams(lp);
            drawablell.load(data);
            drawablell.setStrokeWidth(2.0f);
            drawablell.setDraw2Center();
            drawablell.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            ViewCaptureHelper instViewCaptureHelper = ViewCaptureHelper.getInstance();
            bmpSign = instViewCaptureHelper.getBmpImageWithExactlyDimension(m_context, drawablell, width, height);
            // get signature image data
            if (bmpSign != null) {
                byte[] tmpData = null;
                List<Byte> dataList = new ArrayList<Byte>();
                ImageHelper imageHelper = ImageHelper.getInstance();
                Data_binaryImageBitPadded dataBinaryImageBitPadded = imageHelper.padToBinaryBitData(bmpSign, true);
                dataList.add(BTPrinterProtocolConstant.ESC_VAL);

                dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_PRINTGRAPHIC)));

                dataList.add((byte) (dataBinaryImageBitPadded.m_width / 8));
                dataList.add((byte) dataBinaryImageBitPadded.m_height);
                tmpData = dataBinaryImageBitPadded.m_data;
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpData)));

                signBmpBuf = Bytes.toArray(dataList);
                Logger.i(m_className, "getSignatureData, signBmpBuf.length: "+signBmpBuf.length);
            }
        }
        return signBmpBuf;
    }

    public void initUIs(int position, boolean bDemoPurpose) {

        final String strSharingOptions=ResourcesHelper.getBaseContextString(m_context, R.string.sharing_options);

        m_bDemoPurpose=bDemoPurpose;

        m_receiptContainerParent=(LinearLayout)this.findViewById(R.id.transaction_receipt_blue_container_parent);
        {
            LinearLayout ll=null;
            if (m_bUseLowResolution) {
                ll = (LinearLayout) LinearLayout.inflate(m_context, R.layout.transaction_receipt_blue, null);
            }
            else {
                ll = (LinearLayout) LinearLayout.inflate(m_context, R.layout.transaction_receipt_blue_double, null);
            }

            m_transaction_receipt_container=(TransactionReceiptLinearLayout)ll.findViewById(R.id.transaction_receipt_blue_container);
            m_transaction_receipt_container.initUIParams();
            m_transaction_receipt_container.setUseLowResolution(m_bUseLowResolution);

            m_transaction_receipt=(LinearLayout)ll.findViewById(R.id.transaction_receipt_blue);

            m_receiptContainerParent.addView(ll);
        }
        m_countToggleView=0;

        m_btnOK=(Button)this.findViewById(R.id.transaction_receipt_ok);
        m_btnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_context instanceof TransReceiptActivity) {
                    TransReceiptActivity act=(TransReceiptActivity)m_context;
                    if (!act.isOnPause()) {
                        act.endActivity();
                    }
                }
            }
        });

        m_btnZoom=(Button)this.findViewById(R.id.transaction_receipt_zoom);
        m_btnZoom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean bPause = false;
                if (m_context instanceof SP530DemoBaseActivity) {
                    bPause = ((SP530DemoBaseActivity) m_context).isOnPause();
                } else if (m_context instanceof SP530DemoBaseFragmentActivity) {
                    bPause = ((SP530DemoBaseFragmentActivity) m_context).isOnPause();
                }
                if (!bPause) {
                    if (m_bCapturingImage) {
                        Toast.makeText(m_context, "Please wait ...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean bCapture = captureViewTransactionReceiptPlease();
                    if (!bCapture) {
                        Toast.makeText(m_context, "Fail to generate transaction receipt", Toast.LENGTH_SHORT).show();
                    }
                    int id = Application.ACTIVITY_TRANSRECEIPTTOUCHIMAGESINGLE;
                    Application.startActivity(m_context, id);
                    ((Activity) m_context).overridePendingTransition(0, 0);
                }
            }
        });

        m_btnToggleView=(Button)this.findViewById(R.id.transaction_receipt_toggleview);
        m_btnToggleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleView();
            }
        });

        m_btnPrintView=(Button)this.findViewById(R.id.transaction_receipt_printview);
        m_btnPrintView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printTransactionReceipt();
            }
        });
        if (m_btnPrintView!=null) {
            if (Data_setting_devicebluetoothprinter.isEnableBTPrinter(m_context)) {
                if (m_btnPrintView.getVisibility()!=View.VISIBLE) {
                    m_btnPrintView.setVisibility(View.VISIBLE);
                }
            }
            else {
                if (m_btnPrintView.getVisibility()!=View.GONE) {
                    m_btnPrintView.setVisibility(View.GONE);
                }
            }
        }

        m_btnShare=(LinearLayout)this.findViewById(R.id.transaction_receipt_share_container);
        m_btnShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_bCapturingImage) {
                    Toast.makeText(m_context, "Please wait ...", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean bCapture = captureViewTransactionReceiptPlease();
                if (!bCapture) {
                    Toast.makeText(m_context, "Fail to generate transaction receipt", Toast.LENGTH_SHORT).show();
                }

                String EMAIL_SUBJECT = "Transaction receipt";
                String EMAIL_BODY = "The transaction receipt in jpg format is attached in this email."+
                        "\n"+timeStampFmt.format(new Date());

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("image/jpeg");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
                emailIntent.putExtra(Intent.EXTRA_TEXT, EMAIL_BODY);
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + PathClass.CAPTURE_TRANSRECEIPT_FULLPATH));
                m_context.startActivity(Intent.createChooser(emailIntent, strSharingOptions));
            }
        });

        m_transaction_receipt_container.m_container_signature.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean bOnPause = false;
                if (m_context instanceof SP530DemoBaseActivity) {
                    bOnPause = ((SP530DemoBaseActivity) m_context).isOnPause();
                } else if (m_context instanceof SP530DemoBaseFragmentActivity) {
                    bOnPause = ((SP530DemoBaseFragmentActivity) m_context).isOnPause();
                }
                if (!bOnPause) {
                    if (m_bDemoPurpose) {
                        HashMap<String, String> hash = new HashMap<String, String>();
                        hash.put("demo", "true");
                        int id = Application.ACTIVITY_TRANSRECEIPT_SIGNATURE;
                        Application.startActivity(m_context, id, hash, ActivityRequestCodeEnum.RequestCode.REQUEST_TRANSACTION_SIGNATURE.toInt());
                        return;
                    }
                }
            }
        });
        m_transaction_receipt_container.m_drawable_signature.setEditable(false);

        if (m_bDemoPurpose) {
            m_transaction_receipt_container.setUIForDemo();

            m_fullSavePath_signature = TransReceiptSignatureActivity.getFullSavePathForDemo(m_context);
        }
        else {
            Data_dbTransSummaryDetail dbTransSummaryDetail = Application.dbTransSummaryDetailListForTransReceipt.get(position);
            Data_AP_ONE dataAP=new Data_AP_ONE(dbTransSummaryDetail.m_dataBuf, Application.SkeyForMutuAuth);
            m_dataTransSummary=new Data_trans_summary(dataAP);

            m_transaction_receipt_container.setUI(m_dataTransSummary);

            m_fullSavePath_signature = m_dataTransSummary.m_fullpathSaveSignature;

            // need signature validation?
            if (position==0) {
                if (m_context instanceof TransReceiptActivity) {
                    TransReceiptActivity act=(TransReceiptActivity)m_context;
                    m_bSignatureValidation=act.m_bSignatureValidation;
                }
            }
        }

        // load signature if file exist
        if (!m_bSignatureValidation) {
            if ((m_fullSavePath_signature != null) && (!m_fullSavePath_signature.equals(""))) {
                m_transaction_receipt_container.m_drawable_signature.load(m_fullSavePath_signature);
            }
        }

        setTransactionReceiptFitScreen(m_transaction_receipt_container, m_transaction_receipt);

        // to print receipt view
        toggleView();
    }

    private void setTransactionReceiptFitScreen(LinearLayout container, LinearLayout receipt) {
        if (container==null) {
            Logger.e(m_className, "setTransactionReceiptFitScreen, container is NULL");
            return;
        }
        if (receipt==null) {
            Logger.e(m_className, "setTransactionReceiptFitScreen, receipt is NULL");
            return;
        }

        float viewTransReceiptScaleFactor=1.0f;

        LayoutParams lp= (LayoutParams)receipt.getLayoutParams();

        DisplayHelper instDisplayHelper=DisplayHelper.getInstance();
        viewTransReceiptScaleFactor=instDisplayHelper.getFitScreenScaleFactor(m_context, lp.width, lp.height);
//        viewTransReceiptScaleFactor = instDisplayHelper.getFitScreenScaleFactorByWidth(m_context, lp.width);
        viewTransReceiptScaleFactor*=m_DEFAULTTransReceiptScaleFactor;
        //Logger.i(m_className, "setTransactionReceiptFitScreen, factor: " + m_viewTransReceiptScaleFactor);
        receipt.setScaleX(viewTransReceiptScaleFactor);
        receipt.setScaleY(viewTransReceiptScaleFactor);

        int wActualTransReceipt=(int)(lp.width*viewTransReceiptScaleFactor);
        int hActualTransReceipt=(int)(lp.height*viewTransReceiptScaleFactor);
        LayoutParams tmpLp=new LayoutParams(wActualTransReceipt, hActualTransReceipt);
        container.setLayoutParams(tmpLp);
    }

    private boolean captureViewTransactionReceiptPlease() {
        if (m_countToggleView==0) {
            return captureViewTransactionTraditionalReceiptPlease();
        }
        else {
            return captureViewTransactionPrintReceiptPlease();
        }
    }
    private boolean captureViewTransactionPrintReceiptPlease() {
        boolean bCapture=false;
        if (m_transaction_printreceipt==null) {
            Logger.e(m_className, "captureViewTransactionPrintReceiptPlease, m_transaction_printreceipt is NULL");
            m_bCapturingImage=false;
            return bCapture;
        }
        m_bCapturingImage=true;

        int width=(int)m_context.getResources().getDimension(R.dimen.transaction_printreceipt_width_double);

        String fullPath=PathClass.CAPTURE_TRANSRECEIPT_FULLPATH;
        ViewCaptureHelper instViewCaptureHelper=ViewCaptureHelper.getInstance();
        bCapture=instViewCaptureHelper.captureImageWithWidthHasExactlyDimension(m_context, m_transaction_printreceipt, width, fullPath);
        if (!bCapture) {
            Logger.e(m_className, "captureViewTransactionPrintReceiptPlease, view cannot capture");
            return bCapture;
        }

        m_countCaptureViewTransactionReceipt++;

        Logger.i(m_className, "captureViewTransactionPrintReceiptPlease, called finish");

        m_bCapturingImage=false;

        bCapture=true;
        return bCapture;
    }
    private boolean captureViewTransactionTraditionalReceiptPlease() {
        boolean bCapture=false;
        if (m_transaction_receipt==null) {
            Logger.e(m_className, "captureViewTransactionReceipt, m_transaction_receipt is NULL");
            m_bCapturingImage=false;
            return bCapture;
        }
        m_bCapturingImage=true;

        int width=0;
        int height=0;
        if (m_bUseLowResolution) {
            width=(int)m_context.getResources().getDimension(R.dimen.transaction_receipt_width);
            height=(int)m_context.getResources().getDimension(R.dimen.transaction_receipt_height);
        }
        else {
            width=(int)m_context.getResources().getDimension(R.dimen.transaction_receipt_width_double);
            height=(int)m_context.getResources().getDimension(R.dimen.transaction_receipt_height_double);
        }

        String fullPath=PathClass.CAPTURE_TRANSRECEIPT_FULLPATH;
        ViewCaptureHelper instViewCaptureHelper=ViewCaptureHelper.getInstance();
        bCapture=instViewCaptureHelper.captureImageWithExactlyDimension(m_context, m_transaction_receipt, width, height, fullPath);
        if (!bCapture) {
            Logger.e(m_className, "captureViewTransactionReceipt, view cannot capture");
            return bCapture;
        }

        setTransactionReceiptFitScreen(m_transaction_receipt_container, m_transaction_receipt);

        m_countCaptureViewTransactionReceipt++;

        Logger.i(m_className, "captureViewTransactionReceipt, called finish");

        m_bCapturingImage=false;

        bCapture=true;
        return bCapture;
    }


    // jl commented
    // 20150624
    // we can use one resolution for lower resolution devices during view capturing
    private boolean determineNeed2UseLowResolution() {
        return false;
//        boolean bUseLowResolution=false;
//        int width=(int)m_context.getResources().getDimension(R.dimen.transaction_receipt_width_double);
//        int height=(int)m_context.getResources().getDimension(R.dimen.transaction_receipt_height_double);
//        DisplayHelper instDisplayHelper=DisplayHelper.getInstance();
//        int[] dim=instDisplayHelper.getScreenSize(m_context);
//        if ( (dim[0]<width)||(dim[1]<height) ) {
//            bUseLowResolution=true;
//            Logger.i(m_className, "init, low resolution is used");
//        }
//        return bUseLowResolution;
    }


}
