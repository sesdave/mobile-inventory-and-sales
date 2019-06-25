package com.spectratech.spectratechlib_ftp.data;

import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;

/**
 * FTP download object
 */
public class Data_ftpdlObject {

    private static final String m_className="Data_ftpdlObject";

    public int m_id;
    public boolean m_bForceDownload;
    public boolean m_bEnable;
    public boolean m_bSkip;
    public boolean m_bDownloadSuccess;
    public String m_relative_pathlist;

    public Callback<Object> m_cb_finishdl;

    public Data_ftpdlObject() {
        init();
    }

    public Data_ftpdlObject(Data_ftpdlObject dataFtpdlObject) {
        init();
        if (dataFtpdlObject==null) {
            Logger.w(m_className, "Data_ftpdlObject, dataFtpdlObject is null");
            return;
        }

        m_id=dataFtpdlObject.m_id;
        m_bForceDownload=dataFtpdlObject.m_bForceDownload;
        m_bEnable=dataFtpdlObject.m_bEnable;
        m_bSkip=dataFtpdlObject.m_bSkip;
        m_bDownloadSuccess=dataFtpdlObject.m_bDownloadSuccess;
        m_relative_pathlist=dataFtpdlObject.m_relative_pathlist;
        m_cb_finishdl=dataFtpdlObject.m_cb_finishdl;
    }

    public Data_ftpdlObject(String relative_pathlist) {
        init();
        m_relative_pathlist=relative_pathlist;
    }

    public Data_ftpdlObject(String relative_pathlist, int id) {
        this(relative_pathlist, id, false, null);
    }
    public Data_ftpdlObject(String relative_pathlist, int id, boolean bForceDownload, Callback<Object> cb_finishdl) {
        init();
        m_relative_pathlist=relative_pathlist;
        m_id=id;
        m_cb_finishdl=cb_finishdl;
        m_bForceDownload=bForceDownload;
    }

    private void init() {
        m_id=-1;
        m_bEnable=true;
        m_bSkip=false;
        m_bDownloadSuccess=false;
        m_relative_pathlist="";
        m_cb_finishdl=null;
        m_bForceDownload=false;
    }
}
