package com.spectratech.sp530demo.sale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.domain.inventory.LineItem;
import com.spectratech.sp530demo.domain.sale.Register;
import com.spectratech.sp530demo.extra.UpdatableFragment;
import com.spectratech.sp530demo.techicalservices.NoDaoSetException;


@SuppressLint("ValidFragment")
public class EditFragmentDialog extends DialogFragment {
	private Register register;
	private UpdatableFragment saleFragment;
	private UpdatableFragment reportFragment;
	private EditText quantityBox;
	private EditText priceBox;
	private Button comfirmButton;
	private String saleId;
	private String position;
	private Double vaprice;
	private LineItem lineItem;
	private Button removeButton;

	/**
	 * Construct a new  EditFragmentDialog.
	 * @param saleFragment
	 * @param reportFragment
	 */
	public EditFragmentDialog(UpdatableFragment saleFragment, UpdatableFragment reportFragment) {
		super();
		this.saleFragment = saleFragment;
		this.reportFragment = reportFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_saleedit, container, false);
		try {
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		quantityBox = (EditText) v.findViewById(R.id.quantityBox);
		priceBox = (EditText) v.findViewById(R.id.priceBox);
		comfirmButton = (Button) v.findViewById(R.id.confirmButton);
		removeButton = (Button) v.findViewById(R.id.removeButton);

		saleId = getArguments().getString("sale_id");
		position = getArguments().getString("position");
		vaprice = getArguments().getDouble("varprice");

		lineItem = register.getCurrentSale().getLineItemAt(Integer.parseInt(position));
		quantityBox.setText(lineItem.getQuantity()+"");
		if((lineItem.getProduct().getUnitPrice())==0.0){
			priceBox.setText(vaprice+"");
		}else{
			priceBox.setText(lineItem.getProduct().getUnitPrice()+"");
		}

		removeButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Log.d("remove", "id=" + lineItem.getId());
				register.removeItem(lineItem);
				end();
			}
		});

		comfirmButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				register.updateItem(
						Integer.parseInt(saleId),
						lineItem,
						Integer.parseInt(quantityBox.getText().toString()),
						Double.parseDouble(priceBox.getText().toString())
				);

				end();
			}

		});
		return v;
	}

	/**
	 * End.
	 */
	private void end(){
		saleFragment.update();
		reportFragment.update();
		this.dismiss();
	}


}
