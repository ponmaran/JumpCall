package com.ponmaran.JumpCall;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by PonNi on 2/20/2017.
 */

class CallLogObserver extends ContentObserver {
    private static final String TAG_LOG_OBSERVER = "CallLogObserver";

    private ContentResolver contentResolver;
    private Date callBroadcastTime;
    private String bridgeNumber;
    private String originalNumber;

    CallLogObserver(Handler h) {
        // TODO Auto-generated constructor stub
        super(h);
    }

    void setContentResolver(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    void setCallBroadcastTime(Date callBroadcastTime) {
        this.callBroadcastTime = callBroadcastTime;
    }

    void setBridgeNumber(String bridgeNumber) {
        this.bridgeNumber = bridgeNumber;
    }

    void setOriginalNumber(String originalNumber) {
        this.originalNumber = originalNumber;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG_LOG_OBSERVER, " onChange " + this.toString() + " for " + originalNumber);

        String[] fields = {
                CallLog.Calls._ID,
                android.provider.CallLog.Calls.NUMBER,
                android.provider.CallLog.Calls.DURATION,
                android.provider.CallLog.Calls.DATE
        };

        String[] selectionArgs = {String.valueOf(callBroadcastTime.getTime()),String.valueOf(callBroadcastTime.getTime() - 5000)};

        Cursor c = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
//                fields,
                null,   //all columns
                CallLog.Calls.DATE + " < ? and " + CallLog.Calls.DATE + " > ?",
                selectionArgs,
                android.provider.CallLog.Calls.DATE + " DESC"
        );

        assert c != null;
        if(c.getCount() > 0){
            contentResolver.unregisterContentObserver(this);

            String tableHead = "";
            String [] columns = c.getColumnNames();
            for (String column : columns) tableHead = tableHead + "\t" + column;
            Log.d(TAG_LOG_OBSERVER, tableHead);

            if(c.moveToFirst()){
                do{
                    String dataRow = "";
                    for (int i=0;i<columns.length;i++) dataRow = dataRow + "\t" + c.getString(i);
                    Log.d(TAG_LOG_OBSERVER, dataRow);
                }while(c.moveToNext());
            }

            if(c.moveToFirst()){
                do{
                    String curLogNum = c.getString(c.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                    if (curLogNum.equals(bridgeNumber)){
                        ContentValues valueSet = new ContentValues();
                        valueSet.put(android.provider.CallLog.Calls.NUMBER, originalNumber);

                        String curId = c.getString(c.getColumnIndex(CallLog.Calls._ID));
                        String[] selectionArgsUpdate = {curId};

                        contentResolver.update(
    	    	    			CallLog.Calls.CONTENT_URI,
                                valueSet,
                                CallLog.Calls._ID + " = ?",
                                selectionArgsUpdate);

                        Long callDate = c.getLong(c.getColumnIndex(android.provider.CallLog.Calls.DATE));
                        Log.d(TAG_LOG_OBSERVER, "Broadcast Received: " + new Timestamp(callBroadcastTime.getTime()).toString()
                                + " Log Time: " + new Timestamp(callDate).toString());
                    }
                }
                while (c.moveToNext());
            }
            c.close();
        }
    }
}
