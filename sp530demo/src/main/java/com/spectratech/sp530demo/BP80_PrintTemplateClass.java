// 
// Decompiled by Procyon v0.5.30
// 

package com.spectratech.sp530demo;

import com.spectratech.lib.FontHelper;
import java.io.UnsupportedEncodingException;
import com.spectratech.lib.printer.bp80.constant.BTPrinterProtocolConstant;
import com.spectratech.lib.Logger;
import com.google.common.primitives.Bytes;
import java.util.Collection;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import com.spectratech.lib.R;
import com.spectratech.sp530demo.domain.inventory.LineItem;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;


public class BP80_PrintTemplateClass
{
    private static final String m_className = "BP80PrintTemplateClass";
    private static byte creturn;
    private Context m_context;
    private List<Object> m_lineList;
    private HashMap<String, Object> m_hashInput;
    private Register register;
    private ArrayList<Map<String, String>> saleList;
    
    public BP80_PrintTemplateClass(final Context context) {
        this.m_context = context;
        this.init();
        this.initTemplate();
    }
    
    private void init() {
        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        this.m_lineList = new ArrayList<Object>();
        this.m_hashInput = new HashMap<String, Object>();
    }
    private void showList(List<LineItem> list) {

        this.saleList = new ArrayList<Map<String, String>>();
        for (LineItem line : list) {
            this.saleList.add(line.toMap());
        }
    }


    
    private void initTemplate() {
        showList(register.getCurrentSale().getAllLineItem());
        String tmpStr = "";
        LineBaseClass lbc = null;
        StringReferenceObject strRefObj = null;
        this.addPrintDrawableFromFlash(0);
        lbc = new FullLineClass();
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_mid);
        lbc = new OneThirdAndTwoThirdLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(1);
        this.m_hashInput.put("mid", strRefObj);
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_tid);
        lbc = new OneThirdAndTwoThirdLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(1);
        this.m_hashInput.put("tid", strRefObj);
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_val_separator);
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);

        tmpStr ="COMPANY";
        lbc = new OneThirdAndTwoThirdLineClass();
        lbc.setValue(0, tmpStr);
        tmpStr = "PAYUP PAYMENT SYSTEMS";
        lbc.setValue(1, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);

        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("cardnumber", strRefObj);
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("cardname", strRefObj);
        tmpStr = "";
        lbc = new t240v144LineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("cardtype", strRefObj);
        strRefObj = lbc.m_inputValList.get(1);
        this.m_hashInput.put("expiry", strRefObj);
        this.set_expiry("");
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("txBig", strRefObj);
        tmpStr = "";
        lbc = new t240v144LineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("datetime", strRefObj);
        strRefObj = lbc.m_inputValList.get(1);
        this.m_hashInput.put("batchnumber", strRefObj);
        this.set_batchnumber("");
        tmpStr = "";
        lbc = new t240v144LineClass();
        lbc.setValue(0, tmpStr);
        this.set_rrn("");
        tmpStr = "";
        lbc.setValue(1, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("rrn", strRefObj);
        strRefObj = lbc.m_inputValList.get(1);
        this.m_hashInput.put("tracenumber", strRefObj);
        this.set_rrn("");
        this.set_tracenumber("");
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("ecrref", strRefObj);
        this.set_ecrref("");
        this.addLineFeed();

        tmpStr ="PRODUCT NAME";
        lbc = new ThreeEqualLineClass();
        lbc.setValue(0, tmpStr);
        tmpStr = "QTY  ";
        lbc.setValue(1, tmpStr);
        tmpStr = "AMOUNT";
        lbc.setValue(2, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        for (int i = 0; i < this.saleList.size(); i++) {

            String name = this.saleList.get(i).get("name").toString();
            String quantity = this.saleList.get(i).get("quantity").toString()+"    ";
            String price = this.saleList.get(i).get("price").toString();
            tmpStr = name;
            lbc = new ThreeEqualLineClass();
            lbc.setValue(0, tmpStr);
            tmpStr = quantity;
            lbc.setValue(1, tmpStr);
            tmpStr = price;
            lbc.setValue(2, tmpStr);
            lbc.setNewLineAtTheEnd(true);
            this.m_lineList.add(lbc);

        }

        tmpStr = "";
        lbc = new OneThirdAndTwoThirdLineClass();
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_val_separator);
        lbc.setValue(1, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_total);
        lbc = new ThreeEqualLineClass();
        lbc.setValue(0, tmpStr);
        tmpStr = "";
        lbc.setValue(1, tmpStr);
        lbc.setTruncateLine(1, false);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(1);
        this.m_hashInput.put("amountbase", strRefObj);
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("offline", strRefObj);
        this.set_offline("");
        final SignatureDataLineClass slc = new SignatureDataLineClass();
        slc.setSignatureLineStatus(SIGNATURE_LINE_STATUS.NO_SIGN);
        this.m_lineList.add(slc);
        this.m_hashInput.put("signaturelineclass", slc);
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("approvalcode", strRefObj);
        this.set_approvlcode("");
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("app", strRefObj);
        this.set_app("");
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("aid", strRefObj);
        this.set_aid("");
        tmpStr = "";
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        strRefObj = lbc.m_inputValList.get(0);
        this.m_hashInput.put("tc", strRefObj);
        this.set_tc("");
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_tail_text);
        lbc = new FullLineFullWordClass();
        lbc.setValue(0, tmpStr);
        lbc.setTruncateLine(0, false);
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
        lbc = new BarCodeDataLineClass();
        this.m_lineList.add(lbc);
        this.m_hashInput.put("barcodedatalineclass", lbc);
        this.addLineFeed();
        this.addFeedPaperLine(16);
    }
    
    private byte[] getPrintBytes_PrintDrawableFromFlashWithDoubleUnderline(final int idx) {
        final List<Byte> dataList = new ArrayList<Byte>();
        byte[] buf = this.getPrintBytes_PrintDrawableFromFlash(idx);
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
        buf = this.getPrintBytes_SignatureDoubleUnderlineAndCardHolderSignText();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
        final String tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_val_separator);
        final LineBaseClass lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        buf = lbc.getBytes();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
        dataList.add(BP80_PrintTemplateClass.creturn);
        buf = Bytes.toArray((Collection)dataList);
        return buf;
    }
    
    private byte[] getPrintBytes_PrintDrawableFromFlash(final int idx) {
        if (idx != 0 && idx != 1) {
            Logger.w("BP80PrintTemplateClass", "getPrintBytes_PrintDrawableFromFlash, index error, idx: " + idx);
            return null;
        }
        List<Byte> dataList = null;
        byte[] dataByteBuf = null;
        dataList = new ArrayList<Byte>();
        dataList.add((byte)27);
        switch (idx) {
            case 0: {
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_PRINTLOGO_FROM_FLASH0)));
                break;
            }
            case 1: {
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_PRINTLOGO_FROM_FLASH1)));
                break;
            }
        }
        dataByteBuf = Bytes.toArray((Collection)dataList);
        return dataByteBuf;
    }
    
    private void addPrintDrawableFromFlash(final int idx) {
        final byte[] dataByteBuf = this.getPrintBytes_PrintDrawableFromFlash(idx);
        if (dataByteBuf == null) {
            Logger.w("BP80PrintTemplateClass", "addPrintDrawableFromFlash, dataByteBuf is NULL");
            return;
        }
        this.m_lineList.add(dataByteBuf);
    }
    
    private byte[] getPrintBytes_DeclineMessage() {
        final String msg = this.m_context.getResources().getString(R.string.transaction_printreceipt_declined_req);
        return this.getPrintBytes_SigningMessage(msg);
    }
    
    private byte[] getPrintBytes_CancelMessage() {
        final String msg = this.m_context.getResources().getString(R.string.transaction_printreceipt_canceled_req);
        return this.getPrintBytes_SigningMessage(msg);
    }
    
    private byte[] getPrintBytes_NoSignatureRequiredMessage() {
        final String msg = this.m_context.getResources().getString(R.string.transaction_printreceipt_nosign_req);
        return this.getPrintBytes_SigningMessage(msg);
    }
    
    private byte[] getPrintBytes_SignatureDoubleUnderlineAndCardHolderSignText() {
        final List<Byte> dataList = new ArrayList<Byte>();
        byte[] buf = this.getPrintBytes_SignatureDoubleUnderline();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
        String tmpStr = "";
        LineBaseClass lbc = null;
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_cardholdersign);
        lbc = new FullLineClass();
        lbc.setGravity(0, 17);
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        buf = lbc.getBytes();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
        buf = Bytes.toArray((Collection)dataList);
        return buf;
    }
    
    private byte[] getPrintBytes_SignatureDoubleUnderline() {
        final List<Byte> dataList = new ArrayList<Byte>();
        byte[] buf = this.getPrintBytes_SignatureUnderline();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
        buf = Bytes.toArray((Collection)dataList);
        return buf;
    }
    
    private byte[] getPrintBytes_SignatureUnderline() {
        String tmpStr = "";
        LineBaseClass lbc = null;
        tmpStr = "";
        for (int i = 0; i < 20; ++i) {
            tmpStr += "-";
        }
        lbc = new FullLineClass();
        lbc.setGravity(0, 17);
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        return lbc.getBytes();
    }
    
    private byte[] getPrintBytes_Separator() {
        String tmpStr = "";
        LineBaseClass lbc = null;
        tmpStr = this.m_context.getResources().getString(R.string.transaction_printreceipt_val_separator);
        lbc = new FullLineClass();
        lbc.setValue(0, tmpStr);
        lbc.setNewLineAtTheEnd(true);
        return lbc.getBytes();
    }
    
    private byte[] getPrintBytes_SigningMessage(final String msg) {
        List<Byte> dataList = null;
        dataList = new ArrayList<Byte>();
        byte[] tmpBuf = this.getPrintBytes_BoldOn();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpBuf)));
        tmpBuf = this.getPrintBytes_Separator();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpBuf)));
        LineBaseClass lbc = null;
        lbc = new FullLineClass();
        lbc.setGravity(0, 17);
        lbc.setValue(0, msg);
        lbc.setNewLineAtTheEnd(true);
        tmpBuf = lbc.getBytes();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpBuf)));
        tmpBuf = this.getPrintBytes_Separator();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpBuf)));
        tmpBuf = this.getPrintBytes_BoldOff();
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpBuf)));
        final byte[] dataByteBuf = Bytes.toArray((Collection)dataList);
        return dataByteBuf;
    }
    
    private void addSigningMessage(final String msg) {
        final byte[] dataByteBuf = this.getPrintBytes_SigningMessage(msg);
        this.m_lineList.add(dataByteBuf);
    }
    
    private byte[] getPrintBytes_BoldOn() {
        List<Byte> dataList = null;
        byte[] dataByteBuf = null;
        dataList = new ArrayList<Byte>();
        dataList.add((byte)27);
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_BOLD_ON)));
        dataList.add(BP80_PrintTemplateClass.creturn);
        dataByteBuf = Bytes.toArray((Collection)dataList);
        return dataByteBuf;
    }
    
    private void addBoldOn() {
        final byte[] dataByteBuf = this.getPrintBytes_BoldOn();
        this.m_lineList.add(dataByteBuf);
    }
    
    private byte[] getPrintBytes_BoldOff() {
        List<Byte> dataList = null;
        byte[] dataByteBuf = null;
        dataList = new ArrayList<Byte>();
        dataList.add((byte)27);
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_BOLD_OFF)));
        dataList.add(BP80_PrintTemplateClass.creturn);
        dataByteBuf = Bytes.toArray((Collection)dataList);
        return dataByteBuf;
    }
    
    private void addBoldOff() {
        final byte[] dataByteBuf = this.getPrintBytes_BoldOff();
        this.m_lineList.add(dataByteBuf);
    }
    
    private byte[] getPrintBytes_FeedPaperLine(final int n_lines) {
        List<Byte> dataList = null;
        byte[] dataByteBuf = null;
        dataList = new ArrayList<Byte>();
        dataList.add((byte)27);
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_FEEDPAPER)));
        final byte val = (byte)(n_lines & 0xFF);
        dataList.add(val);
        dataList.add(BP80_PrintTemplateClass.creturn);
        dataByteBuf = Bytes.toArray((Collection)dataList);
        return dataByteBuf;
    }
    
    private void addFeedPaperLine(final int n_lines) {
        final byte[] dataByteBuf = this.getPrintBytes_FeedPaperLine(n_lines);
        this.m_lineList.add(dataByteBuf);
    }
    
    private void addResetPrinter() {
        List<Byte> dataList = null;
        byte[] dataByteBuf = null;
        dataList = new ArrayList<Byte>();
        dataList.add((byte)27);
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_RESETPRINTER)));
        dataList.add(BP80_PrintTemplateClass.creturn);
        dataByteBuf = Bytes.toArray((Collection)dataList);
        this.m_lineList.add(dataByteBuf);
    }
    
    private void addLineFeed() {
        final LineBaseClass lbc = new FullLineClass();
        lbc.setNewLineAtTheEnd(true);
        this.m_lineList.add(lbc);
    }
    
    public void set_offline() {
        final String msg = "(OFFLINE)";
        this.set_offline(msg);
    }
    
    public void set_offline(String msg) {
        final String key = "offline";
        final String strPrefix = "";
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_tc(String msg) {
        final String key = "tc";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_tc_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_aid(String msg) {
        final String key = "aid";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_aid_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_app(String msg) {
        final String key = "app";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_app_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_approvlcode(String msg) {
        final String key = "approvalcode";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_approvalcode_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_amount_tip(final String msg) {
        final String key = "amounttip";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_amount_total(final String msg) {
        final String key = "amounttotal";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_amount_base(final String msg) {
        final String key = "amountbase";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_ecrref(String msg) {
        final String key = "ecrref";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_errref_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_tracenumber(String msg) {
        final String key = "tracenumber";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_trace_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_rrn(String msg) {
        final String key = "rrn";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_rrn_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_batchnumber(String msg) {
        final String key = "batchnumber";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_batch_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_datetime(final String msg) {
        final String key = "datetime";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_txBig(final String msg) {
        final String key = "txBig";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_expiry(String msg) {
        final String key = "expiry";
        final String strPrefix = this.m_context.getResources().getString(R.string.transaction_printreceipt_expiry_with_colon);
        msg = strPrefix + msg;
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_cardtype(final String msg) {
        final String key = "cardtype";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_cardname(final String msg) {
        final String key = "cardname";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_cardnumber(final String msg) {
        final String key = "cardnumber";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_tid(final String msg) {
        final String key = "tid";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public void set_mid(final String msg) {
        final String key = "mid";
        this.set_valueLineBaseClass(key, msg);
    }
    
    public SIGNATURE_LINE_STATUS get_signatureLinesClassStatus() {
        final String key = "signaturelineclass";
        final Object obj = this.m_hashInput.get(key);
        if (obj != null) {
            final SignatureDataLineClass slc = (SignatureDataLineClass)obj;
            return slc.getSignatureLineStatus();
        }
        Logger.w("BP80PrintTemplateClass", "get_signatureLinesClassStatus, SignatureLineClass is NULL");
        return SIGNATURE_LINE_STATUS.EMPTY;
    }
    
    public void set_signatureLinesClassStatus(final SIGNATURE_LINE_STATUS signLineStatus) {
        final String key = "signaturelineclass";
        final Object obj = this.m_hashInput.get(key);
        if (obj != null) {
            final SignatureDataLineClass slc = (SignatureDataLineClass)obj;
            slc.setSignatureLineStatus(signLineStatus);
        }
        else {
            Logger.w("BP80PrintTemplateClass", "set_signatureLinesClassStatus, SignatureLineClass is NULL");
        }
    }
    
    public void set_signatureLinesClassData(final byte[] buf) {
        final String key = "signaturelineclass";
        final Object obj = this.m_hashInput.get(key);
        if (obj != null) {
            final SignatureDataLineClass slc = (SignatureDataLineClass)obj;
            slc.set_signatureLineData(buf);
        }
        else {
            Logger.w("BP80PrintTemplateClass", "set_signatureLinesClassStatus, SignatureLineClass is NULL");
        }
    }
    
    public void set_BarCodeDataLineClassData(final String strNumbers) {
        final String key = "barcodedatalineclass";
        final Object obj = this.m_hashInput.get(key);
        if (obj != null) {
            final BarCodeDataLineClass barcodelc = (BarCodeDataLineClass)obj;
            barcodelc.setBarCodeValues(strNumbers);
        }
        else {
            Logger.w("BP80PrintTemplateClass", "set_BarCodeDataLineClassData, set_BarCodeDataLineClass is NULL");
        }
    }
    
    private void set_valueLineBaseClass(final String key, final String msg) {
        final Object obj = this.m_hashInput.get(key);
        if (obj != null && obj instanceof StringReferenceObject) {
            final StringReferenceObject strRefObj = (StringReferenceObject)obj;
            strRefObj.m_inputVal = msg;
        }
        else if (obj == null) {
            Logger.w("BP80PrintTemplateClass", "set_valueLineBaseClass, obj is NULL");
        }
        else {
            Logger.w("BP80PrintTemplateClass", "set_valueLineBaseClass, obj is not StringReferenceObject, obj type: " + obj.toString());
        }
    }
    
    public byte[] getTemplateBytes() {
        final List<Byte> byteList = new ArrayList<Byte>();
        for (int i = 0; i < this.m_lineList.size(); ++i) {
            final Object obj = this.m_lineList.get(i);
            if (obj instanceof LineBaseClass) {
                final LineBaseClass lbc = (LineBaseClass)obj;
                if (lbc != null) {
                    final byte[] tmpBuf = lbc.getBytes();
                    if (tmpBuf != null) {
                        byteList.addAll(Arrays.asList(ArrayUtils.toObject(tmpBuf)));
                    }
                }
            }
            else if (obj instanceof byte[]) {
                final byte[] byteBuf = (byte[])obj;
                byteList.addAll(Arrays.asList(ArrayUtils.toObject(byteBuf)));
            }
        }
        final byte[] buf = Bytes.toArray((Collection)byteList);
        return buf;
    }
    
    static {
        BP80_PrintTemplateClass.creturn = (byte)"\n".toCharArray()[0];
    }
    
    public enum SIGNATURE_LINE_STATUS
    {
        EMPTY(0), 
        NO_SIGN(1), 
        SIGN(2), 
        CANCEL(3), 
        DECLINE(4);
        
        private final int intValue;
        
        private SIGNATURE_LINE_STATUS(final int value) {
            this.intValue = value;
        }
        
        public int toInt() {
            return this.intValue;
        }
    }
    
    private class StringReferenceObject
    {
        public String m_inputVal;
        
        public StringReferenceObject() {
            this.m_inputVal = "";
        }
        
        public StringReferenceObject(final StringReferenceObject strRefObj) {
            if (strRefObj == null) {
                this.m_inputVal = "";
                Logger.w("BP80PrintTemplateClass", "StringReferenceObject, strRefObj is NULL");
            }
            else {
                this.m_inputVal = strRefObj.m_inputVal;
            }
        }
    }
    
    private class LineBaseClass
    {
        protected boolean m_bNewLineAtTheEnd;
        protected int[] m_max_pixelList;
        protected int[] m_gravityList;
        protected List<StringReferenceObject> m_inputValList;
        protected boolean[] m_bTruncateLine;
        protected String m_actualVal;
        
        public LineBaseClass() {
            this.initBase();
        }
        
        private void initBase() {
            this.m_bNewLineAtTheEnd = false;
            this.m_max_pixelList = null;
            this.m_gravityList = null;
            this.m_inputValList = null;
            this.m_bTruncateLine = null;
            this.m_actualVal = null;
        }
        
        public byte[] getBytes() {
            this.setActualValue();
            byte[] tmpBuf = null;
            if (this.m_actualVal != null) {
                try {
                    tmpBuf = this.m_actualVal.getBytes("Big5");
                }
                catch (UnsupportedEncodingException uee) {
                    Logger.w("BP80PrintTemplateClass", "getBytes, unsupportedEncodingException uee: " + uee.toString());
                    tmpBuf = null;
                }
            }
            byte[] resultBuf = null;
            if (this.m_bNewLineAtTheEnd) {
                int len = 1;
                if (tmpBuf != null) {
                    len += tmpBuf.length;
                }
                resultBuf = new byte[len];
                if (tmpBuf != null) {
                    System.arraycopy(tmpBuf, 0, resultBuf, 0, tmpBuf.length);
                }
                resultBuf[len - 1] = 10;
            }
            else {
                resultBuf = tmpBuf;
            }
            return resultBuf;
        }
        
        public void setValue(final int idx, final String val, final int gravity) {
            final StringReferenceObject strRefObj = this.m_inputValList.get(idx);
            strRefObj.m_inputVal = val;
            this.m_gravityList[idx] = gravity;
        }
        
        public void setValue(final int idx, final String val) {
            final StringReferenceObject strRefObj = this.m_inputValList.get(idx);
            strRefObj.m_inputVal = val;
        }
        
        public void setGravity(final int idx, final int gravity) {
            this.m_gravityList[idx] = gravity;
        }
        
        private int calculateRemainPixels(final String x, final int max_pixels) {
            final int CJKPixel = 16;
            final int englishPixel = 12;
            final int countTotal = x.length();
            final FontHelper instFontHelper = FontHelper.getInstance();
            final int countCJK = instFontHelper.countCJK(x);
            final int countEnglish = countTotal - countCJK;
            final int pixels = CJKPixel * countCJK + englishPixel * countEnglish;
            final int remainPixels = max_pixels - pixels;
            return remainPixels;
        }
        
        private String padSpace(final String x, final int gravity, final int max_element, final boolean bPadEnd) {
            String result = x;
            if (result == null) {
                result = "";
            }
            final int remainPixels = this.calculateRemainPixels(result, max_element);
            final int englishPixel = 12;
            final int countPaddingSpace = (int)Math.floor(remainPixels / englishPixel);
            switch (gravity) {
                case 3: {
                    if (bPadEnd) {
                        for (int i = 0; i < countPaddingSpace; ++i) {
                            result += " ";
                        }
                        break;
                    }
                    break;
                }
                case 17: {
                    for (int countHalfPaddingSpace = (int)Math.floor(countPaddingSpace / 2.0), j = 0; j < countHalfPaddingSpace; ++j) {
                        result = " " + result;
                        if (bPadEnd) {
                            result += " ";
                        }
                    }
                    break;
                }
                case 5: {
                    for (int i = 0; i < countPaddingSpace; ++i) {
                        result = " " + result;
                    }
                    break;
                }
            }
            return result;
        }
        
        protected void setActualValue() {
            List<StringReferenceObject> inputValList = this.m_inputValList;
            final int[] max_pixelList = this.m_max_pixelList;
            if (this instanceof FullLineFullWordClass) {
                final List<StringReferenceObject> wholewordValList = new ArrayList<StringReferenceObject>();
                for (int i = 0; i < inputValList.size(); ++i) {
                    final StringReferenceObject obj = new StringReferenceObject();
                    wholewordValList.add(obj);
                }
                for (int i = 0; i < inputValList.size(); ++i) {
                    final StringReferenceObject orgObj = inputValList.get(i);
                    final String[] words = orgObj.m_inputVal.split(" ");
                    final StringReferenceObject wholewordValObj = wholewordValList.get(i);
                    String concatLineStr = "";
                    for (int j = 0; j < words.length; ++j) {
                        final String word = words[j].trim();
                        if (word != null && !word.equals("")) {
                            String insertChar = "";
                            if (j > 0) {
                                if (word.length() > max_pixelList[i]) {
                                    insertChar = " ";
                                    concatLineStr = concatLineStr + insertChar + word;
                                }
                                else {
                                    final String tmpConcatLineStr = concatLineStr + " " + word;
                                    final int remainPixels = this.calculateRemainPixels(tmpConcatLineStr, max_pixelList[i]);
                                    if (remainPixels < 0) {
                                        insertChar = "\n";
                                        concatLineStr = word;
                                    }
                                    else {
                                        insertChar = " ";
                                        concatLineStr = concatLineStr + insertChar + word;
                                    }
                                }
                            }
                            final StringBuilder sb = new StringBuilder();
                            final StringReferenceObject stringReferenceObject = wholewordValObj;
                            stringReferenceObject.m_inputVal = sb.append(stringReferenceObject.m_inputVal).append(insertChar).append(word).toString();
                        }
                    }
                }
                inputValList = wholewordValList;
            }
            this.setActualValue(inputValList);
        }
        
        protected void setActualValue(final List<StringReferenceObject> inpuValList) {
            if (inpuValList == null) {
                Logger.w("BP80PrintTemplateClass", "setActualValues, inpuValList is NULL");
                return;
            }
            String conCatVal = "";
            for (int i = 0; i < inpuValList.size(); ++i) {
                final StringReferenceObject strRefObj = inpuValList.get(i);
                String x = strRefObj.m_inputVal;
                final int max_pixels = this.m_max_pixelList[i];
                final int gravity = this.m_gravityList[i];
                final boolean bTruncateLine = this.m_bTruncateLine[i];
                final boolean bPadEnd = i != inpuValList.size() - 1;
                if (x == null) {
                    x = "";
                }
                int remainPixels = this.calculateRemainPixels(x, max_pixels);
                if (bTruncateLine) {
                    while (remainPixels < 0) {
                        x = x.substring(0, x.length() - 1);
                        remainPixels = this.calculateRemainPixels(x, max_pixels);
                    }
                }
                x = this.padSpace(x, gravity, max_pixels, bPadEnd);
                conCatVal += x;
            }
            this.m_actualVal = conCatVal;
        }
        
        public void setTruncateLine(final int idx, final boolean flag) {
            this.m_bTruncateLine[idx] = flag;
        }
        
        public void setNewLineAtTheEnd(final boolean flag) {
            this.m_bNewLineAtTheEnd = flag;
        }
    }
    
    private class BarCodeDataLineClass extends LineBaseClass
    {
        private byte[] m_bufBarCode;
        
        public BarCodeDataLineClass() {
            this.m_bufBarCode = null;
        }
        
        public void setBarCodeValues(final String strNumber) {
            if (strNumber == null) {
                Logger.w("BP80PrintTemplateClass", "setBarCodeValues, strNumber is null");
            }
            this.m_bufBarCode = strNumber.getBytes();
        }
        
        @Override
        public byte[] getBytes() {
            byte[] buf = null;
            if (this.m_bufBarCode != null) {
                if (this.m_bufBarCode.length > 13) {
                    Logger.w("BP80PrintTemplateClass", "BarCodeDataLineClass, m_bufBarCode.length>13, val: " + this.m_bufBarCode.length);
                    return buf;
                }
                List<Byte> dataList = null;
                dataList = new ArrayList<Byte>();
                final LineBaseClass lbc = new FullLineClass();
                lbc.setNewLineAtTheEnd(true);
                final byte[] bufLineFeed = lbc.getBytes();
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(bufLineFeed)));
                dataList.add((byte)27);
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_PRINT_BARCODE)));
                final byte lenByte = (byte)(this.m_bufBarCode.length & 0xFF);
                dataList.add(lenByte);
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(this.m_bufBarCode)));
                buf = Bytes.toArray((Collection)dataList);
            }
            return buf;
        }
    }
    
    private class SignatureDataLineClass extends LineBaseClass
    {
        private SIGNATURE_LINE_STATUS m_signLineStatus;
        private byte[] m_bufEmpty;
        private byte[] m_bufNoSign;
        private byte[] m_bufSign;
        private byte[] m_bufCancel;
        private byte[] m_bufDecline;
        
        public SignatureDataLineClass() {
            this.m_signLineStatus = SIGNATURE_LINE_STATUS.EMPTY;
            this.m_bufEmpty = BP80_PrintTemplateClass.this.getPrintBytes_FeedPaperLine(2);
            this.m_bufNoSign = BP80_PrintTemplateClass.this.getPrintBytes_NoSignatureRequiredMessage();
            this.m_bufSign = BP80_PrintTemplateClass.this.getPrintBytes_SignatureDoubleUnderlineAndCardHolderSignText();
            this.m_bufCancel = BP80_PrintTemplateClass.this.getPrintBytes_CancelMessage();
            this.m_bufDecline = BP80_PrintTemplateClass.this.getPrintBytes_DeclineMessage();
        }
        
        public void set_signatureLineData(final byte[] buf) {
            final List<Byte> dataList = new ArrayList<Byte>();
            if (buf == null) {
                byte[] tmp = BP80_PrintTemplateClass.this.getPrintBytes_FeedPaperLine(2);
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmp)));
                tmp = BP80_PrintTemplateClass.this.getPrintBytes_SignatureDoubleUnderlineAndCardHolderSignText();
                dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmp)));
                this.m_bufSign = Bytes.toArray((Collection)dataList);
                return;
            }
            dataList.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
            final byte[] bufUnderLines = BP80_PrintTemplateClass.this.getPrintBytes_SignatureDoubleUnderlineAndCardHolderSignText();
            dataList.addAll(Arrays.asList(ArrayUtils.toObject(bufUnderLines)));
            this.m_bufSign = Bytes.toArray((Collection)dataList);
            this.setSignatureLineStatus(SIGNATURE_LINE_STATUS.SIGN);
        }
        
        public SIGNATURE_LINE_STATUS getSignatureLineStatus() {
            return this.m_signLineStatus;
        }
        
        public void setSignatureLineStatus(final SIGNATURE_LINE_STATUS signLineStatus) {
            this.m_signLineStatus = signLineStatus;
        }
        
        @Override
        public byte[] getBytes() {
            byte[] buf = null;
            switch (this.m_signLineStatus) {
                case EMPTY: {
                    buf = this.m_bufEmpty;
                    break;
                }
                case NO_SIGN: {
                    buf = this.m_bufNoSign;
                    break;
                }
                case SIGN: {
                    buf = this.m_bufSign;
                    break;
                }
                case CANCEL: {
                    buf = this.m_bufCancel;
                    break;
                }
                case DECLINE: {
                    buf = this.m_bufDecline;
                    break;
                }
            }
            return buf;
        }
    }
    
    @Deprecated
    private class SignatureLineClass extends LineBaseClass
    {
        private SIGNATURE_LINE_STATUS m_signLineStatus;
        private byte[] m_bufEmpty;
        private byte[] m_bufNoSign;
        private byte[] m_bufSign;
        private byte[] m_bufCancel;
        private byte[] m_bufDecline;
        
        public SignatureLineClass() {
            this.m_signLineStatus = SIGNATURE_LINE_STATUS.EMPTY;
            this.m_bufEmpty = BP80_PrintTemplateClass.this.getPrintBytes_FeedPaperLine(2);
            this.m_bufNoSign = BP80_PrintTemplateClass.this.getPrintBytes_NoSignatureRequiredMessage();
            this.m_bufSign = BP80_PrintTemplateClass.this.getPrintBytes_PrintDrawableFromFlashWithDoubleUnderline(9);
            this.m_bufCancel = BP80_PrintTemplateClass.this.getPrintBytes_CancelMessage();
            this.m_bufDecline = BP80_PrintTemplateClass.this.getPrintBytes_DeclineMessage();
        }
        
        public SIGNATURE_LINE_STATUS getSignatureLineStatus() {
            return this.m_signLineStatus;
        }
        
        public void setSignatureLineStatus(final SIGNATURE_LINE_STATUS signLineStatus) {
            this.m_signLineStatus = signLineStatus;
        }
        
        @Override
        public byte[] getBytes() {
            byte[] buf = null;
            switch (this.m_signLineStatus) {
                case EMPTY: {
                    buf = this.m_bufEmpty;
                    break;
                }
                case NO_SIGN: {
                    buf = this.m_bufNoSign;
                    break;
                }
                case SIGN: {
                    buf = this.m_bufSign;
                    break;
                }
                case CANCEL: {
                    buf = this.m_bufCancel;
                    break;
                }
                case DECLINE: {
                    buf = this.m_bufDecline;
                    break;
                }
            }
            return buf;
        }
    }
    
    private class FullLineFullWordClass extends LineBaseClass
    {
        public FullLineFullWordClass() {
            (this.m_max_pixelList = new int[1])[0] = 384;
            (this.m_gravityList = new int[1])[0] = 3;
            this.m_inputValList = new ArrayList<StringReferenceObject>();
            final StringReferenceObject obj = new StringReferenceObject();
            this.m_inputValList.add(obj);
            (this.m_bTruncateLine = new boolean[1])[0] = true;
        }
    }
    
    private class FullLineClass extends LineBaseClass
    {
        public FullLineClass() {
            (this.m_max_pixelList = new int[1])[0] = 384;
            (this.m_gravityList = new int[1])[0] = 3;
            this.m_inputValList = new ArrayList<StringReferenceObject>();
            final StringReferenceObject obj = new StringReferenceObject();
            this.m_inputValList.add(obj);
            (this.m_bTruncateLine = new boolean[1])[0] = true;
        }
    }
    
    private class HalfLineClass extends LineBaseClass
    {
        public HalfLineClass() {
            (this.m_max_pixelList = new int[2])[0] = (this.m_max_pixelList[1] = (int)Math.floor(192.0));
            (this.m_gravityList = new int[2])[0] = 3;
            this.m_gravityList[1] = 5;
            this.m_inputValList = new ArrayList<StringReferenceObject>();
            for (int i = 0; i < 2; ++i) {
                final StringReferenceObject obj = new StringReferenceObject();
                this.m_inputValList.add(obj);
            }
            (this.m_bTruncateLine = new boolean[2])[0] = (this.m_bTruncateLine[1] = true);
        }
    }
    
    private class t240v144LineClass extends LineBaseClass
    {
        public t240v144LineClass() {
            this.m_max_pixelList = new int[2];
            final int pixels_secondcol = 144;
            this.m_max_pixelList[0] = 384 - pixels_secondcol;
            this.m_max_pixelList[1] = pixels_secondcol;
            (this.m_gravityList = new int[2])[0] = 3;
            this.m_gravityList[1] = 5;
            this.m_inputValList = new ArrayList<StringReferenceObject>();
            for (int i = 0; i < 2; ++i) {
                final StringReferenceObject obj = new StringReferenceObject();
                this.m_inputValList.add(obj);
            }
            (this.m_bTruncateLine = new boolean[2])[0] = (this.m_bTruncateLine[1] = true);
        }
    }
    
    private class OneThirdAndTwoThirdLineClass extends LineBaseClass
    {
        public OneThirdAndTwoThirdLineClass() {
            (this.m_max_pixelList = new int[2])[0] = (int)Math.floor(128.0);
            this.m_max_pixelList[1] = (int)Math.floor(256.0);
            (this.m_gravityList = new int[2])[0] = 3;
            this.m_gravityList[1] = 5;
            this.m_inputValList = new ArrayList<StringReferenceObject>();
            for (int i = 0; i < 2; ++i) {
                final StringReferenceObject obj = new StringReferenceObject();
                this.m_inputValList.add(obj);
            }
            (this.m_bTruncateLine = new boolean[2])[0] = (this.m_bTruncateLine[1] = true);
        }
    }
    private class ThreeEqualLineClass extends LineBaseClass
    {
        public ThreeEqualLineClass() {
            this.m_max_pixelList = new int[3];
            this.m_max_pixelList[0] = 164;
            this.m_max_pixelList[1] = 80;
            this.m_max_pixelList[2] = 140;
            (this.m_gravityList = new int[3])[0] = 3;
            this.m_gravityList[1] = 5;
            this.m_inputValList = new ArrayList<StringReferenceObject>();
            for (int i = 0; i < 3; ++i) {
                final StringReferenceObject obj = new StringReferenceObject();
                this.m_inputValList.add(obj);
            }
            (this.m_bTruncateLine = new boolean[3])[0] = (this.m_bTruncateLine[1] = true);
        }
    }
}
