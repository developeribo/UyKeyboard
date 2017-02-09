package com.ibo.keyboard;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class KeyboardUtil implements View.OnClickListener, View.OnFocusChangeListener, IOnKeyboardStateChangedListener {

    static final byte KEYBOARD_STATE_SHOW = -3;
    static final byte KEYBOARD_STATE_HIDE = -2;
    static final byte KEYBOARD_STATE_INIT = -1;

    private Activity act;
    private KeyboardView keyboardView;
    private Keyboard k1;// 字母键盘
    private boolean isUpper = false;// 是否大写
    private boolean SystemInputIsShowing = false;
    private boolean isOnceFocus = false;
    private ViewGroup mFloatView;
    private EditText ed;

    public KeyboardUtil(Activity act) {
        this.act = act;
        setSystemInputListener();
        k1 = new Keyboard(act, R.xml.qwerty);
        keyboardView = (KeyboardView) act.findViewById(R.id.keyboard_view);
        keyboardView.setKeyboard(k1);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(true);
        keyboardView.setOnKeyboardActionListener(listener);

        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);

        initFloatView();
    }

    private void initFloatView() {
        mFloatView = (ViewGroup) act.findViewById(R.id.change_2_ug);
        mFloatView.setOnClickListener(this);
    }

    private void setSystemInputListener() {
        View childAt = ((ViewGroup) act.findViewById(android.R.id.content)).getChildAt(0);

        if (childAt instanceof KeyboardListenRelativeLayout) {
            ((KeyboardListenRelativeLayout) childAt).setOnKeyboardStateChangedListener(this);
        }
    }

    public void addUyET(EditText editText) {
        this.ed = editText;
        disableShowSoftInput();
        ed.setOnFocusChangeListener(this);
        ed.setOnClickListener(this);
        keyboardView.setOnKeyboardActionListener(listener);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (isOnceFocus) {
            isOnceFocus = false;
            return;
        }
        if (hasFocus) {
            edCliked(v);
        } else {
            mFloatView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_2_ug) {
            hideAll();
            showKeyboard();
        } else {
            if (SystemInputIsShowing) return;
            if (!keyboardView.isShown()) {
                showKeyboard();
            }
        }
    }

    public void hideAll() {
        mFloatView.setVisibility(View.GONE);
        keyboardView.setVisibility(View.GONE);
        hideSysKeyboard();
        mFloatView.setVisibility(View.GONE);
    }

    private void edCliked(View v) {
        if (v instanceof EditText) {
            EditText ev = (EditText) v;
            addUyET(ev);
            if (SystemInputIsShowing) {
                mFloatView.setVisibility(View.VISIBLE);
                return;
            }
            showKeyboard();
        }
    }

    @Override
    public void onKeyboardStateChanged(int state, int height) {
        SystemInputIsShowing = (state == KEYBOARD_STATE_SHOW);
        if (SystemInputIsShowing){
            hideKeyboard();
        }else if (state == KEYBOARD_STATE_HIDE){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFloatView.setVisibility(View.GONE);
                }
            }, 100);
        }
    }

    /**
     * 禁止Edittext弹出软件盘，光标依然正常显示。
     */
    private void disableShowSoftInput() {
        if (Build.VERSION.SDK_INT <= 10) {
            ed.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(ed, false);
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(ed, false);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public Boolean isShowing() {
        return keyboardView.isShown();
    }

    private KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onPress(int primaryCode) {
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (ed == null) return;

            Editable editable = ed.getText();

            int start = ed.getSelectionStart();

            if (primaryCode == Keyboard.KEYCODE_CANCEL) {// 完成
                hideKeyboard();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {// 回退
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {// 大小写切换
                changeKey();
                keyboardView.setKeyboard(k1);
            } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {// 键盘切换
                changeToSystemInput();
            } else {
                String string = Character.toString((char) primaryCode);
                editable.insert(start, string);
            }
        }
    };

    private void changeToSystemInput() {
        hideKeyboard();

        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        mFloatView.setVisibility(View.VISIBLE);
    }

    private void hideSysKeyboard() {
        if (ed == null) return;
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ed.getWindowToken(), 0);
    }

    private static HashMap<Integer, Integer> shift_map = new HashMap<>();

    static {
        shift_map.put(0x062F, (int) 'ژ');
        shift_map.put(0x0627, (int) 'ف');
        shift_map.put(0x06D5, (int) 'گ');
        shift_map.put(0x0649, (int) 'خ');
        shift_map.put(0x0642, (int) 'ج');
        shift_map.put(0x0643, (int) 'ۆ');
    }

    /**
     * 键盘大小写切换
     */
    private void changeKey() {
        List<Key> keylist = k1.getKeys();
        if (isUpper) {//小写
            isUpper = false;
            for (Key key : keylist) {
                if (shift_map.containsValue(key.codes[0])) {

                    for (Integer mapKey : shift_map.keySet()) {
                        if (shift_map.get(mapKey) == key.codes[0]) {
                            if (mapKey != null) {
                                key.codes[0] = mapKey;
                                int i = mapKey;
                                key.icon =act.getResources().getDrawable(getIcon(Character.toString((char) i)));
                                key.iconPreview = keyboardView.getContext().getResources().getDrawable(getIcon(Character.toString((char) i)));
                            }
                        }
                    }
                }
            }
        } else { //大写
            isUpper = true;

            for (Key key : keylist) {
                if (shift_map.containsKey(key.codes[0])) {
                    Integer o = shift_map.get(key.codes[0]);
                    if (o != null) {
                        key.codes[0] = o;
                        int i = o;
                        key.icon = keyboardView.getContext().getResources().getDrawable(getIcon(Character.toString((char) i)));
                        key.iconPreview = keyboardView.getContext().getResources().getDrawable(getIcon(Character.toString((char) i)));
                    }
                }
            }
        }
    }

    private int getIcon(String s) {
        s = s.trim();
        switch (s) {
            case "د":
                return R.drawable.key_d;
            case "ا":
                return R.drawable.key_a;
            case "ە":
                return R.drawable.key_ga;
            case "ى":
                return R.drawable.key_i;
            case "ق":
                return R.drawable.key_sj;
            case "ك":
                return R.drawable.key_k;
            case "ژ":
                return R.drawable.key_sd;
            case "ف":
                return R.drawable.key_f;
            case "گ":
                return R.drawable.key_g;
            case "خ":
                return R.drawable.key_h;
            case "ج":
                return R.drawable.key_j;
            case "ۆ":
                return R.drawable.key_sk;
        }
        return R.drawable.key_a;
    }

    private void showKeyboard() {
        int delay = 0;
        if (SystemInputIsShowing) {
            hideSysKeyboard();
            delay = 200;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                keyboardView.setVisibility(View.VISIBLE);
            }
        }, delay);
    }

    private void hideKeyboard() {
        keyboardView.setVisibility(View.GONE);
    }
}