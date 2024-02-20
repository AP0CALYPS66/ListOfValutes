package com.example.fragments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {



    Handler handler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<Valute> valutes = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.Rv);
        AdapterValute adapterValute = new AdapterValute(valutes);
        recyclerView.setAdapter(adapterValute);

        handler = new Handler(){

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                ArrayList<Valute> valutes = (ArrayList<Valute>) msg.obj;
                AdapterValute adapterValute = new AdapterValute(valutes);
                recyclerView.setAdapter(adapterValute);

            }

        };







    DataThread thread = new DataThread();
    thread.start();





    }


    class DataThread extends Thread {
        @Override
        public void run() {
            super.run();
            String infoResult = "";
            ArrayList<Valute> valutes = new ArrayList<>();
            String flags = "";
            Bitmap bitmap = null;
            try {
                URL info = new URL("https://www.cbr-xml-daily.ru/daily_json.js");
                URL picture = new URL("https://gist.githubusercontent.com/sanchezzzhak/8606e9607396fb5f8216/raw/8a7209a4c1f4728314ef4208abc78be6e9fd5a2f/ISO3166_RU.json");
                Scanner scanner = new Scanner(info.openStream());
                while (scanner.hasNext()){
                    infoResult += scanner.nextLine();
                }
                scanner.close();
                Scanner scanner1 = new Scanner(picture.openStream());
                while (scanner1.hasNext()){
                    flags += scanner1.nextLine();
                }
                scanner1.close();

                JSONObject json = new JSONObject(infoResult).getJSONObject("Valute");
                JSONArray jsonflags = new JSONArray(flags);

                for (int i = 0; i < json.names().length(); i++){

                    JSONObject jsonValute = json.getJSONObject(json.names().getString(i));
                    String charCode = jsonValute.getString("CharCode").substring(0,2);

                    for (int j = 0; j < jsonflags.length() ; j++){
                        String charCodeFlag = jsonflags.getJSONObject(j).getString("iso_code2");
                        if (charCodeFlag.compareTo(charCode) == 0){
                            URL urlbit = new URL("https:" + jsonflags.getJSONObject(j).getString("flag_url"));
                            HttpsURLConnection connection = (HttpsURLConnection) urlbit.openConnection();
                            connection.setReadTimeout(1500);
//                            connection.setConnectTimeout(1500);
                            connection.connect();

                            int reasponceCode = connection.getResponseCode();
                            if (reasponceCode == 200){
                                InputStream inputStream = connection.getInputStream();
                                bitmap = BitmapFactory.decodeStream(inputStream);

                            }
                        }
                    }



                    valutes.add(new Valute(jsonValute.getString("Value"), jsonValute.getString("Name"), bitmap));
                }
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }

            Message msg = new Message();
            msg.obj = valutes;
            handler.sendMessage(msg);


        }
    }






}