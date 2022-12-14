package com.chhsiao.cghlabelphoneme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.lable2.MESSAGE";
    private static final String REQUIRED = "Required";
    private static final int PICKFILE_RESULT_CODE = 1;
    private StorageReference mStorageRef;
    private String PATH;
    private Boolean f_load = Boolean.FALSE;
    private Button buttonUpload,buttonOpen,buttonLabel,buttonExit;
    public ArrayList<String> testType;
    public ArrayList<String> wordcardType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testType = new ArrayList<>();
        testType.add("stopping");
        testType.add("backing");
        testType.add("affricate");
        testType.add("fricative");
        wordcardType = new ArrayList<>();
        wordcardType.add("data_0820word");
        wordcardType.add("data_0327word");
        wordcardType.add("data_oldword");
        int READ_EXTERNAL_STORAGE = 100;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        buttonOpen = findViewById(R.id.buttonOpen);
        buttonUpload = findViewById(R.id.buttonUploadn);
        buttonLabel = findViewById(R.id.buttonLabel);
        buttonExit = findViewById(R.id.buttonExit);
        buttonOpen.setOnClickListener(v -> openFileChooser());
        buttonLabel.setOnClickListener(this::startLabel);
        buttonUpload.setOnClickListener(v -> {
            if (networkIsConnect()) uploadFile();

            else Toast.makeText(getApplicationContext(), "???????????????????????????????????????", Toast.LENGTH_SHORT).show();
        });
        buttonExit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }
    private boolean networkIsConnect(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null){
            return networkInfo.isConnected();
        }else {
            return false;
        }
    }




    public void uploadFile() {
        TextInputLayout userNameTextInputLayout = findViewById(R.id.userNameTextInputLayout);
        String userName = userNameTextInputLayout.getEditText().getText().toString();
        if (TextUtils.isEmpty(userName)) {
            userNameTextInputLayout.setError(REQUIRED);
        } else {
            userName = userName.replaceAll("\\s", "");
            if(f_load) {
                String json_name = userName + "_" + PATH.split("/")[PATH.split("/").length - 1] + ".json";
                
                File upload_file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), json_name);
                if (upload_file.exists()) {
                    buttonUpload.setEnabled(false);
                    String uploadName = userName + "_" + json_name;
                    Uri jsonUri = Uri.fromFile(upload_file);
                    Log.e("save_name", uploadName);
                    uploadjson(jsonUri, json_name, json_name);
                } else {
                    Toast.makeText(getApplicationContext(), "???????????????????????????", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Button buttonOpen = findViewById(R.id.buttonOpen);
                buttonOpen.setError(REQUIRED);
                Toast.makeText(getApplicationContext(), "???????????????????????????", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void uploadjson(Uri jsonUri, String uploadName, String json_name){
        int uploadIdx = uploadName.split("_").length - 1;
        String uploadTestType = uploadName.split("_")[uploadIdx - 1];
        String uploadWordcardType = PATH.split("/")[PATH.split("/").length-2]; //data_0327word
        StorageReference Ref;
        if(testType.contains(uploadTestType)){
            Ref = mStorageRef.child("test/"+uploadName);
        }
        else if (wordcardType.contains(uploadWordcardType)){
            Ref = mStorageRef.child(uploadWordcardType+"/"+uploadName);
        }
        else {
            Ref = mStorageRef.child(uploadName);
        }


        UploadTask uploadTask = Ref.putFile(jsonUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                buttonUpload.setEnabled(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(json_name + "??????????????????????????????????????????????????????");
                builder.setTitle("????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), json_name + "??????????????????????????????????????????:m11002129@mail.ntust.edu.tw", Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                buttonUpload.setEnabled(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(uploadName + "???????????????");
                builder.setTitle("????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), uploadName + "????????????", Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    public void startLabel(View view) {
        if (f_load) {
            TextInputLayout userNameTextInputLayout = findViewById(R.id.userNameTextInputLayout);
            String userName = userNameTextInputLayout.getEditText().getText().toString();
            if (TextUtils.isEmpty(userName)) {
                userNameTextInputLayout.setError(REQUIRED);
            }else{
                userName = userName.replaceAll("\\s", "");
                Intent intent = new Intent(this, LabelActivity.class);
                intent.putExtra(EXTRA_MESSAGE, PATH);
                intent.putExtra("ST_name",userName);
                startActivity(intent);
            }
        } else {
            Button buttonOpen = findViewById(R.id.buttonOpen);
            buttonOpen.setError(REQUIRED);
            Toast.makeText(getApplicationContext(), "???????????????????????????", Toast.LENGTH_LONG).show();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            PATH = data.getData().getPath();
//            Log.e("test", "PATH"+PATH);
            String[] bits = PATH.split("/");
            PATH = PATH.substring(0, PATH.length() - bits[bits.length - 1].length());
//            Log.e("test", "PATH"+PATH);
            PATH = "/" + PATH.split(":")[1];
//            Log.e("test", "PATH"+PATH);
            setText("?????????????????????: " + PATH);
            f_load = Boolean.TRUE;
        }
    }

    public void setText(String text) {
        TextView textView = findViewById(R.id.textPath);
        textView.setText(text);
    }

}