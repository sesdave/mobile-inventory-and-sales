package com.spectratech.sp530demo.controller;

import android.content.Context;
import static com.spectratech.lib.sp530.constant.DBConstant.DBTRANSSUMMARYDETAIL_CLEAR_AGEINSEC;
import com.spectratech.lib.sp530.db.DBTransSummaryDetail;

/**
 * HouseKeepHelper uses for house keep db records
 * To use this class, call getInstance to obtain HouseKeepHelper instance.
 */
public class HouseKeepHelper {
	private static final String m_className="HouseKeepHelper";
	private static HouseKeepHelper m_inst=null;

    /**
     * Returns ActivityHelper instance
     * @return static HouseKeepHelper instance
     */
    public static HouseKeepHelper getInstance() {
		if (m_inst==null) {
			m_inst=new HouseKeepHelper();
		}
		return m_inst;
	}
	
	private HouseKeepHelper() {
		
	}

    /**
     * Perform house keeping on startup
     * @param context context of application
     */
	public void housekeepingOnStartup(Context context) {
		
		// DB read news house keeping
		long ageInSec=DBTRANSSUMMARYDETAIL_CLEAR_AGEINSEC;
        clearTransHistoryOlderThan(context, ageInSec);
	}

    /**
     * Clear transaction records older than a time
     * @param context context of application
     * @param ageInSec records to be deleted before this time
     */
    public void clearTransHistoryOlderThan(Context context, long ageInSec) {
        DBTransSummaryDetail.clearOlderThan(context, ageInSec);
    }

    /**
     * Clear all transaction records in db
     * @param context context of application
     */
	public void clearTransHistory(Context context) {
		// DB MWS login log clear
		DBTransSummaryDetail.clear(context);
	}
}
