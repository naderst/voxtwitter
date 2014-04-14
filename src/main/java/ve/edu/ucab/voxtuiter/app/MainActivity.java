/*
    Vox Tuiter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtuiter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtuiter.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    private TextToSpeech vox;
    private static final int REQUEST_CODE = 1234;
    private AppMain appMain;
    private Intent intent;
    private ArrayList<String> matches;
    private Thread thread;
    /**
     * Semáforo para bloquear el método speak hasta que el motor tts termine de hablar
     */
    private Semaphore wait4speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vox = new TextToSpeech(this, this);
    }

    /**
     * Escucha al usuario para luego procesar su voz y llevarla a texto.
     * La ejecución se hace de manera bloqueante y es procesada por el método onActivityResult
     *
     * @return Lista de frases escuchadas
     */
    public ArrayList<String> listenSpeech() {
        while(true) {
            startActivityForResult(intent, REQUEST_CODE);
            matches.clear();

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                return matches;
            }

            speak("Lo siento, no escuché lo que dijo, vuelva a intentarlo");
        }
    }

    /**
     * Procesa un texto y lo lleva a voz. El hilo que lo ejecute se bloqueará hasta que
     * el texto se reproduzca
     *
     * @param text Texto para ser llevado a voz
     */
    public void speak(String text) {
        HashMap<String, String> myHashAlarm = new HashMap();

        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FIN");

        vox.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);

        try {
            wait4speak.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre una url en el navegador de Android
     * @param url URL a mostrar en el navegador
     */
    public void openURL(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    /*
        Evento que se dispara cuando el motor de la API TextToSpeech está listo
     */
    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            vox.setLanguage(Locale.getDefault());
            vox.setOnUtteranceCompletedListener(this);
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 0);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 0);
            wait4speak = new Semaphore(0);
            matches = new ArrayList<String>();

            final MainActivity mainActivity = this;

            thread = new Thread() {
                @Override
                public void run() {
                    appMain = new AppMain(mainActivity);
                    appMain.onInit();
                }
            };

            thread.start();

        } else {
            vox = null;
            Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
        Evento que procesará cuando el usuario hable a través del micrófono una vez puesto a la escucha.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // La variable matches contiene las distintas frases que se detectaron a través del micrófono
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            thread.interrupt();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        wait4speak.release(); // Envía la señal de que el motor tts terminó de hablar
    }

    /**
     * Almacena un par clave/valor en un archivo
     *
     * @param key Clave del valor
     * @param val Valor que se desea almacenar
     */
    public void save(String key, String val) {
        SharedPreferences settings = getSharedPreferences("tokens", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, val);
        editor.commit();
    }

    /**
     * Obtiene un valor dado una clave
     *
     * @param key Clave del valor a obtener
     * @return Valor de clave, si no existe la clave retorna vacío
     */
    public String read(String key) {
        SharedPreferences settings = getSharedPreferences("tokens", 0);
        return settings.getString(key, "");
    }

}
