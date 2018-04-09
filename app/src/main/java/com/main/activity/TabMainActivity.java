package com.main.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TabHost;

import com.main.chart.DisplayActivity;

@SuppressWarnings("deprecation")
public class TabMainActivity extends TabActivity {
	private static final String TAG = "TabMainActivity";
	private TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置屏幕旋转
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.tab);
		
		this.tabHost = super.getTabHost();   
//		tabHost.addTab(tabHost.newTabSpec("tab1")
//		.setIndicator("地图",getResources().getDrawable(R.drawable.baidu_map_icon))
//		.setContent(new Intent(this, RoutePlanDemo.class)));

		tabHost.addTab(tabHost.newTabSpec("tab4")
				.setIndicator("",getResources().getDrawable(R.drawable.location_fill))
				.setContent(new Intent(this, OverlayDemo.class)));
		tabHost.setCurrentTab(0);
		
		tabHost.addTab(tabHost.newTabSpec("tab2")
				.setIndicator("",getResources().getDrawable(R.drawable.community_fill))
				.setContent(new Intent(this, DisplayActivity.class)));
		
		tabHost.addTab(tabHost.newTabSpec("tab3")
				.setIndicator("",getResources().getDrawable(R.drawable.new_fill))
				.setContent(new Intent(this, Dis_Info_Activity.class)));
		
	}
}
