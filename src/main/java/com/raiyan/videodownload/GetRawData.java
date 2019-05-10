package com.raiyan.videodownload;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetRawData extends AsyncTask<String,Void, String> {

    private static final String TAG = "GetRawData";
    private static int count = 0;
    Context mContext;

    public GetRawData(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try{
            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder resullt = new StringBuilder();
            String line;
            while((line = reader.readLine())!= null){
                resullt.append(line);
            }
            return resullt.toString();
        }catch (MalformedURLException me){
            Log.d(TAG, "doInBackground: malformed url");
            Toast.makeText((MainActivity)mContext,"malformed url",Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            Log.d(TAG, "doInBackground: IOException");
            Toast.makeText((MainActivity)mContext,"IOEXception",Toast.LENGTH_SHORT).show();
        }catch (SecurityException e){
            Log.d(TAG, "doInBackground: Security Exception");
        }finally {
            if(connection!=null){
                connection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    Log.d(TAG, "doInBackground: IOEXception while closing buffred reader");
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        count++;
        Log.d(TAG, "onPostExecute: "+count+".  \n"+s+"\n\n\n\n\n");
    }
}
