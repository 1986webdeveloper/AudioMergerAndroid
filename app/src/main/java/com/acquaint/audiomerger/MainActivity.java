package com.acquaint.audiomerger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * Created by acquaint on 15/8/18.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView imgUpload;
    TextView tv_fileOne,tv_fileTwo,tv_fileResult;
    ProgressBar pb_progress;
    public static int AUDIO_REQ_CODE=1;
    ArrayList<Uri> ar_file= new ArrayList<>();
    MediaPlayer mediaPlayer;


    private ImageView iv_play;
    private ImageView iv_stop;

    private SeekBar mSeekBar;
    int len = 0;
    boolean isPlaying = false;
    boolean isStartNew=false;


    public Runnable mRunnable;
    RelativeLayout rv_seekbar;
    Handler seekHandler = new Handler();
    File fileResult=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        initListeners();

    }

    private File mergeAudio(ArrayList<Uri> ar_uri) {


        File[] files=new File[ar_uri.size()];
        FileInputStream[] fileInputStreams=new FileInputStream[ar_uri.size()];
        FilterHeaderTrailerInputStream[] filter=new FilterHeaderTrailerInputStream[ar_uri.size()];
        Vector<FilterHeaderTrailerInputStream> inputStreamVector = new Vector<FilterHeaderTrailerInputStream>();
        try {
            for(int i =0;i<ar_uri.size();i++){
                files[i]=new File(ImagePathMarshmallow.getPath(MainActivity.this,ar_uri.get(i)));
                fileInputStreams[i]=new FileInputStream(files[i]);
                filter[i]=new FilterHeaderTrailerInputStream(fileInputStreams[i]);
                inputStreamVector.add(filter[i]);
            }


          File path = new File(Environment.getExternalStorageDirectory()+"/AudioMerger/");
          if(!path.exists()){
              path.mkdir();
          }
          String fileName = GlobalData.random();
            fileResult = new File(path,fileName+".mp3");
            if(!fileResult.exists()){
                fileResult.createNewFile();
            }
            Enumeration<FilterHeaderTrailerInputStream> enu = inputStreamVector.elements();
            SequenceInputStream sistream = new SequenceInputStream(enu);


            FileOutputStream fostream = new FileOutputStream(fileResult);//destinationfile


            int temp;

            while ((temp = sistream.read()) != -1) {

                fostream.write(temp);
            }
            fostream.close();
            sistream.close();
            for(int i =0;i<ar_uri.size();i++){
                fileInputStreams[i].close();
                filter[i].close();
            }

        }
        catch (IOException e){
            e.printStackTrace();

        }
        return fileResult;
    }




    private void initListeners() {
        imgUpload.setOnClickListener(this);
        iv_stop.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mediaPlayer!=null && b){

                    mediaPlayer.seekTo(i*1000);
                    seekBar.setProgress(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initWidgets() {
        imgUpload=findViewById(R.id.imgupload);
        tv_fileOne=findViewById(R.id.tv_fileOne);
        tv_fileResult=findViewById(R.id.tv_fileResult);
        tv_fileTwo=findViewById(R.id.tv_fileTwo);
        pb_progress=findViewById(R.id.pb_progress);
        iv_play=findViewById(R.id.iv_play);
        iv_stop=findViewById(R.id.iv_stop);
        mSeekBar=findViewById(R.id.seek_bar);
        rv_seekbar=findViewById(R.id.rv_seekbar);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgupload:
                mediaPlayer.reset();
                rv_seekbar.setVisibility(View.GONE);
                if(GlobalData.askPermission(MainActivity.this)){
                    Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                    startActivityForResult(intent, AUDIO_REQ_CODE);
                }
                else {
                    Toast.makeText(MainActivity.this,"Please Allow Permission",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.iv_play:
                if(isStartNew){
                try {
                    mediaPlayer=new MediaPlayer();
                    mediaPlayer.setDataSource(fileResult.getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                mediaPlayer.start();
                     seekUpdation();
                    isStartNew=!isStartNew;
                }
                else {
                    if(!isPlaying){
                        iv_play.setImageResource(R.drawable.ic_action_play);
                        mediaPlayer.pause();
                        len = mediaPlayer.getCurrentPosition();
                        mSeekBar.setEnabled(false);


                    }else{
                        iv_play.setImageResource(R.drawable.ic_action_pause);
                        mediaPlayer.seekTo(len);
                        mediaPlayer.start();
                        mSeekBar.setEnabled(true);

                    }
                    isPlaying = !isPlaying;

                }



                break;
            case R.id.iv_stop:
                stopPlaying();
                break;

        }
    }

    protected void stopPlaying(){
        // If media player is not null then try to stop it
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isStartNew=true;
            isPlaying = !isPlaying;
            iv_play.setImageResource(R.drawable.ic_action_pause);


            if(seekHandler!=null){
                seekHandler.removeCallbacks(mRunnable);
            }

        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==AUDIO_REQ_CODE){

                String s;
                ClipData clipData = data.getClipData();


                if(clipData == null){
                 Toast.makeText(MainActivity.this, R.string.toast_msg,Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this,"You have selected "+clipData.getItemCount()+" files.",Toast.LENGTH_LONG).show();
                    s = "clipData != null\n";
                    for(int i=0; i<clipData.getItemCount(); i++){
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();

                        ar_file.add(uri);
                        s += uri.toString() + "\n";

                    }
                    tv_fileOne.setText(ar_file.get(0).getPath());
                    tv_fileTwo.setText(ar_file.get(1).toString());

                        new LongOperation(ar_file).execute("");


                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private class LongOperation extends AsyncTask<String, Void, String> {
        File file;
        ArrayList<Uri> arrayList=new ArrayList<>();
        LongOperation(ArrayList<Uri> arrayList){
        this.arrayList=arrayList;

        }
        @Override
        protected String doInBackground(String... params) {

                file=mergeAudio(arrayList);





            return "Executed";
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            ar_file.clear();

            pb_progress.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this,"Song Merged",Toast.LENGTH_LONG).show();
            tv_fileResult.setText("Your file is created at the following location:\n"+file.getAbsolutePath());
            mediaPlayer = new MediaPlayer();
           try {
               mediaPlayer.setDataSource(file.getPath());
               mediaPlayer.prepare();
               mediaPlayer.start();


               mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                   @Override
                   public void onPrepared(MediaPlayer mediaPlayer) {
                       rv_seekbar.setVisibility(View.VISIBLE);
                       mSeekBar.setMax(mediaPlayer.getDuration());
                       Log.e("Duration",""+mediaPlayer.getDuration());
                   }
               });
               seekUpdation();
           }catch (IOException e){
               e.printStackTrace();
           }



        }






        @Override
        protected void onPreExecute() {
            pb_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    public void seekUpdation() {
        mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                seekUpdation();
            }
        }, 1000);
    }



}
