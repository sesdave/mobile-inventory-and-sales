package com.spectratech.sp530demo.sp530class;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.common.primitives.Bytes;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.TmsHelper;
import com.spectratech.sp530demo.constant.FullEmvEnum;
import com.spectratech.sp530demo.constant.FullEmvEnum.TRANSACTIONSTATUS;
import com.spectratech.sp530demo.data.Data_S3INS_response;
import com.spectratech.sp530demo.data.Data_SP530_tms_module;
import com.spectratech.sp530demo.data.Data_tmsTrans2DeviceObject;
import com.spectratech.sp530demo.data.Data_tms_header;
import com.spectratech.sp530demo.data.Data_tms_header_module;
import com.spectratech.spectratechlib_ftp.data.Data_ftpdlObject;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * SP530_bt_S3INS_TMSDL - SP530 Main application
 * - with feature TMS download
 */
public class SP530_bt_S3INS_TMSDL extends SP530_bt_S3INS {

    private static final String m_className="SP530_bt_S3INS_TMSDL";

    public enum TMS_DL_STATUS {
        STATUS_UNKNOWN(-1),

        STATUS_GET_TMSLIST(2000),
        STATUS_GET_TMSLIST_FAIL(2001),
        STATUS_NUM_TMSDL(2002),
        STATUS_NUM_TMSDL_ERROR(2003),
        STATUS_CHECK_TMSDL_SKIP(2004),
        STATUS_SENDHEADER_INFO(2005),
        STATUS_SENDHEADER_INFO_ERROR(2006),
        STATUS_TMSDL(2007),
        STATUS_TMSDL_ERROR(2008),
        STATUS_TMSDL_RESULT(2009);

        private final int intValue;
        private TMS_DL_STATUS(int value) {
            intValue = value;
        }
        public int toInt() {
            return intValue;
        }
    }

    public static final byte S3INS_NUM_TMSDL=(byte)0x30;
    public static final byte S3INS_TMSDL=(byte)0x31;
    public static final byte S3INS_TMSDLRESULT=(byte)0x32;
    public static final byte S3INS_APPINFO=(byte)0x33;

    private static final int MSG_HANDLER_LOOP=1107;

    private static final int size_block=1016;

    // point to same set
    private LinkedHashMap<String, Data_tmsTrans2DeviceObject> m_dataTmsTrans2DeviceObjectHash;
    private ArrayList<Data_tmsTrans2DeviceObject> m_dataTmsTrans2DeviceObjectList;

    private ArrayList<Data_ftpdlObject> m_dataFtpdlObjectList;
    private String m_filePath;

    private int m_n_tms;
    private int m_n_tms_skip;
    private int m_idxCurrentTms;
    private int m_idxCurrentHeaderDataBlock;
    private int m_idxCurrentDataBlock;
    private int m_currentTotalDataBlock;

    private InputStream m_currentInputStream;
    private byte[] m_currentDataBlock;
    private byte[] m_nextDataBlock;

    private byte m_commandCode;
    private Callback<Object> m_cb_finish_num_tmsdl;
    private Callback<Object> m_cb_finish_sendtmsheaderinfo;
    private Callback<Object> m_cb_finish_tmsdl;

    private Callback<Object> m_cb_finish_tmsdl_result;

    private Callback<Object> m_cb_finish_get_tmsdl;

    private Callback<Object> m_cb_progress;
    private Callback<Object> m_cb_finish;

    private Handler m_handler;

    private TMS_DL_STATUS m_status_dl;

    public SP530_bt_S3INS_TMSDL(Context context, BluetoothSocketClass btSocketClass) {
        super(context, btSocketClass);

        Logger.i(m_className, "SP530_bt_S3INS_TMSDL constructor call");

        m_status_dl=TMS_DL_STATUS.STATUS_UNKNOWN;

        m_dataTmsTrans2DeviceObjectHash=null;
        m_dataTmsTrans2DeviceObjectList=null;

        m_dataFtpdlObjectList=null;
        m_filePath="";

        m_n_tms=0;
        m_n_tms_skip=0;
        m_idxCurrentTms=0;
        m_idxCurrentHeaderDataBlock=0;
        m_idxCurrentDataBlock=0;
        m_currentTotalDataBlock=0;

        m_currentInputStream=null;
        m_currentDataBlock=null;
        m_nextDataBlock=null;

        initHandler();

        initCallbacks();
        m_cb_progress=null;
        m_cb_finish=null;
    }

    private void initCallbacks() {
        m_cb_finish_num_tmsdl=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                if (!m_bCancel) {
                    Object obj = this.getParameter();

                    boolean bValidResult=isResponseResultValid(obj);

                    if (bValidResult) {
                        m_idxCurrentTms=0;
                        m_idxCurrentHeaderDataBlock=0;
                        m_idxCurrentDataBlock=0;
                        m_currentTotalDataBlock=0;

                        m_currentInputStream=null;
                        m_currentDataBlock=null;
                        m_nextDataBlock=null;

                        m_status_dl=TMS_DL_STATUS.STATUS_CHECK_TMSDL_SKIP;
                        m_handler.sendEmptyMessage(MSG_HANDLER_LOOP);
                    }
                    else {
                        m_status_dl = TMS_DL_STATUS.STATUS_NUM_TMSDL_ERROR;
                        finalCallback(obj);
                    }
                }
                else {
                    finalCallback_cancel();
                }
                return null;
            }
        };

        m_cb_finish_sendtmsheaderinfo=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                if (!m_bCancel) {
                    m_idxCurrentHeaderDataBlock++;

                    Object obj = this.getParameter();

                    boolean bValidResult=isResponseResultValid(obj);

                    if (bValidResult) {
                        m_status_dl = TMS_DL_STATUS.STATUS_TMSDL;
                        m_handler.sendEmptyMessage(MSG_HANDLER_LOOP);
                    }
                    else {
                        m_status_dl = TMS_DL_STATUS.STATUS_SENDHEADER_INFO_ERROR;
                        finalCallback(obj);
                    }
                }
                else {
                    finalCallback_cancel();
                }
                return null;
            }
        };

        m_cb_finish_tmsdl=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                if (!m_bCancel) {
                    m_idxCurrentDataBlock++;

                    Object obj = this.getParameter();

                    boolean bValidResult=isResponseResultValid(obj);

                    // update result
                    Data_tmsTrans2DeviceObject dataTmsTrans2DeviceObject=m_dataTmsTrans2DeviceObjectList.get(m_idxCurrentTms);
                    if (dataTmsTrans2DeviceObject==null) {
                        Logger.w(m_className, "m_cb_finish_tmsdl, dataTmsTrans2DeviceObject is null");
                    }
                    else {
                        dataTmsTrans2DeviceObject.m_bTransferSuccess = bValidResult;
                    }

                    if (bValidResult) {
                        if (m_nextDataBlock != null) {
                            m_handler.sendEmptyMessage(MSG_HANDLER_LOOP);
                        }
                        else {
                            goForNextTms(obj);
                        }
                    }
                    else {
                        m_status_dl = TMS_DL_STATUS.STATUS_TMSDL_ERROR;
                        finalCallback(obj);
                    }
                }
                else {
                    finalCallback_cancel();
                }
                return null;
            }
        };

        m_cb_finish_tmsdl_result=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                if (!m_bCancel) {
                    Object obj = this.getParameter();

                    boolean bValidResult=isResponseResultValid(obj);

                    if (bValidResult) {
                        String strMsg="SUCCESS";
                        publishProgress(strMsg);
                        finalCallback(obj);
                    }
                    else {
                        m_status_dl = TMS_DL_STATUS.STATUS_NUM_TMSDL_ERROR;
                        finalCallback(obj);
                    }
                }
                else {
                    finalCallback_cancel();
                }
                return null;
            }
        };

        m_cb_finish_get_tmsdl=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                if (!m_bCancel) {
                    Object obj = this.getParameter();

                    boolean bValidResult=isResponseResultValid(obj);

                    if (bValidResult) {
                        Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)obj;
                        byte[] buf=new byte [dataS3INSResponse.m_data.length-1];
                        System.arraycopy(dataS3INSResponse.m_data, 1, buf, 0, buf.length);

                        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();

//                        String strHex=instByteHexHelper.bytesArrayToHexString(buf);
//                        Logger.i(m_className, "m_cb_finish_get_tmsdl, strHex: "+strHex);
//                        String strText=new String(buf);
//                        Logger.i(m_className, "m_cb_finish_get_tmsdl, strText: "+strText);

                        TmsHelper instTmsHelper=TmsHelper.getInstance();
                        ArrayList<Data_SP530_tms_module> sp530TmsList=instTmsHelper.getSP530TmsModuleList(buf);
                        if (sp530TmsList==null) {
                            Logger.w(m_className, "m_cb_finish_get_tmsdl, sp530TmsList is null");
                        }
                        else if (sp530TmsList.size()==0) {
                            Logger.w(m_className, "m_cb_finish_get_tmsdl, sp530TmsList.size()==0");
                        }
                        else {
                            fetchSkipDataTmsTrans2DeviceObject(sp530TmsList);
                        }

                        m_status_dl=TMS_DL_STATUS.STATUS_NUM_TMSDL;

                        m_handler.sendEmptyMessage(MSG_HANDLER_LOOP);
                    }
                    else {
                        m_status_dl = TMS_DL_STATUS.STATUS_GET_TMSLIST_FAIL;
                        finalCallback(obj);
                    }
                }
                else {
                    finalCallback_cancel();
                }
                return null;
            }
        };
    }

    private void goForNextTms(Object obj) {
        m_idxCurrentTms++;
        if (m_idxCurrentTms < m_n_tms) {
            m_idxCurrentDataBlock = 0;
            m_currentTotalDataBlock = 0;
            m_status_dl=TMS_DL_STATUS.STATUS_CHECK_TMSDL_SKIP;
            m_handler.sendEmptyMessage(MSG_HANDLER_LOOP);
        } else {
            publishTransferSummary2Progress();
            finalCallback(obj);
        }
    }

    private String getSummaryText() {
        String strSummary="";
        if (m_dataTmsTrans2DeviceObjectList==null) {
            return strSummary;
        }
        for (int i=0; i<m_dataTmsTrans2DeviceObjectList.size(); i++) {
            Data_tmsTrans2DeviceObject x = m_dataTmsTrans2DeviceObjectList.get(i);
            String name = x.m_dataFtpdlObject.m_relative_pathlist;
            if (x.m_bFileNotExist) {
                strSummary += "NOT EXIST - " + name;
            }
            else if (x.m_bSkip) {
                strSummary += "SKIP - " + name;
            }
            else if (x.m_bTransferSuccess) {
                strSummary += "SUCCESS - " + name;
            }
            else {
                strSummary += "FAIL - " + name;
            }
            strSummary += "\n";
        }
        return strSummary;
    }

    private void publishTransferSummary2Progress() {
        String strSummary=getSummaryText();
        publishProgress(strSummary);
    }

    private void finalCallback_cancel() {
        String strCancel= ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        finalCallback_withText(strCancel);
    }
    private void finalCallback_withText(String strText) {
        publishProgress(strText);
        finalCallback(null);
    }
    private void finalCallback(Object obj) {
        Logger.i(m_className, "finalCallback call");
        if (m_cb_finish != null) {
            try {
                m_cb_finish.setParameter(obj);
                m_cb_finish.call();
            }
            catch (Exception ex) {
                Logger.w(m_className, "finalCallback, Exception ex: "+ex.toString());
            }
        }
    }

    public static String getTransStatusString(Object obj) {
        String result="";
        if (!(obj instanceof Data_S3INS_response)) {
            Logger.w(m_className, "getStatusString, obj is NOT instanceof Data_S3INS_response");
            return result;
        }
        Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)obj;
        if (dataS3INSResponse==null) {
            Logger.w(m_className, "getStatusString, dataS3INSResponse is null");
            return result;
        }
        result= FullEmvEnum.Status2String(dataS3INSResponse.m_statusTrans);
        return result;
    }

    public static byte[] getResponseCode(Object obj) {
        byte[] result=null;
        if (!(obj instanceof Data_S3INS_response)) {
            Logger.w(m_className, "getResponseCode, obj is NOT instanceof Data_S3INS_response");
            return result;
        }
        Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)obj;
        if (dataS3INSResponse==null) {
            Logger.w(m_className, "getResponseCode, dataS3INSResponse is null");
            return result;
        }
        if ( (dataS3INSResponse.m_data!=null)&&(dataS3INSResponse.m_data.length>0) ) {
            byte responseCode = dataS3INSResponse.m_data[0];
            result=new byte[] { responseCode };
        }
        return result;
    }

    public static boolean isResponseResultValid(Object obj) {
        boolean bResultValid=false;
        if (!(obj instanceof Data_S3INS_response)) {
            Logger.w(m_className, "isResponseResultValid, obj is NOT instanceof Data_S3INS_response");
            return bResultValid;
        }
        Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)obj;
        if (dataS3INSResponse==null) {
            Logger.w(m_className, "isResponseResultValid, dataS3INSResponse is null");
            return bResultValid;
        }
        TRANSACTIONSTATUS statusTrans=dataS3INSResponse.m_statusTrans;
        if (statusTrans==TRANSACTIONSTATUS.STATUS_FINISH) {
            if ( (dataS3INSResponse.m_data!=null)&&(dataS3INSResponse.m_data.length>0) ) {
                byte responseCode = dataS3INSResponse.m_data[0];
                if (responseCode==ApplicationProtocolConstant.S3RC_OK) {
                    bResultValid = true;
                }
            }
        }
        return bResultValid;
    }

    public static int getResponseCodeMessageValue(byte responseCode) {
        byte mask = (byte)((1<<7)^0xFF);
        int iVal=(responseCode&mask)&0xFF;
        return iVal;
    }

    private void initHandler() {
        m_handler=new Handler() {
            public void handleMessage(Message msg) {
                //Logger.i(m_className, "Handler message: "+msg.what);
                switch (msg.what) {
                    case MSG_HANDLER_LOOP: {
                        startProcessState();
                    }
                    break;
                }
            }
        };
    }

    private void fetchSkipDataTmsTrans2DeviceObject(ArrayList<Data_SP530_tms_module> sp530TmsList) {
        Logger.i(m_className, "fetchSkipDataTmsTrans2DeviceObject");
        if (sp530TmsList==null) {
            Logger.w(m_className, "fetchSkipDataTmsTrans2DeviceObject, sp530TmsList==null");
            return;
        }
        if (sp530TmsList.size()==0) {
            Logger.w(m_className, "fetchSkipDataTmsTrans2DeviceObject, sp530TmsList.size()==0");
            return;
        }
        TmsHelper instTmsHelper=TmsHelper.getInstance();
        for (int i=0; i<sp530TmsList.size(); i++) {
            Data_SP530_tms_module dataSP530TmsModule=sp530TmsList.get(i);
            String key=instTmsHelper.getHashKey(dataSP530TmsModule);
            Data_tmsTrans2DeviceObject x=m_dataTmsTrans2DeviceObjectHash.get(key);
            if (x!=null) {
                x.m_bSkip=true;
            }
            String strLog="fetchSkipDataTmsTrans2DeviceObject, key: "+key;
            if (x!=null) {
                strLog+=" SKIPPED";
            }
            Logger.i(m_className, strLog);
        }

        for (int i=0; i<m_dataTmsTrans2DeviceObjectList.size(); i++) {
            Data_tmsTrans2DeviceObject x=m_dataTmsTrans2DeviceObjectList.get(i);
            if (x.m_bSkip) {
                m_n_tms_skip++;
            }
        }
    }

    private void fetchDataTmsTrans2DeviceObject() {
        Logger.i(m_className, "fetchDataTmsTrans2DeviceObject");
        TmsHelper instTmsHelper=TmsHelper.getInstance();
        m_dataTmsTrans2DeviceObjectHash=new LinkedHashMap<>();
        m_dataTmsTrans2DeviceObjectList=new ArrayList<>();
        for (int i=0; i<m_dataFtpdlObjectList.size(); i++) {
            Data_ftpdlObject dataFtpdlObject=m_dataFtpdlObjectList.get(i);
            Data_tmsTrans2DeviceObject x=new Data_tmsTrans2DeviceObject(dataFtpdlObject);
            Data_tms_header dataTmsHeader=instTmsHelper.getHeaderObject(m_filePath, dataFtpdlObject.m_relative_pathlist);
            if (dataTmsHeader==null) {
                Logger.w(m_className, "fetchDataTmsTrans2DeviceObject, dataTmsHeader is null");
                x.m_bFileNotExist=true;
                x.m_bSkip=true;
            }
            else {
                x.m_dataTmsHeader = dataTmsHeader;
            }

            String key = x.getHashKey();

            Data_tmsTrans2DeviceObject objCurrent=m_dataTmsTrans2DeviceObjectHash.get(key);
            if (objCurrent!=null) {
                Logger.w(m_className, "fetchDataTmsTrans2DeviceObject, objCurrent EXIST with key: "+key);
                continue;
            }

            m_dataTmsTrans2DeviceObjectHash.put(key, x);
            m_dataTmsTrans2DeviceObjectList.add(x);
        }

        Logger.i(m_className, "fetchDataTmsTrans2DeviceObject, m_dataTmsTrans2DeviceObjectHash,m_dataTmsTrans2DeviceObjectList size: "+m_dataTmsTrans2DeviceObjectHash.size()+","+m_dataTmsTrans2DeviceObjectList.size());

        for(String key : m_dataTmsTrans2DeviceObjectHash.keySet()) {
            Data_tmsTrans2DeviceObject x=m_dataTmsTrans2DeviceObjectHash.get(key);
            if (x.m_dataTmsHeader!=null) {
                Data_tms_header tmsHeader=x.m_dataTmsHeader;
                Data_tms_header_module dataTmsHeaderModule = tmsHeader.m_moduleList.get(tmsHeader.m_moduleList.size() - 1);
                String line = dataTmsHeaderModule.m_moduleLine;
                Logger.i(m_className, "m_moduleList, key: " + key + ", line: " + line);
                dataTmsHeaderModule.printDebugInfo();
            }
        }
    }

    public void setCallback_progress(Callback<Object> cb_progress) {
        m_cb_progress=cb_progress;
    }

    public void setTmsDlData(ArrayList<Data_ftpdlObject> dataFtpdlObjectList, String filePath) {
        m_dataFtpdlObjectList=dataFtpdlObjectList;
        m_filePath=filePath;
        m_n_tms=m_dataFtpdlObjectList.size();
    }

    public void start(byte commandCode, byte[] dataBuf, Callback<Object> cb_finish) {
        Logger.i(m_className, "start");

        if ( (commandCode!=S3INS_NUM_TMSDL)&&(commandCode!=S3INS_TMSDLRESULT) ) {
            Logger.w(m_className, "start, commandCode unknown, val: "+(commandCode&0xFF));
            return;
        }

        if (commandCode!=S3INS_TMSDLRESULT) {
            if (m_dataFtpdlObjectList == null) {
                Logger.w(m_className, "start, m_dataFtpdlObjectList is null");
                return;
            }
            if (m_dataFtpdlObjectList.size() == 0) {
                Logger.w(m_className, "start, m_dataFtpdlObjectList.size()==0");
                return;
            }
        }

        m_commandCode=commandCode;

        m_status_dl=TMS_DL_STATUS.STATUS_UNKNOWN;
        switch (commandCode) {
            case S3INS_NUM_TMSDL: {
                m_status_dl=TMS_DL_STATUS.STATUS_GET_TMSLIST;
                fetchDataTmsTrans2DeviceObject();
            }
            break;
            case S3INS_TMSDLRESULT: {
                m_status_dl=TMS_DL_STATUS.STATUS_TMSDL_RESULT;
            }
            break;
        }

        m_cb_finish=cb_finish;

        m_idxCurrentTms=0;
        m_idxCurrentDataBlock=0;
        m_currentTotalDataBlock=0;

        startProcessState();
    }

    private void startProcessState() {
        Data_ftpdlObject dataFtpdlObj=null;
        String strName=null;
        switch (m_status_dl) {
            case STATUS_CHECK_TMSDL_SKIP:
            case STATUS_SENDHEADER_INFO:
            case STATUS_TMSDL: {
                dataFtpdlObj=m_dataFtpdlObjectList.get(m_idxCurrentTms);
                strName=dataFtpdlObj.m_relative_pathlist;
            }
        }

        switch (m_status_dl) {
            case STATUS_GET_TMSLIST: {
                Logger.i(m_className, "STATUS_GET_TMSLIST");
                byte[] dataBuf=null;

                String strProgress="Get TMS list . . . ";
                publishProgress(strProgress);

                super.start(S3INS_APPINFO, dataBuf, m_cb_finish_get_tmsdl);
            }
            break;

            case STATUS_NUM_TMSDL: {
                Logger.i(m_className, "STATUS_NUM_TMSDL");
                int val=m_n_tms-m_n_tms_skip;
                if (val<=0) {
                    finalCallback_withText("Skip files ("+m_n_tms_skip+"/"+m_n_tms+")");
                    break;
                }
                byte valByte=(byte)(val&0xFF);
                byte[] dataBuf=new byte[] { valByte };
                Logger.i(m_className, "STATUS_NUM_TMSDL, val: " + valByte + ", m_n_tms: " + m_n_tms + ", m_n_tms_skip: " + m_n_tms_skip);

                String strProgress="Number of TMS to be transferred: "+val+" (skip:"+m_n_tms_skip+")";
                publishProgress(strProgress);

                super.start(S3INS_NUM_TMSDL, dataBuf, m_cb_finish_num_tmsdl);
            }
            break;

            case STATUS_CHECK_TMSDL_SKIP: {
                Logger.i(m_className, "STATUS_CHECK_TMSDL_SKIP");

                Data_tmsTrans2DeviceObject dataTmsTrans2DeviceObject=m_dataTmsTrans2DeviceObjectList.get(m_idxCurrentTms);

                Logger.i(m_className, "STATUS_CHECK_TMSDL_SKIP, m_idxCurrentTms: "+m_idxCurrentTms+", name: "+strName+", skip: "+(dataTmsTrans2DeviceObject.m_bSkip?"true":"false"));

                if (dataTmsTrans2DeviceObject.m_bSkip) {
                    String strProgress="Skip TMS: "+strName;
                    publishProgress(strProgress);
                    Object obj=null;
                    goForNextTms(obj);
                }
                else {
                    m_status_dl = TMS_DL_STATUS.STATUS_SENDHEADER_INFO;
                    m_handler.sendEmptyMessage(MSG_HANDLER_LOOP);
                }
            }
            break;

            case STATUS_SENDHEADER_INFO: {
                Logger.i(m_className, "STATUS_SENDHEADER_INFO");
                byte[] dataBuf=getTMSHeaderData();
                if (dataBuf==null) {
                    Logger.i(m_className, "STATUS_SENDHEADER_INFO, dataBuf is null");
                    m_status_dl=TMS_DL_STATUS.STATUS_SENDHEADER_INFO_ERROR;
                    finalCallback_withText("STATUS_SENDHEADER_INFO_ERROR");
                    break;
                }

                ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                String strHex=instByteHexHelper.bytesArrayToHexString(dataBuf, 0, 3);
                Logger.i(m_className, "STATUS_SENDHEADER_INFO, m_idxCurrentTms: "+m_idxCurrentTms+", m_idxCurrentDataBlock: "+m_idxCurrentDataBlock+", strHex(len:"+dataBuf.length+"): "+strHex);

                String strProgress="Send header, "+strName+" "+(m_idxCurrentTms+1)+"/"+m_n_tms;
                publishProgress(strProgress);

                super.start(S3INS_TMSDL, dataBuf, m_cb_finish_sendtmsheaderinfo);
            }
            break;

            case STATUS_TMSDL: {
                byte[] dataBuf=getTMSData();
                if (dataBuf==null) {
                    Logger.i(m_className, "startProcessStateMachine, STATUS_TMSDL, dataBuf is null");
                    m_status_dl=TMS_DL_STATUS.STATUS_TMSDL_ERROR;
                    finalCallback_withText("STATUS_TMSDL_ERROR");
                    break;
                }

                ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                String strHex=instByteHexHelper.bytesArrayToHexString(dataBuf, 0, 3);
                Logger.i(m_className, "startProcessState, STATUS_TMSDL, tms: ("+(m_idxCurrentTms+1)+"/"+m_n_tms+"), block ("+(m_idxCurrentDataBlock+1)+"/"+m_currentTotalDataBlock+"), strHex(len:"+dataBuf.length+"): "+strHex+", name: "+strName);

                String strProgress="Blk("+(m_idxCurrentDataBlock+1)+"/"+m_currentTotalDataBlock+"), "+strName+"("+(m_idxCurrentTms+1)+"/"+m_n_tms+")";
                publishProgress(strProgress);

                super.start(S3INS_TMSDL, dataBuf, m_cb_finish_tmsdl);
            }
            break;

            case STATUS_TMSDL_RESULT: {
                Logger.i(m_className, "STATUS_TMSDL_RESULT");
                byte[] dataBuf=null;

                String strProgress="Get TMS transfer result . . .";
                publishProgress(strProgress);

                super.start(S3INS_TMSDLRESULT, dataBuf, m_cb_finish_tmsdl_result);
            }
            break;
        }
    }

    private void publishProgress(String strText) {
        if (m_cb_progress!=null) {
            try {
                m_cb_progress.setParameter(strText);
                m_cb_progress.call();
            }
            catch (Exception ex) {
                Logger.w(m_className, "publishProgress, Exception ex: "+ex.toString());
            }

        }
    }

    private byte[] getTMSHeaderData() {
        byte[] dataBuf=null;
        List<Byte> list=new ArrayList<Byte>();
        byte[] buf=null;

        Data_tmsTrans2DeviceObject dataTmsTrans2DeviceObject=m_dataTmsTrans2DeviceObjectList.get(m_idxCurrentTms);
        Data_tms_header dataTmsHeader=dataTmsTrans2DeviceObject.m_dataTmsHeader;

        if ( (dataTmsHeader!=null)&&(dataTmsHeader.m_moduleList!=null)&&(dataTmsHeader.m_moduleList.size()>0) ) {
            Data_tms_header_module dataTmsHeaderModule=dataTmsHeader.m_moduleList.get(dataTmsHeader.m_moduleList.size()-1);
            String strLastModule=dataTmsHeaderModule.m_moduleLine;
            if (strLastModule!=null) {
                buf=strLastModule.getBytes();
            }
        }
        if (buf!=null) {
            list.add((byte) 0x00);
            list.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
            m_currentInputStream=getCurrentTmsInputSteam();
            generateCurrentTotalBlockNumber(m_currentInputStream);
        }
        if (list.size()>0) {
            dataBuf = Bytes.toArray(list);
        }
        return dataBuf;
    }

    private boolean isLastTransferTms(int idxTms) {
        boolean bLastTransferTms=false;
        if (idxTms<0) {
            Logger.w(m_className, "isLastTransferTms, idxTms<0, val: "+idxTms);
            bLastTransferTms=true;
            return bLastTransferTms;
        }
        if (idxTms>=m_n_tms) {
            Logger.w(m_className, "isLastTransferTms, idxTms>=m_n_tms, val: "+idxTms+">="+m_n_tms);
            bLastTransferTms=true;
            return bLastTransferTms;
        }

        if (idxTms == m_n_tms - 1) {
            bLastTransferTms=true;
        }
        if (bLastTransferTms!=true) {
            bLastTransferTms=true;
            for (int i=idxTms+1; i<m_dataTmsTrans2DeviceObjectList.size(); i++) {
                Data_tmsTrans2DeviceObject x=m_dataTmsTrans2DeviceObjectList.get(i);
                if (!x.m_bSkip) {
                    bLastTransferTms=false;
                    break;
                }
            }
        }
        return bLastTransferTms;
    }

    private byte[] getTMSData() {
        byte[] dataBuf=null;
        List<Byte> list=new ArrayList<Byte>();

        fetchDataBlocks(m_idxCurrentTms, m_idxCurrentDataBlock, m_currentInputStream);

        if (m_currentDataBlock==null) {
            Logger.w(m_className, "getTMSData, m_currentDataBlock is null");

            safeCloseCurrentInputStream();

            return dataBuf;
        }

        if (m_nextDataBlock==null) {
            boolean bLastTransferTms=isLastTransferTms(m_idxCurrentTms);
            if (bLastTransferTms) {
                Logger.i(m_className, "getTMSData, m_idxCurrentTms: "+m_idxCurrentTms+" is the last transferred tms");
                list.add((byte) 0x07);
            } else {
                list.add((byte) 0x03);
            }

            safeCloseCurrentInputStream();
        }
        else {
            list.add((byte) 0x01);
        }

        if (m_currentDataBlock.length>0) {
            list.addAll((Arrays.asList(ArrayUtils.toObject(m_currentDataBlock))));
        }

        if (list.size()>0) {
            dataBuf = Bytes.toArray(list);
        }

        return dataBuf;
    }

    private void safeCloseCurrentInputStream() {
        Logger.i(m_className, "safeCloseCurrentInputStream call");
        if (m_currentInputStream!=null) {
            try {
                m_currentInputStream.close();
            }
            catch (IOException io) {
                Logger.w(m_className, "safeCloseCurrentInputStream, IOException io: "+io.toString());
            }
            m_currentInputStream=null;
        }
    }

    private void fetchDataBlocks(int idxCurrentTms, int idxCurrentDataBlock, InputStream is) {
        Logger.i(m_className, "fetchDataBlocks, idxCurrentTms: "+idxCurrentTms+", idxCurrentDataBlock: "+idxCurrentDataBlock);
        byte[] buf=null;
        if (idxCurrentDataBlock==0) {
            Logger.i(m_className, "fetchDataBlocks, fetch CURRENT data block");
            buf = readData_IOStreamMethod(is);
            m_currentDataBlock = buf;
        }
        else {
            // put next data block to current data block
            m_currentDataBlock = m_nextDataBlock;
        }

        // read next data block
        Logger.i(m_className, "fetchDataBlocks, fetch NEXT data block");
        buf = readData_IOStreamMethod(is);
        m_nextDataBlock = buf;
    }

    public byte[] readData_IOStreamMethod(InputStream is) {
        //Logger.i(m_className, "readData_IOStreamMethod called");

        int max_readbyte_length=size_block;

        byte[] readBuffer = null;
        try {
            if (is == null) {
                Logger.e(m_className, "readData_IOStreamMethod, InputStream is NULL");
            }
            else {
                int readCount = 0;
                byte[] buffer = new byte[max_readbyte_length];

                readCount = is.read(buffer);

                if (readCount<0) {
                    Logger.w(m_className, "readData_IOStreamMethod, readCount<0, val: "+readCount);
                }
                else {
                    readBuffer = new byte[readCount];
                    System.arraycopy(buffer, 0, readBuffer, 0, readBuffer.length);

                    ByteHexHelper byteHexInst = ByteHexHelper.getInstance();
                    String strHex = byteHexInst.bytesArrayToHexString(readBuffer);
                    Logger.i(m_className, "readData_IOStreamMethod, readCount: " + readCount + ", strHex: " + strHex);
                }
            }
        } catch (IOException ex_io) {
            Logger.e(m_className, "readData_IOStreamMethod, IOException ex_io: " + ex_io.toString());
        }
        return readBuffer;
    }

    private void generateCurrentTotalBlockNumber(InputStream is) {
        m_currentTotalDataBlock=0;
        if (is!=null) {
            try {
                int len = is.available();
                int n_block=(int)Math.ceil((double)len/size_block);
                m_currentTotalDataBlock=n_block;
            }
            catch (IOException ioex) {
                Logger.w(m_className, "generateCurrentTotalBlockNumber, IOException ioex");
                m_currentTotalDataBlock=0;
            }
        }
        Logger.i(m_className, "generateCurrentTotalBlockNumber, m_currentTotalDataBlock: "+m_currentTotalDataBlock);
    }

    private InputStream getCurrentTmsInputSteam() {
        int idxTms=m_idxCurrentTms;
        Data_ftpdlObject data=m_dataFtpdlObjectList.get(idxTms);
        File f = new File(m_filePath, data.m_relative_pathlist);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        }
        catch (FileNotFoundException fnf) {
            Logger.w(m_className, "getCurrentTmsInputSteam, FileNotFoundException fnf: "+fnf.toString()+", m_filePath: "+m_filePath+", m_relative_pathlist: "+data.m_relative_pathlist);
            fis=null;
        }
        return fis;
    }
}
