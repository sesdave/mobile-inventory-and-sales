package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spectratech.lib.Callback;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.data.Data_S3INS_response;
import com.spectratech.sp530demo.data.Data_device_mcpchcfg;

/**
 * DeviceFragment - Fragment for device events
 */
public class DeviceFragment extends S3CommonFragment {

    private static final String m_className="DeviceFragment";

    private LinearLayout m_mainll;

    private Button m_btn_reboot;

    private TextView m_tv_chone;
    private TextView m_tv_chtwo;

    private EditText m_ch1_et_ipone;
    private EditText m_ch1_et_iptwo;
    private EditText m_ch1_et_ipthree;
    private EditText m_ch1_et_ipfour;
    private EditText m_ch1_et_port;
    private EditText m_ch1_et_to;
    private TextView m_ch1_tv_enableSSL;

    private EditText m_ch2_et_ipone;
    private EditText m_ch2_et_iptwo;
    private EditText m_ch2_et_ipthree;
    private EditText m_ch2_et_ipfour;
    private EditText m_ch2_et_port;
    private EditText m_ch2_et_to;
    private TextView m_ch2_tv_enableSSL;

    private Button m_btn_setmfgconfig;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity act=getActivity();
        m_context=(Context)act;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_mainll=(LinearLayout)inflater.inflate(R.layout.view_device, container, false);

        // focus me please
        m_mainll.setFocusable(true);
        m_mainll.setFocusableInTouchMode(true);
        m_mainll.requestFocus();

        return m_mainll;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final DemoMainActivity act=(DemoMainActivity)m_context;

        final String strSet= ResourcesHelper.getBaseContextString(m_context, R.string.set);
        final String strSuccess=ResourcesHelper.getBaseContextString(m_context, R.string.success);
        final String strFail=ResourcesHelper.getBaseContextString(m_context, R.string.fail);

        m_btn_reboot=(Button)m_mainll.findViewById(R.id.btn_reboot);
        {
            View.OnClickListener listener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    act.device_rebootreq();
                }
            };
            m_btn_reboot.setOnClickListener(listener);
        }

        m_tv_chone=(TextView)m_mainll.findViewById(R.id.tv_chone);
        m_tv_chone.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        m_tv_chtwo=(TextView)m_mainll.findViewById(R.id.tv_chtwo);
        m_tv_chtwo.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        m_ch1_et_ipone=(EditText)m_mainll.findViewById(R.id.val_ch1_ipone);
        m_ch1_et_iptwo=(EditText)m_mainll.findViewById(R.id.val_ch1_iptwo);
        m_ch1_et_ipthree=(EditText)m_mainll.findViewById(R.id.val_ch1_ipthree);
        m_ch1_et_ipfour=(EditText)m_mainll.findViewById(R.id.val_ch1_ipfour);
        m_ch1_et_port=(EditText)m_mainll.findViewById(R.id.val_ch1_port);
        m_ch1_et_to=(EditText)m_mainll.findViewById(R.id.val_ch1_to);
        m_ch1_tv_enableSSL=(TextView)m_mainll.findViewById(R.id.val_ch1_ssl);
        setUI_SSLEnable(m_ch1_tv_enableSSL, true);
        {
            View.OnClickListener listener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClick_SSLEnable(m_ch1_tv_enableSSL);
                }
            };
            m_ch1_tv_enableSSL.setOnClickListener(listener);
        }

        m_ch2_et_ipone=(EditText)m_mainll.findViewById(R.id.val_ch2_ipone);
        m_ch2_et_iptwo=(EditText)m_mainll.findViewById(R.id.val_ch2_iptwo);
        m_ch2_et_ipthree=(EditText)m_mainll.findViewById(R.id.val_ch2_ipthree);
        m_ch2_et_ipfour=(EditText)m_mainll.findViewById(R.id.val_ch2_ipfour);
        m_ch2_et_port=(EditText)m_mainll.findViewById(R.id.val_ch2_port);
        m_ch2_et_to=(EditText)m_mainll.findViewById(R.id.val_ch2_to);
        m_ch2_tv_enableSSL=(TextView)m_mainll.findViewById(R.id.val_ch2_ssl);
        setUI_SSLEnable(m_ch2_tv_enableSSL, true);
        {
            View.OnClickListener listener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClick_SSLEnable(m_ch2_tv_enableSSL);
                }
            };
            m_ch2_tv_enableSSL.setOnClickListener(listener);
        }

        m_btn_setmfgconfig=(Button)m_mainll.findViewById(R.id.btn_setmfgconfig);
        {
            View.OnClickListener listener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bValidInput=checkForValidInput();
                    if (!bValidInput) {
                        return;
                    }
                    Data_device_mcpchcfg data=getDataDeviceMcpChCfg();
                    data.initSP530();
                    Callback<Object> cb_finish=new Callback<Object>() {
                        @Override
                        public Object call() throws Exception {
                            boolean bSucces=false;
                            Object param = this.getParameter();
                            if (param instanceof Data_S3INS_response) {
                                Data_S3INS_response data=(Data_S3INS_response)param;
                                if (data.m_data!=null) {
                                    if (data.m_data[0]==ApplicationProtocolConstant.S3RC_OK) {
                                        bSucces=true;
                                    }
                                }
                            }
                            if (bSucces) {
                                act.ToastMessageRunOnUiThread(strSet+" "+strSuccess);
                            }
                            else {
                                act.ToastMessageRunOnUiThread(strSet+" "+strFail);
                            }
                            return null;
                        }
                    };

                    act.device_updatemcpchcfg(data, cb_finish);
                }
            };
            m_btn_setmfgconfig.setOnClickListener(listener);
        }

        fillUI_defaultValues();
    }

    private boolean checkForValidInput() {
        boolean bValid=true;
        DemoMainActivity act=(DemoMainActivity)m_context;

        final String strPlease=ResourcesHelper.getBaseContextString(m_context, R.string.please);
        final String strCheck=ResourcesHelper.getBaseContextString(m_context, R.string.check);
        final String strChannelOne=ResourcesHelper.getBaseContextString(m_context, R.string.channel_one);
        final String strChannelTwo=ResourcesHelper.getBaseContextString(m_context, R.string.channel_two);
        final String strPort=ResourcesHelper.getBaseContextString(m_context, R.string.port);
        final String strTimeout=ResourcesHelper.getBaseContextString(m_context, R.string.timeout);

        final String strPleaseCheckChannelOne=strPlease+" "+strCheck+" "+" "+strChannelOne;
        final String strPleaseCheckChannelTwo=strPlease+" "+strCheck+" "+" "+strChannelTwo;

        // ch1
        bValid=isValidIPValue(m_ch1_et_ipone);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelOne+" ip");
            return bValid;
        }
        bValid=isValidIPValue(m_ch1_et_iptwo);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelOne+" ip");
            return bValid;
        }
        bValid=isValidIPValue(m_ch1_et_ipthree);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelOne+" ip");
            return bValid;
        }
        bValid=isValidIPValue(m_ch1_et_ipfour);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelOne+" ip");
            return bValid;
        }
        bValid=isValidPortValue(m_ch1_et_port);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelOne+" "+strPort);
            return bValid;
        }
        bValid=isValidTimeoutValue(m_ch1_et_to);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelOne+" "+strTimeout);
            return bValid;
        }


        // ch2
        bValid=isValidIPValue(m_ch2_et_ipone);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelTwo+" ip");
            return bValid;
        }
        bValid=isValidIPValue(m_ch2_et_iptwo);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelTwo+" ip");
            return bValid;
        }
        bValid=isValidIPValue(m_ch2_et_ipthree);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelTwo+" ip");
            return bValid;
        }
        bValid=isValidIPValue(m_ch2_et_ipfour);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelTwo+" ip");
            return bValid;
        }
        bValid=isValidPortValue(m_ch2_et_port);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelTwo+" "+strPort);
            return bValid;
        }
        bValid=isValidTimeoutValue(m_ch2_et_to);
        if (!bValid) {
            act.ToastMessage(strPleaseCheckChannelTwo+" "+strTimeout);
            return bValid;
        }

        return bValid;
    }

    private boolean isValidTimeoutValue(EditText et) {
        String text=et.getText().toString();
        return isValidTimeoutValue(text);
    }
    private boolean isValidTimeoutValue(String text) {
        boolean bValid=isNumber(text);
        if (bValid) {
            try {
                int num=Integer.parseInt(text);
                if ((num<0)||(num>255)) {
                    bValid=false;
                }
            } catch (NumberFormatException e) {
                bValid=false;
            }
        }
        return bValid;
    }

    private boolean isValidPortValue(EditText et) {
        String text=et.getText().toString();
        return isValidPortValue(text);
    }
    private boolean isValidPortValue(String text) {
        boolean bValid=isNumber(text);
        if (bValid) {
            try {
                int num=Integer.parseInt(text);
                if ((num<0)||(num>65535)) {
                    bValid=false;
                }
            } catch (NumberFormatException e) {
                bValid=false;
            }
        }
        return bValid;
    }

    private boolean isValidIPValue(EditText et) {
        String text=et.getText().toString();
        return isValidIPValue(text);
    }
    private boolean isValidIPValue(String text) {
        boolean bValid=isNumber(text);
        if (bValid) {
            try {
                int num=Integer.parseInt(text);
                if ((num<0)||(num>255)) {
                    bValid=false;
                }
            } catch (NumberFormatException e) {
                bValid=false;
            }
        }
        return bValid;
    }

    private boolean isNumber(String text) {
        boolean bValid;
        try {
            Integer.parseInt(text);
            bValid=true;
        } catch (NumberFormatException e) {
            bValid=false;
        }
        return bValid;
    }

    private void fetchDataFromUI(Data_device_mcpchcfg data) {
        String strTmp;
        int val;
        int count;

        // ip
        count=0;
        byte[] ch1_ip=new byte [4];
        strTmp=m_ch1_et_ipone.getText().toString();
        val=Integer.parseInt(strTmp);
        ch1_ip[count++]=(byte)(val&0xFF);
        strTmp=m_ch1_et_iptwo.getText().toString();
        val=Integer.parseInt(strTmp);
        ch1_ip[count++]=(byte)(val&0xFF);
        strTmp=m_ch1_et_ipthree.getText().toString();
        val=Integer.parseInt(strTmp);
        ch1_ip[count++]=(byte)(val&0xFF);
        strTmp=m_ch1_et_ipfour.getText().toString();
        val=Integer.parseInt(strTmp);
        ch1_ip[count++]=(byte)(val&0xFF);
        data.setIP(0, ch1_ip);

        count=0;
        byte[] ch2_ip=new byte [4];
        strTmp=m_ch2_et_ipone.getText().toString();
        val=Integer.parseInt(strTmp);
        ch2_ip[count++]=(byte)(val&0xFF);
        strTmp=m_ch2_et_iptwo.getText().toString();
        val=Integer.parseInt(strTmp);
        ch2_ip[count++]=(byte)(val&0xFF);
        strTmp=m_ch2_et_ipthree.getText().toString();
        val=Integer.parseInt(strTmp);
        ch2_ip[count++]=(byte)(val&0xFF);
        strTmp=m_ch2_et_ipfour.getText().toString();
        val=Integer.parseInt(strTmp);
        ch2_ip[count++]=(byte)(val&0xFF);
        data.setIP(1, ch2_ip);

        // port
        byte ch1_port[]=new byte [2];
        strTmp=m_ch1_et_port.getText().toString();
        val=Integer.parseInt(strTmp);
        ch1_port[0]=(byte)(val&0xFF);
        ch1_port[1]=(byte)((val>>8)&0xFF);
        data.setPort(0, ch1_port);

        byte ch2_port[]=new byte [2];
        strTmp=m_ch2_et_port.getText().toString();
        val=Integer.parseInt(strTmp);
        ch2_port[0]=(byte)(val&0xFF);
        ch2_port[1]=(byte)((val>>8)&0xFF);
        data.setPort(1, ch2_port);

        // timeout
        byte ch1_to;
        strTmp=m_ch1_et_to.getText().toString();
        val=Integer.parseInt(strTmp);
        ch1_to=(byte)(val&0xFF);
        data.setTimeout(0, ch1_to);

        byte ch2_to;
        strTmp=m_ch2_et_to.getText().toString();
        val=Integer.parseInt(strTmp);
        ch2_to=(byte)(val&0xFF);
        data.setTimeout(1, ch2_to);

        //ssl
        byte ch1_ssl;
        boolean ch1_flagSSL=(boolean)m_ch1_tv_enableSSL.getTag();
        if (ch1_flagSSL) {
            ch1_ssl=(byte)1;
        }
        else {
            ch1_ssl=(byte)0;
        }
        data.setEnableSSL(0, ch1_ssl);

        byte ch2_ssl;
        boolean ch2_flagSSL=(boolean)m_ch2_tv_enableSSL.getTag();
        if (ch2_flagSSL) {
            ch2_ssl=(byte)1;
        }
        else {
            ch2_ssl=(byte)0;
        }
        data.setEnableSSL(1, ch2_ssl);
    }

    private Data_device_mcpchcfg getDataDeviceMcpChCfg() {
        Data_device_mcpchcfg data=new Data_device_mcpchcfg();
        fetchDataFromUI(data);
        return data;
    }

    private void fillUI_defaultValues() {
        m_ch1_et_ipone.setText("127");
        m_ch1_et_iptwo.setText("0");
        m_ch1_et_ipthree.setText("0");
        m_ch1_et_ipfour.setText("1");
        m_ch1_et_port.setText("50013");
        m_ch1_et_to.setText("10");

        m_ch2_et_ipone.setText("10");
        m_ch2_et_iptwo.setText("1");
        m_ch2_et_ipthree.setText("6");
        m_ch2_et_ipfour.setText("63");
        m_ch2_et_port.setText("50014");
        m_ch2_et_to.setText("30");
    }

    public void setUI_SSLEnable(TextView tv, boolean flag) {
        if (tv!=null) {
            tv.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
    }
    public void onClick_SSLEnable(TextView tv) {
        if (tv!=null) {
            boolean flag=(boolean)tv.getTag();
            flag=!flag;
            tv.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            if (isVisibleToUser) {
            }
        }
    }
}
