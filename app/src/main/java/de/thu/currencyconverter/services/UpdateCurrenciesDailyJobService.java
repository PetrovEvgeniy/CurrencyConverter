package de.thu.currencyconverter.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import de.thu.currencyconverter.activities.MainActivity;
import de.thu.currencyconverter.exchangeRate.ExchangeRateUpdateRunnable;

public class UpdateCurrenciesDailyJobService extends JobService {
    private LoadCurrenciesAsyncTask loadCurrenciesAsyncTask;


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Toast.makeText(this, "Scheduled Currency Update starts", Toast.LENGTH_SHORT).show();

        loadCurrenciesAsyncTask = new LoadCurrenciesAsyncTask(this,this.getApplicationContext());
        loadCurrenciesAsyncTask.execute(jobParameters);
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }


    private static class LoadCurrenciesAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {
        private final JobService jobService;
        private Context context;
        private ExchangeRateUpdateRunnable exchangeDBUpdateRunnable;
        private SharedPreferences preferences;
        public LoadCurrenciesAsyncTask(JobService service, Context context) {

            exchangeDBUpdateRunnable = new ExchangeRateUpdateRunnable(context);
            this.jobService = service;
            this.context = context;
        }
        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            Thread exchangeRateUpdateThread = new Thread(exchangeDBUpdateRunnable);
            exchangeRateUpdateThread.start();

            return jobParameters[0];
        }
        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            jobService.jobFinished(jobParameters,false);
            //Toast.makeText(jobService, "Scheduled Currency Update ended", Toast.LENGTH_SHORT).show();

        }


    }

}