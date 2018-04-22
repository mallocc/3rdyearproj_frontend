package com.example.mallocc.caloriecompanion;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by hercu on 02-Apr-18.
 */

public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    public Speaker(Context context){
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int i) {
        tts.setLanguage(Locale.getDefault());
    }

    public void speek(String text)
    {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void destroy()
    {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}