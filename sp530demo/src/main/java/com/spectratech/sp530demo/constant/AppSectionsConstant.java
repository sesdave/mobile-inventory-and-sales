package com.spectratech.sp530demo.constant;

/**
 * ActivityRequestCodeEnum - constant for creating app tab page
 */
public class AppSectionsConstant {

    /**
     * section name string array
     */
    public static final String[] SECTION_NAMES={
            "S3-Trans",
            "S3-Auth",
            "S3-Template",
            "TMS-Download",
            "File(s)-Download",
            "File-Download",
            "Device",
            "Command"
//            ,
//            "CMD BT Printer"
    };

    /**
     * index of section name in enum format
     */
    public enum IDX_SECTION_NAME {
        S3_TRANS(0),
        S3_AUTH(1),
        S3_TEMPLATE(2),
        TMS_DOWNLOAD(3),
        FILES_DOWNLOAD(4),
        FILE_DOWNLOAD(5),
        DEVICE(6),
        COMMAND(7),
        CMD_BLUETOOTHPRINTER(8);
        private final int intValue;
        private IDX_SECTION_NAME(int value) {
            intValue = value;
        }
        public int toInt() {
            return intValue;
        }
    }

    /**
     * key for app section index
     */
    public static final String ARG_SECTION_INDEX="SECTION_INDEX";

    /**
     * Share preference name
     */
    public static class Storage {
        // Preference
        public static final String PREFERENCE_CONF_SETTING   = "CONF_SETTING";

        public static final String PREFERENCE_DEVICE_CONF_SETTING   = "DEVICE_CONF_SETTING";

        public static final String PREFERENCE_SP530SSLCERT_CONF_SETTING = "SP530SSLCERT_CONF_SETTING";

        public static final String PREFERENCE_BLUETOOTHPRINTER_CONF_SETTING   = "BLUETOOTHPRINTER_CONF_SETTING";

        public static final String PREFERENCE_ACTIVEBLUETOOTHDEVICE_ADDRESS = "ACTIVE_BLUETOOTH_DEVICE_ADDRESS";
    }
}
