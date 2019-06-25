package com.spectratech.sp530demo.sale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.spectratech.sp530demo.Application;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.MainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.domain.DateTimeStrategy;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.extra.UpdatableFragment;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;

import java.util.HashMap;


/**
 * A dialog shows the total change and confirmation for Sale.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class EndPaymentFragmentDialog extends DialogFragment {

	private Button doneButton;
	private TextView chg;
	private Register regis;
	private Context m_context;
	private DemoMainActivity act;
	private UpdatableFragment saleFragment;
	private UpdatableFragment reportFragment;
	
	/**
	 * End this UI.
	 * @param saleFragment
	 * @param reportFragment
	 */
	public EndPaymentFragmentDialog(UpdatableFragment saleFragment, UpdatableFragment reportFragment) {
		super();
		this.saleFragment = saleFragment;
		this.reportFragment = reportFragment;
	}
	public EndPaymentFragmentDialog(Context ctx, int qNumber) {
		m_context = ctx;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Activity act=getActivity();
		m_context=(Context)act;
		try {
			regis = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		View v = inflater.inflate(R.layout.dialog_paymentsuccession, container,false);
		String strtext=getArguments().getString("edttext");
		chg = (TextView) v.findViewById(R.id.changeTxt);
		chg.setText(strtext);
		doneButton = (Button) v.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				end();
			}
		});
		
		return v;
	}
	
	/**
	 * End
	 */
	private void end(){
		//regis.endSale(DateTimeStrategy.getCurrentTime());
		saleFragment.update();
		reportFragment.update();
		//DemoMainActivity act;
		startDemoTransactionReceipt();
		//Intent intent = new Intent(getActivity(), MainActivity.class);
		//startActivity(intent);
		//this.dismiss();
	}
	public void startDemoTransactionReceipt() {
			HashMap<String, String> hash = new HashMap<String, String>();
			hash.put("demo", "true");
			int id = Application.ACTIVITY_TRANSRECEIPT;
			Application.startActivity(m_context, id, hash);

	}

}
