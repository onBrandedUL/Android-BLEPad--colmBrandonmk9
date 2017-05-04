package com.example.testble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

public class BluetoothControlActivity extends Activity {
    private final static String TAG = BluetoothControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;
    private HolloBluetooth mble;
    private Context context;

    private ScrollView scrollView;

    private Handler mHandler;
    public String received;
    public String A3Value;
    public String A0Value;
    public String A2Value;
    public String A1Value;
    private static final int MSG_DATA_CHANGE = 0x11;
    float Bpm = 100;



    EditText A2Input, peakValue,troughValue, audioRecordFilenameOne, audioPlaybackFilenameOne;
    Button audioRecordEnterOne, audioRecordStartOne, audioRecordStopOne, audioPlaybackFileOpenOne, audioPlaybackStartOne, audioPlaybackStopOne;
    ToggleButton mobileDataOnOff, wifiOnOff, doNotDisturbOnOff, gpsOnOff, airplaneModeOnOff,audioPlaybackLoopOnOffOne,metronomeOnOff,sineWaveOnOff,sawWaveOnOff,squareWaveOnOff,pulseWaveOnOff;
    SeekBar metronomeBpm, pulseWidthControl, reverbDryWet, roomsize, damp;
    TextView textView9, textView10, metronomeBpmPrintOut, pulseWidthValue, midiNote;

    public String toSend;

    StringBuilder output = new StringBuilder();

    private PdUiDispatcher dispatcher;

    private void initPD() throws IOException {
        int sampleRate = AudioParameters.suggestSampleRate();
        PdAudio.initAudio(sampleRate, 1, 2, 8, true);

        dispatcher = new PdUiDispatcher();

        PdBase.setReceiver(dispatcher);

        dispatcher.addListener("ble",receiver);
        PdBase.subscribe("ble");

        dispatcher.addListener("peak",receiver);
        PdBase.subscribe("peak");
        dispatcher.addListener("trough",receiver);
        PdBase.subscribe("trough");

    }


    public void sendPatchData(String receive, String value) {

        sendFloatPD(receive, Float.parseFloat(value));

        Log.e(receive, value);

    }

    public void sendSymbolPD(String receiver, String msg)
    {
        PdBase.sendSymbol(receiver,msg);
    }

    public void sendFloatPD(String receiver, Float value)
    {
        PdBase.sendFloat(receiver, value);
    }

    public void sendBangPD(String receiver)
    {
        PdBase.sendBang(receiver);
    }


    private void loadPDPatch(String patchName) throws IOException {
        File dir = getFilesDir();
        try {
            IoUtils.extractZipResource(getResources().openRawResource(R.raw.synth), dir, true);
            File pdPatch = new File(dir, patchName);
            PdBase.openPatch(pdPatch.getAbsolutePath());
        } catch (IOException e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);


        //audioRecordFilenameOne = (EditText) findViewById(R.id.audioRecordFilenameOne);
        //audioPlaybackFilenameOne = (EditText) findViewById(R.id.audioPlaybackFilenameOne);
        //audioRecordEnterOne = (Button) findViewById(R.id.audioRecordEnterOne);
        audioPlaybackStopOne = (Button) findViewById(R.id.audioRecordStopOne);
        audioRecordStartOne = (Button) findViewById(R.id.audioRecordStartOne);
        audioRecordStopOne = (Button) findViewById(R.id.audioRecordStopOne);
        audioPlaybackLoopOnOffOne = (ToggleButton) findViewById(R.id.audioPlaybackLoopOnOffOne);
        metronomeOnOff = (ToggleButton) findViewById(R.id.metronomeOnOff);
        audioPlaybackFileOpenOne = (Button) findViewById(R.id.audioPlaybackFileOpenOne);
        audioPlaybackStartOne = (Button) findViewById(R.id.audioPlaybackStartOne);
        metronomeBpm = (SeekBar) findViewById(R.id.metronomeBpm);
        mobileDataOnOff = (ToggleButton) findViewById(R.id.mobileDataOnOff);
        wifiOnOff = (ToggleButton) findViewById(R.id.wifiOnOff);
        doNotDisturbOnOff = (ToggleButton) findViewById(R.id.doNotDisturbOnOff);
        gpsOnOff = (ToggleButton) findViewById(R.id.gpsOnOff);
        airplaneModeOnOff = (ToggleButton) findViewById(R.id.airplaneModeOnOff);
        sineWaveOnOff = (ToggleButton) findViewById(R.id.sineWaveOnOff);
        sawWaveOnOff = (ToggleButton) findViewById(R.id.sawWaveOnOff);
        squareWaveOnOff = (ToggleButton) findViewById(R.id.squareWaveOnOff);
        pulseWaveOnOff = (ToggleButton) findViewById(R.id.pulseWaveOnOff);
        textView9 = (TextView) findViewById(R.id.textView9);
        textView10 = (TextView) findViewById(R.id.textView10);
        pulseWidthControl = (SeekBar) findViewById(R.id.pulseWidthControl);
        metronomeBpmPrintOut = (TextView) findViewById(R.id.metroBpmPrintOut);
        pulseWidthValue = (TextView) findViewById(R.id.pulseWidthValue);
        midiNote = (TextView) findViewById(R.id.midiNote);
        reverbDryWet = (SeekBar) findViewById(R.id.reverbDryWet);
        roomsize = (SeekBar) findViewById(R.id.roomsize);
        damp = (SeekBar) findViewById(R.id.damp);
        //audioPlaybackEnterOne = (Button) findViewById(R.id.audioPlaybackEnterOne);
        // Switch onOffSwitch = (Switch) findViewById(R.id.onOffSwitch);

        metronomeBpm.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        float prog = progress / 1.0f;
                        Bpm = prog;
                        String x = Float.toString(prog);
                        metronomeBpmPrintOut.setText("Bpm: " + x);
                        //slide1Value = progress / 100.0f;
                        sendFloatPD("metronomeBpm", prog);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        pulseWidthControl.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        float prog = progress / 100.0f;
                        String x = Float.toString(prog);
                        pulseWidthValue.setText("Pulse Width Value: " + x);
                        //slide1Value = progress / 100.0f;
                        sendFloatPD("pulseWidthOne", prog);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        reverbDryWet.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        float prog = progress / 100.0f;
                        String x = Float.toString(prog);
                        //pulseWidthValue.setText("Pulse Width Value: " + x);
                        //prog = progress / 100.0f;
                        sendFloatPD("reverbDryWet", prog);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        roomsize.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        float prog = progress / 100.0f;
                        //String x = Float.toString(prog);
                        //pulseWidthValue.setText("Pulse Width Value: " + x);
                        //prog = progress / 100.0f;
                        sendFloatPD("roomsize", prog);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
        damp.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        float prog = progress / 100.0f;
                        String x = Float.toString(prog);
                        //pulseWidthValue.setText("Pulse Width Value: " + x);
                        //prog = progress / 100.0f;
                        sendFloatPD("damp", prog);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;

        mble = HolloBluetooth.getInstance(getApplicationContext());


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DATA_CHANGE:
                        int color = msg.arg1;
                        String strData = (String) msg.obj;
                        SpannableStringBuilder builder = new SpannableStringBuilder(strData);

                        //ForegroundColorSpan ，BackgroundColorSpan
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
                        String string;
                        int num;
                        switch (color) {
                            case Color.BLUE: //send

                                builder.setSpan(colorSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case Color.RED:    //error
                                builder.setSpan(colorSpan, 0, strData.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case Color.BLACK: //tips
                                builder.setSpan(colorSpan, 0, strData.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;

                            default: //receive
                                addLogText(strData, Color.BLACK, strData.length());


                                    for (int i = 0; i < strData.length(); i++) {
                                        if (strData.charAt(i) == 'A' || strData.charAt(i) == 'B' || strData.charAt(i) == 'C' || strData.charAt(i) == 'D') {
                                            received = output.toString();
                                            sensorParse();
                                            output.delete(0,output.length());
                                            output.append(strData.charAt(i));

                                        } else {
                                           output.append(strData.charAt(i));
                                        }
                                    }

                                break;
                        }

                        break;

                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

               new Handler().post(new Runnable() {
            @Override
            public void run() {

                int i;
                for (i = 0; i < 5; i++) {
                    if (mble.connectDevice(mDeviceAddress, bleCallBack))
                        break;

                    try {
                        Thread.sleep(10, 0);
                    } catch (Exception e) {

                    }
                }
                if (i == 5) {

                    return;
                }

                try {
                    Thread.sleep(10, 0);
                } catch (Exception e) {

                }


                if (mble.wakeUpBle()) {

                } else {

                }

            }
        });

        try {
            initPD();
            loadPDPatch("synth/synth.pd"); // This is the name of the patch in the zip

            new Handler().post(new Runnable() {
                @Override
                public void run() {

                    if (!mble.sendData("start")) {
                    }

                }
            });


        } catch (IOException e) {
            finish();
        }

        /*audioPlaybackLoopOnOffOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                sendFloatPD("audioPlaybackLoopOnOff", val); //send value to patch, receiveEvent names onOff

            }
        });*/

       //metronomeBpm.setOnSeekBarChangeListener();


        mobileDataOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                sendFloatPD("mobileDataOnOff",val);
                sendFloatPD("metronomeBpm", Bpm);
                //sendFloatPD("wifiOnOff",val);
                //sendFloatPD("doNotDisturbOnOff",val);
                //sendFloatPD("gpsOnOff",val);
                //sendFloatPD("airplaneModeOnOff",val);
            }
        });


        metronomeOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                sendFloatPD("metronomeBpm", Bpm);
                sendFloatPD("metronomeOnOff",val);
            }
        });

        wifiOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                //sendFloatPD("mobileDataOnOff",val);
                sendFloatPD("wifiOnOff",val);
                //sendFloatPD("doNotDisturbOnOff",val);
                //sendFloatPD("gpsOnOff",val);
                //sendFloatPD("airplaneModeOnOff",val);
            }
        });

        doNotDisturbOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                //sendFloatPD("mobileDataOnOff",val);
                //sendFloatPD("wifiOnOff",val);
                sendFloatPD("doNotDisturbOnOff",val);
                //sendFloatPD("gpsOnOff",val);
                //sendFloatPD("airplaneModeOnOff",val);
            }
        });

        gpsOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                //sendFloatPD("mobileDataOnOff",val);
                //sendFloatPD("wifiOnOff",val);
                //sendFloatPD("doNotDisturbOnOff",val);
                sendFloatPD("gpsOnOff",val);
                //sendFloatPD("airplaneModeOnOff",val);
            }
        });

        airplaneModeOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                //sendFloatPD("mobileDataOnOff",val);
                //sendFloatPD("wifiOnOff",val);
                //sendFloatPD("doNotDisturbOnOff",val);
                //sendFloatPD("gpsOnOff",val);
                sendFloatPD("airplaneModeOnOff",val);
            }
        });





        audioPlaybackLoopOnOffOne.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                sendFloatPD("audioPlaybackLoopOnOffOne",val);
            }
        });

        sineWaveOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                sendFloatPD("sineWaveOnOff",val);
            }
        });
        sawWaveOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                sendFloatPD("sawWaveOnOff",val);
            }
        });
        squareWaveOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                sendFloatPD("squareWaveOnOff",val);
            }
        });
        pulseWaveOnOff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                Toast.makeText(context, String.valueOf(val), Toast.LENGTH_SHORT).show();
                sendFloatPD("pulseWaveOnOff",val);
            }
        });

    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        return super.onMenuItemSelected(featureId, item);
    }

    public void audioPlaybackStopOne(View v){
        float val = 0.0f;
        sendFloatPD("audioPlaybackStopOne",val);
    }


    public void audioRecordStartOne(View v){
        float val = 1.0f;
        //sendSymbolPD("audioRecordFilenameOne", audioRecordFilenameOne.getText().toString());
        //Toast.makeText(context, "sound", Toast.LENGTH_SHORT).show();
        sendFloatPD("audioRecordEnterOne", val);
        sendFloatPD("audioRecordStartOne", val);
    }

    public void audioRecordStopOne(View v){
        float val = 1.0f;
        sendFloatPD("audioRecordStopOne",val);
    }

    public void audioPlaybackFileOpenOne(View v){
        float val = 1.0f;
        //sendSymbolPD("audioPlayBackFilenameOne", "sound");/*audioPlaybackFilenameOne.getText().toString()*/
        //Toast.makeText(context, audioPlaybackFilenameOne.getText().toString()+".wav", Toast.LENGTH_SHORT).show();
        //sendSymbolPD("audioPlayBackFilenameOne",audioPlaybackFilenameOne.getText().toString());
        //Toast.makeText(context, audioPlaybackFilenameOne.getText().toString(), Toast.LENGTH_SHORT).show();
        sendFloatPD("audioPlaybackFileOpenOne" ,val);
    }

    public void audioPlaybackStartOne(View v) {
        float val = 1.0f;
        sendFloatPD("audioPlaybackStartOne" , val);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.menu_refresh).setActionView(null);

        return super.onCreateOptionsMenu(menu);
    }

    void sensorParse()
    {
        if(received.length()>0) {
            if (received.charAt(0) == 'A') {
                A0Value = received.substring(1);
                Log.i("A0",A0Value);
                sendPatchData("a_input_0", A0Value);
            }
            else if (received.charAt(0) == 'B') {
                A1Value = received.substring(1);
                Log.i("A1",A1Value);
                textView9.setText(A1Value);
                sendPatchData("a_input_1", A1Value);
            }
            else if (received.charAt(0) == 'C') {
                A2Value = received.substring(1);
                Log.i("A2",A2Value);
                textView10.setText(A2Value);
                sendPatchData("a_input_2", A2Value);
            }
            else if (received.charAt(0) == 'D') {
                A3Value = received.substring(1);
                Log.i("A3",A3Value);
                sendPatchData("a_input_3", A3Value);
            }
        }
    }

    void addLogText(final String log, final int color, int byteLen) {
        Message message = new Message();
        message.what = MSG_DATA_CHANGE;
        message.arg1 = color;
        message.arg2 = byteLen;
        message.obj = log;
        mHandler.sendMessage(message);
    }

    HolloBluetooth.OnHolloBluetoothCallBack bleCallBack = new HolloBluetooth.OnHolloBluetoothCallBack() {

        @Override
        public void OnHolloBluetoothState(int state) {
            if (state == HolloBluetooth.HOLLO_BLE_DISCONNECTED) {
                onBackPressed();
            }
        }

        @Override
        public void OnReceiveData(byte[] recvData) {
            addLogText(ConvertData.bytesToHexString(recvData, false), Color.rgb(139, 0, 255), recvData.length);


        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PdAudio.startAudio(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PdAudio.stopAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mble.disconnectDevice();
        Log.d(TAG, "destroy");
        mble.disconnectLocalDevice();
        Log.d(TAG, "destroyed");
    }

    private PdReceiver receiver = new PdReceiver() {

        private void pdPost(final String msg) {
            Log.e("RECEIVED:", msg);



                    while (!mble.sendData(msg)) {
                        //  Log.e("BLEWRITE","ERROR");
                    }

                    sendFloatPD("stop", 1.0f);

                }




        @Override
        public void print(String s) {
            Log.i("PRINT",s);
        }

        @Override
        public void receiveBang(String source) {
            //pdPost("bang");
        }

        @Override
        public void receiveFloat(String source, float x) {

            if (source.equals("midivalue"))
            {
                //midiNote.setText(Float.toString(x));

            }

            else if (source.equals("trough"))
            {
               // troughValue.setText(Float.toString(x));

            }

        }

        @Override
        public void receiveList(String source, Object... args) {

        }

        @Override
        public void receiveMessage(String source, String symbol, Object... args) {
            //  pdPost("list: " + Arrays.toString(args));
            toSend  = symbol+",";
            for(int i = 0; i < args.length;i++) {
                toSend += args[i].toString();
                if(i != args.length - 1) {
                    toSend += ",";
                }
                else
                {
                    toSend += ";";
                }
            }
            toSend = toSend.replace(".0","");
            sendFloatPD("start", 1.0f);
            pdPost(toSend);

        }

        @Override
        public void receiveSymbol(String source, String symbol) {
            //pdPost("symbol: " + symbol);
        }
    };

}
