package com.spectratech.sp530demo.sp530class;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.google.common.primitives.Bytes;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.FileUriHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.constant.FullEmvEnum;
import com.spectratech.sp530demo.constant.FullEmvEnum.TRANSACTIONSTATUS;
import com.spectratech.sp530demo.data.Data_S3INS_response;

import org.apache.commons.lang3.ArrayUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SP530_bt_S3INS_FILEDL - SP530 Main application
 * - with feature file download
 */
public class SP530_bt_S3INS_FILEDL extends SP530_bt_S3INS {

    private static final String m_className="SP530_bt_S3INS_FILEDL";

    public enum TMS_DL_STATUS {
        STATUS_UNKNOWN(-1),

        STATUS_SENDHEADER_INFO(2005),
        STATUS_SENDHEADER_INFO_ERROR(2006),
        STATUS_TMSDL(2007),
        STATUS_TMSDL_ERROR(2008);

        private final int intValue;
        private TMS_DL_STATUS(int value) {
            intValue = value;
        }
        public int toInt() {
            return intValue;
        }
    }

    public static final byte S3INS_DATADL=(byte)0x34;

    private static final int MSG_HANDLER_LOOP=1107;

    private static final int size_block=1024;

    private Uri m_uri_file;
    private String m_filename;

    private int m_idxCurrentDataBlock;
    private int m_currentTotalDataBlock;

    private InputStream m_currentInputStream;
    private byte[] m_currentDataBlock;
    private byte[] m_nextDataBlock;

    private byte m_commandCode;
    private Callback<Object> m_cb_finish_sendtmsheaderinfo;
    private Callback<Object> m_cb_finish_tmsdl;

    private Callback<Object> m_cb_progress;
    private Callback<Object> m_cb_finish;

    private Handler m_handler;

    private TMS_DL_STATUS m_status_dl;

    public SP530_bt_S3INS_FILEDL(Context context, BluetoothSocketClass btSocketClass) {
        super(context, btSocketClass);

        Logger.i(m_className, "SP530_bt_S3INS_TMSDL constructor call");

        m_status_dl= TMS_DL_STATUS.STATUS_UNKNOWN;

        m_uri_file=null;
        m_filename="";

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
        m_cb_finish_sendtmsheaderinfo=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                if (!m_bCancel) {
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

                    if (bValidResult) {
                        if (m_nextDataBlock != null) {
                            m_handler.sendEmptyMessage(MSG_HANDLER_LOOP);
                        }
                        else {
                            Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)obj;
                            publishTransferSummary2Progress(dataS3INSResponse);
                            finalCallback(obj);
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
    }

    private String getSummaryText(Data_S3INS_response dataS3INSResponse) {
        String strSummary="";

        boolean bSuccess=false;
        if ( (dataS3INSResponse.m_statusTrans==TRANSACTIONSTATUS.STATUS_FINISH)&&
                (dataS3INSResponse.m_data!=null)&&
                (dataS3INSResponse.m_data[0]==ApplicationProtocolConstant.S3RC_OK) ) {
            bSuccess=true;
        }

        String name = m_filename;
        if (bSuccess) {
            strSummary += "SUCCESS - " + name;
        }
        else {
            strSummary += "FAIL - " + name;
        }
        strSummary += "\n";

        return strSummary;
    }

    private void publishTransferSummary2Progress(Data_S3INS_response dataS3INSResponse) {
        String strSummary=getSummaryText(dataS3INSResponse);
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

    public void setCallback_progress(Callback<Object> cb_progress) {
        m_cb_progress=cb_progress;
    }

    public void setFileDlData(Uri uri_file) {
        m_uri_file=uri_file;
    }

    public void start(byte commandCode, byte[] dataBuf, Callback<Object> cb_finish) {
        Logger.i(m_className, "start");

        if (commandCode!=S3INS_DATADL) {
            Logger.w(m_className, "start, commandCode unknown, val: "+(commandCode&0xFF));
            return;
        }

        if (m_uri_file==null) {
            Logger.w(m_className, "start, m_uri_file is null");
            return;
        }

        m_commandCode=commandCode;

        m_status_dl= TMS_DL_STATUS.STATUS_UNKNOWN;
        switch (commandCode) {
            case S3INS_DATADL: {
                m_status_dl= TMS_DL_STATUS.STATUS_SENDHEADER_INFO;
            }
            break;
        }

        m_cb_finish=cb_finish;

        m_idxCurrentDataBlock=0;
        m_currentTotalDataBlock=0;

        FileUriHelper instFileUriHelper=FileUriHelper.getInstance();
        m_filename=instFileUriHelper.getDisplayName(m_context, m_uri_file);

        startProcessState();
    }

    private void startProcessState() {
        switch (m_status_dl) {
            case STATUS_SENDHEADER_INFO: {
                Logger.i(m_className, "STATUS_SENDHEADER_INFO");
                byte[] dataBuf=getTMSHeaderData();
                if (dataBuf==null) {
                    Logger.i(m_className, "STATUS_SENDHEADER_INFO, dataBuf is null");
                    m_status_dl= TMS_DL_STATUS.STATUS_SENDHEADER_INFO_ERROR;
                    finalCallback_withText("STATUS_SENDHEADER_INFO_ERROR");
                    break;
                }

                ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                String strHex=instByteHexHelper.bytesArrayToHexString(dataBuf, 0, 3);
                Logger.i(m_className, "STATUS_SENDHEADER_INFO, m_idxCurrentDataBlock: "+m_idxCurrentDataBlock+", strHex(len:"+dataBuf.length+"): "+strHex);

                String strProgress="Send header, "+m_filename;
                publishProgress(strProgress);

                super.start(m_commandCode, dataBuf, m_cb_finish_sendtmsheaderinfo);
            }
            break;

            case STATUS_TMSDL: {
                byte[] dataBuf=getTMSData();
                if (dataBuf==null) {
                    Logger.i(m_className, "startProcessStateMachine, STATUS_TMSDL, dataBuf is null");
                    m_status_dl= TMS_DL_STATUS.STATUS_TMSDL_ERROR;
                    finalCallback_withText("STATUS_TMSDL_ERROR");
                    break;
                }

                ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                String strHex=instByteHexHelper.bytesArrayToHexString(dataBuf, 0, 3);
                Logger.i(m_className, "startProcessState, STATUS_TMSDL, block ("+(m_idxCurrentDataBlock+1)+"/"+m_currentTotalDataBlock+"), strHex(len:"+dataBuf.length+"): "+strHex+", name: "+m_filename);

                String strProgress="Blk("+(m_idxCurrentDataBlock+1)+"/"+m_currentTotalDataBlock+"), "+m_filename;
                publishProgress(strProgress);

                super.start(m_commandCode, dataBuf, m_cb_finish_tmsdl);
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
        List<Byte> list=new ArrayList<>();

        list.add((byte) 0x00);

        m_currentInputStream=getCurrentTmsInputSteam();
        int size_file = 0;
        try {
            size_file=m_currentInputStream.available();
        }
        catch (Exception ex) {
            Logger.w(m_className, "getTMSHeaderData, Exception ex: "+ex.toString());
            size_file=0;
        }

        // generate total number of data block
        m_currentTotalDataBlock=0;
        int n_block=(int)Math.ceil((double)size_file/size_block);
        m_currentTotalDataBlock=n_block;

        // filename
        byte[] bytesFilename=new byte [30];
        {
            // set default character to bytesFilename: space character
            byte defaultChar=(byte)0x20;
            for (int i=0; i<bytesFilename.length; i++) {
                bytesFilename[i]=defaultChar;
            }

            byte[] tmp=m_filename.getBytes();
            int len_filename=tmp.length;
            if (len_filename>bytesFilename.length) {
                len_filename=bytesFilename.length;
            }
            System.arraycopy(tmp, 0, bytesFilename, 0, len_filename);
        }
        list.addAll(Arrays.asList(ArrayUtils.toObject(bytesFilename)));

        // file size
        byte[] bytesFilesize=new byte [4];
        for (int i=0,j=3; i<4; i++,j--) {
            bytesFilesize[i]=(byte)((size_file>>(8*j))&0xFF);
        }
        list.addAll(Arrays.asList(ArrayUtils.toObject(bytesFilesize)));

        if (list.size()>0) {
            dataBuf = Bytes.toArray(list);
        }
        return dataBuf;
    }

    private byte[] getTMSData() {
        byte[] dataBuf=null;
        List<Byte> list=new ArrayList<Byte>();

        fetchDataBlocks(m_idxCurrentDataBlock, m_currentInputStream);

        if (m_currentDataBlock==null) {
            Logger.w(m_className, "getTMSData, m_currentDataBlock is null");

            safeCloseCurrentInputStream();

            return dataBuf;
        }

        if (m_nextDataBlock==null) {
            list.add((byte) 0x03);
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

    private void fetchDataBlocks(int idxCurrentDataBlock, InputStream is) {
        Logger.i(m_className, "fetchDataBlocks, idxCurrentDataBlock: "+idxCurrentDataBlock);
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

    private InputStream getCurrentTmsInputSteam() {
        InputStream is =null;
        try {
            is = m_context.getContentResolver().openInputStream(m_uri_file);
        }
        catch (FileNotFoundException fnex) {
            Logger.w(m_className, "fnex: " + fnex.toString());
        }
        return is;
    }

    public static String getErrorCodeString(byte errorCode) {
        String strMsg;
        switch (errorCode) {
            case 0x01: {
                strMsg="Memory is not enough";
            }
            break;
            case 0x02: {
                strMsg="File name existed";
            }
            break;
            case 0x03: {
                strMsg="Wrong data format";
            }
            break;
            case 0x04: {
                strMsg="Buffer size if too large";
            }
            break;
            case 0x05: {
                strMsg="No header send";
            }
            break;
            case 0x06: {
                strMsg="Serial flash write error";
            }
            break;
            case 0x07: {
                strMsg="Write file list error";
            }
            break;
            default: {
                strMsg="code:"+(errorCode&0xFF);
            }
            break;
        }
        return strMsg;
    }
}
