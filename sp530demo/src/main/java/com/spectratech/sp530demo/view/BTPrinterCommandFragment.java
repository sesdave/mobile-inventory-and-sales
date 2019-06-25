package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.printer.bp80.Lpt_funcCHelper;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.BTPrinterBluetoothConnectActivity;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.data.Data_logdatapacket;
import com.spectratech.sp530demo.data.Data_setting_devicebluetoothprinter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * BTPrinterCommandFragment - fragment for Bluetooth printer
 */
public class BTPrinterCommandFragment extends S3CommonFragment {

    private static final String m_className="BTPrinterCommandFragment";

    private int m_posCurrentCommand;

    private BTPrinterCommandListView m_commandlv;
    private LinearLayout m_btnScroll2CurrentCommandPos;
    private TextView m_selecttv;

    private EditText m_inputAmountet;
    private LinearLayout m_btnClearAmount;

    private LinearLayout m_btnStart;

    private LinearLayout m_btnLog;

    private LinearLayout m_btnStartInfMFGPrint;
    private LinearLayout m_btnStopInfMFGPrint;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity act=getActivity();
        m_context=(Context)act;
        m_posCurrentCommand=-1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_btprintercommand, container, false);
        Bundle args = getArguments();

        final String strPleaseConnectBTPrinter= ResourcesHelper.getBaseContextString(m_context, R.string.please_connect_bt_printer);

        m_commandlv=(BTPrinterCommandListView)rootView.findViewById(R.id.list);
        m_commandlv.requestFocus();
        m_commandlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectCommand(position);
            }
        });

        m_commandlv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        m_btnScroll2CurrentCommandPos=(LinearLayout)rootView.findViewById(R.id.btn_scrolltocurrentcommandpos);
        m_btnScroll2CurrentCommandPos.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_posCurrentCommand > -1) {
                    m_commandlv.smoothScrollToPosition(m_posCurrentCommand);
                }
            }
        });

        m_selecttv=(TextView)rootView.findViewById(R.id.tv_valselect);

        m_inputAmountet=(EditText)rootView.findViewById(R.id.input_amount);
        m_inputAmountet.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                }
                return false;
            }
        });

        m_btnClearAmount=(LinearLayout)rootView.findViewById(R.id.btn_clearamount);
        m_btnClearAmount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_inputAmountet.setText("");
            }
        });

        m_btnStart=(LinearLayout)rootView.findViewById(R.id.btn_start);
        {
            //OnClickListener listener = initStartOnclickListener();
            //m_btnStart.setOnClickListener(listener);
            m_btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bReadyPrinter= BTPrinterBluetoothConnectActivity.isReadyPrinter();
                    if (!bReadyPrinter) {
                        Toast.makeText(m_context, strPleaseConnectBTPrinter, Toast.LENGTH_SHORT).show();
                        BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
                        return;
                    }
                    Lpt_funcCHelper instLpt_funcCHelper= Lpt_funcCHelper.getInstance();

                    Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
                    instLpt_funcCHelper.setPacketEncapsulateLevel(dataBTPrinterConf.m_packetEncapsulateLevel);
                    BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
                    InputStream is=btSocketClass.getInputStream();
                    OutputStream os=btSocketClass.getOutputStream();
                    instLpt_funcCHelper.setIO(is, os);
                    instLpt_funcCHelper.setContext(m_context);

                    final byte[] commandCodeArray=m_commandlv.getCommandCode(m_posCurrentCommand);
                    Callback<Object> cb_response=new Callback<Object>() {
                        @Override
                        public Void call() throws Exception {
                            Object obj = this.getParameter();
                            if (obj instanceof byte[]) {
                                final byte[] packetBuf=(byte[])obj;
                                // add to log
                                Data_logdatapacket logdataPacket=new Data_logdatapacket(packetBuf, false);
                                Application.add2LogMemory(logdataPacket);

                                ((Activity)m_context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                                        String strHex=instByteHexHelper.bytesArrayToHexString(commandCodeArray);
                                        String strData=instByteHexHelper.bytesArrayToHexString(packetBuf);
                                        Toast.makeText(m_context, "CMD: "+strHex+", finish with data: "+strData, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            return null;
                        }
                    };
                    //byte[] packetBuf=instLpt_funcCHelper.btlpt_mfg_testAndReturnBytes(commandCodeArray, cb_response);

                    // add to log
                    //Data_logdatapacket logdataPacket=new Data_logdatapacket(packetBuf, true);
                    //Application.add2LogMemory(logdataPacket);

//                    String strText=m_inputAmountet.getText().toString();
//                    byte[] dataBuf=strText.getBytes();
//                    byte[] commandCodeArray=m_commandlv.getCommandCode(m_posCurrentCommand);
//                    Callback<Object> cb_finish=null;
//                    if (m_context instanceof DemoMainActivity) {
//                        DemoMainActivity act=(DemoMainActivity)m_context;
//                        act.btprinter_safeStartCommand(commandCodeArray, dataBuf, cb_finish);
//                    }
                }
            });
        }
        m_btnLog=(LinearLayout)rootView.findViewById(R.id.btn_view_log);
        m_btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_context instanceof DemoMainActivity) {
                    DemoMainActivity act = (DemoMainActivity) m_context;
                    act.startViewLogHistory();
                }
            }
        });

        m_btnStartInfMFGPrint=(LinearLayout)rootView.findViewById(R.id.btn_startinfmfgprint);
        {
            m_btnStartInfMFGPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bReadyPrinter= BTPrinterBluetoothConnectActivity.isReadyPrinter();
                    if (!bReadyPrinter) {
                        Toast.makeText(m_context, "Please connect to bluetooth printer", Toast.LENGTH_SHORT).show();
                        BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
                        return;
                    }
                    Lpt_funcCHelper instLpt_funcCHelper= Lpt_funcCHelper.getInstance();
                    Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
                    instLpt_funcCHelper.setPacketEncapsulateLevel(dataBTPrinterConf.m_packetEncapsulateLevel);
                    BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
                    InputStream is=btSocketClass.getInputStream();
                    OutputStream os=btSocketClass.getOutputStream();
                    instLpt_funcCHelper.setIO(is, os);
                    instLpt_funcCHelper.setContext(m_context);

                    //instLpt_funcCHelper.setInfiniteLoopingMFGPrintTest();
                }
            });
        }
        m_btnStopInfMFGPrint=(LinearLayout)rootView.findViewById(R.id.btn_stopinfmfgprint);
        {
            m_btnStopInfMFGPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Lpt_funcCHelper instLpt_funcCHelper= Lpt_funcCHelper.getInstance();
                    //instLpt_funcCHelper.setInfiniteLoopingMFGPrintTest_cancel();
                }
            });
        }


        if (m_commandlv.getCount()>0) {
            m_posCurrentCommand = 0;
            m_commandlv.setItemChecked(m_posCurrentCommand, true);
            m_commandlv.smoothScrollToPosition(m_posCurrentCommand);
            setCurrentCommand();
        }

        return rootView;
    }

//    private OnClickListener initStartOnclickListener() {
//        OnClickListener listener=null;
//
//        listener=new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //byte[] dataBuf=getInputByteArray(m_inputAmountet);
//                String strText=m_inputAmountet.getText().toString();
//                byte[] dataBuf=strText.getBytes();
//                byte[] commandCodeArray=m_commandlv.getCommandCode(m_posCurrentCommand);
//                Callback<Object> cb_finish=null;
//                if (m_context instanceof DemoMainActivity) {
//                    DemoMainActivity act=(DemoMainActivity)m_context;
//                    act.btprinter_safeStartCommand(commandCodeArray, dataBuf, cb_finish);
//                }
//            }
//        };
//
//        return listener;
//    }

    private void selectCommand(int position) {
        m_posCurrentCommand=position;
        setCurrentCommand();
    }
    private void setCurrentCommand() {
        String strMsg = m_commandlv.getCommandStringAtPosition(m_posCurrentCommand);
        m_selecttv.setText(strMsg);
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
                ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                DemoMainActivity act=instActivityHelper.getDemoMainActivity();
                if (act!=null) {
                    act.hideSoftKeyboard();
                }
            }
        }
    }

    private void setMessageWithAlertColor(String strMsg) {
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act=(DemoMainActivity)m_context;
            act.setMessageWithAlertColor(strMsg);
        }
    }

}
