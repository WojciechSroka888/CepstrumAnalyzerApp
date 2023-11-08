package com.prog.gentlemens.cepstrumanalyzer;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.prog.gentlemens.cepstrumanalyzer.activity.DataActivity;
import com.prog.gentlemens.cepstrumanalyzer.data.Data;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DataActivityTest {

    @Test
    public void receiveProperCurrentData() {
        //given
        final String fullPath = "/data/data/com.prog.gentlemens.cepstrumanalyzer/files/_A_1 lip 2019 12:01:28_audio.pcm";
        Data data = new Data();
        data.setPath(fullPath);
        Context appContext = InstrumentationRegistry.getTargetContext();
        Intent intent = new Intent(appContext, DataActivity.class);
        intent.putExtra("current_data", data);

        //when
        appContext.startActivity(intent);

        //then
        // hold the debugger just to have access to activity
        int g = 4;
    }
}
