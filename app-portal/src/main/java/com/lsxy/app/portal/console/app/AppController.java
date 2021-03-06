package com.lsxy.app.portal.console.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsxy.app.portal.base.AbstractPortalController;
import com.lsxy.app.portal.comm.PortalConstants;
import com.lsxy.app.portal.console.app.vo.AgentVO;
import com.lsxy.app.portal.console.app.vo.ConditionVo;
import com.lsxy.app.portal.console.app.vo.ExtensionVO;
import com.lsxy.call.center.api.model.AppExtension;
import com.lsxy.call.center.api.model.CallCenterAgent;
import com.lsxy.call.center.api.model.Condition;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.framework.config.SystemConfig;
import com.lsxy.framework.core.exceptions.api.YunhuniApiException;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.framework.core.utils.StringUtil;
import com.lsxy.framework.web.rest.RestRequest;
import com.lsxy.framework.web.rest.RestResponse;
import com.lsxy.yunhuni.api.apicertificate.model.ApiCertificate;
import com.lsxy.yunhuni.api.apicertificate.model.ApiCertificateSubAccount;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.resourceTelenum.model.TestNumBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用
 * Created by zhangxb on 2016/7/11.
 */
@Controller
@RequestMapping("/console/app")
public class AppController extends AbstractPortalController {
    private static final Logger logger = LoggerFactory.getLogger(AppController.class);
    /**
     * 应用首页
     * @param request
     * @return
     */
    @RequestMapping("/list")
    public ModelAndView list(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        ModelAndView mav = new ModelAndView();
        RestResponse<Page<App>> restResponse = pageList(request,pageNo,pageSize);
        Page<App> pageObj = restResponse.getData();
        mav.addObject("pageObj",pageObj);
        mav.setViewName("/console/app/list");
        return mav;
    }
    /**
     * 获取租户下所有测试绑定号码
     * @param request
     * @return
     */
    private RestResponse getTestNumBindList(HttpServletRequest request){
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/test_num_bind/list";
        return  RestRequest.buildSecurityRequest(token).getList(uri, TestNumBind.class);
    }

    /**
     * 创建应用首页
     * @param request
     * @return
     */
    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request,String id){
        ModelAndView mav = new ModelAndView();
        if(id!=null&&id.length()>0) {//修改时使用
            RestResponse<App> restResponse = findById(request, id);
            App app = restResponse.getData();
            mav.addObject("app", app);
        }
        mav.setViewName("/console/app/index");
        return mav;
    }

    /**
     * 详情页入口
     * @param request
     * @param id
     * @return
     */
    @RequestMapping("/detail")
    public ModelAndView detail(HttpServletRequest request,String id){
        ModelAndView mav = new ModelAndView();
        RestResponse<App> restResponse = findById(request, id);
        App app = restResponse.getData();
        mav.addObject("app", app);
        List<TestNumBind> testNumBindList = (List<TestNumBind>)getTestNumBindList(request).getData();
//        List<AppExtension> appExtensionList = (List<AppExtension>)getAppExtensionList(request,id).getData();
        mav.addObject("testNumBindList",testNumBindList);
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  + "/rest/app/get/{1}/recording/time";
        mav.addObject("cycle",RestRequest.buildSecurityRequest(token).get(uri, Integer.class,id).getData());
        //        AreaSip areaSip = app.getAreaSip();
//        if(areaSip!=null){
//            mav.addObject("sipRegistrar",app.getAreaSip().getRegistrarIp()+":"+app.getAreaSip().getRegistrarPort());
//        }else{
//            mav.addObject("sipRegistrar","");
//        }
        mav.addObject("sipRegistrar", SystemConfig.getProperty("app.cc.opensips.domain"));
        mav.setViewName("/console/app/detail");
        String uri2 = PortalConstants.REST_PREFIX_URL  + "/rest/account/auth/find_auth_status";
        RestResponse<HashMap> authState =  RestRequest.buildSecurityRequest(token).get(uri2,HashMap.class);
        HashMap hs = authState.getData();
        int authStatus  = Integer.valueOf((hs.get("status")+""));
        mav.addObject("authState",authStatus);
        return mav;
    }
    /**
     * 根据id查找应用
     * @param request
     * @param id
     * @return
     */
    private RestResponse findById(HttpServletRequest request,String id ){
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  + "/rest/app/get/{1}";
        return RestRequest.buildSecurityRequest(token).get(uri, App.class,id);
    }
    /**
     * 获取分页信息
     * @param request
     * @param pageNo
     * @param pageSize
     * @return
     */
    private RestResponse pageList(HttpServletRequest request,Integer pageNo,Integer pageSize){
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  + "/rest/app/plist?pageNo={1}&pageSize={2}";
        return RestRequest.buildSecurityRequest(token).getPage(uri,App.class,pageNo,pageSize);
    }
    @RequestMapping("/delete")
    @ResponseBody
    public RestResponse delete(HttpServletRequest request,String id){
        App app = (App)findById(request,id).getData();
        if(App.STATUS_OFFLINE==app.getStatus()||App.STATUS_WAIT==app.getStatus()||App.STATUS_FAIl==app.getStatus()) {
            //Rest删除应用
            String token = getSecurityToken(request);
            String uri = PortalConstants.REST_PREFIX_URL  + "/rest/app/delete?id={1}";
            RestResponse<App> response = RestRequest.buildSecurityRequest(token).get(uri, App.class, id);
            return response;
        }else{
            return RestResponse.failed("0011","当前应用正在运营中，请将其下线后进行删除");
        }

    }
    @RequestMapping("/edit/recording/{id}")
    @ResponseBody
    public RestResponse edit(HttpServletRequest request,@PathVariable String id,int cycle){
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  + "/rest/app/edit/{1}/recording/{2}";
        return RestRequest.buildSecurityRequest(token).get(uri, String.class, id,cycle);
    }
    /**
     * 新建应用
     * @param request
     * @return
     */
    @RequestMapping("/create")
    @ResponseBody
    public RestResponse create(HttpServletRequest request, App app){
        long count = (Long)countName(request,app.getName()).getData();
        if(count==0) {
            app.setStatus(App.STATUS_OFFLINE);//设置状态为未上线
            createApp(request, app);
            return RestResponse.success();
        }else{
            return RestResponse.failed("0000","当前应用名已存在,创建应用失败");
        }
//        Map map = new HashMap();
//        map.put("msg","新建应用成功");
//        return map;
    }
    /**
     * 用户名是否存在
     * @return
     */
    @RequestMapping("/count/name/{name}")
    @ResponseBody
    public RestResponse count(HttpServletRequest request, @PathVariable String name) {
        long count = (Long) countName(request, name).getData();
        RestResponse restResponse = null;
        if(count==0) {
            restResponse = RestResponse.success();
        }else{
            restResponse = RestResponse.failed("0000","用户名已存在");
        }
        return  restResponse;
    }
    /**
     * 更新应用
     * @param request
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public RestResponse update(HttpServletRequest request, App app){
        return updateApp(request, app);
    }
    /**
     * 新建应用
     * @param request
     * @param app 应用对象
     * @return
     */
    private RestResponse createApp(HttpServletRequest request,App app){
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/app/create";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.convertValue(app, Map.class);
        return RestRequest.buildSecurityRequest(token).post(uri,map, App.class);
    }
    /**
     * 更新应用
     * @param request
     * @param app 应用对象
     * @return
     */
    private RestResponse updateApp(HttpServletRequest request,App app){
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/app/update";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.convertValue(app, Map.class);
        return RestRequest.buildSecurityRequest(token).post(uri,map, App.class);
    }
    /**
     * 根据应用名字查找应用数量
     * @param request
     * @param name 应用名字
     * @return
     */
    private RestResponse countName(HttpServletRequest request,String name){
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/app/count/{1}";
        return RestRequest.buildSecurityRequest(token).get(uri, Long.class,name);
    }


    @RequestMapping(value = "/{appId}/app_extension/page",method = RequestMethod.GET)
    @ResponseBody
    public RestResponse listExtensions(HttpServletRequest request, @PathVariable String appId,
                                       @RequestParam(defaultValue = "1",required = false) Integer  pageNo,
                                       @RequestParam(defaultValue = "20",required = false)  Integer pageSize,
    String extensionNum,String subId) throws YunhuniApiException {
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/callcenter/{1}/app_extension/page?pageNo={2}&pageSize={3}&extensionNum={4}&subId={5}";
        RestResponse<Page<AppExtension>> response = RestRequest.buildSecurityRequest(token).getPage(uri, AppExtension.class, appId,pageNo,pageSize,extensionNum,subId);
        if(response.isSuccess()){
            Page<AppExtension> page = response.getData();
            if(page != null){
                List<ExtensionVO> returnResult = new ArrayList<>();
                List<AppExtension> result = page.getResult();
                Map<String,ApiCertificateSubAccount> map = getMapSubAccountList(request,appId);
                if(result != null){
                    result.stream().forEach(extension -> returnResult.add(ExtensionVO.changeAppExtensionToExtensionVO(extension,map.get(extension.getSubaccountId()))));
                    page.setResult(returnResult);
                }
            }
        }
        return response;
    }
    @RequestMapping(value = "/{appId}/agent/delete/{agentId}")
    @ResponseBody
    public RestResponse agentDelete(HttpServletRequest request, @PathVariable String appId,@PathVariable String agentId) throws YunhuniApiException {
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/callcenter/{1}/agent/delete/{2}";
        RestResponse response = RestRequest.buildSecurityRequest(token).get(uri, String.class,appId,agentId);
        return response;
    }
    @RequestMapping(value = "/{appId}/app_extension/delete/{extensionId}")
    @ResponseBody
    public RestResponse dxtensionDelete(HttpServletRequest request, @PathVariable String appId,@PathVariable String extensionId) throws YunhuniApiException {
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/callcenter/{1}/app_extension/delete/{2}";
        RestResponse response = RestRequest.buildSecurityRequest(token).get(uri, String.class,appId,extensionId);
        return response;
    }
    @RequestMapping(value = "/{appId}/app_extension/new")
    @ResponseBody
    public RestResponse extensionNew(HttpServletRequest request, @PathVariable String appId,String subId,String user,String password) throws YunhuniApiException {
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/callcenter/{1}/app_extension/new?subId={2}&user={3}&password={4}";
        RestResponse response = RestRequest.buildSecurityRequest(token).get(uri, String.class,appId,subId,user,password);
        return response;
    }
    @RequestMapping(value = "/{appId}/agent/page",method = RequestMethod.GET)
    @ResponseBody
    public RestResponse agentpage(HttpServletRequest request, @PathVariable String appId,
                             @RequestParam(defaultValue = "1",required = false) Integer  pageNo,
                             @RequestParam(defaultValue = "20",required = false)  Integer pageSize,String agentNum,String subId) throws YunhuniApiException {
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/callcenter/{1}/agent/page?pageNo={2}&pageSize={3}&agentNum={4}&subId={5}";
        RestResponse<Page<CallCenterAgent>> response = RestRequest.buildSecurityRequest(token).getPage(uri, CallCenterAgent.class, appId,pageNo,pageSize,agentNum,subId);
        if(response.isSuccess()){
            Page page = response.getData();
            if(page != null){
                List<AgentVO> returnResult = new ArrayList<>();
                List<CallCenterAgent> result = page.getResult();
                Map<String,ApiCertificateSubAccount> map = getMapSubAccountList(request,appId);
                if(result != null){
                    result.stream().forEach(agent -> returnResult.add(AgentVO.changeCallCenterAgentToAgentVO(agent,map.get(agent.getSubaccountId()))));
                    page.setResult(returnResult);
                }
            }
        }
        return response;
    }
    @RequestMapping(value = "/{appId}/queue/page",method = RequestMethod.GET)
    @ResponseBody
    public RestResponse queuepage(HttpServletRequest request, @PathVariable String appId,
                             @RequestParam(defaultValue = "1",required = false) Integer  pageNo,
                             @RequestParam(defaultValue = "20",required = false)  Integer pageSize,String queueNum,String subId) throws YunhuniApiException {
        String token = getSecurityToken(request);
        String uri = PortalConstants.REST_PREFIX_URL  +   "/rest/callcenter/{1}/queue/page?pageNo={2}&pageSize={3}&queueNum={4}&subId={5}";
        RestResponse<Page<Condition>> response = RestRequest.buildSecurityRequest(token).getPage(uri, Condition.class, appId,pageNo,pageSize,queueNum,subId);
        if(response.isSuccess()){
            Page<Condition> page = response.getData();
            if(page != null){
                List<ConditionVo> returnResult = new ArrayList<>();
                List<Condition> result = page.getResult();
                Map<String,ApiCertificateSubAccount> map = getMapSubAccountList(request,appId);
                if(result != null){
                    result.stream().forEach(condition -> returnResult.add(ConditionVo.changeConditionToConditionVO( condition,map.get(condition.getSubaccountId()))));
                    page.setResult(returnResult);
                }
            }
        }
        return response;
    }
}
