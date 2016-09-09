package com.lsxy.app.api.gateway.rest;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lsxy.app.api.gateway.dto.VerifyCallInputDTO;
import com.lsxy.app.api.gateway.response.ApiGatewayResponse;
import com.lsxy.area.api.CallService;
import com.lsxy.area.api.DuoCallbackDTO;
import com.lsxy.area.api.NotifyCallDTO;
import com.lsxy.area.api.exceptions.RequestIllegalArgumentException;
import com.lsxy.area.api.exceptions.YunhuniApiException;
import com.lsxy.framework.web.utils.WebUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tandy on 2016/6/28.
 * 呼叫API入口
 */
@RestController
public class CallController extends AbstractAPIController{
    private static final Logger logger = LoggerFactory.getLogger(CallController.class);

    @Reference(timeout=3000)
    private CallService callService;

//    @Autowired
//    private MQService mqService;
//
//    @Autowired(required = false)
//    private StasticsCounter sc;
//
//    @Autowired
//    private AsyncRequestContext asyncRequestContext;
//
//    @Autowired
//    private AppService appService;

//    /**
//     * 自动化测试用例使用
//     * @param accountId
//     * @return
//     */
//    @RequestMapping("/{accountId}/calltest")
//    public ApiGatewayResponse test(@PathVariable String accountId){
//        return ApiGatewayResponse.Success(accountId);
//    }
//@RequestMapping("/{accountId}/call")
//public DeferredResult<ApiGatewayResponse> doCall(@PathVariable String accountId, HttpServletResponse response, HttpServletRequest request){

//    /**
//     *  语音呼叫API
//     *  api.dev.yunhuni.com/v1/account/1234567/call?to=13971068693&maxAnswerSec=10&maxRingSec=20
//     * @param certId   鉴权账号
//     * @param response
//     * @param request
//     * @return
//     */
//    @RequestMapping("/{certId}/call")
//    public ApiGatewayResponse<String> doCall(@PathVariable String certId, HttpServletResponse response, HttpServletRequest request, String to, String from, int maxAnswerSec, int maxRingSec) throws YunhuniApiException {
//        if(logger.isDebugEnabled()){
//            WebUtils.logRequestParams(request);
//        }
//        /*发送请求次数计数*/
//        if(sc!=null)sc.getSendGWRequestCount().incrementAndGet();
//
//        String callid = callService.call(from,to,maxAnswerSec,maxRingSec);
//        return ApiGatewayResponse.success(callid);
//
//    }
//
//    @RequestMapping("/{accountId}/call/{callId}")
//    public ApiGatewayResponse getCall(@PathVariable String accountId,@PathVariable String callId){
//        return ApiGatewayResponse.success(callId);
//    }

    @RequestMapping(value = "/{account_id}/call/duo_callback",method = RequestMethod.POST)
    public ApiGatewayResponse duoCallback(HttpServletRequest request, @RequestBody DuoCallbackDTO duoCallbackDTO, @PathVariable String account_id) throws YunhuniApiException {
        String appId = request.getHeader("AppID");
        String ip = WebUtils.getRemoteAddress(request);
        String callId = null;

        callId = callService.duoCallback(ip,appId, duoCallbackDTO);

        Map<String,String> result = new HashMap<>();
        result.put("callId",callId);
        result.put("user_data", duoCallbackDTO.getUser_data());
        return ApiGatewayResponse.success(result);
    }

    @RequestMapping(value = "/{account_id}/call/duo_callback/cancel",method = RequestMethod.POST)
    public ApiGatewayResponse duoCallback(HttpServletRequest request, @RequestBody Map params, @PathVariable String account_id) throws YunhuniApiException {
        String appId = request.getHeader("AppID");
        String ip = WebUtils.getRemoteAddress(request);
        callService.duoCallbackCancel(ip,appId, (String)params.get("callId"));
        return ApiGatewayResponse.success();
    }

    @RequestMapping(value = "/{account_id}/call/notify_call",method = RequestMethod.POST)
    public ApiGatewayResponse duoCallback(HttpServletRequest request, @RequestBody NotifyCallDTO notifyCallDTO, @PathVariable String account_id) throws YunhuniApiException {
        String appId = request.getHeader("AppID");
        String ip = WebUtils.getRemoteAddress(request);
        //参数校验
        if(StringUtils.isBlank(notifyCallDTO.getTo())){
            throw new RequestIllegalArgumentException();
        }
        String callId = callService.notifyCall(ip,appId, notifyCallDTO);
        Map<String,String> result = new HashMap<>();
        result.put("callId",callId);
        result.put("user_data", notifyCallDTO.getUser_data());
        return ApiGatewayResponse.success(result);
    }

    @RequestMapping(value = "/{account_id}/call/verify_call",method = RequestMethod.POST)
    public ApiGatewayResponse verify_call(HttpServletRequest request, @RequestBody VerifyCallInputDTO dto, @PathVariable String account_id) throws YunhuniApiException {
        String appId = request.getHeader("AppID");
        String ip = WebUtils.getRemoteAddress(request);

        //参数校验
        checkInputLen(dto.getFrom());
        if(StringUtils.isBlank(dto.getTo())){
            throw new RequestIllegalArgumentException();
        }

        if(dto.getMaxDialDuration() !=null && dto.getMaxDialDuration() <=0){
            throw new RequestIllegalArgumentException();
        }

        if(StringUtils.isBlank(dto.getPlayFile()) && StringUtils.isBlank(dto.getVerifyCode())){
            throw new RequestIllegalArgumentException();
        }

        if(StringUtils.isNotBlank(dto.getVerifyCode())
                || dto.getVerifyCode().length()>12
                || !StringUtils.isNumeric(dto.getVerifyCode())){
            throw new RequestIllegalArgumentException();
        }

        if(dto.getRepeat() != null && dto.getRepeat()<0){
            throw new RequestIllegalArgumentException();
        }

        checkInputLen(dto.getUserData());

        String callId = callService.verifyCall(ip,appId, dto.getFrom(),dto.getTo(),dto.getMaxDialDuration(),dto.getVerifyCode(),dto.getPlayFile(),dto.getRepeat(),dto.getUserData());
        Map<String,String> result = new HashMap<>();
        result.put("callId",callId);
        return ApiGatewayResponse.success(result);
    }

    /**
     * 高级版语音验证码，接受用户输入验证码
     */
    /*@RequestMapping(value = "/{account_id}/call/captcha_call",method = RequestMethod.POST)
    public ApiGatewayResponse captcha_call(HttpServletRequest request, @RequestBody CaptchaCallDTO dto, @PathVariable String account_id) throws YunhuniApiException {
        String appId = request.getHeader("AppID");
        String ip = WebUtils.getRemoteAddress(request);

        //参数校验
        checkInputLen(dto.getFrom());
        if(StringUtils.isBlank(dto.getTo())){
            throw new RequestIllegalArgumentException();
        }
        if(dto.getMax_dial_duration() !=null && dto.getMax_dial_duration() <=0){
            throw new RequestIllegalArgumentException();
        }
        checkInputLen(dto.getVerify_code());
        if(dto.getMax_keys()!=null && dto.getMax_keys() <=0){
            throw new RequestIllegalArgumentException();
        }
        checkInputLen(dto.getUser_data());

        String callId = callService.captchaCall(ip,appId, dto);
        Map<String,String> result = new HashMap<>();
        result.put("callId",callId);
        return ApiGatewayResponse.success(result);
    }*/

//
//
//        @RequestMapping("/async/test")
//        @ResponseBody
//        public Callable<ApiGatewayResponse> callable(HttpServletResponse response) {
//            if(logger.isDebugEnabled()){
//                logger.debug("111111111");
//            }
//            // 这么做的好处避免web server的连接池被长期占用而引起性能问题，
//            // 调用后生成一个非web的服务线程来处理，增加web服务器的吞吐量。
//            return new Callable<ApiGatewayResponse>() {
//                @Override
//                public ApiGatewayResponse call() throws Exception {
//
//                    return ApiGatewayResponse.Success("中文2");
//                }
//            };
//        }


//    public static void main(String[] args) {
//        RestRequest restRequest = RestRequest.buildRequest();
//        Map<String,Object> hashMap =  new HashedMap();
//        hashMap.put("res_id","5566778");
//        ApiGatewayResponse<String> response = restRequest.post("http://www.yunhuni.cn:3000/incoming",hashMap);
//        System.out.println(response.getData());
//    }
}
