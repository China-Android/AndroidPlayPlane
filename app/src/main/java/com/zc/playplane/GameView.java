package com.zc.playplane;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable,SurfaceHolder.Callback {

    public GameView(Context context) {
        super(context);
        //注册回调方法
        getHolder().addCallback(this);
        gameBitmap = Bitmap.createBitmap(500,500, Bitmap.Config.ARGB_8888);
    }

    private boolean runState = false;
    //绘画对象
    private SurfaceHolder surfaceHolder = null;
    private Bitmap gameBitmap = null;
    @Override
    public void run() {
        Random random = new Random();
        try {
            while (true){
                //获得绘画的画布，要先锁定，多线程绘画，谁先锁上谁先用
                Canvas canvas = surfaceHolder.lockCanvas();

                Paint p = new Paint();
                //在二级缓存里面画线条
                Canvas c = new Canvas(gameBitmap);
                p.setColor(Color.rgb(random.nextInt(255),random.nextInt(255),random.nextInt(255)));
                c.drawLine(random.nextInt(1000),random.nextInt(1000),random.nextInt(1000),random.nextInt(1000),p);

                //把在二级缓存画的图片添加到我们的视图中
                canvas.drawBitmap(gameBitmap,0,0,new Paint());
                //解锁，把绘画好的内容提交上去
                surfaceHolder.unlockCanvasAndPost(canvas);
                Thread.sleep(100);
            }
        }catch (Exception e){
            Log.e("APP.TAG","异常");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        runState = true;
        //激活线程
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        runState = false;
    }
}
