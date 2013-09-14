package com.qingjiao.adapter;

import java.util.ArrayList;

import com.qingjiao.bean.MediaBean;
import com.qingjiao.common.LogUtils;
import com.qingjiao.common.MediaUtils;
import com.testplayer.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
	private ArrayList<String> paths;
	private Holder mHolder;
	
	public GridAdapter(ArrayList<String> paths) {
		// TODO Auto-generated constructor stub
		this.paths=paths;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return paths.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return paths.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView!=null){
			mHolder=(Holder) convertView.getTag();
		}else{
			mHolder=new Holder();
			LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_grid, null);
			mHolder.mImgView = (ImageView) convertView.findViewById(R.id.media_img);
			mHolder.mTextName = (TextView) convertView.findViewById(R.id.media_name);
			mHolder.mTextInfo = (TextView) convertView.findViewById(R.id.media_info);
			convertView.setTag(mHolder);
		}
		
		MediaBean bean=new MediaBean();
		Log.i("Adapter", paths.get(index));
		MediaUtils.getMediaInfo(paths.get(index), bean);

		Log.i("TAG", ""+bean.getTitle()+","+bean.getDuration()+","+bean.getWidth()+","+bean.getHeight()+","+bean.getSize());
		if(bean.getBitmap()!=null){
			mHolder.mImgView.setImageBitmap(bean.getBitmap());
		}else{
			//ImageUtils.getBitmapById(mHolder.mImgView.getResources(), R.drawable.file_icon_video, null);
			LogUtils.i("缩略图为空，选用站位图");
			mHolder.mImgView.setImageResource(R.drawable.file_icon_video);
		}
		
		mHolder.mTextName.setText(bean.getTitle());
		mHolder.mTextInfo.setText(bean.getSize()+"M  "+bean.getWidth()+"x"+bean.getHeight()+"  "+MediaUtils.turnNumToTime(Integer.valueOf(bean.getDuration())));
		return convertView;
	}
	
	public class Holder{
		public ImageView mImgView;
		public TextView mTextName;
		public TextView mTextInfo;
	}
}
