package com.spectratech.sp530demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.spectratech.sp530demo.component.ButtonAdapter;
import com.spectratech.sp530demo.domain.inventory.Inventory;
import com.spectratech.sp530demo.domain.inventory.Product;
import com.spectratech.sp530demo.domain.inventory.ProductCatalog;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.extra.UpdatableFragment;
import com.spectratech.sp530demo.inventory.ProductDetailActivity;
import com.spectratech.sp530demo.techicalservices.DatabaseExecutor;
import com.spectratech.sp530demo.techicalservices.Demo;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class ItemPage extends AppCompatActivity {

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
    private FloatingActionButton fab;
    private String productId;
    private Product product;

    private final static String LOG_TAG = ItemPage.class.getCanonicalName();
   // InventoryDbHelper dbHelper;
    //StockCursorAdapter adapter;
    int lastVisibleItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_page);
        //dbHelper = new InventoryDbHelper(this);
        try {
            productCatalog = Inventory.getInstance().getProductCatalog();
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        res = getResources();
        inventoryListView = (ListView)findViewById(R.id.productListView);
        addProductButton = (Button)findViewById(R.id.addProductButton);
        scanButton = (Button)findViewById(R.id.scanButton);
        searchBox = (EditText) findViewById(R.id.searchBox);

        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);
        showList(productCatalog.getAllProduct());

        //main = (MainActivity) getActivity();
        //viewPager = main.getViewPager();

        fab = (FloatingActionButton) findViewById(R.id.fab);

       // View emptyView = findViewById(R.id.empty_view);
       // inventoryListView.setEmptyView(emptyView);



        initUI();


    }

    private void initUI() {

       /* addProductButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPopup(v);
            }
        });*/
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
               // AddItems fragment=new AddItems();
               // ft.replace(R.id.frame,fragment);
                startActivity(new Intent(ItemPage.this, AddItems.class));
            }
        });

       /* searchBox.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.length() >= SEARCH_LIMIT) {
                    search();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });*/

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
                int id = Integer.parseInt(inventoryList.get(position).get("id").toString());

              // register.addItem(productCatalog.getProductById(id), 1);
              // saleFragment.update();
              // viewPager.setCurrentItem(1);
            }
        });


    }

    public void onBackPressed() {

            super.onBackPressed();
            return;
        }

    private void showList(List<Product> list) {

        inventoryList = new ArrayList<Map<String, String>>();
        for(Product product : list) {
            inventoryList.add(product.toMap());
        }

        ButtonAdapter sAdap = new ButtonAdapter(this,inventoryList,
                R.layout.listview_item_page, new String[]{"name","image"}, new int[] {R.id.name,R.id.image_view}, R.id.optionView, "id");
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
            Toast.makeText(this, res.getString(R.string.fail),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void optionOnClickHandler(View view) {
        //mViewPager.setCurrentItem(0);
        String id = view.getTag().toString();
        productId = id;
        try {
            productCatalog = Inventory.getInstance().getProductCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        product = productCatalog.getProductById(Integer.parseInt(productId));
        openDetailDialog();

    }

    /**
     * Open detail dialog.
     */
    private void openDetailDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(ItemPage.this);
        quitDialog.setTitle(product.getName());
        quitDialog.setPositiveButton((R.string.remove), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                openRemoveDialog();
            }
        });

        quitDialog.setNegativeButton((R.string.product_detail), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent newActivity = new Intent(ItemPage.this,
                        ProductDetailActivity.class);
                newActivity.putExtra("id", productId);
                startActivity(newActivity);
            }
        });

        quitDialog.show();
    }

    /**
     * Open remove dialog.
     */
    private void openRemoveDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                ItemPage.this);
        quitDialog.setTitle((R.string.dialog_remove_product));
        quitDialog.setPositiveButton((R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        quitDialog.setNegativeButton((R.string.remove), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                productCatalog.suspendProduct(product);
               // pagerAdapter.update(0);
            }
        });

        quitDialog.show();
    }


    /**
     * Test adding product
     */
    protected void testAddProduct() {
        Demo.testProduct(this);
        Toast.makeText(this, res.getString(R.string.success),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Show popup.
     * @param anchorView
     */
   /* public void showPopup(View anchorView) {
        AddItems newFragment = new AddItems();
        newFragment.show(getFragmentManager(), "");
    }*/



    @Override
    public void onResume() {
        super.onResume();
       // update();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_dummy_data:
                // add dummy data for testing

             //   adapter.swapCursor(dbHelper.readStock());
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add data for demo purposes
     */

}
