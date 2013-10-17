package com.nbeckman.megabudget.adapters.defaultadapter;

import android.provider.BaseColumns;

// A 'contract' for the SQLLite database table that holds
// budget months. The contract is just a convenient way
// of specifying the schema for a table.
//
// The idea for this comes from the Android dev docs:
// https://developer.android.com/training/basics/data-storage/databases.html
public final class DefaultBudgetMonthContract {
	private DefaultBudgetMonthContract() {}
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	
	// How to create this table.
	public static final String SQL_CREATE_TABLE =
	    "CREATE TABLE " + MonthEntry.TABLE_NAME + " (" +
	    MonthEntry._ID + " INTEGER PRIMARY KEY," +
	    MonthEntry.COLUMN_NAME_FEED_URL_ID + TEXT_TYPE + COMMA_SEP +
	    MonthEntry.COLUMN_NAME_MONTH_NAME + TEXT_TYPE + COMMA_SEP +
	    MonthEntry.COLUMN_NAME_COL + INTEGER_TYPE + COMMA_SEP +
	    " )";
	
	// How to delete this table
	static final String SQL_DELETE_TABLE =
		"DROP TABLE IF EXISTS " + MonthEntry.TABLE_NAME;
	
	// Inner class that defines the table contents.
    public static abstract class MonthEntry implements BaseColumns {
    	public static final String TABLE_NAME = "budget_months";
    	
    	// Each constant starting with COLUMN_NAME_ is a new column.
    	
    	// I am using the Cell Feed URL as the ID, because it is unique, and I am pretty sure, constant
    	// across time. (This is the URL of the cell that contains the category name.)
    	public static final String COLUMN_NAME_FEED_URL_ID = "feed_url";
    	// The name of this month.
    	public static final String COLUMN_NAME_MONTH_NAME = "month_name";
    	// The corresponding col in the spreadsheet.
    	public static final String COLUMN_NAME_COL = "col";
    }
}