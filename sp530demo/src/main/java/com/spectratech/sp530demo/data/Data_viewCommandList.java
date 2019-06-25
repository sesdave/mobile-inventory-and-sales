package com.spectratech.sp530demo.data;

import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Data_viewCommandList - data class for listing view commands
 */
public class Data_viewCommandList {

    public LinkedHashMap<Byte, String> m_commandListHash;
    public ArrayList<Data_viewCommand> m_list;

    public Data_viewCommandList() {
        init();
    }

    private void init() {
        m_commandListHash=new LinkedHashMap<Byte, String>(ApplicationProtocolConstant.S3RC_COMMAND_MAP);

        // remove those functions which are supported in other pages
        m_commandListHash.remove(ApplicationProtocolConstant.S3INS_INIT_MODE);
        m_commandListHash.remove(ApplicationProtocolConstant.S3INS_INIT_AUTH);
        m_commandListHash.remove(ApplicationProtocolConstant.S3INS_MUTU_AUTH);
        m_commandListHash.remove(ApplicationProtocolConstant.S3INS_GEN_KEY);

        m_list=new ArrayList<Data_viewCommand>();
        for(Entry<Byte, String> entry : m_commandListHash.entrySet()) {
            Byte cmd=entry.getKey();
            String val=entry.getValue();
            Data_viewCommand x=new Data_viewCommand(cmd, val);
            m_list.add(x);
        }
    }

    public ArrayList<Data_viewCommand> getList() {
        return m_list;
    }

    public byte getCommandCode(int pos) {
        Data_viewCommand x=m_list.get(pos);
        return x.m_cmd;
    }

    public String getCommandDescriptionStringAtPosition(int pos) {
        Data_viewCommand x=m_list.get(pos);
        return x.toString();
    }

    public int getCount() {
        return m_list.size();
    }
}
