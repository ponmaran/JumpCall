package com.ponmaran.JumpCall;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    static final String SHARED_PREF_NAME = "BRIDGE_DATA";
    static final String SHARED_PREF_KEY_BRIDGES = "BRIDGE";

    private SharedPreferences sharedPref;

    private LinearLayout fieldParent;

	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        fieldParent = (LinearLayout) findViewById(R.id.field_parent);

        ImageButton buttonPlus = (ImageButton) findViewById(R.id.buttonAddRow);
        buttonPlus.setOnClickListener(listener);

        sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String[][] set = getSavedBridgeData();
        for (String[] aSet : set) {
            addFieldSet(aSet[0], aSet[1]);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");
            switch (view.getId()){
                case R.id.buttonAddRow:
                    addFieldSet("","");
                    break;
                case R.id.buttonDeleteRow:
                    Log.d(TAG, "delete pressed");
                    LinearLayout v = (LinearLayout) view.getParent();
                    fieldParent.removeView(v);
                    break;
            }
        }
    };

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.menu_save:
                saveBridgeData(captureBridgeDataFromView());
                Toast.makeText(getApplicationContext(), "Data Saved!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_reset:
                fieldParent.removeAllViews();
                saveBridgeData(new String[][]{{"",""}});
                addFieldSet("","");
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

    public String[][] captureBridgeDataFromView() {
        Log.d(TAG, "Read Data from user");
        List<String[]> setList = new ArrayList<String[]>();
        for(int i=0; i < fieldParent.getChildCount();i++){
            View viewBridgeSetHolder = fieldParent.getChildAt(i);
            EditText numBox = (EditText) viewBridgeSetHolder.findViewById(R.id.editTextNumber);
            EditText delBox = (EditText) viewBridgeSetHolder.findViewById(R.id.editTextDelay);

            String[] lineData = new String[2];
            lineData[0] = numBox.getText().toString();
            if (lineData[0].length() > 0)
                lineData[1] = delBox.getText().toString();
            setList.add(i, lineData);
        }

        String[][] setArray = new String[setList.size()][2];
        for(int i=0; i<setList.size();i++)
            setArray[i] = setList.get(i);

//        for(String[] a:setArray) Log.d(TAG, a[0] + " " + a[1]);

        return setArray;
    }

    private void addFieldSet(String number, String delay){
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

        numberBox.setText(number);
        delayBox.setText(delay);
    }

    private String[][] getSavedBridgeData(){
        Log.d(TAG, "Read saved data");
        String a = sharedPref.getString(SHARED_PREF_KEY_BRIDGES, getString(R.string.bridge_default_value));
        String[] pairs = StringUtils.split(a,":");
        String[][] set = new String[pairs.length][2];
        for(int i=0;i<pairs.length;i++)
            set[i] = StringUtils.split(pairs[i],"~");
        return set;
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