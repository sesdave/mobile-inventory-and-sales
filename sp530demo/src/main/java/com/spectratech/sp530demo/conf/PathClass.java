package com.spectratech.sp530demo.conf;

import android.content.Context;
import android.os.Environment;

/**
 * PathClass - class for define external path for read/write data
 */
public class PathClass {
    /**
     * Root directory for read/write data
     */
    public static final String ROOTDIR=getRootDir();

    /**
     * Directory for read/write pre-video
     */
    public static final String PREVIDEODIR="Prevideo";
    /**
     * Directory for read/write image
     */
    public static final String IMGDIR="Image";
    /**
     * Directory for read/write icon
     */
    public static final String ICONIMGDIR="Icon";
    /**
     * Directory for read/write captured image
     */
    public static final String IMGCAPTUREDIR="Imagecapture";
    /**
     * Directory for read/write signature image
     */
    public static final String SIGNATUREDIR="Signature";

    /**
     * External directory
     */
    public static final String EXTERNAL_ROOTPATH=Environment.getExternalStorageDirectory().getPath()+"/"+ROOTDIR;

    /**
     * External directory for pre-video
     */
    public static final String EXTERNAL_PREVIDEOPATH=EXTERNAL_ROOTPATH+"/"+PREVIDEODIR;
    /**
     * External directory for image
     */
    public static final String EXTERNAL_IMGPATH=EXTERNAL_ROOTPATH+"/"+IMGDIR;
    /**
     * External directory for icon
     */
    public static final String EXTERNAL_ICONIMGPATH=EXTERNAL_ROOTPATH+"/"+ICONIMGDIR;
    /**
     * External directory for capatured image
     */
    public static final String EXTERNAL_IMGCAPTUREPATH=EXTERNAL_ROOTPATH+"/"+IMGCAPTUREDIR;

    /**
     * External directory for transaction receipt
     */
    public static final String CAPTURE_TRANSRECEIPT_FULLPATH=EXTERNAL_IMGCAPTUREPATH+"/"+"transreceipt.jpg";

    /**
     * Get root directory
     * @return root directoy in string format
     */
	public static final String getRootDir() {
		String strRet="spectratech_sp530demo";
		return strRet;
	}

    /**
     * Get cache directory
     * @param context context of application
     * @return cache directory in string format
     */
    public static final String getCacheDir(Context context) {
        String strDir=context.getCacheDir().toString();
        return strDir;
    }

    /**
     * Get signature directory
     * @param context context of application
     * @return signature directory in string fomrat
     */
    public static final String getSignatureDir(Context context) {
        return getCacheDir(context)+"/"+SIGNATUREDIR;
    }
    public static final String getSignatureDirDebug(Context context) {
        return EXTERNAL_IMGCAPTUREPATH;
    }
}
