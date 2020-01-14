package com.titvt.fulizhan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final int version = 2;
    Fragment home = new HomeActivity(),
            web_view = new WebViewActivity(),
            remote_list = new RemoteListActivity(),
            current;
    @BindView(R.id.drawerlayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation)
    NavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, home).add(R.id.fragment, web_view).hide(web_view).add(R.id.fragment, remote_list).hide(remote_list).commit();
        current = home;
        navigation.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    getSupportFragmentManager().beginTransaction().hide(current).show(home).commit();
                    current = home;
                    break;
                case R.id.menu_web:
                    getSupportFragmentManager().beginTransaction().hide(current).show(web_view).commit();
                    current = web_view;
                    break;
                case R.id.menu_remote:
                    getSupportFragmentManager().beginTransaction().hide(current).show(remote_list).commit();
                    current = remote_list;
                    break;
                case R.id.menu_github:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Titvt")));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        new Thread() {
            @Override
            public void run() {
                checkVersion();
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
                drawerLayout.openDrawer(GravityCompat.START);
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void checkVersion() {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("https://www.titvt.com/flz/version.php").openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(1000);
            httpURLConnection.setReadTimeout(1000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                if (Integer.parseInt(bufferedReader.readLine()) > version) {
                    final String latestUri = bufferedReader.readLine(),
                            latestInfo = bufferedReader.readLine();
                    Looper.prepare();
                    new AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(latestUri)));
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }).setTitle("发现新版本").setMessage(latestInfo).setCancelable(false).show();
                    Looper.loop();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
