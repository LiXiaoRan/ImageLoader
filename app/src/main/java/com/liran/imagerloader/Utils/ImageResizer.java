package com.liran.imagerloader.Utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;

/**
 * 图片压缩
 * Created by LiRan on 2015-12-14.
 */
public class ImageResizer {

    public static final String TAG = "ImageResizer";


    public ImageResizer() {
    }


    /**
     * 加载图片
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap decodeSampleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    /**
     * 加载图片
     * @param fd
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    /**
     * 计算压缩尺寸
     *
     * @param options
     * @param reqWidth  期望宽度 一般来说就是imageview的宽度
     * @param reqHeight 期望高度 一般来说就是imageview的高度
     * @return 缩放的inSampleSize数值
     */

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        //默认的缩放比例
        int inSampleSize = 1;

        if (reqHeight == 0 || reqWidth == 0) {
            return 1;
        }

        //取出图片原始宽高
        final int width = options.outWidth;
        final int height = options.outHeight;

        //如果图片的原始宽高都小于期望宽高就不做处理 否则就进行处理
        if (width >= reqWidth || height >= reqHeight) {

            final int halfWidth = width / 2;
            final int halfHeight = height / 2;

            //找到一个恰当的压缩比例使压缩后的宽高都小于期望宽高
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {

                inSampleSize *= 2;
            }


        }

        return inSampleSize;
    }

}
