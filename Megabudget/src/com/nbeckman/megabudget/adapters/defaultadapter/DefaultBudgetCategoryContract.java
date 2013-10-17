package com.nbeckman.megabudget.adapters.defaultadapter;

import android.provider.BaseColumns;

// A 'contract' for the SQLLite database table that holds
// budget categories. The contact is just a convenient way
// of specifying the schema for a table.
//
// The idea for this comes from the Android dev docs:
// https://developer.android.com/training/basics/data-storage/databases.html
public final class DefaultBudgetCategoryContract {
	private DefaultBudgetCategoryContract() {}
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	
	// How to create this table.
	public static final String SQL_CREATE_TABLE =
	    "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
	    CategoryEntry._ID + " INTEGER PRIMARY KEY," +
	    CategoryEntry.COLUMN_NAME_FEED_URL_ID + TEXT_TYPE + COMMA_SEP +
	    CategoryEntry.COLUMN_NAME_CATEGORY_NAME + TEXT_TYPE + COMMA_SEP +
	    CategoryEntry.COLUMN_NAME_ROW + INTEGER_TYPE + COMMA_SEP +
	    " )";
	
	// How to delete this table
	static final String SQL_DELETE_TABLE =
		"DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME;
	
	// Inner class that defines the table contents.
    public static abstract class CategoryEntry implements BaseColumns {
    	public static final String TABLE_NAME = "budget_categories";
    	
    	// Each constant starting with COLUMN_NAME_ is a new column.
    	
    	// I am using the Cell Feed URL as the ID, because it is unique, and I am pretty sure, constant
    	// across time. (This is the URL of the cell that contains the category name.)
    	public static final String COLUMN_NAME_FEED_URL_ID = "feed_url";
    	// The name of this category.
    	public static final String COLUMN_NAME_CATEGORY_NAME = "category_name";
    	// The corresponding row in the spreadsheet.
    	public static final String COLUMN_NAME_ROW = "row";
    }
}