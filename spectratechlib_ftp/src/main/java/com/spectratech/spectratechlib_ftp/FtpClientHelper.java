package com.spectratech.spectratechlib_ftp;

import com.spectratech.lib.Logger;
import com.spectratech.spectratechlib_ftp.data.Data_ftpClient;
import com.spectratech.spectratechlib_ftp.data.Data_ftpdlObject;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * FTP client helper
 */
public class FtpClientHelper {

    private static final String m_className="FtpClientHelper";

    public static final int DEFAULT_FTP_PORT=21;

    private static FtpClientHelper m_inst;

    /**
     * Returns FtpClientHelper instance
     * @param bForceNew Force new instance flag
     * @return static FtpClientHelper instance
     */
    public static FtpClientHelper getInstance(boolean bForceNew) {
        if (bForceNew) {
            m_inst=null;
        }
        return getInstance();
    }

    /**
     * Returns FtpClientHelper instance
     * @return static FtpClientHelper instance
     */
    public static FtpClientHelper getInstance() {
        if (m_inst==null) {
            m_inst=new FtpClientHelper();
        }
        return m_inst;
    }

    private FtpDownloadThread m_ftpDownloadThread;

    private FtpClientHelper() {
        init();
    }

    private void init() {
    }

    public long getFtpFileSize(FTPClient ftp, String filePath) {
        long fileSize = 0;
        try {
            FTPFile[] files = ftp.listFiles(filePath);
            if (files.length == 1 && files[0].isFile()) {
                fileSize = files[0].getSize();
            }
            Logger.i(m_className, "getFtpFileSize, " + filePath + ", file size = " + fileSize);
        }
        catch (Exception ex) {
            Logger.w(m_className, "getFtpFileSize, " + filePath + ",  exception ex: " + ex.toString());
        }
        return fileSize;
    }

    public void downloadFile(Data_ftpClient dataFtpClient, Data_ftpdlObject dataFtpdlObject, String downloadFilePath) {
        ArrayList<Data_ftpdlObject> dataFtpdlObjectList=new ArrayList<>();
        dataFtpdlObjectList.add(dataFtpdlObject);
        downloadFile(dataFtpClient, dataFtpdlObjectList, downloadFilePath);
    }

    public void downloadFile(Data_ftpClient dataFtpClient, ArrayList<Data_ftpdlObject> dataFtpdlObjectList, String downloadFilePath) {
        safeFreeFtpDownloadThread();
        m_ftpDownloadThread=new FtpDownloadThread(dataFtpClient, dataFtpdlObjectList, downloadFilePath);
        m_ftpDownloadThread.start();
    }

    public void cancelDownloadThread() {
        safeFreeFtpDownloadThread();
    }

    private void safeFreeFtpDownloadThread() {
        if (m_ftpDownloadThread!=null) {
            boolean bFinish=m_ftpDownloadThread.isFinish();
            if (!bFinish) {
                m_ftpDownloadThread.cancel();
            }
            m_ftpDownloadThread=null;
        }
    }

    private String fixFolderSlash(String strFolder) {
        String resultFolder=strFolder;
        if ( (resultFolder==null)||(resultFolder.length()==0) ) {
            return resultFolder;
        }
        resultFolder=resultFolder.replace('\\','/');
        resultFolder=resultFolder.replace("//","/");
        char charLast=resultFolder.charAt(resultFolder.length()-1);
        if (charLast!='/')  {
            resultFolder+="/";
        }
        return resultFolder;
    }

    private class FtpDownloadThread extends Thread {

        boolean m_bCancel;
        boolean m_bFinish;

        private FTPClient m_ftpClient;

        private Data_ftpClient m_dataFtpClient;
        private ArrayList<Data_ftpdlObject> m_dataFtpdlObjectList;
        private String m_downloadFilePath;

        public FtpDownloadThread(Data_ftpClient dataFtpClient, ArrayList<Data_ftpdlObject> dataFtpdlObjectList, String downloadFilePath) {
            init();
            m_dataFtpClient=dataFtpClient;
            m_dataFtpdlObjectList=dataFtpdlObjectList;
            m_downloadFilePath=downloadFilePath;
        }

        private void init() {
            m_bCancel=false;
            m_bFinish=true;

            m_ftpClient=null;
            m_dataFtpClient=null;
            m_dataFtpdlObjectList=null;
            m_downloadFilePath=null;
        }

        @Override
        public void run() {
            m_bFinish=false;

            // open ftp connection
            m_ftpClient=new FTPClient();
            try {
                m_ftpClient.setConnectTimeout(m_dataFtpClient.m_connectTimeoutInMs);
                m_ftpClient.connect(m_dataFtpClient.m_host, m_dataFtpClient.m_port);
                int iReplyCode=m_ftpClient.getReplyCode();
                if (FTPReply.isPositiveCompletion(iReplyCode)) {
                    boolean bLogin=m_ftpClient.login(m_dataFtpClient.m_username, m_dataFtpClient.m_password);
                    if (!bLogin) {
                        Logger.w(m_className, "downloadFilesFromFtpList, login is false");
                    }
                    if (m_dataFtpClient.m_bPassiveMode) {
                        Logger.i(m_className, "downloadFilesFromFtpList, passive mode");
                        m_ftpClient.enterLocalPassiveMode();
                    }

                    // set to binary type
                    if (!m_ftpClient.setFileType(FTP.BINARY_FILE_TYPE)) {
                        Logger.w("downloadFilesFromFtpList, binary", "Setting binary file type failed.");
                    }
                }
                else {
                    Logger.w(m_className, "downloadFilesFromFtpList, FTPReply.isPositiveCompletion is false, val: " + iReplyCode);
                }
            }
            catch (SocketException socex) {
                Logger.w(m_className, "downloadFilesFromFtpList, socex: " + socex.toString());
            }
            catch (IOException ioex) {
                Logger.w(m_className, "downloadFilesFromFtpList, ioex: " + ioex.toString());
            }


            // start ftp download
            for (int i=0; i<m_dataFtpdlObjectList.size(); i++) {
                if (m_bCancel) {
                    Logger.i(m_className, "FtpDownloadThread, cancel flag is TRUE");
                    break;
                }

                Data_ftpdlObject dataFtpdlObject=m_dataFtpdlObjectList.get(i);
                if (!dataFtpdlObject.m_bEnable) {
                    Logger.i(m_className, "downloadFilesFromFtpList, SKIP idx: " + i + ", m_bEnable is FALSE, m_id: " + dataFtpdlObject.m_id + ", m_relative_pathlist: " + dataFtpdlObject.m_relative_pathlist);
                    call_callback_finish(dataFtpdlObject);
                    continue;
                }
                String filename = fixFolderSlash(m_downloadFilePath) + dataFtpdlObject.m_relative_pathlist;

                File fObj = new File(filename);

                if (dataFtpdlObject.m_bForceDownload) {
                    Logger.i(m_className, "downloadFilesFromFtpList, force download " + dataFtpdlObject.m_relative_pathlist);
                }
                else {
                    boolean bSkip = false;
                    if (fObj.exists()) {
                        long size_localFile = fObj.length();
                        long size_ftpFile = getFtpFileSize(m_ftpClient, dataFtpdlObject.m_relative_pathlist);
                        Logger.i(m_className, "downloadFilesFromFtpList, size_localFile:size,size_ftpFile: " + size_localFile + "," + size_ftpFile);
                        if (size_localFile>0) {
                            if ((size_localFile == size_ftpFile)) {
                                bSkip = true;
                            }
                        }
                    }
                    if (bSkip) {
                        dataFtpdlObject.m_bSkip = bSkip;
                        call_callback_finish(dataFtpdlObject);
                        continue;
                    }
                }

                Logger.i(m_className, "downloadFilesFromFtpList, idx: " + i + ", m_id: " + dataFtpdlObject.m_id + ", m_relative_pathlist: " + dataFtpdlObject.m_relative_pathlist);

                String path = fObj.getParent();
                fObj = new File(path);
                if (!fObj.exists()) {
                    boolean bMakeNewDir = fObj.mkdirs();
                    if (!bMakeNewDir) {
                        Logger.w(m_className, "FtpDownloadThread, make new directory FAIL: " + filename);
                    }
                }
                InputStream is=null;
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(filename);
                    boolean bRetrieveFile = false;
                    if (fos==null) {
                        Logger.w(m_className, "FtpDownloadThread, FileOutputStream fos is null");
                    }
                    else {
                        bRetrieveFile = m_ftpClient.retrieveFile(dataFtpdlObject.m_relative_pathlist, fos);
                    }

                    if (bRetrieveFile) {
                        Logger.i(m_className, "FtpDownloadThread, retrieveFile SUCCESS");
                        dataFtpdlObject.m_bDownloadSuccess=true;
                    } else {
                        Logger.w(m_className, "FtpDownloadThread, retrieveFile FAILED");
                        dataFtpdlObject.m_bDownloadSuccess=false;
                    }
                } catch (IOException ioex) {
                    Logger.w(m_className, "FtpDownloadThread, ioex: " + ioex.toString());
                    dataFtpdlObject.m_bDownloadSuccess=false;
                    call_callback_finish(dataFtpdlObject);
                    continue;
                }
                finally {
                    try {
                        if (is!=null) {
                            is.close();
                        }
                    } catch (IOException ioex) {
                        Logger.w(m_className, "downloadFilesFromFtpList, is, ioex: " + ioex.toString());
                    }
                    try {
                        if (fos!=null) {
                            fos.close();
                        }
                    } catch (IOException ioex) {
                        Logger.w(m_className, "downloadFilesFromFtpList, fos, ioex: " + ioex.toString());
                    }
                }

                call_callback_finish(dataFtpdlObject);
            }

            // close ftp connection
            try {
                m_ftpClient.disconnect();
            } catch (IOException ioex) {
                Logger.w(m_className, "downloadFilesFromFtpList, ioex: " + ioex.toString());
            }

            m_bFinish=true;
        }

        private void call_callback_finish(Data_ftpdlObject dataFtpdlObject) {
            if (dataFtpdlObject.m_cb_finishdl!=null) {
                try {
                    dataFtpdlObject.m_cb_finishdl.setParameter(dataFtpdlObject);
                    dataFtpdlObject.m_cb_finishdl.call();
                }
                catch (Exception ex) {
                    Logger.w(m_className, "downloadFilesFromFtpList, m_cb_finishdl.call ex: " + ex.toString());
                }
            }
        }

        public boolean isFinish() {
            return m_bFinish;
        }

        public void cancel() {
            Logger.i(m_className, "downloadFilesFromFtpList, FtpDownloadThread, cancel called");
            m_bCancel=true;
            interrupt();
        }
    }
}
