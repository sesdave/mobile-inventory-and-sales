package com.spectratech.sp530demo.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;


import com.spectratech.sp530demo.constant.AppSectionsConstant;
import com.spectratech.sp530demo.data.Data_setting_general;
import com.spectratech.sp530demo.view.BTPrinterCommandFragment;
import com.spectratech.sp530demo.view.DeviceFragment;
import com.spectratech.sp530demo.view.FileDownloadFragment;
import com.spectratech.sp530demo.view.FilesDownloadFragment;
import com.spectratech.sp530demo.view.S3AuthFragment;
import com.spectratech.sp530demo.view.S3CommandFragment;
import com.spectratech.sp530demo.view.S3DummySectionFragment;
import com.spectratech.sp530demo.view.S3TemplateFragment;
import com.spectratech.sp530demo.view.S3TransFragment;
import com.spectratech.sp530demo.view.TMSDownloadFragment;

/**
 * AppSectionsPagerAdapter - app section pager adapter
 */
public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String m_className="AppSectionsPagerAdapter";

    private final FragmentManager m_fm;

    private Context m_context;

    private boolean m_bLandscape;

    /**
     * Constructor for AppSectionsPagerAdapter
     * @param fm FragmentManager object
     * @param context context of application
     */
    public AppSectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        m_fm=fm;
        m_context=context;
        m_bLandscape=false;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment=null;

        if (i==AppSectionsConstant.IDX_SECTION_NAME.S3_TRANS.toInt()) {
            fragment = new S3TransFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.S3_AUTH.toInt()) {
            fragment = new S3AuthFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.S3_TEMPLATE.toInt()) {
            fragment = new S3TemplateFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.TMS_DOWNLOAD.toInt()) {
            fragment = new TMSDownloadFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.FILES_DOWNLOAD.toInt()) {
            fragment = new FilesDownloadFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.FILE_DOWNLOAD.toInt()) {
            fragment = new FileDownloadFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.DEVICE.toInt()) {
            fragment = new DeviceFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.COMMAND.toInt()) {
            fragment = new S3CommandFragment();
        }
        else if (i==AppSectionsConstant.IDX_SECTION_NAME.CMD_BLUETOOTHPRINTER.toInt()) {
            fragment = new BTPrinterCommandFragment();
        }
        else {
            // The other sections of the app are dummy placeholders.
            fragment = new S3DummySectionFragment();
        }

        Bundle args = new Bundle();
        args.putInt(AppSectionsConstant.ARG_SECTION_INDEX, i);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Set landscape flag
     * @param flag true of landscape; false otherwise
     */
    public void setLandscape(boolean flag) {
        m_bLandscape=flag;
    }

    @Override
    public int getCount() {
        boolean bDebugMode=Data_setting_general.isDebugMode(m_context);
        int size= 0;
        if ( (bDebugMode)&&(!m_bLandscape) ) {
            size = AppSectionsConstant.SECTION_NAMES.length;
//            if (!Data_setting_devicebluetoothprinter.isEnableBTPrinter(m_context)) {
//                size--;
//            }
        }
        else {
            if (AppSectionsConstant.SECTION_NAMES.length>0) {
                size=1;
            }
            else {
                size=0;
            }
        }
        return size;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return AppSectionsConstant.SECTION_NAMES[position];
    }


    /**
     * Get S3Trans fragment
     * @param container View pager container for fragments
     * @return Fragment object; null for no Fragment object
     */
    public Fragment getS3TransFragment(ViewPager container) {
        Fragment frag=null;
        int count=getCount();
        for (int i=0; i<count; i++) {
            Fragment tmp=getActiveFragment(container, i);
            if (tmp instanceof S3TransFragment) {
                frag=tmp;
                break;
            }
        }
        return frag;
    }

    public Fragment getS3TemplateFragment(ViewPager container) {
        Fragment frag=null;
        int count=getCount();
        for (int i=0; i<count; i++) {
            Fragment tmp=getActiveFragment(container, i);
            if (tmp instanceof S3TemplateFragment) {
                frag=tmp;
                break;
            }
        }
        return frag;
    }

    public Fragment getS3CommandFragment(ViewPager container) {
        Fragment frag=null;
        int count=getCount();
        for (int i=0; i<count; i++) {
            Fragment tmp=getActiveFragment(container, i);
            if (tmp instanceof S3CommandFragment) {
                frag=tmp;
                break;
            }
        }
        return frag;
    }


    /**
     * Get active fragment
     * @param container View pager container for fragments
     * @param position fragment index
     * @return Fragment object; null for no Fragment object
     */
    public Fragment getActiveFragment(ViewPager container, int position) {
        if (container==null) {

            return null;
        }
        String name = makeFragmentName(container.getId(), position);
        return m_fm.findFragmentByTag(name);
    }

    /**
     * Get corresponding fragment name
     * @param viewId parent view id
     * @param index fragment index
     * @return fragment name in string fomrat
     */
    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

}
