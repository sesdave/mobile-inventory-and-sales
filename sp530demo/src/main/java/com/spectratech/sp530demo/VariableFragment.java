package com.spectratech.sp530demo;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.sp530demo.domain.inventory.Inventory;
import com.spectratech.sp530demo.domain.inventory.ProductCatalog;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.extra.UpdatableFragment;
import com.spectratech.sp530demo.techicalservices.Demo;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import java.util.List;
import java.util.Map;



/**
 * Created by ASUS on 12/15/2017.
 */
@SuppressLint("ValidFragment")
public class VariableFragment extends UpdatableFragment {

    // private UpdatableFragment saleFragment;
    private static final String TAG = "[MainActivity]";
    private static String tp_seq = "", token_id = "";
    final int PIN_LENGTH = 6;

    EditText etVerificationCode;
    //Toolbar toolbar;

    TextView tvVerificationText;

    Button button0;

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button7;
    Button button8;
    Button button9;
    Button buttonc;
    ImageButton buttonClear;
    TextView buttonProceed;
    ImageButton buttonProceedn;
    private String verificationCode = "";
    private String userEntered = "";
    private Bundle bundle;
    private Integer id;
    private ProductCatalog productCatalog;
    private Register register;
    private MainActivity main;
    private ViewPager viewPager;
    private UpdatableFragment saleFragment;
    private EditText searchBox;
    private ListView inventoryListView;
    private List<Map<String, String>> inventoryList;
    private Resources res;
    private Integer ide;
    private String Hal;
    private Integer dataId;


    private ToneGenerator toneGeneratorKeypad;


    private View.OnTouchListener otl = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    public VariableFragment(UpdatableFragment saleFragment) {
        super();
        this.saleFragment = saleFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
             productCatalog = Inventory.getInstance().getProductCatalog();
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }


        View view = inflater.inflate(R.layout.layout_variable_frag, container, false);

        res = getResources();
       // Hal=this.getArguments().getString("halo");
        etVerificationCode = (EditText) view.findViewById(R.id.etVerificationCode);
        button0 = (Button) view.findViewById(R.id.button0);
        button1 = (Button) view.findViewById(R.id.button1);
        button2 = (Button) view.findViewById(R.id.button2);
        button3 = (Button) view.findViewById(R.id.button3);
        button4 = (Button) view.findViewById(R.id.button4);
        button5 = (Button) view.findViewById(R.id.button5);
        button6 = (Button) view.findViewById(R.id.button6);
        button7 = (Button) view.findViewById(R.id.button7);
        button8 = (Button) view.findViewById(R.id.button8);
        button9 = (Button) view.findViewById(R.id.button9);
        buttonc = (Button) view.findViewById(R.id.buttonc);
        searchBox = (EditText) view.findViewById(R.id.searchBox);
        inventoryListView = (ListView) view.findViewById(R.id.productListView);

        buttonClear = (ImageButton) view.findViewById(R.id.buttonClear);
        buttonProceedn = (ImageButton) view.findViewById(R.id.buttonProceedn);
        buttonc.setText(Hal);
        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();
       // ide = getArguments().getInt("ide");

        //setSupportActionBar(toolbar);

        toneGeneratorKeypad = new ToneGenerator(AudioManager.STREAM_DTMF, 70);


        initUI();
        return view;
    }


    private void initUI() {
        etVerificationCode.setOnTouchListener(otl);

        //buttonProceed.setEnabled(true);

        etVerificationCode.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            @SuppressLint("LongLogTag")
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //                Log.d(TAG, "count(beforeTextChanged)===>"+count);
            }

            @SuppressLint("LongLogTag")
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "count(onTextChanged)===>" + count);
                if (count >= 6) {
                    Log.d(TAG, "count===>" + count);
                    //etVerificationCode.setEnabled(false);
                   // buttonProceed.setEnabled(true);
                } else {
                    Log.d(TAG, "else count===>" + count);
                    //buttonProceed.setEnabled(false);
                }
            }
        });


        etVerificationCode.setOnEditorActionListener(new EditText.OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "actionId===>" + actionId);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //                    performClickAction();
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    return true;
                }
                return false;
            }
        });


        try {

            buttonClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userEntered.length() > 0) {
                        userEntered = userEntered.substring(0, userEntered.length() - 1);
                        Log.v("PinView", "User entered=" + userEntered);
                        etVerificationCode.setText(userEntered);
                    }
                }
            });

            buttonProceedn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(dataId==0){
                        verificationCode = etVerificationCode.getText().toString().trim();
                        buttonc.setText(verificationCode);
                        double varprice = Double.parseDouble(verificationCode);
                        register.addItem("custom", 1,varprice);
                        saleFragment.update();
                        etVerificationCode.setText("");
                        viewPager.setCurrentItem(0);

                    }else{
                        verificationCode = etVerificationCode.getText().toString().trim();
                       // buttonc.setText(verificationCode);
                        double varprice = Double.parseDouble(verificationCode);

                        //int ide = bundle.getInt("ide");
                        //ide = getArguments().getInt("ide");
                        register.addItem("null",productCatalog.getProductById(dataId), 1,varprice);
                        saleFragment.update();
                         etVerificationCode.setText("");
                        viewPager.setCurrentItem(0);
                        // VariableFragment.this.dismiss();
                    }



                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            View.OnClickListener pinButtonHandler = new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressedButton = (Button) v;
                    if (userEntered.length() < PIN_LENGTH) {
                        userEntered = userEntered + pressedButton.getText();

                        if (pressedButton.getText().toString().equalsIgnoreCase("0")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_0, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("1")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_1, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("2")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_2, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("3")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_3, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("4")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_4, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("5")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_5, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("6")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_6, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("7")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_7, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("8")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_8, 120);
                        } else if (pressedButton.getText().toString().equalsIgnoreCase("9")) {
                            toneGeneratorKeypad.stopTone();
                            toneGeneratorKeypad.startTone(ToneGenerator.TONE_DTMF_9, 120);
                        }

                        Log.v("PinView", "User entered=" + userEntered);
                        etVerificationCode.setText(userEntered);
                    }
                }
            };

            // Keypad click listener
            button0.setOnClickListener(pinButtonHandler);
            button1.setOnClickListener(pinButtonHandler);
            button2.setOnClickListener(pinButtonHandler);
            button3.setOnClickListener(pinButtonHandler);
            button4.setOnClickListener(pinButtonHandler);
            button5.setOnClickListener(pinButtonHandler);
            button6.setOnClickListener(pinButtonHandler);
            button7.setOnClickListener(pinButtonHandler);
            button8.setOnClickListener(pinButtonHandler);
            button9.setOnClickListener(pinButtonHandler);


//            buttonClear.setOnClickListener(pinButtonHandler);
//            buttonProceed.setOnClickListener(pinButtonHandler);
//
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void testAddProduct() {
        Demo.testProduct(getActivity());
        Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.success),
                Toast.LENGTH_SHORT).show();
    }


public void update(){}

    @Override
    public void onResume() {
        super.onResume();
       // update();
    }
    public void addDataId(int data){
        this.dataId=data;
    }

}

