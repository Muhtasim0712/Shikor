package com.shikor.app.util;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shikor.app.R;

import java.util.Locale;

/**
 * TtsHelper - Helper managing Android TextToSpeech engine lifecycle, language detection
 * (English / Bangla), speech rate adjustment, and system voice data installation intents.
 *
 * CONCEPT: TextToSpeech API, activity lifecycle integration, and accessibility options.
 */
public class TtsHelper implements TextToSpeech.OnInitListener {

    private static final String TAG = "TtsHelper";

    private final Context context;
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private boolean isSpeaking = false;
    private OnTtsStateChangeListener listener;

    public interface OnTtsStateChangeListener {
        void onTtsStarted();
        void onTtsStopped();
        void onTtsError(String error);
    }

    public TtsHelper(Context context) {
        this.context = context;
        this.tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    public void setListener(OnTtsStateChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true;
            updateSpeechRate();
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    isSpeaking = true;
                    if (listener != null) listener.onTtsStarted();
                }

                @Override
                public void onDone(String utteranceId) {
                    isSpeaking = false;
                    if (listener != null) listener.onTtsStopped();
                }

                @Override
                public void onError(String utteranceId) {
                    isSpeaking = false;
                    if (listener != null) listener.onTtsError("TTS playback error");
                }
            });
            Log.d(TAG, "TextToSpeech initialized successfully.");
        } else {
            isInitialized = false;
            Log.e(TAG, "Failed to initialize TextToSpeech.");
        }
    }

    public void updateSpeechRate() {
        if (tts != null && isInitialized) {
            float rate = Prefs.getInstance(context).getTtsSpeechRate();
            tts.setSpeechRate(rate);
        }
    }

    public void speak(String textToSpeak) {
        if (textToSpeak == null || textToSpeak.trim().isEmpty()) return;
        if (!isInitialized || tts == null) {
            Toast.makeText(context, "Text-to-Speech engine initializing...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSpeaking) {
            stop();
            return;
        }

        String appLang = Prefs.getInstance(context).getLanguage();
        Locale targetLocale = "bn".equalsIgnoreCase(appLang) ? new Locale("bn", "BD") : Locale.US;

        int langStatus = tts.isLanguageAvailable(targetLocale);
        if (langStatus == TextToSpeech.LANG_MISSING_DATA || langStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
            if ("bn".equalsIgnoreCase(appLang)) {
                targetLocale = new Locale("bn");
                langStatus = tts.isLanguageAvailable(targetLocale);
            }
        }

        if (langStatus == TextToSpeech.LANG_MISSING_DATA || langStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
            showMissingDataDialog(targetLocale.getDisplayName());
            return;
        }

        tts.setLanguage(targetLocale);
        updateSpeechRate();

        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "ShikorUtteranceId_" + System.currentTimeMillis());
    }

    private void showMissingDataDialog(String langName) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("TTS Voice Data Missing")
                .setMessage("Voice data for " + langName + " is missing on your device. Would you like to install speech synthesis voice data?")
                .setPositiveButton("Install Voice Data", (dialog, which) -> {
                    try {
                        Intent installIntent = new Intent();
                        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        context.startActivity(installIntent);
                    } catch (Exception e) {
                        Toast.makeText(context, "Could not open TTS settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public boolean isSpeaking() {
        return isSpeaking || (tts != null && tts.isSpeaking());
    }

    public void stop() {
        if (tts != null) {
            try {
                tts.stop();
            } catch (Exception ignored) {}
        }
        isSpeaking = false;
        if (listener != null) listener.onTtsStopped();
    }

    public void shutdown() {
        stop();
        if (tts != null) {
            try {
                tts.shutdown();
            } catch (Exception ignored) {}
            tts = null;
        }
        isInitialized = false;
    }
}
