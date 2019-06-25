package com.spectratech.sp530demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.printer.bp80.constant.BTPrinterProtocolConstant;
import com.spectratech.sp530demo.R;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * BTPrinterCommandListView - list view for Bluetooth printer command
 */
public class BTPrinterCommandListView extends ListView implements AbsListView.OnScrollListener  {

    private static final String m_className="BTPrinterCommandListView";

    private OnScrollListener m_listener = null;

    protected Context m_context;

    protected PrinterCommandArrayAdapter m_adapter;

    protected LinkedHashMap<byte[], String> m_commandListHash;
    protected List<byte[]> m_commandCodeList;
    protected List<String> m_commandDescriptionList;
    public BTPrinterCommandListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        initListViewAdapter();
    }

    public BTPrinterCommandListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        initListViewAdapter();
    }

    protected void init(Context context, AttributeSet attrs) {
        m_context = context;
        m_adapter = null;
        super.setOnScrollListener(this);

        m_commandListHash=new LinkedHashMap<byte[], String>(BTPrinterProtocolConstant.BTPRINTER_COMMAND_MAP);

        m_commandCodeList=new ArrayList<byte[]>(m_commandListHash.keySet());
        m_commandDescriptionList=new ArrayList<String>(m_commandListHash.values());
    }

    public void initListViewAdapter() {
        m_adapter = new PrinterCommandArrayAdapter(m_context, -1);
        setAdapter(m_adapter);

        setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setFastScrollAlwaysVisible(true);
    }

    public void notifyDataSetChanged() {
        if (m_adapter!=null) {
            m_adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (this.m_listener != null) {
            this.m_listener.onScrollStateChanged(view, scrollState);
        }
    }

    public byte[] getCommandCode(int position) {
        return m_commandCodeList.get(position);
    }

    public String getCommandStringAtPosition(int position) {
        String strRet="";
        byte[] keyArray=m_commandCodeList.get(position);
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String strKey=instByteHexHelper.bytesArrayToHexString(keyArray);
        String val=m_commandDescriptionList.get(position);
        strRet=val+" - ["+strKey+"]";
        return strRet;
    }

    private class PrinterCommandArrayAdapter extends ArrayAdapter<String> {
        private static final String m_className = "PrinterCommandArrayAdapter";

        public PrinterCommandArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return m_commandCodeList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if ((v == null) || (v.getId() != android.R.id.text1)) {
                v = (CheckedTextView) CheckedTextView.inflate(m_context, R.layout.view_simple_list_item_checked, null);
                int height=50;
                DisplayHelper instDisplayHelper=DisplayHelper.getInstance();
                height=instDisplayHelper.convertPixelsToDp(m_context, height);
                AbsListView.LayoutParams lp=new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height);
                v.setLayoutParams(lp);
            }

            String strMsg = getCommandStringAtPosition(position);
            ((CheckedTextView) v).setText(strMsg);

            return v;
        }

    }
}
