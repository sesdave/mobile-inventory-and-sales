package com.spectratech.sp530demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * FixKeyboardEditText - parse keyboard input class
 */
public class FixKeyboardEditText extends EditText {

    private static final String m_className="FixKeyboardEditText";

    private Context m_context;

    public FixKeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context=context;
    }
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//        Logger.i(m_className, "onKeyPreIme, keyCode: "+keyCode);

//        int action=event.getAction();
//        if (action==KeyEvent.ACTION_UP) {
//            if (keyCode==KeyEvent.KEYCODE_BACK) {
//                if (m_context instanceof DemoMainActivity) {
//                    DemoMainActivity act = (DemoMainActivity) m_context;
//                    act.onBackPressed();
//                }
//                return true;
//            }
//        }

        //return true;
        return super.onKeyPreIme(keyCode, event);
    }

    public String getTextRaw() {
        String tmp = getText().toString();
        tmp=tmp.replace(".","");
        return tmp;
    }

    @Override
    public void onSelectionChanged(int start, int end) {
        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }
        super.onSelectionChanged(start, end);
    }
}
