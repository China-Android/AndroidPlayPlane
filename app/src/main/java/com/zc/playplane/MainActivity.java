package com.zc.playplane;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    PlayView view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new PlayView(this);
        setContentView(view);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            view.stop();
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("你要退出吗？");
            alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //杀死自己的进程
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            view.start();
                            dialogInterface.dismiss();
                        }
                    }).show();
        }
        return super.onKeyDown(keyCode, event);
    }
}