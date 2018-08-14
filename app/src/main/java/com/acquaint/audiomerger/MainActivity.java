package com.acquaint.audiomerger;

import android.content.ClipData;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView imgUpload;
    TextView tv_fileOne,tv_fileTwo,tv_fileResult;
    ProgressBar pb_progress;
    public static int AUDIO_REQ_CODE=1;
    ArrayList<Uri> ar_file= new ArrayList<>();
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        initListeners();
      //  mergeAudio();
    }

    private File mergeAudio(ArrayList<Uri> ar_uri) {
        List<File> file=new ArrayList<>();
        File file3=null;
        try {
           File file1=new File(ImagePathMarshmallow.getPath(MainActivity.this,ar_uri.get(0)));
           File file2=new File(ImagePathMarshmallow.getPath(MainActivity.this,ar_uri.get(1)));

          File path = new File(Environment.getExternalStorageDirectory()+"/AudioMerger/");
          if(!path.exists()){
              path.mkdir();
          }
           file3 = new File(path,"temp.mp3");
            if(!file3.exists()){
                file3.createNewFile();
            }
          /* // File file1= new File("/storage/emulated/0/MRingtoneMaker/ringtones/Myringtone.mp3");
            File file1= new File("/storage/emulated/0/JioChat/Channel_Sound_Files/during-question-new.mp3");
            file.add(file1);

            File file2=new File("/storage/emulated/0/JioChat/Channel_Sound_Files/during-question-new.mp3");
            file.add(file2);
*/
          /*  MediaExtractor mex = new MediaExtractor();
            MediaExtractor mex2 = new MediaExtractor();
            try {
                mex.setDataSource(file1.getPath());// the adresss location of the sound on sdcard.
                mex2.setDataSource(file2.getPath());// the adresss location of the sound on sdcard.
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            MediaFormat mf = mex.getTrackFormat(0);
            MediaFormat mf2 = mex2.getTrackFormat(0);

            int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
            int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            Log.e("Sample Data One ","bitrate"+bitRate+"sampleRate"+sampleRate+"channelCount"+channelCount);
            int bitRate2 = mf2.getInteger(MediaFormat.KEY_BIT_RATE);
            int sampleRate2 = mf2.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount2 = mf2.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            Log.e("Sample Data Two","bitrate"+bitRate2+"sampleRate"+sampleRate2+"channelCount"+channelCount2);

*/

            FileInputStream fistream1 = new FileInputStream(file1);  // first source file
            FilterHeaderTrailerInputStream filter1= new FilterHeaderTrailerInputStream(fistream1);
            FileInputStream fistream2 = new FileInputStream(file2);//second source file
            FilterHeaderTrailerInputStream filter2= new FilterHeaderTrailerInputStream(fistream2);
            SequenceInputStream sistream = new SequenceInputStream(filter1, filter2);
            FileOutputStream fostream = new FileOutputStream(file3);//destinationfile
//            tv_fileResult.setText(file3.getAbsolutePath());

            int temp;

            while ((temp = sistream.read()) != -1) {
                // System.out.print( (char) temp ); // to print at DOS prompt
                fostream.write(temp);   // to write to file
            }
            fostream.close();
            sistream.close();
            fistream1.close();
            fistream2.close();
        }
        catch (IOException e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Exception"+e,Toast.LENGTH_LONG).show();
        }
        return file3;
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Audio.Media.DATA };

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        CursorLoader cursorLoader = new CursorLoader(
                this,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void initListeners() {
        imgUpload.setOnClickListener(this);
    }

    private void initWidgets() {
        imgUpload=findViewById(R.id.imgupload);
        tv_fileOne=findViewById(R.id.tv_fileOne);
        tv_fileResult=findViewById(R.id.tv_fileResult);
        tv_fileTwo=findViewById(R.id.tv_fileTwo);
        pb_progress=findViewById(R.id.pb_progress);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgupload:
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(intent, AUDIO_REQ_CODE);
              /*  Intent.createChooser(intent,"Select Audio ")*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==AUDIO_REQ_CODE){

                String s;
                ClipData clipData = data.getClipData();

                //Both approach work

                if(clipData == null){
                    s = "clipData == null\n";
                    s += data.getData().toString();
                }else{
                    s = "clipData != null\n";
                    for(int i=0; i<clipData.getItemCount(); i++){
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();

                        ar_file.add(uri);
                        s += uri.toString() + "\n";

                    }
                    tv_fileOne.setText(ar_file.get(0).getPath());
                    tv_fileTwo.setText(ar_file.get(1).toString());
                  //  mergeAudio();
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

        @Override
        protected void onPostExecute(String result) {
            pb_progress.setVisibility(View.GONE);
           Toast.makeText(MainActivity.this,"Song Merged",Toast.LENGTH_LONG).show();
           tv_fileResult.setText("Song Merged");
           mediaPlayer = new MediaPlayer();
           try {
               mediaPlayer.setDataSource(file.getPath());
               mediaPlayer.prepare();
           }catch (IOException e){
               e.printStackTrace();
           }

            mediaPlayer.start();
        }

        @Override
        protected void onPreExecute() {
            pb_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }





}
