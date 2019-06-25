package com.spectratech.sp530demo.data;

import com.google.common.primitives.Bytes;
import com.spectratech.lib.ByteHexHelper;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data_device_mcpchcfg - data class for sending mcp config to device
 */
public class Data_device_mcpchcfg {
    private static final String m_className="Data_device_mcpchcfg";

    Data_device_a_mcpch[] m_chArray;

    public class Data_device_a_mcpch {
        public byte[] m_ip;
        public byte[] m_port;
        public byte m_timeout;
        public byte m_dummy;
        public byte m_flagSsl;
        public byte[] m_sslKeyIndexZero;
        public byte[] m_caKeyIndex;
        public byte[] m_clientCertKeyIndex;

        public Data_device_a_mcpch() {
            byte valByte=0;
            m_ip=new byte [4];
            m_port=new byte [2];
            m_timeout=valByte;
            m_dummy=valByte;
            m_flagSsl=valByte;
            m_sslKeyIndexZero=new byte [4];
            m_caKeyIndex=new byte [4];
            m_clientCertKeyIndex=new byte [4];
        }
    }

    public void initSP530() {
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        int idx=0;
        {
            String oneSslKeyIndex="00000000";
            byte[] tmp = instByteHexHelper.hexStringToByteArray(oneSslKeyIndex);
            System.arraycopy(tmp, 0, m_chArray[idx].m_sslKeyIndexZero, 0, tmp.length);
        }
        {
            String oneCaKeyIndex="10CA0000";
            byte[] tmp = instByteHexHelper.hexStringToByteArray(oneCaKeyIndex);
            System.arraycopy(tmp, 0, m_chArray[idx].m_caKeyIndex, 0, tmp.length);
        }
        {
            String oneClientCertKeyIndex="00CA0000";
            byte[] tmp = instByteHexHelper.hexStringToByteArray(oneClientCertKeyIndex);
            System.arraycopy(tmp, 0, m_chArray[idx].m_clientCertKeyIndex, 0, tmp.length);
        }

        idx=1;
        {
            String oneSslKeyIndex="00000000";
            byte[] tmp = instByteHexHelper.hexStringToByteArray(oneSslKeyIndex);
            System.arraycopy(tmp, 0, m_chArray[idx].m_sslKeyIndexZero, 0, tmp.length);
        }
        {
            String oneCaKeyIndex="11CA0000";
            byte[] tmp = instByteHexHelper.hexStringToByteArray(oneCaKeyIndex);
            System.arraycopy(tmp, 0, m_chArray[idx].m_caKeyIndex, 0, tmp.length);
        }
        {
            String oneClientCertKeyIndex="00CA0000";
            byte[] tmp = instByteHexHelper.hexStringToByteArray(oneClientCertKeyIndex);
            System.arraycopy(tmp, 0, m_chArray[idx].m_clientCertKeyIndex, 0, tmp.length);
        }
    }

    public Data_device_mcpchcfg() {
        m_chArray=new Data_device_a_mcpch [2];
        for (int i=0; i<m_chArray.length; i++) {
            m_chArray[i]=new Data_device_a_mcpch();
        }
    }

    public void setIP(int idx, byte[] ipArray) {
        Data_device_a_mcpch x=m_chArray[idx];
        System.arraycopy(ipArray, 0, x.m_ip, 0, 4);
    }

    public void setPort(int idx, byte[] dataArray) {
        Data_device_a_mcpch x=m_chArray[idx];
        System.arraycopy(dataArray, 0, x.m_port, 0, 2);
    }

    public void setTimeout(int idx, byte data) {
        Data_device_a_mcpch x=m_chArray[idx];
        x.m_timeout=data;
    }

    public void setEnableSSL(int idx, byte data) {
        Data_device_a_mcpch x=m_chArray[idx];
        x.m_flagSsl=data;
    }

    public byte[] getByteData() {
        List<Byte> list=new ArrayList<Byte>();

        for (int i=0; i<m_chArray.length; i++) {
            Data_device_a_mcpch x=m_chArray[i];

            list.addAll(Arrays.asList(ArrayUtils.toObject(x.m_ip)));
            list.addAll(Arrays.asList(ArrayUtils.toObject(x.m_port)));
            list.add(x.m_timeout);
            list.add(x.m_dummy);

            byte[] sslKeyIndexZero=new byte [x.m_sslKeyIndexZero.length];
            System.arraycopy(x.m_sslKeyIndexZero, 0, sslKeyIndexZero, 0, sslKeyIndexZero.length);
            if ((x.m_flagSsl&0xFF)>0) {
                sslKeyIndexZero[0] = (byte)0x01;
            }
            else {
                sslKeyIndexZero[0] = (byte)0x00;
            }
            list.addAll(Arrays.asList(ArrayUtils.toObject(sslKeyIndexZero)));

            list.addAll(Arrays.asList(ArrayUtils.toObject(x.m_caKeyIndex)));
            list.addAll(Arrays.asList(ArrayUtils.toObject(x.m_clientCertKeyIndex)));
        }

        byte[] result= Bytes.toArray(list);
        return result;
    }
}
