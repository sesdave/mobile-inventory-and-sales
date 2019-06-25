package com.spectratech.sp530demo.data;

import com.spectratech.lib.Callback;
import com.spectratech.spectratechlib_ftp.data.Data_ftpdlObject;
import java.util.ArrayList;

/**
 * Data_executeCommand_tmsdl - data class for executing TMS download command
 */
public class Data_executeCommand_tmsdl extends Data_executeCommand {

    public ArrayList<Data_ftpdlObject> m_listFtpdlObj;
    public String m_filePath;
    public Callback m_cb_progress;

    public Data_executeCommand_tmsdl() {
        super();

        m_listFtpdlObj=null;
        m_filePath="";
        m_cb_progress=null;
    }

}
