package com.spectratech.sp530demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Bytes;
import com.spectratech.lib.ImageHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.data.Data_binaryImageBitPadded;
import com.spectratech.lib.printer.bp80.Lpt_funcCHelper;
import com.spectratech.lib.printer.bp80.constant.BP80_Constant;
import com.spectratech.lib.printer.bp80.constant.BTPrinterProtocolConstant;
import com.spectratech.sp530demo.conf.PathClass;
import com.spectratech.sp530demo.data.Data_setting_devicebluetoothprinter;
import com.spectratech.sp530demo.view.CustomLinearLayoutWithMeasureWH;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BTPrinterBluetoothConnectActivity - activity to generate image for Bluetooth printer
 */
public class BTPrinterGenerateImageActivity extends SP530DemoBaseActivity {
    private static final String m_className="BTPrinterGenerateImageActivity";

    /**
     * Request code for loading image from photo album
     */
    public static final int REQ_CODE_LOADIMAGE_FROM_PHOTOALBUM = 1;

    /**
     * Variable to store maximum pixel width which could be stored in storage of printer
     */
    public static final int PRINT_WIDTH=BP80_Constant.MAX_PIXEL_WIDTH;
    /**
     * Variable to store maximum pixel height which could be stored in storage of printer
     */
    public static final int PRINT_HEIGHT=48;

    private LinearLayout m_mainll;

    private CustomLinearLayoutWithMeasureWH m_container_orgimg;
    private ImageView m_orgIV;

    private CustomLinearLayoutWithMeasureWH m_container_printimg;
    private ImageView m_printIV;

    private int m_widthContainer;
    private int m_heightContainer;

    // photo
    private Bitmap m_inputBmp;
    private Bitmap m_printBmp;

    private TextView m_threshold_tv;
    private SeekBar m_threshold_seekbar;
    private int m_threshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_btprintergenerateimage, null);
        setContentView(m_mainll);

        String strTitle=ResourcesHelper.getBaseContextString(m_context, R.string.title_printer_gen_img);
        setTitle(strTitle);
        setActionBarTitle(strTitle);

        m_container_orgimg=(CustomLinearLayoutWithMeasureWH)m_mainll.findViewById(R.id.container_orgimg);
        m_orgIV=(ImageView)m_mainll.findViewById(R.id.orgimg);

        m_container_printimg=(CustomLinearLayoutWithMeasureWH)m_mainll.findViewById(R.id.container_printimg);
        m_printIV=(ImageView)m_mainll.findViewById(R.id.printimg);

        m_threshold=127;
        m_threshold_tv=(TextView)m_mainll.findViewById(R.id.threshold_tv);
        m_threshold_seekbar=(SeekBar)m_mainll.findViewById(R.id.threshold_seekbar);
        setThresholdUI();
        m_threshold_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_threshold=progress;
                setThresholdUI_text();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        m_widthContainer=-1;
        m_heightContainer=-1;

        m_inputBmp=null;
        m_printBmp=null;

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    private void setThresholdUI() {
        setThresholdUI_text();
        setThresholdUI_seekbar();
    }
    private void setThresholdUI_text() {
        if (m_threshold_tv != null) {
            m_threshold_tv.setText("" + m_threshold);
        }
    }
    private void setThresholdUI_seekbar() {
        if (m_threshold_seekbar!=null) {
            m_threshold_seekbar.setProgress(m_threshold);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        validateImageDimension(true);
    }

    /**
     * Get path of image
     * @param context context of application
     * @param contentUri Uri of content
     * @return path of content
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    /**
     * Get capture image matrix from orientation value
     * @param orientation orientation value
     * @return Matrix corresponds to the input orientation value
     */
    public Matrix getCaptureImageOrientationMatrix(int orientation) {
        Matrix matrix = new Matrix();
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            matrix.postRotate(90.0f);
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            matrix.postRotate(180.0f);
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            matrix.postRotate(270.0f);
        }
        return matrix;
    }

    /**
     * Get capture image File object
     * @return File object
     */
    public File getCaptureImageFile() {
        String imageFolderPath = PathClass.EXTERNAL_IMGCAPTUREPATH;
        String imageName="captureprintimage.png";
        File image = new File(imageFolderPath, imageName);
        return image;
    }

    /**
     * Get capture image File Uri object
     * @return Uri object
     */
    public Uri getCaptureImageFileUri() {
        File image=getCaptureImageFile();
        Uri fileUri = Uri.fromFile(image);
        return fileUri;
    }

    /**
     * Write bitmap data to catpure folder
     * @param bmp input bitmap
     */
    public void write2ImageCaptureFolder(Bitmap bmp) {
        File file=getCaptureImageFile();
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception ex) {
            Logger.e(m_className, "write2ImageCaptureFolder, Exception");
        }
    }

    /**
     * Write bitmap data to capture folder
     * @param bmp input bit map
     * @param orientation input orientation value
     */
    public void writeValidateCaptureImage2Folder(Bitmap bmp, int orientation) {
        if (bmp==null) {
            return;
        }

        //int wScale=512;
        int wScale=PRINT_WIDTH;

        int wOrg=bmp.getWidth();
        int hOrg=bmp.getHeight();
        //int nh = (int) ( hOrg * ((float)wScale / wOrg) );
        int nh=PRINT_HEIGHT;

        Matrix matrix=getCaptureImageOrientationMatrix(orientation);
        int wNew=wScale;
        int hNew=nh;
        if (orientation>0) {
            if ( (orientation == ExifInterface.ORIENTATION_ROTATE_90) || (orientation == ExifInterface.ORIENTATION_ROTATE_270) ) {
                wNew=nh;
                hNew=wScale;
            }
        }

        Bitmap result=null;

        result=Bitmap.createBitmap(bmp, 0, 0, wOrg, hOrg, matrix, false);
        if (result!=null) {
            if (wOrg!=wScale) {
                result=Bitmap.createScaledBitmap(result, wNew, hNew, true);
            }
        }
        if (result!=null) {
            write2ImageCaptureFolder(result);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_LOADIMAGE_FROM_PHOTOALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getData() != null && !data.getData().equals("")) {
                    try {
                        String realPath=getRealPathFromURI(m_context, (Uri) data.getData());
                        int orientation=-1;
                        try {
                            ExifInterface exifInterface = new ExifInterface(realPath);
                            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        }
                        catch (Exception exception) {
                            Logger.v(m_className, "onActivityResult, could not get orientation of the image");
                        }

                        InputStream is = getContentResolver().openInputStream((Uri) data.getData());
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        is.close();

                        // write validate capture image to file
                        writeValidateCaptureImage2Folder(bitmap, orientation);

                        Uri fileUri=getCaptureImageFileUri();
                        is = getContentResolver().openInputStream(fileUri);
                        m_inputBmp = BitmapFactory.decodeStream(is);
                        is.close();

                        if (m_inputBmp!=null) {
                            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(m_widthContainer, m_heightContainer);
                            m_container_orgimg.setLayoutParams(lp);
                            m_orgIV.setImageBitmap(m_inputBmp);
                            m_orgIV.setScaleType(ImageView.ScaleType.FIT_XY);
                        }

                    } catch (Exception e) {
                        Logger.e(m_className, "onActivityResult, REQ_CODE_CHG_PROFILE_PHOTOALBUM, Exception");
                    }
                }
            }
        }
    }

    private void updatePrintImageView() {
        if (m_printBmp!=null) {
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(m_widthContainer, m_heightContainer);
            m_container_printimg.setLayoutParams(lp);
            m_printIV.setImageBitmap(m_printBmp);
            m_printIV.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    /**
     * Validate dimension of input image to usable dimension for printing
     */
    public void validateImageDimension() {
        validateImageDimension(false);
    }
    public void validateImageDimension(boolean bForce) {
        if ( (m_widthContainer<1)||(bForce) ) {
            m_widthContainer = m_container_orgimg.getCustomMeasuredWidth();
            if (m_widthContainer>0) {
                m_heightContainer=(int)(((double)PRINT_HEIGHT/PRINT_WIDTH)*m_widthContainer);
                m_heightContainer*=2;
            }
            Logger.i(m_className, "validateAspectRatio, m_widthContainer: " + m_widthContainer + ", m_heightContainer: " + m_heightContainer);
        }
    }

    /**
     * Check for printer ready
     * @return true if ready; false otherwise
     */
    private boolean isReadyForPrint() {
        boolean flag = false;
        if (m_printBmp == null) {
            Logger.w(m_className, "printImage, m_printBmp is NULL");
            return flag;
        }
        return BTPrinterBluetoothConnectActivity.isReadyPrinter();
    }

    /**
     * Print generated image
     */
    private void printImage() {
        if (!isReadyForPrint()) {
            return;
        }

        BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
        InputStream is=btSocketClass.getInputStream();
        OutputStream os=btSocketClass.getOutputStream();

        byte[] tmpData=null;
        List<Byte> dataList=new ArrayList<Byte>();
        ImageHelper imageHelper=ImageHelper.getInstance();
        Data_binaryImageBitPadded dataBinaryImageBitPadded=imageHelper.padToBinaryBitData(m_printBmp, true);
        dataList.add(BTPrinterProtocolConstant.ESC_VAL);
        dataList.add(BTPrinterProtocolConstant.CMD_PRINTGRAPHIC[0]);
        dataList.add((byte)(dataBinaryImageBitPadded.m_width/8));
        dataList.add((byte)dataBinaryImageBitPadded.m_height);
        tmpData=dataBinaryImageBitPadded.m_data;
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpData)));

        Lpt_funcCHelper instLpt_funcCHelper=Lpt_funcCHelper.getInstance();
        Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
        instLpt_funcCHelper.setPacketEncapsulateLevel(dataBTPrinterConf.m_packetEncapsulateLevel);
        instLpt_funcCHelper.setIO(is, os);
        instLpt_funcCHelper.setContext(m_context);
        byte[] dataBuf=null;
        dataBuf= Bytes.toArray(dataList);
        byte[] printBuf=new byte[dataBuf.length+4];
        instLpt_funcCHelper.page_pack_start(printBuf, printBuf.length);
        instLpt_funcCHelper.page_pack_mem(dataBuf, dataBuf.length);
        instLpt_funcCHelper.page_pack_end();
    }

    private void printFromflash(int idx) {
        if (!BTPrinterBluetoothConnectActivity.isReadyPrinter()) {
            return;
        }
        if ( (idx!=0)&&(idx!=1) ) {
            Logger.w(m_className, "printFromflash, index error");
            return;
        }

        BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
        InputStream is=btSocketClass.getInputStream();
        OutputStream os=btSocketClass.getOutputStream();

        byte[] tmpData=null;
        List<Byte> dataList=new ArrayList<Byte>();
        dataList.add(BTPrinterProtocolConstant.ESC_VAL);
        switch (idx) {
            case 0: {
                dataList.addAll( Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_PRINTLOGO_FROM_FLASH0)) );
            }
            break;
            case 1: {
                dataList.addAll( Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_PRINTLOGO_FROM_FLASH1)) );
            }
            break;
        }

        Lpt_funcCHelper instLpt_funcCHelper=Lpt_funcCHelper.getInstance();
        Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
        instLpt_funcCHelper.setPacketEncapsulateLevel(dataBTPrinterConf.m_packetEncapsulateLevel);
        instLpt_funcCHelper.setIO(is, os);
        instLpt_funcCHelper.setContext(m_context);
        byte[] dataBuf=null;
        dataBuf= Bytes.toArray(dataList);
        byte[] printBuf=new byte[dataBuf.length+4];
        instLpt_funcCHelper.page_pack_start(printBuf, printBuf.length);
        instLpt_funcCHelper.page_pack_mem(dataBuf, dataBuf.length);
        instLpt_funcCHelper.page_pack_end();
    }

    private void upload2flash(int idx) {
        if (!isReadyForPrint()) {
            return;
        }
        if ( (idx!=0)&&(idx!=1)&&(idx!=9) ) {
            Logger.w(m_className, "upload2flash, index error");
            return;
        }

        BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
        InputStream is=btSocketClass.getInputStream();
        OutputStream os=btSocketClass.getOutputStream();

        byte[] tmpData=null;
        List<Byte> dataList=new ArrayList<Byte>();
        ImageHelper imageHelper=ImageHelper.getInstance();
        Data_binaryImageBitPadded dataBinaryImageBitPadded=imageHelper.padToBinaryBitData(m_printBmp, true);
        dataList.add(BTPrinterProtocolConstant.ESC_VAL);

        switch (idx) {
            case 0: {
                dataList.addAll( Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_STORELOGO_FLASH0)) );
            }
            break;
            case 1: {
                dataList.addAll( Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_STORELOGO_FLASH1)) );
            }
            break;
            case 9: {
                dataList.addAll( Arrays.asList(ArrayUtils.toObject(BTPrinterProtocolConstant.CMD_STORELOGO_RAM)) );
            }
            break;
        }
        dataList.add((byte)(dataBinaryImageBitPadded.m_width/8));
        dataList.add((byte) dataBinaryImageBitPadded.m_height);
        tmpData=dataBinaryImageBitPadded.m_data;
        dataList.addAll(Arrays.asList(ArrayUtils.toObject(tmpData)));

        Lpt_funcCHelper instLpt_funcCHelper=Lpt_funcCHelper.getInstance();
        Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
        instLpt_funcCHelper.setPacketEncapsulateLevel(dataBTPrinterConf.m_packetEncapsulateLevel);
        instLpt_funcCHelper.setIO(is, os);
        instLpt_funcCHelper.setContext(m_context);
        byte[] dataBuf=null;
        dataBuf= Bytes.toArray(dataList);
        byte[] printBuf=new byte[dataBuf.length+4];
        instLpt_funcCHelper.page_pack_start(printBuf, printBuf.length);
        instLpt_funcCHelper.page_pack_mem(dataBuf, dataBuf.length);
        instLpt_funcCHelper.page_pack_end();
    }

    /**
     * On click function for printer management
     * @param v corresponding view
     */
    public void onClick_manageprinter(View v) {
        BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
    }

    /**
     * On click function for loading image
     * @param v corresponding view
     */
    public void onClick_loadimage(View v) {
        Logger.i(m_className, "onClick_loadimage");
        if (!isOnPause()) {
            validateImageDimension();
            startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), REQ_CODE_LOADIMAGE_FROM_PHOTOALBUM);
        }
    }

    /**
     * On click function for generating image
     * @param v corresponding view
     */
    public void onClick_generateimage(View v) {
        Logger.i(m_className, "onClick_generateimage");
        String strPleaseLoadImg=ResourcesHelper.getBaseContextString(m_context, R.string.please_load_img);
        if (m_inputBmp==null) {
            Toast.makeText(m_context, strPleaseLoadImg, Toast.LENGTH_SHORT).show();
            return;
        }

        ImageHelper imageHelper=ImageHelper.getInstance();
        m_printBmp=imageHelper.convert2PrinterBP80Bmp(m_inputBmp, m_threshold);
        updatePrintImageView();
    }

    /**
     * On click function for printing image
     * @param v corresponding view
     */
    public void onClick_print(View v) {
        Logger.i(m_className, "onClick_print");
        String strPleaseCreatePrintImg=ResourcesHelper.getBaseContextString(m_context, R.string.please_create_print_img);
        if (m_printBmp==null) {
            Toast.makeText(m_context, strPleaseCreatePrintImg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity==null) {
            BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
            return;
        }
        printImage();
    }

    /**
     * On click function for uploading generated image data to serial flash position zero
     * @param v corresponding view
     */
    public void onClick_uploadflashzero(View v) {
        Logger.i(m_className, "onClick_uploadflashzero");
        final String strPleaseCreatePrintImg=ResourcesHelper.getBaseContextString(m_context, R.string.please_create_print_img);

        if (m_printBmp==null) {
            Toast.makeText(m_context, strPleaseCreatePrintImg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity==null) {
            BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
            return;
        }
        upload2flash(0);
    }

    /**
     * On click function for uploading generated image data to serial flash position one
     * @param v corresponding view
     */
    public void onClick_uploadflashone(View v) {
        Logger.i(m_className, "onClick_uploadflashone");
        final String strPleaseCreatePrintImg=ResourcesHelper.getBaseContextString(m_context, R.string.please_create_print_img);

        if (m_printBmp==null) {
            Toast.makeText(m_context, strPleaseCreatePrintImg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity==null) {
            BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
            return;
        }
        upload2flash(1);
    }

    /**
     * On click function for uploading generated image data to ram
     * @param v corresponding view
     */
    public void onClick_uploadram(View v) {
        Logger.i(m_className, "onClick_uploadram");
        final String strPleaseCreatePrintImg=ResourcesHelper.getBaseContextString(m_context, R.string.please_create_print_img);

        if (m_printBmp==null) {
            Toast.makeText(m_context, strPleaseCreatePrintImg, Toast.LENGTH_SHORT).show();
            return;
        }
        if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity==null) {
            BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
            return;
        }
        upload2flash(9);
    }

    /**
     * On click function for printing data in serial flash position zero
     * @param v corresponding view
     */
    public void onClick_printflashzero(View v) {
        Logger.i(m_className, "onClick_printflashzero");
        if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity==null) {
            BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
            return;
        }
        printFromflash(0);
    }

    /**
     * On click function for printing data in serial flash position one
     * @param v corresponding view
     */
    public void onClick_printflashone(View v) {
        Logger.i(m_className, "onClick_printflashone");
        if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity==null) {
            BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
            return;
        }
        printFromflash(1);
    }

    private void onOrientationPortrait() {
        // not full screen mode
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void onOrientationLandscape() {
        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                onOrientationPortrait();
            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE: {
                onOrientationLandscape();
            }
            break;
        }
    }
}
