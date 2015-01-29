/**
 * DDSBitmapViewerActivity.java
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

package com.example.ddsbitmapviewer_android;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class DDSBitmapViewerActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		GridLayout layout = new GridLayout(this);
		ScrollView scroll = new ScrollView(this);
		scroll.addView(layout);
		setContentView(scroll);
		
		try {
			String [] list = getAssets().list("images");
			
			// count dds images
			int count = 0;
			for(int i=0; i<list.length; i++) {
				if(list[i].endsWith(".dds")) count++;
			}
			
			layout.setColumnCount(3);
			layout.setRowCount(count/3 + 1);
			
			// create dds image view
			for(int i=0; i<list.length; i++) {
				if(list[i].endsWith(".dds")) {
					LinearLayout view = createDDSView(list[i]);
					if(view != null) layout.addView(view);
				}
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private LinearLayout createDDSView(String name) {
		ImageView image = createDDSImageView("images/"+name);
		if(image != null) {
			LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(image);
			TextView label = new TextView(this);
			label.setText(name);
			label.setMaxWidth(128);
			label.setPadding(8, 8, 0, 0);
			layout.addView(label);
			return layout;
		}
		return null;
	}
	
	private ImageView createDDSImageView(String path) {
		Bitmap bitmap = createDDSBitmap(path);
		if(bitmap != null) {
			ImageView imageView = new ImageView(this);
			imageView.setImageBitmap(bitmap);
			imageView.setAdjustViewBounds(true);
			imageView.setMaxWidth(128);
			imageView.setMaxHeight(128);
			imageView.setPadding(8, 8, 0, 0);
			return imageView;
		}
		return null;
	}
	
	private Bitmap createDDSBitmap(String path) {
		Bitmap bitmap = null;
		try {
			InputStream is = getAssets().open(path);
			byte [] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();
			
			int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
			int width = DDSReader.getWidth(buffer);
			int height = DDSReader.getHeight(buffer);
			
			bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Config.ARGB_8888);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

}
