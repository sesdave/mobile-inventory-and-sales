package com.spectratech.sp530demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.sp530.ApplicationProtocolHelper;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.constant.S3_AuthConstant;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.data.Data_runtime_s3Auth;

/**
 * ViewS3AuthLinearLayout - linear layout view for mutu auth
 */
public class ViewS3AuthLinearLayout extends LinearLayout {

    private static final String m_className = "ViewS3AuthLinearLayout";

    protected Context m_context;

    protected LinearLayout m_mainll;

    private RadioGroup m_radiogroup_authkey;
    private RadioButton m_radio_mrmk;
    private RadioButton m_radio_rmk;

    private LinearLayout m_container_mutualauthcontrol;
    private Button m_btn_initmode;
    private Button m_btn_initauth;
    private Button m_btn_mutuauth;
    private Button m_btn_genkey;

    private EditText m_input_mrmk_idx;
    private EditText m_input_mrmk_val;
    private EditText m_input_rmk_idx;
    private EditText m_input_rmk_val;

    private EditText m_input_skey_val;
    private EditText m_input_trand_val;
    private EditText m_input_rrand_val;
    private EditText m_input_modemutuauth_val;

    private LinearLayout m_container_register_save_btn;

    private Data_runtime_s3Auth m_dataRunTimeS3Auth;

    public ViewS3AuthLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        m_context = context;
        init();
    }

    public ViewS3AuthLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
        init();
    }

    private void init() {
        m_mainll=this;
        m_dataRunTimeS3Auth=null;
    }

    public void setDataRuntimeS3Auth(Data_runtime_s3Auth dataRunTimeS3Auth) {
        m_dataRunTimeS3Auth=dataRunTimeS3Auth;
    }

    public void showSaveButton() {
        if (m_container_register_save_btn!=null) {
            m_container_register_save_btn.setVisibility(View.VISIBLE);
        }
    }

    public void hideMutuAuthControl() {
        if (m_container_mutualauthcontrol!=null) {
            m_container_mutualauthcontrol.setVisibility(View.GONE);
        }
    }

    public void initUIs() {
        final String strPleaseClickInitAuthFirst= ResourcesHelper.getBaseContextString(m_context, R.string.please_click_initauth_first);
        m_radiogroup_authkey=(RadioGroup)m_mainll.findViewById(R.id.radiogroup_authkey);
        m_radio_mrmk=(RadioButton)m_radiogroup_authkey.findViewById(R.id.radio_mrmk);
        m_radio_rmk=(RadioButton)m_radiogroup_authkey.findViewById(R.id.radio_rmk);
        if (m_dataRunTimeS3Auth.m_bSelectMRMK) {
            m_radio_mrmk.setChecked(true);
        }
        else {
            m_radio_rmk.setChecked(true);
        }

        m_container_mutualauthcontrol=(LinearLayout)m_mainll.findViewById(R.id.container_mutualauthcontrol);

        m_btn_initmode=(Button)m_mainll.findViewById(R.id.btn_initmode);
        m_btn_initmode.setVisibility(View.GONE);

        m_btn_initauth=(Button)m_mainll.findViewById(R.id.btn_initauth);
        m_btn_initauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRuntimeParameters();

                byte[] buf=DemoMainActivity.getInitAuthDataBytes();

                byte commandCode= ApplicationProtocolConstant.S3INS_INIT_AUTH;

                // set current sequence number to zero
                ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
                instApplicationProtocolHelper.setCurrentSequenceNumber2Zero();

                Callback<Object> cb_finish=null;
                ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                final DemoMainActivity act=instActivityHelper.getDemoMainActivity();
                if (act!=null) {
                    act.showProgressDialogAndDismissInTimeSlot(10*1000);
                    cb_finish=new Callback<Object>() {
                        @Override
                        public Void call() throws Exception {
                            act.dismissProgressDialog();
                            return null;
                        }
                    };
                    act.safeStartCommand(commandCode, buf, cb_finish);
                }
            }
        });

        m_btn_mutuauth=(Button)m_mainll.findViewById(R.id.btn_mutuauth);
        m_btn_mutuauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRuntimeParameters();

                if ( (Application.SkeyForMutuAuth==null)||Application.SkeyForMutuAuth.equals("") ) {
                    Toast.makeText(m_context, strPleaseClickInitAuthFirst, Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buf=DemoMainActivity.getMutuAuthDataBytes();

                byte commandCode=ApplicationProtocolConstant.S3INS_MUTU_AUTH;

                Callback<Object> cb_finish=null;
                ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                final DemoMainActivity act=instActivityHelper.getDemoMainActivity();
                if (act!=null) {
                    act.showProgressDialogAndDismissInTimeSlot(10*1000);
                    cb_finish=new Callback<Object>() {
                        @Override
                        public Void call() throws Exception {
                            act.dismissProgressDialog();
                            return null;
                        }
                    };
                    act.safeStartCommand(commandCode, buf, cb_finish);
                }
            }
        });

        m_btn_genkey=(Button)m_mainll.findViewById(R.id.btn_genkey);
        m_btn_genkey.setVisibility(View.GONE);

        m_input_mrmk_idx=(EditText)m_mainll.findViewById(R.id.s3auth_input_mrmk_idx);
        String idxMrmk= S3_AuthConstant.IDX_MRMK;
        if (m_dataRunTimeS3Auth.m_bSelectMRMK) {
            idxMrmk = S3_AuthConstant.IDX_MRMK;
        }
        else {
            idxMrmk = S3_AuthConstant.IDX_RMK;
        }
        m_input_mrmk_idx.setText(idxMrmk);
        moveCursor2Tail(m_input_mrmk_idx);
        m_input_mrmk_idx.setFocusable(false);

        m_input_mrmk_val=(EditText)m_mainll.findViewById(R.id.s3auth_input_mrmk_val);
        String valMrmk=S3_AuthConstant.MRMK;
        m_input_mrmk_val.setText(valMrmk);
        moveCursor2Tail(m_input_mrmk_val);
        m_input_mrmk_val.setFocusable(false);

        m_input_rmk_idx=(EditText)m_mainll.findViewById(R.id.s3auth_input_rmk_idx);
        String idxRmk=S3_AuthConstant.IDX_RMK;
        m_input_rmk_idx.setText(idxRmk);
        moveCursor2Tail(m_input_rmk_idx);
        m_input_rmk_idx.setFocusable(false);

        m_input_rmk_val=(EditText)m_mainll.findViewById(R.id.s3auth_input_rmk_val);
        String valRmk=S3_AuthConstant.RMK;
        m_input_rmk_val.setText(valRmk);
        moveCursor2Tail(m_input_rmk_val);
        m_input_rmk_val.setFocusable(false);

        m_input_skey_val=(EditText)m_mainll.findViewById(R.id.s3auth_input_skey_val);
        String valSkey=S3_AuthConstant.SKEY;
        m_input_skey_val.setText(valSkey);
        moveCursor2Tail(m_input_skey_val);

        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String strTmp="";

        m_input_trand_val=(EditText)m_mainll.findViewById(R.id.s3auth_input_trand_val);
        //String valTrand=S3_AuthConstant.TRAND;
        String valTrand=instByteHexHelper.bytesArrayToHexString(m_dataRunTimeS3Auth.TRnd);
        m_input_trand_val.setText(valTrand);
        moveCursor2Tail(m_input_trand_val);

        m_input_rrand_val=(EditText)m_mainll.findViewById(R.id.s3auth_input_rrand_val);
        //String valRand=S3_AuthConstant.RRAND;
        String valRand=instByteHexHelper.bytesArrayToHexString(m_dataRunTimeS3Auth.RRnd);
        m_input_rrand_val.setText(valRand);
        moveCursor2Tail(m_input_rrand_val);

        m_input_modemutuauth_val=(EditText)m_mainll.findViewById(R.id.s3auth_input_mode_mutuauth_val);
        //String valModeMutuAuth=S3_AuthConstant.MODE_MUTU_AUTH;
        String valModeMutuAuth=instByteHexHelper.byteToHexString(m_dataRunTimeS3Auth.ModeMutuAuth);
        m_input_modemutuauth_val.setText(valModeMutuAuth);
        moveCursor2Tail(m_input_modemutuauth_val);

        m_container_register_save_btn=(LinearLayout)m_mainll.findViewById(R.id.container_register_save_btn);
    }

    public void refreshUIs() {
        Logger.i(m_className, "refreshUIs");
        if (m_dataRunTimeS3Auth.m_bSelectMRMK) {
            m_radio_mrmk.setChecked(true);
        }
        else {
            m_radio_rmk.setChecked(true);
        }

        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();

        String valTrand=instByteHexHelper.bytesArrayToHexString(m_dataRunTimeS3Auth.TRnd);
        m_input_trand_val.setText(valTrand);
        moveCursor2Tail(m_input_trand_val);

        String valRand=instByteHexHelper.bytesArrayToHexString(m_dataRunTimeS3Auth.RRnd);
        m_input_rrand_val.setText(valRand);
        moveCursor2Tail(m_input_rrand_val);

        String valModeMutuAuth=instByteHexHelper.byteToHexString(m_dataRunTimeS3Auth.ModeMutuAuth);
        m_input_modemutuauth_val.setText(valModeMutuAuth);
        moveCursor2Tail(m_input_modemutuauth_val);
    }

    private void moveCursor2Tail(EditText ed) {
        int pos_amount = ed.getText().length();
        ed.setSelection(pos_amount);
    }

    public void updateRuntimeParameters() {
        if (m_radio_mrmk.isChecked()) {
            m_dataRunTimeS3Auth.m_bSelectMRMK=true;
        }
        else {
            m_dataRunTimeS3Auth.m_bSelectMRMK=false;
        }

        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();

        String strVal="";
        byte[] tmpBuf=null;

        // KType
        if (m_dataRunTimeS3Auth.m_bSelectMRMK) {
            m_dataRunTimeS3Auth.KType=(byte)0x00;
        }
        else {
            m_dataRunTimeS3Auth.KType=(byte)0x00;
        }

        // KIdx
        if (m_dataRunTimeS3Auth.m_bSelectMRMK) {
            strVal=m_input_mrmk_idx.getText().toString();
        }
        else {
            strVal=m_input_rmk_idx.getText().toString();
        }
        if ( (strVal!=null)&&(!strVal.equals("")) ) {
            tmpBuf=instByteHexHelper.hexStringToByteArray(strVal);
            m_dataRunTimeS3Auth.KIdx=tmpBuf[0];
        }
        else {
            m_dataRunTimeS3Auth.KIdx = (byte) 0x00;
        }

        // Key
        if (m_dataRunTimeS3Auth.m_bSelectMRMK) {
            strVal=m_input_mrmk_val.getText().toString();
        }
        else {
            strVal=m_input_rmk_val.getText().toString();
        }
        if ( (strVal!=null)&&(!strVal.equals("")) ) {
            tmpBuf=instByteHexHelper.hexStringToByteArray(strVal);
            m_dataRunTimeS3Auth.Key=tmpBuf;
        }
        else {
            m_dataRunTimeS3Auth.Key=null;
        }

        // TRnd
        strVal=m_input_trand_val.getText().toString();
        if ( (strVal!=null)&&(!strVal.equals("")) ) {
            tmpBuf=instByteHexHelper.hexStringToByteArray(strVal);
            m_dataRunTimeS3Auth.TRnd=tmpBuf;
        }
        else {
            m_dataRunTimeS3Auth.TRnd=null;
        }

        // RRnd
        strVal=m_input_rrand_val.getText().toString();
        if ( (strVal!=null)&&(!strVal.equals("")) ) {
            tmpBuf=instByteHexHelper.hexStringToByteArray(strVal);
            m_dataRunTimeS3Auth.RRnd=tmpBuf;
        }
        else {
            m_dataRunTimeS3Auth.RRnd=null;
        }

        // ModeMutuAuth
        strVal=m_input_modemutuauth_val.getText().toString();
        if ( (strVal!=null)&&(!strVal.equals("")) ) {
            tmpBuf=instByteHexHelper.hexStringToByteArray(strVal);
            m_dataRunTimeS3Auth.ModeMutuAuth=tmpBuf[0];
        }
        else {
            m_dataRunTimeS3Auth.ModeMutuAuth=(byte)0x00;
        }
    }

}
