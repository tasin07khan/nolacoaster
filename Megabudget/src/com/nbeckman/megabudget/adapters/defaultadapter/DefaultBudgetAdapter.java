package com.nbeckman.megabudget.adapters.defaultadapter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;
import com.nbeckman.megabudget.SpreadsheetUtils;
import com.nbeckman.megabudget.adapters.BudgetAdapter;
import com.nbeckman.megabudget.adapters.BudgetCategory;
import com.nbeckman.megabudget.adapters.BudgetMonth;
import com.nbeckman.megabudget.adapters.defaultadapter.DefaultPendingExpensesContract.ExpenseEntry;

// The default budget category is an adapter for the budget
// format I use for my own personal budget. It encodes the following
// layout:
// Categories - Categories are rows. Their names appear in the first column,
//              but some categories have subcategories whose names are in the
//              third column. (Concatenate these two for the category name.)
//              Categories stop upon reaching "Total", which is always the
//              final category.
// Months - Months are columns, starting at column G.
// Total - The total for each month is in a category called "Total".
//
// TODO(nbeckman): I am almost certainly doing WAY too many lookups here,
// so let's consider just grabbing the entire worksheet and then doing everything
// else on the resulting feed.
public class DefaultBudgetAdapter implements BudgetAdapter {
	// The first row that could contain a category.
	private static final int kCategoryStartRow = 2;
	// The start and end columns for the categories
	private static final int kCategoryStartCol = 1;
	private static final int kCategoryEndCol = 3;
	
	// The first column that could contain a month.
	private static final int kMonthStartCol = 7;  // 7 = G
	// The row containing the months.
	private static final int kMonthRow = 1;
	
	// The string name that will appear in the Total category.
	private static final String kTotalCellInputValue = "Total";
	
	class DefaultBudgetMonth implements BudgetMonth {
		final String name;
		final String cell_feed_url;
		final int column;
		
		public DefaultBudgetMonth(String name, String cell_feed_url, int column) {
			this.name = name;
			this.cell_feed_url = cell_feed_url;
			this.column = column;
		}

		@Override
		public String getName() {
			return name;
		}
		
		@Override 
		public String toString() {
			return getName();
		}
	}
	
	class DefaultBudgetCategory implements BudgetCategory {
		final String name;
		final String cell_feed_url;
		final int row;
		
		public DefaultBudgetCategory(String name, String cell_feed_url, int row) {
			this.name = name;
			this.cell_feed_url = cell_feed_url;
			this.row = row;
		}

		@Override
		public String getName() {
			return name;
		}
		
		@Override 
		public String toString() {
			return getName();
		}
	}
	
	private final DefaultAdapterDbHelper dbHelper;
	private final WorksheetEntry worksheetFeed;
	private final SpreadsheetService spreadsheetService;
	
	public DefaultBudgetAdapter(
			Context context,
			WorksheetEntry feed, 
			SpreadsheetService spreadsheetService) {
		this.dbHelper = new DefaultAdapterDbHelper(context);
		this.worksheetFeed = feed;
		this.spreadsheetService = spreadsheetService;
		
//		// TODO
//		final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
//		db.delete(ExpenseEntry.TABLE_NAME, "1", null);
	}
	
	private List<CellEntry> cachedMonthCells = null;
	private List<CellEntry> cachedMonthCells() {
		if (cachedMonthCells == null) {
			try {
				// Create a URL to grab all the months; every column in row
				// kMonthRow starting after column kMonthStartCol.
				URL cellFeedUrl = new URI(worksheetFeed.getCellFeedUrl().toString()
						+ "?min-row="
						+ Integer.toString(kMonthRow)
						+ "&max-row="
						+ Integer.toString(kMonthRow)
						+ "&min-col="
						+ Integer.toString(kMonthStartCol)).toURL();
				CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
				cachedMonthCells = cellFeed.getEntries();
				return cachedMonthCells;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new ArrayList<CellEntry>(0);
		}
		return cachedMonthCells;
	}
	
	@Override
	public List<BudgetMonth> getMonths() {
		ArrayList<BudgetMonth> result = new ArrayList<BudgetMonth>(cachedMonthCells().size());
		for (CellEntry cell : cachedMonthCells()) {
			final String cell_url =
				SpreadsheetUtils.cellUrl(worksheetFeed.getCellFeedUrl(), cell).toString();
			result.add(new DefaultBudgetMonth(
					cell.getCell().getValue(),
					cell_url,
					cell.getCell().getCol()));
		}
		return result;
	}

	// A cached list of CellEntries that correspond to
	// categories. null until we have loaded them for the
	// first time.
	private List<CellEntry> cachedCategoryCells = null;
	private List<CellEntry> cachedCategoryCells() {
		if (cachedCategoryCells == null) {
			// For cells, things are a little different. We get three columns,
			// kCategoryStartCol to kCategoryEndRow, and all rows starting 
			// at kCategoryStartRow.
			try {
				URL cellFeedUrl = new URI(worksheetFeed.getCellFeedUrl().toString()
						+ "?min-row="
						+ Integer.toString(kCategoryStartRow)
						+ "&min-col="
						+ Integer.toString(kCategoryStartCol)
						+ "&max-col="
						+ Integer.toString(kCategoryEndCol)).toURL();
				CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
				cachedCategoryCells = cellFeed.getEntries();
				// TODO(nbeckman): Ugly control flow!
				return cachedCategoryCells;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cachedCategoryCells = new ArrayList<CellEntry>(0);
		}
		return cachedCategoryCells;
	}
	
	// The cached total category cell. (This is the cell that
	// says "total" not a specific month's total.
	private CellEntry cachedTotalCategoryCell = null;
	private CellEntry cachedTotalCategoryCell() {
		if (cachedTotalCategoryCell == null) {
			List<CellEntry> cells = cachedCategoryCells();
			for (CellEntry cell : cells) {
				if (kTotalCellInputValue.equals(cell.getCell().getInputValue()) &&
						kCategoryStartCol == cell.getCell().getCol()) {
					cachedTotalCategoryCell = cell;
					return cachedTotalCategoryCell;
				}
			}
			return null;
		}
		return cachedTotalCategoryCell;
	}
	
	@Override
	public List<BudgetCategory> getCategories() {
		final ArrayList<BudgetCategory> result = 
				new ArrayList<BudgetCategory>(cachedCategoryCells().size());
		
		// Go through all of the cells. Look at the row:
		// 1 - A major category, create a category and store the name as last major.
		// 2 - Ignore.
		// 3 - A minor category, concatenate name with last major.
		String last_major_category = "";
		for (CellEntry cell : cachedCategoryCells()) {
			final int col = cell.getCell().getCol();
			String category_name;
			final String cell_value = cell.getCell().getValue();
			if ("".equals(cell_value)) {
				continue;
			} else if (kTotalCellInputValue.equals(cell_value)) {
				// Don't do 'Total' or go any further.
				break;
			} else if (col == kCategoryStartCol) {
				category_name = cell.getCell().getValue();
				last_major_category = category_name;
			} else if (col == kCategoryEndCol) {
				category_name = last_major_category +
						" - " + cell.getCell().getValue();
			} else {
				continue;
			}
			final String cell_url =
					SpreadsheetUtils.cellUrl(worksheetFeed.getCellFeedUrl(), cell).toString();
			result.add(new DefaultBudgetCategory(
					category_name, cell_url, cell.getCell().getRow()));
		}
		return result;
	}

	@Override
	public CellEntry getCell(BudgetMonth m, BudgetCategory c) {
		if (!(m instanceof DefaultBudgetMonth)) {
			return null;
		}
		DefaultBudgetMonth month = (DefaultBudgetMonth)m;
		
		if (!(c instanceof DefaultBudgetCategory)) {
			return null;
		}
		DefaultBudgetCategory category = (DefaultBudgetCategory)c;
		
		try {
			URL cellFeedUrl = new URI(worksheetFeed.getCellFeedUrl().toString()
					+ "?min-row="
					+ Integer.toString(category.row)
					+ "&max-row="
					+ Integer.toString(category.row)
					+ "&min-col="
					+ Integer.toString(month.column)
					+ "&max-col="
					+ Integer.toString(month.column)).toURL();
			CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
			if (cellFeed.getTotalResults() == 1) {
				return cellFeed.getEntries().get(0);
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public CellEntry getMonthTotalCell(BudgetMonth m) {
		if (!(m instanceof DefaultBudgetMonth)) {
			return null;
		}
		DefaultBudgetMonth month = (DefaultBudgetMonth)m;
		
		final CellEntry total_cell = cachedTotalCategoryCell();
		try {
			URL cellFeedUrl = new URI(worksheetFeed.getCellFeedUrl().toString()
					+ "?min-row="
					+ Integer.toString(total_cell.getCell().getRow())
					+ "&max-row="
					+ Integer.toString(total_cell.getCell().getRow())
					+ "&min-col="
					+ Integer.toString(month.column)
					+ "&max-col="
					+ Integer.toString(month.column)).toURL();
			CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
			if (cellFeed.getTotalResults() == 1) {
				return cellFeed.getEntries().get(0);
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private boolean AddValueToSpreadsheet(int row, int col, double amount) {
		try {
			URL cellFeedUrl = new URI(worksheetFeed.getCellFeedUrl().toString()
					+ "?min-row="
					+ Integer.toString(row)
					+ "&max-row="
					+ Integer.toString(row)
					+ "&min-col="
					+ Integer.toString(col)
					+ "&max-col="
					+ Integer.toString(col)
					// The original version of this program did not have this parameter, and
					// would never update empty cells, because getTotalResults would return 0
					// below. So we need this.
					+ "&return-empty=true").toURL();
			CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
			if (cellFeed.getTotalResults() != 1) {
				return false;
			}
			final CellEntry cell = cellFeed.getEntries().get(0);
			// TODO(nbeckman): If cell is not a double, this will fail. FIXME
			// We are proposing to update the input value, so we get the
			// current input value. This basically means that these cells
			// cannot be formulas. I think that's okay.
			final double current_value = 
					"".equals(cell.getCell().getInputValue()) ?
							0.0 :
							cell.getCell().getDoubleValue();
			final double new_value = current_value + amount;
			// TODO(nbeckman): Hardcoded locale. Get from spreadsheet.
			final String new_string = String.format(Locale.FRANCE, "%.2f", new_value);
			cell.changeInputValueLocal(new_string);
			cell.update();
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void AddValue(BudgetMonth month, BudgetCategory category, double amount) {
		if (!(month instanceof DefaultBudgetMonth)) {
			System.err.println("Month was not a DefaultBudgetMonth: " + month.toString());
			return;
		}
		if (!(category instanceof DefaultBudgetCategory)) {
			System.err.println("Category was not a DefaultBudgetCategory: " + category.toString());
			return;
		}
		DefaultBudgetMonth m = (DefaultBudgetMonth)month;
		DefaultBudgetCategory c = (DefaultBudgetCategory)category;
		
		// Create a new database entry for this expense. The DB handler will post it later.
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ExpenseEntry.COLUMN_NAME_CATEGORY_FEED_URL, c.cell_feed_url);
		values.put(ExpenseEntry.COLUMN_NAME_CATEGORY_NAME, c.getName());
		values.put(ExpenseEntry.COLUMN_NAME_DATE_ADDED, System.currentTimeMillis() / 1000L);
		values.put(ExpenseEntry.COLUMN_NAME_EXPENSE_AMOUNT, amount);
		values.put(ExpenseEntry.COLUMN_NAME_MONTH_FEED_URL, m.cell_feed_url);
		values.put(ExpenseEntry.COLUMN_NAME_MONTH_NAME, m.getName());
		db.insert(ExpenseEntry.TABLE_NAME, null, values);
	}

	@Override
	public long NumOutstandingExpenses() {
		final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return DatabaseUtils.queryNumEntries(db, ExpenseEntry.TABLE_NAME);
	}
	
	@Override
	public boolean PostOneExpense() {
		// Get the oldest expense by ID if there is one, and then
		// post it.
		final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		// Projection: Until it matters, just return all the columns.
		final String[] projection = null;
		final String sortOrder =
				ExpenseEntry.COLUMN_NAME_DATE_ADDED + " ASC";
		final Cursor cursor = db.query(
				ExpenseEntry.TABLE_NAME, 
			    projection,                              
			    "",                 
			    new String[0],                        
			    null,                           
			    null,                           
			    sortOrder,             
			    "1");  // Return just the oldest row.
		if (!cursor.moveToFirst() || cursor.getCount() == 0) {
			return false;
		}
		final double amount_to_add =
			cursor.getDouble(
				cursor.getColumnIndexOrThrow(
					ExpenseEntry.COLUMN_NAME_EXPENSE_AMOUNT));
		final String row_url = 
			cursor.getString(
				cursor.getColumnIndexOrThrow(
					ExpenseEntry.COLUMN_NAME_CATEGORY_FEED_URL));
		final String col_url = 
			cursor.getString(
				cursor.getColumnIndexOrThrow(
					ExpenseEntry.COLUMN_NAME_MONTH_FEED_URL));
		try {
			CellFeed row_cell_feed = spreadsheetService.getFeed(new URL(row_url), CellFeed.class);
			if (row_cell_feed.getTotalResults() != 1) {
				return false;
			}
			final CellEntry row_cell = row_cell_feed.getEntries().get(0);
			CellFeed col_cell_feed = spreadsheetService.getFeed(new URL(col_url), CellFeed.class);
			if (col_cell_feed.getTotalResults() != 1) {
				return false;
			}
			final CellEntry col_cell = col_cell_feed.getEntries().get(0);
			if (this.AddValueToSpreadsheet(
					row_cell.getCell().getRow(), 
					col_cell.getCell().getCol(), 
					amount_to_add)) {
				// Now delete that row so we don't process it again.
				// Technically we've got no atomicity here... Hopefully
				// the network operation is much more likely to fail than
				// this.
				final long row_id = 
						cursor.getLong(
							cursor.getColumnIndexOrThrow(
								ExpenseEntry._ID));
				// Define 'where' part of query.
				String selection = ExpenseEntry._ID + " LIKE ?";
				// Specify arguments in placeholder order.
				String[] selectionArgs = { String.valueOf(row_id) };
				// Issue SQL statement.
				db.delete(ExpenseEntry.TABLE_NAME, selection, selectionArgs);
				return true;
			} else {
				return false;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private List<Loader<?>> month_oberservers_ = new ArrayList<Loader<?>>();
	@Override
	public void AddMonthsObserver(Loader<?> observer) {
		month_oberservers_.add(observer);
	}
	@Override
	public void RemoveMonthsObserver(Loader<?> observer) {
		month_oberservers_.remove(observer);
	}
	private void notifyMonthObservers() {
		for (Loader<?> loader : this.month_oberservers_) {
			loader.onContentChanged();
		}
	}
	// TODO: Lots
	// have a start() method that will start a periodic (?) thread
	// it will load from the network
	// when network load is done, call 'detect changes & update'
	// after updates, call notifyMonthObservers()
	
	private void UpdateMonthsTableIfChanged(List<CellEntry> results) {
		boolean changed = false;
		// Store DB entries in map by name. 
		// For each cell, see if that category is already in the DB, and
		// if so compare the other fields. 
		// If not, add it. 
		// If so & other fields are the same do nothing.
		// If so & other fields have changed, update them.
		if (changed) {
			this.notifyMonthObservers();
		}
	}
}
