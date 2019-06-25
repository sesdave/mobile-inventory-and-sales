package com.spectratech.sp530demo.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.spectratech.lib.Logger;
import com.spectratech.lib.conf.Config;
import com.spectratech.lib.conf.Const;
import com.spectratech.sp530demo.DemoMainActivity;
import java.util.ArrayList;

/**
 * ActivityHelper uses for managing activities of app
 * To use this class, call getInstance to obtain ActivityHelper instance.
 */
public class ActivityHelper {
	private static final String m_className="ActivityHelper";

	private static ActivityHelper m_inst;

    /**
     * Returns ActivityHelper instance
     * @return static ActivityHelper instance
     */
    public static ActivityHelper getInstance() {
		if (m_inst==null) {
			m_inst=new ActivityHelper();
		}
		return m_inst;
	}
	
	private ArrayList<Activity> m_activityList;
	
	private DemoMainActivity m_demoMainActivity;
	
    private Activity m_currentActivity;

	private ActivityHelper() {
		m_activityList=new ArrayList<Activity>();
        m_currentActivity=null;
	}

    /**
     * Function to check activity in the front most
     * @param act activity to be checked
     * @return true if in the front most; false otherwise
     */
    public boolean isActivityBring2Front(Activity act) {
        boolean bRet=false;
        if (act!=null) {
            if ((act.getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
                Config conf= Config.getInstance();
                if (conf.env== Const.Environment.DEV) {
                    Context context=(Context)act;
                    Toast.makeText(context, "FLAG_ACTIVITY_BROUGHT_TO_FRONT", Toast.LENGTH_SHORT).show();
                }
                bRet=true;
            }
        }
        return bRet;
    }

    /**
     * Function to set current activity
     * @param activity activity to be set as current
     */
    public void setCurrentActivity(Activity activity) {
        m_currentActivity=activity;
    }

    /**
     * Get current activity
     * @return Activity object; null if doesn't have
     */
    private Activity getCurrentActivity() {
        return m_currentActivity;
    }

    /**
     * Get current activity safely. If there is no current activity, DemoMainActivity is return as current activity
     * @return current activity
     */
    public Activity safeGetCurrentActivity() {
        Activity act=getCurrentActivity();
        if (act==null) {
            act=getDemoMainActivity();
        }
        return act;
    }

    /**
     * See whether the activity is on pause or not
     * @param activity activity to be determined
     * @return true if on puase; false otherwise
     */
//	public int getOnPauseStatus(Activity activity) {
//		int iRet=-1;
//		if (activity instanceof SP530DemoBaseActivity) {
//			iRet=(((SP530DemoBaseActivity)activity).isOnPause())?1:0;
//		}
//		else if (activity instanceof SP530DemoBaseFragmentActivity) {
//			iRet=(((SP530DemoBaseFragmentActivity)activity).isOnPause())?1:0;
//		}
//		return iRet;
//	}


    /**
     * Get DemoMainActivity
     * @return DemoMainActivity
     */
	public DemoMainActivity getDemoMainActivity() {
		return m_demoMainActivity;
	}

    /**
     * Set DemoMainActivity
     * @param activity activity to be set as DemoMainActivity
     */
	public void setDemoMainActivity(DemoMainActivity activity) {
		m_demoMainActivity=activity;
	}

    /**
     * insert activity to the list. Do not add to list if the activity is DemoMainActivity
     * @param activity activity to be inserted
     */
	public void insertWithoutDemoMainActivity(Activity activity) {
		if ( (activity!=null)&&(!(activity instanceof DemoMainActivity)) ) {
			insert(activity);
		}
	}

    /**
     * Get size of activity list
     * @return size of activity list
     */
    public int getSize() {
        if (m_activityList==null) {
            return 0;
        }
        return m_activityList.size();
    }

    /**
     * Get last activity in the list
     * @return last activity
     */
	public Activity getLastActivity() {
		Activity activity=null;
		if (m_activityList.size()>0) {
			activity=m_activityList.get(m_activityList.size()-1);
		}
		return activity;
	}

    /**
     * Insert activity to the list if doesn't exist
     * @param activity activity to be inserted
     */
	private void insert(Activity activity) {
		if (activity!=null) {
			if (m_activityList.indexOf(activity)<0) {
				m_activityList.add(activity);
			}
			else {
				Logger.v(m_className, "insert() - duplicate insert");
			}
		}
	}

    /**
     * Remove activity from the list
     * @param activity activity to be removed
     */
	public void remove(Activity activity) {
		if (activity!=null) {
			if (m_activityList.indexOf(activity)>-1) {
				m_activityList.remove(activity);
			}
			else {
				Logger.v(m_className, "remove() - no this activity: "+activity.toString());
			}
		}
	}

    /**
     * Finish all activities in the list
     */
	public void finishAllActivities() {
		for (int i=0; i<m_activityList.size(); i++) {
			Activity activity=m_activityList.get(i);
            if (activity!=null) {
				activity.finish();
			}
		}
        Activity act=getDemoMainActivity();
        if (act!=null) {
            act.finish();
            setDemoMainActivity(null);
        }
	}


    /**
     * Finish all activities in the list but only exclude the input activity
     * @param act activity to be excluded to finish
     */
	public void finishAllActivitiesExcludeActivity(Activity act) {
		for (int i=m_activityList.size()-1; i>=0; i--) {
			Activity activity=m_activityList.get(i);
			if (activity!=null) {
				if (activity.equals(act)) {
					continue;
				}
				activity.finish();
			}
		}
	}
}
