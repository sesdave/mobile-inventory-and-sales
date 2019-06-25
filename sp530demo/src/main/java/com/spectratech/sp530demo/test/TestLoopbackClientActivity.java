package com.spectratech.sp530demo.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.spectratech.lib.BluetoothHelper;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.sp530.comm_protocol_c.CommCHelper;
import com.spectratech.lib.sp530.comm_protocol_c.T_BUFClass;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.BTPrinterBluetoothConnectActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.SP530DemoBaseActivity;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * TestLoopbackClientActivity - test loop back (client) activity
 */
public class TestLoopbackClientActivity extends SP530DemoBaseActivity {

    private static final String m_className="TestLoopbackServerActivity";

    private static final int REQUEST_ENABLE_BT=1001;

    private LinearLayout m_mainll;
    private TextView m_tv;
    private TextView m_tv2;

    private LoopbackThread m_loopbackThread;

    InputStream m_is;
    OutputStream m_os;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_context=this;

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_testloopbackclient, null);
        setContentView(m_mainll);

        m_tv=(TextView)m_mainll.findViewById(R.id.output);
        m_tv2=(TextView)m_mainll.findViewById(R.id.output2);

        BTPrinterBluetoothConnectActivity.m_BTEventsForActivity=null;

        BluetoothHelper instBluetoothHelper=BluetoothHelper.getInstance();
        if (!instBluetoothHelper.isEnableBluetooth()) {
            instBluetoothHelper.requestEnableBluetooth(this, REQUEST_ENABLE_BT);
        }
    }

    public void startBTPrinterBluetoothConnectActivityWithNoAnimation() {
        Application.startActivity(m_context, Application.ACTIVITY_BTPRINTER_BLUETOOTH_CONNECT);
        ((Activity) m_context).overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_loopbackThread!=null) {
            m_loopbackThread.cancel();
            m_loopbackThread=null;
        }
    }

    public class LoopbackThread extends Thread {
        private final String m_className = "LoopbackThread";

        private CommCHelper m_commHelper;

        private boolean m_bCancel;

        private int m_count;
        private static final int m_max_count=2048;

        private int m_count_success;
        private int m_count_fail;

        public LoopbackThread() {
            m_bCancel=false;

            m_count=0;
            //m_count=1500;

            m_count_success=0;
            m_count_fail=0;
            m_commHelper=new CommCHelper();
            m_commHelper.comm_start(m_is, m_os);
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

        public void setText2(final String strText) {
            if (m_tv2!=null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_tv2.setText(strText);
                    }
                });
            }
        }

        public void run() {
            Logger.i(m_className, "Thread start looping");
            while ((m_count<m_max_count)&&(!m_bCancel)) {
                m_count++;
                byte[] data=new byte [m_count];
                for (int i=0; i<m_count; i++) {
                    data[i]=(byte)(i&0xFF);
                }
                T_BUFClass buf=new T_BUFClass();
                buf.add2tail_sbuf(data);
                buf.d_len=m_count;

                byte[] check=buf.buf_top_getClonedData();
                ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                String strHex=instByteHexHelper.bytesArrayToHexString(check);
                strHex="";

                boolean bWrite=m_commHelper.comm_write(buf);
                if (bWrite) {
                    setText("write " + m_count + " byte data SUCCESS: "+strHex);
                }
                else {
                    setText("write " + m_count + " byte data FAIL: "+strHex);
                }

                long tMaxReadWaitInMs=500;
                long tWaitAccInMs=0;
                do {
                    buf = m_commHelper.comm_read();
                    if (buf==null) {
                        tWaitAccInMs+=10;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ie) {
                        }
                    }
                }
                while ( (buf==null)&&(tWaitAccInMs<tMaxReadWaitInMs) );

                if (buf!=null) {
                    byte[] receivedata=buf.buf_top_getClonedData();
                    if (Arrays.equals(data, receivedata)) {
                        setText("read " + m_count + " byte data SUCCESS: "+strHex);
                        m_count_success++;
                    }
                    else {
                        setText("read " + m_count + " byte data FAIL: "+strHex);
                        m_count_fail++;
                    }
                }
                else {
                    m_count_fail++;
                }

                setText2("Success: "+m_count_success+"\nFail: "+m_count_fail);

                try {
                    Thread.sleep(5);
                }
                catch (InterruptedException ie) {
                }
            }

            m_commHelper.comm_stop();
            BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.safeFreeBTSocket();
        }

        public void cancel() {
            Logger.i(m_className, "cancel called");
            m_bCancel=true;
        }


    }

    public void onClick_btprinter(View v) {
        startBTPrinterBluetoothConnectActivityWithNoAnimation();
    }

    public void onClick_start(View v) {
        BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
        if (btSocketClass==null) {
            return;
        }
        m_is=btSocketClass.getInputStream();
        if (m_is==null) {
            return;
        }
        m_os=btSocketClass.getOutputStream();
        if (m_os==null) {
            return;
        }

        if (m_loopbackThread!=null) {
            m_loopbackThread.cancel();
            m_loopbackThread=null;
        }
        m_loopbackThread = new LoopbackThread();
        m_loopbackThread.start();
    }
}
