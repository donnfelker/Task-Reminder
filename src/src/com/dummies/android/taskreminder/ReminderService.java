package com.dummies.android.taskreminder;

import android.content.Intent;
import android.util.Log;

public class ReminderService extends WakeReminderIntentService {

	public ReminderService() {
		super("ReminderService");
			}

	@Override
	void doReminderWork(Intent intent) {
		Log.d("ReminderService", "Doing work.");
		
		// In here, set a notificatoin bar with an icon 
		// that when the user clicks on the notificaiton bar
		// it opens the task at hand. 

	}

}
