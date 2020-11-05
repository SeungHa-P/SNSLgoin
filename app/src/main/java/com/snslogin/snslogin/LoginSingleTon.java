package com.snslogin.snslogin;

import com.kakao.usermgmt.response.model.UserAccount;

public class LoginSingleTon {
    private static LoginSingleTon instance;
    private UserAccount kakaoAccount;

    private LoginSingleTon() {  }

    public static LoginSingleTon getInstance(){
        if(instance == null){
            instance = new LoginSingleTon();
        }
        return instance;
    }


    public UserAccount getKakaoAccount() {
        return kakaoAccount;
    }

    public void setKakaoAccount(UserAccount kakaoAccount) {
        this.kakaoAccount = kakaoAccount;
    }
}
