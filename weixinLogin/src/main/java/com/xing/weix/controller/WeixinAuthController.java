package com.xing.weix.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
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
 *
 * 这个入口主要处理，微信扫一扫登录
 *
 */
@Controller
@RequestMapping(value="/weixin")
public class WeixinAuthController {

    protected static Logger logger = Logger.getLogger( WeixinAuthController.class );

    private static String redirect_uri = "http://www.zcsjw.com/guides/weixin/authwechat" ;

    private static String weixinInfoJson = "{\n" +
            "    \"subscribe\": 1, \n" +
            "    \"openid\": \"o6_bmjrPTlm6_2sgVt7hMZOPfL3M\", \n" +
            "    \"nickname\": \"悟能的师兄\", \n" +
            "    \"sex\": 1, \n" +
            "    \"language\": \"zh_CN\", \n" +
            "    \"city\": \"广州\", \n" +
            "    \"province\": \"广东\", \n" +
            "    \"country\": \"中国\", \n" +
            "    \"headimgurl\":\"http://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0\",\n" +
            "    \"subscribe_time\": 1382694957,\n" +
            "    \"unionid\": \" o6_bmasdasdsad6_2sgVt7hMZOPfL\"\n" +
            "}" ;

    @Autowired
    private SendNote confInfo ;
    
    /***
     * 跳转至二维码页面
     * @return
     */
    @RequestMapping(value = "/toCode",method = GET)
    public void weixinRegister( HttpServletRequest request, HttpServletResponse response )throws Exception{
        String tempURL =  WeixinUtil.toRequestCodeUrl( confInfo.weixinAppid , redirect_uri ) ;
        logger.info( "微信登录，跳转至二维码页面：-->" + tempURL );
        response.sendRedirect( tempURL );
    }

    /***
     * 跳转至注册
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/{id}/weixinRegister",method = GET)
    public String weixinRegister(@PathVariable("id") Integer id, Model model){
        MemberChannel memberChannel = null ; //memberChannelService.selectByPrimaryKey( id ) ;
        model.addAttribute("memberChannel", memberChannel);
        return  "/auth/weixinRegister" ;
    }

    /***
     * 跳转至绑定
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/{id}/weixinBind",method = GET)
    public String weixinBind(@PathVariable("id") Integer id, Model model){
        MemberChannel memberChannel = null ; //memberChannelService.selectByPrimaryKey( id ) ;
        model.addAttribute("memberChannel", memberChannel);
        return  "/auth/weixinbind" ;
    }

    /****
     * 测试入口 本地测试入口
     * 回调到此处后，做再次请求至微信端获取会员的信息
     * @return
     */
    @RequestMapping(value = "/authverify",method = GET)
    public String authverify( HttpServletRequest request , HttpServletResponse response ){
        Map<String,Object> responsemap = new HashMap<>() ;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            responsemap = objectMapper.readValue(weixinInfoJson, Map.class); //json转换成map
        }catch ( Exception e ){
            logger.error( e.getMessage() );
        }
        return commonHandler( responsemap ,request , response ) ;
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
            memberChannel = null ; //memberChannelService.selectByOpenid( responsemap.get("openid").toString() ) ;

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
                String weixinInfoJson1 = objectMapper.writeValueAsString(responsemap);
                memberChannel.setMoreinfo(weixinInfoJson1);
            }catch ( Exception e){}
            //去存储 - 换成我们自己的操作即可
            memberChannel = null ; //memberChannelService.insertSelective( memberChannel ) ;
            request.setAttribute("memberChannel", memberChannel);
            // 2.1：跳转至用户去绑定

            return "/auth/weixinbind" ;
        }else{
            if( memberChannel.getMemberId() == null ){
                request.setAttribute("memberChannel", memberChannel);
                // 2.1.2：跳转至用户去绑定
                return "/auth/weixinbind" ;
            }else {
                // 2.2：做正常用户的登录操作
                Member member = null ; //memberService.getByUserid(memberChannel.getMemberId());
                boolean isSuccess = weixinAutoLongin( null , request, response);
                // 2.3：自动登录成功跳转至首页
                if ( isSuccess ) {
                    request.setAttribute("topage","/center");
                    return "/auth/weixinTransfer" ;
                    //return "/center/member/edit";
                    //return infoWrapper.getResults().get("backURL").toString();
                } else {
                    // 2.4：自动登录成功跳转至登录页
                    request.setAttribute("topage","/login.html");
                    return "/auth/weixinTransfer" ;
                }
            }
        }
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

    /**
     * 数据关联成功后的自动登录，session等数据关联绑定的操作
     * @param username
     * @param request
     * @param response
     * @return
     */
    public boolean weixinAutoLongin( String username , HttpServletRequest request, HttpServletResponse response) {
    	return true ;
    }


}
