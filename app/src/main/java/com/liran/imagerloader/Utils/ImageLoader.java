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

/**
 * ImageLoader主类
 * Created by LiRan on 2015-12-14.
 */
public class ImageLoader {

    /**
     * 缓存大小
     */
    public static final int DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;

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

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        //判断当前是够在UI线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不能在UI线程进行网络操作");
        }

        if (mDiskLruCache == null) {
            return null;
        }

        String key = hashKeyFromUrl(url);

        DiskLruCache.Editor editor=mDiskLruCache.edit(key);

        if(editor!=null){
            OutputStream outputStream=editor.newOutputStream(DISK_CACHE_INDEX);
//            if()

        }

        return loadBitmapFromDiskLruCache(url,reqWidth,reqHeight);
    }

    /**
     * 从FromDiskLruCache获取bitmap
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
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex=Integer.toHexString(0xFF&bytes[i]);
            if(hex.length()==1){
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
