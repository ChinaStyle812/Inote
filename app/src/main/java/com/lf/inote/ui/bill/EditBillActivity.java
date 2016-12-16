package com.lf.inote.ui.bill;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.util.JsonParser;
import com.lf.inote.R;
import com.lf.inote.db.BillDBImpl;
import com.lf.inote.model.Bill;
import com.lf.inote.ui.MainActivity;
import com.lf.inote.utils.TimeUtil;
import com.lf.inote.view.NoteEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditBillActivity extends Activity implements OnClickListener {

	/**
	 * 账单类型-收入
	 */
	public static final int TYPE_IN = 1;

	/**
	 * 账单类型-支出
	 */
	public static final int TYPE_OUT = 0;

	private TextView mTvDate;

	private Button mBtnTypeIn;

	private Button mBtnTypeOut;

	private EditText mEtAmount;

	private NoteEditText mEtRemark;

	private LinearLayout mLayoutSave;
	private LinearLayout mLayoutSpeech;

	/**
	 * 账单类型：1-收入，0-支出
	 */
	private int mBillType = TYPE_OUT;

	private Context mContext;

	private Bill mBill;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_bill);

		mContext = this;

		initView();
		initData();
	}

	private void initView() {
		mTvDate = (TextView) findViewById(R.id.tv_add_bill_date);
		mBtnTypeIn = (Button) findViewById(R.id.btn_bill_type_in);
		mBtnTypeOut = (Button) findViewById(R.id.btn_bill_type_out);

		mEtAmount = (EditText) findViewById(R.id.et_add_bill_amount);
		mEtRemark = (NoteEditText) findViewById(R.id.et_bill_remark);

		mLayoutSave = (LinearLayout) findViewById(R.id.ll_add_bill_save);
		mLayoutSpeech = (LinearLayout) findViewById(R.id.ll_add_bill_speech);
		((ImageView) findViewById(R.id.iv_add_bill_speech)).setColorFilter(getResources().getColor(R.color.light_blue));

		mTvDate.setOnClickListener(this);
		mBtnTypeIn.setOnClickListener(this);
		mBtnTypeOut.setOnClickListener(this);
		mLayoutSave.setOnClickListener(this);
		mLayoutSpeech.setOnClickListener(this);

		setBillType(TYPE_OUT);
	}

	private void initData() {
		mBill = (Bill) getIntent().getSerializableExtra(MainActivity.BILL);
		if (null != mBill) {
			mTvDate.setText(mBill.getDate());
			setBillType(mBill.getType());
			mEtAmount.setText(mBill.getMoney() + "");
			mEtRemark.setText(mBill.getRemark());
		} else {
			mTvDate.setText(TimeUtil.getCurrentDateTime("yyyy-MM-dd"));
		}
	}

	private void setBillType(int type) {
		mBillType = type;
		if (TYPE_IN == type) {
			mBtnTypeIn.setBackgroundResource(R.drawable.button_plain_clicked_left);
			mBtnTypeOut.setBackgroundResource(R.drawable.button_plain_normal_right);
		} else if (TYPE_OUT == type) {
			mBtnTypeIn.setBackgroundResource(R.drawable.button_plain_normal_left);
			mBtnTypeOut.setBackgroundResource(R.drawable.button_plain_clicked_right);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_add_bill_date:
				selectDate();
				break;
			case R.id.btn_bill_type_in:
				setBillType(TYPE_IN);
				break;
			case R.id.btn_bill_type_out:
				setBillType(TYPE_OUT);
				break;
			case R.id.ll_add_bill_save:
				saveBill();
				break;
			case R.id.ll_add_bill_speech:
				speechInput();
				break;
			default:
				break;
		}
	}

	private void selectDate() {
		Calendar calendar = Calendar.getInstance();
		DatePickerDialog date = new DatePickerDialog(mContext, new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mTvDate.setText(String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
			}
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		date.show();
	}

	private void saveBill() {
		boolean isNewBill = false;
		String date = mTvDate.getText().toString();
		if (null == mBill) {
			mBill = new Bill();
			isNewBill = true;
		}
		mBill.setDate(date);
		mBill.setType(mBillType);
		try {
			double money = Double.parseDouble(mEtAmount.getText().toString());
			mBill.setMoney(money);
		} catch (NumberFormatException e) {
			Toast.makeText(mContext, "金额格式错误", Toast.LENGTH_SHORT).show();
			return;
		}
		if (mEtRemark.getText().toString().trim().equals("")) {
			Toast.makeText(mContext, "请添加备注", Toast.LENGTH_SHORT).show();
			return;
		}
		mBill.setRemark(mEtRemark.getText().toString().trim());
		if (isNewBill) {
			BillDBImpl.getInstance(mContext).insert(mBill);
		} else {
			BillDBImpl.getInstance(mContext).update(mBill);
		}
		setResult(RESULT_OK);
		finish();
	}

	/**
	 * 返回键处理
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Toast.makeText(mContext, "取消编辑账单", Toast.LENGTH_SHORT).show();
			setResult(RESULT_CANCELED);
			finish();
		}

		return true;
	}

	private Toast mToast;
	private List<String> mResultList = new ArrayList<>();

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	private void speechInput() {
		mToast = Toast.makeText(EditBillActivity.this, "", Toast.LENGTH_SHORT);
		mResultList.clear();
		showDialog();
//		understander();
	}

	private void noDialog() {
		//1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
		SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(EditBillActivity.this, null);
		//2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
		setParam(mIat);
		//3.开始听写
		mIat.startListening(mRecoListener);
	}

	private void understander() {
		//1.创建文本语义理解对象
		SpeechUnderstander understander = SpeechUnderstander.createUnderstander(EditBillActivity.this, null);
		//2.设置参数，语义场景配置请登录http://osp.voicecloud.cn/
		understander.setParameter(SpeechConstant.DOMAIN, "iat");
		understander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		understander.setParameter(SpeechConstant.ACCENT, "mandarin");
		understander.setParameter(SpeechConstant.NLP_VERSION, "2.0");
		understander.setParameter(SpeechConstant.RESULT_TYPE, "json");
		//3.开始语义理解
		understander.startUnderstanding(mUnderstanderListener);
		// XmlParser为结果解析类，见SpeechDemo
	}

	private SpeechUnderstanderListener mUnderstanderListener = new SpeechUnderstanderListener() {
		@Override
		public void onVolumeChanged(int i, byte[] bytes) {
			showTip("正在说话，当前音量：" + i);
		}

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}

		@Override
		public void onEndOfSpeech() {
			showTip("结束说话");
		}

		@Override
		public void onResult(UnderstanderResult understanderResult) {
			Log.d("Result:", understanderResult.getResultString());
		}

		@Override
		public void onError(SpeechError speechError) {
			showTip(speechError.getPlainDescription(true));
			Log.e("error:", speechError.getPlainDescription(true));
		}

		@Override
		public void onEvent(int i, int i1, int i2, Bundle bundle) {

		}
	};

	private void showDialog() {
		//1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
		RecognizerDialog iatDialog = new RecognizerDialog(this, null);
		//2.设置听写参数，同上节
		//3.设置回调接口
		setDialogParam(iatDialog, false);
		iatDialog.setListener(mDialogListener);
		//4.开始听写
		iatDialog.show();
	}

	private void setDialogParam(RecognizerDialog mIat, boolean nlpEnable) {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		mIat.setParameter(SpeechConstant.DOMAIN, "iat");
		if (nlpEnable) {
			mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
			mIat.setParameter(SpeechConstant.NLP_VERSION, "2.0");
			mIat.setParameter(SpeechConstant.PARAMS, "sch=1");
		}
		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		/**
		 * mandarin: 普通话
		 * cantonese: 粤语
		 * henanese: 河南话
		 * en_us: 英语
		 */
		String lag = "mandarin";
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
	}

	public void setParam(SpeechRecognizer mIat) {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		mIat.setParameter(SpeechConstant.DOMAIN, "iat");
		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		/**
		 * mandarin: 普通话
		 * cantonese: 粤语
		 * henanese: 河南话
		 * en_us: 英语
		 */
		String lag = "mandarin";
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
	}

	//听写监听器
	private RecognizerListener mRecoListener = new RecognizerListener() {
		//听写结果回调接口(返回Json格式结果，用户可参见附录12.1)；
		//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
		//关于解析Json的代码可参见MscDemo中JsonParser类；
		//isLast等于true时会话结束。
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d("Result:", results.getResultString());
			printResult(results);
		}

		//会话发生错误回调接口
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
		}

		//开始录音
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}

		//结束录音
		public void onEndOfSpeech() {
			showTip("结束说话");
		}

		//扩展用接口
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};

	private RecognizerDialogListener mDialogListener = new RecognizerDialogListener() {
		@Override
		public void onResult(RecognizerResult recognizerResult, boolean b) {
			Log.d("Result:", recognizerResult.getResultString());
			printResult(recognizerResult);
		}

		@Override
		public void onError(SpeechError speechError) {
			showTip(speechError.getPlainDescription(true));
		}
	};

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());
		mResultList.add(text);
		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		StringBuffer sb = new StringBuffer();
		for (String r : mResultList) {
			if (!TextUtils.isEmpty(r)) {
				sb.append(r);
			}
		}
		String finalText = sb.toString();
		mEtRemark.setText(finalText);
		mEtRemark.setSelection(finalText.length());
	}
}
