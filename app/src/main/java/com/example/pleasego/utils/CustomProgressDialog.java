package com.example.pleasego.utils;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.example.pleasego.R;


public class CustomProgressDialog extends Dialog {

    private Context context;
    private ImageView imageView;
    private int theme;

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        this.theme = theme;
    }

    public CustomProgressDialog createDialog(WindowManager windowManager) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_progressdialog, null);
        imageView = (ImageView) view.findViewById(R.id.progress);
        LinearInterpolator linearInterpolator = new LinearInterpolator();//线性差值器
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0F, 360F);
        objectAnimator.setDuration(1000);
        objectAnimator.setInterpolator(linearInterpolator);
        objectAnimator.setRepeatCount(-1);
        objectAnimator.start();


        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams dialogLP = dialogWindow.getAttributes();
        Display display = windowManager.getDefaultDisplay();
        int windowHeight = display.getHeight();
        int windowWidth = display.getWidth();
        dialogLP.width = windowWidth/3;
        dialogLP.height = windowHeight/10;
        dialogLP.y = (int) (windowHeight*0.45);//往下偏移屏幕的1/4
        dialogWindow.setAttributes(dialogLP);
        this.setContentView(view);
     //   this.setCancelable(false);
        return this;
    }

}
