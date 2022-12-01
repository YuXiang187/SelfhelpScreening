package com.yuxiang.selfhelpscreening;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private long exitTime = 0;
    private List<String> pool;
    private SharedPreferences poolValue;
    private final String defaultPool = "语文,数学,英语,物理,化学,地理";

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        LinearLayout fullscreen = findViewById(R.id.fullscreen);
        text = findViewById(R.id.text);

        poolValue = this.getSharedPreferences("pool_value", Context.MODE_PRIVATE);
        pool = new ArrayList<>();
        Collections.addAll(pool, poolValue.getString("pool_value", defaultPool).split(","));
        text.setText(pool.get((int) (Math.random() * pool.size())));

        fullscreen.setOnClickListener(view -> text.setText(pool.get((int) (Math.random() * pool.size()))));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x11 && resultCode == 0x11 && data != null) {
            Bundle bundle = data.getExtras();
            int returnCode = bundle.getInt("return_code");
            if (returnCode == 0) {
                clear();
            } else if (returnCode == 1) {
                settings(bundle.getString("text_string"));
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 0x11);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Toast.makeText(this, "筛选池仅剩" + String.join("、", pool), Toast.LENGTH_LONG).show();
                return true;
            case KeyEvent.KEYCODE_BACK:
                exit();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void clear() {
        if (pool.size() > 1) {
            pool.remove(text.getText().toString());
            SharedPreferences.Editor editor = poolValue.edit();
            editor.putString("pool_value", String.join(",", pool));
            editor.apply();
            text.setText(pool.get((int) (Math.random() * pool.size())));
            System.out.println(pool.size());
        } else {
            Toast.makeText(MainActivity.this, "筛选池仅剩" + String.join("、", pool), Toast.LENGTH_LONG).show();
        }
    }

    private void settings(String settingText) {
        if (!settingText.equals("")) {
            SharedPreferences.Editor editor = poolValue.edit();
            editor.putString("pool_value", settingText);
            editor.apply();
            pool.clear();
            Collections.addAll(pool, poolValue.getString("pool_value", defaultPool).split(","));
            text.setText(pool.get((int) (Math.random() * pool.size())));
        } else {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    public static class SettingsActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_settings);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            EditText editText = findViewById(R.id.edit_text);
            Button cancelBtn = findViewById(R.id.cancel);
            Button clearBtn = findViewById(R.id.clear_item);
            Button settingsBtn = findViewById(R.id.settings);
            cancelBtn.setOnClickListener(view -> finish());
            clearBtn.setOnClickListener(view -> {
                setResult(0x11, getIntent().putExtra("return_code", 0));
                finish();
            });
            settingsBtn.setOnClickListener(view -> {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putInt("return_code", 1);
                bundle.putString("text_string", editText.getText().toString());
                intent.putExtras(bundle);
                setResult(0x11, intent);
                finish();
            });
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_VOLUME_UP:
                    finish();
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
}
