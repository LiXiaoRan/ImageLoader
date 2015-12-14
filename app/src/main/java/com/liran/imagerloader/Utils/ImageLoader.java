package com.liran.imagerloader.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.LruCache;

import com.liran.imagerloader.Disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

/**
 * ImageLoader主类
 * Created by LiRan on 2015-12-14.
 */
public class ImageLoader {

    /**
     * 缓存大小
     */
    public static final int DISK_CACHE_SIZE = 1024 * 1024 * 50;

    private boolean mIsDiskLruCacheCareated;

    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;


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

    /**
     * 获取此路径空间大小
     *
     * @param diskCacheDir
     * @return
     */
    private int getUsabkleSpace(File diskCacheDir) {
        return 0;
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
