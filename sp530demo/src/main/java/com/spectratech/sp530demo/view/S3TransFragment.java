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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.data.Data_setting_devicebluetoothprinter;
import com.spectratech.sp530demo.data.Data_setting_general;

import java.util.ArrayList;
import java.util.List;

/**
 * S3TransFragment - fragment for SP530 transaction
 */
public class S3TransFragment extends S3CommonFragment {

    private static final String m_className="S3TransFragment";

    private boolean m_bCreate;
    private boolean m_bPause;

    private RelativeLayout m_inputAmountContainer;
    private FixKeyboardEditText m_inputAmountet;

    private LinearLayout m_btnStartTrans;
    private LinearLayout m_btnStartTransBatch;
    private LinearLayout m_btnClearAmount;

    private LinearLayout m_btnViewTransHistory;

    private LinearLayout m_btnDemoTransReceipt;

    private int m_messageCallCount;
    private boolean m_bClearMessageOnNextCall;

    private TextWatcher m_textWatcherParse;
    private TextWatcher m_textWatcher;

    private LinearLayout m_transaction_user_input_setone;

    private TextView m_textStatus;

    private LinearLayout m_textDevicell;
    private TextView m_textDevice;

    private LinearLayout m_textPrinterll;
    private TextView m_textPrinter;

    private String m_strMessageLocal;
    private String m_strMessageDevice;
    private String m_strMessagePrinter;

    private LinearLayout m_landscape_bluetooth_container;
    private LinearLayout m_landscape_printer_bluetooth_container;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity act=getActivity();
        m_context=(Context)act;

        m_bCreate=true;
        m_bPause=false;

        m_messageCallCount=-1;
        m_bClearMessageOnNextCall=false;

        m_strMessageLocal="";
        m_strMessageDevice="";
        m_strMessagePrinter="";

        m_landscape_bluetooth_container=null;
        m_landscape_printer_bluetooth_container=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            rootView = inflater.inflate(R.layout.view_s3trans_land, container, false);
            m_textStatus=(TextView)rootView.findViewById(R.id.text_status);
            m_textDevicell=(LinearLayout)rootView.findViewById(R.id.text_device_ll);
            m_textDevice=(TextView)rootView.findViewById(R.id.text_device);
            m_textPrinterll=(LinearLayout)rootView.findViewById(R.id.text_printer_ll);
            m_textPrinter=(TextView)rootView.findViewById(R.id.text_printer);
            if (m_textStatus!=null) {
                m_textStatus.setText(m_strMessageLocal);
            }
            if (m_textDevice!=null) {
                m_textDevice.setText(m_strMessageDevice);
            }
            if (m_textPrinter!=null) {
                m_textPrinter.setText(m_strMessagePrinter);
            }
            ActivityHelper instActivityHelper=ActivityHelper.getInstance();
            DemoMainActivity act=instActivityHelper.getDemoMainActivity();
            if (act!=null) {
                ImageView ivBluetooth = (ImageView) rootView.findViewById(R.id.row_bluetooth_iv);
                if (ivBluetooth != null) {
                    ivBluetooth.setOnClickListener(act.m_btrowOnClickListener);
                }
                ImageView ivBluetoothPrinter = (ImageView) rootView.findViewById(R.id.row_printer_bluetooth_iv);
                if (ivBluetoothPrinter != null) {
                    ivBluetoothPrinter.setOnClickListener(act.m_btrowPrinterOnClickListener);
                }
            }

            m_landscape_bluetooth_container=(LinearLayout)rootView.findViewById(R.id.row_bluetooth_container);
            m_landscape_printer_bluetooth_container=(LinearLayout)rootView.findViewById(R.id.row_printer_bluetooth_container);

            if (act!=null) {
                if (m_landscape_bluetooth_container != null) {
                    m_landscape_bluetooth_container.setOnClickListener(act.m_btrowOnClickListener);
                }
                if (m_textDevicell!=null) {
                    m_textDevicell.setOnClickListener(act.m_btrowOnClickListener);
                }
            }
            if (act!=null) {
                if (m_landscape_printer_bluetooth_container != null) {
                    m_landscape_printer_bluetooth_container.setOnClickListener(act.m_btrowPrinterOnClickListener);
                }
                if (m_textPrinterll!=null) {
                    m_textPrinterll.setOnClickListener(act.m_btrowPrinterOnClickListener);
                }
            }
        }
        else {
            rootView = inflater.inflate(R.layout.view_s3trans, container, false);
            m_textStatus=null;
            m_textDevice=null;
            m_textPrinter=null;
        }
        Bundle args = getArguments();

        m_inputAmountContainer=(RelativeLayout)rootView.findViewById(R.id.input_amount_container);

        m_textWatcherParse=new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Logger.i(m_className, "m_textWatcherParse, afterTextChanged called, s: "+s);
                S3TransFragment.this.parseInputAmountText();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Logger.i(m_className, "m_textWatcherParse, beforeTextChanged called, s: "+s);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Logger.i(m_className, "m_textWatcherParse, onTextChanged called, s: "+s);
            }
        };
        m_textWatcher=new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Logger.i(m_className, "m_textWatcher, afterTextChanged called, s: "+s);
                m_inputAmountet.removeTextChangedListener(this);
                m_inputAmountet.addTextChangedListener(m_textWatcherParse);

                String tmp = m_inputAmountet.getTextRaw();
                int len = tmp.length();
                int MAX_LENGTH = m_context.getResources().getInteger(R.integer.max_input_amount_digits);
                if (len >= MAX_LENGTH) {
                    setMessage("Maximum number of digits (" + MAX_LENGTH + ") has been reached");
                    m_bClearMessageOnNextCall = true;
                } else {
                    if (m_bClearMessageOnNextCall) {
                        clearMessageWithMessageCallCountValidation();
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Logger.i(m_className, "m_textWatcher, beforeTextChanged called, s: "+s);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Logger.i(m_className, "m_textWatcher, onTextChanged called, s: "+s);
            }
        };

        m_inputAmountet=(FixKeyboardEditText)rootView.findViewById(R.id.input_amount);
        m_inputAmountet.addTextChangedListener(m_textWatcherParse);
        m_inputAmountet.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    startTransaction();
                }
                return false;
            }
        });
        parseInputAmountText();

//        m_inputAmountet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    m_inputAmountContainer.setBackgroundResource(R.drawable.login_roundcorner_focus);
//                }
//                else {
//                    m_inputAmountContainer.setBackgroundResource(R.drawable.login_roundcorner_nofocus);
//                }
//            }
//        });
        m_inputAmountet.setFocusable(false);
        m_inputAmountet.setFocusableInTouchMode(false);

        m_btnStartTrans=(LinearLayout)rootView.findViewById(R.id.btn_starttransaction);
        if (m_btnStartTrans!=null) {
            OnClickListener listener = initStartTransOnclickListener();
            m_btnStartTrans.setOnClickListener(listener);

            OnLongClickListener longClickListener=initStartTransOnLongClickListener();
            m_btnStartTrans.setOnLongClickListener(longClickListener);
        }

        m_btnStartTransBatch=(LinearLayout)rootView.findViewById(R.id.btn_starttransaction_batch);
        if (m_btnStartTransBatch!=null) {
            OnClickListener listener = initStartTransBatchOnclickListener();
            m_btnStartTransBatch.setOnClickListener(listener);
        }


        m_btnClearAmount=(LinearLayout)rootView.findViewById(R.id.btn_clearamount);
        m_btnClearAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_inputAmountet.setText("");
            }
        });

        m_btnViewTransHistory=(LinearLayout)rootView.findViewById(R.id.btn_view_trans_history);
        if (m_btnViewTransHistory!=null) {
            m_btnViewTransHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_context instanceof DemoMainActivity) {
                        DemoMainActivity act = (DemoMainActivity) m_context;
                        act.startViewTransactionHistory();
                    }
                }
            });
        }

        m_btnDemoTransReceipt=(LinearLayout)rootView.findViewById(R.id.btn_view_trans_receipt_demo);
        if (m_btnDemoTransReceipt!=null) {
          /*  m_btnDemoTransReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_context instanceof DemoMainActivity) {
                        DemoMainActivity act = (DemoMainActivity) m_context;
                        act.startDemoTransactionReceipt();
                    }
                }
            });*/
        }

        initNumPad(rootView);

        m_transaction_user_input_setone=(LinearLayout)rootView.findViewById(R.id.transaction_user_input_setone);

        return rootView;
    }


    private List<LinearLayout> m_numpadViewList;
    private void initNumPad(View rootView) {
        m_numpadViewList=new ArrayList<LinearLayout>();

        OnClickListener listener=new OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagString=(String)v.getTag();
                if (tagString.equals("del")) {
                    removeCharInputAmountText();
                }
                else if (tagString.equals("go")) {
                    startTransaction();
                }
                else {
                    append2InputAmountText(tagString);
                }
            }
        };
        LinearLayout ll=null;
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_zero);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_one);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_two);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_three);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_four);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_five);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_six);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_seven);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_eight);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_nine);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_del);
        ll.setOnClickListener(listener);
        m_numpadViewList.add(ll);
        ll=(LinearLayout)rootView.findViewById(R.id.numpad_go);
        ll.setOnClickListener(listener);
        OnLongClickListener longClickListener=initStartTransOnLongClickListener();
        ll.setOnLongClickListener(longClickListener);
        m_numpadViewList.add(ll);
    }

    public void setInputAmountAndStartTransaction(String s) {
        setInputAmount(s);
        startTransaction();
    }

    public void setInputAmount(String s) {
        m_inputAmountet.setText(s);
    }

    private void removeCharInputAmountText() {
        String s=m_inputAmountet.getTextRaw();
        if ((s==null)||(s.length()<1)) {
            return;
        }
        s=s.substring(0,s.length()-1);
        parseInputAmountText(s);
    }
    private void append2InputAmountText(String inString) {
        if ( (inString==null)||(inString.equals("")) ) {
            Logger.i(m_className, "parseInputAmountText, inString is NULL");
            return;
        }
        String s=m_inputAmountet.getTextRaw();
        int MAX_LENGTH = m_context.getResources().getInteger(R.integer.max_input_amount_digits);
        if ( (s!=null)&&(s.length()>=MAX_LENGTH) ) {
            return;
        }
        s+=inString;
        parseInputAmountText(s);
    }
    private void parseInputAmountText() {
        String s=m_inputAmountet.getTextRaw();
        int MAX_LENGTH = m_context.getResources().getInteger(R.integer.max_input_amount_digits);
        if ( (s!=null)&&(s.length()>=MAX_LENGTH) ) {
            return;
        }
        parseInputAmountText(s);
    }
    private void parseInputAmountText(String s) {
        int len=s.length();
        if (len<1) {
            return;
        }

        long val=getLongFromString(s);


        if (val<0) {
            Logger.i(m_className, "case 1, length: "+len);
            switchTextChangedListener2Phasetwo();
            m_inputAmountet.setText("");
            return;
        }

        String textToBeSet=null;
        if (val==0) {
            Logger.i(m_className, "case 2, length: "+len);
            if (len<2) {
                textToBeSet="0";
            }
            else {
                textToBeSet="0.0";
            }
        } else {
            Logger.i(m_className, "case 3, length: "+len);
            textToBeSet=""+val;
            if (textToBeSet.length()<2) {
                textToBeSet="00"+textToBeSet;
            }
            else if (textToBeSet.length()<3) {
                textToBeSet="0"+textToBeSet;
            }
            textToBeSet=textToBeSet.substring(0, textToBeSet.length()-2)+"."+textToBeSet.substring(textToBeSet.length()-2);
        }

        if (textToBeSet!=null) {
            switchTextChangedListener2Phasetwo();
            m_inputAmountet.setText(textToBeSet);
            moveInputAmountCursor2Tail();
        }
    }

    private void switchTextChangedListener2Phasetwo() {
        m_inputAmountet.removeTextChangedListener(m_textWatcherParse);
        m_inputAmountet.addTextChangedListener(m_textWatcher);
    }

    private void moveInputAmountCursor2Tail() {
        int pos_amount = m_inputAmountet.getText().length();
        m_inputAmountet.setSelection(pos_amount);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_bPause=true;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            if (m_landscape_printer_bluetooth_container!=null) {
                if (Data_setting_devicebluetoothprinter.isEnableBTPrinter(m_context)) {
                    if (m_landscape_printer_bluetooth_container.getVisibility()!=View.VISIBLE) {
                        m_landscape_printer_bluetooth_container.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    if (m_landscape_printer_bluetooth_container.getVisibility()!=View.INVISIBLE) {
                        m_landscape_printer_bluetooth_container.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }

        if (m_transaction_user_input_setone!=null) {
            boolean bTransactionUIShowMore = Data_setting_general.isTransactionUIShowMore(m_context);
            int currentVis = m_transaction_user_input_setone.getVisibility();
            if (bTransactionUIShowMore) {
                if (currentVis != View.VISIBLE) {
                    m_transaction_user_input_setone.setVisibility(View.VISIBLE);
                }
            } else {
                if (currentVis != View.GONE) {
                    m_transaction_user_input_setone.setVisibility(View.GONE);
                }
            }
        }

        m_bCreate=false;
        if (m_bPause) {
            m_bPause=false;
        }
    }

    public EditText getViewInputAmountEditText() {
        return m_inputAmountet;
    }

    private long getInputAmountLong() {
        String s=m_inputAmountet.getTextRaw();
        return getLongFromString(s);
    }
    private long getLongFromString(String s) {
        long l = -1;
        if ( (s==null)||(s.equals("")) ) {
            return l;
        }
        try {
            l = Long.parseLong(s);
        }
        catch (NumberFormatException ex) {
            Logger.e(m_className, "getLongFromString, NumberFormatException: "+ex.toString());
            l=-1;
        }
        return l;
    }

    private OnClickListener initStartTransBatchOnclickListener() {
        OnClickListener listener=null;

        listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTransactionBatch();
            }
        };

        return listener;
    }

    private OnLongClickListener initStartTransOnLongClickListener() {
        OnLongClickListener listener=null;

        listener=new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startTransactionBySelectDevice();
                return true;
            }
        };

        return listener;
    }
    private OnClickListener initStartTransOnclickListener() {
        OnClickListener listener=null;

        listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTransaction();
            }
        };

        return listener;
    }

    private void startTransactionBatch() {
        final String strStartBatchTransactions=ResourcesHelper.getBaseContextString(m_context, R.string.start_batch_transactions);
        final String strYes=ResourcesHelper.getBaseContextString(m_context, R.string.yes);
        final String strNo=ResourcesHelper.getBaseContextString(m_context, R.string.no);

        boolean bValid2Trans=checkValidTransactionProcess();
        if (bValid2Trans) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            byte[] dataBuf=getInputByteArray(m_inputAmountet);
                            if (m_context instanceof DemoMainActivity) {
                                DemoMainActivity act=(DemoMainActivity)m_context;
                                act.startTransaction_s3trans_batch(dataBuf);
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
            builder.setMessage(strStartBatchTransactions+"?").setPositiveButton(strYes, dialogClickListener)
                    .setNegativeButton(strNo, dialogClickListener).show();
        }
    }

    private void startTransactionBySelectDevice() {
        boolean bValid2Trans=checkValidTransactionProcess();
        if (bValid2Trans) {
            byte[] dataBuf=getInputByteArray(m_inputAmountet);
            if (m_context instanceof DemoMainActivity) {
                DemoMainActivity act=(DemoMainActivity)m_context;
                act.startTransaction_s3transBySelectDevice(dataBuf);
            }
        }
    }

    private void startTransaction() {
        boolean bValid2Trans=checkValidTransactionProcess();
        if (bValid2Trans) {
            byte[] dataBuf=getInputByteArray(m_inputAmountet);
            if (m_context instanceof DemoMainActivity) {
                DemoMainActivity act=(DemoMainActivity)m_context;
                act.startTransaction_s3trans(dataBuf);
            }
        }
    }

    private void clearMessageWithMessageCallCountValidation() {
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act=(DemoMainActivity)m_context;
            int messageCallCount=act.getMessageCallCount();
            if (messageCallCount==m_messageCallCount) {
                act.setMessage(" ");
                m_messageCallCount=act.getMessageCallCount();
            }
        }
        m_bClearMessageOnNextCall=false;
    }
    private void setMessage(String strMsg) {
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act=(DemoMainActivity)m_context;
            act.setMessage(strMsg);
            m_messageCallCount=act.getMessageCallCount();
        }
        m_bClearMessageOnNextCall=false;
    }
    private void setMessageWithAlertColor(String strMsg) {
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act=(DemoMainActivity)m_context;
            act.setMessageWithAlertColor(strMsg);
            m_messageCallCount=act.getMessageCallCount();
        }
        m_bClearMessageOnNextCall=false;
    }

    private boolean checkValidTransactionProcess() {
        final String strPleaseInputCorrrectAmt=ResourcesHelper.getBaseContextString(m_context, R.string.please_input_correct_amount);

        boolean bValid=false;

        bValid=checkValidInputAmount();
        if (!bValid) {
            Logger.i(m_className, "checkValidTransactionProcess, checkValidInputAmount is FALSE");
            setMessageWithAlertColor(strPleaseInputCorrrectAmt);
            return bValid;
        }

        return bValid;
    }
    private boolean checkValidInputAmount() {
        boolean bRet=false;
        long l=getInputAmountLong();
        if (l>0) {
            bRet=true;
        }
        return bRet;
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

    @Override
    public void setMessageLocal(String strMsg) {
        m_strMessageLocal=strMsg;
        if (m_textStatus!=null) {
            m_textStatus.setText(m_strMessageLocal);
        }
    }

    @Override
    public void setMessageDeviceLocal(String strMsg) {
        m_strMessageDevice=strMsg;
        if (m_textDevice!=null) {
            m_textDevice.setText(m_strMessageDevice);
        }
    }

    @Override
    public void setMessagePrinterLocal(String strMsg) {
        m_strMessagePrinter=strMsg;
        if (m_textPrinter!=null) {
            m_textPrinter.setText(m_strMessagePrinter);
        }
    }


}
