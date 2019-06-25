package com.spectratech.sp530demo.data;

import android.net.Uri;
import com.spectratech.lib.Callback;

/**
 * Data_executeCommand_filedl - data class for executing file download command
 */
public class Data_executeCommand_filedl extends Data_executeCommand {

    public Uri m_uri_file;
    public Callback m_cb_progress;

    public Data_executeCommand_filedl() {
        super();

        m_uri_file=null;
        m_cb_progress=null;
    }

}
