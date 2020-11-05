package com.snslogin.snslogin.listener;

import com.kakao.usermgmt.response.model.AgeRange;
import com.kakao.usermgmt.response.model.Gender;
import com.kakao.usermgmt.response.model.Profile;

public interface LoginListener {
    void loginSuccess();
    void kakaoUserData(Profile profile, String email, AgeRange age_range,
                       String birthday, Gender gender);
    void facebookUserData(String id,String name,String email,String gender,String birthday);
    void naverUserData();
    void googleUserData();


}
