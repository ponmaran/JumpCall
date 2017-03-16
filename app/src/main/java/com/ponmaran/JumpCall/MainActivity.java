package com.ponmaran.JumpCall;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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
    static final String SHARED_PREF_NAME = "ROUTE_DATA";
    static final String SHARED_PREF_KEY_BRIDGE_SETS = "BRIDGE_SETS";
    static final String SHARED_PREF_KEY_FILTER_SETS = "FILTER_SETS";

    static final String SHARED_PREF_KEY_RECEIVER_STATE = "RECEIVER_STATE";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 7;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 14;

    private SharedPreferences sharedPref;

    private LinearLayout fieldParent;
    private LinearLayout fieldParentFilters;
    private int buttonIdFilterLineDelete;

	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        
        if (Build.VERSION.SDK_INT >= 23){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        setContentView(R.layout.activity_main);

        fieldParent = (LinearLayout) findViewById(R.id.field_parent);
        fieldParentFilters = (LinearLayout) findViewById(R.id.field_parent_filters);
        buttonIdFilterLineDelete = ImageButton.generateViewId();

        ImageButton buttonPlus = (ImageButton) findViewById(R.id.buttonAddRow);
        buttonPlus.setOnClickListener(listener);

        ImageButton buttonFilterPlus = (ImageButton) findViewById(R.id.buttonAddFilterRow);
        buttonFilterPlus.setOnClickListener(listener);

        Switch aSwitch = (Switch) findViewById(R.id.switchReceiverOnOff);
        aSwitch.setOnClickListener(listener);

        sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        boolean receiverState = getSavedReceiverState();
        setReceiverState(receiverState);
        aSwitch.setChecked(receiverState);

        String[][] filter = getSavedFilterData();
        if(filter.length == 0) filter = new String[][]{{"",""}};
        for (String[] aSet : filter)
            addFilterSet(aSet);

        String[][] set = getSavedBridgeData();
        if(set.length == 0) set = new String[][]{{"",""}};
        for (String[] aSet : set)
            addFieldSet(aSet);
    }

    @Override public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        Log.d(TAG, StringUtils.join(permissions,'~') + StringUtils.join(grantResults,';'));
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                if (grantResults[0] != 0)
                    new AlertDialog.Builder(this)
                            .setMessage( getString(R.string.app_name) +
                                    " " +
                                    getString(R.string.permissionRequestMessageCallPhone)
                                    + "\n\n"
                                    + getString(R.string.permissionRequestCallPhoneIfDenied))
                            .setPositiveButton("RE-TRY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this, permissions, requestCode);
                                }
                            })
                            .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    finish();
                                }
                            })
                            .show();
                break;
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]) && grantResults[0] != 0){
                    new AlertDialog.Builder(this)
                            .setMessage(
                                    getString(R.string.app_name)
                                            + " "
                                            + getString(R.string.permissionRequestMessageReadContacts)
                                            + "\n\n"
                                            + getString(R.string.permissionRequestReadContactsIfDenied))
                            .setPositiveButton("RE-TRY", new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this, permissions, requestCode);
                                }
                            }).setNegativeButton("I'M FINE", null)
                            .show();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");
            switch (view.getId()) {
                case R.id.buttonAddRow:
                    Log.d(TAG, "new field set");
                    addFieldSet(new String[]{"", ""});
                    break;
                case R.id.buttonAddFilterRow:
                    Log.d(TAG, "new filter");
                    addFilterSet(new String[]{"", ""});
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
                            getString(R.string.app_name) + " " + (
                                    checked ?
                                            "Enabled" :
                                            "Disabled"),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                default:
                    if (view.getId() == buttonIdFilterLineDelete) {
                        Log.d(TAG, "delete filter pressed");
                        v = (LinearLayout) view.getParent();
                        fieldParentFilters.removeView(v);
                    }
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
                saveFilterData(captureFilterDataFromView());
                Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_reset:
                String[][] emptySet2D = new String[][]{{"",""}};
                String[] emptySet = new String[]{"",""};
                fieldParent.removeAllViews();
                saveBridgeData(emptySet2D);
                addFieldSet(emptySet);
                fieldParentFilters.removeAllViews();
                addFilterSet(emptySet);
                saveFilterData(emptySet2D);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        String[][] a = captureBridgeDataFromView();
        String[][] b = getSavedBridgeData();
        String[][] c = captureFilterDataFromView();
        String[][] d = getSavedFilterData();

        if(a.length != b.length || c.length != d.length){
            changeAlert(a,c);
            return;
        } else {
            for (int i = 0; i < a.length; i++) {
                if (!Arrays.equals(a[i], b[i])) {
                    changeAlert(a, c);
                    return;
                }
            }
            for (int i = 0; i < c.length; i++) {
                if (!Arrays.equals(c[i], d[i])){
                    changeAlert(a, c);
                    return;
                }
            }
        }

        super.onBackPressed();
    }

    private void changeAlert(final String[][] a, final String[][] c) {
        new AlertDialog.Builder(this)
                .setMessage("Save Changes?")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveBridgeData(a);
                        saveFilterData(c);
                        Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_LONG).show();
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .show();
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

    public String[][] captureFilterDataFromView() {
        Log.d(TAG, "Read Data from user");
        List<String[]> setList = new ArrayList<String[]>();
        for(int i=0; i < fieldParentFilters.getChildCount();i++){
            LinearLayout viewBridgeSetHolder = (LinearLayout) fieldParentFilters.getChildAt(i);
            EditText numBox = (EditText) viewBridgeSetHolder.getChildAt(0);
            EditText delBox = (EditText) viewBridgeSetHolder.getChildAt(1);

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

    private void addFilterSet(String[] aSet){
        Log.d(TAG, "Insert a filter line");

        LinearLayout.LayoutParams setParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setParams.setLayoutDirection(LinearLayout.HORIZONTAL);
        LinearLayout fieldSet = new LinearLayout(this);
        fieldParentFilters.addView(fieldSet,fieldParentFilters.getChildCount(),setParams);

        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
        EditText countryCode = new EditText(this);
        countryCode.setHint("Country Code");
        LinearLayout.LayoutParams numberParams = new LinearLayout.LayoutParams((displayWidth * 45)/100, LinearLayout.LayoutParams.WRAP_CONTENT);
        fieldSet.addView(countryCode,0,numberParams);

        EditText convertTo = new EditText(this);
        convertTo.setHint("Convert to");
        LinearLayout.LayoutParams delayParams = new LinearLayout.LayoutParams((displayWidth * 45)/100, LinearLayout.LayoutParams.WRAP_CONTENT);
        fieldSet.addView(convertTo,1,delayParams);

        ImageButton buttonDeleteSet = new ImageButton(this);
        buttonDeleteSet.setBackground(ContextCompat.getDrawable(this, android.R.drawable.ic_delete));
        buttonDeleteSet.setId(buttonIdFilterLineDelete);
        buttonDeleteSet.setOnClickListener(listener);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fieldSet.addView(buttonDeleteSet,2,buttonParams);

        countryCode.setText(aSet[0]);
        convertTo.setText(aSet[1]);
    }

    private boolean getSavedReceiverState() {
        Log.d(TAG,"Read Receiver state");
        return sharedPref.getBoolean(SHARED_PREF_KEY_RECEIVER_STATE, false);
    }

    private String[][] getSavedFilterData() {
        Log.d(TAG, "Read saved filter data");
        String a = sharedPref.getString(SHARED_PREF_KEY_FILTER_SETS, "");
        String[] pairs = StringUtils.split(a,":");
        String[][] set = new String[pairs.length][2];
        for(int i=0;i<pairs.length;i++) {
            String[] filterPair = StringUtils.split(pairs[i],"~");;
            set[i][0] = filterPair[0];
            if (filterPair.length > 1)
                set[i][1] = filterPair[1];
            else
                set[i][1] = "";
        }
        return set;
    }

    private String[][] getSavedBridgeData(){
        Log.d(TAG, "Read saved route data");
        String a = sharedPref.getString(SHARED_PREF_KEY_BRIDGE_SETS, "");
        String[] pairs = StringUtils.split(a,":");
        String[][] set = new String[pairs.length][2];
        for(int i=0;i<pairs.length;i++) {
            String[] bridgePair = StringUtils.split(pairs[i],"~");;
            set[i][0] = bridgePair[0];
            if (bridgePair.length > 1)
                set[i][1] = bridgePair[1];
            else
                set[i][1] = "";
        }
        return set;
    }

    private void saveReceiverState(boolean state) {
        Log.d(TAG,"Write Receiver state");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SHARED_PREF_KEY_RECEIVER_STATE, state);
        editor.apply();
    }

    private void saveBridgeData(String[][] set){
        Log.d(TAG, "Write route data");

        String[] pairs = new String[set.length];
        for(int i=0;i<set.length;i++){
            pairs[i] = StringUtils.join(set[i],'~');
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_PREF_KEY_BRIDGE_SETS, StringUtils.join(pairs, ':'));
        editor.apply();
    }

    private void saveFilterData(String[][] set){
        Log.d(TAG, "Write route data");

        String[] pairs = new String[set.length];
        for(int i=0;i<set.length;i++){
            pairs[i] = StringUtils.join(set[i],'~');
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_PREF_KEY_FILTER_SETS, StringUtils.join(pairs, ':'));
        editor.apply();
    }
}