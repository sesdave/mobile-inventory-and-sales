package com.spectratech.sp530demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.tcpip.data.Data_SSLServerLocal;
import com.spectratech.sp530demo.conf.PathClass;
import com.spectratech.sp530demo.data.Data_setting_sp530sslcert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;

/**
 * SettingSP530SSLCertActivity - setting SP530 SSL cert activity
 */
public class SettingSP530SSLCertActivity extends SP530DemoBaseActivity {

    private static final String m_className = "SettingSP530SSLCertActivity";

    private static final String FTYPE = ".p12";
    private static final String FTYPE_PEM = ".pem";

    public static Data_setting_sp530sslcert m_dataSettingSp530SSLCert;

    private LinearLayout m_mainll;

    private TextView m_tv_default_rootpath;

    private TextView m_tv_param_p12cert_filename;
    private Button m_btn_param_p12cert_path_select;
    private EditText m_et_param_p12cert_pwd;

    private TextView m_tv_param_needclientauth;
    private LinearLayout m_cacert_container;
    private TextView m_tv_param_CAcert_filename;
    private Button m_btn_param_CAcert_path_select;

    private Toast m_toast=null;

    private String[] m_fileList;

    public static Data_setting_sp530sslcert getDataSettingSp530SSLCert(Context context) {
        if (m_dataSettingSp530SSLCert==null) {
            m_dataSettingSp530SSLCert=new Data_setting_sp530sslcert(context);
        }
        return m_dataSettingSp530SSLCert;
    }

    public static String getFileUriString(Context context) {
        getDataSettingSp530SSLCert(context);
        if (m_dataSettingSp530SSLCert==null) {
            Logger.w(m_className, "getFileUriString, m_dataSettingSp530SSLCert is null");
            return Data_setting_sp530sslcert.DEFAULT_FILENAME_STRING;
        }
        return m_dataSettingSp530SSLCert.m_strFilename;
    }

    public static String getP12Password(Context context) {
        getDataSettingSp530SSLCert(context);
        if (m_dataSettingSp530SSLCert==null) {
            Logger.w(m_className, "getP12Password, m_dataSettingSp530SSLCert is null");
            return Data_setting_sp530sslcert.DEFAULT_P12PASSWORD_STRING;
        }
        return m_dataSettingSp530SSLCert.m_strP12Password;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String strNoP12FileFound= ResourcesHelper.getBaseContextString(m_context, R.string.no_p12_file_found);
        final String strNoPEMFileFound= ResourcesHelper.getBaseContextString(m_context, R.string.no_pem_file_found);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_settingsp530sslcert, null);
        setContentView(m_mainll);

        m_tv_default_rootpath=(TextView)m_mainll.findViewById(R.id.tv_default_rootpath);
        m_tv_default_rootpath.setText(PathClass.EXTERNAL_ROOTPATH);

        m_tv_param_p12cert_filename=(TextView)m_mainll.findViewById(R.id.param_p12cert_filename);
        m_btn_param_p12cert_path_select=(Button)m_mainll.findViewById(R.id.param_p12cert_path_select);
        m_btn_param_p12cert_path_select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!isOnPause()) {
                    loadFileList(FTYPE);
                    if ( (m_fileList==null)||(m_fileList.length==0) ) {
                        toastMessage(strNoP12FileFound);
                        return;
                    }
                    popupFileListDialog();
                }
            }
        });
        m_et_param_p12cert_pwd=(EditText)m_mainll.findViewById(R.id.param_p12cert_pwd);

        m_tv_param_needclientauth=(TextView)m_mainll.findViewById(R.id.param_needclientauth);
        m_cacert_container=(LinearLayout)m_mainll.findViewById(R.id.cacert_select_container);
        m_tv_param_CAcert_filename=(TextView)m_mainll.findViewById(R.id.param_cacert_filename);
        m_btn_param_CAcert_path_select=(Button)m_mainll.findViewById(R.id.param_cacert_path_select);
        m_btn_param_CAcert_path_select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!isOnPause()) {
                    loadFileList(FTYPE_PEM);
                    if ( (m_fileList==null)||(m_fileList.length==0) ) {
                        toastMessage(strNoPEMFileFound);
                        return;
                    }
                    popupFileListDialog_forCACert();
                }
            }
        });



        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }

        fetchValues2UI();

        m_fileList=null;

        m_mainll.setFocusable(true);
        m_mainll.setFocusableInTouchMode(true);
        m_mainll.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void toastMessage(String strmsg) {
        if (m_toast!=null) {
            m_toast.cancel();
            m_toast=null;
        }
        m_toast=Toast.makeText(m_context, strmsg, Toast.LENGTH_SHORT);
        m_toast.show();
    }

    private void fetchValues2UI() {
        getDataSettingSp530SSLCert(m_context);
        m_tv_param_p12cert_filename.setText(m_dataSettingSp530SSLCert.m_strFilename);
        m_et_param_p12cert_pwd.setText(m_dataSettingSp530SSLCert.m_strP12Password);
        m_tv_param_CAcert_filename.setText(m_dataSettingSp530SSLCert.m_strCAFilename);
        setUI_needClientAuth(m_dataSettingSp530SSLCert.m_bNeedClientAuth);
    }

    public void setUI_needClientAuth(boolean flag) {
        if (m_tv_param_needclientauth!=null) {
            m_tv_param_needclientauth.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tv_param_needclientauth.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (m_cacert_container!=null) {
                if (flag) {
                    m_cacert_container.setVisibility(View.VISIBLE);
                }
                else {
                    m_cacert_container.setVisibility(View.GONE);
                }
            }
        }
    }

    public void onClickCAClientAuth(View v) {
        if (m_tv_param_needclientauth!=null) {
            boolean flag=(boolean)m_tv_param_needclientauth.getTag();
            flag=!flag;
            setUI_needClientAuth(flag);
        }
    }

    /**
     * On click function for saving parameters and finish this activity
     * @param v corresponding view
     */
    public void onProcess(View v) {
        final String strParamSaved=ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);

        getDataSettingSp530SSLCert(m_context);

        String strFilename=m_tv_param_p12cert_filename.getText().toString();
        boolean bNeedClientAuth=(boolean)m_tv_param_needclientauth.getTag();
        String strCAFilename="";
        if (bNeedClientAuth) {
            strCAFilename=m_tv_param_CAcert_filename.getText().toString();
            // checking
            if (strFilename.isEmpty()) {
                toastMessage("Client Auth. checked, p12 file EMPTY");
                return;
            }
            if (strCAFilename.isEmpty()) {
                toastMessage("Client Auth. checked, CA file EMPTY");
                return;
            }
        }

        m_dataSettingSp530SSLCert.setFilenameString(m_context, strFilename);
        m_dataSettingSp530SSLCert.setNeedClientAuth(m_context, bNeedClientAuth);
        m_dataSettingSp530SSLCert.setCAFilenameString(m_context, strCAFilename);

        String strP12Password=m_et_param_p12cert_pwd.getText().toString();
        m_dataSettingSp530SSLCert.setP12Password(m_context, strP12Password);


        Application.FLAG_UPDATE_DATA_SETTING_SSLCERT_PARAMS=true;

        toastMessage(strParamSaved);
        finish();
    }

    @Override
    public void onBackPressed() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        toastMessage(strCancel);
        super.onBackPressed();
    }

    private void onOrientationPortrait() {
        // not full screen mode
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void onOrientationLandscape() {
        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                onOrientationPortrait();
            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE: {
                onOrientationLandscape();
            }
            break;
        }
    }

    private String fixFolderSlash(String strFolder) {
        String resultFolder=strFolder;
        if ( (resultFolder==null)||(resultFolder.length()==0) ) {
            return resultFolder;
        }
        resultFolder=resultFolder.replace('\\','/');
        resultFolder=resultFolder.replace("//", "/");
        char charLast=resultFolder.charAt(resultFolder.length()-1);
        if (charLast!='/')  {
            resultFolder+="/";
        }
        return resultFolder;
    }

    private void loadFileList(final String ftype) {
        String path=PathClass.EXTERNAL_ROOTPATH;
        path=fixFolderSlash(path);
        File fpath=new File(path);
        if(fpath.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(ftype);
                }

            };
            m_fileList = fpath.list(filter);
        }
        else {
            m_fileList= new String[0];
        }
    }

    private Dialog popupFileListDialog() {
        final String strChooseAFile=ResourcesHelper.getBaseContextString(m_context, R.string.select_afile);

        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(strChooseAFile);
        builder.setItems(m_fileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String chooseFile = m_fileList[which];
                Logger.i(m_className, "onCreateDialog, choseFile: " + chooseFile);
                m_tv_param_p12cert_filename.setText(chooseFile);
            }
        });
        dialog=builder.show();
        return dialog;
    }

    private Dialog popupFileListDialog_forCACert() {
        final String strChooseAFile=ResourcesHelper.getBaseContextString(m_context, R.string.select_afile);

        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(strChooseAFile);
        builder.setItems(m_fileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String chooseFile = m_fileList[which];
                Logger.i(m_className, "onCreateDialog, choseFile: " + chooseFile);
                m_tv_param_CAcert_filename.setText(chooseFile);
            }
        });
        dialog=builder.show();
        return dialog;
    }

    public static Data_SSLServerLocal generateDataSSLServerLocal(Context context) {
        if (context==null) {
            Logger.w(m_className, "generateDataSSLServerLocal, context is null");
            return null;
        }
        getDataSettingSp530SSLCert(context);
        if ( (m_dataSettingSp530SSLCert.m_strFilename==null)||(m_dataSettingSp530SSLCert.m_strFilename.equals("")) ) {
            return null;
        }

        Data_SSLServerLocal dataSSLServerLocal=new Data_SSLServerLocal();

        dataSSLServerLocal.m_inputstream_ks=getInputStreamFromFile(context, PathClass.EXTERNAL_ROOTPATH, m_dataSettingSp530SSLCert.m_strFilename);
        dataSSLServerLocal.m_bNeedClientAuth=m_dataSettingSp530SSLCert.m_bNeedClientAuth;
        dataSSLServerLocal.m_inputstream_ca=getInputStreamFromFile(context, PathClass.EXTERNAL_ROOTPATH, m_dataSettingSp530SSLCert.m_strCAFilename);
        dataSSLServerLocal.m_keystorepass=null;
        dataSSLServerLocal.m_keypassword=null;
        if ( (m_dataSettingSp530SSLCert.m_strP12Password!=null)&&(m_dataSettingSp530SSLCert.m_strP12Password.length()>0) ) {
            dataSSLServerLocal.m_keystorepass = m_dataSettingSp530SSLCert.m_strP12Password.toCharArray();
            dataSSLServerLocal.m_keypassword = m_dataSettingSp530SSLCert.m_strP12Password.toCharArray();
        }

        dataSSLServerLocal.m_keyStoreTypeString="PKCS12";

        dataSSLServerLocal.m_port=0;

        return dataSSLServerLocal;
    }
    private static InputStream getInputStreamFromFile(Context context, String strDir, String strFilename) {
        InputStream is =null;
        if (context==null) {
            Logger.w(m_className, "getInputStreamFromFile, context is null");
            return is;
        }
        if (strFilename==null) {
            Logger.w(m_className, "getInputStreamFromFile, strFileUri is null");
            return is;
        }
        try {
            File f = new File(strDir, strFilename);
            FileInputStream fis=new FileInputStream(f);
            is=fis;
        }
        catch (FileNotFoundException fnex) {
            Logger.w(m_className, "getInputStreamFromFile, fnex: " + fnex.toString());
        }

        return is;
    }


    public void onClick_reset(View v) {
        m_tv_param_p12cert_filename.setText(Data_setting_sp530sslcert.DEFAULT_FILENAME_STRING);
        m_et_param_p12cert_pwd.setText(Data_setting_sp530sslcert.DEFAULT_P12PASSWORD_STRING);
        m_tv_param_CAcert_filename.setText(Data_setting_sp530sslcert.DEFAULT_CAFILENAME_STRING);
        setUI_needClientAuth(Data_setting_sp530sslcert.DEFAULT_NEEDCLIENTAUTH_VALUE);
    }
}
