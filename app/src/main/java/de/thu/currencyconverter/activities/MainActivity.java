package de.thu.currencyconverter.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;


import com.google.gson.Gson;

import de.thu.currencyconverter.R;
import de.thu.currencyconverter.adaptors.CurrencySpinnerAdapter;
import de.thu.currencyconverter.exchangeRate.ExchangeRateDatabase;
import de.thu.currencyconverter.exchangeRate.ExchangeRateUpdateRunnable;
import de.thu.currencyconverter.services.UpdateCurrenciesDailyJobService;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 101;
    private ShareActionProvider shareActionProvider;
    private ExchangeRateDatabase exchangeDB;
    private ExchangeRateUpdateRunnable exchangeDBUpdateRunnable;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar logic here
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //QUICKFIX FOR NETWORK ACCESS IN MAIN THREAD
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        //Shared preferences logic
        preferences = getSharedPreferences("INITIAL_APP_DATA",MODE_PRIVATE);

        exchangeDB = new ExchangeRateDatabase(this);
        exchangeDBUpdateRunnable = new ExchangeRateUpdateRunnable(this);

        //Set update schedule job to update the values once in 24 hours
        setUpdateSchedule();

        //Fill in the data
        loadAllData(exchangeDB);

    }

    @Override
    protected void onRestart() {
        //Fill in the toolbar again
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Fill the spinners again
        loadAllData(exchangeDB);


        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.app_menu,menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
         shareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);
        setShareText(null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        setContentView(R.layout.activity_main);
        switch (item.getItemId()){
            case R.id.app_menu_entry:
                openCurrencyListActivity();
                return true;
            case R.id.app_menu_refresh:
                //Refresh rates
                Thread exchangeRateUpdateThread = new Thread(exchangeDBUpdateRunnable);
                exchangeRateUpdateThread.start();

                //Fill in the toolbar again
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                //load spinners
                loadAllData(exchangeDB);




                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    double getEnteredNumberValue(){
        EditText enteredNumberView = (EditText) findViewById(R.id.editTextNumberDecimal);

        String enteredNumberText = enteredNumberView.getText().toString();
        double value = (enteredNumberText.equals("")) ? 0 : Double.parseDouble(enteredNumberText);

        return value;
    }

    public void openCurrencyListActivity(){
        Intent intent = new Intent(this,CurrencyListActivity.class);
        startActivity(intent);
    }

    private void loadAllData(ExchangeRateDatabase db){
        //Here all of currencies will be loaded and then the spinners (and the input text view) will be filled with the required/saved data

        //Load of currencies
        exchangeDBUpdateRunnable.loadExchangeRatesData();


        //View elements logic
        Spinner fromSpinner = (Spinner) findViewById(R.id.fromCurrencySpinner);
        Spinner toSpinner = (Spinner) findViewById(R.id.toCurrencySpinner);

        TextView fromInputValueTextView = (TextView) findViewById(R.id.editTextNumberDecimal);


        CurrencySpinnerAdapter adaptor = new CurrencySpinnerAdapter(db);

        fromSpinner.setAdapter(adaptor);
        toSpinner.setAdapter(adaptor);

        //Set default values (from saved preferences)
        int fromSpinnerSelectedId = preferences.getInt("FromCurrencySelectedID",8);
        int toSpinnerSelectedId = preferences.getInt("ToCurrencySelectedID",30);

        String fromInputValueString = preferences.getString("FromInputValueString","");

        fromSpinner.setSelection(fromSpinnerSelectedId);
        toSpinner.setSelection(toSpinnerSelectedId);

        fromInputValueTextView.setText(fromInputValueString);


    }

     public void onConvertButtonClick(View view) {
        //Set onClick listener on the convert button
        Spinner fromCurrencySpinner = (Spinner) findViewById(R.id.fromCurrencySpinner);
        Spinner toCurrencySpinner = (Spinner) findViewById(R.id.toCurrencySpinner);

        int fromSelectedId = (int)fromCurrencySpinner.getSelectedItemId();
        int toSelectedId = (int)toCurrencySpinner.getSelectedItemId();

        TextView resultTextView = (TextView) findViewById(R.id.conversionResultTextView);

        String[] currencyNames = exchangeDB.getCurrencies();

        String fromCurrency = currencyNames[fromSelectedId];
        String toCurrency = currencyNames[toSelectedId];

        double fromValue = getEnteredNumberValue();

        double convertedValue = exchangeDB.convert(fromValue,fromCurrency,toCurrency);

         String formattedConvertedValue = String.format("%.2f",convertedValue);

         resultTextView.setText(formattedConvertedValue);

        //SAVE DATA TO PREFERENCES
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt("FromCurrencySelectedID",fromSelectedId);
         editor.putInt("ToCurrencySelectedID",toSelectedId);
         editor.putString("FromInputValueString",fromValue + "");
         editor.apply();

         //END OF SAVE


        //SHARE CURRENCY INFO OPTION LOGIC

        String shareText = String.format("Currency Converter says,\n %.2f %s are worth %.2f %s",fromValue,fromCurrency,convertedValue,toCurrency);

        setShareText(shareText);


    }

    private void setShareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }

        shareActionProvider.setShareIntent(shareIntent);

    }


    private void setUpdateSchedule(){
        //Set a job schedule to update the currencies every 24 hours
        ComponentName serviceName = new ComponentName(this, UpdateCurrenciesDailyJobService.class);


        JobInfo jobInfo = new JobInfo.Builder(JOB_ID,serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setPeriodic(86400000) //ones per day (24h)
                //.setPeriodic(15000)
                .build();


        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        int jobResult = jobScheduler.schedule(jobInfo);

        if(jobResult == JobScheduler.RESULT_SUCCESS){
            Log.i("CurrencyUpdateSchedule","Successfully scheduled");
        }
    }




}