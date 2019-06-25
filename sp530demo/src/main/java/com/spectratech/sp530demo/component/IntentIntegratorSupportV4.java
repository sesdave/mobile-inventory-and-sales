package com.spectratech.sp530demo.component;

/**
 * Created by ASUS on 12/26/2017.
 */


        import android.content.Intent;
        import android.support.v4.app.Fragment;

        import com.google.zxing.integration.android.IntentIntegrator;

/**
 * IntentIntegrator for the V4 Android compatibility package.
 *
 * @author Lachezar Dobrev
 */
public final class IntentIntegratorSupportV4 extends IntentIntegrator {

    private final Fragment fragment;

    /**
     * @param fragment Fragment to handle activity response.
     */
    public IntentIntegratorSupportV4(Fragment fragment) {
        super(fragment.getActivity());
        this.fragment = fragment;
    }

    @Override
    protected void startActivityForResult(Intent intent, int code) {
        fragment.startActivityForResult(intent, code);
    }

}
