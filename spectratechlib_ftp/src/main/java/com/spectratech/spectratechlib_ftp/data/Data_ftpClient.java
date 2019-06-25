package com.spectratech.spectratechlib_ftp.data;

import com.spectratech.spectratechlib_ftp.FtpClientHelper;

/**
 * FTP Client Object
 */
public class Data_ftpClient {
    private static final String m_className="Data_ftpClient";

    public String m_host;
    public int m_port;
    public boolean m_bPassiveMode;
    public String m_username;
    public String m_password;
    public int m_connectTimeoutInMs;

    public Data_ftpClient() {
        init();
    }

    public Data_ftpClient(String host, int port, boolean bPassiveMode, String username, String password) {
        init();
        m_host=host;
        m_port=port;
        m_bPassiveMode=bPassiveMode;
        m_username=username;
        m_password=password;
    }

    private void init() {
        m_host="";
        m_port= FtpClientHelper.DEFAULT_FTP_PORT;
        m_bPassiveMode=false;
        m_username="";
        m_password="";
        m_connectTimeoutInMs=20*1000;
    }
}
