package com.spectratech.sp530demo.t1000class;

import android.content.Context;
import com.google.common.primitives.Bytes;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.EncryptionHelper;
import com.spectratech.lib.IOStreamHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.socket.SocketBaseClass;
import com.spectratech.lib.sp530.ApplicationProtocolHelper;
import com.spectratech.lib.sp530.comm_protocol_c.T_BUFClass;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.lib.sp530.constant.ApplicationProtocolEnum;
import com.spectratech.lib.sp530.constant.SP530Constant;
import com.spectratech.lib.sp530.data.Data_AP_ONE;
import com.spectratech.lib.sp530.db.DBTransSummaryDetail;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.conf.ConfigDevice;
import com.spectratech.sp530demo.constant.FullEmvEnum.TRANSACTIONSTATUS;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.controller.SimHostHelper;
import com.spectratech.sp530demo.controller.SkeyForMutuAuthHelper;
import com.spectratech.sp530demo.data.Data_S3INS_response;
import com.spectratech.sp530demo.data.Data_logdatapacket;
import com.spectratech.sp530demo.data.Data_runtime_s3Auth;
import com.spectratech.sp530demo.data.Data_setting_s3trans;
import org.apache.commons.lang3.ArrayUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * T1000_S3INS - T1000 Main application
 */
public class T1000_S3INS {

    private static final String m_className="T1000_S3INS";

    /**
     * Variable to store context of application
     */
    protected Context m_context;

    /**
     * Variable to store the transaction result is simulated from Android device
     */
    protected boolean m_bTransactionSimulateTCPHost;

    /**
     * Variable to indicate the always use of CRC for checksum
     */
    protected boolean m_bAlwaysUseCRC;

    /**
     * Variable to indicate logical channel one uses SSL
     */
    protected boolean m_bLogicalChOneUseSSL;

    /**
     * Variable to store socketClass
     */
    protected SocketBaseClass m_socketClass;
    private Data_setting_s3trans m_dataS3TransConf;
    private Callback<Object> m_cb_finish;
    private Callback<Object> m_cb_progressText;

    /**
     * Variable to store input command packet
     */
    protected Data_AP_ONE m_inputDataAP;

    /**
     * Variable to store command packets needed to be executed
     */
    protected Stack<Data_AP_ONE> m_dataAPStack;

    /**
     * Variable to store current executing command packet
     */
    protected Data_AP_ONE m_currentDataAP;

    /**
     * Worker thread for T1000 application
     */
    protected TransactionThread m_workerThread;
    /**
     * Flag to indicate cancel of operation
     */
    protected boolean m_bCancel;

    /**
     * Variable to indicate accept of transaction when sim host is used
     */
    public static boolean m_bAcceptTransaction = false;

    private int m_countInitAuthOperation;
    private int m_countMutuAuthOperation;

    /**
     * Constructor for SP530_bt_S3INS
     * @param context context of application
     * @param socketClass SocketBaseClass object
     */
    public T1000_S3INS(Context context, SocketBaseClass socketClass) {
        init(context);
        m_socketClass=socketClass;
    }

    private void init(Context context) {
        m_context=context;
        m_bTransactionSimulateTCPHost=false;
        m_bAlwaysUseCRC=false;
        m_bLogicalChOneUseSSL=true;
        m_socketClass=null;
        m_cb_finish=null;
        m_cb_progressText=null;

        m_currentDataAP=null;

        m_workerThread=null;
        m_bCancel=false;
    }

    /**
     * get response data after running an instruction
     * @return response data object which includes status and byte array data
     */
    public synchronized Data_S3INS_response getS3INSResponseObject() {
        Data_S3INS_response dataResponse=null;
        if (m_workerThread!=null) {
            return m_workerThread.getS3INSResponse();
        }
        return dataResponse;
    }

    private boolean checkAndAddMutuAuthCommand2Stack() {
        boolean flag=false;
        // if the input command is S3INS_MUTU_AUTH, no need to add the S3INS_MUTU_AUTH command to stack
        if (m_inputDataAP.m_commandCode==ApplicationProtocolConstant.S3INS_MUTU_AUTH) {
            return flag;
        }
        if (Application.SkeyForMutuAuth!=null) {
            // suppose we only carry initial authorization at most one time during each input command
            if (m_countMutuAuthOperation < 1) {
                m_countMutuAuthOperation++;
                byte commandCode = ApplicationProtocolConstant.S3INS_MUTU_AUTH;
                byte[] buf = DemoMainActivity.getMutuAuthDataBytes();
                Data_AP_ONE dataMutuAuth = encapsulateBaseData_ONE(commandCode, buf);
                m_dataAPStack.push(dataMutuAuth);
                flag=true;
            }
        }
        return flag;
    }

    private boolean checkAndAddInitAuthCommand2Stack() {
        boolean flag=false;
        // if the input command is S3INS_INIT_AUTH, no need to add the S3INS_INIT_AUTH command to stack
        if (m_inputDataAP.m_commandCode==ApplicationProtocolConstant.S3INS_INIT_AUTH) {
            return flag;
        }
        if (Application.SkeyForMutuAuth==null) {
            // suppose we only carry initial authorization at most one time during each input command
            if (m_countInitAuthOperation < 1) {
                m_countInitAuthOperation++;

                byte commandCode=ApplicationProtocolConstant.S3INS_INIT_AUTH;
                byte[] buf=DemoMainActivity.getInitAuthDataBytes();
                // set current sequence number to zero
                ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
                instApplicationProtocolHelper.setCurrentSequenceNumber2Zero();
                Data_AP_ONE dataInitAuth=encapsulateBaseData_ONE(commandCode, buf);
                m_dataAPStack.push(dataInitAuth);
                flag=true;
            }
        }
        return flag;
    }

    private void printStartCommand() {
        Data_AP_ONE dataAP=m_inputDataAP;
        byte commandCode=dataAP.m_commandCode;
        String strCommandCode=ApplicationProtocolConstant.S3RC_COMMAND_MAP.get(commandCode);
        Logger.i(m_className, "printStartCommand, commandCode: " + strCommandCode);
    }

    private void makeValidateCommand() {
        ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
        switch (m_inputDataAP.m_commandCode) {
            case ApplicationProtocolConstant.S3INS_SHOW_STAT: {
                m_inputDataAP.m_dataLength=instApplicationProtocolHelper.converDataLength2byteArray(SP530Constant.DATABYTES_SHOWSTATUS.length);
                m_inputDataAP.m_dataArray=new byte[SP530Constant.DATABYTES_SHOWSTATUS.length];
                System.arraycopy(SP530Constant.DATABYTES_SHOWSTATUS, 0, m_inputDataAP.m_dataArray, 0, SP530Constant.DATABYTES_SHOWSTATUS.length);
            }
            break;
            case ApplicationProtocolConstant.S3INS_FULL_EMV: {
            }
            break;
        }
    }

    private Data_AP_ONE encapsulateBaseData_ONE(byte commandCode, byte[] dataBuf) {
        ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();

        Data_AP_ONE dataAP=instApplicationProtocolHelper.encapsulateBaseData_ONE(dataBuf);

        // save application sequence number
        SkeyForMutuAuthHelper instSkeyForMutuAuthHelper=SkeyForMutuAuthHelper.getInstance();
        int seqno=instApplicationProtocolHelper.getCurrentSequnceNumber();
        instSkeyForMutuAuthHelper.setSeqnoForApplicationLayer(m_context, seqno);

        dataAP.m_commandCode=commandCode;

        dataAP.m_srcAddress= ConfigDevice.ADDRESS;
        dataAP.m_desAddress=(byte)0xE2;

        return dataAP;
    }

    /**
     * Start SP503 applciation
     * @param commandCode execute command code
     * @param dataBuf data
     * @param cb_finish callback for finish executing command code
     */
    public void start(byte commandCode, byte[] dataBuf, Callback<Object> cb_finish) {
        safeCancelThread();

        m_inputDataAP=encapsulateBaseData_ONE(commandCode, dataBuf);
        printStartCommand();
        makeValidateCommand();

        m_dataAPStack=new Stack<Data_AP_ONE>();
        m_countInitAuthOperation=0;
        m_countMutuAuthOperation=0;

        String strPacketEncap="RAW";
        Logger.i(m_className, "start called, packet encapsulate method: " + strPacketEncap);
        if (m_bTransactionSimulateTCPHost) {
            Logger.i(m_className, "start called, SimulateTCPHost is TRUE");
        }
        else {
            Logger.i(m_className, "start called, SimulateTCPHost is FALSE");
        }

        m_dataS3TransConf=new Data_setting_s3trans(m_context);
        m_cb_finish=cb_finish;

        m_workerThread = new TransactionThread();
        m_workerThread.start();
    }

    public boolean isShowingTransReceipt() {
        if (m_workerThread==null) {
            return false;
        }
        return m_workerThread.isShowingTransReceipt();
    }

    /**
     * Check for finish of executing command
     * @return true for finish; false otherwise
     */
    public boolean isFinish() {
        if (m_workerThread==null) {
            return true;
        }
        return m_workerThread.isFinish();
    }

    private class TransactionThread extends Thread {
        private TRANSACTIONSTATUS m_status;
        protected InputStream m_is;
        protected OutputStream m_os;
        protected List<Byte> m_packet_response;

        private int m_count;

        private boolean m_bInputCommandRequestSend;

        public TransactionThread() {
            super();

            m_is=getInputStream();
            m_os=getOutputStream();
            m_bCancel=false;

            m_status= TRANSACTIONSTATUS.STATUS_UNKNOWN;
            m_bInputCommandRequestSend=false;
        }

        private InputStream getInputStream() {
            if (m_socketClass==null) {
                Logger.e(m_className, "TransactionThread, getInputStream, m_btSocketClass is NULL");
                return null;
            }
            return m_socketClass.getInputStream();
        }

        private OutputStream getOutputStream() {
            if (m_socketClass==null) {
                Logger.e(m_className, "TransactionThread, getOutputStream, m_btSocketClass is NULL");
                return null;
            }
            return m_socketClass.getOutputStream();
        }

        public void run() {
            m_status= TRANSACTIONSTATUS.STATUS_START;
            sequential_process();
        }

        public void sequential_process () {
            boolean bExistLoop = false;
            while ( (!bExistLoop)&&(!m_bCancel)&&(!m_workerThread.isInterrupted()) ) {
                switch (m_status) {
                    case STATUS_UNKNOWN: {
                        Logger.i(m_className, "STATUS_UNKNOWN");
                        bExistLoop = true;
                    }
                    break;

                    case STATUS_START: {
                        Logger.i(m_className, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                        Logger.i(m_className, "STATUS_START");
                        m_status = TRANSACTIONSTATUS.STATUS_START_CLEARSTREAM;
                    }
                    break;

                    case STATUS_START_CLEARSTREAM: {
                        processClearStream();
                        m_status=TRANSACTIONSTATUS.STATUS_REQUEST_SEND;
                    }
                    break;

                    case STATUS_REQUEST_SEND: {
                        Logger.i(m_className, "STATUS_REQUEST_SEND");

                        checkAndAddInitAuthCommand2Stack();

                        if (m_dataAPStack.size()>0) {
                            m_currentDataAP=m_dataAPStack.pop();
                            m_bInputCommandRequestSend=false;
                        }
                        else {
                            m_currentDataAP=m_inputDataAP;
                            m_bInputCommandRequestSend=true;
                        }

                        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                        String strHex=instByteHexHelper.byteToHexString(m_currentDataAP.m_commandCode);
                        Logger.i(m_className, "STATUS_REQUEST_SEND, commandCode: "+strHex);

                        byte[] packetBuf=null;
                        switch (m_currentDataAP.m_commandCode) {
                            case ApplicationProtocolConstant.S3INS_INIT_AUTH:
                            case ApplicationProtocolConstant.S3INS_MUTU_AUTH:
                            {
                                packetBuf = m_currentDataAP.getPacket();
                            }
                            break;
                            default: {
                                if (m_bAlwaysUseCRC) {
                                    packetBuf = m_currentDataAP.getPacket();
                                }
                                else {
                                    packetBuf = m_currentDataAP.getPacket(Application.SkeyForMutuAuth);
                                }
                            }
                            break;
                        }

                        // add to log
                        Data_logdatapacket logdataPacket=new Data_logdatapacket(packetBuf, true);
                        Application.add2LogMemory(logdataPacket);

                        // print info
                        strHex=instByteHexHelper.bytesArrayToHexString(packetBuf);
                        Logger.i(m_className, "AL: "+strHex);

                        // pack to T_BUFClass
                        T_BUFClass buf=new T_BUFClass();
                        buf.add2tail_sbuf(packetBuf);
                        buf.d_len+=packetBuf.length;

                        boolean bWrite=false;
                        IOStreamHelper instIOStreamHelper=IOStreamHelper.getInstance();
                        bWrite=instIOStreamHelper.writeData(m_os, packetBuf);

                        if (m_status== TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL) {
                            return;
                        }
                        if (bWrite) {
                            m_status= TRANSACTIONSTATUS.STATUS_RESPONSE_GET;
                        }
                        else {
                            Logger.e(m_className, "send request fail");
                            m_status= TRANSACTIONSTATUS.STATUS_REQUEST_SEND_FAIL;
                        }
                    }
                    break;

                    case STATUS_REQUEST_SEND_FAIL: {
                        Logger.i(m_className, "STATUS_REQUEST_SEND_FAIL");
                        bExistLoop = true;
                    }
                    break;

                    case STATUS_RESPONSE_GET: {
                        Logger.i(m_className, "STATUS_RESPONSE_GET");
                        TRANSACTIONSTATUS status_success_next = TRANSACTIONSTATUS.STATUS_RESPONSE_GET_SUCCESS;
                        TRANSACTIONSTATUS status_fail_next = TRANSACTIONSTATUS.STATUS_RESPONSE_GET_FAIL;
                        getResponse(status_success_next, status_fail_next);
                    }
                    break;

                    case STATUS_RESPONSE_GET_SUCCESS: {
                        Logger.i(m_className, "STATUS_RESPONSE_GET_SUCCESS");
                        processResponse();
                    }
                    break;

                    case STATUS_RESPONSE_GET_FAIL: {
                        Logger.i(m_className, "STATUS_RESPONSE_GET_FAIL");
                        bExistLoop = true;
                    }
                    break;

                    case STATUS_SHOW_TRANSRECEIPT: {
                        Logger.i(m_className, "STATUS_SHOW_TRANSRECEIPT");

                        ActivityHelper instActivityHelper = ActivityHelper.getInstance();
                        final DemoMainActivity act = instActivityHelper.getDemoMainActivity();
                        Callback cbCheck = act.getCBTransFinishBatchMode();
                        if (m_cb_finish.equals(cbCheck)) {
                            m_status = TRANSACTIONSTATUS.STATUS_FINISH;
                        } else {
                            showTransactionReceipt();
                        }
                    }
                    break;

                    case STATUS_WAIT_SHOW_TRANSRECEIPT: {
                        Logger.i(m_className, "STATUS_WAIT_SHOW_TRANSRECEIPT");
                        try {
                            synchronized(this) {
                                this.wait();
                            }
                        }
                        catch (InterruptedException ie) {
                            Logger.e(m_className, "STATUS_WAIT_SHOW_TRANSRECEIPT, InterruptedException, ie: "+ie.toString());
                        }
                        m_status = TRANSACTIONSTATUS.STATUS_FINISH;
                    }
                    break;

                    case STATUS_FINISH: {
                        Logger.i(m_className, "STATUS_FINISH");
                        bExistLoop = true;
                    }
                    break;

                    case STATUS_REQUEST_CANCEL: {
                        Logger.i(m_className, "STATUS_CANCEL");
                        bExistLoop = true;
                    }
                    break;

                    case STATUS_FAIL: {
                        Logger.i(m_className, "STATUS_FAIL");
                        bExistLoop = true;
                    }
                    break;
                }
            }

            if (m_cb_finish!=null) {
                Data_S3INS_response dataS3InsResponse=getS3INSResponse();
                m_cb_finish.setParameter(dataS3InsResponse);
                try {
                    m_cb_finish.call();
                }
                catch (Exception ex) {
                    Logger.e(m_className, "sequential_process, m_status:"+m_status+", callback Exception: "+ex.toString());
                }
            }
        }

        public void processClearStream() {
            if ((!m_workerThread.isInterrupted()) && (!m_bCancel)) {
                int countClearByte = 0;
                int iISAvailable = 0;
                try {
                    if (m_is == null) {
                        Logger.e(m_className, "processClearStream, InputStream is NULL");
                    } else {
                        iISAvailable = m_is.available();
                        byte[] readBuffer = null;
                        int n_loop=0;
                        while (iISAvailable > 0) {
                            n_loop++;

                            //m_is.skip(iISAvailable);
                            readBuffer=new byte [iISAvailable];
                            iISAvailable=m_is.read(readBuffer);
                            countClearByte += iISAvailable;

                            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                            String strHex=instByteHexHelper.bytesArrayToHexString(readBuffer);
                            Logger.i(m_className, "processClearStream, strHex: "+strHex);
                            Logger.i(m_className, "processClearStream, loop: "+n_loop+", byte cleared: " + iISAvailable);

                            iISAvailable = m_is.available();
                        }
                    }
                } catch (IOException ex) {
                    Logger.e(m_className, "processClearStream excepton: " + ex.toString());
                }
                Logger.i(m_className, "processClearStream, byte cleared: " + countClearByte);
            }
        }

        /**
         * get response data after running an instruction
         * @return response data object which includes status and byte array data
         */
        private synchronized Data_S3INS_response getS3INSResponse() {
            byte[] dataBuf=null;
            if (m_packet_response!=null) {
                ApplicationProtocolHelper instApplicationProtocolHelper = ApplicationProtocolHelper.getInstance();
                ApplicationProtocolEnum.FORMAT_IDENTIFIER id_fi = instApplicationProtocolHelper.getFormatIdentifier(m_packet_response);
                switch (id_fi) {
                    case ONE: {
                        Data_AP_ONE dataAPOne=new Data_AP_ONE(m_packet_response, Application.SkeyForMutuAuth);
                        dataBuf=dataAPOne.m_dataArray;
                    }
                    break;
                    default: {
                        Logger.i(m_className, "getS3INSResponse, no Data_AP_ONE is found");
                    }
                    break;
                }
            }
            Data_S3INS_response dataResponse=new Data_S3INS_response(m_status, dataBuf);
            return dataResponse;
        }

        public void getResponse(TRANSACTIONSTATUS status_success_next) {
            TRANSACTIONSTATUS status_fail_next= TRANSACTIONSTATUS.STATUS_RESPONSE_GET_FAIL;
            getResponse(status_success_next, status_fail_next);
        }
        public void getResponse(TRANSACTIONSTATUS status_success_next, TRANSACTIONSTATUS status_fail_next) {
            long waitReponseTimeInMs=m_dataS3TransConf.m_waitReponseTimeInMs;
            switch (m_inputDataAP.m_commandCode) {
                case ApplicationProtocolConstant.S3INS_RESET: {
                    waitReponseTimeInMs=2*1000;
                }
                break;
            }

            IOStreamHelper instIOStreamHelper=IOStreamHelper.getInstance();
            byte[] buf=instIOStreamHelper.readDataWithWaitTime_IOStreamMethod(m_is, waitReponseTimeInMs, m_cb_progressText);
            if (buf==null) {
                if (m_status!=TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL) {
                    m_status = status_fail_next;
                }
                return;
            }
            m_packet_response = new ArrayList<Byte>();
            m_packet_response.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
            m_status = status_success_next;
        }

        public void showTransactionReceipt() {
            if (m_context instanceof DemoMainActivity) {
                DemoMainActivity act=(DemoMainActivity)m_context;

                m_cb_finish=null;

                // require Guava: Google Core Libraries
                byte[] byteBuffer = Bytes.toArray(m_packet_response);
                Callback cb_showReceiptFinish = new Callback<Object>() {
                    @Override
                    public Void call() throws Exception {
                        if (m_workerThread!=null) {
                            synchronized (m_workerThread) {
                                m_workerThread.notifyAll();
                            }
                        }
                        return null;
                    }
                };
                act.showTransactionReceiptOnly(byteBuffer, cb_showReceiptFinish);

                act.enableAllUIInputsRunOnUiThreadWithDelay(1000);

                // please wait
                m_status= TRANSACTIONSTATUS.STATUS_WAIT_SHOW_TRANSRECEIPT;
            }
        }

        public void processResponse() {
            if (m_packet_response==null) {
                Logger.w(m_className, "processResponse, m_packet_response is NULL");
                m_status= TRANSACTIONSTATUS.STATUS_FINISH;
                return;
            }

            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
            String strHex=instByteHexHelper.bytesArrayToHexString(m_packet_response);
            Logger.i(m_className, "processResponse, m_packet_response: "+strHex);

            ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
            ApplicationProtocolEnum.FORMAT_IDENTIFIER id_fi=instApplicationProtocolHelper.getFormatIdentifier(m_packet_response);
            switch (id_fi) {
                case ZERO: {
                    m_status=processResponse_FI_ZERO();
                }
                break;
                case ONE: {
                    m_status=processResponse_FI_ONE();
                }
                break;
                default: {
                    Logger.i(m_className, "processData, DEFAUT (none)");
                    m_status=TRANSACTIONSTATUS.STATUS_FINISH;
                }
                break;
            }
        }

        private void generate_tdes_skey(byte[] dataPartOne, byte[] dataPartTwo) {
            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
            String strHex="";
            EncryptionHelper instEncryptionHelper=EncryptionHelper.getInstance();

            Data_runtime_s3Auth dataRunTimeS3Auth=DemoMainActivity.m_dataRunTimeS3Auth;

            // generate sKey
            byte[] sKey=null;
            byte[] key=dataRunTimeS3Auth.Key;
            byte[] txMsg=dataRunTimeS3Auth.TRnd;
            byte[] rxMsg=dataPartOne;
            try {
                sKey=instEncryptionHelper.encrypt_tdes_16bytes_interpolate4bytes(key, rxMsg, txMsg);
                strHex=instByteHexHelper.bytesArrayToHexString(sKey);
                Logger.i(m_className, "sKey: "+strHex);
            }
            catch (Exception ex) {
                Logger.i(m_className, "Exception ex1: "+ex.toString());
            }

            // verify sKey
            byte[] tdes_check=null;
            try {
                tdes_check = instEncryptionHelper.encrypt_tdes_16bytes(sKey, txMsg, rxMsg);
                strHex=instByteHexHelper.bytesArrayToHexString(tdes_check);
                Logger.i(m_className, "tdes_check: "+strHex);
            }
            catch (Exception ex) {
                Logger.i(m_className, "Exception ex2: "+ex.toString());
            }
            boolean bValid=false;
            if (Arrays.equals(dataPartTwo, tdes_check)) {
                bValid=true;
            }

            if (!bValid) {
                Logger.w(m_className, "tdes checks failed");
                return;
            }

            // generate tDes for debug
            byte[] tdes=null;
            try {
                tdes=instEncryptionHelper.encrypt_tdes_16bytes_interpolate4bytes(sKey, txMsg, rxMsg);
            }
            catch (Exception ex) {
                Logger.i(m_className, "Exception ex1: "+ex.toString());
            }
            strHex=instByteHexHelper.bytesArrayToHexString(tdes);
            Logger.i(m_className, "debug ,tdes: "+strHex);

            Application.RxmsgForMutuAuth=rxMsg;
            Application.SkeyForMutuAuth=sKey;
            SkeyForMutuAuthHelper instSkeyForMutuAuthHelper=SkeyForMutuAuthHelper.getInstance();
            instSkeyForMutuAuthHelper.setRxmsg(m_context, Application.RxmsgForMutuAuth);
            instSkeyForMutuAuthHelper.setSKey(m_context, Application.SkeyForMutuAuth);
        }

        public TRANSACTIONSTATUS processResponse_FI_ONE() {
            Logger.i(m_className, "processResponse, FI_ONE");

            final String strResponseKey=ResourcesHelper.getBaseContextString(m_context, R.string.response);

            TRANSACTIONSTATUS status=TRANSACTIONSTATUS.STATUS_FAIL;
            ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();

            Data_AP_ONE dataResponse=new Data_AP_ONE(m_packet_response, Application.SkeyForMutuAuth);
            if (!dataResponse.isSuccessLoadData()) {
                Logger.e(m_className, "processResponse, FI_ONE, load Data_AP_ONE failed");
                return TRANSACTIONSTATUS.STATUS_FAIL;
            }

            byte responseCode=dataResponse.m_dataArray[0];
            int seqNumber=instApplicationProtocolHelper.getSequenceNumber(m_packet_response);
            String strResponse= ApplicationProtocolConstant.S3RC_REPONSE_MAP.get(responseCode);
            String strResponseCode=instByteHexHelper.byteToHexString(responseCode);
            final String strOut=strResponseKey+" ("+seqNumber+"): "+strResponse+" [0x"+strResponseCode+"]";
            ActivityHelper instActivityHelper = ActivityHelper.getInstance();
            final DemoMainActivity act = instActivityHelper.getDemoMainActivity();
            if (act != null) {
                act.setResponseMessageRunOnUiThread(strOut);
            }
            Logger.i(m_className, "processResponse_FI_ONE, responseCode: 0x"+strResponseCode);

            // need init auth?
            switch (responseCode) {
                case ApplicationProtocolConstant.S3RC_ERR_CSUM: {
                    // add init auth to stack
                    Application.SkeyForMutuAuth=null;
                    boolean flag=checkAndAddInitAuthCommand2Stack();
                    if (flag) {
                        status=TRANSACTIONSTATUS.STATUS_REQUEST_SEND;
                        return status;
                    }
                }
                break;
                case ApplicationProtocolConstant.S3RC_ERR_INS: {
                    // add init auth to stack
                    Application.SkeyForMutuAuth=null;
                    boolean flag=checkAndAddInitAuthCommand2Stack();
                    if (flag) {
                        status=TRANSACTIONSTATUS.STATUS_REQUEST_SEND;
                        return status;
                    }
                }
                break;
                case ApplicationProtocolConstant.S3RC_ERR_SEQ: {
                    // add init auth to stack
                    Application.SkeyForMutuAuth=null;
                    boolean flag=checkAndAddInitAuthCommand2Stack();
                    if (flag) {
                        status=TRANSACTIONSTATUS.STATUS_REQUEST_SEND;
                        return status;
                    }
                }
                break;
            }

            boolean bValidChecksum = instApplicationProtocolHelper.isValidChecksum(m_packet_response, ApplicationProtocolEnum.FORMAT_IDENTIFIER.ONE, Application.SkeyForMutuAuth);
            if (bValidChecksum) {
                String strHex="";

                // add to log
                Data_logdatapacket logdataPacket=new Data_logdatapacket(m_packet_response, false);
                Application.add2LogMemory(logdataPacket);

                strHex=instByteHexHelper.bytesArrayToHexString(dataResponse.m_dataArray);
                Logger.i(m_className, "processResponse, FI_ONE, data strHex: "+strHex);

                if (m_currentDataAP.m_commandCode==dataResponse.m_commandCode) {

                    // need to update sequence number?
                    if  (m_currentDataAP.m_commandCode==ApplicationProtocolConstant.S3INS_INIT_AUTH) {
                        m_currentDataAP.m_sequenceNumber = dataResponse.m_sequenceNumber;
                        int n_seq = instApplicationProtocolHelper.getDataLengthFromRaw(m_currentDataAP.m_sequenceNumber);
                        n_seq++;
                        instApplicationProtocolHelper.setCurrentSequenceNumber(n_seq);
                        n_seq=instApplicationProtocolHelper.getCurrentSequnceNumber();
                        SkeyForMutuAuthHelper instSkeyForMutuAuthHelper=SkeyForMutuAuthHelper.getInstance();
                        instSkeyForMutuAuthHelper.setSeqnoForApplicationLayer(m_context, n_seq);
                    }

                    if (Arrays.equals(m_currentDataAP.m_sequenceNumber, dataResponse.m_sequenceNumber)||(m_currentDataAP.m_commandCode==ApplicationProtocolConstant.S3INS_INIT_AUTH)) {
                        switch (responseCode) {
                            case ApplicationProtocolConstant.S3RC_OK: {
                                switch (m_currentDataAP.m_commandCode) {
                                    case ApplicationProtocolConstant.S3INS_INIT_AUTH: {
                                        int count = 1;
                                        byte Ktype = dataResponse.m_dataArray[count++];
                                        byte KIdx = dataResponse.m_dataArray[count++];
                                        byte[] dataPartOne = new byte[8];
                                        System.arraycopy(dataResponse.m_dataArray, count, dataPartOne, 0, 8);
                                        count += 8;
                                        byte[] dataPartTwo = new byte[16];
                                        System.arraycopy(dataResponse.m_dataArray, count, dataPartTwo, 0, 16);
                                        count += 16;
                                        generate_tdes_skey(dataPartOne, dataPartTwo);
                                        if (Application.SkeyForMutuAuth == null) {
                                            Logger.w(m_className, "processResponse_FI_ONE, tdes Application.SkeyForMutuAuth is NULL");
                                        }

                                        // add mutual auth to stack
                                        checkAndAddMutuAuthCommand2Stack();

                                        status = TRANSACTIONSTATUS.STATUS_FINISH;
                                    }
                                    break;
                                    case ApplicationProtocolConstant.S3INS_MUTU_AUTH: {
                                        // repack the packet with correct sequence number
                                        if (m_inputDataAP!=null) {
                                            m_inputDataAP=encapsulateBaseData_ONE(m_inputDataAP.m_commandCode, m_inputDataAP.m_dataArray);
                                        }
                                        status = TRANSACTIONSTATUS.STATUS_FINISH;
                                    }
                                    break;
                                    case ApplicationProtocolConstant.S3INS_FULL_EMV: {
                                        byte[] byteBuffer = Bytes.toArray(m_packet_response);
                                        long idDbInsert = DBTransSummaryDetail.insert(m_context, byteBuffer);
                                        status = TRANSACTIONSTATUS.STATUS_SHOW_TRANSRECEIPT;
                                    }
                                    break;
                                    default: {
                                        status = TRANSACTIONSTATUS.STATUS_FINISH;
                                    }
                                    break;
                                }
                            }
                            break;
                            default: {
                                status = TRANSACTIONSTATUS.STATUS_FAIL;
                            }
                            break;
                        }
                    }
                    else {
                        status = TRANSACTIONSTATUS.STATUS_FAIL;
                    }
                }
                else {
                }
            }
            else {
            }

            if (status==TRANSACTIONSTATUS.STATUS_FINISH) {
                if ( (m_dataAPStack.size()>0) || (!m_bInputCommandRequestSend) ) {
                    status=TRANSACTIONSTATUS.STATUS_REQUEST_SEND;
                }
            }

            return status;
        }
        public TRANSACTIONSTATUS processResponse_FI_ZERO() {
            Logger.i(m_className, "processResponse, FI_ZERO");


            ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
            boolean bValidCRC = instApplicationProtocolHelper.isValidCRC(m_packet_response, ApplicationProtocolEnum.FORMAT_IDENTIFIER.ZERO);
            if (bValidCRC) {

                // add to log
                Data_logdatapacket logdataPacket=new Data_logdatapacket(m_packet_response, false);
                Application.add2LogMemory(logdataPacket);

                if (m_context instanceof DemoMainActivity) {
                    DemoMainActivity act = (DemoMainActivity) m_context;
                    act.append2PopupTransMessageRunOnUiThread("waiting for the authorization . . .");
                }

                SimHostHelper instSimHostHelper = SimHostHelper.getInstance();

                Data_setting_s3trans dataS3transConfig=new Data_setting_s3trans(m_context);
                switch (dataS3transConfig.m_idxTransResult) {
                    case 0:
                        m_bAcceptTransaction=true;
                        break;
                    case 1:
                        m_bAcceptTransaction=false;
                        break;
                    case 2:
                        m_bAcceptTransaction=!m_bAcceptTransaction;
                        break;
                    case 3:
                        Random r = new Random();
                        int val=r.nextInt(99);
                        if (val<50) {
                            m_bAcceptTransaction=true;
                        }
                        else {
                            m_bAcceptTransaction=false;
                        }
                        break;
                    default:
                        m_bAcceptTransaction=true;
                        break;
                }

                byte[] responseData = instSimHostHelper.getpacketbuf_responseOnLineRequest(m_bAcceptTransaction);
                T_BUFClass buf=new T_BUFClass();
                buf.replace_sbuf(responseData);
                buf.d_len+=responseData.length;

                IOStreamHelper instIOStreamHelper=IOStreamHelper.getInstance();
                instIOStreamHelper.writeData(m_os, responseData);
            }

            m_packet_response.clear();
            m_packet_response=null;

            TRANSACTIONSTATUS status=TRANSACTIONSTATUS.STATUS_RESPONSE_GET;
            return status;
        }

        public void cancel() {
            m_bCancel=true;
            m_status= TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL;
            m_workerThread.interrupt();
        }

        public boolean isShowingTransReceipt() {
            boolean bRet=false;
            switch (m_status) {
                case STATUS_SHOW_TRANSRECEIPT:
                case STATUS_WAIT_SHOW_TRANSRECEIPT: {
                    bRet=true;
                }
                break;
            }
            return bRet;
        }

        public boolean isFinish() {
            boolean bRet=false;
            switch (m_status) {
                case STATUS_UNKNOWN:
                case STATUS_WAIT_COMM_READY_FAIL:
                //case STATUS_WAIT_TCP_READY_FAIL:
                case STATUS_WAIT_SSLSERVER_READY_FAIL:
                case STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL:
                case STATUS_REQUEST_SEND_FAIL:
                case STATUS_RESPONSE_GET_FAIL:
                case STATUS_FINISH:
                case STATUS_REQUEST_CANCEL:
                case STATUS_FAIL:
                    bRet=true;
                    break;
            }
            return bRet;
        }
    }


    /**
     * Cancel thread safely
     */
    public void safeCancelThread() {
        if (m_workerThread!=null) {
            Logger.i(m_className, "safeCancelThread called");
            m_workerThread.cancel();
            m_workerThread=null;
        }
    }

    /**
     * Free resource of this class
     */
    public void onDestroy() {
        Logger.i(m_className, "onDestroy");
        safeCancelThread();
    }


    public void set_callback_progressText(Callback<Object> cb) {
        m_cb_progressText=cb;
    }
}
