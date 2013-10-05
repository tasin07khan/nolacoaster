package com.nbeckman.megabudget.adapters;

// An interface to be implemented by budget categories.
// A budget category is something like "rent" or "entertainment".
// Publicly, a budget category just needs to provide a name
// suitable for display. However, it is also used as an index
// into a BudgetAdapter.
public interface BudgetCategory {
	// Returns the name of this category, suitable for display.
	public String getName();
}
