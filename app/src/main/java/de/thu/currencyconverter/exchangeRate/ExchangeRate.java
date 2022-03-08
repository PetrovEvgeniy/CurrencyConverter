package de.thu.currencyconverter.exchangeRate;

public class ExchangeRate {
    private final String currencyName;
    private double rateForOneEuro;
    private String capital;


    public ExchangeRate(String currencyName, String capital, double rateForOneEuro) {
        this.currencyName = currencyName;
        this.rateForOneEuro = rateForOneEuro;
        this.capital = capital;
    }


    public void setExchangeRate(double newExchangeRate){
        this.rateForOneEuro = newExchangeRate;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCapital() {
        return capital;
    }

    public double getRateForOneEuro() {
        return rateForOneEuro;
    }
}
