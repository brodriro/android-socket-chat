package com.demo.socketchat.ui.chat;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.socketchat.R;
import com.demo.socketchat.model.Message;
import com.demo.socketchat.ui.login.LoginActivity;
import com.demo.socketchat.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private static final int REQUEST_LOGIN = 0;
    private static final int TYPING_TIMER_LENGTH = 600;

    private List<Message> mMessages = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;


    @BindView(R.id.messages)
    RecyclerView mMessagesView;
    @BindView(R.id.message_input)
    EditText mInputMessageView;

    private ChatViewModel viewModel;
    private Context mContext;

    public ChatFragment() {
        super();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mAdapter = new MessageAdapter(context, mMessages);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        mContext = getActivity();
        ButterKnife.bind(this, view);
        observeViewModel();
        setUpViews();
        return view;
    }

    private void setUpViews() {
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView.setOnEditorActionListener((v, id, event) -> {
            if (id == R.id.send || id == EditorInfo.IME_NULL) {
                attemptSend();
                return true;
            }
            return false;
        });

        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!viewModel.getSocket().connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    viewModel.socketEmit(Constants.SOCKET_EMIT_TYPING);
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setHasOptionsMenu(true);
    }

    private void observeViewModel() {
        viewModel.start();
        viewModel.isLoginRequired.observe(this, this::isLoginRequired);
        viewModel.onSocketConnect.observe(this, this::onSocketConnect);
        viewModel.onSocketDisconnect.observe(this, this::onSocketDisconnect);
        viewModel.onSocketConnectError.observe(this, this::onSocketConnectError);
        viewModel.onSocketNewMessage.observe(this, this::onSocketNewMessage);
        viewModel.onSocketUserJoined.observe(this, this::onSocketUserJoined);
        viewModel.onSocketUserLeft.observe(this, this::onSocketUserLeft);
        viewModel.onSocketUserTyping.observe(this, this::onSocketUserTyping);
        viewModel.onSocketUserStopTyping.observe(this, this::onSocketUserStopTyping);
    }


    private void onSocketUserStopTyping(JSONObject data) {
        String username;
        try {
            username = data.getString("username");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        removeTyping(username);
    }

    private void onSocketUserTyping(JSONObject data) {
        String username;
        try {
            username = data.getString("username");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        addTyping(username);
    }

    private void onSocketUserLeft(JSONObject data) {
        String username;
        int numUsers;
        try {
            username = data.getString("username");
            numUsers = data.getInt("numUsers");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        addLog(getResources().getString(R.string.message_user_left, username));
        addParticipantsLog(numUsers);
        removeTyping(username);
    }

    private void onSocketUserJoined(JSONObject data) {
        String username;
        int numUsers;
        try {
            username = data.getString("username");
            numUsers = data.getInt("numUsers");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        addLog(getResources().getString(R.string.message_user_joined, username));
        addParticipantsLog(numUsers);
    }

    private void onSocketNewMessage(JSONObject data) {
        String username;
        String message;
        try {
            username = data.getString("username");
            message = data.getString("message");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        removeTyping(username);
        addMessage(username, message);
    }

    private void onSocketConnectError(Boolean aBoolean) {
        if (!aBoolean) return;
        Toast.makeText(mContext,
                R.string.error_connect, Toast.LENGTH_LONG).show();
    }

    private void onSocketDisconnect(Boolean aBoolean) {
        if (!aBoolean) return;
        Toast.makeText(mContext,
                R.string.disconnect, Toast.LENGTH_LONG).show();
    }

    private void onSocketConnect(Boolean aBoolean) {
        if (!aBoolean) return;

        if (null != mUsername) viewModel.socketEmit(Constants.SOCKET_EMIT_ADD_USER, mUsername);

        Toast.makeText(mContext,
                R.string.connect, Toast.LENGTH_LONG).show();
    }

    private void isLoginRequired(Boolean aBoolean) {
        if (!aBoolean) return;
        startSignIn();
    }

    @OnClick(R.id.send_button)
    void SendMessage() {
        attemptSend();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            Objects.requireNonNull(getActivity()).finish();
            return;
        }

        mUsername = data.getStringExtra("username");
        int numUsers = data.getIntExtra("numUsers", 1);

        addLog(getResources().getString(R.string.message_welcome));
        addParticipantsLog(numUsers);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_leave) {
            leave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addLog(String message) {
        viewModel.addLog(mMessages, mAdapter, message);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }

    private void addMessage(String username, String message) {
        viewModel.addMessage(mMessages, mAdapter, username, message);
        scrollToBottom();
    }

    private void addTyping(String username) {
        viewModel.addTyping(mMessages, mAdapter, username);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        viewModel.removeTyping(mMessages, mAdapter, username);
    }

    private void attemptSend() {
        if (null == mUsername) return;
        if (!viewModel.socketIsConnected()) return;
        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message);

        viewModel.socketEmit(Constants.SOCKET_EMIT_NEW_MESSAGE, message);
    }

    private void startSignIn() {
        mUsername = null;

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity());
            startActivityForResult(intent, REQUEST_LOGIN, options.toBundle());
        } else  {
            startActivityForResult(intent, REQUEST_LOGIN);
        }


    }

    private void leave() {
        mUsername = null;
        viewModel.logout();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }


    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;
            mTyping = false;
            viewModel.socketEmit(Constants.SOCKET_EMIT_STOP_TYPING);
        }
    };
}