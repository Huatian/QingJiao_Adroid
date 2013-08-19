package com.qingjiao.common;

import java.io.File;
import java.util.ArrayList;

import com.qingjiao.bean.MediaBean;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.util.Log;

public class MediaUtils {
	
	public static void getVedios(Context context, ArrayList<String> media_paths){
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (cursor == null) {
		    // query failed, handle error.
		} else if (!cursor.moveToFirst()) {
		    // no media on the device
		} else {
			
			int data=cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DATA);
			
		    do {
		    	String path = cursor.getString(data);
		    	Log.i("TAG", path);
		    	media_paths.add(path);
		    } while (cursor.moveToNext());
		}
	}
	
	public static void getMediaInfo(String path, MediaBean bean){
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(path);
		
		File file=new File(path);
		if(file!=null){
			Log.i("TAG", "地址可用!");
			long length=file.length();
			bean.setSize(length/(1024*1024));
		}
		
		if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)!=null){
			bean.setTitle(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
		}else{
			String title = path.substring(path.lastIndexOf(File.separator)+1);
			bean.setTitle(title);
		}
		
		if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!=null){
			bean.setWidth(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
		}else{
			bean.setWidth("*");
		}
		
		if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!=null){
			bean.setHeight(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
		}else{
			bean.setHeight("*");
		}
		
		if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!=null){
			bean.setDuration(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
		}else{
			bean.setDuration("*");
		}
		
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, 0);
		bean.setBitmap(bitmap);
		/*byte[] buffer = retriever.getEmbeddedPicture();
		if(buffer!=null){
			bean.setBitmap(BitmapFactory.decodeByteArray(buffer, 0, buffer.length));
		}else{
			bean.setBitmap(retriever.getFrameAtTime(Long.parseLong(bean.getDuration())/2));
		}*/
		
		retriever.release();
	}
	
	public static StringBuilder turnNumToTime(int progress){
		StringBuilder str = new StringBuilder();
		int second=progress/1000;
		int minute=second/60;
		second=second%60;
		
		int hour=0;
		if(minute!=0){
			hour=minute/60;
			minute=(minute=minute%60)!=0?minute:0;
			
		}
		
		str.append(hour!=0?hour+":":"");
		str.append(minute!=0?minute>9?minute+":":"0"+minute+":":"00:");
		str.append(second!=0?second>9?second:"0"+second:"00");
		
		return str;
	}
	
}
