package org.steinsapk.pjnotification.object;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.EditText;

import org.steinsapk.pjnotification.R;

public class UserInfo {
    private EditText idInput;
    private EditText passwordInput;
    private CheckBox idCheckBox;
    private CheckBox pwCheckBox;
    private SharedPreferences sharedPreferences;

    public UserInfo(Activity activity) {
        // 뷰 객체 가져오기
        idInput = activity.findViewById(R.id.idInput);
        passwordInput = activity.findViewById(R.id.passwordInput);
        idCheckBox = activity.findViewById(R.id.idCheckBox);
        pwCheckBox = activity.findViewById(R.id.pwCheckBox);

        // SharedPreferences 객체 가져오기
        sharedPreferences = activity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
    }

    public UserInfo(Context context) {
        // SharedPreferences 객체 가져오기
        sharedPreferences = context.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
    }

    public boolean loadUserInfo() {
        // 아이디 가져오기
        if (sharedPreferences.getBoolean("SAVE_ID", false))
            idInput.setText(sharedPreferences.getString("ID", ""));

        // 비밀번호 가져오기
        if (sharedPreferences.getBoolean("SAVE_PW", false))
            passwordInput.setText(sharedPreferences.getString("PW", ""));

        // 체크 박스 상태 가져오기
        idCheckBox.setChecked(sharedPreferences.getBoolean("SAVE_ID", false));
        pwCheckBox.setChecked(sharedPreferences.getBoolean("SAVE_PW", false));

        return sharedPreferences.getBoolean("LOGIN_RECORD", false);
    }

    public void saveUserInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // boolean 값 저장하기
        editor.putBoolean("SAVE_ID", idCheckBox.isChecked());
        editor.putBoolean("SAVE_PW", pwCheckBox.isChecked());
        editor.putBoolean("LOGIN_RECORD", true);

        // 아이디, 비밀번호 저장
        editor.putString("ID", idInput.getText().toString());
        editor.putString("PW", passwordInput.getText().toString());

        // 적용하기
        editor.apply();
    }

    public String getSavedInfo(String str) {
        return sharedPreferences.getString(str, "");
    }

    public boolean getLoginRecord() { return sharedPreferences.getBoolean("LOGIN_RECORD", false); }
}