package com.spectratech.sp530demo;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.spectratech.lib.LanguageHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.controller.ActivityHelper;

import java.util.HashMap;

import static com.spectratech.sp530demo.SP530DemoBaseApplication.KEY_SP_LANGUAGEPREFERENCE;

/**
 * SettingLanguageActivity - setting language activity
 */
public class SettingLanguageActivity extends SP530DemoBaseActivity {

    private static final String m_className="SettingLanguageActivity";

    private LinearLayout m_mainll;

    private RadioGroup m_rg_lang;

    private Button m_btn_unselect;

    private HashMap<String, RadioButton> m_langRadioButtonHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_settinglanguage, null);
        setContentView(m_mainll);

        m_langRadioButtonHash=new HashMap<String, RadioButton>();

        m_rg_lang=(RadioGroup)m_mainll.findViewById(R.id.rg_language);

        RadioButton rb;
        rb=(RadioButton)m_mainll.findViewById(R.id.rb_lanuage_en);
        m_langRadioButtonHash.put((String)rb.getTag(), rb);

        rb=(RadioButton)m_mainll.findViewById(R.id.rb_lanuage_zh);
        m_langRadioButtonHash.put((String)rb.getTag(), rb);

        ContextWrapper cw=new ContextWrapper(m_context);
        String strLocalLanguage=LanguageHelper.getLocaleStringFromPreference(cw, KEY_SP_LANGUAGEPREFERENCE);
        rb=m_langRadioButtonHash.get(strLocalLanguage);
        if (rb!=null) {
            rb.setChecked(true);
        }

        m_btn_unselect=(Button)m_mainll.findViewById(R.id.btn_unselect);
        {
            View.OnClickListener listener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_rg_lang!=null) {
                        m_rg_lang.clearCheck();
                    }
                }
            };
            m_btn_unselect.setOnClickListener(listener);
        }

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    @Override
    public void onDestroy() {
        if (m_langRadioButtonHash!=null) {
            m_langRadioButtonHash=null;
        }

        super.onDestroy();
    }

    private String getSelectedLocaleLanguage() {
        String result=null;
        int id=getSelectedRadioButtonId();
        if (id>-1) {
            RadioButton rb=(RadioButton)m_mainll.findViewById(id);
            if (rb!=null) {
                result=(String)rb.getTag();
            }
        }
        return result;
    }
    private int getSelectedRadioButtonId() {
        int id=m_rg_lang.getCheckedRadioButtonId();
        return id;
    }

    @Override
    public void onBackPressed() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        Toast.makeText(m_context, strCancel, Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    /**
     * On click function for saving parameters and finish this activity
     * @param v corresponding view
     */
    public void onProcess(View v) {
        Logger.i(m_className, "onProcess");
        final String strParamSaved=ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);
        final String strPleaseRestart=ResourcesHelper.getBaseContextString(m_context, R.string.please_restart);
        final String strRestart=ResourcesHelper.getBaseContextString(m_context, R.string.restart);
        final String strYes=ResourcesHelper.getBaseContextString(m_context, R.string.yes);
        final String strNo=ResourcesHelper.getBaseContextString(m_context, R.string.no);

        // save to preference
        String strLanguageCode=getSelectedLocaleLanguage();
        if (strLanguageCode==null) {
            strLanguageCode="";
        }
        ContextWrapper cw=new ContextWrapper(m_context.getApplicationContext());
        LanguageHelper.set2Preference(cw, KEY_SP_LANGUAGEPREFERENCE, strLanguageCode);

        Toast.makeText(m_context, strParamSaved+", "+strPleaseRestart, Toast.LENGTH_SHORT).show();

        // ask for restarting?
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                        instActivityHelper.finishAllActivities();

                        //restart
                        Intent intent = new Intent(m_context, OpeningActivity.class);
                        m_context.startActivity(intent);
                    }
                    break;
                    case DialogInterface.BUTTON_NEGATIVE: {
                        Toast.makeText(m_context, strPleaseRestart, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setMessage(strRestart+"?").setPositiveButton(strYes, dialogClickListener)
                .setNegativeButton(strNo, dialogClickListener)
                .setCancelable(false).show();
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
}
