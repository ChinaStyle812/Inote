package com.lf.inote.ui.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lf.inote.NoteApp;
import com.lf.inote.R;
import com.lf.inote.db.NoteDBImpl;
import com.lf.inote.model.Note;
import com.lf.inote.ui.MainActivity;
import com.lf.inote.utils.StringUtils;
import com.lf.inote.utils.TimeUtil;
import com.lf.inote.view.NoteEditText;

import java.util.ArrayList;
import java.util.Arrays;

public class EditNoteActivity extends Activity implements RecognitionListener {
    
    private String mDefTitle;
    
    private EditText mEtTitle;
    
    private NoteEditText mEtNote;
    
    private Note mNote;
    
    private boolean isNewNote;
    
    private String mCreateTime;
    
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_note);
        
        mContext = this;

        initView();
        initData();
    }
    
    /**
     * @description 初始化控件
     * @date 2015-2-11
     */
    private void initView() {
        mDefTitle = getString(R.string.text_def_note_title);
        mEtTitle = (EditText) findViewById(R.id.et_note_title);
        mEtNote = (NoteEditText) findViewById(R.id.et_note_content);
        ((ImageView) findViewById(R.id.iv_add_note_speech)).setColorFilter(getResources().getColor(R.color.light_blue));
    }
    
    /**
     * @description 初始化显示数据
     * @date 2015-2-11
     */
    private void initData() {
        mNote = (Note) getIntent().getSerializableExtra(MainActivity.NOTE);
        if (null == mNote) {
            mEtTitle.setText("");
            mEtNote.setText("");
            isNewNote = true;
            mCreateTime = TimeUtil.getCurrentDateTime(null);
        }
        else {
            mEtTitle.setText(mNote.getTitle());
            mEtNote.setText(mNote.getContent());
            isNewNote = false;
        }
    }
    
    /**
     * 返回键处理
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String title = mEtTitle.getText().toString();
            String content = mEtNote.getText().toString();
            if (!StringUtils.isEmpty(title)) {
                if (isNewNote) {
                    mNote = new Note();
                    mNote.setTitle(title);
                    mNote.setContent(content);
                    mNote.setCreateTime(mCreateTime);
                    mNote.setModifyTime(TimeUtil.getCurrentDateTime(null));
                    mNote.setAuthor(NoteApp.getInstance().getUser().getUserName());
                    NoteDBImpl.getInstance(mContext).insert(mNote);
                }
                else {
                    if (title.equals(mNote.getTitle()) && content.equals(mNote.getContent())) {
                        // 无任何修改
                    }
                    else {
                        mNote.setTitle(title);
                        mNote.setContent(content);
                        mNote.setModifyTime(TimeUtil.getCurrentDateTime(null));
                        mNote.setAuthor(NoteApp.getInstance().getUser().getUserName());
                        NoteDBImpl.getInstance(mContext).update(mNote);
                    }
                }
            }
            else {
                if (isNewNote && !StringUtils.isEmpty(content)) {
                    // 添加默认标题
                    mNote = new Note();
                    mNote.setTitle(mDefTitle);
                    mNote.setContent(content);
                    mNote.setCreateTime(mCreateTime);
                    mNote.setModifyTime(TimeUtil.getCurrentDateTime(null));
                    mNote.setAuthor(NoteApp.getInstance().getUser().getUserName());
                    NoteDBImpl.getInstance(mContext).insert(mNote);
                }
                else if(!isNewNote && !StringUtils.isEmpty(content)){
                    mNote.setTitle(mDefTitle);
                    mNote.setContent(content);
                    mNote.setModifyTime(TimeUtil.getCurrentDateTime(null));
                    mNote.setAuthor(NoteApp.getInstance().getUser().getUserName());
                    NoteDBImpl.getInstance(mContext).update(mNote);
                }
                else if (isNewNote && StringUtils.isEmpty(content)) {
                    Toast.makeText(mContext, "取消编辑笔记", Toast.LENGTH_SHORT).show();
                }
            }
            setResult(RESULT_OK);
            finish();
        }
        
        return true;
    }

    private static final int REQUEST_UI = 1;

    public void onSpeechInput(View view) {
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
        sb.append(":" + error);
        Toast.makeText(mContext, sb.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        mEtNote.setText(nbest.get(0));
        Toast.makeText(mContext, "识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            onResults(data.getExtras());
        }
    }
}
