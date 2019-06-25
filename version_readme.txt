20160926
versionName 1.1.4
versionName: 47
user ANDROID_LIB_SPECTRATECH version 1.3.3
[sp530demo]
- add UI for SSL ca cert input
- SP530_bt_S3INS.java
   - neglect response data with different sequence number


20160816
versionName 1.1.3
versionCode: 46
use ANDROID_LIB_SPECTRATECH version 1.3.2
[sp530demo]
- add "Secure Bluetooth Connection" option in Setting->Setting-Bluetooth Printer
[bp80demo]
- add "Secure Bluetooth Connection" option in Setting->Setting-Bluetooth Printer

20160722
versionName 1.1.2
versionCode: 45
use ANDROID_LIB_SPECTRATECH version 1.3.2
- restore HK font based views in libpack

20160712
versionName 1.1.1
versionCode: 44
use ANDROID_LIB_SPECTRATECH version 1.3.1
- remove HK font based views

20160621
versionName: 1.1.0
versionCode: 43
use ANDROID_LIB_SPECTRATECH version 1.3.0
[sp530demo]
- use newly developed MCP module (support 8 mcp channels)
- remove status STATUS_WAIT_SSLSERVER_READY, STATUS_WAIT_SSLSERVER_READY_FAIL, STATUS_START_COMM, STATUS_WAIT_TCP_READY, STATUS_WAIT_TCP_READY_FAIL, STATUS_SEND_TCP_DISCONNECTED for - support multi-languages en and zh
- add File(s)-Download fragment
SP530_bt_S3INS
- add status STATUS_GET_LOCALCHANNEL_INDEX, STATUS_GET_LOCALCHANNEL_INDEX_SUCCESS, STATUS_GET_LOCALCHANNEL_INDEX_FAIL, STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED for SP530_bt_S3INS
- UI
   - change name "SSL - logical channel one" in "Setting->Setting - SP530 Device" to name "Enable SSL".
   - delete "Simulate TCP host (for NO SSL)" in "Setting->Setting - Transaction"
   - change name "Transaction result (for simulate TCP host only)" in "Setting->Setting - Transaction" to name "Transaction result (for RAW packet encapsulate level ONLY)"
- set transreceipt view to print transreceipt
- add "Send echo when connected" field in "Setting->Setting General"
- add "Device" fragment; add "S3INS_UPDMCPCHCFG(0x35)" cmd; add "S3INS_REBOOTREQ(0x36)" cmd
   
20160331
versionName: 1.0.10
versionCode: 42
use ANDROID_LIB_SPECTRATECH version 1.2.6
[sp530demo]
- add S3INS_ECHO (0x00) when connected with Bluetooth
- change name "Debug Mode" in "Setting->Setting - General" to name "Advance Mode"
- change to no first two digits for S3INS_APPINFO cmd in TMS list 
- File-Download: S3INS_DATADL (0x34) 
   - File name cannot contains 0x00. Use 0x20 (space char) instead.
   - delete separator byte 0x00.
   - modify download result ok (S3RC_OK) only have response code but not response data.
- add function calls setFetchRxSeq and setSOH_fetchRxSeq in validateBTConnections of DemoMainActivity.java
[bp80demo]
- add function call setSOH_fetchRxSeq in validateBTConnections of BP80demo_mainActivity.java
[bp80mfgtest]
- fix BT Channel Loopback does not work after changing testing flow

20160211
versionName: 1.0.9
versionCode: 41
use ANDROID_LIB_SPECTRATECH version 1.2.6
[sp530demo]
- add "Setting - SP530 SSL Certificate" item in Setting menu
- add Data_setting_sp530sslcert.java
- add SettingSP530SSLCertActivity.java
- change showCancelGeneralDialog to showResetGeneralDialog in TMSDLProgressOverlayFragment.java
- support data file download
- add Data_executeCommand_filedl.java
- add SP530_bt_S3INS_FILEDL.java for data file download
- add FileDownloadFragment.java
- add view_filedownload.xml
- add function call setFetchRxSeq in manageConnectedSocket of DemoMainActivity.java
- add function call setSOH_fetchRxSeq in manageConnectedSocket of BTPrinterBluetoothConnectActivity.java
[bp80demo]
- add flag m_bPrintError in BP80demo_viewTransreceiptLinearLayout
- add sfl size in MB info in setUI_printerinfo of BP80demo_printerinfoActivity.java
- fix little endian of checksum in BP80demo_printerinfoActivity
- add function call setSOH_fetchRxSeq in manageConnectedSocket of BP80demo_btPrinterBluetoothConnectActivity.java
[bp80mfgtest]
- develop mfgtest testing flow
- add Data_mfgtest_params.java
- add BP80mfgtest_setmfgparamsActivity
- add bp80mfgtest_activity_setmfgparams.xml
- add ACTIVITY_MFGTEST_SETMFGPARAMS for BP80mfgtest_setmfgparamsActivity
- add error message when fail
   - error texview in bp80mfgtest_activity.xml
- add MfgtestResultCollector.java for generating .mfg file extension format
- add Data_bp80_printerinfo.java for storing printer info obtained from BP80
- add sfl size in MB info in setUI_printerinfo of BP80mfgtest_printerinfoActivity.java
- change BT loop back packet size from 1532 to 512
- fix little endian of checksum in BP80mfgtest_printerinfoActivity
- disable print pyramid button in main screen
- add function call setSOH_fetchRxSeq in manageConnectedSocket of BP80mfgtest_btPrinterBluetoothConnectActivity.java

20160121
versionName: 1.0.8
versionCode: 40
use ANDROID_LIB_SPECTRATECH version 1.2.5
[sp530demo]
- add time stamp in share
- fix issue of app checksum equals to app display checksum in Data_SP530_tms_module.java
- fix number of skip files equals to total number of files shows error
- fix crash when pop up blueooth connection in tms download
[bp80demo]
- add QR code printing
- add time stamp in share
- revise Gen QR code activity
- QR code activity: change the maximum height from 48 to 255
[bp80mfgtest]
- fix share summary no K_CMD_MFG_KBD_TEST and K_CMD_MFG_SFL_TEST results
- remove checking of isSocketConnected in isReadyPrinter funciton of BP80mfgtest_demoMainActivity.java: some devices do not work well in this function.
- add time stamp in share

20160119
versionName: 1.0.7
versionCode: 39
use ANDROID_LIB_SPECTRATECH version 1.2.5
[sp530demo]
- first version support downloading tms from ftp server to SP530
- add Data_executeCommand_tmsdl.java
- add Data_tms_header.java
- add TmsHelper.java
- add TMSDlProgressOverlayFragment.java
- add Data_tms_header_module.java
- add Data_SP530_tms_module.java
- add Data_tmsTrans2DeviceObject.java

20160112
versionName: 1.0.6
versionCode: 38
use ANDROID_LIB_SPECTRATECH version 1.2.3
- SOH use 0x01 value for starting, instead of 0x02 value
- add spectratechlib_ftpprivate lib module
- add spectratechlib_ftp lib module
[sp530demo]
- add spectratechlib_ftp lib module
- add TMSDownloadFragment.java
[bp80demo]
- fix start print looping does not loop.
- add Callback<Object> cb_error in BP80demo_viewTransreceiptLinearLayout.java

20160104
versionName: 1.0.5
versionCode: 37
use ANDROID_LIB_SPECTRATECH version 1.2.3

20151217
versionName: 1.0.4
versionCode: 36
use ANDROID_LIB_SPECTRATECH version 1.2.2
- add Data_viewCommand, Data_viewCommandList
- add S3TransOverlayFragment
- add function getS3INSReponseObject for output response data

20151216
versionName: 1.0.3
versionCode: 35
use ANDROID_LIB_SPECTRATECH version 1.2.1
compileSdkVersion 23
buildToolsVersion "23.0.1"
minSdkVersion 15
targetSdkVersion 23
com.android.tools.build:gradle:1.3.0
Android Studio 1.5.1, built on December 1, 2015
JRE: 1.8.0_45-b14 x86_64
JDK: jdk1.8.0_45
- add android-support-v13.jar to libs/
- add permission "android.permission.CAMERA" for bar code scanner
- add runtime permission ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE, CAMERA for android os 6
- remove BP80 upload/print from ram
[bp80demo]

[bp80mfgtest]
- disable "Direct Send", "BT", "PRINT", "LED", "KBD", "FLASH", "RECORD" buttons until revamp is finished
- create new class "BP80mfgtest_loopbackClientActivity.java" and not use "MFGTestLoopbackClientActivity.java" in ANDROID_LIB_SPECTRATECH


20151208
versionName: 1.0.2
versionCode: 34
use ANDROID_LIB_SPECTRATECH version 1.1.0
compileSdkVersion 22
buildToolsVersion "22.0.1"
minSdkVersion 15
targetSdkVersion 21
com.android.tools.build:gradle:1.3.0
Android Studio 1.4.1, built on October 15, 2015
JRE: 1.8.0_45-b14 x86_64
JDK: jdk1.8.0_45
- bp80demo add using zxing_lib (version 1.0.1) 

20151112
versionName: 1.0.1
versionCode: 33
Use ANDROID_LIB_SPECTRATECH version: 1.0.0
compileSdkVersion 22
buildToolsVersion "22.0.1"
minSdkVersion 15
targetSdkVersion 21
com.android.tools.build:gradle:1.3.0
Android Studio 1.4.1, built on October 15, 2015
JRE: 1.8.0_45-b14 x86_64
JDK: jdk1.8.0_45
- fix bluetooth disconnect crash on Android OS 4.4

20151111
versionName: 1.0.0
versionCode: 32
compileSdkVersion 22
buildToolsVersion "22.0.1"
minSdkVersion 15
targetSdkVersion 21
com.android.tools.build:gradle:1.3.0
Android Studio 1.4.1, built on October 15, 2015
JRE: 1.8.0_45-b14 x86_64
JDK: jdk1.8.0_45
- remove reflection method of bluetooth connection for android devices with os version 4.0.3(api15) or 4.1.2(api16)
- remove invert bit order in ImageHelper
- [BP80]fix can't set serial number in bp80 when default is no serial
- [SP530DEMO]add S3-Template tab page, support sending configuration values to device

20151103
versionName: 0.1.17
- fix crash when return data does not contain transaction date, time and terminal identification.

20151009
versionName: 0.1.16
- support QR scan
- support URL scheme
- remove disconnect pending state
- remove K_TcpStateDisconnecting parameter
