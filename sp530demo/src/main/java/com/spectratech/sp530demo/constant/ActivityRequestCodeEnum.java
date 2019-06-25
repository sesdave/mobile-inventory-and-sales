package com.spectratech.sp530demo.constant;

/**
 * ActivityRequestCodeEnum - enum for activity request
 * It has REQUEST_STARTTRANSACTION_AMOUNT, REQUEST_SHOWTRANSACTION_RECEIPT, REQUEST_TRANSACTION_SIGNATURE and REQUEST_BTPRINTER_CONNECT
 */
public class ActivityRequestCodeEnum {

    public enum RequestCode {
        REQUEST_STARTTRANSACTION_AMOUNT(1002),
        REQUEST_SHOWTRANSACTION_RECEIPT(1003),
        REQUEST_TRANSACTION_SIGNATURE(1004),

        REQUEST_BTPRINTER_CONNECT(1005),

        REQUEST_DEVICE_SELECT_AND_STARTTRANSACTION(1006),

        REQUEST_FILES_DOWNLOAD(1007);

        private final int intValue;

        private RequestCode(int value) {
            intValue = value;
        }

        public int toInt() {
            return intValue;
        }
    }

    // Can only use lower 8 bits for requestCode
    public enum RequestPermissionCode {
        REQUEST_PERMISSION_ACCESS_COARSE_LOCATION(1),
        REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE(2),
        REQUEST_PERMISSION_CAMERA(3);

        private final int intValue;

        private RequestPermissionCode(int value) {
            intValue = value;
        }

        public int toInt() {
            return intValue;
        }
    }
}
