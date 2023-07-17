package com.plumsoftware.risovalka;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ImageAdapter extends ArrayAdapter<UserImage> {
    private Activity activity;
    private String nameD = "";

    ImageAdapter(@NonNull Context context, @NonNull List<UserImage> objects, Activity activity) {
        super(context, 0, objects);
        this.activity = activity;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        //Convert view
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, null);

        //User label
        final UserImage userImage = getItem(position);

        //Initialise variables
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        final ArrayList<String> stringArrayList = new ArrayList<>();

        assert userImage != null;
        Uri imageUri = Uri.parse(userImage.getPath());

        try {
            Glide.with(getContext()).load(imageUri).into(image);
        } catch (Exception e) {
            e.printStackTrace();
            Glide.with(getContext()).load(R.drawable.ic_launcher_foreground).into(image);
        }

        //Read data
        FileInputStream fileInputStream = null;
        try {
            try {
                fileInputStream = getContext().openFileInput("images");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            assert fileInputStream != null;
            Scanner scanner = new Scanner(fileInputStream);

            //Scanner read
            while (scanner.hasNextLine()) {
                String string = scanner.nextLine();
                stringArrayList.add(string);
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Invert
        Collections.reverse(stringArrayList);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ImageViewer.class);
                String string = stringArrayList.get(position);
                intent.setData(Uri.parse(string));
                getContext().startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        return convertView;
    }
}
