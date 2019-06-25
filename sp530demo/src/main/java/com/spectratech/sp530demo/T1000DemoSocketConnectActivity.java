package com.spectratech.sp530demo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.Callback;
import com.spectratech.lib.CustomBaseActivity;
import com.spectratech.lib.Logger;
import com.spectratech.lib.NetworkHelper;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.tcpip.TCPHelper;
import com.spectratech.lib.tcpip.TCPIPSocketClass;
import com.spectratech.lib.tcpip.data.Data_tcpip_v4;
import com.spectratech.sp530demo.data.Data_setting_devicet1000;
import com.spectratech.sp530demo.t1000class.T1000HeartbeatThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * T1000DemoSocketConnectActivity - activity to connect T1000 tcp socket
 */
public class T1000DemoSocketConnectActivity extends CustomBaseActivity {

    private static final String m_className="T1000DemoSocketConnectActivity";

    private final int MSG_HANDLER_REFRESHUI_STATUS              =1102;

    private Handler m_handler;

    private LinearLayout m_mainll;
    private LinearLayout m_socketconnectPanelConnectionll;
    private TextView m_statustv;
    private TextView m_messageSinglelinetv;

    private TextView m_titleWifiConnectTV;

    private TextView m_localIp_tv;
    private EditText m_ipone_et;
    private EditText m_iptwo_et;
    private EditText m_ipthree_et;
    private EditText m_ipfour_et;
    private EditText m_port_et;

    private Data_setting_devicet1000 m_dataSettingT1000;

    public static Data_tcpip_v4 m_dataTcpIpV4;

    private Toast m_toast;

    /**
     * Heartbeat thread for sending T1000 heartbeat
     */
    private static T1000HeartbeatThread m_heartbeatThread;

    /**
     * OnCreate function
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //initBTEventsForActivityInstance(this);
        super.onCreate(savedInstanceState);

        m_activitycommmon = new SP530DemoBaseActivityCommonClass(m_context);
        m_activitycommmon.onCreate(savedInstanceState, this);

        initHandler();

        m_mainll = (LinearLayout) LinearLayout.inflate(m_context, R.layout.activity_t1000socketconnect, null);
        setContentView(m_mainll);
        initUIs();

        m_dataSettingT1000=new Data_setting_devicet1000(m_context);

        setTitle("T1000");
        loadLocalIpInfo();
        loadIPSocketInfo();

        m_toast=null;
    }

    private void initHandler() {
        m_handler=new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_HANDLER_REFRESHUI_STATUS:
                        String strPrefix=(String)msg.obj;
                        Logger.i(m_className, "m_handler, MSG_HANDLER_REFRESHUI_STATUS, " + strPrefix);
                        updateUI_SocketStatus(strPrefix);
                        break;
                }
            }
        };
    }

    private void loadLocalIpInfo() {
        NetworkHelper instNetworkHelper=NetworkHelper.getInstance();
        String strLocalIP=instNetworkHelper.getIPAddressStringFromWifi(m_context);
        m_localIp_tv.setText(strLocalIP);
    }

    private void loadIPSocketInfo() {
        m_ipone_et.setText("" + m_dataSettingT1000.m_ipArray[0]);
        m_iptwo_et.setText("" + m_dataSettingT1000.m_ipArray[1]);
        m_ipthree_et.setText("" + m_dataSettingT1000.m_ipArray[2]);
        m_ipfour_et.setText("" + m_dataSettingT1000.m_ipArray[3]);
        m_port_et.setText("" + m_dataSettingT1000.m_port);
    }

    private void initUIs() {
        m_socketconnectPanelConnectionll=(LinearLayout)m_mainll.findViewById(R.id.socketconnect_panel_connection);
        m_statustv=(TextView)m_mainll.findViewById(com.spectratech.lib.R.id.tv_btStatus);
        m_messageSinglelinetv=(TextView)m_mainll.findViewById(com.spectratech.lib.R.id.message_singleline);
        m_titleWifiConnectTV=(TextView)m_mainll.findViewById(com.spectratech.lib.R.id.title_wifi_connect);

        m_localIp_tv=(TextView)m_mainll.findViewById(R.id.param_local_ip);

        m_ipone_et=(EditText)m_mainll.findViewById(R.id.val_ipone);
        m_iptwo_et=(EditText)m_mainll.findViewById(R.id.val_iptwo);
        m_ipthree_et=(EditText)m_mainll.findViewById(R.id.val_ipthree);
        m_ipfour_et=(EditText)m_mainll.findViewById(R.id.val_ipfour);
        m_port_et=(EditText)m_mainll.findViewById(R.id.val_port);

        refreshUI_SocketStatus();

        int col=m_context.getResources().getColor(R.color.color_88);
        m_ipone_et.setTextColor(col);
        m_iptwo_et.setTextColor(col);
        m_ipthree_et.setTextColor(col);
        m_ipfour_et.setTextColor(col);
        m_port_et.setTextColor(col);
    }

    /**
     * Set the title of this activity
     * @param val title string
     */
    public void setTitle(String val) {
        if (m_titleWifiConnectTV!=null) {
            if (val!=null) {
                m_titleWifiConnectTV.setText(val);
            }
        }
    }

    public static boolean isSocketConnected() {
        boolean bRet=false;
        if (m_dataTcpIpV4==null) {
            Logger.i(m_className, "isSocketConnected, m_dataTcpIpV4 is NULL");
            return bRet;
        }
        TCPIPSocketClass socketClass=m_dataTcpIpV4.m_tcpipSocketClass;
        if (socketClass==null) {
            Logger.i(m_className, "isSocketConnected, socketClass is NULL");
            return bRet;
        }
        if (!socketClass.isSocketConnected()) {
            Logger.i(m_className, "isSocketConnected, socketClass.isSocketConnected is FALSE");
            return bRet;
        }

        bRet=true;
        return bRet;
    }

    /**
     * Refresh TextView text which shown the connection status of Socket
     */
    public void refreshUI_SocketStatus() {
        boolean bConnected=isSocketConnected();
        String strPrefix="";
        if (bConnected) {
            String strConnected= ResourcesHelper.getBaseContextString(m_context, R.string.connected);
            strPrefix=strConnected;
        }
        else {
            String strDisconnected=ResourcesHelper.getBaseContextString(m_context, R.string.disconnected);
            strPrefix=strDisconnected;
        }
        updateUI_SocketStatus(strPrefix);
    }
    private void updateUI_SocketStatus(String strPrefix) {
        if (strPrefix==null) {
            strPrefix="";
        }

        Data_tcpip_v4 dataTcpIPV4=m_dataTcpIpV4;

        if (dataTcpIPV4!=null) {
            String strMiddle="";
            if (!strPrefix.equals("")) {
                strMiddle=" - ";
            }
            String strConcat=dataTcpIPV4.getIPString()+":"+dataTcpIPV4.m_port;
            setTVStatus(strPrefix + strMiddle + strConcat);
        }
        else {
            setTVStatus("-");
        }
    }
    private void updateUI_SocketStatusByHandler(String strPrefix) {
        Message msg=new Message();
        msg.what=MSG_HANDLER_REFRESHUI_STATUS;
        msg.obj=strPrefix;
        m_handler.sendMessage(msg);
    }
    private void setTVStatus() {
        setTVStatus("");
    }
    private void setTVStatus(String strText) {
        setTV(m_statustv, strText);
    }
    private void setTV(TextView tv, String strText) {
        if (tv!=null) {
            tv.setText(strText);
        }
    }


    /**
     * Function used for onclick of Translucent view
     * @param v Corresponding onclick view
     */
    public void onClick_translucent(View v) {
        endActivity();
    }

    private void ToastRunOnUiThread(final String msg) {
        ((Activity)m_context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (m_toast!=null) {
                    m_toast.cancel();
                    m_toast=null;
                }
                m_toast=Toast.makeText(m_context, msg, Toast.LENGTH_SHORT);
                m_toast.show();
            }
        });
    }

    private boolean safeStartHeartbeatThread() {
        boolean bRet=false;
        if (m_dataTcpIpV4==null) {
            Logger.w(m_className, "safeStartHeartbeatThread, m_dataTcpIpV4 is NULL");
            return bRet;
        }
        TCPIPSocketClass tcpipSocketClass=m_dataTcpIpV4.m_tcpipSocketClass;
        if (tcpipSocketClass==null) {
            Logger.w(m_className, "safeStartHeartbeatThread, tcpipSocketClass is NULL");
            return bRet;
        }
        Socket socket=m_dataTcpIpV4.m_tcpipSocketClass.getSocket();
        if (socket==null) {
            Logger.w(m_className, "safeStartHeartbeatThread, socket is NULL");
            return bRet;
        }
        InputStream is=null;
        OutputStream os=null;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        }
        catch (IOException io) {
            Logger.w(m_className, "io: " + io.toString());
            is=null;
            os=null;
        }
        if ( (is==null)||(os==null) ) {
            Logger.w(m_className, "safeStartHeartbeatThread, is or os is NULL");
            return bRet;
        }

        safeFreeHeartbeatThread();
        m_heartbeatThread = new T1000HeartbeatThread(is, os);
        m_heartbeatThread.start();
        bRet=true;
        return bRet;
    }

    private void connect2TCP() {
        Data_tcpip_v4 dataTcpIPv4=m_dataSettingT1000.convert2DataTcpIpV4();

        int timeoutInS=m_dataSettingT1000.m_connectionTimeoutInS;
        this.showProgressDialogAndDismissInTimeSlot(timeoutInS * 1000);

        String key=m_dataSettingT1000.getName();

        final T1000DemoSocketConnectActivity fact=this;
        Callback<Object> cb_finish=new Callback<Object>() {
            @Override
            public Object call() throws Exception {
                fact.dismissProgressDialogRunOnUiThread();

                String strConnect=ResourcesHelper.getBaseContextString(m_context, R.string.connect);
                String strDisconnected=ResourcesHelper.getBaseContextString(m_context, R.string.disconnected);

                Object obj=this.getParameter();
                if (obj instanceof Data_tcpip_v4) {
                    Data_tcpip_v4 data=(Data_tcpip_v4)obj;
                    if (data.m_tcpipSocketClass==null) {
                        Logger.i(m_className, "onClick_socketconnect, data.m_tcpipSocketClass is NULL");
                        String strMsg=ResourcesHelper.getBaseContextString(m_context, R.string.connection_fail_nosocobj_found);
                        ToastRunOnUiThread(strMsg+"!");
                        updateUI_SocketStatusByHandler(strDisconnected);
                        return null;
                    }
                    if (!data.m_tcpipSocketClass.isSocketConnected()) {
                        Logger.i(m_className, "onClick_socketconnect, data.m_tcpipSocketClass is NULL");
                        String strMsg=ResourcesHelper.getBaseContextString(m_context, R.string.connection_fail_soc_isnotconnect);
                        ToastRunOnUiThread(strMsg+"!");
                        updateUI_SocketStatusByHandler(strDisconnected);
                        return null;
                    }
                    m_dataTcpIpV4=data;
                    ToastRunOnUiThread(strConnect+" to: " + m_dataSettingT1000.getName());
                    String strConnected=ResourcesHelper.getBaseContextString(m_context, R.string.connected);
                    updateUI_SocketStatusByHandler(strConnected);

                    boolean bStartedThread=safeStartHeartbeatThread();
                    if (!bStartedThread) {
                        Logger.w(m_className, "connect2TCP, callback, bStartedThread is FALSE");
                    }

                    return null;
                }

                Logger.i(m_className, "onClick_socketconnect, obj instanceof Data_tcpip_v4 is FALSE");
                String strConFail=ResourcesHelper.getBaseContextString(m_context, R.string.connection_fail);
                ToastRunOnUiThread(strConFail+"!");
                updateUI_SocketStatusByHandler(strDisconnected);
                return null;
            };
        };

        TCPHelper instTCPHelper=TCPHelper.getInstance();
        instTCPHelper.tcp_connect(key, dataTcpIPv4, cb_finish);
    }

    private void disconnectFromTcp() {
        String strDisconnected=ResourcesHelper.getBaseContextString(m_context, R.string.disconnected);
        String key=m_dataSettingT1000.getName();
        TCPHelper instTCPHelper=TCPHelper.getInstance();
        instTCPHelper.tcp_disconnect(key);
        if (m_toast!=null) {
            m_toast.cancel();
            m_toast=null;
        }
        String strDisconnectFrom=ResourcesHelper.getBaseContextString(m_context, R.string.disconnect_from);
        m_toast=Toast.makeText(m_context, strDisconnectFrom+": "+m_dataSettingT1000.getName(), Toast.LENGTH_SHORT);
        m_toast.show();
        updateUI_SocketStatus(strDisconnected);

        safeFreeHeartbeatThread();
    }

    /**
     * Function used for onclick of socket connect button
     * @param v Corresponding onclick view
     */
    public void onClick_socketconnect(View v) {
        connect2TCP();
    }

    /**
     * Function used for onclick of socket disconnect button
     * @param v Corresponding onclick view
     */
    public void onClick_socketdisconnect(View v) {
        disconnectFromTcp();
    }


    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
        endActivity();
    }

    private void endActivityWithResultOK() {
        Intent returnIntent = new Intent();
        //returnIntent.putExtra("result",result);
        setResult(Activity.RESULT_OK,returnIntent);
        endActivity();
    }
    private void endActivity() {
        this.finish();
        overridePendingTransition(0, 0);
    }

    private static void safeFreeHeartbeatThread() {
        if (m_heartbeatThread!=null) {
            Logger.i(m_className, "safeFreeHeartbeatThread called");
            m_heartbeatThread.cancel();
            m_heartbeatThread=null;
        }
    }

    public static void safeFreeAll() {
        Logger.i(m_className, "safeFreeAll called");
        if (m_dataTcpIpV4!=null) {
            m_dataTcpIpV4=null;
        }
        if (m_heartbeatThread!=null) {
            m_heartbeatThread.cancel();
            m_heartbeatThread=null;
        }
    }
}
