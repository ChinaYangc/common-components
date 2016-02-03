package com.fansz.event.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by allan on 16/2/3.
 */
public class UserEvent implements Serializable{

    private static final long serialVersionUID = 131526879871163230L;

    private Long id;

    private String sn;

    private String loginname;

    private String password;

    private String mobile;

    private String email;

    private String nickname;

    private String gender;

    private String birthday;

    @JSONField(name = "member_avatar")
    private String memberAvatar;

    private String signature;

    @JSONField(name = "member_type")
    private String memberType;

    @JSONField(name = "member_status")
    private String memberStatus;

    @JSONField(name = "profile_createtime")
    private Date profileCreatetime;

    @JSONField(name = "profile_updatetime")
    private Date profileUpdatetime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getMemberAvatar() {
        return memberAvatar;
    }

    public void setMemberAvatar(String memberAvatar) {
        this.memberAvatar = memberAvatar;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public Date getProfileCreatetime() {
        return profileCreatetime;
    }

    public void setProfileCreatetime(Date profileCreatetime) {
        this.profileCreatetime = profileCreatetime;
    }

    public Date getProfileUpdatetime() {
        return profileUpdatetime;
    }

    public void setProfileUpdatetime(Date profileUpdatetime) {
        this.profileUpdatetime = profileUpdatetime;
    }
}
