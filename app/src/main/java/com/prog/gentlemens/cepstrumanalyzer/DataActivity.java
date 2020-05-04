package com.prog.gentlemens.cepstrumanalyzer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
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
import com.prog.gentlemens.cepstrumanalyzer.permission.Permission;
import com.prog.gentlemens.cepstrumanalyzer.thread.service.DataActivityThreadService;

import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import static com.prog.gentlemens.cepstrumanalyzer.math.MathOperations.round;

public class DataActivity extends AppCompatActivity {
	private Logger logger = Logger.getLogger(DataActivity.class.getName());
	private DataActivityThreadService dataActivityThreadService;
	private Toolbar toolbar;
	private CountDownTimer counter;
	private Data currentData;
	private int vowelNameOrdinal;
	private int recordingTime;
	private boolean isRecordAllowed = true;
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face);
		
		Button nextButton = findViewById(R.id.next_button);
		setNextButton(new Intent(this, ResultActivity.class), nextButton);
		
		Button recordButton = findViewById(R.id.record_button);
		setRecordButton(recordButton);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.recording_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Apply the adapter to the spinner
		Spinner spinner = findViewById(R.id.recording_spinner_list);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				vowelNameOrdinal = pos + 1;
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				vowelNameOrdinal = 0;
			}
		});
		
		toolbar = findViewById(R.id.face_toolbar);
		setSupportActionBar(toolbar);
		
		dataActivityThreadService = DataActivityThreadService.getInstance();
		Permission.setPermissions(this);
	}
	
	private void setNextButton(final Intent intent, final Button nextButton) {
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentData != null) {
					if (!dataActivityThreadService.getAudioRecordState()) {
						intent.putExtra("currentData", currentData);
						startActivity(intent);
					} else {
						nextButton.setText(R.string.wait_until_record);
					}
				} else {
					nextButton.setText(R.string.record_first);
				}
			}
		});
	}
	
	private void setRecordButton(final Button recordButton) {
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRecordAllowed) {
					if (!dataActivityThreadService.getAudioRecordState()) {
						recordButton.setText(R.string.stop);
						startRecording();
					} else {
						recordButton.setText(R.string.recording_in_progress);
						ProgressBar progressBar = findViewById(R.id.recording_progress_bar);
						progressBar.setProgress(0);
						counter.onFinish();
						counter.cancel();
						recordButton.setText("Record");
					}
				} else {
					recordButton.setText(R.string.need_user_access_to_record);
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		toolbar.inflateMenu(R.menu.three_dot_menu);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
	
	private void initData() {
		EditText nameEditText = findViewById(R.id.name_edit_text);
		EditText surnameEditText = findViewById(R.id.surname_edit_text);
		
		currentData = new Data();
		currentData.setName(nameEditText.getText().toString(),//
				surnameEditText.getText().toString(),//
				VowelName.values()[vowelNameOrdinal].toString());
		currentData.setPath(getFilesDir().toString());
		currentData.createNewFile();
	}
	
	private void setRecordingTime() {
		EditText recordingTimeEditText = findViewById(R.id.recording_time_edit_text);
		final int defaultTime = 5000;
		final int maxTime = 30000;
		int functionTemp = defaultTime;
		
		try {
			functionTemp = Integer.parseInt(recordingTimeEditText.getText().toString());
			if (functionTemp <= 0) {
				recordingTime = defaultTime;
			} else {
				if (functionTemp > maxTime) {
					recordingTime = maxTime;
				}
			}
		} catch (NumberFormatException e) {
			logger.warning("Could not parse " + e);
		}
		
		recordingTime = functionTemp;
	}
	
	private void recordingTimer(final long millisInFuture) {
		counter = new CountDownTimer(millisInFuture, 10) {
			ProgressBar progressBar = findViewById(R.id.recording_progress_bar);
			long timeElapsed;
			
			@Override
			public void onTick(long millisUntilFinished) {
				timeElapsed = millisInFuture - millisUntilFinished;
				int progressStatus = round(((timeElapsed) * 100.0) / millisInFuture);
				progressBar.setProgress(progressStatus + 1);
			}
			
			@Override
			public void onFinish() {
				Button recordButton = findViewById(R.id.record_button);
				recordButton.setText(R.string.recorded);
				Button nextButton = findViewById(R.id.next_button);
				nextButton.setText(R.string.next);
				stopRecording();
				currentData.setDuration(timeElapsed);
			}
		};
		
		counter.start();
	}
	
	private void stopRecording() {
		dataActivityThreadService.stopRecording();
	}
	
	private void startRecording() {
		setRecordingTime();
		initData();
		dataActivityThreadService.initWriteThread(currentData);
		dataActivityThreadService.start();
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
	
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public void onResume() {
		super.onResume();
		
		Intent intent = getIntent();
		Bundle extrasBundle = intent.getExtras();
		
		if (extrasBundle != null && !extrasBundle.isEmpty() && extrasBundle.containsKey("currentData")) {
			currentData = (Data) extrasBundle.getSerializable("currentData");
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public void onPause() {
		super.onPause();
		Bundle bundle = new Bundle();
		bundle.putSerializable("currentData", currentData);
		
		Intent intent = getIntent();
		intent.putExtras(bundle);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Bundle bundle = new Bundle();
		bundle.putSerializable("currentData", currentData);
		
		Intent intent = getIntent();
		intent.putExtras(bundle);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (isFinishing() && currentData != null && currentData.getCurrentFile() != null) {
			try {
				Files.delete(currentData.getCurrentFile().toPath());
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
		}
	}
	
}
