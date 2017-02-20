package com.ponmaran.JumpCall;

import java.util.Date;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CallLogWatchService extends Service {

    CallLogWatch callLogWatch = new CallLogWatch(new Handler());
	public CallLogWatchService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		callLogWatch.getStartId(startId);
	    return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		Toast.makeText(getApplicationContext(), "Thanks for using JumpCall", Toast.LENGTH_LONG).show();
//		System.out.println("Ending service");
	}

	@Override
	public void onCreate() {
		callLogWatch.getCallingInstReference(this);
//		System.out.println("Content observer started");
        getContentResolver().registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, callLogWatch);
	}
	
    private class CallLogWatch extends ContentObserver{
    	CallLogWatchService servInstLocal;
    	int startIdLocal;
    	public CallLogWatch(Handler h) {
		// TODO Auto-generated constructor stub
    		super(h);
    	}

    	private void getCallingInstReference(CallLogWatchService servInstPassed){
//    		System.out.println("Assigned to local reference");
    		servInstLocal = servInstPassed;
    	}
    	
    	private void getStartId(int startIdPassed){
    		startIdLocal = startIdPassed;
    	}

    	@Override
    	 public void onChange(boolean selfChange, Uri uri) {
//    	    Handle change.
//			System.out.println("Call log update detected:: selfChange: " + selfChange + "\tUri: " + uri.toString() + " Date: " + new Date().getTime());

//Block from EndCallListener

//    		System.out.println("Call log change detected");
			if(((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getCallState() == TelephonyManager.CALL_STATE_IDLE){
				ContentResolver contentResolver = getContentResolver();
				contentResolver.unregisterContentObserver(this);
//				System.out.println("Unregister Call Log observer");
				servInstLocal.stopSelfResult(startIdLocal);
//				System.out.println("End CallLogWatchService");
				Date dte = new Date();
				String curLogNum = new String();

	    		String[] fields = {
			    android.provider.CallLog.Calls.NUMBER,
			    android.provider.CallLog.Calls.DURATION,
			    android.provider.CallLog.Calls.DATE
			    };

	    		Cursor c = contentResolver.query(
	    				Uri.parse("content://call_log/calls"),
//	    				android.provider.CallLog.Calls.CONTENT_URI,
	    				fields,
	        		    android.provider.CallLog.Calls.DATE + " > " + (dte.getTime() - 18000000),
	        		    null,
	            		android.provider.CallLog.Calls.DATE + " DESC" 
	        		    );

/*	    		System.out.println("Column names: ");
        		String [] s = c.getColumnNames();
        		for(int i=0;i<s.length;i++){
	        		System.out.println(s[i]);
        		}

	    		if(c.moveToFirst()){
	        		do{
	        			System.out.println(c.getString(0) + "\t" + c.getString(1) + "\t" + c.getString(2));
	        		}while(c.moveToNext());
	    		}
*/
	    		if(c.moveToFirst()){

	    			String[] callStack = MainActivity.BRIDGE_PAIRS.split("~");

	        		int callStackIdx =callStack.length - 1; 

	        		do{
	        			curLogNum = c.getString(c.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
	        			Long callDate = c.getLong(c.getColumnIndex(android.provider.CallLog.Calls.DATE));
	        			Long callDuration = c.getLong(c.getColumnIndex(android.provider.CallLog.Calls.DURATION));
//	    				System.out.println("Current log # : " + curLogNum);

	        			if (curLogNum.equals(callStack[callStackIdx])){
//	        				System.out.println("Bridge # from stack: " + callStack[callStackIdx]);

	        				ContentValues valueSet = new ContentValues();
	    	    	    	valueSet.put(android.provider.CallLog.Calls.NUMBER, callStack[callStackIdx - 1]);

//	    	    	    	System.out.println("Writing: " + callStack[callStackIdx - 1]);

//	    	    	    	int numRowsUpdt = contentResolver.update(
			    	    	contentResolver.update(
			    	    			Uri.parse("content://call_log/calls"),
//	    	    	    			android.provider.CallLog.Calls.CONTENT_URI, 
	    	    	    			valueSet, 
	    	    	    			android.provider.CallLog.Calls.NUMBER + "=" + curLogNum
	    	    	    			+ " and " + android.provider.CallLog.Calls.DATE + "=" + callDate
	    	    	    			+ " and " + android.provider.CallLog.Calls.DURATION + "=" + callDuration 
	    	    	    			,null);

//	    	    	    	System.out.println("Called # from stack: " + callStack[callStackIdx]);
//		        			System.out.println("Rows updated: " + numRowsUpdt);
		        			callStackIdx = callStackIdx - 2;
	        			}
//	    				System.out.println("Cursor @ " + curLogNum);
	    			}
	    			while (c.moveToNext() && callStackIdx > 0);
	    			MainActivity.BRIDGE_PAIRS = new String();
	    		}
	    		c.close();
			}
			
//			Toast.makeText(getApplicationContext(), "Hola", Toast.LENGTH_SHORT).show();
//			getContentResolver().unregisterContentObserver();
    	 }
    }
}
