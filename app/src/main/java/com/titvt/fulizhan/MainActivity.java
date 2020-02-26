package com.titvt.fulizhan;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.JsonReader;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.titvt.fulizhan.ai.AIFragment;
import com.titvt.fulizhan.home.HomeFragment;
import com.titvt.fulizhan.remote.RemoteListFragment;
import com.titvt.fulizhan.setting.SettingFragment;
import com.titvt.fulizhan.translate.TranslateBinder;
import com.titvt.fulizhan.translate.TranslateRecord;
import com.titvt.fulizhan.translate.TranslateService;
import com.titvt.fulizhan.web.WebFragment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private static final int version = 4;
    public String language;
    public int quality, offset;
    public HomeFragment home;
    public WebFragment web;
    public RemoteListFragment remote;
    public AIFragment ai;
    public SettingFragment setting;
    public Fragment current;
    private Menu menu;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigation;
    private boolean translate;
    private TranslateBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkVersion();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        drawerLayout = findViewById(R.id.drawerlayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_home);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigation = findViewById(R.id.navigation);
        navigation.setCheckedItem(R.id.menu_home);
        navigation.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    toolbar.setTitle(R.string.menu_home);
                    menu.clear();
                    menu.add(R.string.menu_refresh);
                    getSupportFragmentManager().beginTransaction().hide(current).show(home).commit();
                    current = home;
                    break;
                case R.id.menu_web:
                    toolbar.setTitle(R.string.menu_web);
                    menu.clear();
                    menu.add(R.string.menu_refresh);
                    if (web == null) {
                        web = new WebFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, web).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(web).commit();
                    current = web;
                    break;
                case R.id.menu_remote:
                    toolbar.setTitle(R.string.menu_remote);
                    menu.clear();
                    if (remote == null) {
                        remote = new RemoteListFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, remote).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(remote).commit();
                    current = remote;
                    break;
                case R.id.menu_ai:
                    toolbar.setTitle(R.string.menu_ai);
                    menu.clear();
                    menu.add(R.string.menu_clear);
                    if (ai == null) {
                        ai = new AIFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, ai).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(ai).commit();
                    current = ai;
                    break;
                case R.id.menu_setting:
                    toolbar.setTitle(R.string.menu_setting);
                    menu.clear();
                    if (setting == null) {
                        setting = new SettingFragment();
                        getSupportFragmentManager().beginTransaction().hide(current).add(R.id.fragment, setting).commit();
                    } else
                        getSupportFragmentManager().beginTransaction().hide(current).show(setting).commit();
                    current = setting;
                    break;
                case R.id.menu_translate:
                    if (!translate) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(this)) {
                                bindService(new Intent(this, TranslateService.class), this, Context.BIND_AUTO_CREATE);
                                startActivityForResult(((MediaProjectionManager) Objects.requireNonNull(getSystemService(Context.MEDIA_PROJECTION_SERVICE))).createScreenCaptureIntent(), 1);
                            } else
                                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                        } else {
                            Toast.makeText(this, "安卓版本过低，不支持悬浮窗", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        stopService(new Intent(this, TranslateService.class));
                        unbindService(this);
                        navigation.getMenu().findItem(R.id.menu_translate).setTitle(R.string.menu_translate_on);
                        translate = false;
                    }
                    break;
                case R.id.menu_about:
                    new AlertDialog.Builder(this).setPositiveButton(R.string.ok, null).setTitle("福利栈").setMessage("作者：古月浪子\nQQ：1044805408\n版本：3.1415").show();
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
        home = new HomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, home).commit();
        current = home;
        language = getSharedPreferences("flz", Context.MODE_PRIVATE).getString("language", "en");
        quality = getSharedPreferences("flz", Context.MODE_PRIVATE).getInt("quality", 60);
        offset = getSharedPreferences("flz", Context.MODE_PRIVATE).getInt("offset", 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else if (current == home && home.wv.canGoBack()) {
                home.wv.goBack();
            } else if (current == web && web.wv.canGoBack()) {
                web.wv.goBack();
            }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        menu.add(R.string.menu_refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (current == home)
            home.wv.reload();
        else if (current == web)
            web.wv.reload();
        else if (current == ai)
            ai.clearMessage();
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (TranslateBinder) service;
        binder.setActivity(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        bindService(new Intent(this, TranslateService.class), this, Context.BIND_AUTO_CREATE);
                        startActivityForResult(((MediaProjectionManager) Objects.requireNonNull(getSystemService(Context.MEDIA_PROJECTION_SERVICE))).createScreenCaptureIntent(), 1);
                    }
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    binder.service.initScreenShot((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE), resultCode, data);
                    navigation.getMenu().findItem(R.id.menu_translate).setTitle(R.string.menu_translate_off);
                    translate = true;
                } else {
                    stopService(new Intent(this, TranslateService.class));
                    unbindService(this);
                }
        }
    }

    public void screenshot() {
        new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = binder.service.screenshot();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                ArrayList<TranslateRecord> translateRecords = new ArrayList<>();
                JsonReader jsonReader = new JsonReader(new StringReader(new Https("https://www.titvt.com/flz/translate.php").post("language=" + language + "&image=" + URLEncoder.encode(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)))));
                try {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        if (jsonReader.nextName().equals("data")) {
                            jsonReader.beginObject();
                            while (jsonReader.hasNext()) {
                                if (jsonReader.nextName().equals("image_records")) {
                                    jsonReader.beginArray();
                                    while (jsonReader.hasNext()) {
                                        String target_text = "";
                                        int x = 0, y = 0, width = 0, height = 0;
                                        jsonReader.beginObject();
                                        while (jsonReader.hasNext()) {
                                            switch (jsonReader.nextName()) {
                                                case "target_text":
                                                    target_text = jsonReader.nextString();
                                                    break;
                                                case "x":
                                                    x = jsonReader.nextInt();
                                                    break;
                                                case "y":
                                                    y = jsonReader.nextInt();
                                                    break;
                                                case "width":
                                                    width = jsonReader.nextInt();
                                                    break;
                                                case "height":
                                                    height = jsonReader.nextInt();
                                                    break;
                                                default:
                                                    jsonReader.skipValue();
                                            }
                                        }
                                        jsonReader.endObject();
                                        translateRecords.add(new TranslateRecord(target_text, x, y, width, height));
                                    }
                                    jsonReader.endArray();
                                } else
                                    jsonReader.skipValue();
                            }
                            jsonReader.endObject();
                        } else
                            jsonReader.skipValue();
                    }
                    jsonReader.endObject();
                } catch (Exception ignored) {
                }
                binder.service.screenshotCallback(translateRecords);
            }
        }.start();
    }

    private void checkVersion() {
        new Thread() {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new StringReader(new Https("https://www.titvt.com/flz/version").get()));
                    if (Integer.parseInt(bufferedReader.readLine()) > version) {
                        final String uri = bufferedReader.readLine();
                        StringBuilder stringBuilder = new StringBuilder();
                        String temp;
                        while ((temp = bufferedReader.readLine()) != null)
                            stringBuilder.append(temp).append('\n');
                        Looper.prepare();
                        new AlertDialog.Builder(MainActivity.this).setPositiveButton(R.string.ok, (dialog, which) -> {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }).setTitle("发现新版本").setMessage(stringBuilder.toString()).setCancelable(false).show();
                        Looper.loop();
                    }
                } catch (Exception ignored) {
                }
            }
        }.start();
    }
}