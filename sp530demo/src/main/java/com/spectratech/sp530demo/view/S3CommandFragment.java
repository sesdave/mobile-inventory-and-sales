package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.data.Data_viewCommand;
import com.spectratech.sp530demo.data.Data_viewCommandList;

import java.util.ArrayList;

/**
 * S3CommandFragment - fragment for command of SP530
 */
public class S3CommandFragment extends S3CommonFragment {

    private static final String m_className="S3CommandFragment";

    private boolean m_bCreate;

    private int m_posCurrentCommand;

    private LinearLayout m_mainll;

    private TextView m_tv_command_dropdown;

    //private CommandListView m_commandlv;
    //private LinearLayout m_btnScroll2CurrentCommandPos;
    private EditText m_et_inputcmd;

    private EditText m_inputAmountet;
    private LinearLayout m_btnClearAmount;

    private LinearLayout m_btnStart;

    private LinearLayout m_btnLog;

    private LinearLayout m_btn_view_responsedata;

    private static Data_viewCommandList DATAVIEWCOMMANDLIST;

    private ArrayAdapter<Data_viewCommand> m_adapter_dropdown;

    private boolean m_bPostponseInputCmdTextChangedTrigger;

    private Toast m_toast;

    public static Data_viewCommandList getViewCommandList() {
        if (DATAVIEWCOMMANDLIST==null) {
            DATAVIEWCOMMANDLIST=new Data_viewCommandList();
        }
        return DATAVIEWCOMMANDLIST;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity act=getActivity();
        m_context=(Context)act;
        m_posCurrentCommand=-1;

        m_bPostponseInputCmdTextChangedTrigger=false;

        m_toast=null;

        DATAVIEWCOMMANDLIST=new Data_viewCommandList();
        ArrayList<Data_viewCommand> listTitleArray=DATAVIEWCOMMANDLIST.getList();
        m_adapter_dropdown=new ArrayAdapter<Data_viewCommand>(m_context, android.R.layout.simple_spinner_dropdown_item, listTitleArray);

        m_bCreate=true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_mainll = (LinearLayout)inflater.inflate(R.layout.view_s3command, container, false);

        View rootView = m_mainll;
        Bundle args = getArguments();

//        m_commandlv=(CommandListView)rootView.findViewById(R.id.list);
//        m_commandlv.requestFocus();
//        m_commandlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                selectCommand(position);
//            }
//        });
//
//        m_commandlv.setOnTouchListener(new View.OnTouchListener() {
//            // Setting on Touch Listener for handling the touch inside ScrollView
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // Disallow the touch request for parent scroll on touch of child view
//                v.getParent().requestDisallowInterceptTouchEvent(true);
//                return false;
//            }
//        });

//        m_btnScroll2CurrentCommandPos=(LinearLayout)rootView.findViewById(R.id.btn_scrolltocurrentcommandpos);
//        m_btnScroll2CurrentCommandPos.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (m_posCurrentCommand > -1) {
//                    m_commandlv.smoothScrollToPosition(m_posCurrentCommand);
//                }
//            }
//        });

        m_et_inputcmd=(EditText)rootView.findViewById(R.id.et_inputcmd);
        m_et_inputcmd.addTextChangedListener(new TextWatcher() {
             public void afterTextChanged(Editable s) {
                 //Logger.i(m_className, "m_et_inputcmd, afterTextChanged");
                 if (m_bPostponseInputCmdTextChangedTrigger) {
                     m_bPostponseInputCmdTextChangedTrigger = false;
                 }
             }
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                 //Logger.i(m_className, "m_et_inputcmd, beforeTextChanged");
             }
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 //Logger.i(m_className, "m_et_inputcmd, onTextChanged");
                 if (!m_bPostponseInputCmdTextChangedTrigger) {
                     if (m_posCurrentCommand > -1) {
                         m_posCurrentCommand = -1;
                         setDropDownTextView(m_tv_command_dropdown, m_adapter_dropdown, m_posCurrentCommand);
                     }
                 }
             }
         });

        m_inputAmountet=(EditText)rootView.findViewById(R.id.input_amount);
        m_inputAmountet.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        m_inputAmountet.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                }
                return false;
            }
        });

        m_btnClearAmount=(LinearLayout)rootView.findViewById(R.id.btn_clearamount);
        m_btnClearAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_inputAmountet.setText("");
            }
        });

        m_btnStart=(LinearLayout)rootView.findViewById(R.id.btn_start);
        {
            OnClickListener listener = initStartOnclickListener();
            m_btnStart.setOnClickListener(listener);
        }
        m_btnLog=(LinearLayout)rootView.findViewById(R.id.btn_view_log);
        m_btnLog.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (m_context instanceof DemoMainActivity) {
                    DemoMainActivity act=(DemoMainActivity)m_context;
                    act.startViewLogHistory();
                }
            }
        });

        m_btn_view_responsedata=(LinearLayout)rootView.findViewById(R.id.btn_view_responsedata);
        m_btn_view_responsedata.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (m_context instanceof DemoMainActivity) {
                    DemoMainActivity act=(DemoMainActivity)m_context;
                    act.showS3INSResponseData();
                }
            }
        });

        if (m_bCreate) {
            if (DATAVIEWCOMMANDLIST.getCount() > 0) {
                m_posCurrentCommand = 0;
                setCurrentCommand();
            }
        }

        m_bPostponseInputCmdTextChangedTrigger=true;
        initDropDownUIs();

//        if (m_commandlv.getCount() > 0) {
//            m_commandlv.setItemChecked(m_posCurrentCommand, true);
//            m_commandlv.smoothScrollToPosition(m_posCurrentCommand);
//        }

        return rootView;
    }

    private void ToastMessage(String strMsg) {
        if (m_toast!=null) {
            m_toast.cancel();
            m_toast=null;
        }
        m_toast=Toast.makeText(m_context, strMsg, Toast.LENGTH_SHORT);
        m_toast.show();;
    }

    private boolean isValidCommandCodeString(String strInput) {
        boolean bValid=false;
        if (strInput==null) {
            Logger.d(m_className, "isValidCommandCodeString, strInput is null");
            return bValid;
        }
        if (strInput.length()!=2) {
            Logger.d(m_className, "isValidCommandCodeString, strInput.length()!=2");
            return bValid;
        }
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        byte[] cmdCodeArray=instByteHexHelper.hexStringToByteArray(strInput);
        if (cmdCodeArray==null) {
            Logger.d(m_className, "isValidCommandCodeString, cmdCodeArray is null");
            return bValid;
        }
        if (cmdCodeArray.length==1) {
            bValid=true;
        }
        return bValid;
    }

    private byte[] getDataByteArrayFromUI(EditText et) {
        String strInput=et.getText().toString();
        if (strInput==null) {
            return null;
        }
        String x=strInput.replaceAll("[ \\r\\n]", "");
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        byte[] buf=instByteHexHelper.hexStringToByteArray(x);
        return buf;
    }

    private OnClickListener initStartOnclickListener() {
        final String strCancel= ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        final String strOK=ResourcesHelper.getBaseContextString(m_context, R.string.ok);
        final String strInputCmdInvalid=ResourcesHelper.getBaseContextString(m_context, R.string.input_command_is_invalid);

        OnClickListener listener=null;

        listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strByte=m_et_inputcmd.getText().toString();
                boolean bValidCmdCodeString=isValidCommandCodeString(strByte);
                if (!bValidCmdCodeString) {
                    ToastMessage(strInputCmdInvalid+"!");
                    return;
                }

                //byte commandCode=m_commandlv.getCommandCode(m_posCurrentCommand);
                ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                byte[] cmdCodeArray=instByteHexHelper.hexStringToByteArray(strByte);
                byte commandCode=cmdCodeArray[0];

                Logger.i(m_className, "Button start, commandCode, val: "+(commandCode&0xFF));

                byte[] dataBuf=getDataByteArrayFromUI(m_inputAmountet);

                if (m_context instanceof DemoMainActivity) {
                    Callback<Object> cb_finish=new Callback<Object>() {
                        @Override
                        public Object call() throws Exception {
                            Logger.i(m_className, "callback finish");
                            DemoMainActivity act=(DemoMainActivity)m_context;
                            S3LogOverlayFragment frag=act.getLogOverlayFragment();
                            if (frag!=null) {
                                frag.setButtonTextByPost(strOK);
                            }
                            return null;
                        }
                    };

                    DemoMainActivity act=(DemoMainActivity)m_context;
                    S3LogOverlayFragment frag=act.getLogOverlayFragment();
                    frag.setButtonTextByPost(strCancel);
                    frag.clearLog();

                    act.safeStartCommand(commandCode, dataBuf, cb_finish);

                    act.disableAllUIInputs();
                    act.setLogOverlayFragment();
                }
            }
        };

        return listener;
    }

//        private void selectCommand(int position) {
//        m_posCurrentCommand=position;
//        setCurrentCommand();
//    }
    private void setCurrentCommand() {
        byte cmd = DATAVIEWCOMMANDLIST.getCommandCode(m_posCurrentCommand);
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String strMsg=instByteHexHelper.byteToHexString(cmd);
        m_bPostponseInputCmdTextChangedTrigger=true;
        m_et_inputcmd.setText(strMsg);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (m_bCreate) {
            m_bCreate=false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            if (isVisibleToUser) {
                ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                DemoMainActivity act=instActivityHelper.getDemoMainActivity();
                if (act!=null) {
                    act.hideSoftKeyboard();
                }
            }
        }
    }

    private void setMessage(String strMsg) {
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act=(DemoMainActivity)m_context;
            act.setMessage(strMsg);
        }
    }
    private void setMessageWithAlertColor(String strMsg) {
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act=(DemoMainActivity)m_context;
            act.setMessageWithAlertColor(strMsg);
        }
    }


    private void initDropDownUIs() {
        final String strCommandList=ResourcesHelper.getBaseContextString(m_context, R.string.command_list);
        m_tv_command_dropdown = (TextView) m_mainll.findViewById(R.id.tv_command_dropdown);
        {
            final String title=strCommandList;
            if ( (m_tv_command_dropdown!=null)&&(m_adapter_dropdown!=null) ) {
                setDropDownTextView(m_tv_command_dropdown, m_adapter_dropdown, m_posCurrentCommand);
                m_tv_command_dropdown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialog=new AlertDialog.Builder(m_context);
                        dialog.setTitle(title);
                        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_posCurrentCommand=which;
                                setDropDownTextView(m_tv_command_dropdown, m_adapter_dropdown, m_posCurrentCommand);
                                setCurrentCommand();
                                dialog.dismiss();
                            }
                        };

                        if (m_posCurrentCommand<0) {
                            dialog.setAdapter(m_adapter_dropdown, listener).show();
                        }
                        else {
                            dialog.setSingleChoiceItems(m_adapter_dropdown, m_posCurrentCommand, listener).show();
                        }
                    }
                });
            }
        }
    }

    private void setDropDownTextView(TextView tv, ArrayAdapter<Data_viewCommand> adapter, int which) {
        if (which>-1) {
            Data_viewCommand x = adapter.getItem(which);
            String val=x.toString();
            tv.setText(val);
            int idx = which;
            tv.setTag("" + idx);
        }
        else {
            tv.setText("");
            tv.setTag(null);
        }
    }
}
