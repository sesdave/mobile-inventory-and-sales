package com.spectratech.sp530demo.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.data.Data_trans_summary;
import com.spectratech.lib.data.Data_tvl;
import com.spectratech.lib.data.Data_tvlTagInfo;
import com.spectratech.lib.sp530.ApplicationProtocolHelper;
import com.spectratech.lib.StringHelper;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.lib.sp530.constant.sp530_tlvTagInfoConstant;
import com.spectratech.lib.sp530.data.Data_AP_ONE;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.R;
import com.spectratech.lib.data.Data_dbTransSummaryDetail;
import com.spectratech.sp530demo.SP530DemoBaseActivity;
import com.spectratech.sp530demo.SP530DemoBaseFragmentActivity;
import com.spectratech.sp530demo.view.TransSummaryListView;
import java.util.HashMap;

/**
 * TransSummaryDetailPagerAdapter - transaction summary detail pager adapter
 */
public class TransSummaryDetailPagerAdapter extends PagerAdapter {

    private final String m_className="TransSummaryDetailPagerAdapter";

    private Context m_context;

    private LinearLayout m_transaction_receiptl;

    /**
     * Constructor for TransSummaryDetailPagerAdapter
     * @param context context of application
     */
    public TransSummaryDetailPagerAdapter(Context context) {
        m_context=context;
    }

    /**
     * Start transaction receipt activity
     * @param context context of application
     * @param fposition position of records
     */
    public static void startTransactionReceipt(Context context, final int fposition) {
        boolean bPause = false;
        if (context instanceof SP530DemoBaseActivity) {
            bPause = ((SP530DemoBaseActivity) context).isOnPause();
        } else if (context instanceof SP530DemoBaseFragmentActivity) {
            bPause = ((SP530DemoBaseFragmentActivity) context).isOnPause();
        }
        if (!bPause) {
            Application.dbTransSummaryDetailListForTransReceipt=Application.dbTransSummaryDetailList;
            HashMap<String, String> hash = new HashMap<String, String>();
            hash.put("pos", ""+fposition);
            int id= Application.ACTIVITY_TRANSRECEIPT;
            Application.startActivity(context, id, hash);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final String strRecord=ResourcesHelper.getBaseContextString(m_context, R.string.record);
        final String strCreatedAt=ResourcesHelper.getBaseContextString(m_context, R.string.created_at);

        LinearLayout layout = (LinearLayout) LinearLayout.inflate(m_context, R.layout.view_transummarydetail, null);
        LinearLayout contentll = (LinearLayout) layout.findViewById(R.id.transsummarydetail_contentcontainer);

        ByteHexHelper instByteHexHelper = ByteHexHelper.getInstance();
        StringHelper instStringHelper = StringHelper.getInstance();
        ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
        String strAll = "";
        String strTmp = "";

        final int fposition=position;
        m_transaction_receiptl=(LinearLayout)layout.findViewById(R.id.transaction_receipt);
        m_transaction_receiptl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTransactionReceipt(m_context, fposition);
            }
        });

        TextView tv_caption = (TextView) contentll.findViewById(R.id.tv_Caption);
        tv_caption.setText(strRecord+" " + (position + 1));

        Data_dbTransSummaryDetail dbTransSummaryDetail = Application.dbTransSummaryDetailList.get(position);
        long tInMs = dbTransSummaryDetail.m_importDate;
        String strDateOnly = TransSummaryListView.getDateOnlyString(tInMs);
        String strTimeOnly = TransSummaryListView.getTimeOnlyString(tInMs);
        TextView tv_date = (TextView) contentll.findViewById(R.id.tv_date);
        tv_date.setText(strCreatedAt+" " + strDateOnly);
        TextView tv_time = (TextView) contentll.findViewById(R.id.tv_time);
        tv_time.setText(strTimeOnly);

        Data_AP_ONE dataAP = new Data_AP_ONE(dbTransSummaryDetail.m_dataBuf, Application.SkeyForMutuAuth);

        // Packet information
        {
            TextView tv_dummy=(TextView) LinearLayout.inflate(m_context, R.layout.textview_green, null);
            tv_dummy.setText("Packet Information");
            contentll.addView(tv_dummy);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Packet size:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = ""+((dbTransSummaryDetail.m_dataBuf==null)?0:dbTransSummaryDetail.m_dataBuf.length);
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Format identifier:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = instByteHexHelper.bytesArrayToHexString(dataAP.m_formatIdentifier);
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Source address:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = instByteHexHelper.byteToHexString(dataAP.m_srcAddress);
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Destination address:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = instByteHexHelper.byteToHexString(dataAP.m_desAddress);
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Sequence number:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = instByteHexHelper.bytesArrayToHexString(dataAP.m_sequenceNumber);
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Command code:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp=instByteHexHelper.byteToHexString(dataAP.m_commandCode);
            String commandString= ApplicationProtocolConstant.S3RC_COMMAND_MAP.get(dataAP.m_commandCode);
            strTmp+=" ("+commandString+")";
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Data Format:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = instByteHexHelper.byteToHexString(dataAP.m_dataFormat);
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Data length:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = instByteHexHelper.bytesArrayToHexString(dataAP.m_dataLength);
            strTmp+= " (Length="+instApplicationProtocolHelper.getDataLengthFromRaw(dataAP.m_dataLength)+")";
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Checksum:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp = instByteHexHelper.bytesArrayToHexString(dataAP.m_checksum);
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            LinearLayout llspace=new LinearLayout(m_context);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int height=(int)m_context.getResources().getDimension(R.dimen.margin_bottom_general);
            lp.setMargins(0, 0, 0, height);
            llspace.setLayoutParams(lp);
            contentll.addView(llspace);

            LinearLayout separator=(LinearLayout)LinearLayout.inflate(m_context, R.layout.view_separator_endfadeout_margin, null);
            contentll.addView(separator);
        }

        Data_trans_summary dataTransSummary=new Data_trans_summary(dataAP);
        if (!dataTransSummary.isSuccessLoadData()) {
            Logger.e(m_className, "Transaction summary is not loaded successfully, pos="+position);
        }

        // Transaction status information
        {
            LinearLayout tmpll=new LinearLayout(m_context);
            int height=20;
            DisplayHelper instDisplayHelper=DisplayHelper.getInstance();
            height=instDisplayHelper.convertPixelsToDp(m_context, height);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
            tmpll.setLayoutParams(lp);
            contentll.addView(tmpll);
        }
        {
            TextView tv_dummy=(TextView) LinearLayout.inflate(m_context, R.layout.textview_green, null);
            tv_dummy.setText("Transaction Status Information");
            contentll.addView(tv_dummy);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Transaction complete:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            boolean flag=dataTransSummary.isTransactionComplete();
            if (flag) {
                strTmp="TRUE";
            }
            else {
                strTmp="FALSE";
            }
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);

            TextView tmpTV = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp="eid: 0x"+instByteHexHelper.bytesArrayToHexString(dataTransSummary.m_eid);
            tmpTV.setText(strTmp);
            contentll.addView(tmpTV);
            tmpTV = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp="sts[0]: 0x"+instByteHexHelper.byteToHexString(dataTransSummary.m_sts[0]);
            tmpTV.setText(strTmp);
            contentll.addView(tmpTV);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("sts[1]:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            strTmp=instByteHexHelper.byteToHexString(dataTransSummary.m_sts[1]);
            tv_info2.setText("0x"+strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Signature required:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            boolean flag=dataTransSummary.isSignatureRequired();
            if (flag) {
                strTmp="TRUE";
            }
            else {
                strTmp="FALSE";
            }
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Default CVM:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            boolean flag=dataTransSummary.isDefaultCVM();
            if (flag) {
                strTmp="TRUE";
            }
            else {
                strTmp="FALSE";
            }
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Online authorization:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            boolean flag=dataTransSummary.isOnlineAuthorization();
            if (flag) {
                strTmp="TRUE";
            }
            else {
                strTmp="FALSE";
            }
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Referral required:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            boolean flag=dataTransSummary.isReferralRequired();
            if (flag) {
                strTmp="TRUE";
            }
            else {
                strTmp="FALSE";
            }
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Host approved:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            boolean flag=dataTransSummary.isHostApp();
            if (flag) {
                strTmp="TRUE";
            }
            else {
                strTmp="FALSE";
            }
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }
        {
            TextView tv_info1 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey, null);
            tv_info1.setText("Transaction approved:");
            TextView tv_info2 = (TextView) LinearLayout.inflate(m_context, R.layout.textview_grey_right, null);
            boolean flag=dataTransSummary.isTransactionApproved();
            if (flag) {
                strTmp="TRUE";
            }
            else {
                strTmp="FALSE";
            }
            tv_info2.setText(strTmp);
            contentll.addView(tv_info1);
            contentll.addView(tv_info2);
        }

        {
            LinearLayout llspace=new LinearLayout(m_context);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int height=(int)m_context.getResources().getDimension(R.dimen.margin_bottom_general);
            lp.setMargins(0, 0, 0, height);
            llspace.setLayoutParams(lp);
            contentll.addView(llspace);

            LinearLayout separator=(LinearLayout)LinearLayout.inflate(m_context, R.layout.view_separator_endfadeout_margin, null);
            contentll.addView(separator);
        }


        // Transaction information

        for (int i=0; i<dataTransSummary.m_tvlList.size(); i++) {
            Data_tvl x=dataTransSummary.m_tvlList.get(i);
            if (x==null) {
                continue;
            }
            LinearLayout element=(LinearLayout)LinearLayout.inflate(m_context, R.layout.view_transummarydetail_text, null);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int height=(int)m_context.getResources().getDimension(R.dimen.margin_top_general);
            lp.setMargins(0, height, 0, height);
            element.setLayoutParams(lp);

            TextView tv_green=(TextView)element.findViewById(R.id.tv_green);
            TextView tv_grey=(TextView)element.findViewById(R.id.tv_grey);
            Data_tvlTagInfo tvlTagInfo= sp530_tlvTagInfoConstant.TLVTagInfoMap.get(""+x.m_wTag);
            String strFieldName="";
            if (tvlTagInfo!=null) {
                strFieldName = tvlTagInfo.m_description;
            }
            else {
                strFieldName = ""+x.m_wTag;
            }
            strFieldName=strFieldName.trim();
            strFieldName+=" (Length: "+x.m_sizeTagLength+")";
            tv_green.setText(strFieldName);

            strAll="";
            int size=x.m_sizeTagLength;
            byte[] byteTag=new byte[size];
            for (int j=0; j<size; j++) {
                byteTag[j]|=(byte)((x.m_wTag>>(8*(size-1-j)))&0xFF);
            }
            strTmp=instByteHexHelper.bytesArrayToHexString(byteTag);
            strAll+=""+strTmp;

            size=x.m_sizeDataLength;
            byte[] byteLength=new byte[size];
            for (int j=0; j<size; j++) {
                byteLength[j]|=(byte)((x.m_lengthData>>(8*(size-1-j)))&0xFF);
            }
            strTmp=instByteHexHelper.bytesArrayToHexString(byteLength);
            strAll+="\t"+strTmp+" (Length="+x.m_lengthData+")";

            strTmp=instByteHexHelper.bytesArrayToHexString(x.m_dataBuf);
            strTmp=instStringHelper.addPaddingStartingFromHead("\t", strTmp, 4);
            strAll+="\n"+strTmp;
            tv_grey.setText(strAll);

            contentll.addView(element);

            LinearLayout separator=(LinearLayout)LinearLayout.inflate(m_context, R.layout.view_separator_endfadeout_margin, null);
            contentll.addView(separator);
        }


        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getCount() {
        if (Application.dbTransSummaryDetailList==null) {
            return 0;
        }
        return Application.dbTransSummaryDetailList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
