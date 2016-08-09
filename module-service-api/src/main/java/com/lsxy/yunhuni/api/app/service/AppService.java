package com.lsxy.yunhuni.api.app.service;

import com.lsxy.framework.api.base.BaseService;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.yunhuni.api.app.model.App;

import java.util.List;

/**
 * 应用相关接口
 * Created by liups on 2016/6/29.
 */
public interface AppService extends BaseService<App> {
    /**
     * 获取记录的集合
     * @param userName
     * @return
     */
    List<App> findAppByUserName(String userName)  ;

    /**
     * 获取分页信息
     * @param userName 用户名
     * @param pageNo 第几页
     * @param pageSize 每页面记录数
     * @return
     */
    Page<App> pageList(String userName,Integer pageNo,Integer pageSize);

    /**
     * 应用是否属于用户
     * @param userName
     * @param appId
     * @return
     */
    boolean isAppBelongToUser(String userName, String appId);

    /**
     * 已上线应用数
     * @return
     */
    long countOnline();

    /**
     * 应用总数
     * @return
     */
    long countValid();
}
