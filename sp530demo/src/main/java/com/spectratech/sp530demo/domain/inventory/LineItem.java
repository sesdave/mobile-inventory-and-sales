package com.spectratech.sp530demo.domain.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * LineItem of Sale.
 * 
 * @author Refresh Team
 * 
 */
public class LineItem {

	private final Product product;
	private int quantity;
	private int id;
	private double unitPriceAtSale;
	private double varprice;
	private String varname;
	private String custom="custom";


	/**
	 * Static value for UNDEFINED ID.
	 */
	public static final int UNDEFINED = -1;

	/**
	 * Constructs a new LineItem.
	 * @param product product of this LineItem.
	 * @param quantity product quantity of this LineItem.
	 */
	public LineItem(Product product, int quantity,String varname) {
		this(UNDEFINED, product, quantity, product.getUnitPrice(),varname);
	}

	public LineItem(Product product, int quantity, double varprice,String varname) {
		this(UNDEFINED, product, quantity, varprice,varname);
	}
	public LineItem(int quantity, double varprice,String varname) {
		this(UNDEFINED, new Product(UNDEFINED,varname,varname,varprice,varname), quantity, varprice,varname);
	}

	/**
	 * Constructs a new LineItem.
	 * @param id ID of this LineItem, This value should be assigned from database.
	 * @param product product of this LineItem.
	 * @param quantity product quantity of this LineItem.
	 * @param unitPriceAtSale unit price at sale time. default is price from ProductCatalog.
	 */
	public LineItem(int id, Product product, int quantity,
                    double unitPriceAtSale,String varname) {
		this.id = id;
		this.product = product;
		this.quantity = quantity;
		this.unitPriceAtSale = unitPriceAtSale;
		this.varname = varname;
	}

	/**
	 * Returns product in this LineItem.
	 * @return product in this LineItem.
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * Return quantity of product in this LineItem.
	 * @return quantity of product in this LineItem.
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Sets quantity of product in this LineItem.
	 * @param quantity quantity of product in this LineItem.
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Adds quantity of product in this LineItem.
	 * @param amount amount for add in quantity.
	 */
	public void addQuantity(int amount) {
		this.quantity += amount;
	}
	public double getUnitPrice1() {
		return varprice;
	}

	/**
	 * Returns total price of this LineItem.
	 * @return total price of this LineItem.
	 */
	public double getTotalPriceAtSale() {
		return unitPriceAtSale * quantity;
	}
	public int getTotalQuantity() {
		return quantity;
	}
	public double getTotalPriceAtSale1() {
		return varprice * quantity;
	}

	/**
	 * Returns the description of this LineItem in Map format.
	 * @return the description of this LineItem in Map format.
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		if (varname.equals("null")){
			map.put("name", product.getName());
		}else{
			map.put("name", custom);
		}

		map.put("quantity", quantity + "");
		if (getTotalPriceAtSale()==0.0){
			map.put("price", getTotalPriceAtSale1() + "");
		}else{
			map.put("price", getTotalPriceAtSale() + "");
		}

		return map;

	}

	/**
	 * Returns id of this LineItem.
	 * @return id of this LineItem.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets id of this LineItem.
	 * @param id of this LineItem.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets price product of this LineItem.
	 * @param unitPriceAtSale price product of this LineItem.
	 */
	public void setUnitPriceAtSale(double unitPriceAtSale) {
		this.unitPriceAtSale = unitPriceAtSale;
	}

	/**
	 * Returns price product of this LineItem.
	 * @return unitPriceAtSale price product of this LineItem.
	 */
	public Double getPriceAtSale() {
		return unitPriceAtSale;
	}

	/**
	 * Determines whether two objects are equal or not.
	 * @return true if Object is a LineItem with same ID ; otherwise false.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (!(object instanceof LineItem))
			return false;
		LineItem lineItem = (LineItem) object;
		return lineItem.getId() == this.getId();
	}
}
