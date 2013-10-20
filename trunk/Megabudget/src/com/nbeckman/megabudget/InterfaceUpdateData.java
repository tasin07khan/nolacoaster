package com.nbeckman.megabudget;

// A number of pieces of information need to be queried in the
// background before being updated in the interface in the foreground.
// This is a value class for holding those pieces of data.
// If a field is null, that should be interpreted as, no update.
public final class InterfaceUpdateData {
	// The number of expenses waiting to be posted to the spreadsheet.
	final Long numOutstandingExpenses;

	public InterfaceUpdateData(Long numOutstandingExpenses) {
		this.numOutstandingExpenses = numOutstandingExpenses;
	}

	public Long getNumOutstandingExpenses() {
		return numOutstandingExpenses;
	}
}
