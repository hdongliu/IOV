package com.main.baiduMap;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.main.activity.MyApplication;
import com.main.activity.R;

public class Group extends Fragment implements OnClickListener {
	private Button backBtn, creatBtn;
	public static EditText editName1, editIntro1, editStart1, editEnd1, editNote1;

	/*
	 * 这里定义一个车辆组队frament（碎片），用于显示车辆组队的功能
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.car_regester_group, container, false);
		// 界面组件
		editName1 = (EditText) view.findViewById(R.id.group_name);
		editIntro1 = (EditText) view.findViewById(R.id.group_introduce);
		editStart1 = (EditText) view.findViewById(R.id.group_start);
		editEnd1 = (EditText) view.findViewById(R.id.group_end);
		editNote1 = (EditText) view.findViewById(R.id.group_note);

		backBtn = (Button) view.findViewById(R.id.group_back_btn);
		backBtn.setOnClickListener(this);

		creatBtn = (Button) view.findViewById(R.id.creat_btn);
		creatBtn.setOnClickListener(this);

		return view;

	}

	/**
	 * 设置按钮点击的回调
	 * 
	 */
	public interface FBackBtnClickListener {// 定义 返回键 接口
		void onBackBtnClick();
	}

	public interface FCreakBtnClickListener {// 定义 创建按钮 接口
		void onCreakBtnClick();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.group_back_btn:
			((FBackBtnClickListener) getActivity()).onBackBtnClick();
			break;
		case R.id.creat_btn:
//			MyApplication.editName = editName1.getText().toString();
//			MyApplication.editIntro = editIntro1.getText().toString();
//			MyApplication.editStart = editStart1.getText().toString();
//			MyApplication.editEnd = editEnd1.getText().toString();
//			MyApplication.editNote = editNote1.getText().toString();
			((FCreakBtnClickListener) getActivity()).onCreakBtnClick();
			break;
		}
	}
}
