package com.spectratech.sp530demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

import com.spectratech.sp530demo.extra.UpdatableFragment;


/**
 * Created by ASUS on 12/28/2017.
 */

public class TransactionFragment extends UpdatableFragment {


    private Button cashTransaction;

    private Button cardTransaction;
    private Button backTransaction;


    /**
     * Construct a new SaleFragment.
     * @param
     */
    public TransactionFragment() {
        super();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.layout_transaction_frag, container, false);

        cashTransaction = (Button) view.findViewById(R.id.cashTransaction);
        cardTransaction = (Button) view.findViewById(R.id.cardTransaction);
        backTransaction = (Button) view.findViewById(R.id.transactionBack);


        initUI();
        return view;
    }

    /**
     * Initiate this UI.
     */
    private void initUI() {



        cashTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager viewPager = ((MainActivity) getActivity()).getViewPager();
                viewPager.setCurrentItem(3);
            }
        });

        backTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager viewPager = ((MainActivity) getActivity()).getViewPager();
                viewPager.setCurrentItem(0);
            }
        });

        cardTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DemoMainActivity.class));
            }
        });


    }

    /**
     * Show list
     * @param list
     */




    @Override
    public void update() {

    }


    /**
     * Show confirm or clear dialog.
     */


}

