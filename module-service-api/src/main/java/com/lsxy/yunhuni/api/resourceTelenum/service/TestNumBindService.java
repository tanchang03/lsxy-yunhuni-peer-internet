package com.lsxy.yunhuni.api.resourceTelenum.service;

import com.lsxy.framework.api.base.BaseService;
import com.lsxy.yunhuni.api.resourceTelenum.model.TestNumBind;

import java.util.List;

/**
 * 测试绑定号码
 * Created by zhangxb on 2016/7/2.
 */
public interface TestNumBindService extends BaseService<TestNumBind>  {
    /**
     * 查询全部测试绑定号码
     * @param userName 用户名
     * @return
     */
    public List<TestNumBind> findAll(String userName )  ;

    /**
     * 根据手机号码查找测试号码对象
     * @param number 查找的手机号码
     * @param userName 用户名
     * @return
     */
    public List<TestNumBind> findByNumber(String userName, String number)  ;

    public List<TestNumBind> findByTenant(String tenant,String appId);

    public TestNumBind findByNumber(String number)  ;

    /**
     * 根据应用ID获取测试号码
     * @param appId
     * @return
     */
    List<String> findNumByAppId(String appId);
}
