package com.appomart.erlyvideo_android.Common;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appomart.erlyvideo_android.CamsActivity;
import com.appomart.erlyvideo_android.LoginActivity;
import com.appomart.erlyvideo_android.R;

public class NavigationTab {
    private ImageView exit;
    private ImageView back;

    private TextView title;

    public NavigationTab(View view){
        exit  = view.findViewById(R.id.exit);
        back  = view.findViewById(R.id.back);
        title  = view.findViewById(R.id.title);
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void initLogin(final Context context){
        exit.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        title.setText(context.getResources().getText(R.string.title_login));
    }

    public void initList(final Context context){
        exit.setVisibility(View.VISIBLE);
        back.setVisibility(View.GONE);
        title.setText(context.getResources().getText(R.string.title_list));
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        });
    }

    public void initCam(final Context context, String login, String password){
        exit.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        title.setText(context.getResources().getText(R.string.title_cam));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CamsActivity.class);
                intent.putExtra("Login", login);
                intent.putExtra("Password", password);
                context.startActivity(intent);
            }
        });
    }
}
