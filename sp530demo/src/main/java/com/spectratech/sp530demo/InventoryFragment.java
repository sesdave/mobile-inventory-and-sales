package com.spectratech.sp530demo;

/**
 * Created by ASUS on 11/23/2017.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.google.zxing.integration.android.IntentResult;
import com.spectratech.sp530demo.component.ButtonAdapter1;
import com.spectratech.sp530demo.component.IntentIntegratorSupportV4;
import com.spectratech.sp530demo.domain.inventory.Inventory;
import com.spectratech.sp530demo.domain.inventory.Product;
import com.spectratech.sp530demo.domain.inventory.ProductCatalog;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.extra.UpdatableFragment;
import com.spectratech.sp530demo.inventory.AddProductDialogFragment;
import com.spectratech.sp530demo.techicalservices.DatabaseExecutor;
import com.spectratech.sp530demo.techicalservices.Demo;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@SuppressLint("ValidFragment")
public class InventoryFragment extends UpdatableFragment {

    protected static final int SEARCH_LIMIT = 0;
    private ListView inventoryListView;
    private ProductCatalog productCatalog;
    private List<Map<String, String>> inventoryList;
    private Button addProductButton;
    private EditText searchBox;
    private Button scanButton;

    private ViewPager viewPager;
    private Register register;
    private MainActivity main;

    private UpdatableFragment saleFragment;
    private Resources res;
    private int passSale;

    /**
     * Construct a new InventoryFragment.
     * @param saleFragment
     */
    public InventoryFragment(UpdatableFragment saleFragment) {
        super();
        this.saleFragment = saleFragment;
    }
    public interface OnDataPass{
        public void onDataPass(int data);
    }
    public interface OnDataId{
        public void OnDataId(int data);
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

        View view = inflater.inflate(R.layout.norm_fragment_layout, container, false);

        res = getResources();
        inventoryListView = (ListView) view.findViewById(R.id.productListView);
        addProductButton = (Button) view.findViewById(R.id.addProductButton);
        scanButton = (Button) view.findViewById(R.id.scanButton);
        searchBox = (EditText) view.findViewById(R.id.searchBox);

        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();

        View emptyView = view.findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);
        //passData(passSale);
        passId(0);

        initUI();
        return view;
    }

    OnDataPass dataPasser;
    OnDataId dataId;
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        dataPasser=(OnDataPass)context;
        dataId=(OnDataId)context;
    }

    /**
     * Initiate this UI.
     */
    private void initUI() {

       /* addProductButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPopup(v);
            }
        });*/


        searchBox.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.length() >= SEARCH_LIMIT) {
                    search();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {

                    String priceValue=inventoryList.get(position).get("unitPrice").toString();
               // scanButton.setText(priceValue);
                    if(priceValue.equals("Variable")){
                        String halo="Hello";
                        int id = Integer.parseInt(inventoryList.get(position).get("id").toString());
                        passId(id);
                       /* Bundle bundle=new Bundle();
                        bundle.putInt("ide",id);
                        bundle.putString("Hey",halo+"");

                        VariableFragment vf=new VariableFragment(saleFragment);
                        vf.setArguments(bundle);
                        vf.show(getFragmentManager(), "");*/
                        viewPager.setCurrentItem(5);


                    }else{
                        int id = Integer.parseInt(inventoryList.get(position).get("id").toString());
                        register.addItem("null",productCatalog.getProductById(id), 1);
                        saleFragment.update();
                        viewPager.setCurrentItem(0);
                        passSale++;
                        passData(passSale);
                    }







               // viewPager.setCurrentItem(0);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegratorSupportV4 scanIntegrator = new IntentIntegratorSupportV4(InventoryFragment.this);
                scanIntegrator.initiateScan();
            }
        });

    }

    /**
     * Show list.
     * @param list
     */
    private void showList(List<Product> list) {

        inventoryList = new ArrayList<Map<String, String>>();
        for(Product product : list) {
            inventoryList.add(product.toMap());
        }

        ButtonAdapter1 sAdap = new ButtonAdapter1(getActivity().getBaseContext(), inventoryList,
                R.layout.listview_inventory, new String[]{"name","image","unitPrice"}, new int[] {R.id.name,R.id.image_view,R.id.price});
        inventoryListView.setAdapter(sAdap);
    }

    /**
     * Search.
     */
    private void search() {
        String search = searchBox.getText().toString();

        if (search.equals("/demo")) {
            testAddProduct();
            searchBox.setText("");
        } else if (search.equals("/clear")) {
            DatabaseExecutor.getInstance().dropAllData();
            searchBox.setText("");
        }
        else if (search.equals("")) {
            showList(productCatalog.getAllProduct());
        } else {
            List<Product> result = productCatalog.searchProduct(search);
            showList(result);
            if (result.isEmpty()) {

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, intent);

        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            searchBox.setText(scanContent);
        } else {
            Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.fail),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Test adding product
     */
    protected void testAddProduct() {
        Demo.testProduct(getActivity());
        Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.success),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Show popup.
     * @param anchorView
     */
    public void showPopup(View anchorView) {
        AddProductDialogFragment newFragment = new AddProductDialogFragment(InventoryFragment.this);
        newFragment.show(getFragmentManager(), "");
    }

    @Override
    public void update() {
        search();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void passData(int data){
        dataPasser.onDataPass(data);
    }
    public void passId(int data){
        dataId.OnDataId(data);
    }

}
