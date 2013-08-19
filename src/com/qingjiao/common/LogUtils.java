package com.qingjiao.common;

import android.util.Log;

public class LogUtils {
	public static String LOG="TestPlayer";
	
	public static void i(String msg){
		Log.i(LOG, msg);
	}
	
	public static void e(String msg){
		Log.e(LOG, msg);
	}
}
