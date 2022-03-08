package de.thu.currencyconverter.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.thu.currencyconverter.R;
import de.thu.currencyconverter.exchangeRate.ExchangeRate;
import de.thu.currencyconverter.exchangeRate.ExchangeRateDatabase;

public class CurrencySpinnerAdapter extends BaseAdapter {

    private final ExchangeRateDatabase rateDb;


    public CurrencySpinnerAdapter(ExchangeRateDatabase db){
        rateDb = db;
    }

    @Override
    public int getCount() {
        return rateDb.getCurrencies().length;
    }

    @Override
    public Object getItem(int i) {
        return rateDb.getCurrencies()[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {


        Context context = parent.getContext();
        String currencyName = rateDb.getCurrencies()[i];

        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.simple_spinner_item,null,false);
        }

        //Set the country flag image source
        ImageView currencyFlagImageView = (ImageView) view.findViewById(R.id.currency_flag_image);
        String currencyFlagFileName = "flag_" + currencyName.toLowerCase();

        int currencyFlagFileId = context.getResources().getIdentifier(currencyFlagFileName,"drawable", context.getPackageName());
        currencyFlagImageView.setImageResource(currencyFlagFileId);

        //Set the text of the currency name
        TextView currencyNameTextView = (TextView) view.findViewById(R.id.currency_name);
        currencyNameTextView.setText(currencyName);

        return view;
    }
}
