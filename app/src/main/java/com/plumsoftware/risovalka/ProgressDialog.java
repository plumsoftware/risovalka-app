package com.plumsoftware.risovalka;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

public class ProgressDialog {
    private Dialog dialog;
    private Context context;

    public ProgressDialog(Context context) {
        this.context = context;
    }

    @SuppressLint("InflateParams")
    public void showDialog() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null, false);
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }
}
