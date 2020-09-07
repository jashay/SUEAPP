package com.example.sue;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Uri uri;
    Button btn_selectFile;
    Intent myFileIntent;

    public static final String URI_STRING = "com.example.sue.URI_STRING";


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 7:

                if (resultCode == RESULT_OK) {

                    uri = data.getData();
                    openMapActivity(uri.toString());

                }
                break;

        }

    }

    void openMapActivity(String s){
        Intent intent = new Intent(this, KmlActivity.class);
        intent.putExtra(URI_STRING,s);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);


        btn_selectFile = (Button)findViewById(R.id.btn_selectFile);
        btn_selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent, 7);

            }
        });




    }
}