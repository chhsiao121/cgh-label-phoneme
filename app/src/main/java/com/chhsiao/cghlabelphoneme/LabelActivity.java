package com.chhsiao.cghlabelphoneme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.io.ByteStreams;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LabelActivity extends AppCompatActivity {
    private File[] files;
    private MediaPlayer mediaPlayer;
    private String folder_path;
    private String ST;
    public int select_file = 0;
    public int select_file_max = 0;
    public boolean f_first_play = true;
    public String target_name;
    private TextView textCurrentTime,textTotalDuration;
    private SeekBar playerSeekBar;
    private Handler handler = new Handler();
    private float playSpeed = 1.00F;
    public JSONObject jsonData;
    Button outlinedBtn_ph;
    Button outlinedBtn_m;
    Button outlinedBtn_t;
    Button outlinedBtn_f;
    Button outlinedBtn_k;
    Button btnDialog;
    TextView textViewPhoneme;
    ImageButton imageBackButton;
    ArrayList <String> phonemeList;
    String [] errorPhoneList = {"ph","m","t","f","k"};
    String [] testList = {"test","test1","test2","test3"};
    String pickedDialogItem;
    private int errorIdxId = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);


        FloatingActionButton buttonSave = findViewById(R.id.buttonSave);
        FloatingActionButton buttonPlay = findViewById(R.id.buttonPlay);
        FloatingActionButton buttonNext = findViewById(R.id.buttonNext);
        FloatingActionButton buttonPrevious = findViewById(R.id.buttonPrevious);
        FloatingActionButton buttonReset = findViewById(R.id.buttonReset);
        EditText editIndex = findViewById(R.id.editIndex);
        Spinner spinner1 = findViewById(R.id.speedOptions);
        playerSeekBar = findViewById(R.id.playerSeekBar);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalDuration = findViewById(R.id.textTotalDuration);
        outlinedBtn_ph = findViewById(R.id.outlinedButton_ph);
        outlinedBtn_m = findViewById(R.id.outlinedButton_m);
        outlinedBtn_t = findViewById(R.id.outlinedButton_t);
        outlinedBtn_f = findViewById(R.id.outlinedButton_f);
        outlinedBtn_k = findViewById(R.id.outlinedButton_k);
        textViewPhoneme = findViewById(R.id.textViewPhoneme);
        imageBackButton = findViewById(R.id.imageBackButton);
        btnDialog = findViewById(R.id.btnDialog);
        Intent intent = getIntent();
        folder_path = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        ST = intent.getStringExtra("ST_name");
        playerSeekBar.setMax(1000);
        mediaPlayer = new MediaPlayer();
        jsonData = new JSONObject();
        phonemeList = new ArrayList<>();
        checkJsonFile();
        loadFileList();
        prepareMediaPlayer();

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this
                ,R.array.speeds_array,android.R.layout.simple_dropdown_item_1line);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                switch (item) {
                    case "1.00":
                        playSpeed = 1.00F;
                        break;
                    case "0.75":
                        playSpeed = 0.75F;
                        break;
                    case "0.50":
                        playSpeed = 0.50F;
                        break;
                    case "0.25":
                        playSpeed = 0.25F;
                        break;
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        buttonSave.setOnClickListener(v -> {
            savePhoneme2json();
            saveJson2Phone(target_name, jsonData);
        });
        buttonPlay.setOnClickListener(v -> {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                buttonPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }else{
                setSpeed(playSpeed);
                mediaPlayer.start();
                buttonPlay.setImageResource(R.drawable.ic_baseline_pause_24);
                updateSeekBar();
            }
        });

        buttonNext.setOnClickListener(v -> {
            savePhoneme2json();
            if (!TextUtils.isEmpty(editIndex.getText())) {
                select_file = Integer.parseInt(editIndex.getText().toString()) - 1;
                if (select_file > select_file_max)
                    select_file = select_file_max;
                else if (select_file < 1)
                    select_file = 0;
            } else {
                if (select_file < select_file_max)
                    select_file = select_file + 1;
            }
            mediaPlayer.reset();
            update_text_selectGroup();
            prepareMediaPlayer();
        });
        buttonPrevious.setOnClickListener(v -> {
            savePhoneme2json();
            if (!TextUtils.isEmpty(editIndex.getText())) {
                select_file = Integer.parseInt(editIndex.getText().toString()) - 1;
                if (select_file > select_file_max)
                    select_file = select_file_max;
                else if (select_file < 1)
                    select_file = 0;
            } else {
                if (select_file > 0)
                    select_file = select_file - 1;
            }
            mediaPlayer.reset();
            update_text_selectGroup();
            prepareMediaPlayer();
        });

        outlinedBtn_ph.setOnClickListener(v ->{
            addPhonemeTextview("ph");
        });
        outlinedBtn_m.setOnClickListener(v ->{
            addPhonemeTextview("m");
        });
        outlinedBtn_t.setOnClickListener(v ->{
            addPhonemeTextview("t");
        });
        outlinedBtn_f.setOnClickListener(v ->{
            addPhonemeTextview("f");
        });
        outlinedBtn_k.setOnClickListener(v ->{
            addPhonemeTextview("k");
        });
        imageBackButton.setOnClickListener(v ->{delPhonemeTextview();});

        btnDialog.setOnClickListener(v ->{
            confirmationDialogOpen(errorPhoneList);
            confirmationDialogOpen(testList);
//            Toast.makeText(this, pickedDialogItem, Toast.LENGTH_SHORT).show();
        });
//        playerSeekBar.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                SeekBar seekBar = (SeekBar) view;
//                int playPosition = (mediaPlayer.getDuration()/1000) * seekBar.getProgress();
//                mediaPlayer.seekTo(playPosition);
//                textCurrentTime.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
//                return false;
//            }
//        });


        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                playerSeekBar.setSecondaryProgress(i);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playerSeekBar.setProgress(0);
                buttonPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                textCurrentTime.setText(R.string.zero);
                textTotalDuration.setText(R.string.zero);
                mp.reset();
                prepareMediaPlayer();

            }
        });



    }

    protected void onStop() {
        super.onStop();
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }
    public void confirmationDialogOpen(String [] itemList){
        String [] selectedItem = {itemList[errorIdxId]};
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmation Dialog")
                .setSingleChoiceItems(itemList, errorIdxId, (dialogInterface, which) -> {
                    errorIdxId = which;
                    selectedItem[0] = itemList[which];
                })
                .setPositiveButton("OK", (dialogInterface, which) ->
                        pickedDialogItem = selectedItem[0])
                .setNegativeButton("Cancel", (dialogInterface, which) ->{
                    pickedDialogItem = null;
                    Toast.makeText(LabelActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                });
        builder.show();
    }
    private String returnIndex(){
        String path = "/Documents/CGH_recording/0827/data_0820word/2022.08.25.15.57.08_738115259_adult/";
        String[] tmp = path.split("/");
        String folder = tmp[tmp.length -1];
        return folder.split("_")[1];
    }
    private void savePhoneme2json(){
        String tmpPhonemeStr = textViewPhoneme.getText().toString();
        String index = returnIndex();
        String fileName = files[select_file].getName();
        String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
        try {
            jsonData.put(index+"_"+fileNameWithOutExt, tmpPhonemeStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void addPhonemeTextview(String phoneme){

        phonemeList.add(phoneme);
        String join = StringUtils.join(phonemeList," ");
        textViewPhoneme.setText(join);
    }
    private void delPhonemeTextview(){
        if(phonemeList.isEmpty()){
            Toast.makeText(this, "Annotation is empty!", Toast.LENGTH_SHORT).show();
        }else {
            phonemeList.remove(phonemeList.size() - 1);
            String join = StringUtils.join(phonemeList, " ");
            textViewPhoneme.setText(join);
        }
    }

    private void checkJsonFile(){//1.建立json檔(如果json檔不存在)
        int READ_EXTERNAL_STORAGE = 100;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);

        String path = Environment.getExternalStorageDirectory().toString() + folder_path;
        target_name = ST + "_" + path.split("/")[path.split("/").length - 1] + ".json";
        if (!hasExternalStoragePrivateJson(target_name)) {
            createExternalStoragePrivateJson(target_name);
        } else {
            try {
                jsonData = new JSONObject(readJsonFromPhone(target_name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public String readJsonFromPhone(String filename) {
        File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, filename);
        String line = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            line = new String(ByteStreams.toByteArray(fileInputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    void createExternalStoragePrivateJson(String filename) {
        File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        AssetManager assetManager = getAssets();
        File file = new File(path, filename);
        try {
            InputStream is = assetManager.open("new.json");
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException ignored) {
        }
    }

    boolean hasExternalStoragePrivateJson(String filename) {
        // Create a path where we will place our picture in the user's
        // public pictures directory and check if the file exists.  If
        // external storage is not currently mounted this will think the
        // picture doesn't exist.
        File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (path != null) {
            File file = new File(path, filename);
            return file.exists();
        }
        return false;
    }

    public void saveJson2Phone(String filename, @NonNull JSONObject JsonObject) {
        String userString = JsonObject.toString();
        File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, filename);
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(userString);
            bufferedWriter.close();
            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareMediaPlayer(){
        try {
            Uri myUri = Uri.fromFile(files[select_file]);
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
            textTotalDuration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

        }catch (Exception e){
            e.printStackTrace();
//            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long currentDuration = mediaPlayer.getCurrentPosition();
            textCurrentTime.setText(milliSecondsToTimer(currentDuration));
        }
    };
    public void setSpeed(float speed) {
        PlaybackParams pp = mediaPlayer.getPlaybackParams();
        pp.setSpeed(speed);
        mediaPlayer.setPlaybackParams(pp);
    }
    private void updateSeekBar(){
        if(mediaPlayer.isPlaying()){
            playerSeekBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 1000));
            handler.postDelayed(updater,1);
        }
    }
    private String milliSecondsToTimer(long milliSeconds){
        String timerString = "";
        String tenmilliSecondsString;
        int seconds = (int)(milliSeconds/1000);
        int tenmilliSeconds = (int)((milliSeconds%1000)/10);
        if(seconds > 0){
            timerString = seconds + ":";
        }else{
            timerString = "0:";
        }
        if(tenmilliSeconds < 10 ){
            tenmilliSecondsString = "0" + tenmilliSeconds;
        }else{
            tenmilliSecondsString = "" + tenmilliSeconds;
        }
        timerString = timerString  + tenmilliSecondsString;
        return timerString;
    }
    public void loadFileList() { //1.建立json檔(如果json檔不存在) 2.建立files陣列，裡面存了所有音檔的絕對路徑 3.呼叫update_text_selectGroup更新介面(並讀取其內容存在jsonData)
        int READ_EXTERNAL_STORAGE = 100;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);

        String path = Environment.getExternalStorageDirectory().toString() + folder_path;
        File directory = new File(path);
        files = directory.listFiles(new WavFileFilter());
        assert files != null;
        Arrays.sort(files);
        List<File> files_list = new ArrayList<>();
        for (File file : files) {
            /*It's assumed that all file in the path are in supported type*/
            String filePath = file.getPath();
            String fileName = file.getName();
            String fileTyle = fileName.substring(fileName.lastIndexOf("."));
            if (fileTyle.equals(".wav")) {
                files_list.add(file);
            }
        }
        files = files_list.toArray(new File[0]); //把ArrayList轉成File[](files_list.get(1) = files[1])

        if ((directory.canRead()) && (files != null) && (files.length != 0)) {
            select_file_max = files.length - 1;
            update_text_selectGroup();
        }else{
            Toast.makeText(this, "親，選擇資料夾裡面沒有.wav音檔喔，APP會壞掉", Toast.LENGTH_SHORT).show();
        }
    }
    public void update_text_selectGroup() { //靠select_file設定label介面上的音檔名稱、字卡名稱、第幾個音檔，並更新ChipGroup裡的label
        TextView textFileName = findViewById(R.id.textFileName);
        TextView textFileNumber = findViewById(R.id.textFileNumber);
        TextView textClassName = findViewById(R.id.textClassName);
        EditText editIndex = findViewById(R.id.editIndex);
        editIndex.setText(null);
        textViewPhoneme.setText(null);
        phonemeList = new ArrayList<>();
        String index = returnIndex();
        String fileName = files[select_file].getName();
        String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);

        try {
            String data = jsonData.get(index+"_"+fileNameWithOutExt).toString();//jsonData.get("16_36_1_k83_1n.wav")會拿到"16_36_1_k83_1n.wav"所對應的label
            String str[]= data.split(" ");
            phonemeList =  new ArrayList<>(Arrays.asList(str));
            String join = StringUtils.join(phonemeList," ");
            textViewPhoneme.setText(join);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        textFileName.setText(files[select_file].getName());
        String nn = (select_file + 1) + " / " + (select_file_max + 1);
        textFileNumber.setText(nn);
        //這裡如果tmp所得到的字卡編號，不在res/values/strings.xml所宣告的字卡中，等等getStringResourceByName所拿到的resid會有問題
//        String tmp = "wordcard" + files[select_file].getName().split("_")[1] + "_" + files[select_file].getName().split("_")[2];
        String tmp = files[select_file].getName();
        tmp = tmp.substring(0,tmp.lastIndexOf("."));
        textClassName.setText(getStringResourceByName(tmp));

    }


    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }
    public static class WavFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[] { "wav"};

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}