package com.spectratech.sp530demo.data;

import com.google.common.primitives.Bytes;
import java.util.Calendar;
import java.util.List;

/**
 * Data_logdatapacket - data class for log data packet
 */
public class Data_logdatapacket {

    /**
     * Variable to store log date time in timestamp
     */
    public long m_logDatetime;
    /**
     * Varaible to indicating the log message comes from android device sends to SP530
     */
    public boolean m_bFromSend;
    /**
     * Variable to store data
     */
    public byte[] m_dataBuf;

    /**
     * Constructor for Data_logdatapacket
     */
    public Data_logdatapacket() {
        init();
    }

    /**
     * Constructor for Data_logdatapacket
     * @param dataList input data
     * @param bFromSend flag to indcates the log message comes from android device sends to SP530
     */
    public Data_logdatapacket(List<Byte> dataList, boolean bFromSend) {
        init();
        long tInMs= Calendar.getInstance().getTimeInMillis();
        m_logDatetime=tInMs;
        m_bFromSend=bFromSend;
        // require Guava: Google Core Libraries
        byte[] byteBuffer = Bytes.toArray(dataList);
        cloneDataPacket(byteBuffer);
    }

    /**
     * Conctructor for Data_logdatapacket
     * @param dataBytes input data
     * @param bFromSend flag to indcates the log message comes from android device sends to SP530
     */
    public Data_logdatapacket(byte[] dataBytes, boolean bFromSend) {
        init();
        long tInMs= Calendar.getInstance().getTimeInMillis();
        m_logDatetime=tInMs;
        m_bFromSend=bFromSend;
        cloneDataPacket(dataBytes);
    }

    private void init() {
        m_logDatetime=-1;
        m_bFromSend=false;
        m_dataBuf=null;
    }

    /**
     * Get packet
     * @param dataList input data
     */
    public void cloneDataPacket(List<Byte> dataList) {
        // require Guava: Google Core Libraries
        byte[] byteBuffer = Bytes.toArray(dataList);
        cloneDataPacket(byteBuffer);
    }

    /**
     * Get packet
     * @param dataBytes input data
     */
    public void cloneDataPacket(byte[] dataBytes) {
        if ( (dataBytes!=null)&&(dataBytes.length>0) ) {
            m_dataBuf=new byte [dataBytes.length];
            System.arraycopy(dataBytes, 0, m_dataBuf, 0, dataBytes.length);
        }
    }
}
