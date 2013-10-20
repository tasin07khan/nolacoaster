package com.nbeckman.megabudget;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.widget.TextView;

import com.nbeckman.megabudget.adapters.BudgetAdapter;

// This class creates a periodic thread to regularly
// attempt to post any outstanding expenses in the
// database to the spreadsheet task. If tasks are
// posted, it will update the UI as well. This is a
// pretty boring class, but does the work of setting up
// the periodic thread.
public class OutstandingExpensesPoster {
	// 1000ms period
	private static final long kPeriod = 1000L;
	private static final TimeUnit kTimeUnit = TimeUnit.MILLISECONDS;
	
	private final TextView num_expenses_text_view_;
	private final BudgetAdapter budget_adapater_;
	private final ScheduledThreadPoolExecutor executor_ = 
			new ScheduledThreadPoolExecutor(1);
	
	// A reference to the running thread, so it can be stopped.
	private ScheduledFuture<?> future_ = null;
	
	public OutstandingExpensesPoster(TextView num_expenses_text_view,
			BudgetAdapter budget_adapter) {
		this.num_expenses_text_view_ = num_expenses_text_view;
		this.budget_adapater_ = budget_adapter;
	}
	
	public void start() {
		if (future_ != null) {
			future_.cancel(false);
		}
		
		// Schedule a single thread to execute the update()
		// method every kPeriod kTimeUnits.
		ScheduledFuture<?> future = 
				this.executor_.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				update();
			}
		}, 0L, kPeriod, kTimeUnit);
	}
	
	public void stop() {
		if (future_ != null) {
			future_.cancel(false);
		}
	}
	
	private void update() {
		if (budget_adapater_.PostOneExpense()) {
			final long num_expenses = 
				budget_adapater_.NumOutstandingExpenses();
			num_expenses_text_view_.post(new Runnable(){
				@Override
				public void run() {
					num_expenses_text_view_.setText(" " + Long.toString(num_expenses));
				}
			});
		}
	}
}
