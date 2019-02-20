package com.demo.socketchat.ui.chat;

import android.app.Application;

import com.demo.socketchat.ChatApplication;
import com.demo.socketchat.controllers.SocketHelper;
import com.demo.socketchat.di.DaggerViewModelsComponent;
import com.demo.socketchat.model.Message;
import com.demo.socketchat.utils.Constants;

import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ChatViewModel extends AndroidViewModel {

    final MutableLiveData<Boolean> isLoginRequired = new MutableLiveData<>();
    final MutableLiveData<Boolean> onSocketConnect = new MutableLiveData<>();
    final MutableLiveData<Boolean> onSocketDisconnect = new MutableLiveData<>();
    final MutableLiveData<Boolean> onSocketConnectError = new MutableLiveData<>();
    final MutableLiveData<JSONObject> onSocketNewMessage = new MutableLiveData<>();
    final MutableLiveData<JSONObject> onSocketUserJoined = new MutableLiveData<>();
    final MutableLiveData<JSONObject> onSocketUserLeft = new MutableLiveData<>();
    final MutableLiveData<JSONObject> onSocketUserTyping = new MutableLiveData<>();
    final MutableLiveData<JSONObject> onSocketUserStopTyping = new MutableLiveData<>();

    final MutableLiveData<Integer> onRemoveTyping  = new MutableLiveData<>();

    @Inject
    SocketHelper mSocketHelper;

    private Socket mSocket;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        DaggerViewModelsComponent.builder()
                .applicationComponent(((ChatApplication) application).getApplicationComponent())
                .build().inject(this);
    }


    public void start() {
        mSocket = mSocketHelper.getSocket();

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on(Constants.SOCKET_ACTION_NEW_MESSAGE, onNewMessage);
        mSocket.on(Constants.SOCKET_ACTION_USER_JOINED, onUserJoined);
        mSocket.on(Constants.SOCKET_ACTION_USER_LEFT, onUserLeft);
        mSocket.on(Constants.SOCKET_ACTION_TYPING, onTyping);
        mSocket.on(Constants.SOCKET_ACTION_STOP_TYPING, onStopTyping);
        mSocket.connect();

        isLoginRequired.postValue(true);
    }

    void destroy() {
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off(Constants.SOCKET_ACTION_NEW_MESSAGE, onNewMessage);
        mSocket.off(Constants.SOCKET_ACTION_USER_JOINED, onUserJoined);
        mSocket.off(Constants.SOCKET_ACTION_USER_LEFT, onUserLeft);
        mSocket.off(Constants.SOCKET_ACTION_TYPING, onTyping);
        mSocket.off(Constants.SOCKET_ACTION_STOP_TYPING, onStopTyping);
    }


    private Emitter.Listener onConnect = args -> onSocketConnect.postValue(true);

    private Emitter.Listener onDisconnect = args -> onSocketDisconnect.postValue(true);

    private Emitter.Listener onConnectError = args -> onSocketConnectError.postValue(true);

    private Emitter.Listener onNewMessage = args -> onSocketNewMessage.postValue((JSONObject) args[0]);

    private Emitter.Listener onUserJoined = args -> onSocketUserJoined.postValue((JSONObject) args[0]);

    private Emitter.Listener onUserLeft = args -> onSocketUserLeft.postValue((JSONObject) args[0]);

    private Emitter.Listener onTyping = args -> onSocketUserTyping.postValue((JSONObject) args[0]);

    private Emitter.Listener onStopTyping = args -> onSocketUserStopTyping.postValue((JSONObject) args[0]);


    void logout() {
        mSocket.disconnect();
        mSocket.connect();
        isLoginRequired.postValue(true);
    }

    Socket getSocket() {
        return mSocket;
    }

    boolean socketIsConnected() {
        return getSocket().connected();
    }

    void socketEmit(String param) {
        socketEmit(param, null);
    }

    void socketEmit(String param, String message) {
        if (message == null) mSocket.emit(param);
        else mSocket.emit(param, message);
    }

    public void removeTyping(List<Message> mMessages, RecyclerView.Adapter mAdapter, String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    public void addTyping(List<Message> mMessages, RecyclerView.Adapter mAdapter, String username) {
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
    }

    public void addMessage(List<Message> mMessages, RecyclerView.Adapter mAdapter, String username, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
    }

    public void addLog(List<Message> mMessages, RecyclerView.Adapter mAdapter, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
    }
}
