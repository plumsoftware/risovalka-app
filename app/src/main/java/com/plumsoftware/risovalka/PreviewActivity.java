package com.plumsoftware.risovalka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class PreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_acrivity);

        //FVBI
        ListView listView = (ListView) findViewById(R.id.listView);
        ImageButton imageButton = (ImageButton) findViewById(R.id.add);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PreviewActivity.this, LinearLayoutManager.HORIZONTAL, false);

        ArrayList<UserImage> userImageArrayList = new ArrayList<>();

        //Read data
        FileInputStream fileInputStream = null;
        try {
            try {
                fileInputStream = openFileInput("images");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Scanner scanner = new Scanner(fileInputStream);

            //Scanner read
            while (scanner.hasNextLine()) {
                String string = scanner.nextLine();
                userImageArrayList.add(new UserImage(string));
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Adapter
        ImageAdapter adapter = new ImageAdapter(PreviewActivity.this, userImageArrayList, PreviewActivity.this);
        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PreviewActivity.this, DrawActivity.class));
            }
        });
    }
}