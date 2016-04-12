package com.example.santiagogranados.recbypas;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

//inicializamos los botones como variables y el estado recording como un booleano//
    Button grabar, parar, reproducir;
    Boolean recording;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//relacionamos las variables de los botonoes con el id de los wigets//
        grabar = (Button)findViewById(R.id.grabar);
        parar = (Button)findViewById(R.id.parar);
        reproducir = (Button)findViewById(R.id.reproducir);

        imageView = (ImageView) findViewById(R.id.imageView);
//inicializamos los botones parar y reproducir como apagados//
        parar.setEnabled(false);
        reproducir.setEnabled(false);
//relacionamos el estado onclick de los botones con las instacias que seran los metodos que realicen cada proceso//
        grabar.setOnClickListener(startRecOnClickListener);
        parar.setOnClickListener(stopRecOnClickListener);
        reproducir.setOnClickListener(playBackOnClickListener);

    }
//onclick grabar//
    OnClickListener startRecOnClickListener
            = new OnClickListener(){

        @Override
        public void onClick(View arg0) {
            imageView.setImageResource(R.drawable.rec);
            grabar.setEnabled(false);
            parar.setEnabled(true);

            Thread recordThread = new Thread(new Runnable(){

                @Override
                public void run() {
                    recording = true;
                    startRecord();
                }
            });

            recordThread.start();
        }};
//onclick parar//

    OnClickListener stopRecOnClickListener
            = new OnClickListener(){

        @Override
        public void onClick(View arg0) {
            parar.setEnabled(false);
            reproducir.setEnabled(true);
            imageView.setVisibility(View.INVISIBLE);
            recording = false;
        }};
//onclick reproducir//
    OnClickListener playBackOnClickListener
            = new OnClickListener(){

        @Override
        public void onClick(View v) {
            imageView.setImageResource(R.drawable.playy);
            imageView.setVisibility(View.VISIBLE);
            playRecord();
        }

    };
//creacion del archivo//
    private void startRecord(){

        //variable que contendrá ubicacion y nombre de nuestro archivo
        File file = new File(Environment.getExternalStorageDirectory(), "test.wav");

        try {
            file.createNewFile();
//Stream del archivo de audio//
            OutputStream outputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
//declaramos buffer//
            int minBufferSize = AudioRecord.getMinBufferSize(11025,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            short[] audioData = new short[minBufferSize];

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    11025,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);
//inicia la grabacion//
            audioRecord.startRecording();
//estado grabando true//
            while(recording){
                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                for(int i = 0; i < numberOfShort; i++){
                    dataOutputStream.writeShort(audioData[i]);
                }
            }
//para la grabacion//
            audioRecord.stop();
            dataOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
//metodo reproducir//
    void playRecord(){
//nombre y ubicacion del archivo//
        File file = new File(Environment.getExternalStorageDirectory(), "test.wav");
//bytes//
        int shortSizeInBytes = Short.SIZE/Byte.SIZE;
//buffer size//
        int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while(dataInputStream.available() > 0){
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    11025,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}