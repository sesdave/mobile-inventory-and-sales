package com.spectratech.sp530demo.techicalservices;

import android.content.Context;
import android.util.Log;

import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.domain.inventory.Inventory;
import com.spectratech.sp530demo.domain.inventory.ProductCatalog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Reads a demo products from CSV in res/raw/
 * 
 * @author Refresh Team
 *
 */
public class Demo {

	/**
	 * Adds the demo product to inventory.
	 * @param context The current stage of the application.
	 */
	public static void testProduct(Context context) {
        InputStream instream = context.getResources().openRawResource(R.raw.products);
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		String line;
		try {
			ProductCatalog catalog = Inventory.getInstance().getProductCatalog();
			while (( line = reader.readLine()) != null ) {
				String[] contents = line.split(",");
				Log.d("Demo", contents[0] + ":" + contents[1] + ": " + contents[2]);
				catalog.addProduct(contents[1], contents[0], Double.parseDouble(contents[2]),contents[3]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
