package com.example.mdzahidul.bluetoothclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private boolean CONTINUE_READ_WRITE = true;

    private BluetoothSocket socket;
    BluetoothAdapter adapter;
    private OutputStreamWriter os;
    private InputStream is;
    private BluetoothDevice remoteDevice;

    String charToSend = "";
    String bt_address;

    Button btn_s_top,btn_s_bottom,btn_s_left,btn_s_right,btn_s_tl,btn_s_tr,btn_s_br,btn_s_bl;
    Button top,btm,left,right;
    Button move_left,move_right;

    Button snap,servo1,servo2;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    Button keyPad,speech,gDrive;
    LinearLayout keyPad_layout,speechLayout,webviewLayout;

    int servoNumber = 0;

    /* WebView */
    WebView webView;
    Button refreshBtn,backBtn;
    EditText urlTxt;
    ProgressBar progressBar;
    SharedPreferences saveUrl;

    AlertDialog.Builder alert;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        declearBtn();
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressUrl);
        progressBar.setVisibility(View.INVISIBLE);

        saveUrl = getSharedPreferences("urlSave",100);
        if(!saveUrl.getString("url","").equals("")) urlTxt.setText(saveUrl.getString("url",""));
        else urlTxt.setText("");
        if(saveUrl.getString("isBtListEnable","").equals("")){
            SharedPreferences.Editor editor = saveUrl.edit();
            editor.putString("isBtListEnable", "false" );
            editor.commit();
        }


        new Thread(errorToConnect).start();
        //getListofBtDevice();

        alert = new AlertDialog.Builder(this);
        adapter = BluetoothAdapter.getDefaultAdapter();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(!urlTxt.getText().toString().equals("") ){
                webView.loadUrl(urlTxt.getText().toString());
            }
            else {
                Toast.makeText(getApplicationContext(),"No url",Toast.LENGTH_SHORT).show();
            }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.goBack();
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
            urlTxt.setText(url);
            webView = view;
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int progress) {
            if(progress > 95){
                progressBar.setVisibility(View.INVISIBLE);
            }
            else{
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
            }
            }
        });

        urlTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences.Editor editor = saveUrl.edit();
                editor.putString("url", urlTxt.getText().toString());
                editor.commit();
            }
        });
    }

    private void declearBtn(){

        keyPad_layout = (LinearLayout) findViewById(R.id.keyPad_layout);
        speechLayout = (LinearLayout) findViewById(R.id.speechLayout);
        webviewLayout = (LinearLayout) findViewById(R.id.webview_Layout);

        webView = (WebView) findViewById(R.id.webview_gdrive);
        refreshBtn = (Button) findViewById(R.id.refreshBtn);
        backBtn = (Button) findViewById(R.id.backBtnWebview);
        urlTxt = (EditText) findViewById(R.id.webUrl);

        btn_s_top = (Button) findViewById(R.id.btn_s_top);
        btn_s_bottom = (Button) findViewById(R.id.btn_s_btm);
        btn_s_left = (Button) findViewById(R.id.btn_s_left);
        btn_s_right = (Button) findViewById(R.id.btn_s_right);

        btn_s_tl = (Button) findViewById(R.id.btn_s_top_left);
        btn_s_tr = (Button) findViewById(R.id.btn_s_top_right);
        btn_s_bl = (Button) findViewById(R.id.btn_s_btm_left);
        btn_s_br = (Button) findViewById(R.id.btn_s_btm_right);

        top = (Button) findViewById(R.id.top);
        btm = (Button) findViewById(R.id.bottom);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);

        move_left = (Button) findViewById(R.id.rotate_left);
        move_right = (Button) findViewById(R.id.rotate_right);

        snap = (Button) findViewById(R.id.snap);
        servo1 = (Button) findViewById(R.id.servo1);
        servo2 = (Button) findViewById(R.id.servo2);

        speech = (Button) findViewById(R.id.speech);
        gDrive = (Button) findViewById(R.id.gDrive);
        keyPad = (Button) findViewById(R.id.keyPad);


        btn_s_top.setOnClickListener(this);
        btn_s_bottom.setOnClickListener(this);
        btn_s_left.setOnClickListener(this);
        btn_s_right.setOnClickListener(this);

        btn_s_tl.setOnClickListener(this);
        btn_s_tr.setOnClickListener(this);
        btn_s_br.setOnClickListener(this);
        btn_s_bl.setOnClickListener(this);

        top.setOnClickListener(this);
        btm.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);

        move_right.setOnClickListener(this);
        move_left.setOnClickListener(this);

        snap.setOnClickListener(this);
        servo1.setOnClickListener(this);
        servo2.setOnClickListener(this);

        speech.setOnClickListener(this);
        gDrive.setOnClickListener(this);
        keyPad.setOnClickListener(this);

    }

    @Override
    public void finish() {
        super.finish();
        if(socket != null){
            try{
                is.close();
                os.close();
                socket.close();
                CONTINUE_READ_WRITE = false;
            }catch(Exception e){}
        }
    }

    private Runnable reader = new Runnable() {

        @Override
        public void run() {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            socket = getBluetoothDevice().createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            android.util.Log.e("TrackingFlow", "Connected...");
            os = new OutputStreamWriter(socket.getOutputStream());
            is = socket.getInputStream();
            android.util.Log.e("TrackingFlow", "Socket is On");
            charToSend = "Connected";
            new Thread(writter).start();
            android.util.Log.e("TrackingFlow", "Writing Thread is On: " + CONTINUE_READ_WRITE);

            /* may be here the bottom code */
        } catch (IOException e) {
            Log.e("check","error");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                Toast.makeText(getApplicationContext(),"Connection Lost",Toast.LENGTH_SHORT).show();
                }
            });
            new Thread(errorToConnect).start();
        }
        }
    };

    private Runnable writter = new Runnable() {

        @Override
        public void run() {
        int index = 0;
        try {
            os.write(charToSend+"\n");
            os.flush();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                Toast.makeText(getApplicationContext(),"Problem with data sending",Toast.LENGTH_SHORT).show();
                new Thread(errorToConnect).start();
                }
            });
        }
        }
    };

    /**
    * Showing google speech input dialog
    **/
     private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"For servo ");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),"Don't get anything",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* For Voice command */
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(servoNumber == 1){
                        if(result.get(0).equals("left")) {
                            charToSend = "p";
                            new Thread(writter).start();
                        }
                        else if(result.get(0).equals("right")) {
                            charToSend = "q";
                            new Thread(writter).start();
                        }
                        else Toast.makeText(getApplicationContext(),"Nothing Match with \""+result.get(0)+"\"",Toast.LENGTH_SHORT).show();
                        servoNumber = 0;
                    }
                    else if(servoNumber == 2){
                        if(result.get(0).equals("up")) {
                            charToSend = "r";
                            new Thread(writter).start();
                        }
                        else if(result.get(0).equals("down")) {
                            charToSend = "s";
                            new Thread(writter).start();
                        }
                        else Toast.makeText(getApplicationContext(),"Nothing Match with \""+result.get(0)+"\"",Toast.LENGTH_SHORT).show();
                        servoNumber = 0;
                    }
                }
                break;
            }
        }

        /* For selection of bluetooth device */
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                bt_address =data.getStringExtra("bt_address");
                Toast.makeText(getApplicationContext(),"Connecting ... ",Toast.LENGTH_SHORT).show();
                new Thread(reader).start();
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),"Please Select anyone",Toast.LENGTH_SHORT).show();
                new Thread(errorToConnect).start();
            }
        }
    }

    private BluetoothDevice getBluetoothDevice(){
        Set<BluetoothDevice> allDevice = adapter.getBondedDevices();
        for(BluetoothDevice bt : allDevice) {
            if(bt.getAddress().equals(bt_address) ){
                return bt;
            }
        }
        return null;
    }

    Runnable errorToConnect = new Runnable() {
        @Override
        public void run() {

            if(!saveUrl.getString("isBtListEnable","true").equals("true")){
                Intent intent = new Intent(getApplicationContext(), BluetoothDeviceList.class);
                intent.putExtra("check","Device not found");
                startActivityForResult(intent, 1);
            }
        }
    };

    @Override
    public void onClick(View view) {

        boolean doSend = true;
        if(view == btn_s_top) charToSend = "a";
        else if(view == btn_s_tr) charToSend = "b";
        else if(view == btn_s_right) charToSend = "c";
        else if(view == btn_s_br) charToSend = "d";
        else if(view == btn_s_bottom) charToSend = "e";
        else if(view == btn_s_bl) charToSend = "f";
        else if(view == btn_s_left) charToSend = "g";
        else if(view == btn_s_tl) charToSend = "h";

        else if(view == top) charToSend = "i";
        else if(view == left) charToSend = "l";
        else if(view == btm) charToSend = "k";
        else if(view == right) charToSend = "j";

        else if(view == move_left) charToSend = "n";
        else if(view == move_right) charToSend = "m";

        else if(view == snap) charToSend = "0";
        else if(view == servo1) {
            doSend = false;
            servoNumber = 1;
            promptSpeechInput();
        }
        else if(view == servo2){
            doSend = false;
            servoNumber = 2;
            promptSpeechInput();
        }

        if(view == keyPad){

            speechLayout.setVisibility(View.INVISIBLE);
            keyPad_layout.setVisibility(View.VISIBLE);
            webviewLayout.setVisibility(View.INVISIBLE);

            keyPad.setBackgroundColor(Color.WHITE);
            keyPad.setTextColor(Color.parseColor("#ff4f4f4f"));

            speech.setBackgroundColor(Color.TRANSPARENT);
            speech.setTextColor(Color.WHITE);

            gDrive.setBackgroundColor(Color.TRANSPARENT);
            gDrive.setTextColor(Color.WHITE);

            doSend = false;
        }
        else if(view == speech){
            speechLayout.setVisibility(View.VISIBLE);
            keyPad_layout.setVisibility(View.INVISIBLE);
            webviewLayout.setVisibility(View.INVISIBLE);

            speech.setBackgroundColor(Color.WHITE);
            speech.setTextColor(Color.parseColor("#ff4f4f4f"));

            keyPad.setBackgroundColor(Color.TRANSPARENT);
            keyPad.setTextColor(Color.WHITE);

            gDrive.setBackgroundColor(Color.TRANSPARENT);
            gDrive.setTextColor(Color.WHITE);

            doSend = false;
        }
        else if(view == gDrive){
            webviewLayout.setVisibility(View.VISIBLE);
            speechLayout.setVisibility(View.INVISIBLE);
            keyPad_layout.setVisibility(View.INVISIBLE);

            gDrive.setBackgroundColor(Color.WHITE);
            gDrive.setTextColor(Color.parseColor("#ff4f4f4f"));

            keyPad.setBackgroundColor(Color.TRANSPARENT);
            keyPad.setTextColor(Color.WHITE);

            speech.setBackgroundColor(Color.TRANSPARENT);
            speech.setTextColor(Color.WHITE);

            if(!urlTxt.getText().toString().equals("") ){
                webView.loadUrl(urlTxt.getText().toString());
            }
            else {
                Toast.makeText(getApplicationContext(),"No url",Toast.LENGTH_SHORT).show();
            }
            doSend = false;
        }

        if(doSend) new Thread(writter).start();
    }

    @Override
    public void onBackPressed() {
        alert.setTitle("close application");
        alert.setMessage("Do you want to close this application?");
        alert.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            if(adapter.enable()) adapter.disable();
            finish();
            }
        });
        alert.setNegativeButton("No",null);
        alert.show();
    }

}




    /*int bufferSize = 1024;
    int bytesRead = -1;
    byte[] buffer = new byte[bufferSize];
    //Keep reading the messages while connection is open...
    while(CONTINUE_READ_WRITE){
        Log.e("TrackingFlow", "WWWTTTFFF3wwwww4243");
        final StringBuilder sb = new StringBuilder();
        bytesRead = is.read(buffer);
        if (bytesRead != -1) {
            String result = "";
            while ((bytesRead == bufferSize) && (buffer[bufferSize-1] != 0)){
                result = result + new String(buffer, 0, bytesRead - 1);
                bytesRead = is.read(buffer);
            }
            result = result + new String(buffer, 0, bytesRead - 1);
            sb.append(result);
        }

        android.util.Log.e("TrackingFlow", "Read: " + sb.toString());

        //Show message on UIThread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }*/
