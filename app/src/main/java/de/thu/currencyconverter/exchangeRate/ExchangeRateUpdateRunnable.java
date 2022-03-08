package de.thu.currencyconverter.exchangeRate;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import de.thu.currencyconverter.R;
import de.thu.currencyconverter.activities.MainActivity;


public class ExchangeRateUpdateRunnable extends ExchangeRateDatabase implements Runnable{

    private Context context;
    private SharedPreferences preferences;
    private boolean running = false;
    private boolean success = false;

    public ExchangeRateUpdateRunnable(Context context){
        super(context);
        this.context = context;
        this.preferences = context.getSharedPreferences("INITIAL_APP_DATA",MODE_PRIVATE);

    }

    @Override
    public void run() {
        synchronized (ExchangeRateUpdateRunnable.this){
            if(!running){
                running = true;
                updateCurrencies();

                if(success){
                    //display success notification
                    showSuccessToast();
                }
                else{
                    //display error notification
                    showErrorAlert();
                }


            }

        }


    }

    //This method will update the old exchange rates with the actual ones (from the API)

    synchronized public void updateCurrencies(){
        String queryString = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

        try{
            URL url = new URL(queryString);
            URLConnection connection = url.openConnection();

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(connection.getInputStream(),connection.getContentEncoding());

            int eventType = parser.getEventType();

            while (eventType!=XmlPullParser.END_DOCUMENT){

                if(eventType==XmlPullParser.START_TAG){
                    if("Cube".equals(parser.getName())
                            && parser.getAttributeValue(null,"currency") != null){

                        //Here we have to update the exchange value
                        String currencyName = parser.getAttributeValue(null,"currency");
                        double currencyExchangeRate = Double.parseDouble(parser.getAttributeValue(null,"rate"));

                        //Here we find the given exchange rate
                        int idxOfRate = getIndexOfExchangeRate(currencyName);
                        if(idxOfRate != -1){
                            //Set the value
                            RATES[idxOfRate].setExchangeRate(currencyExchangeRate);
                        }


                    }
                }

                eventType = parser.next();
            }


            //here we sett running to false
            running = false;
            success = true;

            //Save the data to the preferences

            saveExchangeRatesData();


        }
        catch (Exception e){
            running = false;
            success = false;
            Log.e("ExchangeRateDB Update", "DATABASE ERROR!!");
        }
    }

    synchronized private static int getIndexOfExchangeRate(String currencyName) {
        int first = 0;
        int upto = RATES.length;

        for (int i = first; i < upto; i++) {
            if (RATES[i].getCurrencyName().equals(currencyName)) {
                return i;
            }
        }

        //if not found
        return -1;
    }

    synchronized public void showErrorAlert() {

        ContextCompat.getMainExecutor(context).execute(()  -> {
            // This is where the UI code goes.

            AlertDialog ad = new AlertDialog.Builder(this.context)
                    .setTitle("Database error!")
                    .setMessage("An error has occurred while trying to receive data from the API server. Please check the log message!")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, null)

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setIcon(android.R.drawable.stat_notify_error)
                    .show();
        });

    }

    synchronized public void showSuccessToast(){
        ContextCompat.getMainExecutor(context).execute(()  -> {
            // This is where the UI code goes.

            Toast t = Toast.makeText(this.context, "Successfully updated the currencies!", Toast.LENGTH_LONG);
            t.show();
        });
    }


    synchronized public void saveExchangeRatesData(){
        //Save the data to the preferences as a JSON string in the shared preferences
        Gson gson = new Gson();
        String json = gson.toJson(RATES);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Currencies_JSON",json);
        editor.apply();


    }

    synchronized public void loadExchangeRatesData(){
        //Load the data from the JSON string file and save it to the RATES array
        Gson gson = new Gson();

        String json = preferences.getString("Currencies_JSON","");


        //MAKE a check json is empty and if it is refresh the RATE's values (or use the hard coded ones)
        if(json.isEmpty()){
            //Then update the currencies and save them
            updateCurrencies();
            saveExchangeRatesData();

            json = preferences.getString("Currencies_JSON","");
        }

        ExchangeRate[] newRateData = gson.fromJson(json,ExchangeRate[].class);

        int indexOfRate;

        for(int i = 0; i < newRateData.length; i++){

            String newCurrencyName = newRateData[i].getCurrencyName();
            indexOfRate = getIndexOfExchangeRate(newCurrencyName);
            String currencyName = RATES[indexOfRate].getCurrencyName();


            double newExchangeRate = newRateData[i].getRateForOneEuro();
            //Change the rate
            RATES[indexOfRate].setExchangeRate(newExchangeRate);
        }


    }



}
