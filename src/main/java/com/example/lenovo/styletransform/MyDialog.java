package com.example.lenovo.styletransform;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MyDialog {
    private Activity act;
    //涓婁笅鏂囩幆澧冿紝宓屽叆寮忔暟鎹簱
    public MyDialog(Activity act) {
        super();
        this.act = act;
    }
    public Dialog showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
    //    builder.setIcon(R.drawable.youyi_logo);
    //    builder.setTitle("AI计算中");
        final View contentView = LayoutInflater.from(act).inflate(R.layout.layout, null);
        builder.setView(contentView);
        builder.setCancelable(false);
        View mview=(ImageView)contentView.findViewById(R.id.gif);
         Glide.with(act).load(R.drawable.loadinggif).into((ImageView)mview);
        return builder.show();
    }
}
