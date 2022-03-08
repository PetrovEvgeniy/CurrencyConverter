package de.thu.currencyconverter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import de.thu.currencyconverter.R;
import de.thu.currencyconverter.adaptors.CurrencyListAdapter;
import de.thu.currencyconverter.exchangeRate.ExchangeRateDatabase;

public class CurrencyListActivity extends AppCompatActivity {

    private ExchangeRateDatabase exchangeDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        exchangeDB = new ExchangeRateDatabase(this);

        ListView currencyListView = (ListView) findViewById(R.id.currency_list_view);

        CurrencyListAdapter adapter = new CurrencyListAdapter(exchangeDB);
        currencyListView.setAdapter(adapter);

        currencyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String capitalLocationQuery = getFormattedCapitalLocationQuery(i);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0`?q=" +capitalLocationQuery));
                startActivity(intent);
            }
        });

    }

    private String getFormattedCapitalLocationQuery(int i){
        ExchangeRateDatabase db = exchangeDB;

        String currencyName = db.getCurrencies()[i];
        String capitalName = db.getCapital(currencyName);

        String formattedCapitalLocationQuery = capitalName.replace(" ","%20");

        return formattedCapitalLocationQuery;

    }



}