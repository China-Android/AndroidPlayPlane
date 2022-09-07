package com.zc.playplane;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayView extends SurfaceView implements SurfaceHolder.Callback, Runnable, View.OnTouchListener {

    private Bitmap myfeiji;
    private Bitmap baozha;
    private Bitmap diren;
    private Bitmap beijing;
    private Bitmap zidan;
    private int w;//屏幕的宽
    private int h;//屏幕的高
    private ArrayList<gameImage> bgImages = new ArrayList<>();
    private SurfaceHolder surfaceHolder;
    private Bitmap twoCache;//缓存图片
    private int height;
    private boolean state = false;
    private feijiImage feijishili;
    private Context context;
    private ArrayList<ZiDan> ZiDans = new ArrayList<ZiDan>();
    private long fenshu = 0;
    private int guanqia = 1;
    private int chudishu = 60;
    private int dijiyidongsudu = 6;
    private int xiayiguan;
    private int[][] sj = {
            {1, 50, 55, 6},
            {2, 60, 50, 7},
            {3, 70, 45, 8},
            {4, 80, 40, 9},
            {5, 90, 35, 10},
            {6, 100, 30, 11},
            {7, 110, 25, 12},
            {8, 120, 20, 13},
            {9, 130, 10, 14},
            {10, 140, 5, 15},
    };

    private SoundPool pool = null;//音乐播放池
    //下面保存声音池的节点
    private int sound_bomb;
    private int sound_gameover;
    private int sound_shot;


    public PlayView(Context context) {
        super(context);
        this.context = context;
        //注册我们相应的方法
        getHolder().addCallback(this);
        this.setOnTouchListener(this);
    }

    /**
     * 加载我们的素材照片
     */
    private void initeBitmap() {
        myfeiji = BitmapFactory.decodeResource(getResources(), R.drawable.my);
        baozha = BitmapFactory.decodeResource(getResources(), R.drawable.baozha);
        diren = BitmapFactory.decodeResource(getResources(), R.drawable.diren);
        beijing = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        zidan = BitmapFactory.decodeResource(getResources(), R.drawable.zidan);
        //二级缓存的创建
        twoCache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //背景照片的处理,包括飞机，背景等
        bgImages.add(new bgImage(beijing));
        bgImages.add(new feijiImage(myfeiji));
        bgImages.add(new dijiImage(diren, baozha));

        //添加游戏声音,用声音池
        pool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 0);
        sound_bomb = pool.load(context, R.raw.bomb, 1);
        sound_gameover = pool.load(context, R.raw.gameover, 1);
        sound_shot = pool.load(context, R.raw.shot, 1);
    }

    /**
     * 飞机的触摸事件
     *
     * @param view
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //点击的触摸事件
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            for (gameImage feiji : bgImages) {
                if (feiji instanceof feijiImage) {

                    feijishili = (PlayView.feijiImage) feiji;

                    if (feiji.getX() < motionEvent.getX() && motionEvent.getX() < feiji.getX() + feijishili.getFeiji_width() && feiji.getY() < motionEvent.getY() && feiji.getY() + feijishili.getFeiji_height() > feiji.getY()) {
                    } else {
                        feijishili = null;
                    }
                    break;
                }

            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            if (feijishili != null) {
                feijishili.setFeiji_x(motionEvent.getX() - feijishili.getFeiji_width() / 2);
                feijishili.setFeiji_y(motionEvent.getY() - feijishili.getFeiji_height() / 2);
            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            feijishili = null;
        }
        return true;
    }


    /**
     * 游戏所有对象处理接口
     */
    public interface gameImage {
        public Bitmap getBitmap();

        public float getX();

        public float getY();
    }

    /**
     * 子弹处理类
     */
    private class ZiDan implements gameImage {

        private Bitmap zidan;
        private feijiImage feiji;
        private float zidan_x;
        private float zidan_y;

        public ZiDan(feijiImage feiji, Bitmap zidan) {
            this.feiji = feiji;
            this.zidan = zidan;
            zidan_x = feiji.getX() + feiji.getFeiji_width() / 2 - 8;
            zidan_y = feiji.getY() - zidan.getHeight();
        }

        @Override
        public Bitmap getBitmap() {
            zidan_y -= 19;
            if (zidan_y < 10) {
                ZiDans.remove(this);
            }
            return zidan;
        }


        @Override
        public float getX() {
            return zidan_x;
        }

        @Override
        public float getY() {
            return zidan_y;
        }
    }

    /**
     * 敌机的处理类
     */
    private class dijiImage implements gameImage {

        private Bitmap diren = null;
        private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        private List<Bitmap> baozhas = new ArrayList<Bitmap>();
        private int x_diren;//设置敌机出现的x轴方向
        private int y_diren;//设置敌机出现的y轴方向
        private float diji_width;//敌机的宽
        private float diji_height;//敌机的高


        public dijiImage(Bitmap diren, Bitmap baozha) {
            this.diren = diren;
            bitmaps.add(Bitmap.createBitmap(diren, 0, 0, diren.getWidth() / 4, diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren, diren.getWidth() / 4, 0, diren.getWidth() / 4, diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren, diren.getWidth() / 4 * 2, 0, diren.getWidth() / 4, diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren, diren.getWidth() / 4 * 3, 0, diren.getWidth() / 4, diren.getHeight()));

            baozhas.add(Bitmap.createBitmap(baozha, 0, 0, baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha, baozha.getWidth() / 4, 0, baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha, baozha.getWidth() / 4 * 2, 0, baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha, baozha.getWidth() / 4 * 3, 0, baozha.getWidth() / 4, baozha.getHeight() / 2));

            baozhas.add(Bitmap.createBitmap(baozha, 0, baozha.getHeight() / 2, baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha, baozha.getWidth() / 4, baozha.getHeight() / 2, baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha, baozha.getWidth() / 4 * 2, baozha.getHeight() / 2, baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha, baozha.getWidth() / 4 * 3, baozha.getHeight() / 2, baozha.getWidth() / 4, baozha.getHeight() / 2));

            Random random = new Random();
            x_diren = random.nextInt(w - (diren.getWidth() / 4));
            diji_width = diren.getWidth() / 4;
            diji_height = diren.getHeight();
        }

        private int num = 0;
        private int index = 0;

        @Override
        public Bitmap getBitmap() {
            Bitmap bitmap = bitmaps.get(index);
            if (num == 7) {
                index++;
                if (index == 7 && state) {
                    bgImages.remove(this);
                }
                if (index == bitmaps.size()) {
                    index = 0;
                }
                num = 0;
            }
            num++;
            y_diren += dijiyidongsudu;
            if (y_diren > h) {
                new playSound(sound_gameover).start();
                stop();
                bgImages.remove(this);
            }
            return bitmap;
        }

        private boolean state = false;//飞机的存在状态

        //受到攻击
        public void shoudaogongji(ArrayList<ZiDan> ZiDans) {
            if (!state) {
                for (gameImage zidan : (List<gameImage>) ZiDans.clone()) {
                    if (zidan.getX() > x_diren && zidan.getX() < x_diren + diji_width && zidan.getY() > y_diren && zidan.getY() < y_diren + diji_height) {
                        ZiDans.remove(this);
                        state = true;
                        bitmaps = baozhas;
                        fenshu += 10;
                        new playSound(sound_bomb).start();
                        break;
                    }
                }
            }
        }

        @Override
        public float getX() {
            return x_diren;
        }

        @Override
        public float getY() {
            return y_diren;
        }
    }

    /**
     * 我方飞机的处理类
     */
    protected class feijiImage implements gameImage {

        private Bitmap feijibg;
        private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        private float feiji_x;//设置飞机开始的横向位置
        private float feiji_y;//设置飞机开始的纵向位置
        private int feiji_width;//飞机的宽,用来确定飞机的有效区域
        private int feiji_height;//飞机的高，用来确定飞机的有效区域

        public void setFeiji_x(float feiji_x) {
            this.feiji_x = feiji_x;
        }

        public void setFeiji_y(float feiji_y) {
            this.feiji_y = feiji_y;
        }

        public int getFeiji_width() {
            return feiji_width;
        }

        public int getFeiji_height() {
            return feiji_height;
        }


        private feijiImage(Bitmap feijibg) {
            this.feijibg = feijibg;
            bitmaps.add(Bitmap.createBitmap(feijibg, 0, 0, feijibg.getWidth() / 4, feijibg.getHeight()));
            bitmaps.add(Bitmap.createBitmap(feijibg, feijibg.getWidth() / 4, 0, feijibg.getWidth() / 4, feijibg.getHeight()));
            bitmaps.add(Bitmap.createBitmap(feijibg, feijibg.getWidth() / 4 * 2, 0, feijibg.getWidth() / 4, feijibg.getHeight()));
            bitmaps.add(Bitmap.createBitmap(feijibg, feijibg.getWidth() / 4 * 3, 0, feijibg.getWidth() / 4, feijibg.getHeight()));
            feiji_x = (w - feijibg.getWidth() / 4) / 2;
            feiji_y = (h - feijibg.getHeight() - 10);
            //得到战机的实际的高和宽
            feiji_width = feijibg.getWidth() / 4;
            feiji_height = feijibg.getHeight();
        }


        private int index = 0;

        @Override
        public Bitmap getBitmap() {
            Bitmap bitmap = bitmaps.get(index);
            index++;
            if (index == bitmaps.size()) {
                index = 0;
            }
            return bitmap;
        }

        @Override
        public float getX() {
            return feiji_x;
        }

        @Override
        public float getY() {
            return feiji_y;
        }
    }

    /**
     * 游戏背景的处理类
     */
    private class bgImage implements gameImage {
        private Bitmap beijing;
        private Bitmap newbeijing = null;

        private bgImage(Bitmap beijing) {
            this.beijing = beijing;
            newbeijing = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }

        public Bitmap getBitmap() {
            Canvas canvas = new Canvas(newbeijing);
            Paint p = new Paint();
            canvas.drawBitmap(beijing, new Rect(0, 0, beijing.getWidth(), beijing.getHeight())
                    , new Rect(0, height, w, h + height), p);
            canvas.drawBitmap(beijing, new Rect(0, 0, beijing.getWidth(), beijing.getHeight())
                    , new Rect(0, -h + height, w, height), p);
            height += 3;
            if (height >= h) {
                height = 0;
            }
            return newbeijing;
        }

        @Override
        public float getX() {
            return 0;
        }

        @Override
        public float getY() {
            return 0;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        w = i1;
        h = i2;
        initeBitmap();
        this.surfaceHolder = surfaceHolder;
        state = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        state = false;
    }


    private boolean stopStates = false;

    private class playSound extends Thread {

        private int i;

        public playSound(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            super.run();
            pool.play(i,1,1,1,0,1);
        }
    }

    /**
     * 暂停按钮
     */
    public void stop() {
        stopStates = true;
    }

    public void start() {
        stopStates = false;
        thread.interrupt();//唤醒线程
    }

    //绘画中心
    Thread thread = null;

    @Override
    public void run() {
        Canvas c = new Canvas(twoCache);
        Paint fenshu_p = new Paint();
        fenshu_p.setColor(Color.YELLOW);
        fenshu_p.setTextSize(30);
        fenshu_p.setDither(true);
        fenshu_p.setAntiAlias(true);
        Paint p = new Paint();
        int diji_number = 0;
        int zidan_number = 0;
        try {
            while (state) {

                while (stopStates) {
                    try {
                        Thread.sleep(1000000);
                    } catch (Exception e) {

                    }
                }
                if (feijishili != null) {
                    if (zidan_number == 10) {
                        zidan_number = 0;
                        new playSound(sound_shot).start();
                        ZiDans.add(new ZiDan(feijishili, zidan));
                    }
                    zidan_number++;
                }

                for (gameImage images : (List<gameImage>) bgImages.clone()) {
                    if (images instanceof dijiImage) {
                        //把子弹的位置告诉敌机
                        ((dijiImage) images).shoudaogongji(ZiDans);
                    }
                    c.drawBitmap(images.getBitmap(), images.getX(), images.getY(), p);
                }
                for (gameImage ziDan : (List<gameImage>) ZiDans.clone()) {
                    c.drawBitmap(ziDan.getBitmap(), ziDan.getX(), ziDan.getY(), p);
                }
                //分数
                c.drawText("分数" + fenshu, 0, 50, fenshu_p);
                c.drawText("关数" + guanqia, 0, 80, fenshu_p);
                c.drawText("下一关所需分数" + xiayiguan, 0, 110, fenshu_p);
                if (sj[guanqia - 1][1] < fenshu) {
                    fenshu = sj[guanqia][1] - fenshu;
                    chudishu = sj[guanqia][2];
                    xiayiguan = sj[guanqia][1];
                    dijiyidongsudu = sj[guanqia][3];
                    guanqia = sj[guanqia][0];
                }
                //每隔多少新增一架飞机
                if (diji_number == chudishu) {
                    diji_number = 0;
                    bgImages.add(new dijiImage(diren, baozha));
                }
                diji_number++;

                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawBitmap(twoCache, 0, 0, p);
                surfaceHolder.unlockCanvasAndPost(canvas);
//                Thread.sleep(10);
            }
        } catch (Exception e) {
            Log.e("error", "线程异常！");
        }
    }
}
