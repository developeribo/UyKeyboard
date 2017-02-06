package com.ibo.uykeyboard;

import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ibo.keyboard.KeyboardUtil;

public class MainActivity extends AppCompatActivity  {

    /***
     * UyKeyboard 1.0
     * author : ibo
     * date : 2017/2/6
     * email : developeribo@outlook.com
     *
     * 使用：
     * 1.manifet 里面 activity 属性添加 android:windowSoftInputMode="stateVisible|adjustResize"
     * 2.layout 最外层放这个 com.ibo.keyboard.KeyboardListenRelativeLayout
     * 3.new KeyboardUtil(this);
     * 4.keyboardUtil.addUyET(uy1);  可以添加多个EditText
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText uy1 = (EditText) findViewById(R.id.uy1);
        EditText uy2 = (EditText) findViewById(R.id.uy2);

        KeyboardUtil keyboardUtil=new KeyboardUtil(this);
        keyboardUtil.addUyET(uy1);
        keyboardUtil.addUyET(uy2);
    }

    @Override
    public void onBackPressed() {}

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
