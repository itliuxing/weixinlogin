package com.xing.weix.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.lang.reflect.Member;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xing.weix.auth.WeixinUtil;
import com.xing.weix.confi.SendNote;
import com.xing.weix.domain.MemberChannel;

/**
 * @Class WeixinAuth
 * @Author 作者姓名:刘兴
 * @Version 1.0
 * @Date 创建时间：2018/8/20 10:00
 * @Copyright Copyright by 智多星
 * @Direction 类说明
 *
 * API：微信登录接口：https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=&lang=zh_CN
 * API：微信用户信息接口：https://www.cnblogs.com/sunshq/p/5132811.html       主要是参考返回的用户信息怎么获取
 *
 *	这个借口主要处理---》已登录的用户，在账户管理端绑定到微信
 *
 */
@Controller
@RequestMapping(value="/weixinbind")
public class WeixinBindController {

    protected static Logger logger = Logger.getLogger( WeixinUtil.class );

    //这个是回调的域名以及地址，这个域名一定要跟你的网站应用配置的域名一样哦，不然微信开放平台是不会回调的
    private static String redirect_uri = "http://www.zcsjw.com/guides/weixinbind/authwechat?memberid=" ;

    @Autowired
    private SendNote confInfo ;
    
    /***
     * 跳转至二维码页面，带上用户ID 用户扫一扫后就知道是谁在做绑定操作，这个数据别人篡改不了的，是微信成功扫描后才会回调
     * @return
     */
    @RequestMapping(value = "/toCode",method = GET)
    public void weixinRegister( HttpServletRequest request, HttpServletResponse response )throws Exception{
        Member member = null ; // ShiroSecurityHelper.getCurrentUser();
        String tempURL =  WeixinUtil.toRequestCodeUrl( confInfo.weixinAppid , redirect_uri + 1 ) ;
        logger.info( "用户自己操作绑定已有用户至微信，跳转至二维码页面：-->" + tempURL );
        response.sendRedirect( tempURL );
    }


    /****
     * 回调到此处后，做再次请求至微信端获取会员的信息
     * 
     * 真实的回调到了这里
     * 
     * @return
     */
    @RequestMapping(value = "/authwechat",method = GET)
    public String authwechat( String state ,HttpServletRequest request, HttpServletResponse response ){

        String code = request.getParameter( "code" ) ;
        logger.info( "回调code:" + code );
        // 1.1 用户扫描后，点击了则回调至此处
        if( StringUtils.isEmpty(code) ) {
            logger.info("用户微信扫描登录，点击了取消登录" );
            return "/login" ;
        }else {
            logger.info("用户微信扫描登录，点击了确认登录" );
            // 1.2 根据返回的code去获取登录有效信息,传入appid,appsecret
            String appid = confInfo.weixinAppid ;
            String appSecret = confInfo.weixinAppSecret ;
            try {
                // 2.1 根据返回的code获取access_token
                // 2.2 刷新access_token有效期 【假如失效自动刷新】
                Map<String, Object> responsemap = WeixinUtil.accessTokenReq(appid, appSecret, code );
                // 3.1 获取用户信息，和其他我们需要的信息
                responsemap = WeixinUtil.userInfoReq(responsemap.get("access_token").toString(), responsemap.get("openid").toString());
                //验证此微信用户是否登录过
                //============================================================================
                	//这一步你们写自己的业务，我前面的这些功能都是封装，封装成一个工具使用
                return commonHandler( responsemap , request , response) ;
                //============================================================================
            } catch (Exception e) {
                logger.error("用户微信扫描登录失败，原因是：" + e.getMessage());
            }

        }
        return "/login" ;
    }

    /****
     * 微信会员信息获取成功后的处理方式 本地业务处理
     * @param responsemap
     * @param request
     * @return
     */
    public String commonHandler( Map<String,Object> responsemap ,HttpServletRequest request , HttpServletResponse response  ){
        MemberChannel memberChannel = null ;
        try {
            // 1：查询此微信用户是否存在
            memberChannel = new MemberChannel() ; //memberChannelService.selectByOpenid( responsemap.get("openid").toString() ) ;

        }catch ( Exception e ){
            logger.error( e.getMessage() );
        }
        // 2：微信用户不存在，则存储一份---》跳转至要用户去绑定账户
        if( memberChannel == null || memberChannel.getId() == null ){
            memberChannel = new MemberChannel() ;
            memberChannel.setHeadImg( responsemap.get("headimgurl").toString() );
            memberChannel.setOpenid( responsemap.get("openid").toString() );
            memberChannel.setName( responsemap.get("nickname").toString() );
            //memberChannel.setMoreinfo( weixinInfoJson );
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String weixinInfoJson = objectMapper.writeValueAsString(responsemap);
                memberChannel.setMoreinfo(weixinInfoJson);
             }catch ( Exception e){}
             //memberChannel.setMemberId( memberid == null ? null : Integer.parseInt(memberid) );
             //memberChannel = memberChannelService.insertSelective( memberChannel ) ;
             request.setAttribute("memberChannel", memberChannel);
             // 2.1：用户第一次绑定成功
             logger.info( "用户第一次绑定成功至微信号：" + responsemap.get("nickname") );
        }else{
            // 2.2 解除绑定后，再次绑定
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String weixinInfoJson = objectMapper.writeValueAsString(responsemap);
                memberChannel.setMoreinfo(weixinInfoJson);
            }catch ( Exception e){}
            //业务处理
            //memberChannel.setMemberId( memberid == null ? null : Integer.parseInt(memberid) );
            //memberChannelService.updateByPrimaryKey( memberChannel ) ;
            logger.info( "用户解除绑定后，再次成功绑定至微信号：" + responsemap.get("nickname") );
        }
        return "/main" ;
    }


}
