package com.spectratech.sp530demo.sp530class;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.common.primitives.Bytes;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.EncryptionHelper;
import com.spectratech.lib.IOStreamHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.constant.BluetoothTransmissionEnum;
import com.spectratech.lib.sp530.ApplicationProtocolHelper;
import com.spectratech.lib.sp530.comm_protocol_c.SP530_AppMcpCHelper;
import com.spectratech.lib.sp530.comm_protocol_c.T_BUFClass;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.lib.sp530.constant.ApplicationProtocolEnum;
import com.spectratech.lib.sp530.constant.CommConstant;
import com.spectratech.lib.sp530.constant.SP530Constant;
import com.spectratech.lib.sp530.data.Data_AP_ONE;
import com.spectratech.lib.sp530.db.DBTransSummaryDetail;
import com.spectratech.lib.tcpip.SSLServerHelper;
import com.spectratech.lib.tcpip.data.Data_SSLServerLocal;
import com.spectratech.lib.tcpip.socket.SSLSocketServerThread;
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
 * SP530_bt_S3INS - SP530 Main application
 */
public class SP530_bt_S3INS {

    private static final String m_className="SP530_bt_S3INS";

    private static final BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL DEFAULT_PACKET_ENCAPSULATE_LEVEL= BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL.MCP;

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
     * Variable to indicate local channeluses SSL
     */
    protected boolean m_bLocalChannelUseSSL;

    /**
     * Variable to store the packet encapsulate level
     */
    protected BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL m_packetEncapsulateLevel;
    /**
     * Variable to store BluetoothSocketClass
     */
    protected BluetoothSocketClass m_btSocketClass;
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
     * Worker thread for SP530 application
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

    private int m_idxLocalChannel;

    private BroadcastReceiver m_broadcastReceiver;

    /**
     * Constructor for SP530_bt_S3INS
     * @param context context of application
     * @param btSocketClass BluetoothSocketClass object
     */
    public SP530_bt_S3INS(Context context, BluetoothSocketClass btSocketClass) {
        init(context);
        m_btSocketClass=btSocketClass;
    }

    private void init(Context context) {
        m_context=context;
        m_bTransactionSimulateTCPHost=false;
        m_bAlwaysUseCRC=false;
        m_bLocalChannelUseSSL=true;
        m_packetEncapsulateLevel=DEFAULT_PACKET_ENCAPSULATE_LEVEL;
        m_btSocketClass=null;
        m_cb_finish=null;
        m_cb_progressText=null;

        m_currentDataAP=null;

        m_workerThread=null;
        m_bCancel=false;

        m_idxLocalChannel=-1;

        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        Logger.i(m_className, "registerBroadcastReceiver");
        if (m_broadcastReceiver == null) {
            m_broadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (SP530_AppMcpCHelper.ACTION_DISCONNECTED_LOCALCHANNEL.equals(action)) {
                        Logger.i(m_className, "SP530_AppMcpCHelper.ACTION_DISCONNECTED_LOCALCHANNEL received");
                        if (!m_bCancel) {
                            if (m_workerThread!=null) {
                                synchronized (m_workerThread.m_status) {
                                    m_workerThread.m_status = TRANSACTIONSTATUS.STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED;
                                }
                            }
                        }
                    }

                }
            };
        }
        Activity act = (Activity) m_context;
        IntentFilter filter = new IntentFilter();
        // ACTION_DISCONNECTED_LOCALCHANNEL
        filter.addAction(SP530_AppMcpCHelper.ACTION_DISCONNECTED_LOCALCHANNEL);

        act.registerReceiver(m_broadcastReceiver, filter);
    }
    private void unregisterBroadcastReceiver() {
        Logger.i(m_className, "unregisterBroadcastReceiver");
        if (m_broadcastReceiver!=null) {
            Activity act = (Activity) m_context;
            act.unregisterReceiver(m_broadcastReceiver);
            m_broadcastReceiver=null;
        }
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

    /**
     * Set simulate TCP host
     */
    public void setSimulateTCPHost() {
        m_bTransactionSimulateTCPHost=true;
    }

    /**
     * Set always use CRC as checksum
     * @param flag true for always use CRC as checksum; false otherwise
     */
    public void setAlwaysUseCRCChecksum(boolean flag) {
        m_bAlwaysUseCRC=flag;
    }

    /**
     * Set logical channel one uses SSL
     * @param flag true for enablel false for disable
     */
    @Deprecated
    public void setLogicalChannelOneUseSSL(boolean flag) {
        setLocalChannelUseSSL(flag);
    }
    public void setLocalChannelUseSSL(boolean flag) {
        m_bLocalChannelUseSSL=flag;
    }

    /**
     * Set packet encapsulate level
     * @param encapsulateLevel packet encapsulate level
     */
    public void setPacketEncapsulateLevel(BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL encapsulateLevel) {
        m_packetEncapsulateLevel=encapsulateLevel;
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
        if (strCommandCode==null) {
            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
            strCommandCode=instByteHexHelper.byteToHexString(commandCode);
        }
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

        String strPacketEncap="";
        switch (m_packetEncapsulateLevel) {
            case RAW:
                strPacketEncap="RAW";
                break;
            case MCP:
                strPacketEncap="MCP";
                break;
        }
        Logger.i(m_className, "start called, packet encapsulate method: " + strPacketEncap);
        if (m_bTransactionSimulateTCPHost) {
            Logger.i(m_className, "start called, SimulateTCPHost is TRUE");
        }
        else {
            Logger.i(m_className, "start called, SimulateTCPHost is FALSE");
        }

        m_dataS3TransConf=new Data_setting_s3trans(m_context);
        m_cb_finish=cb_finish;

        m_idxLocalChannel=-1;

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

        private boolean m_bInputCommandRequestSend;

        public TransactionThread() {
            super();

            m_is=getInputStream();
            m_os=getOutputStream();
            m_packet_response=null;
            m_bCancel=false;

            m_status= TRANSACTIONSTATUS.STATUS_UNKNOWN;
            m_bInputCommandRequestSend=false;
        }

        public InputStream getInputStream() {
            if (m_btSocketClass==null) {
                Logger.e(m_className, "TransactionThread, getInputStream, m_btSocketClass is NULL");
                return null;
            }
            return m_btSocketClass.getInputStream();
        }

        public OutputStream getOutputStream() {
            if (m_btSocketClass==null) {
                Logger.e(m_className, "TransactionThread, getOutputStream, m_btSocketClass is NULL");
                return null;
            }
            return m_btSocketClass.getOutputStream();
        }

        private void setStatus(TRANSACTIONSTATUS status) {
            synchronized (m_status) {
                if (m_status!=TRANSACTIONSTATUS.STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED) {
                    m_status=status;
                }
            }
        }

        public void run() {
            setStatus(TRANSACTIONSTATUS.STATUS_START);
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

                        switch (m_packetEncapsulateLevel) {
                            case RAW: {
                                setStatus(TRANSACTIONSTATUS.STATUS_START_CLEARSTREAM);
                            }
                            break;
                            default: {
                                setStatus(TRANSACTIONSTATUS.STATUS_GET_LOCALCHANNEL_INDEX);
                            }
                            break;
                        }
                    }
                    break;

                    case STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED: {
                        Logger.w(m_className, "STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED, m_idxLocalChannel: "+m_idxLocalChannel);
                        synchronized (m_status) {
                            m_status=TRANSACTIONSTATUS.STATUS_GET_LOCALCHANNEL_INDEX;
                        }
                    }
                    break;

                    case STATUS_GET_LOCALCHANNEL_INDEX: {
                        String statusName="STATUS_GET_LOCALCHANNEL_INDEX";
                        Logger.i(m_className, statusName);

                        SP530_AppMcpCHelper instAppMcpCHelper=SP530_AppMcpCHelper.getInstance();
                        m_idxLocalChannel=instAppMcpCHelper.getLocalChannelIndex();
                        switch (m_packetEncapsulateLevel) {
                            case RAW: {
                            }
                            break;
                            default: {
                                long MAX_WAIT_TIMEINMS=(10*1000);
                                long tSlotInMS=500;
                                long tAccInMS=0;
                                int countSleep=0;
                                while ((m_idxLocalChannel<1)&&(tAccInMS<MAX_WAIT_TIMEINMS)) {
                                    if (m_bCancel) {
                                        setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL);
                                        return;
                                    }
                                    try {
                                        countSleep++;
                                        Logger.i(m_className, statusName+" Sleep "+countSleep+" for "+tSlotInMS+" ms");
                                        Thread.sleep(tSlotInMS);
                                        tAccInMS+=tSlotInMS;
                                    } catch (InterruptedException ie) {
                                        Logger.i(m_className, statusName+" Sleep exception, ie: "+ie.toString());
                                    }
                                    instAppMcpCHelper=SP530_AppMcpCHelper.getInstance();
                                    m_idxLocalChannel=instAppMcpCHelper.getLocalChannelIndex();
                                }
                            }
                            break;
                        }

                        Logger.i(m_className, statusName+" idx: "+m_idxLocalChannel);
                        if (m_idxLocalChannel<1) {
                            setStatus(TRANSACTIONSTATUS.STATUS_GET_LOCALCHANNEL_INDEX_FAIL);
                        }
                        else {
                            setStatus(TRANSACTIONSTATUS.STATUS_GET_LOCALCHANNEL_INDEX_SUCCESS);
                        }
                    }
                    break;

                    case STATUS_GET_LOCALCHANNEL_INDEX_FAIL: {
                        String statusName="STATUS_GET_LOCALCHANNEL_INDEX_FAIL";
                        Logger.i(m_className, statusName);
                        bExistLoop = true;
                    }
                    break;

                    case STATUS_GET_LOCALCHANNEL_INDEX_SUCCESS: {
                        String statusName="STATUS_GET_LOCALCHANNEL_INDEX_SUCCESS";
                        Logger.i(m_className, statusName);

                        // clear local channel buffer
                        SP530_AppMcpCHelper instAppMcpCHelper = SP530_AppMcpCHelper.getInstance();
                        instAppMcpCHelper.clearLocalChannelBuffer();

                        switch (m_packetEncapsulateLevel) {
                            case RAW: {
                                setStatus(TRANSACTIONSTATUS.STATUS_START_CLEARSTREAM);
                            }
                            break;
                            default: {
                                setStatus(TRANSACTIONSTATUS.STATUS_WAIT_COMM_READY);
                            }
                            break;
                        }
                    }
                    break;

                    case STATUS_START_CLEARSTREAM: {
                        processClearStream();
                        setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_SEND);
                    }
                    break;

                    case STATUS_WAIT_SSL_HANDSHAKE_COMPLETE: {
                        Logger.i(m_className, "STATUS_WAIT_SSL_HANDSHAKE_COMPLETE");

                        SSLServerHelper instSSLServerHelper=SSLServerHelper.getInstance();
                        boolean bValid=instSSLServerHelper.isValidConnection();

                        if (bValid) {
                            Logger.i(m_className, "STATUS_WAIT_SSL_HANDSHAKE_COMPLETE, SSL with key "+SSLServerHelper.KEY_LOCAL_COMMON+" is valid connection");
                            setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_SEND);
                            break;
                        }

                        boolean bHandShakeCompleted=false;

                        Data_SSLServerLocal dataSSLServerLocal;

                        dataSSLServerLocal=(Data_SSLServerLocal)instSSLServerHelper.m_sslServerHash.get(SSLServerHelper.KEY_LOCAL_COMMON);
                        if ( (dataSSLServerLocal!=null)&&(dataSSLServerLocal.m_acceptThread!=null) ) {
                            bHandShakeCompleted = ((SSLSocketServerThread) dataSSLServerLocal.m_acceptThread).isHandShakeCompleted();
                        }

                        long MAX_WAIT_TIMEINMS=(10*1000);
                        long tSlotInMS=500;
                        long tAccInMS=0;
                        int countSleep=0;
                        while ((!bHandShakeCompleted)&&(tAccInMS<MAX_WAIT_TIMEINMS)) {
                            if (m_bCancel) {
                                setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL);
                                return;
                            }
                            try {
                                countSleep++;
                                Logger.i(m_className, "STATUS_WAIT_SSL_HANDSHAKE_COMPLETE, Sleep "+countSleep+" for "+tSlotInMS+" ms");
                                Thread.sleep(tSlotInMS);
                                tAccInMS+=tSlotInMS;
                            } catch (InterruptedException ie) {
                                Logger.i(m_className, "STATUS_WAIT_SSL_HANDSHAKE_COMPLETE, Sleep exception, ie: "+ie.toString());
                            }
                            dataSSLServerLocal=(Data_SSLServerLocal)instSSLServerHelper.m_sslServerHash.get(SSLServerHelper.KEY_LOCAL_COMMON);
                            if ( (dataSSLServerLocal!=null)&&(dataSSLServerLocal.m_acceptThread!=null) ) {
                                bHandShakeCompleted = ((SSLSocketServerThread) dataSSLServerLocal.m_acceptThread).isHandShakeCompleted();
                            }
                        }

                        if (bHandShakeCompleted) {
                            setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_SEND);
                        }
                        else {
                            setStatus(TRANSACTIONSTATUS.STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL);
                        }
                    }
                    break;

                    case STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL: {
                        Logger.i(m_className, "STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL");
                        bExistLoop = true;
                    }
                    break;

                    case STATUS_WAIT_COMM_READY: {
                        Logger.i(m_className, "STATUS_WAIT_COMM_READY");

                        SP530_AppMcpCHelper instAppMcpCHelper=SP530_AppMcpCHelper.getInstance();

                        long MAX_WAIT_TIMEINMS=(10*1000);
                        long tSlotInMS=500;
                        long tAccInMS=0;
                        int countSleep=0;

                        byte a_mcp_ch=(byte)(m_idxLocalChannel&0xFF);
                        byte status = instAppMcpCHelper.mcp_get_status(a_mcp_ch);
                        int val = (status & CommConstant.K_LinkOnLine) & 0xFF;
                        while ((val == 0) && (tAccInMS < MAX_WAIT_TIMEINMS)) {
                            if (m_bCancel) {
                                setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL);
                                return;
                            }
                            try {
                                countSleep++;
                                Logger.i(m_className, "STATUS_WAIT_COMM_READY, mcp_ch: "+m_idxLocalChannel+", status: " + (status & 0xFF) + ", Sleep " + countSleep + " for " + tSlotInMS + " ms");
                                Thread.sleep(tSlotInMS);
                                tAccInMS += tSlotInMS;
                            } catch (InterruptedException ie) {
                                Logger.i(m_className, "STATUS_WAIT_COMM_READY, mcp_ch: "+m_idxLocalChannel+", Sleep exception, ie: " + ie.toString());
                            }

                            status = instAppMcpCHelper.mcp_get_status(a_mcp_ch);
                            val = (status & CommConstant.K_LinkOnLine) & 0xFF;
                        }

                        if (val != 0) {
                            if (m_bLocalChannelUseSSL) {
                                setStatus(TRANSACTIONSTATUS.STATUS_WAIT_SSL_HANDSHAKE_COMPLETE);
                            }
                            else {
                                setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_SEND);
                            }
                        } else {
                            setStatus(TRANSACTIONSTATUS.STATUS_WAIT_COMM_READY_FAIL);
                        }

                        if (m_status==TRANSACTIONSTATUS.STATUS_WAIT_COMM_READY_FAIL) {
                            Logger.i(m_className, "STATUS_WAIT_COMM_READY, mcp_ch: "+m_idxLocalChannel+", proto ready is FALSE");
                            break;
                        }
                    }
                    break;

                    case STATUS_WAIT_COMM_READY_FAIL: {
                        Logger.i(m_className, "STATUS_WAIT_COMM_READY_FAIL");
                        bExistLoop = true;
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
                        switch (m_packetEncapsulateLevel) {
                            case RAW: {
                                IOStreamHelper instIOStreamHelper=IOStreamHelper.getInstance();
                                bWrite=instIOStreamHelper.writeData(m_os, packetBuf);
                            }
                            break;
                            default: {
                                int max_try=3;
                                int count=0;
                                do {
                                    count++;
                                    Logger.i(m_className, "STATUS_REQUEST_SEND, try: "+count);

                                    SP530_AppMcpCHelper instAppMcpCHelper=SP530_AppMcpCHelper.getInstance();
                                    bWrite=instAppMcpCHelper.write2mcpOrThroughSSL(buf, (byte)(m_idxLocalChannel&0xFF));

                                    if ((!bWrite)&&(count<max_try)) {
                                        try {
                                            Thread.sleep(100);
                                        }
                                        catch (InterruptedException ie) {
                                        }
                                    }
                                }
                                while ((bWrite==false)&&(count<max_try));
                            }
                            break;
                        }

                        if (m_status== TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL) {
                            return;
                        }
                        if (bWrite) {
                            setStatus(TRANSACTIONSTATUS.STATUS_RESPONSE_GET);
                        }
                        else {
                            Logger.e(m_className, "send request fail");
                            setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_SEND_FAIL);
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
                            setStatus(TRANSACTIONSTATUS.STATUS_FINISH);
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
                        setStatus(TRANSACTIONSTATUS.STATUS_FINISH);
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

            // clear local channel buffer
            SP530_AppMcpCHelper instAppMcpCHelper = SP530_AppMcpCHelper.getInstance();
            instAppMcpCHelper.clearLocalChannelBuffer();

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
            final String strWaitingTime=ResourcesHelper.getBaseContextString(m_context, R.string.waiting_time);
            final String strSeconds=ResourcesHelper.getBaseContextString(m_context, R.string.seconds);

            long waitReponseTimeInMs=m_dataS3TransConf.m_waitReponseTimeInMs;
            switch (m_inputDataAP.m_commandCode) {
                case ApplicationProtocolConstant.S3INS_RESET: {
                    waitReponseTimeInMs=2*1000;
                }
                break;
            }

            switch (m_packetEncapsulateLevel) {
                case RAW: {
                    IOStreamHelper instIOStreamHelper=IOStreamHelper.getInstance();
                    byte[] buf=instIOStreamHelper.readDataWithWaitTime_IOStreamMethod(m_is, waitReponseTimeInMs, m_cb_progressText);
                    if (buf==null) {
                        if (m_status!=TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL) {
                            setStatus(status_fail_next);
                        }
                        return;
                    }
                    m_packet_response = new ArrayList<Byte>();
                    m_packet_response.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
                    setStatus(status_success_next);

                }
                break;
                default: {
                    long tAcc = 0;
                    long tSlot = 1000;

                    Logger.i(m_className, "getResponse wait START");

                    long tmpWaitTimeInS=waitReponseTimeInMs/1000;

                    SP530_AppMcpCHelper instAppMcpCHelper = SP530_AppMcpCHelper.getInstance();
                    int size_data = 0;
                    int count = 0;
                    do {

                        if (m_bCancel) {
                            break;
                        }

                        count++;

                        // check local channel buffer size
                        size_data = instAppMcpCHelper.getLocalChannelBufferSize();

                        if (size_data < 1) {
                            try {
                                Thread.sleep(tSlot);
                                tAcc += tSlot;

                                long tmpTWaitAccInS=tAcc/1000;
                                String strProgress=strWaitingTime+" "+tmpTWaitAccInS+" of "+tmpWaitTimeInS+" ("+strSeconds+")";
                                progressText2CallbackProgressText(strProgress);

                            } catch (InterruptedException ie) {
                            }
                        }
                    }
                    while ((size_data < 1) && (tAcc < waitReponseTimeInMs));

                    progressText2CallbackProgressText("");

                    Logger.i(m_className, "getResponse wait FINISH");

                    if (m_status == TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL) {
                        return;
                    }

                    if (size_data < 1) {
                        setStatus(status_fail_next);
                    } else {
                        byte[] buf;

                        // read local channel buffer
                        buf=instAppMcpCHelper.readLocalChannelBuffer();

                        m_packet_response = new ArrayList<Byte>();
                        m_packet_response.addAll(Arrays.asList(ArrayUtils.toObject(buf)));
                        setStatus(status_success_next);
                    }
                }
                break;
            }
        }

        public void progressText2CallbackProgressText(String strText) {
            if (m_cb_progressText!=null) {
                m_cb_progressText.setParameter(strText);
                try {
                    m_cb_progressText.call();
                }
                catch (Exception ex) {
                    Logger.w(m_className, "ex: " + ex.toString());
                }
            }
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
                act.showTransactionReceipt(byteBuffer, cb_showReceiptFinish);

                act.enableAllUIInputsRunOnUiThreadWithDelay(1000);

                // please wait
                setStatus(TRANSACTIONSTATUS.STATUS_WAIT_SHOW_TRANSRECEIPT);
            }
        }

        public void processResponse() {
            if (m_packet_response==null) {
                Logger.w(m_className, "processResponse, m_packet_response is NULL");
                setStatus(TRANSACTIONSTATUS.STATUS_FINISH);
                return;
            }

            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
            String strHex=instByteHexHelper.bytesArrayToHexString(m_packet_response);
            Logger.i(m_className, "processResponse, m_packet_response: "+strHex);

            ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
            ApplicationProtocolEnum.FORMAT_IDENTIFIER id_fi=instApplicationProtocolHelper.getFormatIdentifier(m_packet_response);
            TRANSACTIONSTATUS status;
            switch (id_fi) {
                case ZERO: {
                    status=processResponse_FI_ZERO();
                }
                break;
                case ONE: {
                    status=processResponse_FI_ONE();
                }
                break;
                default: {
                    Logger.i(m_className, "processData, DEFAUT (none)");
                    status=TRANSACTIONSTATUS.STATUS_FINISH;
                }
                break;
            }
            setStatus(status);
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
                        if (!Arrays.equals(m_currentDataAP.m_sequenceNumber, dataResponse.m_sequenceNumber)) {
                            status = TRANSACTIONSTATUS.STATUS_RESPONSE_GET;
                        }
                        else {
                            status = TRANSACTIONSTATUS.STATUS_FAIL;
                        }
                    }
                }
                else {
                    if (!Arrays.equals(m_currentDataAP.m_sequenceNumber, dataResponse.m_sequenceNumber)) {
                        status = TRANSACTIONSTATUS.STATUS_RESPONSE_GET;
                    }
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

                switch (m_packetEncapsulateLevel) {
                    case RAW: {
                        IOStreamHelper instIOStreamHelper=IOStreamHelper.getInstance();
                        instIOStreamHelper.writeData(m_os, responseData);
                    }
                    break;
                    default: {
//                        SP530_AppMcpCHelper instAppMcpCHelper = SP530_AppMcpCHelper.getInstance();
//                        if (m_bTransactionSimulateTCPHost) {
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException ie) {
//                                Logger.e(m_className, "processResponse_FI_ZERO, InterruptedException ie: " + ie.toString());
//                            }
//
//                            instAppMcpCHelper.write2mcp(buf, MCP.K_McpCh2);
//                        }
//                        else {
//                            instAppMcpCHelper.tcp_readAndWrite2Mcp(MCP.K_McpCh2);
//                        }
                        Logger.w(m_className, "processResponse_FI_ZERO, not support redirect to mcp host channel");
                    }
                    break;
                }
            }

            m_packet_response.clear();
            m_packet_response=null;

            TRANSACTIONSTATUS status=TRANSACTIONSTATUS.STATUS_RESPONSE_GET;
            return status;
        }

        public void cancel() {
            m_bCancel=true;
            setStatus(TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL);
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
                case STATUS_GET_LOCALCHANNEL_INDEX_FAIL:
                case STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL:
                case STATUS_REQUEST_SEND_FAIL:
                case STATUS_RESPONSE_GET_FAIL:
                case STATUS_FINISH:
                case STATUS_REQUEST_CANCEL:
                case STATUS_FAIL:
                case STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED:
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
        unregisterBroadcastReceiver();
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
