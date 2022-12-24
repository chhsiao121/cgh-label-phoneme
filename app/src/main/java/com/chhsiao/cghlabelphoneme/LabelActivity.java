package com.chhsiao.cghlabelphoneme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class LabelActivity extends AppCompatActivity implements View.OnClickListener{
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

    Button outlinedBtnAddError;
    TextView textViewPhoneme;
    ImageButton imageBackButton;
    ArrayList <String> phonemeList;
    String [] errorPhoneList =   {"ㄅ : p","ㄆ : pʰ","ㄇ : m","ㄈ : f","ㄉ : t","ㄊ : tʰ","ㄋ : n","ㄌ : l","ㄍ : k","ㄎ : kʰ","ㄏ : x","ㄐ : tɕ",
            "ㄑ : tʰɕ","ㄒ : ɕ","ㄓ : tʂ","ㄔ : tʰʂ","ㄕ : ʂ","ㄖ : ʐ","ㄗ : ts","ㄘ : tsʰ","ㄙ : s","ㄧ : i","ㄨ : u","ㄩ : y","ㄚ : a","ㄛ : o",
            "ㄜ : ɤ","ㄝ : e","ㄞ : ai","ㄟ : ei","ㄠ : au","ㄡ : ou","ㄢ : an","ㄣ : ən","ㄤ : aŋ","ㄥ : əŋ","err"};
    String [] canonicalList =   {"ㄅ : p","ㄆ : pʰ","ㄇ : m","ㄈ : f","ㄉ : t","ㄊ : tʰ","ㄋ : n","ㄌ : l","ㄍ : k","ㄎ : kʰ","ㄏ : x","ㄐ : tɕ",
            "ㄑ : tʰɕ","ㄒ : ɕ","ㄓ : tʂ","ㄔ : tʰʂ","ㄕ : ʂ","ㄖ : ʐ","ㄗ : ts","ㄘ : tsʰ","ㄙ : s","ㄧ : i","ㄨ : u","ㄩ : y","ㄚ : a","ㄛ : o",
            "ㄜ : ɤ","ㄝ : e","ㄞ : ai","ㄟ : ei","ㄠ : au","ㄡ : ou","ㄢ : an","ㄣ : ən","ㄤ : aŋ","ㄥ : əŋ"};
    String [] errorPhoneList_h = {"p","ph","m","f","t","th","n","l","k","kh","x","tɕ","thɕ","ɕ","tʂ","thʂ","ʂ","ʐ","ts","tsh","s","i","u","y","a","o","ɤ","e","ai","ei","au","ou","an","ən","aŋ","əŋ","sil","err"};

    String [] errorTypeList = {"substitution","addition","deletion"};
    String [] silenceList = {"sil"};
    AutoCompleteTextView canonicalPhoneTxt, perceivedPhoneTxt,errorTypeTxt;
    ArrayAdapter<String> adapterCanonicalPhone,adapterPhone,adapterErrorTypes,adapterSilPhone;

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
        Button outlinedBtn_p = findViewById(R.id.outlinedButton_p);
        outlinedBtn_p.setOnClickListener(this);
        Button outlinedBtn_ph = findViewById(R.id.outlinedButton_ph);
        outlinedBtn_ph.setOnClickListener(this);
        Button outlinedBtn_m = findViewById(R.id.outlinedButton_m);
        outlinedBtn_m.setOnClickListener(this);
        Button outlinedBtn_f = findViewById(R.id.outlinedButton_f);
        outlinedBtn_f.setOnClickListener(this);
        Button outlinedBtn_t = findViewById(R.id.outlinedButton_t);
        outlinedBtn_t.setOnClickListener(this);
        Button outlinedBtn_th = findViewById(R.id.outlinedButton_th);
        outlinedBtn_th.setOnClickListener(this);
        Button outlinedBtn_n = findViewById(R.id.outlinedButton_n);
        outlinedBtn_n.setOnClickListener(this);
        Button outlinedBtn_l = findViewById(R.id.outlinedButton_l);
        outlinedBtn_l.setOnClickListener(this);
        Button outlinedBtn_k = findViewById(R.id.outlinedButton_k);
        outlinedBtn_k.setOnClickListener(this);
        Button outlinedBtn_kh = findViewById(R.id.outlinedButton_kh);
        outlinedBtn_kh.setOnClickListener(this);
        Button outlinedBtn_x = findViewById(R.id.outlinedButton_x);
        outlinedBtn_x.setOnClickListener(this);
        Button outlinedBtn_tɕ = findViewById(R.id.outlinedButton_tɕ);
        outlinedBtn_tɕ.setOnClickListener(this);
        Button outlinedBtn_thɕ = findViewById(R.id.outlinedButton_thɕ);
        outlinedBtn_thɕ.setOnClickListener(this);
        Button outlinedBtn_ɕ = findViewById(R.id.outlinedButton_ɕ);
        outlinedBtn_ɕ.setOnClickListener(this);
        Button outlinedBtn_tʂ = findViewById(R.id.outlinedButton_tʂ);
        outlinedBtn_tʂ.setOnClickListener(this);
        Button outlinedBtn_thʂ = findViewById(R.id.outlinedButton_thʂ);
        outlinedBtn_thʂ.setOnClickListener(this);
        Button outlinedBtn_ʂ = findViewById(R.id.outlinedButton_ʂ);
        outlinedBtn_ʂ.setOnClickListener(this);
        Button outlinedBtn_ʐ = findViewById(R.id.outlinedButton_ʐ);
        outlinedBtn_ʐ.setOnClickListener(this);
        Button outlinedBtn_ts = findViewById(R.id.outlinedButton_ts);
        outlinedBtn_ts.setOnClickListener(this);
        Button outlinedBtn_tsh = findViewById(R.id.outlinedButton_tsh);
        outlinedBtn_tsh.setOnClickListener(this);
        Button outlinedBtn_s = findViewById(R.id.outlinedButton_s);
        outlinedBtn_s.setOnClickListener(this);
        Button outlinedBtn_i = findViewById(R.id.outlinedButton_i);
        outlinedBtn_i.setOnClickListener(this);
        Button outlinedBtn_u = findViewById(R.id.outlinedButton_u);
        outlinedBtn_u.setOnClickListener(this);
        Button outlinedBtn_y = findViewById(R.id.outlinedButton_y);
        outlinedBtn_y.setOnClickListener(this);
        Button outlinedBtn_a = findViewById(R.id.outlinedButton_a);
        outlinedBtn_a.setOnClickListener(this);
        Button outlinedBtn_o = findViewById(R.id.outlinedButton_o);
        outlinedBtn_o.setOnClickListener(this);
        Button outlinedBtn_ɤ = findViewById(R.id.outlinedButton_ɤ);
        outlinedBtn_ɤ.setOnClickListener(this);
        Button outlinedBtn_e = findViewById(R.id.outlinedButton_e);
        outlinedBtn_e.setOnClickListener(this);
        Button outlinedBtn_ai = findViewById(R.id.outlinedButton_ai);
        outlinedBtn_ai.setOnClickListener(this);
        Button outlinedBtn_ei = findViewById(R.id.outlinedButton_ei);
        outlinedBtn_ei.setOnClickListener(this);
        Button outlinedBtn_au = findViewById(R.id.outlinedButton_au);
        outlinedBtn_au.setOnClickListener(this);
        Button outlinedBtn_ou = findViewById(R.id.outlinedButton_ou);
        outlinedBtn_ou.setOnClickListener(this);
        Button outlinedBtn_an = findViewById(R.id.outlinedButton_an);
        outlinedBtn_an.setOnClickListener(this);
        Button outlinedBtn_ən = findViewById(R.id.outlinedButton_ən);
        outlinedBtn_ən.setOnClickListener(this);
        Button outlinedBtn_aŋ = findViewById(R.id.outlinedButton_aŋ);
        outlinedBtn_aŋ.setOnClickListener(this);
        Button outlinedBtn_əŋ = findViewById(R.id.outlinedButton_əŋ);
        outlinedBtn_əŋ.setOnClickListener(this);
        Button outlinedBtn_sil = findViewById(R.id.outlinedButton_sil);
        outlinedBtn_sil.setOnClickListener(this);
        Button outlinedBtn_err = findViewById(R.id.outlinedButton_err);
        outlinedBtn_err.setOnClickListener(this);
        outlinedBtnAddError = findViewById(R.id.outlinedButton_add_error);
        textViewPhoneme = findViewById(R.id.textViewPhoneme);
        imageBackButton = findViewById(R.id.imageBackButton);
        errorTypeTxt = findViewById((R.id.error_type_txt));
        canonicalPhoneTxt = findViewById(R.id.canonical_phone_txt);
        perceivedPhoneTxt = findViewById(R.id.perceived_phone_txt);

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
        adapterErrorTypes = new ArrayAdapter<String>(this,R.layout.list_item,errorTypeList);
        errorTypeTxt.setAdapter(adapterErrorTypes);
        errorTypeTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String errorType = adapterView.getItemAtPosition(i).toString();
                if(errorType.equals(errorTypeList[0])){
                    canonicalPhoneTxt.setAdapter(adapterCanonicalPhone);
                    perceivedPhoneTxt.setAdapter(adapterPhone);
                }
                else if(errorType.equals(errorTypeList[1])){
                    perceivedPhoneTxt.setAdapter(adapterPhone);
                    canonicalPhoneTxt.setAdapter(adapterSilPhone);
                    canonicalPhoneTxt.setText(silenceList[0]);
                }
                else if(errorType.equals(errorTypeList[2])){
                    canonicalPhoneTxt.setAdapter(adapterCanonicalPhone);
                    perceivedPhoneTxt.setAdapter(adapterSilPhone);
                    perceivedPhoneTxt.setText(silenceList[0]);
                }
            }
        });
        adapterCanonicalPhone = new ArrayAdapter<String>(this,R.layout.list_item,canonicalList);
        adapterPhone = new ArrayAdapter<String>(this,R.layout.list_item,errorPhoneList);
        adapterSilPhone = new ArrayAdapter<String>(this,R.layout.list_item,silenceList);
        canonicalPhoneTxt.setAdapter(adapterCanonicalPhone);
        perceivedPhoneTxt.setAdapter(adapterPhone);

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
        buttonReset.setOnClickListener(v -> {
            resetPhonemeTextview();
        });
        buttonSave.setOnClickListener(v -> {
            savePhoneme2json();
            saveJson2Phone(target_name, jsonData);
        });
        buttonPlay.setOnClickListener(v -> {
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
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

        imageBackButton.setOnClickListener(v ->{delPhonemeTextview();});
        outlinedBtnAddError.setOnClickListener(v ->{
            String canonicalPhone = canonicalPhoneTxt.getText().toString();
            String perceivedPhone = perceivedPhoneTxt.getText().toString();
            String errorType = errorTypeTxt.getText().toString();
            if(errorType.equals("")){
                Toast.makeText(this,"請先選擇Error Type!!",Toast.LENGTH_SHORT).show();
            }
            else if(canonicalPhone.equals("")){
                Toast.makeText(this,"請先選擇Canonical Phoneme!!",Toast.LENGTH_SHORT).show();
            }
            else if(perceivedPhone.equals("")){
                Toast.makeText(this,"請先選擇Perceived Phoneme!!",Toast.LENGTH_SHORT).show();
            }
            else{

                String tmp = null;
                if(errorType.equals(errorTypeList[0])){
                    tmp = "s";
                }
                else if(errorType.equals(errorTypeList[1])){
                    tmp = "a";
                }
                else if(errorType.equals(errorTypeList[2])){
                    tmp = "d";
                }
                if(perceivedPhone.contains(":")){
                    perceivedPhone = perceivedPhone.split(":")[1];
                    perceivedPhone = perceivedPhone.trim();
                    perceivedPhone = perceivedPhone.replace("ʰ","h");
                }
                if(canonicalPhone.contains(":")){
                    canonicalPhone = canonicalPhone.split(":")[1];
                    canonicalPhone = canonicalPhone.trim();
                    canonicalPhone = canonicalPhone.replace("ʰ","h");
                }
                addPhonemeTextview("<"+canonicalPhone+","+perceivedPhone+","+tmp+">");

            }
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
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.outlinedButton_p:
                addPhonemeTextview("p");
                break;
            case R.id.outlinedButton_ph:
                addPhonemeTextview("ph");
                break;
            case R.id.outlinedButton_m:
                addPhonemeTextview("m");
                break;
            case R.id.outlinedButton_f:
                addPhonemeTextview("f");
                break;
            case R.id.outlinedButton_t:
                addPhonemeTextview("t");
                break;
            case R.id.outlinedButton_th:
                addPhonemeTextview("th");
                break;
            case R.id.outlinedButton_n:
                addPhonemeTextview("n");
                break;
            case R.id.outlinedButton_l:
                addPhonemeTextview("l");
                break;
            case R.id.outlinedButton_k:
                addPhonemeTextview("k");
                break;
            case R.id.outlinedButton_kh:
                addPhonemeTextview("kh");
                break;
            case R.id.outlinedButton_x:
                addPhonemeTextview("x");
                break;
            case R.id.outlinedButton_tɕ:
                addPhonemeTextview("tɕ");
                break;
            case R.id.outlinedButton_thɕ:
                addPhonemeTextview("thɕ");
                break;
            case R.id.outlinedButton_ɕ:
                addPhonemeTextview("ɕ");
                break;
            case R.id.outlinedButton_tʂ:
                addPhonemeTextview("tʂ");
                break;
            case R.id.outlinedButton_thʂ:
                addPhonemeTextview("thʂ");
                break;
            case R.id.outlinedButton_ʂ:
                addPhonemeTextview("ʂ");
                break;
            case R.id.outlinedButton_ʐ:
                addPhonemeTextview("ʐ");
                break;
            case R.id.outlinedButton_ts:
                addPhonemeTextview("ts");
                break;
            case R.id.outlinedButton_tsh:
                addPhonemeTextview("tsh");
                break;
            case R.id.outlinedButton_s:
                addPhonemeTextview("s");
                break;
            case R.id.outlinedButton_i:
                addPhonemeTextview("i");
                break;
            case R.id.outlinedButton_u:
                addPhonemeTextview("u");
                break;
            case R.id.outlinedButton_y:
                addPhonemeTextview("y");
                break;
            case R.id.outlinedButton_a:
                addPhonemeTextview("a");
                break;
            case R.id.outlinedButton_o:
                addPhonemeTextview("o");
                break;
            case R.id.outlinedButton_ɤ:
                addPhonemeTextview("ɤ");
                break;
            case R.id.outlinedButton_e:
                addPhonemeTextview("e");
                break;
            case R.id.outlinedButton_ai:
                addPhonemeTextview("ai");
                break;
            case R.id.outlinedButton_ei:
                addPhonemeTextview("ei");
                break;
            case R.id.outlinedButton_au:
                addPhonemeTextview("au");
                break;
            case R.id.outlinedButton_ou:
                addPhonemeTextview("ou");
                break;
            case R.id.outlinedButton_an:
                addPhonemeTextview("an");
                break;
            case R.id.outlinedButton_ən:
                addPhonemeTextview("ən");
                break;
            case R.id.outlinedButton_aŋ:
                addPhonemeTextview("aŋ");
                break;
            case R.id.outlinedButton_əŋ:
                addPhonemeTextview("əŋ");
                break;
            case R.id.outlinedButton_sil:
                addPhonemeTextview("sil");
                break;
            case R.id.outlinedButton_err:
                addPhonemeTextview("err");
                break;
        }
    }

//    protected void onStop() {
//        super.onStop();
//        if(mediaPlayer!=null){
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(mediaPlayer!=null){
//            mediaPlayer.reset();
//            prepareMediaPlayer();
//        }
//    }

    private String returnIndex(String path){
//        String path = "/Documents/CGH_recording/0827/data_0820word/2022.08.25.15.57.08_738115259_adult/";
        String[] tmp = path.split("/");
        String folder = tmp[tmp.length -1];
        return folder.split("_")[1];
    }
    private void savePhoneme2json(){
        String tmpPhonemeStr = textViewPhoneme.getText().toString();
        tmpPhonemeStr = tmpPhonemeStr.trim(); //最前面會有空格
        String index = returnIndex(folder_path);
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
    private void resetPhonemeTextview(){
        if(phonemeList.isEmpty()){
            Toast.makeText(this, "Annotation is empty!", Toast.LENGTH_SHORT).show();
        }else {
            phonemeList.clear();
//            phonemeList.removeAll(phonemeList);
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
        if(mediaPlayer == null){
            this.mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            prepareMediaPlayer();
        }
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
        String index = returnIndex(folder_path);
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

        textFileName.setText(fileName);
        String nn = (select_file + 1) + " / " + (select_file_max + 1);
        textFileNumber.setText(nn);
        //這裡如果tmp所得到的字卡編號，不在res/values/strings.xml所宣告的字卡中，等等getStringResourceByName所拿到的resid會有問題
//        String tmp = "wordcard" + files[select_file].getName().split("_")[1] + "_" + files[select_file].getName().split("_")[2];

        String uploadWordcardType = folder_path.split("/")[folder_path.split("/").length-2];
        String tmp = fileNameWithOutExt;
        if(uploadWordcardType.equals("data_0327word")){
            String [] wordcard0327 = {"wordcard04_06","wordcard04_06_1","wordcard04_06_2","wordcard05_05","wordcard05_05_1",
                    "wordcard05_05_2","wordcard05_06","wordcard05_06_1","wordcard05_07","wordcard05_07_1","wordcard05_08","wordcard05_08_1"};
            if(Arrays.asList(wordcard0327).contains(fileNameWithOutExt)){
                tmp = fileNameWithOutExt+"_0327";
            }

        }else if(uploadWordcardType.equals("data_oldword")){
            tmp = "oldword"+fileNameWithOutExt.split("_")[1]+"_"+fileNameWithOutExt.split("_")[2];
        }
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