package com.spectratech.sp530demo;

import com.spectratech.lib.Logger;
import com.spectratech.sp530demo.data.Data_SP530_tms_module;
import com.spectratech.sp530demo.data.Data_tms_header;
import com.spectratech.sp530demo.data.Data_tms_header_module;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Tms Helper
 */
public class TmsHelper {

    private final String m_className="TmsHelper";

    private static TmsHelper m_inst;

    /**
     * Returns TmsHelper instance
     * @param bForceNew Force new instance flag
     * @return static TmsHelper instance
     */
    public static TmsHelper getInstance(boolean bForceNew) {
        if (bForceNew) {
            m_inst=null;
        }
        return getInstance();
    }

    /**
     * Returns TmsHelper instance
     * @return static TmsHelper instance
     */
    public static TmsHelper getInstance() {
        if (m_inst==null) {
            m_inst=new TmsHelper();
        }
        return m_inst;
    }

    public ArrayList<Data_SP530_tms_module> getSP530TmsModuleList(byte[] buf) {
        if (buf==null) {
            Logger.w(m_className, "getSP530TmsModuleList, buf is null");
            return null;
        }
        String strText=new String(buf);
        return getSP530TmsModuleList(strText);
    }

    public ArrayList<Data_SP530_tms_module> getSP530TmsModuleList(String strText) {
        if (strText==null) {
            Logger.w(m_className, "getSP530TmsModuleList, strText is null");
            return null;
        }

        ArrayList<Data_SP530_tms_module> list=new ArrayList<>();

        Scanner scanner=new Scanner(strText);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // empty
            if (line==null) {
                continue;
            }
            if (line.length()==0) {
                continue;
            }

            Data_SP530_tms_module x=new Data_SP530_tms_module(line);
            list.add(x);
        }

        return list;
    }

    public Data_tms_header getHeaderObject(String dirPath, String name) {
        Data_tms_header dataTmsHeader=null;
        InputStream is=getInputStreamFromFile(dirPath, name);
        if (is==null) {
            Logger.w(m_className, "getHeaderObject, is is null");
            return dataTmsHeader;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            String line="";

            // parse "#"
            line=br.readLine();
            if (!line.equals("#")) {
                Logger.w(m_className, "getHeaderObject, cp1, not TMS header");
                if (is!=null) {
                    is.close();
                    is=null;
                }
                return dataTmsHeader;
            }

            // parse modle name
            dataTmsHeader=new Data_tms_header();
            if (dataTmsHeader.m_moduleList==null) {
                dataTmsHeader.m_moduleList = new ArrayList<>();
            }
            dataTmsHeader.m_modelName=br.readLine();

            // parse "!"
            line=br.readLine();
            if (!line.equals("!")) {
                Logger.w(m_className, "getHeaderObject, cp2, not TMS header");
                dataTmsHeader=null;
                if (is!=null) {
                    is.close();
                    is=null;
                }
                return dataTmsHeader;
            }

            line=br.readLine();
            if (line.equals("*")) {
                dataTmsHeader.m_bBinary=false;
            }
            else if (line.equals("%")) {
                dataTmsHeader.m_bBinary=true;
            }
            do {
                if ( (!line.equals("*"))&&(!line.equals("%")) ) {
                    Data_tms_header_module dataTmsHeaderModule=new Data_tms_header_module(line);
                    dataTmsHeader.m_moduleList.add(dataTmsHeaderModule);
                    line=br.readLine();
                    if (line.equals("*")) {
                        dataTmsHeader.m_bBinary=false;
                    }
                    else if (line.equals("%")) {
                        dataTmsHeader.m_bBinary=true;
                    }
                }
            }
            while ( (line!=null)&&(!line.equals("*"))&&(!line.equals("%")) );

            //Logger.i(m_className, "getHeaderObject, number of module: "+dataTmsHeader.m_moduleList.size());

            if (is!=null) {
                is.close();
                is=null;
            }
        }
        catch (IOException io) {
            Logger.w(m_className, "getHeaderObject, IOException io: " + io.toString());
            dataTmsHeader=null;
        }

        return dataTmsHeader;
    }

    private InputStream getInputStreamFromFile(String dirPath, String name) {
        File f = new File(dirPath, name);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        }
        catch (FileNotFoundException fnf) {
            Logger.w(m_className, "getInputStreamFromFile, FileNotFoundException fnf: " + fnf.toString() + ", dirPath: " + dirPath + ", name: " + name);
            fis=null;
        }
        return fis;
    }

    public String getHashKey(Data_SP530_tms_module dataSP530TmsModule) {
        if (dataSP530TmsModule==null) {
            Logger.w(m_className, "getHashKey, Data_SP530_tms_module is null");
            return null;
        }

        String key="";
        key+=dataSP530TmsModule.m_name;
        key+=dataSP530TmsModule.m_version;
        key+=dataSP530TmsModule.m_subVersion;
        key+= dataSP530TmsModule.m_csumApp;
        key+=dataSP530TmsModule.m_csumAppDis;
        return key;
    }

    public String getHashKey(Data_tms_header_module x) {
        if (x==null) {
            Logger.w(m_className, "getHashKey, Data_tms_header_module is null");
            return null;
        }

        String key="";
        key+=x.m_name;
        key+=x.m_version;
        key+=x.m_subVersion;
        String strTrim=x.m_name.trim();
        if (strTrim.equals("BLOADER")) {
            Logger.i(m_className, "getHashKey, BLOADER obtained, assign dis csumApp to csumApp");
            key+=x.m_csumAppDis;
        }
        else {
            key+= x.m_csumApp;
        }

        key+=x.m_csumAppDis;
        return key;
    }
}
