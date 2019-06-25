package com.spectratech.sp530demo.controller;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.spectratech.lib.Logger;
import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.DemoMainActivity;
import java.util.HashMap;
import java.util.Set;
import static com.spectratech.sp530demo.conf.ConfigURLScheme.SCHEME_HOST_SP530DEMO;
import static com.spectratech.sp530demo.conf.ConfigURLScheme.SCHEME_NAME_EMAIL;
import static com.spectratech.sp530demo.conf.ConfigURLScheme.SCHEME_NAME_HTTP;
import static com.spectratech.sp530demo.conf.ConfigURLScheme.SCHEME_NAME_HTTPS;
import static com.spectratech.sp530demo.conf.ConfigURLScheme.SCHEME_NAME_SPECTRATECH;
import static com.spectratech.sp530demo.conf.ConfigURLScheme.SCHEME_NAME_TEL;

/**
 * Url Scheme helper
 */
public class UrlSchemeHelper {
	private static final String m_className="UrlSchemeHelper";
	
	private static UrlSchemeHelper m_inst;
	
	public static UrlSchemeHelper getInstance() {
		if (m_inst==null) {
			m_inst = new UrlSchemeHelper();
		}
		return m_inst;
	}

    public void process(Context context, String urlSchemeString) {
        if ( (urlSchemeString==null)||(urlSchemeString.equals("")) ) {
            Logger.v(m_className, "process: urlSchemeString is EMPTY");
            return;
        }

        if (urlSchemeString.startsWith("www.")) {
            urlSchemeString="http://"+urlSchemeString;
        }

        Uri urlSchemeUri = Uri.parse(urlSchemeString);
        process(context, urlSchemeUri);
    }

    public void process(Context context, Uri urlSchemeUri) {
        String urlSchemeUriString=urlSchemeUri.toString();
        Logger.i(m_className, "process called, urlScheme string: "+urlSchemeUriString);
        if (urlSchemeUri==null) {
            Logger.v(m_className, "process: urlSchemeUri is EMPTY");
            return;
        }

        String scheme=urlSchemeUri.getScheme();
        if (scheme!=null) {
            scheme = scheme.toLowerCase();
        }
        else {
            scheme="";
        }
        String host=urlSchemeUri.getHost();
        if (host!=null) {
            host = host.toLowerCase();
        }
        else {
            host="";
        }

        Logger.v(m_className, "process: " + host);

        if ( scheme.equals(SCHEME_NAME_HTTP)||scheme.equals(SCHEME_NAME_HTTPS) ){
            Intent intent = new Intent(Intent.ACTION_VIEW, urlSchemeUri);
            context.startActivity(intent);
        }
        else if (scheme.equals(SCHEME_NAME_EMAIL)) {
            Intent intent = new Intent(Intent.ACTION_SEND, urlSchemeUri);
            intent.setType("text/plain");
            context.startActivity(intent);
        }
        else if (scheme.equals(SCHEME_NAME_TEL)) {
            Uri telSchemeUri = Uri.parse("tel:"+host);
            Intent intent = new Intent(Intent.ACTION_DIAL, telSchemeUri);
            context.startActivity(intent);
        }
        else if (scheme.equals(SCHEME_NAME_SPECTRATECH)){
            if (host.equals(SCHEME_HOST_SP530DEMO)) {
                HashMap<String, String> hash=getHasMapFromUri(urlSchemeUri);
                launchDemoMain(context, hash);
            }
            else {
                Logger.i(m_className, "SCHEME_NAME_SPECTRATECH, No appropriated scheme host is found");

            }
        }
        else {
            Logger.i(m_className, "SCHEME_NAME, No appropriated scheme name is found");
            launchDemoMain(context);
        }
    }

    private HashMap<String, String> getHasMapFromUri(Uri uri) {
        HashMap<String, String> hash=new HashMap<String, String>();
        if (uri==null) {
            return hash;
        }
        Set<String> qparamSet = uri.getQueryParameterNames();
        for (String key: qparamSet) {
            String val=uri.getQueryParameter(key);
            if ( (val!=null)&&(!val.equals("")) ) {
                hash.put(key, val);
            }
        }
        return hash;
    }

    private void launchDemoMain(Context context) {
        launchDemoMain(context, null);
    }
    private void launchDemoMain(Context context, HashMap<String, String> hash) {
        Logger.i(m_className, "launchDemoMain");

        Application.hashUrlSchemeForDemoMain=hash;

        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity dmAct=instActivityHelper.getDemoMainActivity();
        if (dmAct!=null) {
            boolean bShowReceipt=dmAct.isShowingTransReceipt();
            if (!bShowReceipt) {
                instActivityHelper.finishAllActivitiesExcludeActivity(dmAct);
                bringApplicationToFront(context);
            }
            else {
                Application.hashUrlSchemeForDemoMain=null;
                Toast.makeText(context, "Please finish current transaction first", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Intent i = new Intent(context, DemoMainActivity.class);
            context.startActivity(i);
        }
    }


    private void bringApplicationToFront(Context context) {
        Logger.i(m_className, "====Bringging Application to Front====");
        Intent notificationIntent = new Intent(context, DemoMainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        try {
            pendingIntent.send();
        }
        catch (PendingIntent.CanceledException e) {
            Logger.w(m_className, "e: " + e.toString());
        }
    }
}
