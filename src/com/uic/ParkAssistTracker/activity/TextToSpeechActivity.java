package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.speech.tts.TextToSpeech;

/**
 * Created by rrajath on 4/27/14.
 */
public class TextToSpeechActivity extends Activity implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;

    @Override
    public void onInit(int status) {
        textToSpeech = new TextToSpeech(this, this);


    }
}
