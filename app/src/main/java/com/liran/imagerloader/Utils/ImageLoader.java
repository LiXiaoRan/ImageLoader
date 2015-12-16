package com.liran.imagerloader.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Looper;
import android.util.LruCache;

import com.liran.imagerloader.Disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ImageLoader主类
 * Created by LiRan on 2015-12-14.
 */
public class ImageLoader {


    public static final String TAG = "ImageLoader";

    public static final int MESSAGE_POST_REQUEST = 1;

    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    public static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    public static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    public static final long KEEP_ALIVE = 10L;

//    public static final int TAG_KEY_URI= R.id.im


    /**
     * 缓存大小
     */
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;
    private static final int IO_BUFFER_SIZE = 8 * 1024 * 1024;

    private boolean mIsDiskLruCacheCareated;

    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;


    public static final ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            //以原子方式将当前值加 1。
            return new Thread(r, "ImageLoader# " + mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), mThreadFactory);

    private Context mContext;


    private ImageLoader(Context context) {
        mContext = context.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }

        if (getUsabkleSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCareated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }


    public static ImageLoader build(Context context) {
        return new ImageLoader(context);
    }

    /**
     * 获取此路径空间大小
     *
     * @param diskCacheDir
     * @return
     */
    private int getUsabkleSpace(File diskCacheDir) {

        return 0;
    }

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        //判断当前是够在UI线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不能在UI线程进行网络操作");
        }

        if (mDiskLruCache == null) {
            return null;
        }

        String key = hashKeyFromUrl(url);

        DiskLruCache.Editor editor = mDiskLruCache.edit(key);

        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
//            if()

        }

        return loadBitmapFromDiskLruCache(url, reqWidth, reqHeight);
    }

    /**
     * 从FromDiskLruCache获取bitmap
     *
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap loadBitmapFromDiskLruCache(String url, int reqWidth, int reqHeight) {

        //判断当前是够在UI线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不能在UI线程进行网络操作");
        }

        if (mDiskLruCache == null) {
            return null;
        }


        return null;
    }


    /**
     * 通过url获取key
     *
     * @param url
     * @return
     */
    private String hashKeyFromUrl(String url) {
        String cacheKey = null;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    /**
     * 获取缓存路径
     *
     * @param context
     * @param name
     * @return
     */
    private File getDiskCacheDir(Context context, String name) {

        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                context.getCacheDir().getPath();
        return new File(cachePath + File.separator + name);
    }


    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapToMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }

    }

    private Bitmap getBitmapToMemoryCache(String key) {
        return mMemoryCache.get(key);
    }


}
