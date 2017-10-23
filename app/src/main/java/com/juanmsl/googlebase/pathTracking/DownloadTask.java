package com.juanmsl.googlebase.pathTracking;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.juanmsl.googlebase.logic.Utils;

public class DownloadTask extends AsyncTask<String, Void, String> {

    private GoogleMap googleMap;

    public DownloadTask(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    protected String doInBackground(String... url) {

        String data = "";
        try{
            data = Utils.downloadUrl(url[0]);
        }catch(Exception e){
            Log.e(DownloadTask.class.getName(), e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        ParserTask parserTask = new ParserTask(googleMap);
        parserTask.execute(result);
    }
}