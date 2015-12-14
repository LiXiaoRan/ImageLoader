package com.liran.imagerloader.Utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.liran.imagerloader.Disklrucache.DiskLruCache;

/**
 * ImageLoader主类
 * Created by LiRan on 2015-12-14.
 */
public class ImageLoader {

    private LruCache<String,Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;


}
