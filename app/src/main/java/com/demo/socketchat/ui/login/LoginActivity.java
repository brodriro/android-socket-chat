package com.demo.socketchat.ui.login;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.demo.socketchat.R;
import com.demo.socketchat.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends AppCompatActivity implements View.OnTouchListener {

    private String mUsername;
    private LoginViewModel viewModel;

    @BindView(R.id.username_input)
    EditText mUsernameView;
    @BindView(R.id.sign_in_button)
    Button SignInButton;
    @BindView(R.id.login_scroll)
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        ButterKnife.bind(this);
        observeViewModel();
        setUpViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpViews() {
        mUsernameView.clearFocus();
        mUsernameView.setOnTouchListener(this);
        scrollView.setOnTouchListener(this);

        mUsernameView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }


    @OnClick(R.id.sign_in_button)
    void signIn() {
        attemptLogin();
    }

    private void observeViewModel() {
        viewModel.start();
        viewModel.socketOn(Constants.SOCKET_ON_LOGIN);
        viewModel.onLoginResponse.observe(this, this::onLoginResponse);
    }

    private void onLoginResponse(JSONObject data) {
        int numUsers;
        try {
            numUsers = data.getInt("numUsers");
        } catch (JSONException e) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("username", mUsername);
        intent.putExtra("numUsers", numUsers);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.socketOff(Constants.SOCKET_OFF_LOGIN);
    }


    private void attemptLogin() {
        mUsernameView.setError(null);
        String username = mUsernameView.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }
        mUsername = username;

        viewModel.socketEmit(Constants.SOCKET_EMIT_ADD_USER, username);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mUsernameView.isFocused()) {
            mUsernameView.clearFocus();
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        return false;
    }
}