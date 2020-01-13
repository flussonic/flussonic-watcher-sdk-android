package com.appomart.erlyvideo_android;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.appomart.erlyvideo_android.Api.ServerApi;
import com.appomart.erlyvideo_android.Common.NavigationTab;
import com.appomart.erlyvideo_android.Control.ServerConnect;
import com.appomart.erlyvideo_android.Models.User;

import flussonic.watcher.sdk.domain.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    private TextView error_message;
    //private EditText server;
    private EditText login;
    private EditText password;
    private TextView btn_login;
    private NavigationTab navigationTab;
    boolean error = false;
    private User user;
    ContentLoadingProgressBar progressIndicator;

    private final ServerApi sampleNetworkService = new ServerConnect()
            .provideSampleNetworkService("https://cloud.vsaas.io");
    private Disposable loadDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        error_message = findViewById(R.id.error_message);
        error_message.setVisibility(View.INVISIBLE);
        //server = findViewById(R.id.server);
        login = findViewById(R.id.login);
        password = findViewById(R.id.password);
        btn_login = findViewById(R.id.btn_login);
        navigationTab = new NavigationTab(findViewById(R.id.navigation));
        navigationTab.initLogin(this);

        progressIndicator = findViewById(R.id.login_progress_indicator);

        user = new User();

        //server.setText("test");
        login.setText("demo");
        password.setText("demo");

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()){
                    checkEdits();
                } else {
                    error_message.setVisibility(View.VISIBLE);
                    error_message.setText(getResources().getText(R.string.error_message2));
                    return;
                }
                if (error){
                    error_message.setVisibility(View.VISIBLE);
                    error_message.setText(getResources().getText(R.string.error_message3));
                    return;
                } else {
                    user.setLogin(login.getText().toString());
                    user.setPassword(password.getText().toString());
                    checklogin(user);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private boolean checkEdits(){
        /*if (server.getText().toString().isEmpty()){
            server.setBackground(this.getResources().getDrawable(R.drawable.edit_text_error_bg));
            error = true;
        }*/
        if (login.getText().toString().isEmpty()){
            login.setBackground(this.getResources().getDrawable(R.drawable.edit_text_error_bg));
            error = true;
        }
        if (password.getText().toString().isEmpty()){
            password.setBackground(this.getResources().getDrawable(R.drawable.edit_text_error_bg));
            error = true;
        } else {
            //server.setBackground(this.getResources().getDrawable(R.drawable.edit_text_bg));
            login.setBackground(this.getResources().getDrawable(R.drawable.edit_text_bg));
            password.setBackground(this.getResources().getDrawable(R.drawable.edit_text_bg));
            error = false;
        }
        return error;
    }

    private void goCams(){
        Intent intent = new Intent(this, CamsActivity.class);
        intent.putExtra("Login", login.getText().toString());
        intent.putExtra("Password", password.getText().toString());
        this.startActivity(intent);
    }

    private void showError(Throwable throwable) {
        Log.d("Login", "ERRROR = " + throwable.getMessage());
        if (throwable.getMessage().equals("HTTP 403 Forbidden")){
            error_message.setVisibility(View.VISIBLE);
            error_message.setText(getResources().getText(R.string.error_message1));
        }
        Timber.e(throwable, "showError");
        new AlertDialog.Builder(this)
                .setTitle(throwable.getClass()
                        .getSimpleName()
                        .replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2"))
                .setMessage(throwable.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    public void check(Boolean b) {
        if (b){
            goCams();
        }
    }

    public void checklogin(User user) {
        showProgress();
        RxUtils.dispose(loadDisposable);
        loadDisposable = sampleNetworkService.checklogin(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> showProgress())
                .doFinally(this::closeProgress)
                .subscribe(this::check, this::showError);
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    protected void showProgress() {
        progressIndicator.setVisibility(View.VISIBLE);
    }

    protected void closeProgress() {
        progressIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
