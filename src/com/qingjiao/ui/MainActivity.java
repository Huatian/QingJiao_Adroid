package com.qingjiao.ui;

import java.util.ArrayList;

import com.qingjiao.adapter.GridAdapter;
import com.qingjiao.common.MediaUtils;
import com.testplayer.R;
import com.testplayer.R.id;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;


public class MainActivity extends Activity implements OnItemClickListener{
	private GridView grid;
	
	public ArrayList<String> media_paths; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		grid=(GridView) findViewById(R.id.grid);
		
		media_paths=new ArrayList<String>();
		if(media_paths.size()==0){
			MediaUtils.getVedios(getApplicationContext(), media_paths);
		}
		
		GridAdapter adapter = new GridAdapter(media_paths);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		//Intent intent = new Intent(getApplicationContext(),PlayerActivity.class);
		//intent.putExtra("path", media_paths.get(position));
		Intent intent = new Intent(getApplicationContext(), GstPlayerActivity.class);
		intent.putExtra("uri", "file:" + media_paths.get(position));
		startActivity(intent);
	}
	
}
