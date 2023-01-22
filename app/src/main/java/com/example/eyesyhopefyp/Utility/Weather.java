package com.example.eyesyhopefyp.Utility;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather extends AsyncTask<String,Void,String> {

    @Override
    protected String doInBackground(String... address) {
        try {
            URL url = new URL(address[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream is = connection.getInputStream();
            InputStreamReader irs = new InputStreamReader(is);

            int data = irs.read();
            String content = "";
            char ch;
            while (data != -1){
                ch = (char) data;
                content = content + ch;
                data = irs.read();
            }
            return content;
        }catch (Exception e){

        }
        return null;
    }
}

