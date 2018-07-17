package com.main.chart;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.yanzi.shareserver.Client;
import org.yanzi.shareserver.Manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.Li.LogFile.LogcatHelper;
import com.Li.data.SharePreferenceUtil;
import com.Li.serviceThread.ClientManager;
import com.Li.serviceThread.ServiceClient;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.speech.util.JsonParser;
import com.main.activity.MyApplication;
import com.main.activity.R;
import com.main.utilTools.MyDate;

public class DisplayActivity extends Activity implements OnClickListener {

	MsgReceiver msgReceiver = null;
	private SpeechSynthesizer mTts;
	public JSONObject DisplayActivityHeadParam = new JSONObject();
	// 默认云端发音人
	public static String voicerCloud = "xiaorong";
	// 默认本地发音人
	public static String voicerLocal = "xiaorong";
	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;
	// 云端/本地选择按钮
	// private RadioGroup mRadioGroup;
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	private Toast nToast;
	private SharedPreferences nSharedPreferences;
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog iatDialog;

	// 定义一个现实群组聊天信息的文字框
	public String tag = "DisplayActivity";
	private String recordTime = MyDate.getDateEN(), nowTime;
	private boolean result = false;
	private EditText mEditTextContent;
	// 申明全局变量
	private Button mBtnSend;
	private MyApplication application;
	private ListView mListView;
	private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
	// private SharePreferenceUtil util;
	// private User user;
	private Button soundBtn;
	private Toast mToast;
	private SharedPreferences mSharedPreferences;
	private JSONObject out = new JSONObject();
	public SharePreferenceUtil util;

	public static boolean flag = false;
	Context context1;

	private TextView logText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置屏幕旋转
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.group);

		logText = (TextView) findViewById(R.id.logText);
		LogReceiver logReceiver = new LogReceiver();
		IntentFilter logFilter = new IntentFilter();
		logFilter.addAction("com.liu.client.logText");
		registerReceiver(logReceiver, logFilter);

		util = new SharePreferenceUtil(DisplayActivity.this, "saveUserID");

		// application = (MyApplication) this.getApplicationContext();//
		// 创建好client
		System.out.println(MyDate.getDateEN());
		// 初始化识别对象
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		// 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
		iatDialog = new RecognizerDialog(this, mInitListener);
		initView();
		msgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("org.yanzi.shareserver.receiver");
		registerReceiver(msgReceiver, intentFilter);
		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
				Activity.MODE_PRIVATE);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);

		nSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME,
				Activity.MODE_PRIVATE);
		nToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.textView);
		mBtnSend = (Button) findViewById(R.id.send);
		mBtnSend.setOnClickListener(this);

		mEditTextContent = (EditText) findViewById(R.id.input);

		// mResultText = ((EditText)findViewById(R.id.iat_text));
		// mEditTextContent.setBackgroundColor(Color.BLUE);
		soundBtn = (Button) findViewById(R.id.soundBtn);
		soundBtn.setOnClickListener(this);
	}

	int ret = 0;// 函数调用返回值

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send:// 发送按钮点击事件
			send();
			break;
		case R.id.soundBtn:
			System.out.println("------ 点击 按钮了-----");
			setParam();
			boolean isShowDialog = mSharedPreferences.getBoolean(
					getString(R.string.pref_key_iat_show), false);
			if (isShowDialog) {
				// 显示听写对话框
				iatDialog.setListener(recognizerDialogListener);
				iatDialog.show();
				showTip(getString(R.string.text_begin));
			} else {
				Log.i("wangyonglong", "isShowDialog");
				// 不显示听写对话框
				ret = mIat.startListening(recognizerListener);
				Log.i("wangyonglong", ""+ret);
				if (ret != ErrorCode.SUCCESS) {
					showTip("听写失败,错误码：" + ret);
				} else {
					showTip(getString(R.string.text_begin));
				}
			}
			break;
		}
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			// Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码：" + code);
			}
		}
	};
	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			// Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码：" + code);
			}
		}
	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			mPercentForBuffering = percent;
			nToast.setText(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));

			nToast.show();
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			mPercentForPlaying = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				showTip("播放完成");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * 听写监听器。
	 */
	private RecognizerListener recognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onEndOfSpeech() {
			showTip("结束说话");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = JsonParser.parseIatResult(results.getResultString());
			mEditTextContent.append(text);
			mEditTextContent.setSelection(mEditTextContent.length());
			if (isLast) {
				// TODO 最后的结果
				send();
			}
		}

		@Override
		public void onVolumeChanged(int volume) {
			showTip("当前正在说话，音量大小：" + volume);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

		}
	};

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult result, boolean isLast) {

			// Log.d(TAG, "recognizer result：" + result.getResultString());
			System.out
					.println("=======进入RecognizerDialogListener（）中========= ");
			String text = JsonParser.parseIatResult(result.getResultString());
			// 语音识别文本
			// mEditTextContent.append(text);
			// try {
			// Thread.sleep(3000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// send2();
			if (text.length() > 1) {
				nowTime = MyDate.getDateEN();
				// 判断 两个时间是否相差 一段时间
				if (isTimePassed()) {
					ChatMsgEntity entity = new ChatMsgEntity("我", text, false);
					mDataArrays.add(entity);
					mAdapter = new ChatMsgViewAdapter(DisplayActivity.this,
							mDataArrays);
					mListView.setAdapter(mAdapter);
					mListView.setSelection(mAdapter.getCount() - 1);
					mEditTextContent.setText("");// 清空编辑框数据
					mListView.setSelection(mListView.getCount() - 1);
					recordTime = MyDate.getDateEN(); // 记录当前时间
				}
			}
			ImageView imageView = new ImageView(DisplayActivity.this);
			if (text.equals("。")) {
				mEditTextContent.append("\r\n");
			}
			mEditTextContent.setSelection(mEditTextContent.length());

		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};

	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		System.out
				.println("================setParam()第一步111111111111============");
		String lag = mSharedPreferences.getString("iat_language_preference",
				"mandarin");
		// 设置引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

		System.out
				.println("================setParam()第一步222222222222222============");

		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}
		System.out
				.println("================setParam()第一步33333333333333============");

		// 设置语音前端点
		mIat.setParameter(SpeechConstant.VAD_BOS,
				mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		// 设置语音后端点
		mIat.setParameter(SpeechConstant.VAD_EOS,
				mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		// 设置标点符号
		mIat.setParameter(SpeechConstant.ASR_PTT,
				mSharedPreferences.getString("iat_punc_preference", "1"));
		// 设置音频保存路径
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				Environment.getExternalStorageDirectory()
						+ "/iflytek/wavaudio.pcm");
		System.out
				.println("================setParam()第一步444444444444444444============");

	}

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	private void setParam1() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 设置合成
		if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			// 设置使用云端引擎
			mTts.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_CLOUD);
			// 设置发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
		}
		// else {
		// //设置使用本地引擎
		// mTts.setParameter(SpeechConstant.ENGINE_TYPE,
		// SpeechConstant.TYPE_LOCAL);
		// //设置发音人资源路径
		// mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath());
		// //设置发音人
		// mTts.setParameter(SpeechConstant.VOICE_NAME,voicerLocal);
		// }

		// 设置语速
		mTts.setParameter(SpeechConstant.SPEED,
				nSharedPreferences.getString("speed_preference", "50"));

		// 设置音调
		mTts.setParameter(SpeechConstant.PITCH,
				nSharedPreferences.getString("pitch_preference", "50"));

		// 设置音量
		mTts.setParameter(SpeechConstant.VOLUME,
				nSharedPreferences.getString("volume_preference", "50"));

		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE,
				nSharedPreferences.getString("stream_preference", "3"));
	}

	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

	private void showTip1(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				nToast.setText(str);
				nToast.show();
			}
		});
	}

	/**
	 * 发送消息
	 */
	private void send() {
		String contString = mEditTextContent.getText().toString();

		if (contString.length() > 0) {
			nowTime = MyDate.getDateEN();
			// 判断 两个时间是否相差 一段时间
			if (isTimePassed()) {
				ChatMsgEntity entity = new ChatMsgEntity("我", contString, false);
				mDataArrays.add(entity);
				mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
				mListView.setAdapter(mAdapter);
				mListView.setSelection(mAdapter.getCount() - 1);
				mEditTextContent.setText("");// 清空编辑框数据
				mListView.setSelection(mListView.getCount() - 1);
				recordTime = MyDate.getDateEN(); // 记录当前时间
			} else {
				ChatMsgEntity entity = new ChatMsgEntity("我",
						MyDate.getDateEN(), contString, false);
				// entity.setName(util.getName());//要先存 名字在util里面
				// entity.setName("小强");
				// entity.setMessage(contString);
				// entity.setMsgType(false);

				mDataArrays.add(entity);
				mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
				mListView.setAdapter(mAdapter);

				mListView.setSelection(mAdapter.getCount() - 1);
				System.out.println("----->>>>>>>>>>>>>>>>" + entity);
				mEditTextContent.setText("");// 清空编辑框数据
				mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项

				recordTime = MyDate.getDateEN(); // 记录当前时间

			}
			System.out.println("[DisplayActivity_test]: send chatContent 1 !!");
			// /wkl 20150729 +
			try {
				if (out.has("chatContent"))
					out.remove("chatContent");
				if (out.has("command"))
					out.remove("command");
				out.put("chatContent", contString);
				Client ct = Manager.getManager().getClient("CHATTx");
				if (ct == null) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"[ERROR] ：DSRC聊天通信关闭，发送失败，请确保服务器连接成功后重新发送！",
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return;
				}
				Manager.getManager().publish(ct, out);
				flag = true;

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 时间差值：小于30秒 返回true 大于则为false
	 * 
	 * @return
	 */
	public boolean isTimePassed() {
		nowTime = MyDate.getDateEN();
		try {
			MyDate.format1.parse(recordTime);
			long between = (MyDate.format1.parse(nowTime).getTime() - MyDate.format1
					.parse(recordTime).getTime());
			result = between < 1000 * 30;
			System.out.println(result);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//

		return result;
	}

	private Object setText() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 接收mk5发过来的消音，在此类中显示
	 * 
	 * @author Administrator
	 * 
	 */

	public class MsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String s = intent.getStringExtra("INFO");
			// ChatMsgEntity entity = new ChatMsgEntity(user.getName(),
			// MyDate.getDateEN(), s, true);// 收到的消息

			System.out.println("********" + s + "********");

			mAdapter = new ChatMsgViewAdapter(DisplayActivity.this, mDataArrays);
			if (isTimePassed()) {
				ChatMsgEntity entity = new ChatMsgEntity("其他车辆", s, true);
				mDataArrays.add(entity);
				mListView.setAdapter(mAdapter);
				mListView.setSelection(mAdapter.getCount() - 1);
				mEditTextContent.setText("");// 清空编辑框数据
				mListView.setSelection(mListView.getCount() - 1);

				// recordTime=Integer.parseInt(MyDate.getDateEN());
			} else {
				ChatMsgEntity entity = new ChatMsgEntity("其他车辆",
						MyDate.getDateEN(), s, true);
				mDataArrays.add(entity);
				mListView.setAdapter(mAdapter);
				mListView.setSelection(mAdapter.getCount() - 1);

				// recordTime=Integer.parseInt(MyDate.getDateEN());

				// show.append(s);
			}
			setParam1();
			int code = mTts.startSpeaking(s, mTtsListener); // 文字转语音 wkl
															// 02150728
			if (code != ErrorCode.SUCCESS) {
				showTip1("语音合成失败,错误码: " + code);
			}
		}

	}

	@Override
	public void onBackPressed() {
		// 捕获返回按键
		exitDialog(DisplayActivity.this, "提示", "亲！您真的要退出吗？");
	}

	/**
	 * 退出时的提示框
	 * 
	 * @param context
	 *            上下文对象
	 * @param title
	 *            标题
	 * @param msg
	 *            内容
	 */
	private void exitDialog(Context context, String title, String msg) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 关闭Log 记录
						if (LogcatHelper.getInstance(getApplicationContext()) != null) {
							LogcatHelper.getInstance(getApplicationContext())
									.stop();

						}
						// 向LongRunningService发送 退出广播
						Intent intent = new Intent(
								"com.main.baiduMap.LongRunningService");
						sendBroadcast(intent);

						Intent intent1 = new Intent(
								"com.main.baiduMap.MK5LongRunningService");
						sendBroadcast(intent1);

						Intent intent2 = new Intent(
								"com.main.baiduMap.CarWarningLongRunningService");
						sendBroadcast(intent2);

						// 关闭L2A服务
						MyApplication.MyService
								.stopService(DisplayActivity.this);// wkl
																	// 20151125
						/*
						 * LJL 退出发送 退出信息 给后台
						 */
						try {
							DisplayActivityHeadParam.put("datatype",
									"VEH_LOGOUT");
							DisplayActivityHeadParam.put("fromtype", "veh");
							DisplayActivityHeadParam.put("veh_id",
									util.getAccounts());

							ServiceClient ct = ClientManager.getManager()
									.getClient();
							Log.i(tag, "ct   -----" + ct);
							if (ct != null) {
								// 向后台发送 Jason包
								ClientManager.getManager().servicePublish(ct,
										DisplayActivityHeadParam);
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// if (Manager.getManager().getClientNumber() != 0) {
						// // 如果连接还在，说明服务还在运行
						// // /wkl 20150728 关闭程序向所有客户端 发送断开连接消息
						// if (out.has("chatContent"))
						// out.remove("chatContent");
						// if (out.has("command"))
						// out.remove("command");
						// try {
						// out.put("command", Manager.command.CLOSE);
						// } catch (JSONException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						// client ct = Manager.getManager().getClient("OBERx");
						// if(ct!=null){
						// Log.i(tag, "Manager.getManager().getClient(OBERx);");
						// Manager.getManager().publish(ct, out);
						// }
						// }

						// 关闭语音让输入和输出关闭
						mIat.cancel();
						mIat.destroy();
						mTts.stopSpeaking();

						// 向TCPService发送 退出广播
						// IntentFilter intentFilter=new IntentFilter();
						// intentFilter.addAction("com.main.TCPService.TCPService");
						// TCPServiceBroadcastReceiver TCP=new
						// TCPServiceBroadcastReceiver();
						// registerReceiver(TCP, intentFilter);

						// 向TCPService发送 退出广播
						// Intent intent=new
						// Intent("com.main.TCPService.TCPService");
						// MyApplication.getcontext().sendBroadcast(intent);

						DisplayActivity.this.finish();// 结束当前Activity
						// Intent startMain = new Intent(Intent.ACTION_MAIN);
						// startMain.addCategory(Intent.CATEGORY_HOME);
						// startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						// startActivity(startMain);
						// System.exit(0);// 退出程序

					}
				}).setNegativeButton("取消", null).create().show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 释放掉音频
		Log.i(tag, "DispalyActivity onDestroy()-------");
		// 关闭Log 记录
		if (LogcatHelper.getInstance(getApplicationContext()) != null) {
			LogcatHelper.getInstance(getApplicationContext()).stop();

		}
		// 退出时释放连接
		// if (Manager.getManager().getClientNumber() != 0) {
		// // 如果连接还在，说明服务还在运行
		// // /wkl 20150728 关闭程序向所有客户端 发送断开连接消息
		// if (out.has("chatContent"))
		// out.remove("chatContent");
		// if (out.has("command"))
		// out.remove("command");
		// try {
		// out.put("command", Manager.command.CLOSE);
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// client ct = Manager.getManager().getClient("OBERx");
		// Log.i(tag, "Manager.getManager().getClient(OBERx);");
		//
		// Manager.getManager().publish(ct, out);
		// }
		// 关闭L2A服务
		MyApplication.MyService.stopService(DisplayActivity.this);// wkl
																	// 20151125
		// 关闭语音让输入和输出关闭
		mIat.cancel();
		mIat.destroy();
		mTts.stopSpeaking();

		mTts.destroy();

		ServiceClient.isConnect = false;

	}

	//msgID信息接收广播
	private class  LogReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			HashMap<Integer, Integer> logHM = (HashMap<Integer, Integer>) intent.getSerializableExtra("log");
			StringBuffer logBuffer = new StringBuffer();
			if (!logHM.isEmpty()) {
				for (Integer i : logHM.keySet()) {
					logBuffer.append("msgID:").append(i).append("\n");
				}
				logText.setText(logBuffer);
			}

		}
	}

}
