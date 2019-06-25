package com.spectratech.sp530demo;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.BluetoothHelper;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.bluetooth.BluetoothAcceptConnectThread;
import com.spectratech.lib.bluetooth.BluetoothConnectConstant;
import com.spectratech.lib.sp530.comm_protocol_c.CommCHelper;
import com.spectratech.lib.sp530.comm_protocol_c.T_BUFClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Test loop back server activity
 */
public class TestLoopbackServerActivity extends SP530DemoBaseActivity {

    private static final String m_className="TestLoopbackServerActivity";

    private static final int REQUEST_ENABLE_BT=1001;

    private LinearLayout m_mainll;
    private TextView m_tv;

    private BluetoothAcceptConnectThread m_btAcceptConnectThread;
    private Callback<Object> m_cbmanageConnectedSocket;

    BluetoothSocket m_btSocket;
    InputStream m_is;
    OutputStream m_os;

    private LoopbackThread m_loopbackThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_context=this;

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_testloopbackserver, null);
        setContentView(m_mainll);

        m_tv=(TextView)m_mainll.findViewById(R.id.output);

        m_cbmanageConnectedSocket=new Callback<Object>() {
            @Override
            public Object call() throws Exception {
                String strConnected= ResourcesHelper.getBaseContextString(m_context, R.string.connected);
                setText(strConnected);

                Object obj=getParameter();
                m_btSocket=(BluetoothSocket)obj;
                m_is=m_btSocket.getInputStream();
                m_os=m_btSocket.getOutputStream();

                Logger.i(m_className, "m_cbmanageConnectedSocket callback called");

                m_loopbackThread=new LoopbackThread();
                m_loopbackThread.start();

                return null;
            }
        };

        BluetoothHelper instBluetoothHelper=BluetoothHelper.getInstance();
        if (!instBluetoothHelper.isEnableBluetooth()) {
            instBluetoothHelper.requestEnableBluetooth(this, REQUEST_ENABLE_BT);
        }
        else {
            startServer();
        }

    }

    public void setText(final String strText) {
        if (m_tv!=null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_tv.setText(strText);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_loopbackThread!=null) {
            m_loopbackThread.cancel();
            m_loopbackThread=null;
        }
    }

    public void onClick_discoverable(View v) {
        //Toast.makeText(m_context, "onClick_discoverable", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                String strEnableBluetT="Bluetooth enabled";
                Toast.makeText(m_context, strEnableBluetT, Toast.LENGTH_SHORT).show();
                startServer();
            }
        }
    }

    private void startServer() {
        BluetoothHelper instBluetoothHelper=BluetoothHelper.getInstance();
        instBluetoothHelper.setBluetoothDiscoverability(m_context);

        m_btAcceptConnectThread=new BluetoothAcceptConnectThread("BTServer", BluetoothConnectConstant.DEFAULT_UUID_STRING);
        m_btAcceptConnectThread.setCallbackManageConnectedSocket(m_cbmanageConnectedSocket);
        m_btAcceptConnectThread.start();

        String strEnableBluetT="Start server";
        Toast.makeText(m_context, strEnableBluetT, Toast.LENGTH_SHORT).show();
    }

    public class LoopbackThread extends Thread {
        private final String m_className = "LoopbackThread";

        private CommCHelper m_commHelper;

        private boolean m_bCancel;

        public LoopbackThread() {
            m_bCancel=false;
            m_commHelper=new CommCHelper();
            m_commHelper.comm_start(m_is, m_os);
        }

        public void run() {
            Logger.i(m_className, "Thread start looping");
            while ((true)&&(!m_bCancel)) {
                T_BUFClass buf=m_commHelper.comm_read();
                if (buf!=null) {
                    buf.printDebugInfo("LoopbackThread", "");

                    byte[] data=buf.buf_top_getClonedData();
                    ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                    String strHex=instByteHexHelper.bytesArrayToHexString(data);
//                    setText("strHex: "+strHex);

                    setText("Read "+buf.d_len+" bytes");

                    m_commHelper.comm_write(buf);
                }
            }

            m_commHelper.comm_stop();
            safeFreeBTSocket();
        }

        public void safeFreeBTSocket() {
            Logger.i(m_className, "safeFreeBTSocket");
            if (m_btSocket!=null) {
                try {
                    m_btSocket.close();
                }
                catch (IOException io) {

                }
                m_btSocket = null;
            }
            if (m_is!=null) {
                try {
                    m_is.close();
                }
                catch (IOException io) {

                }
                m_is = null;
            }
            if (m_os!=null) {
                try {
                    m_os.close();
                }
                catch (IOException io) {

                }
                m_os = null;
            }
        }

        public void cancel() {
            Logger.i(m_className, "cancel called");
            m_bCancel=true;
        }


    }
}
