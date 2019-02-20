package com.demo.socketchat.ui.login;

import android.app.Application;

import com.demo.socketchat.ChatApplication;
import com.demo.socketchat.controllers.SocketHelper;
import com.demo.socketchat.di.DaggerViewModelsComponent;

import org.json.JSONObject;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginViewModel extends AndroidViewModel {


    final MutableLiveData<JSONObject> onLoginResponse  = new MutableLiveData<>();

    @Inject
    SocketHelper mSocketHelper;

    private Socket mSocket;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        DaggerViewModelsComponent.builder()
                .applicationComponent(((ChatApplication) application).getApplicationComponent())
                .build()
                .inject(this);
    }

    public void start() {
        mSocket = mSocketHelper.getSocket();
    }

    void socketOn(String action) {
        mSocket.on(action, onLogin);
    }


    void socketEmit(String action, String username) {
        mSocket.emit(action, username);
    }

    void socketOff(String action) {
        mSocket.off(action, onLogin);
    }


    private Emitter.Listener onLogin = args -> onLoginResponse.postValue((JSONObject) args[0]);
}
