package com.shanjian.sreenshotdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.util.LruCache;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Android 截屏方法整理
 */
public class Activity_Main extends AppCompatActivity implements View.OnClickListener {

    private TextView tvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTime = findViewById(R.id.tvTime);
        Button btnShot = findViewById(R.id.btnExcludeTop);
        btnShot.setOnClickListener(this);
        Button btnShotOut = findViewById(R.id.btnShotOut);
        btnShotOut.setOnClickListener(this);
    }

    /**
     * 根据指定的Activity截图（带空白的状态栏）
     *
     * @param context 要截图的Activity
     * @return Bitmap
     */
    public static Bitmap shotActivity(Activity context) {
        View view = context.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        return bitmap;
    }

    /**
     * 根据指定的Activity截图（去除状态栏）
     *
     * @param activity 要截图的Activity
     * @return Bitmap
     */
    public Bitmap shotActivityNoStatusBar(Activity activity) {
        // 获取windows中最顶层的view
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;
    }

    /**
     * 根据指定的view截图
     *
     * @param v 要截图的view
     * @return Bitmap
     */
    public static Bitmap getViewBitmap(View v) {
        if (null == v) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        if (Build.VERSION.SDK_INT >= 11) {
            v.measure(View.MeasureSpec.makeMeasureSpec(v.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(v.getHeight(), View.MeasureSpec.EXACTLY));
            v.layout((int) v.getX(), (int) v.getY(), (int) v.getX() + v.getMeasuredWidth(), (int) v.getY() + v.getMeasuredHeight());
        } else {
            v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        }

        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache(), 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.setDrawingCacheEnabled(false);
        v.destroyDrawingCache();
        return bitmap;
    }

    /**
     * Scrollview截屏
     *
     * @param scrollView 要截图的ScrollView
     * @return Bitmap
     */
    public static Bitmap shotScrollView(ScrollView scrollView) {
        int h = 0;
        Bitmap bitmap = null;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
            scrollView.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    /**
     * ListView截图
     *
     * @param listView 要截图的ListView
     * @return Bitmap
     */
    public static Bitmap shotListView(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        int itemsCount = adapter.getCount();
        int allItemsHeight = 0;

        ArrayList<Bitmap> bmps = new ArrayList<>();
        for (int i = 0; i < itemsCount; i++) {
            View childView = adapter.getView(i, null, listView);
            childView.measure(View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
            childView.setDrawingCacheEnabled(true);
            childView.buildDrawingCache();
            bmps.add(childView.getDrawingCache());
            allItemsHeight += childView.getMeasuredHeight();
        }

        Bitmap bigBitmap = Bitmap.createBitmap(listView.getMeasuredWidth(), allItemsHeight, Bitmap.Config.ARGB_8888);
        Canvas bigCanvas = new Canvas(bigBitmap);

        Paint paint = new Paint();
        int iHeight = 0;

        for (int i = 0; i < bmps.size(); i++) {
            Bitmap bmp = bmps.get(i);
            bigCanvas.drawBitmap(bmp, 0, iHeight, paint);
            iHeight += bmp.getHeight();

            bmp.recycle();
            bmp = null;
        }
        return bigBitmap;
    }

    /**
     * RecyclerView截屏
     *
     * @param view 要截图的RecyclerView
     * @return Bitmap
     */
    public static Bitmap shotRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(),
                        holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            Drawable lBackground = view.getBackground();
            if (lBackground instanceof ColorDrawable) {
                ColorDrawable lColorDrawable = (ColorDrawable) lBackground;
                int lColor = lColorDrawable.getColor();
                bigCanvas.drawColor(lColor);
            }

            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }
        }
        return bigBitmap;
    }

    /**
     * 截取webView快照(webView加载的整个内容的大小)
     *
     * @param webView
     * @return
     */
    private Bitmap captureWebView(WebView webView) {
        Picture snapShot = webView.capturePicture();

        Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(), snapShot.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        snapShot.draw(canvas);
        return bmp;
    }

    /**
     * 截屏
     *
     * @param context
     * @return
     */
    private Bitmap captureScreen(Activity context) {
        View cv = context.getWindow().getDecorView();
        Bitmap bmp = Bitmap.createBitmap(cv.getWidth(), cv.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        cv.draw(canvas);
        return bmp;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnExcludeTop:
                GetRequestPermission(0);
                break;
            case R.id.btnShotOut:
                GetRequestPermission(1);
                break;
        }
    }

    protected String[] permission1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    protected String[] permission2 = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.READ_PHONE_STATE};

    private void GetRequestPermission(int type) {
        AndPermission.with(this)
                .runtime()
                .permission(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? permission1 : permission2)
                .onGranted(permissions -> {
                    switch (type) {
                        case 0:
                            try {
                                SaveFile(captureScreen(this));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 1:
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (!Settings.canDrawOverlays(this)) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivityForResult(intent, 1);
                                } else {
                                    Intent intent = new Intent(this, Activity_ShotOut.class);
                                    startActivity(intent);
                                }
                            }
                            break;
                    }
                })
                .onDenied(permissions -> {
                    Toast.makeText(this, permissions.toString() + "权限拒绝", Toast.LENGTH_LONG).show();
                })
                .start();
    }

    private void SaveFile(Bitmap bitmap) throws IOException {
        if (bitmap != null) {
            String fileName = "";
            Bitmap bit = compressImage(bitmap);
            initFile();
            // 首先保存图片
            File appDir = new File(DCIM_PHOTOPath, "/myImage");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bit.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "保存失败!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(this, "保存失败!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            String baseUrl = DCIM_PHOTOPath + "/myImage";
            String saveUrl = baseUrl + Uri.fromFile(new File(fileName)).getPath();
            Toast.makeText(this, "保存成功:" + saveUrl, Toast.LENGTH_SHORT).show();
            // 其次把文件插入到系统图库
//            try {
//                MediaStore.Images.Media.insertImage(weakReference.get().getContentResolver(),
//                        file.getAbsolutePath(), fileName, null);
//                Toast.makeText(weakReference.get(), "保存成功:" + saveUrl, Toast.LENGTH_SHORT).show();
//            } catch (FileNotFoundException e) {
//                Toast.makeText(weakReference.get(), "保存失败!", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
            // 最后通知图库更新
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getPath()))));
        }
    }

    public static final int IMAGE_SIZE = 32768;

    /**
     * Bitmap质量压缩法，压缩到32k以下
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length > IMAGE_SIZE && options != 10) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    private String CompressPath = "", DCIM_VIDEOPath = "", DCIM_PHOTOPath = "";

    /*
     * 初始化项目文件
     */
    public void initFile() {
        try {
            File file = getExternalFilesDir("CompressImage");
            if (file == null || !file.exists()) {
                file.mkdirs();
            }
            CompressPath = file.getAbsolutePath();//图片压缩后保存的位置

            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "redFile/video");
            //判断文件夹是否存在，如果不存在就创建，否则不创建
            if (file == null || !file.exists()) {
                file.mkdirs();
            }
            DCIM_VIDEOPath = file.getAbsolutePath();//视频的保存地址
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "redFile/photo");
            //判断文件夹是否存在，如果不存在就创建，否则不创建
            if (file == null || !file.exists()) {
                file.mkdirs();
            }
            DCIM_PHOTOPath = file.getAbsolutePath();//照片的保存地址
        } catch (Exception e) {

        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    tvTime.setText("时间:" + getCurrTime("HH:mm:ss"));
                    handler.sendEmptyMessageDelayed(1, 1000);
                    break;
                case 1:
                    handler.sendEmptyMessage(0);
                    break;
            }
        }
    };

    /**
     * 返回的格式为 自定义
     */
    public String getCurrTime(String formatStr) {
        Calendar calendar = Calendar.getInstance();
        return TimeParse(calendar.getTime(), formatStr);
    }

    /**
     * 返回的格式为自定义
     *
     * @param time
     * @return
     */
    public static String TimeParse(Date time, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sdf.format(time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handler != null) {
            handler.sendEmptyMessage(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeMessages(0);
            handler.removeMessages(1);
            handler = null;
        }
    }

}
