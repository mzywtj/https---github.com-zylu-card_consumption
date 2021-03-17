package com.huohuzhihui.web.controller.api;

import com.huohuzhihui.account.constant.AccountConstants;
import com.huohuzhihui.account.service.IAccUserAccountService;
import com.huohuzhihui.api.service.ApiAccountService;
import com.huohuzhihui.common.core.controller.BaseController;
import com.huohuzhihui.common.core.domain.AjaxResult;
import com.huohuzhihui.common.core.domain.entity.Account;
import com.huohuzhihui.common.core.domain.entity.Card;
import com.huohuzhihui.common.core.domain.entity.SysUser;
import com.huohuzhihui.common.core.domain.model.LoginUser;
import com.huohuzhihui.common.core.page.TableDataInfo;
import com.huohuzhihui.common.exception.CustomException;
import com.huohuzhihui.common.utils.SecurityUtils;
import com.huohuzhihui.common.utils.StringUtils;
import com.huohuzhihui.framework.web.service.TokenService;
import com.huohuzhihui.system.service.ISysDictDataService;
import com.huohuzhihui.trade.domain.TradeOrder;
import com.huohuzhihui.trade.service.ITradeOrderService;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.IpKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户Controller
 * 
 * @author zylu
 * @date 2020-11-14
 */
@RestController
@RequestMapping("/api/account")
public class ApiAccountController extends BaseController
{
    @Autowired
    private ApiAccountService apiAccountService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ISysDictDataService iSysDictDataService;

    /**
     * 个人账户交易记录
     * @return
     */
    @PostMapping("/findTradeRecord")
    public TableDataInfo findTradeRecord( HttpServletRequest request,String beginTime, String endTime){

        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setAccountId(accUserAccount.getId());
        tradeOrder.setBeginTime(beginTime);
        tradeOrder.setEndTime(endTime);
        tradeOrder.setStatus(1);

        startPage();
        List<TradeOrder> list = apiAccountService.findAccountTradeRecordList(tradeOrder);
        return getDataTable(list);
    }


    /**
     * 挂失卡
     * @param request
     * @return
     */
    @PostMapping("/lossCard")
    public AjaxResult lossCard(HttpServletRequest request){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());
        Card card = apiAccountService.findCardByAccoutId(accUserAccount.getId());
        return new AjaxResult(200, "卡挂失成功", apiAccountService.lossCard(card.getId(),sysUser.getUserName()));
    }

    /**
     * 解挂卡
     * @param request
     * @return
     */
    @PostMapping("/unlossCard")
    public AjaxResult unlossCard(HttpServletRequest request){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());
        Card card = apiAccountService.findCardByAccoutId(accUserAccount.getId());
        return new AjaxResult(200, "卡解挂成功", apiAccountService.unlossCard(card.getId(),sysUser.getUserName()));
    }

    /**
     * 获取当前登录人信息
     * @param request
     * @return
     */
    @PostMapping("/getLoginUser")
    public AjaxResult getLoginUser(HttpServletRequest request){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        return new AjaxResult(200, "查询用户信息成功", sysUser);
    }


    /**
     * 修改密码
     * @param request
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @PostMapping("/updatePwd")
    public AjaxResult updatePwd(HttpServletRequest request,String oldPassword,String newPassword){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        if(!SecurityUtils.matchesPassword(oldPassword,sysUser.getPassword())){
            return AjaxResult.error("修改密码失败，原密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, sysUser.getPassword()))
        {
            return AjaxResult.error("修改密码失败，新密码不能与旧密码相同");
        }
        return new AjaxResult(200, "密码修改成功", apiAccountService.resetPassword(sysUser.getUserName(),newPassword));
    }

    /**
     * 获取个人充值总金额
     * @param request
     * @return
     */
    @PostMapping("/getSumRecharge")
    public AjaxResult getSumRecharge(HttpServletRequest request,String beginTime,String endTime){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());

        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setAccountId(accUserAccount.getId());
        tradeOrder.setType(AccountConstants.OPER_RECHARGE);
        tradeOrder.setBeginTime(beginTime);
        tradeOrder.setEndTime(endTime);
        return new AjaxResult(200, "查询充值总金额成功", apiAccountService.getSumTrade(tradeOrder));
    }


    /**
     * 获取个人消费总金额
     * @param request
     * @return
     */
    @PostMapping("/getSumConsume")
    public AjaxResult getSumConsume(HttpServletRequest request,String beginTime,String endTime){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());

        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setAccountId(accUserAccount.getId());
        tradeOrder.setType(AccountConstants.OPER_CONSUME);
        tradeOrder.setBeginTime(beginTime);
        tradeOrder.setEndTime(endTime);
        return new AjaxResult(200, "查询消费总金额成功", apiAccountService.getSumTrade(tradeOrder));
    }

    /**
     * 开通虚拟卡
     * @param request
     * @return
     */
    @PostMapping("/openVirtualCard")
    public AjaxResult openVirtualCard(HttpServletRequest request){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());

        try{
            int count = apiAccountService.openVirtualCard(accUserAccount);
            return new AjaxResult(200, "开通虚拟卡成功",count );
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 查询虚拟卡字符串
     * @param request
     * @return
     */
    @PostMapping("/getQrCodeStr")
    public AjaxResult getQrCodeStr(HttpServletRequest request){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();
        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());
        try{
            String result = apiAccountService.getQrCodeStr(accUserAccount);

            return new AjaxResult(200, "查询虚拟卡成功", result);
        }catch (CustomException e){
            return new AjaxResult(e.getCode(), e.getMessage(), null);
        }
    }
    @PostMapping("/rechargeByWx")
    public AjaxResult rechargeByWx(HttpServletRequest request, BigDecimal amount, String source, String channelCode){
        LoginUser loginUser = tokenService.getLoginUser(request);
        SysUser sysUser = loginUser.getUser();

        Account accUserAccount = apiAccountService.getAccountByUserId(sysUser.getUserId());
        accUserAccount.setUserName(sysUser.getUserName());
        String ip = IpKit.getRealIp(request);
        if (StringUtils.isBlank(ip)) {
            ip = "127.0.0.1";
        }
        return new AjaxResult(200, "获取支付参数成功", apiAccountService.recharge(   accUserAccount, amount,  source, channelCode,sysUser.getOpenId(),ip));
    }
    /**
     * 异步通知
     */
    @RequestMapping(value = "/wxPayNotify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String wxPayNotify(HttpServletRequest request) {
        return  apiAccountService.wxPayNotify(request  );
    }




}
