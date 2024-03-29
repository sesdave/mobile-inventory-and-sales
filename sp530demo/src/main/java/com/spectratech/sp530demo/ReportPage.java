package com.spectratech.sp530demo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.spectratech.sp530demo.domain.DateTimeStrategy;
import com.spectratech.sp530demo.domain.sale.Sale;
import com.spectratech.sp530demo.domain.sale.SaleLedger;
import com.spectratech.sp530demo.sale.SaleDetailActivity;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;



/**
 * Created by ASUS on 11/27/2017.
 */

public class ReportPage extends AppCompatActivity {
    private SaleLedger saleLedger;
    List<Map<String, String>> saleList;
    private ListView saleLedgerListView;
    private TextView totalBox;
    private Spinner spinner;
    private Button previousButton;
    private Button nextButton;
    private TextView currentBox;
    private Calendar currentTime;
    private DatePickerDialog datePicker;

    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;
    public static final int YEARLY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.black_fragment_layout);

        try {
            saleLedger = SaleLedger.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }


        previousButton = (Button)findViewById(R.id.previousButton);
        nextButton = (Button)findViewById(R.id.nextButton);
        currentBox = (TextView)findViewById(R.id.currentBox);
        saleLedgerListView = (ListView)findViewById(R.id.saleListView);
        totalBox = (TextView)findViewById(R.id.totalBox);
        spinner = (Spinner)findViewById(R.id.spinner1);

        initUI();

    }

    /**
     * Initiate this UI.
     */
    private void initUI() {
        currentTime = Calendar.getInstance();
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int y, int m, int d) {
                currentTime.set(Calendar.YEAR, y);
                currentTime.set(Calendar.MONTH, m);
                currentTime.set(Calendar.DAY_OF_MONTH, d);
                update();
            }
        }, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }

        });

        currentBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });



        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDate(-1);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDate(1);
            }
        });

        saleLedgerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
                String id = saleList.get(position).get("id").toString();
                Intent newActivity = new Intent(ReportPage.this, SaleDetailActivity.class);
                newActivity.putExtra("id", id);
                startActivity(newActivity);
            }
        });

    }

    /**
     * Show list.
     * @param list
     */
    private void showList(List<Sale> list) {

        saleList = new ArrayList<Map<String, String>>();
        for (Sale sale : list) {
            saleList.add(sale.toMap());
        }

        SimpleAdapter sAdap = new SimpleAdapter(this , saleList,
                R.layout.listview_report, new String[] { "id", "startTime", "total"},
                new int[] { R.id.sid, R.id.startTime , R.id.total});
        saleLedgerListView.setAdapter(sAdap);
    }


    public void update() {
        int period = spinner.getSelectedItemPosition();
        List<Sale> list = null;
        Calendar cTime = (Calendar) currentTime.clone();
        Calendar eTime = (Calendar) currentTime.clone();

        if(period == DAILY){
            currentBox.setText(" [" + DateTimeStrategy.getSQLDateFormat(currentTime) +  "] ");
            currentBox.setTextSize(16);
        } else if (period == WEEKLY){
            while(cTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                cTime.add(Calendar.DATE, -1);
            }

            String toShow = " [" + DateTimeStrategy.getSQLDateFormat(cTime) +  "] ~ [";
            eTime = (Calendar) cTime.clone();
            eTime.add(Calendar.DATE, 7);
            toShow += DateTimeStrategy.getSQLDateFormat(eTime) +  "] ";
            currentBox.setTextSize(16);
            currentBox.setText(toShow);
        } else if (period == MONTHLY){
            cTime.set(Calendar.DATE, 1);
            eTime = (Calendar) cTime.clone();
            eTime.add(Calendar.MONTH, 1);
            eTime.add(Calendar.DATE, -1);
            currentBox.setTextSize(18);
            currentBox.setText(" [" + currentTime.get(Calendar.YEAR) + "-" + (currentTime.get(Calendar.MONTH)+1) + "] ");
        } else if (period == YEARLY){
            cTime.set(Calendar.DATE, 1);
            cTime.set(Calendar.MONTH, 0);
            eTime = (Calendar) cTime.clone();
            eTime.add(Calendar.YEAR, 1);
            eTime.add(Calendar.DATE, -1);
            currentBox.setTextSize(20);
            currentBox.setText(" [" + currentTime.get(Calendar.YEAR) +  "] ");
        }
        currentTime = cTime;
        list = saleLedger.getAllSaleDuring(cTime, eTime);
        double total = 0;
        for (Sale sale : list)
            total += sale.getTotal();

        totalBox.setText(total + "");
        showList(list);
    }

    @Override
    public void onResume() {
        super.onResume();
        // update();
        // it shouldn't call update() anymore. Because super.onResume()
        // already fired the action of spinner.onItemSelected()
    }

    /**
     * Add date.
     * @param increment
     */
    private void addDate(int increment) {
        int period = spinner.getSelectedItemPosition();
        if (period == DAILY){
            currentTime.add(Calendar.DATE, 1 * increment);
        } else if (period == WEEKLY){
            currentTime.add(Calendar.DATE, 7 * increment);
        } else if (period == MONTHLY){
            currentTime.add(Calendar.MONTH, 1 * increment);
        } else if (period == YEARLY){
            currentTime.add(Calendar.YEAR, 1 * increment);
        }
        update();
    }

}
