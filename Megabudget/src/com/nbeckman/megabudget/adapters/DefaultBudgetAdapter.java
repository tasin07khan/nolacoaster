package com.nbeckman.megabudget.adapters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

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
	private static final String kCellInputValue = "Total";
	
	class DefaultBudgetMonth implements BudgetMonth {
		final String name;
		final int column;
		
		public DefaultBudgetMonth(String name, int column) {
			this.name = name;
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
		final int row;
		
		public DefaultBudgetCategory(String name, int row) {
			this.name = name;
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
	
	private final WorksheetEntry worksheetFeed;
	private final SpreadsheetService spreadsheetService;
	
	public DefaultBudgetAdapter(
			WorksheetEntry feed, SpreadsheetService spreadsheetService) {
		this.worksheetFeed = feed;
		this.spreadsheetService = spreadsheetService;
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
			result.add(new DefaultBudgetMonth(
					cell.getCell().getValue(), cell.getCell().getCol()));
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
				if (kCellInputValue.equals(cell.getCell().getInputValue()) &&
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
		// TODO Auto-generated method stub
		return null;
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

}
