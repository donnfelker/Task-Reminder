
package com.dummies.android.taskreminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class ReminderEditActivity extends Activity {

	// 
	// Dialog Constants
	//
	private static final int DATE_PICKER_DIALOG = 0;
	private static final int TIME_PICKER_DIALOG = 1;
	
	// 
	// Date Format 
	//
	private static final String DATE_FORMAT = "yyyy-MM-dd"; 
	private static final String TIME_FORMAT = "kk:mm";
	private static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT; 
	
	private EditText mTitleText;
    private EditText mBodyText;
    private Button mDateButton;
    private Button mTimeButton;
    private Long mRowId;
    private RemindersDbAdapter mDbHelper;
    private Calendar mCalendar;  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCalendar = Calendar.getInstance(); 
        mDbHelper = new RemindersDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.note_edit);
        
        
       
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mDateButton = (Button) findViewById(R.id.reminder_date);
        mTimeButton = (Button) findViewById(R.id.reminder_time);
      
        Button confirmButton = (Button) findViewById(R.id.confirm);
       
        mRowId = savedInstanceState != null ? savedInstanceState.getLong(RemindersDbAdapter.KEY_ROWID) 
                							: null;
		setRowIdFromIntent();

		populateFields();
		
        confirmButton.setOnClickListener(new View.OnClickListener() {

        	public void onClick(View view) {
        		saveState(); 
        		setResult(RESULT_OK);
        	    Toast.makeText(ReminderEditActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
        	    finish(); 
        	}
          
        });
        
        
        registerButtonListenersAndSetDefaultText();
    }

	private void setRowIdFromIntent() {
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(RemindersDbAdapter.KEY_ROWID) 
									: null;
			
		}
	}
    
    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close(); 
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper.open(); 
    	setRowIdFromIntent();
		populateFields();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    		case DATE_PICKER_DIALOG: 
    			return showDatePicker();
    		case TIME_PICKER_DIALOG: 
    			return showTimePicker(); 
    	}
    	return super.onCreateDialog(id);
    }
    
    private TimePickerDialog showTimePicker() {
		
    	TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mCalendar.set(Calendar.MINUTE, minute); 
				updateTimeButtonText(); 
			}
		}, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true); 
		
    	return timePicker; 
	}

	private DatePickerDialog showDatePicker() {
		
		
		DatePickerDialog datePicker = new DatePickerDialog(ReminderEditActivity.this, new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mCalendar.set(Calendar.YEAR, year);
				mCalendar.set(Calendar.MONTH, monthOfYear);
				mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateDateButtonText(); 
			}
		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)); 
		return datePicker; 
	}

	private void registerButtonListenersAndSetDefaultText() {

		mDateButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DATE_PICKER_DIALOG);  
			}
		}); 
		
		
		mTimeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(TIME_PICKER_DIALOG); 
			}
		}); 
	}

    
    private void populateFields()  {
    	
    	// Get a calendar object which starts with todays date.
    	
    	
    	// Only populate the text boxes and change the calendar date
    	// if the row is not null from the database. 
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchReminder(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
    	            note.getColumnIndexOrThrow(RemindersDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(RemindersDbAdapter.KEY_BODY)));
            

            // Get the date from the database and format it for our use. 
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            Date date = null;
			try {
				String dateString = note.getString(note.getColumnIndexOrThrow(RemindersDbAdapter.KEY_DATE_TIME)); 
				date = dateTimeFormat.parse(dateString);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} 
            
            // Tell the calendar what date we are now going to work with. 
            mCalendar.setTime(date); 

        } 
        
        updateDateButtonText(); 
        updateTimeButtonText(); 
        	
    }

	private void updateTimeButtonText() {
		// Set the time button text based upon the value from the database
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT); 
        String timeForButton = timeFormat.format(mCalendar.getTime()); 
        mTimeButton.setText(timeForButton);
	}

	private void updateDateButtonText() {
		// Set the date button text based upon the value from the database 
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT); 
        String dateForButton = dateFormat.format(mCalendar.getTime()); 
        mDateButton.setText(dateForButton);
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(RemindersDbAdapter.KEY_ROWID, mRowId);
    }
    

    
    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT); 
    	String reminderDateTime = dateTimeFormat.format(mCalendar.getTime());
        
        if (mRowId == null) {
        	
        	long id = mDbHelper.createReminder(title, body, reminderDateTime);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateReminder(mRowId, title, body, reminderDateTime);
        }
       
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, OnAlarmReceiver.class);
        i.putExtra(RemindersDbAdapter.KEY_ROWID, (long)mRowId); 

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_ONE_SHOT); 
        
        alarmManager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pi);
        
        /*		
        
        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i=new Intent(context, OnAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0,i, 0);
		
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+30000,PERIOD,pi);
		
		*/
        
    }
    
}
