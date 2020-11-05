package com.snslogin.snslogin.manager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.snslogin.snslogin.listener.LoginListener;


import org.json.JSONObject;

public class FLoginCallback implements FacebookCallback<LoginResult> {
    private Context context;
    private LoginListener loginListener;
    public FLoginCallback(Context context,LoginListener loginListener){
        this.context=context;
        this.loginListener=loginListener;
    }

    @Override
    public void onSuccess(LoginResult loginResult) {

        requestMe(loginResult.getAccessToken());




    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }

    public void requestMe(AccessToken token){
        GraphRequest graphRequest = GraphRequest.newMeRequest(token
                , new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e("resut",object.toString());
                        String id = object.optString("id");
                        String name = object.optString("name");
                        String email = object.optString("email");
                        String gender = object.optString("gender");
                        String birthday = object.optString("birthday");

                        Log.d("facedata",id+"\n"+name+"\n"+email+"\n"+gender+"\n"+birthday);
                        loginListener.facebookUserData(id,name,email,gender,birthday);

                    }
                });


        Toast.makeText(context,"asdlkfgalksdjf",Toast.LENGTH_LONG).show();
        loginListener.loginSuccess();
        Bundle bundle = new Bundle();
        bundle.putString("fields","id,name,email,gender,birthday");
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();


    }
}
