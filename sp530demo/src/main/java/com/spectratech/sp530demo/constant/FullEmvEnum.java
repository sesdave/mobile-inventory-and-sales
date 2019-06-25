package com.spectratech.sp530demo.constant;

/**
 * FullEmvEnum - enum for EMV transaction
 * It uses for indicating status of EMV transaction process
 */
public class FullEmvEnum {

    public enum TRANSACTIONSTATUS {
        STATUS_UNKNOWN(-1),

        STATUS_START(100),

        STATUS_GET_LOCALCHANNEL_INDEX(101),
        STATUS_GET_LOCALCHANNEL_INDEX_SUCCESS(102),
        STATUS_GET_LOCALCHANNEL_INDEX_FAIL(103),

        STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED(104),

        STATUS_START_CLEARSTREAM(200),

        STATUS_WAIT_COMM_READY(301),
        STATUS_WAIT_COMM_READY_FAIL(302),

        STATUS_WAIT_SSLSERVER_READY(306),
        STATUS_WAIT_SSLSERVER_READY_FAIL(307),

        STATUS_WAIT_SSL_HANDSHAKE_COMPLETE(308),
        STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL(309),

        STATUS_REQUEST_SEND(400),
        STATUS_REQUEST_SEND_FAIL(401),

        STATUS_RESPONSE_GET(500),
        STATUS_RESPONSE_GET_SUCCESS(501),
        STATUS_RESPONSE_GET_FAIL(502),

        STATUS_SHOW_TRANSRECEIPT(700),
        STATUS_WAIT_SHOW_TRANSRECEIPT(701),

        STATUS_FINISH(800),

        STATUS_REQUEST_CANCEL(900),

        STATUS_FAIL(1000);

        private final int intValue;
        private TRANSACTIONSTATUS(int value) {
            intValue = value;
        }
        public int toInt() {
            return intValue;
        }
    }

    public static String Status2String(TRANSACTIONSTATUS status) {
        String result="";
        switch (status) {
            case STATUS_UNKNOWN:
                result="STATUS_UNKNOWN";
                break;
            case STATUS_START:
                result="STATUS_START";
                break;
            case STATUS_START_CLEARSTREAM:
                result="STATUS_START_CLEARSTREAM";
                break;
            case STATUS_WAIT_COMM_READY:
                result="STATUS_WAIT_COMM_READY";
                break;
            case STATUS_WAIT_COMM_READY_FAIL:
                result="STATUS_WAIT_COMM_READY_FAIL";
                break;
            case STATUS_GET_LOCALCHANNEL_INDEX:
                result="STATUS_GET_LOCALCHANNEL_INDEX";
                break;
            case STATUS_GET_LOCALCHANNEL_INDEX_SUCCESS:
                result="STATUS_GET_LOCALCHANNEL_INDEX_SUCCESS";
                break;
            case STATUS_GET_LOCALCHANNEL_INDEX_FAIL:
                result="STATUS_GET_LOCALCHANNEL_INDEX_FAIL";
                break;
            case STATUS_WAIT_SSLSERVER_READY:
                result="STATUS_WAIT_SSLSERVER_READY";
                break;
            case STATUS_WAIT_SSLSERVER_READY_FAIL:
                result="STATUS_WAIT_SSLSERVER_READY_FAIL";
                break;
            case STATUS_WAIT_SSL_HANDSHAKE_COMPLETE:
                result="STATUS_WAIT_SSL_HANDSHAKE_COMPLETE";
                break;
            case STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL:
                result="STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL";
                break;
            case STATUS_REQUEST_SEND:
                result="STATUS_REQUEST_SEND";
                break;
            case STATUS_REQUEST_SEND_FAIL:
                result="STATUS_REQUEST_SEND_FAIL";
                break;
            case STATUS_RESPONSE_GET:
                result="STATUS_RESPONSE_GET";
                break;
            case STATUS_RESPONSE_GET_SUCCESS:
                result="STATUS_RESPONSE_GET_SUCCESS";
                break;
            case STATUS_RESPONSE_GET_FAIL:
                result="STATUS_RESPONSE_GET_FAIL";
                break;
            case STATUS_SHOW_TRANSRECEIPT:
                result="STATUS_SHOW_TRANSRECEIPT";
                break;
            case STATUS_WAIT_SHOW_TRANSRECEIPT:
                result="STATUS_WAIT_SHOW_TRANSRECEIPT";
                break;
            case STATUS_FINISH:
                result="STATUS_FINISH";
                break;
            case STATUS_REQUEST_CANCEL:
                result="STATUS_REQUEST_CANCEL";
                break;
            case STATUS_FAIL:
                result="STATUS_FAIL";
                break;
        }
        return result;
    }
}
