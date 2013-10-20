package com.nbeckman.megabudget.adapters.defaultadapter;

import com.nbeckman.megabudget.adapters.defaultadapter.DefaultBudgetCategoryContract.CategoryEntry;

import android.provider.BaseColumns;

// A 'contract' for the SQLLite database table that holds
// pending expenses. All expenses entered by the user go into
// this table until such time as they can be written back to the
// spreadsheet. The contract is just a convenient way
// of specifying the schema for a table.
//
// The idea for this comes from the Android dev docs:
// https://developer.android.com/training/basics/data-storage/databases.html
public final class DefaultPendingExpensesContract {
	private DefaultPendingExpensesContract() {}
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String REAL_TYPE = " REAL";
	private static final String COMMA_SEP = ",";
	
	// How to create this table.
	public static final String SQL_CREATE_TABLE =
	    "CREATE TABLE " + ExpenseEntry.TABLE_NAME + " (" +
	    ExpenseEntry._ID + " INTEGER PRIMARY KEY," +
	    ExpenseEntry.COLUMN_NAME_EXPENSE_AMOUNT + REAL_TYPE + COMMA_SEP +
	    ExpenseEntry.COLUMN_NAME_MONTH_FEED_URL + TEXT_TYPE + COMMA_SEP +
	    ExpenseEntry.COLUMN_NAME_MONTH_NAME + TEXT_TYPE + COMMA_SEP +
	    ExpenseEntry.COLUMN_NAME_CATEGORY_FEED_URL + TEXT_TYPE + COMMA_SEP +
	    ExpenseEntry.COLUMN_NAME_CATEGORY_NAME + TEXT_TYPE + COMMA_SEP +
	    ExpenseEntry.COLUMN_NAME_DATE_ADDED + INTEGER_TYPE +
	    ");";
	
	// How to delete this table
	static final String SQL_DELETE_TABLE =
		"DROP TABLE IF EXISTS " + ExpenseEntry.TABLE_NAME;
	
	// Inner class that defines the table contents.
    public static abstract class ExpenseEntry implements BaseColumns {
		public static final String TABLE_NAME = "outstanding_expenses";
    	
    	// Each constant starting with COLUMN_NAME_ is a new column.
    	
    	// A 'real' value holding the expense amount. 
    	public static final String COLUMN_NAME_EXPENSE_AMOUNT = "expense_amount";
    	// The URL of the cell feed of the expense's month.
    	public static final String COLUMN_NAME_MONTH_FEED_URL = "month_feed_url";
    	// The name of this month. This can be used to verify that the meaning of this
    	// cell hasn't changed since the user added the expense.
    	public static final String COLUMN_NAME_MONTH_NAME = "month_name";
    	// The URL of the cell feed of the expense's category.
    	public static final String COLUMN_NAME_CATEGORY_FEED_URL = "category_feed_url";
    	// The name of this category. This can be used to verify that the meaning of this
    	// cell hasn't changed since the user added the expense.
    	public static final String COLUMN_NAME_CATEGORY_NAME = "category_name";
    	// The time (unix epoch) that this expense was added. Also used as an ID...
    	// Perhaps not a good idea.
    	public static final String COLUMN_NAME_DATE_ADDED = "date_added";

    }
}