package com.appomart.erlyvideo_android;

import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.appomart.erlyvideo_android.Common.NavigationTab;

public class MainActivity extends AppCompatActivity {

    FragmentTransaction fTrans;

    private NavigationTab navigationTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationTab = new NavigationTab(findViewById(R.id.navigation));
        navigationTab.initLogin(this);
        fTrans = getSupportFragmentManager().beginTransaction();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public NavigationTab getNavigationTab() {
        return navigationTab;
    }

    public void setNavigationTab(NavigationTab navigationTab) {
        this.navigationTab = navigationTab;
    }
}
