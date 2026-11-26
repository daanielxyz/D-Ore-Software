package com.dore.assistantprototype.inventory.domain;

public class Product {

	private final Long id;
	private String name;
	private String category;
	private String unit;
	private String sku;
	private int stock;
	private int minStock;
	private int cost;
	private int price;
	private String supplier;
	private boolean active;

	public Product(
			Long id,
			String name,
			String category,
			String unit,
			String sku,
			int stock,
			int minStock,
			int cost,
			int price,
			String supplier,
			boolean active) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.unit = unit;
		this.sku = sku;
		this.stock = stock;
		this.minStock = minStock;
		this.cost = cost;
		this.price = price;
		this.supplier = supplier;
		this.active = active;
	}

	public Product copy() {
		return new Product(id, name, category, unit, sku, stock, minStock, cost, price, supplier, active);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getMinStock() {
		return minStock;
	}

	public void setMinStock(int minStock) {
		this.minStock = minStock;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
