package com.spectratech.sp530demo;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.spectratech.sp530demo.extra.UpdatableFragment;


public class ViewPagerAdapter extends FragmentPagerAdapter {

	private String[] tabsTitles;
	private UpdatableFragment[] fragments;
	private Resources res;

    public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
        //this.tabsTitles = tabsTitles;
		UpdatableFragment reportFragment = new ReportFragment();
		UpdatableFragment saleFragment = new SaleFragment(reportFragment);
		UpdatableFragment paymentpage = new PaymentPage(reportFragment);
		UpdatableFragment inventoryFragment = new InventoryFragment(
				saleFragment);
		UpdatableFragment transactionFragment = new TransactionFragment();
		VariableFragment variableFragment = new VariableFragment(
				saleFragment);

		fragments = new UpdatableFragment[] {inventoryFragment, saleFragment,reportFragment,paymentpage,transactionFragment,variableFragment};


	}



    @Override
	public Fragment getItem(int index) {

		return fragments[index];
	}
	
	@Override
	public int getCount() {
        return 6;
	}
	public void update(int index) {
		fragments[index].update();
	}
}
