package com.titvt.fulizhan;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.StringReader;

public class MainActivity extends AppCompatActivity {
    private static final int version = 3;
    DrawerLayout drawerLayout;
    Fragment home, web_view, remote_list, ai, translate, ncov, current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        drawerLayout = findViewById(R.id.drawerlayout);
        home = new HomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, home).commit();
        current = home;
        NavigationView navigation = findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    getSupportFragmentManager().beginTransaction().hide(current).show(home).commit();
                    current = home;
                    break;
                case R.id.menu_web:
                    if (web_view == null) {
                        web_view = new WebViewFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, web_view).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(web_view).commit();
                    current = web_view;
                    break;
                case R.id.menu_remote:
                    if (remote_list == null) {
                        remote_list = new RemoteListFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, remote_list).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(remote_list).commit();
                    current = remote_list;
                    break;
                case R.id.menu_ai:
                    if (ai == null) {
                        ai = new AIFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, ai).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(ai).commit();
                    current = ai;
                    break;
                case R.id.menu_translate:
                    if (translate == null) {
                        translate = new TranslateFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, translate).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(translate).commit();
                    current = translate;
                    break;
                case R.id.menu_ncov:
                    if (ncov == null) {
                        ncov = new NCOVFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, ncov).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(ncov).commit();
                    current = ncov;
                    break;
                case R.id.menu_info:
                    new AlertDialog.Builder(this).setPositiveButton(R.string.ok, null).setTitle("福利栈").setMessage("作者：古月浪子\nQQ：1044805408\n版本：3.14").show();
                    break;
                case R.id.menu_github:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Titvt")));
                    break;
                case R.id.menu_exit:
                    android.os.Process.killProcess(android.os.Process.myPid());
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            drawerLayout.openDrawer(GravityCompat.START);
        return true;
    }

    void checkVersion() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(new Https("https://www.titvt.com/flz/version").get()));
            if (Integer.parseInt(bufferedReader.readLine()) > version) {
                final String uri = bufferedReader.readLine();
                StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while ((temp = bufferedReader.readLine()) != null)
                    stringBuilder.append(temp).append('\n');
                Looper.prepare();
                new AlertDialog.Builder(this).setPositiveButton(R.string.ok, (dialog, which) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                    android.os.Process.killProcess(android.os.Process.myPid());
                }).setTitle("发现新版本").setMessage(stringBuilder.toString()).setCancelable(false).show();
                Looper.loop();
            }
        } catch (Exception ignored) {
        }
    }
}
