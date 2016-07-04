package gateway.rest;

import com.alipay.util.AlipayNotify;
import com.lsxy.framework.core.exceptions.MatchMutiEntitiesException;
import com.lsxy.yuhuni.api.recharge.enums.RechargeType;
import com.lsxy.yuhuni.api.recharge.model.Recharge;
import com.lsxy.yuhuni.api.recharge.model.ThirdPayRecord;
import com.lsxy.yuhuni.api.recharge.service.RechargeService;
import com.lsxy.yuhuni.api.recharge.service.ThirdPayRecordService;
import gateway.base.AbstractAPIController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by liups on 2016/7/4.
 */
@RestController
@RequestMapping("/third/alipay")
public class AliPayController extends AbstractAPIController{
    private static final Logger logger = LoggerFactory.getLogger(AliPayController.class);
    @Autowired
    RechargeService rechargeService;

    @Autowired
    ThirdPayRecordService thirdPayRecordService;

    @RequestMapping("/notify")
    public String aliPayNotify(HttpServletRequest request,String trade_status) throws Exception {
        return handleAliPayResult(request, trade_status);
    }

    /**
     * 对支付宝返回的支付结果进行处理
     * @param request
     * @param tradeStatus 返回的支付状态
     * @return 如果校验通过，返回success给支付宝(一定要是success)
     */
    private String handleAliPayResult(HttpServletRequest request, String tradeStatus) throws MatchMutiEntitiesException {
        String result = null;
        Map<String,String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        //将返回的数据进行处理
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        //计算得出通知验证结果
        boolean verify_result = AlipayNotify.verify(params);
        if(verify_result){
            //验证成功
            if(tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")){
                ThirdPayRecord payRecord = new ThirdPayRecord();
                payRecord.setPayType(RechargeType.ZHIFUBAO.getName());
                payRecord.setOrderId(params.get("out_trade_no"));
                payRecord.setTradeNo(params.get("trade_no"));
                payRecord.setTradeStatus(params.get("trade_status"));
                payRecord.setTotalFee(new Double(params.get("total_fee").trim()));
                payRecord.setSellerId(params.get("seller_id"));
                payRecord.setBuyerId(params.get("buyer_id"));
                payRecord.setSellerName(params.get("seller_email"));
                payRecord.setBuyerName(params.get("buyer_email"));
                //对该付款记录进行处理
                Recharge recharge = rechargeService.paySuccess(payRecord.getOrderId());
                payRecord.setRecharge(recharge);
                try {
                    //将付款记录存到数据库
                    thirdPayRecordService.save(payRecord);
                    result =  "success";
                } catch (DataIntegrityViolationException e) {
                    logger.error("插入付款记录失败，交易号已存在，交易号：{}",payRecord.getTradeNo());
                }
            }
        }
        return result;
    }


}
