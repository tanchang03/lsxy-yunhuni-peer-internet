package com.lsxy.framework.web.rest;

import com.sun.org.apache.xpath.internal.operations.String;

/**
 * Created by Tandy on 2016/6/14.
 * 用户登录后的凭证
 */
public class UserRestToken {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
