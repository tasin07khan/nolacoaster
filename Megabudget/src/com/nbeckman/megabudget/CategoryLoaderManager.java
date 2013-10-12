package com.nbeckman.megabudget;

import java.util.List;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.nbeckman.megabudget.adapters.BudgetAdapter;
import com.nbeckman.megabudget.adapters.BudgetCategory;

// This loader manager and its related classes are responsible for loading
// the budget's categories from the spreadsheet, so they can be displayed in a
// spinner.
// TODO(nbeckman): This is REALLY similar to MonthLoaderManager. Is there anything
// we can do here?
public class CategoryLoaderManager implements LoaderCallbacks<List<BudgetCategory>> {

	private final Context context_;
	private final BudgetAdapter budget_adapter_;
	private final ArrayAdapter<BudgetCategory> spinner_adapter_;
	
	public CategoryLoaderManager(Context context, 
			BudgetAdapter budget_adapter, 
			ArrayAdapter<BudgetCategory> spinner_adapter) {
		this.context_ = context;
		this.budget_adapter_ = budget_adapter;
		this.spinner_adapter_ = spinner_adapter;
	}
	
	@Override
	public Loader<List<BudgetCategory>> onCreateLoader(int arg0, Bundle arg1) {
		return new CategoryLoader(context_, budget_adapter_);
	}

	@Override
	public void onLoadFinished(Loader<List<BudgetCategory>> arg0,
			List<BudgetCategory> arg1) {
		spinner_adapter_.clear();
		spinner_adapter_.addAll(arg1);
	}

	@Override
	public void onLoaderReset(Loader<List<BudgetCategory>> arg0) {
		spinner_adapter_.clear();
	}

}

class CategoryLoader extends AsyncTaskLoader<List<BudgetCategory>> {

	private final BudgetAdapter budget_adapter_;
	
	public CategoryLoader(Context context, BudgetAdapter budget_adapter) {
		super(context);
		this.budget_adapter_ = budget_adapter;
	}

	@Override
	public List<BudgetCategory> loadInBackground() {
		return this.budget_adapter_.getCategories();
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}
}