package com.lsxy.area.api.exceptions;

/**
 * Created by liups on 2016/8/23.
 */
public class BalanceNotEnough extends RuntimeException {
    public BalanceNotEnough(String message) {
        super(message);
    }
}
