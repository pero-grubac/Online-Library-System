package org.unibl.etf.mdp.model;

public enum BookSupplier {
	BOOKSUPPLIER_A("BookSupplier_A"), BOOKSUPPLIER_B("BookSupplier_B"), BOOKSUPPLIER_C("BookSupplier_C");

	private final String name;

	BookSupplier(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
