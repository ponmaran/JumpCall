package com.ponmaran.JumpCall;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    static final String SHARED_PREF_NAME = "BRIDGE_DATA";
    static final String SHARED_PREF_KEY_BRIDGES = "BRIDGE";
    static final String SHARED_PREF_KEY_RECEIVER_STATE = "RECEIVER_STATE";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 7;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 14;

    private SharedPreferences sharedPref;

    private LinearLayout fieldParent;

	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        checkPermission(Manifest.permission.READ_CONTACTS,
                getString(R.string.app_name) + " " + getString(R.string.permissionRequestMessageReadContacts) + "\n\n" + getString(R.string.permissionRequestReadContactsIfDenied),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        checkPermission(Manifest.permission.CALL_PHONE,
                getString(R.string.app_name) + " " + getString(R.string.permissionRequestMessageCallPhone) + "\n\n" + getString(R.string.permissionRequestCallPhoneIfDenied),
                MY_PERMISSIONS_REQUEST_CALL_PHONE);
        setContentView(R.layout.activity_main);

        fieldParent = (LinearLayout) findViewById(R.id.field_parent);

        ImageButton buttonPlus = (ImageButton) findViewById(R.id.buttonAddRow);
        buttonPlus.setOnClickListener(listener);

        Switch aSwitch = (Switch) findViewById(R.id.switchReceiverOnOff);
        aSwitch.setOnClickListener(listener);

        sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        boolean receiverState = getSavedReceiverState();
        setReceiverState(receiverState);
        aSwitch.setChecked(receiverState);

        String[][] set = getSavedBridgeData();
        if(set.length == 0) set = new String[][]{{"",""}};
        for (String[] aSet : set)
            addFieldSet(aSet);
    }

    private void checkPermission(final String permission, String message, final int request){
        Log.d(TAG,"Checking Permission");
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, request);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
//                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, request);
            }
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");
            switch (view.getId()){
                case R.id.buttonAddRow:
                    addFieldSet(new String[]{"",""});
                    break;
                case R.id.buttonDeleteRow:
                    Log.d(TAG, "delete pressed");
                    LinearLayout v = (LinearLayout) view.getParent();
                    fieldParent.removeView(v);
                    break;
                case R.id.switchReceiverOnOff:
                    Switch aSwitch = (Switch) view;
                    boolean checked = aSwitch.isChecked();
                    setReceiverState(checked);
                    saveReceiverState(checked);
                    Toast.makeText(
                            getApplicationContext(),
                            "AltRoute " + (
                                    checked?
                                            "Enabled" :
                                            "Disabled"),
                            Toast.LENGTH_LONG)
                            .show();
            }
        }
    };

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item){
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_save:
                saveBridgeData(captureBridgeDataFromView());
                Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_reset:
                fieldParent.removeAllViews();
                saveBridgeData(new String[][]{{"",""}});
                addFieldSet(new String[]{"",""});
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        String[][] a = captureBridgeDataFromView();
        String[][] b = getSavedBridgeData();

        if(a.length != b.length){
            saveBridgeData(a);
            Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_LONG).show();
        }
        else
            for (int i = 0; i < a.length; i++) {
                if (!Arrays.equals(a[i], b[i])){
                    saveBridgeData(a);
                    Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_LONG).show();
                }
        }
        super.onBackPressed();
    }

    private void setReceiverState(boolean checked) {
        Log.d(TAG, "Receiver ON/OFF");
        ComponentName componentName = new ComponentName(MainActivity.this,CatchOutgoingCall.class);
        getPackageManager()
                .setComponentEnabledSetting(
                        componentName,
                        checked ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
    }

    public String[][] captureBridgeDataFromView() {
        Log.d(TAG, "Read Data from user");
        List<String[]> setList = new ArrayList<String[]>();
        for(int i=0; i < fieldParent.getChildCount();i++){
            View viewBridgeSetHolder = fieldParent.getChildAt(i);
            EditText numBox = (EditText) viewBridgeSetHolder.findViewById(R.id.editTextNumber);
            EditText delBox = (EditText) viewBridgeSetHolder.findViewById(R.id.editTextDelay);

            String[] lineData = new String[2];
            lineData[0] = numBox.getText().toString();
            if (lineData[0].length() > 0) {
                lineData[1] = delBox.getText().toString();
                setList.add(i, lineData);
            }
        }

        String[][] setArray = new String[setList.size()][2];
        for(int i=0; i<setList.size();i++)
            setArray[i] = setList.get(i);

        return setArray;
    }

    private void addFieldSet(String[] aSet){
        Log.d(TAG, "Insert a line");
        LinearLayout fieldSet = (LinearLayout) LayoutInflater.from(fieldParent.getContext()).inflate(R.layout.number_delay_set,fieldParent,false);
        LinearLayout.LayoutParams setParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setParams.weight = 10f;
        setParams.setLayoutDirection(LinearLayout.HORIZONTAL);
        fieldParent.addView(fieldSet,fieldParent.getChildCount(),setParams);

        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
        EditText numberBox = (EditText) fieldSet.findViewById(R.id.editTextNumber);
        LinearLayout.LayoutParams numberParams = new LinearLayout.LayoutParams((displayWidth * 6)/10, LinearLayout.LayoutParams.WRAP_CONTENT);
        numberBox.setLayoutParams(numberParams);

        EditText delayBox = (EditText) fieldSet.findViewById(R.id.editTextDelay);
        LinearLayout.LayoutParams delayParams = new LinearLayout.LayoutParams((displayWidth * 3)/10, LinearLayout.LayoutParams.WRAP_CONTENT);
        delayBox.setLayoutParams(delayParams);

        ImageButton buttonDeleteSet = (ImageButton) fieldSet.findViewById(R.id.buttonDeleteRow);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonDeleteSet.setLayoutParams(buttonParams);
        buttonDeleteSet.setOnClickListener(listener);

        numberBox.setText(aSet[0]);
        delayBox.setText(aSet[1]);
    }

    private boolean getSavedReceiverState() {
        Log.d(TAG,"Read Receiver state");
        return sharedPref.getBoolean(SHARED_PREF_KEY_RECEIVER_STATE, false);
    }

    private String[][] getSavedBridgeData(){
        Log.d(TAG, "Read saved data");
        String a = sharedPref.getString(SHARED_PREF_KEY_BRIDGES, "");
        String[] pairs = StringUtils.split(a,":");
        String[][] set = new String[pairs.length][2];
        for(int i=0;i<pairs.length;i++)
            set[i] = StringUtils.split(pairs[i],"~");
        return set;
    }

    private void saveReceiverState(boolean state) {
        Log.d(TAG,"Write Receiver state");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SHARED_PREF_KEY_RECEIVER_STATE, state);
        editor.apply();
    }

    private void saveBridgeData(String[][] set){
        Log.d(TAG, "Write user data");

        String[] pairs = new String[set.length];
        for(int i=0;i<set.length;i++){
            pairs[i] = StringUtils.join(set[i],'~');
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_PREF_KEY_BRIDGES, StringUtils.join(pairs, ':'));
        editor.apply();
    }
}