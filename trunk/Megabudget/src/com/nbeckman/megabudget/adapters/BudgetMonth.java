package com.nbeckman.megabudget.adapters;

// An interface to be implemented by budget months.
// A month is something obvious, like January. But
// since other people might keep yearly or daily
// budgets (or something else?) consider a BudgetMonth
// to really be just some unit of time corresponding to
// a column (or row) in a budget spreadsheet.
public interface BudgetMonth {
	// Returns the name of this month, suitable for display.
	public String getName();
}
