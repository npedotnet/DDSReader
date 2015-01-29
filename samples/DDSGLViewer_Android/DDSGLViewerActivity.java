/**
 * DDSGLViewerActivity.java
 * 
 * Copyright (c) 2015 Kenji Sasaki
 * Released under the MIT license.
 * https://github.com/npedotnet/DDSReader/blob/master/LICENSE
 * 
 * English document
 * https://github.com/npedotnet/DDSReader/blob/master/README.md
 * 
 * Japanese document
 * http://3dtech.jp/wiki/index.php?DDSReader
 * 
 */

package com.example.ddsglviewer_android;

import android.app.Activity;
import android.os.Bundle;

public class DDSGLViewerActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new DDSGLSurfaceView(this);
		setContentView(view);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		view.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		view.onPause();
	}
	
	private DDSGLSurfaceView view;
	
}
