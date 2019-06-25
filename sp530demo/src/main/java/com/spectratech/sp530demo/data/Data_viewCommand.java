package com.spectratech.sp530demo.data;

import com.spectratech.lib.ByteHexHelper;

/**
 * Data_viewCommand - data class for view command
 */
public class Data_viewCommand {

    byte m_cmd;
    String m_description;

    public Data_viewCommand() {
        init();
    }

    public Data_viewCommand(byte cmd, String description) {
        init();
        m_cmd=cmd;
        m_description=description;
    }

    private void init() {
        m_cmd=(byte)0x00;
        m_description="";
    }

    @Override
    public String toString() {
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String strKey=instByteHexHelper.byteToHexString(m_cmd);
        String strRet=m_description+" - [0x"+strKey+"]";
        return strRet;
    }
}
