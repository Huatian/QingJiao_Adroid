package com.qingjiao.common;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class ImageUtils {
	/**
	 * 获取bitmap
	 * 
	 * @param filePath
	 * @return
	 */
	
	public static Bitmap getBitmapById(Resources res, int id,
			BitmapFactory.Options opts) {
		Bitmap bitmap=null;
		if (id > 0) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			bitmap = BitmapFactory.decodeResource(res, id, opts);
			if (bitmap == null) {
				LogUtils.i("bitmap is null");
			}
		}
		return bitmap;
	}
}
