package com.prog.gentlemens.cepstrumanalyzer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import com.prog.gentlemens.cepstrumanalyzer.data.Data;
import com.prog.gentlemens.cepstrumanalyzer.enums.VowelName;
import com.prog.gentlemens.cepstrumanalyzer.message.RecordMessage;
import com.prog.gentlemens.cepstrumanalyzer.permission.Permission;
import com.prog.gentlemens.cepstrumanalyzer.thread.RecordThread;
import com.prog.gentlemens.cepstrumanalyzer.thread.WriteThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.round;
import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.shortToByteConversion;

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
	
	private int vowelNameOrdinal;
	private String fileName;
	private String pathFaceActivity = null;
	
	private int recordingTime = 5000;
	private int mProgressStatus = 0;
	private RecordThread recordThread = null;
	private CountDownTimer counter;
	
	private boolean recordAllowed = true;
	
	private Toolbar myToolbar;
	
	private boolean isRecording = false;
	
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
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.recording_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		
		Permission.setPermissions(this);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mspinner.setAdapter(adapter);
		final Intent mainActivityIntent = new Intent(this, MainActivity.class);
		mspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Object item = parent.getItemAtPosition(pos);
				System.out.println("pos = " + pos + " item = " + item.toString());
				vowelNameOrdinal = pos;
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				vowelNameOrdinal = 0;
			}
			
		});
		
		mprogress = (ProgressBar) findViewById(R.id.progress_bar);
		
		myToolbar = (Toolbar) findViewById(R.id.face_toolbar);
		setSupportActionBar(myToolbar);
		
		faceWelcome();      //becose of mrecordbutton
		
		mnextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//android.content.Intent public Intent(android.content.Context packageContext,
				//              Class<?> cls
				if (pathFaceActivity != null) {
					if (isRecording == false) {
						mainActivityIntent.putExtra("put_vowel", vowelNameOrdinal);
						mainActivityIntent.putExtra("pathFaceActivity", pathFaceActivity);
						mainActivityIntent.putExtra("put_name", fileName);
						//intent.putExtra("put_channelConfig", channelConfig);
						mainActivityIntent.putExtra("recordingTime", recordingTime);
						
						startActivity(mainActivityIntent);
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
				} else {
					if (recordAllowed == false) {
						mrecordButton.setText("Need user access to record");
					}
				}
			}
		});
	}
	
	private void faceWelcome() {
       /*
       Intent intent = new Intent();

        if(getIntent().getStringExtra("pathMainActivity"). == false)     //powrót
        {
            pathFaceActivity = getIntent().getStringExtra("pathMainActivity");
        }*/
		
		if (getIntent().getBooleanExtra("comeBack", false) == true) {
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
		Data currentData = new Data();
		currentData.setName(mnameEdit.getText().toString(),
							msurnameEdit.getText().toString(),
				VowelName.values()[vowelNameOrdinal].toString());
		
		currentData.setPath(getFilesDir().toString());
		currentData.createNewFile();
		
		pathFaceActivity = currentData.getPath();
		
		BlockingQueue<RecordMessage> queueWithRecordThread = new LinkedBlockingQueue<>();
		isRecording = true;
		new Thread(new WriteThread(queueWithRecordThread, currentData), "writeThread").start();
		recordThread = new RecordThread(queueWithRecordThread);
		new Thread(recordThread, "recordThread").start();
	}
	
	private int setRecordingTime() {
		int functionTemp = 5000;
		
		try {
			functionTemp = Integer.parseInt(mrecordingTime.getText().toString());
			if (functionTemp < 0) {
				functionTemp = 5000;
			} else {
				if (functionTemp >= 30000) {
					functionTemp = 30000;
				}
			}
		} catch (NumberFormatException nfe) {
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
	
	private void stopRecording() {
		recordThread.stopRecording();
		isRecording = false;
	}
	
	private void startRecording() {
		recordingTime = setRecordingTime();
		
		saveData();
		
		recordingTimer(recordingTime);
	}
	
	public void openWebPage(String url) {
		try {
			Uri uri = Uri.parse(url);  //http://cepstralanalysisapp.webnode.com/privacy-policy/
			
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			}
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG)
			     .show();
		}
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		System.out.println("*****FaceActivity onSaveInstanceState()***** ");
		
		if (pathFaceActivity != null) {
			savedInstanceState.putString("pathFaceActivity", pathFaceActivity);
		}
	}
	
	public void onRestoreInstanceState(Bundle savedInstantState) {
		super.onRestoreInstanceState(savedInstantState);
		
		System.out.println("*****FaceActivity onRestoreInstanceState()***** ");
		
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
