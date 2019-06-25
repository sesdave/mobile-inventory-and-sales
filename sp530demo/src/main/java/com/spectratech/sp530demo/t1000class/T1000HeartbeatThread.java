package com.spectratech.sp530demo.t1000class;

import com.spectratech.lib.IOStreamHelper;
import com.spectratech.lib.Logger;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * T1000HeartbeatThread - T1000 heartbeat thread class
 */
public class T1000HeartbeatThread extends Thread {

    private static final String m_className="T1000HeartbeatThread";

    private static final long HEARTBEAT_TIME_INTERVAL_INS=5;
    private static final String HEARTBEAT_MESSAGE="Alive";

    protected InputStream m_is;
    protected OutputStream m_os;

    public T1000HeartbeatThread(InputStream is, OutputStream os) {
        super();

        m_is=is;
        m_os=os;
    }

    public void run() {
        Logger.i(m_className, "T1000HeartbeatThread started");
        long heartbeat_time_interval_inms=HEARTBEAT_TIME_INTERVAL_INS*1000;
        int n_try=0;
        int MAX_TRY=3;
        int countWriteFail=0;
        while (!isInterrupted()) {
            IOStreamHelper instIOStreamHelper=IOStreamHelper.getInstance();
            boolean bWrite=false;
            byte[] buf=HEARTBEAT_MESSAGE.getBytes();
            n_try=0;
            do {
                n_try++;
                if (isInterrupted()) {
                    break;
                }
                bWrite = instIOStreamHelper.writeData(m_os, buf);
                if (bWrite) {
                    Logger.i(m_className, "T1000HeartbeatThread, send heartbeat: " + HEARTBEAT_MESSAGE);
                    countWriteFail=0;
                    try {
                        Thread.sleep(heartbeat_time_interval_inms);
                    }
                    catch (InterruptedException ie) {
                        Logger.w(m_className, "T1000HeartbeatThread, sleep InterruptedException ie");
                        interrupt();
                    }
                }
                else {
                    Logger.w(m_className, "T1000HeartbeatThread, instIOStreamHelper.writeData fail with try "+n_try);
                    countWriteFail++;
                }
            }
            while ( (!bWrite)&&(n_try<MAX_TRY) );

            if (countWriteFail>=MAX_TRY) {
                Logger.w(m_className, "T1000HeartbeatThread, instIOStreamHelper.writeData fail reach maximum count: "+countWriteFail);
                break;
            }
        }
    }

    public void cancel() {
        interrupt();
    }
}
