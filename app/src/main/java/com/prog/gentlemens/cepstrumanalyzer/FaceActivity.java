package com.prog.gentlemens.cepstrumanalyzer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class FaceActivity extends AppCompatActivity {

    private String getName = "John";
    private String getSurname = "Smith";

    private EditText mnameEdit;
    private EditText msurnameEdit;
    private EditText mrecordingTime;

    private Button mnextButton;
    private Button mrecordButton;

    private Spinner mspinner = null;
    private ProgressBar mprogress;

    private int vowelName = 2;
    private String fileName;

    private String pathFaceActivity = null;
    //private boolean comeBack = false;   //pathFaceActivity != null -> recorded == true
    //public static final String PREFS_NAME = "myPrefsFile";
    //private SharedPreferences mFaceShared;

    private int recordingTime = 5000;
    private int mProgressStatus = 0;

    private CountDownTimer counter;

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_CODE = 1;
    private boolean PERMISSION_WRITE_EXTERNAL_STORAGE;
    private static final int PERMISSION_RECORD_AUDIO_CODE = 2;
    private boolean PERMISSION_RECORD_AUDIO;

    private boolean recordAllowed = true;

    private Toolbar myToolbar;

    //****************************AUDIO RECORD VARIABLE*******************************************

    private static final int sampleRateInHz = 22050;
    private static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private boolean isRecording = false;

    private AudioRecord recorder = null;
    private Thread recordingThread = null;

    private int BufferElementsRec = 1024;
    private int BytesPerElement = 2;

    //************************************END AUDIO RECORD VARIABLE*********************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);

        mnameEdit = (EditText) findViewById(com.prog.gentlemens.cepstrumanalyzer.R.id.name_edit);
        msurnameEdit = (EditText) findViewById(com.prog.gentlemens.cepstrumanalyzer.R.id.surname_edit);
        mrecordingTime = (EditText) findViewById(R.id.recording_time);

        mrecordButton = (Button) findViewById(R.id.record_button);
        mnextButton = (Button) findViewById(R.id.next_button);

        mspinner = (Spinner) findViewById(R.id.recording_list);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recording_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mspinner.setAdapter(adapter);

        mspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                System.out.println("pos = " + pos + " item = " + item.toString());
                vowelName = pos + 1;
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mprogress = (ProgressBar) findViewById(R.id.progress_bar);

        myToolbar = (Toolbar) findViewById(R.id.face_toolbar);
        setSupportActionBar(myToolbar);

        faceWelcome();      //becose of mrecordbutton

        //Dangerous permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (setPermissions() != true) {
                recordAllowed = false;
            }
        }
        //*******************

        mnextButton.setOnClickListener(new View.OnClickListener() {
            //send required values to MainActivity
            @Override
            public void onClick(View v) {

                if(pathFaceActivity != null){//if (recorded == true) {
                    if (isRecording == false) {
                        Context context;
                        context = getApplicationContext();

                        Intent intent = new Intent(context, MainActivity.class);

                        intent.putExtra("put_vowel", vowelName);
                        intent.putExtra("pathFaceActivity", pathFaceActivity);
                        intent.putExtra("put_name", fileName);
                        //intent.putExtra("put_channelConfig", channelConfig);
                        intent.putExtra("recordingTime", recordingTime);
                        intent.putExtra("put_sampleRateInHz", sampleRateInHz);

                        startActivity(intent);

                    } else {
                        mnextButton.setText("Wait until record");
                    }
                } else {
                    mnextButton.setText("record first");
                }
            }
        });

        mrecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (recordAllowed == true) {
                    //needs to record -> does not record
                    if (isRecording == false) {
                        mrecordButton.setText("Stop");
                        startRecording();
                    }

                    //just stopping
                    else {
                        mrecordButton.setText("Recording in progress");
                        //stopRecording();
                    }
                } else if (recordAllowed == false) {
                    mrecordButton.setText("Need user access to record");
                }
            }
        });
    }

    private void faceWelcome ()
    {
       /*
       Intent intent = new Intent();

        if(getIntent().getStringExtra("pathMainActivity"). == false)     //powrót
        {
            pathFaceActivity = getIntent().getStringExtra("pathMainActivity");
        }*/

       if( getIntent().getBooleanExtra("comeBack", false) == true)
       {
           pathFaceActivity = getIntent().getStringExtra("pathMainActivity");
           mrecordButton.setText("Recorded");
       }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //PL: "nie w onCreate - logiczniej jest tworzyć widok menu w tej funkcji
        //onCreateOptionsMenu niż onCreate jak już taka funkcja istnieje (onCreate)"

        myToolbar = (Toolbar) findViewById(R.id.face_toolbar);
        myToolbar.inflateMenu(R.menu.threedotmenu);
        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.click_website:
                openWebPage("http://cepstralanalysisapp.webnode.com");
                return true;

            case R.id.click_privacy_url:
                openWebPage("http://cepstralanalysisapp.webnode.com/privacy-policy/");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveData() {
        System.out.println("przed fiel ext");
        //File externalStorage = Environment.getExternalStoragePublicDirectory
        //       (Environment.DIRECTORY_MUSIC + "/hoarseness");
        //Mkdir() & Mkdirs()
        System.out.println("po file ext");
        //String fileName;

        File appFile = getFilesDir();

        try {
            getName = mnameEdit.getText().toString();
            System.out.println("SUCCESS GET NAME");

        } catch (Throwable t) {
            System.out.println("FALSE GET NAME");
        }

        try {
            getSurname = msurnameEdit.getText().toString();
            System.out.println("SUCCESS GET SURNAME");

        } catch (Throwable t) {
            System.out.println("FALSE GET SURNAME");
        }

        switch (vowelName) {
            case 1:
                fileName = "_A_" + getDate() + "_audio.pcm";
                break;

            case 2:
                fileName = "_E_" + getDate() + "_audio.pcm";
                break;

            case 3:
                fileName = "_I_" + getDate() + "_audio.pcm";
                break;

            default:
                fileName = "_?_" + getDate() + "_audio.pcm";
                break;
        }

        fileName = getName + getSurname + fileName;

        File file = new File(appFile.getAbsolutePath(), fileName);

        pathFaceActivity = file.getAbsolutePath();

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        short shortData[] = new short[BufferElementsRec];
        byte byteData[] = new byte[BufferElementsRec * BytesPerElement];

        FileOutputStream stream_to_write = null;

        try {
            stream_to_write = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {

            int ilosc = recorder.read(shortData, 0, BufferElementsRec); //stack
            //ilosc = recorder.read(shortData, 0, BufferElementsRec);              //stack

            isRecording = ilosc > 0;

            if (isRecording) {
                try {
                    /*PL:
                    "tutaj mogę wysyłać dane do analizy
                    cepstrumStream(short [] part);
                    private cepstrum;
                    ->f, JT, SH
                    --> cepstrum cepstrumStream( short [] part );"
                    */
                    short_to_byte_conversion(shortData, ilosc, byteData);
                    stream_to_write.write(byteData, 0, ilosc * BytesPerElement);
                    // stream needs byte
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("FALSE");
                }
            }
        }

        try {
            stream_to_write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDate() {
        Date date = new Date(); //java.util or java.sql

        String current_date = DateFormat.getDateTimeInstance().format(date);

        System.out.println("**getDate()**");

        return current_date;
    }

    private void short_to_byte_conversion(short[] shortData, int ilosc, byte[] byteData) {
        for (int i = 0; i < ilosc; i++) {
            byteData[i * 2] = (byte) (shortData[i] & 0x00FF);
            byteData[(i * 2) + 1] = (byte) (shortData[i] >> 8);
            //shortData[i] = 0;						//why ?
        }
    }

    private int setRecordingTime()
    {
        int functionTemp = 5000;

        try{
            functionTemp = Integer.parseInt(mrecordingTime.getText().toString());
            if(functionTemp < 0)
            {
                functionTemp = 5000;
            }

            else if(functionTemp >= 30000)
            {
                functionTemp = 30000;
            }
        }catch (NumberFormatException nfe)
        {
            System.out.println("Could not parse " + nfe);
        }

        return functionTemp;
    }

    private void recordingTimer(int interval) {
        counter = new CountDownTimer(interval, 10) {
            public void onTick(long millisUntilFinished) {
                mProgressStatus = round(((recordingTime - millisUntilFinished) * 100) / recordingTime);

                mprogress.setProgress(mProgressStatus + 1);        //99 ?
                //counter.onFinish();
                System.out.println("mProgressStatus = " + mProgressStatus + " [ms]");
            }

            public void onFinish() {
                mProgressStatus = 0;
                //recorded = true; //pathFaceActivity != null -> recorded == true
                mrecordButton.setText("Recorded");
                mnextButton.setText("next");
                stopRecording();
            }
        };

        counter.start();
    }

    protected int round(double value) {
        if ((value - (int) value) >= 0.5)            //(int) value == floor(value)
        {
            return (int) Math.floor(value + 0.5);
        } else {
            return (int) Math.floor(value);
        }
    }

    private void interruptFunction() {
        //stops timer and progressbar


        //stops recording
        stopRecording();
    }

    private void stopRecording() {
        //closes audio
        if (recorder != null) {
            recordingThread = null;
            recorder.stop();
            recorder.release();

            isRecording = false;

            recorder = null;
            System.out.println("stop recording!!");
        }
    }

    private void startRecording() {
        //set recording time
        recordingTime = setRecordingTime();

        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRateInHz, channelConfig, audioFormat, BufferElementsRec * BytesPerElement);

        recorder.startRecording();            //override ???

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            public void run() {
                saveData();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();      //"really" start

        recordingTimer(recordingTime);
    }

    private boolean setPermissions() {
        if (checkPermissionWriteExternalStorage() == false) {
            requestWriteExternalStoragePermission();
        } else {
            PERMISSION_WRITE_EXTERNAL_STORAGE = true;
        }

        if (checkPermissionRecordAudio() == false) {
            requestRecordAudioPermission();
        } else {
            PERMISSION_RECORD_AUDIO = true;
        }

        if (PERMISSION_RECORD_AUDIO == true && PERMISSION_WRITE_EXTERNAL_STORAGE == true) {
            return true;
        } else {
            return false;
        }
    }

    //DANGEROUS PERMISSION MARSHMALLOW OR HIGHER
    private boolean checkPermissionWriteExternalStorage() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    //DANGEROUS PERMISSION MARSHMALLOW OR HIGHER
    private boolean checkPermissionRecordAudio() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestWriteExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(FaceActivity.this, "Write External Storage permission allows us to do store record. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_CODE);
        }
    }

    private void requestRecordAudioPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(FaceActivity.this, "Record Audio permission allows us to do record. Please allow this permission in App Settings  .", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE_CODE: {
                //allowed
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PERMISSION_WRITE_EXTERNAL_STORAGE = true;
                }

                //denied
                else {
                    PERMISSION_WRITE_EXTERNAL_STORAGE = false;
                }
                return;
            }

            case PERMISSION_RECORD_AUDIO_CODE: {
                //allowed
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PERMISSION_RECORD_AUDIO = true;
                }

                //denied
                else {
                    PERMISSION_RECORD_AUDIO = false;
                }
                return;
            }
        }
    }

    public void openWebPage(String url) {
        try {
            Uri uri = Uri.parse(url);  //http://cepstralanalysisapp.webnode.com/privacy-policy/

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application can handle this request."
                    + " Please install a web browser", Toast.LENGTH_LONG).show();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        System.out.println( "*****FaceActivity onSaveInstanceState()***** " );

        if (pathFaceActivity != null) {
            savedInstanceState.putString("pathFaceActivity", pathFaceActivity);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstantState) {
        super.onRestoreInstanceState(savedInstantState);

        System.out.println( "*****FaceActivity onRestoreInstanceState()***** " );

        pathFaceActivity = savedInstantState.getString("pathFaceActivity");
    }
}
//******************CODE UNDERLINE IS IMPORTANT FOR CODE IMPROVEMENT********************************
/*
    @Override
    public void onPause()
    {
        System.out.println("*****FaceActivity onPause()*****");
        super.onPause();

        //*******************SAVE*********************

        if (pathFaceActivity != null) {
            SharedPreferences mFaceShared = getApplicationContext().getSharedPreferences("pathFaceActivity", MODE_PRIVATE);
            SharedPreferences.Editor editor = mFaceShared.edit();
            editor.putString("pathFaceActivity", pathFaceActivity);
            editor.commit();
        }
    }

    @Override
    public void onResume()          //onResume() vs onRestoreInstanceState()
    {
        System.out.println("*****FaceActivity onResume()*****");
        super.onResume();

        //*******************RESTORE*******************
        SharedPreferences mFaceShared = getApplicationContext().getSharedPreferences("pathFaceActivity", MODE_PRIVATE);
        //pomimo, że nic nie zostało nagrane, SharedPreferences zwraca jakiś adres nagrania ?
        //-> trzeba dać pomocniczą zmienną, która poinformuje, czy było coś nagrywane ? -> słabe, gdyż
        //----> lub użyć blok onSave 0nRestore -> nie ma tego problemu
        pathFaceActivity = mFaceShared.getString("pathFaceActivity", null);

        System.out.println("*****FaceActivity onResume()*****");
    }*/

