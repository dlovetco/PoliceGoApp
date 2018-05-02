package com.example.pleasego.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.pleasego.R;

/**
 * Created by dlovetco on 2017/9/26.
 */

public class AttackChooseDialog extends Dialog {

    private Context context;
    private Button useGun;
    private Button useGrenade;

    public AttackChooseDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public AttackChooseDialog createDialog(final UseWeapons useWeapons) {
        View view = LayoutInflater.from(context).inflate(R.layout.attack_choose_dialog, null);
        useGun = (Button) view.findViewById(R.id.use_gun);
        useGun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useWeapons.setUseGunButton();
            }
        });
        useGrenade = (Button) view.findViewById(R.id.use_grenade);
        useGrenade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useWeapons.setUseGrenadeButton();
            }
        });
        this.setContentView(view);
        return this;
    }
}
