package com.nbeckman.megabudget;

import java.util.List;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.nbeckman.megabudget.adapters.BudgetAdapter;
import com.nbeckman.megabudget.adapters.BudgetMonth;

// This loader manager and its related classes are responsible for loading
// the budget's months from the spreadsheet, so they can be displayed in a
// spinner.
public class MonthLoaderManager implements LoaderCallbacks<List<BudgetMonth>> {

	private final Context context_;
	private final BudgetAdapter budget_adapter_;
	private final ArrayAdapter<BudgetMonth> spinner_adapter_;
	
	public MonthLoaderManager(Context context, 
			BudgetAdapter budget_adapter, 
			ArrayAdapter<BudgetMonth> spinner_adapter) {
		this.context_ = context;
		this.budget_adapter_ = budget_adapter;
		this.spinner_adapter_ = spinner_adapter;
	}
	
	@Override
	public Loader<List<BudgetMonth>> onCreateLoader(int arg0, Bundle arg1) {
		return new MonthLoader(context_, budget_adapter_);
	}

	@Override
	public void onLoadFinished(Loader<List<BudgetMonth>> arg0,
			List<BudgetMonth> arg1) {
		spinner_adapter_.clear();
		spinner_adapter_.addAll(arg1);
	}

	@Override
	public void onLoaderReset(Loader<List<BudgetMonth>> arg0) {
		spinner_adapter_.clear();
	}

}

class MonthLoader extends AsyncTaskLoader<List<BudgetMonth>> {

	private final BudgetAdapter budget_adapter_;
	
	public MonthLoader(Context context, BudgetAdapter budget_adapter) {
		super(context);
		this.budget_adapter_ = budget_adapter;
	}

	@Override
	public List<BudgetMonth> loadInBackground() {
		return this.budget_adapter_.getMonths();
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}
}