package com.spectratech.sp530demo;

//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;


import com.spectratech.sp530demo.domain.inventory.Product;
import com.spectratech.sp530demo.domain.inventory.ProductCatalog;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.extra.SlidingTabLayout;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,InventoryFragment.OnDataPass,InventoryFragment.OnDataId,SaleFragment.OnDataPass1 {

    private ViewPager mViewPager;
    private Register register;
    private SlidingTabLayout mSlidingTabLayout;
    private TabLayout tabLayout;
    private String[] pageTitle = {" ", "  ", "","",""};
    private Button charge;
    private Button currentsale;
    private String currentInt;
    private Resources res;
    private ProductCatalog productCatalog;
    private String productId;
    private Product product;
    private ViewPagerAdapter pagerAdapter;


    @Override
    public void onDataPass(int data){
        Log.d("LOG","hello"+data);
        if (data==0){charge.setText("No Sales");
        }else {
           // currentInt = Integer.toString(data);
           // Drawable d=Drawable.createFromPath("@drawable/cart");
            //charge.enter.setImageDrawable(d);
            //charge.setBackgroundResource(R.drawable.cart);
            //currentsale.setText(currentInt);
        }
    }
    @Override
    public void OnDataId(int data){
        Log.d("LOG","hello"+data);
        VariableFragment frag=(VariableFragment) pagerAdapter.getItem(5);
        frag.addDataId(data);
        /*if (data==0){charge.setText("No Sales");
        }else {
            currentInt = Integer.toString(data);
            // Drawable d=Drawable.createFromPath("@drawable/cart");
            //charge.enter.setImageDrawable(d);
            //charge.setBackgroundResource(R.drawable.cart);
            currentsale.setText(currentInt);
        }*/
    }
    @Override
    public void onDataPass1(int currentsal,String data){
        currentInt = Integer.toString(currentsal);
        if (currentInt.equals("0")){
            currentsale.setText(R.string.no_sale);
        }else{
            currentsale.setText(currentInt);
        }


           String add="CHARGE ₦";
            charge.setText(add+data);
        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentsale=(Button) findViewById(R.id.currentsale);
        charge=(Button) findViewById(R.id.charge);

        currentsale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(1);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        for (int i = 0; i < 2; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(pageTitle[i]));
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);


        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        //change Tab selection when swipe ViewPager
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //change ViewPager page when tab selected
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        initUI();

        //mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
       // mSlidingTabLayout.setViewPager(mViewPager);
    }

    public void initUI(){
        charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mViewPager.setCurrentItem(4);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            openQuitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Open quit dialog.
     */
    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle((R.string.dialog_quit));
        quitDialog.setPositiveButton((R.string.quit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        quitDialog.setNegativeButton((R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pos) {
            /*FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ItemPage fragment=new ItemPage();
            ft.replace(R.id.frame,fragment);*/
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            // Handle the camera action
        } else if (id == R.id.nav_inventory) {
            startActivity(new Intent(MainActivity.this, ItemPage.class));
            // drawer.closeDrawer;
            //  return true;

        } else if (id == R.id.nav_transaction) {
            startActivity(new Intent(MainActivity.this, TransactionPage.class));

        } else if (id == R.id.nav_report) {
            startActivity(new Intent(MainActivity.this, ReportPage.class));

        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            

        } else if (id == R.id.nav_help) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }
}
