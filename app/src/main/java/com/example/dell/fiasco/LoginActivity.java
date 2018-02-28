package com.example.dell.fiasco;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dell.fiasco.config.TaskConfig;
import com.example.dell.fiasco.tools.SnackBarCreator;
import com.example.dell.fiasco.utils.SuperTask;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements SuperTask.TaskListener {

    private static final String LOGIN_PREF = "login_pref";
    private static final String LOGIN_ID = "login_id";

    private EditText etEmail;
    private EditText etPassword;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private View rootView;

    private boolean errorEmptyEmail;
    private boolean errorEmptyPassword;
    private boolean errorInvalidLogin;
    private boolean errorVerificationRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rootView = findViewById(R.id.login_root_view);

        tilEmail = (TextInputLayout) findViewById(R.id.login_email_container);
        tilPassword = (TextInputLayout) findViewById(R.id.login_password_container);

        etEmail = (EditText) findViewById(R.id.login_et_email);

        etPassword = (EditText) findViewById(R.id.login_et_password);
        etPassword.setTypeface(Typeface.DEFAULT);
        etPassword.setTransformationMethod(new PasswordTransformationMethod());

        btnLogin = (Button) findViewById(R.id.login_btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                errorEmptyEmail = email.isEmpty();
                errorEmptyPassword = password.isEmpty();

                if (errorEmptyEmail || errorEmptyPassword) {
                    Log.d("fias", "empty");
                    return;
                }

                SuperTask.execute(LoginActivity.this, "login", TaskConfig.LOGIN_URL);
            }
        });

        tvRegister = (TextView) findViewById(R.id.tv_create_account);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREF, MODE_PRIVATE);
        int loggedOut = getIntent().getIntExtra("logout", -1);
        int loginId = sharedPreferences.getInt(LOGIN_ID, -1);

        if (loggedOut != -1) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(LOGIN_ID, -1);
            editor.apply();

            SnackBarCreator.set(R.string.snackbar_success_logout);
        }

        if (loginId != -1 && loggedOut == -1) {
            successfulLogin(loginId, false);
        }
    }

    private void successfulLogin(int id, boolean showLoginMessage) {
        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
        intent.putExtra("show_message", showLoginMessage);
        intent.putExtra("uid", id);

        startActivity(intent);
        finish();
    }

    private void setErrorFields() {
        if (errorEmptyEmail) {
            tilEmail.setError(getResources().getString(R.string.snackbar_fail_empty_email));
        } else {
            tilEmail.setError(null);
        }

        if (errorEmptyPassword) {
            tilPassword.setError(getResources().getString(R.string.snackbar_fail_empty_password));
        } else {
            tilPassword.setError(null);
        }

        if (errorInvalidLogin) {
            tilEmail.setError(getResources().getString(R.string.snackbar_fail_login_invalid));
            tilPassword.setError(getResources().getString(R.string.snackbar_fail_login_invalid));
        }

        if (errorVerificationRequired) {
            tilEmail.setError(getResources().getString(R.string.snackbar_fail_login_invalid));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    @Override
    public void onTaskRespond(String identifier, String json) {
        Log.d("fias", json);
        switch (identifier) {
            case "login": {
                try {
                    Log.d("fias", json);
                    if (true)
                        return;
                    JSONObject jsonObject = new JSONObject(json);
                    int id = jsonObject.getInt("login");

                    errorInvalidLogin = id == 0;
                    errorVerificationRequired = id == -1;

                    if (!(errorInvalidLogin || errorVerificationRequired)) {
                        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREF, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(LOGIN_ID, id);
                        editor.apply();

                        setErrorFields();
                        successfulLogin(id, true);
                    }
                } catch (Exception ignored) {

                }
            }
            break;
        }
        setErrorFields();
    }


    @Override
    public ContentValues setRequestValues(String id, ContentValues contentValues) {
        switch (id) {
            case "login": {
                Log.d("fias", "here1");
                String email = etEmail.getText().toString().trim();
                Log.d("fias", email);
                String password = etPassword.getText().toString().trim();

                contentValues.put("login", true);
                contentValues.put("email", email);
                contentValues.put("password", password);
            }
            break;
        }
        Log.d("fias", "here2");
        return contentValues;
    }
}