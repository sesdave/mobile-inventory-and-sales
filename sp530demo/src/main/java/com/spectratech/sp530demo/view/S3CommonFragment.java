package com.spectratech.sp530demo.view;

import android.app.Fragment;
import android.content.Context;
import android.widget.EditText;
import com.spectratech.lib.Logger;
import com.spectratech.sp530demo.R;

/**
 * S3CommonFragment - command fragment functions for other fragment to inherited from
 */
public class S3CommonFragment extends Fragment {

    private static final String m_className="S3CommonFragment";

    protected Context m_context;

    public void setMessageLocal(String strMsg) {
    }

    public void setMessageDeviceLocal(String strMsg) {
    }

    public void setMessagePrinterLocal(String strMsg) {
    }

    protected byte[] getInputByteArray(EditText et) {
        byte[] buf=null;

        String s=et.getText().toString();
        if (s!=null) {
            s=s.replace(".", "");
        }
        if ( (s==null)||(s.equals("")) ) {
            Logger.w(m_className, "getInputByteArray input amount is EMPTY");
            return buf;
        }

        int len=s.length();
        int MAX_LENGTH=m_context.getResources().getInteger(R.integer.max_input_amount_digits);
        if (len>MAX_LENGTH) {
            Logger.e(m_className, "getInputByteArray the number of digits of input amount is larger than MAX_LENGTH ("+MAX_LENGTH+")");
            return buf;
        }

        int lengthBuf=(int)Math.ceil((float)MAX_LENGTH/2);
        buf=new byte[lengthBuf];
        for (int i=len-1, j=0; i>=0; i-=2,j++) {
            char c0='0';
            if (i-1>-1) {
                c0=s.charAt(i - 1);
            }
            char c1=s.charAt(i);
            int val0=Character.getNumericValue(c0);
            int val1=Character.getNumericValue(c1);
            byte b=(byte)( ((val0&0x0F)<<4) | (val1&0x0F) );
            buf[lengthBuf-1-j]=b;
        }

        return buf;
    }
}
