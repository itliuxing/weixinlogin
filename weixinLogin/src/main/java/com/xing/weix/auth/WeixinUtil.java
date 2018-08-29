package com.xing.weix.auth;

import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;

import com.xing.weix.util.HttpReqUtil;

/**
 * @Class WeixinUtil
 * @Author 作者姓名:刘兴
 * @Version 1.0
 * @Date 创建时间：2018/8/20 10:02
 * @Copyright Copyright by 智多星
 * @Direction 类说明
 * 
 * 				这个类集成了整个微信开放平台登录体系的四步
 * 					第一步：toRequestUrl  		打开微信二维码	你的开放平台一定要认证
 * 					第二步：accessTokenUrl		通过微信的回调，使用code去微信平台获取访问令牌
 * 					第三步：refreshTokenUrl	根据令牌的有效时间过低，则刷新令牌有效时间
 * 					第四步：snsapiUserinfo		就是根据openid获取我们微信用户信息了
 */
public class WeixinUtil {

    protected static Logger logger = Logger.getLogger( WeixinUtil.class );

    private static String toRequestUrl = "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=zcsjw#wechat_redirect" ;
    // accessToken 获取连接
    private static String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=%s" ;
    // refreshToken 刷新有效期连接
    private static String refreshTokenUrl = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=%s&refresh_token=%s" ;
    // snsapiUser 用户信息获取连接
    private static String snsapiUserinfo = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s" ;

    private static String  refresh_token = "refresh_token" ;

    /**
     *  生成用于获取tocode的Code的Url
     * @param APPID         配置文件
     * @param REDIRECT_URI          回调地址
     * @return
     */
    public static String toRequestCodeUrl(  String APPID , String  REDIRECT_URI  ) {
        REDIRECT_URI = URLEncoder.encode( REDIRECT_URI ) ;
        // 替换%s
        String tempURL = String.format( toRequestUrl ,APPID, REDIRECT_URI  );
        return tempURL ;
    }

    /**
     *  生成用于获取access_token的Code的Url
     * @param APPID         配置文件
     * @param AppSecret     配置文件
     * @param code          回调参数
     * @param grant_type
     * @return
     */
    public static String getRequestCodeUrl(  String APPID ,String AppSecret , String  code  ,String grant_type ) {
        // 替换%s
        return String.format( accessTokenUrl ,APPID, AppSecret, code , grant_type );
    }

    /****
     *  请求公共开放平台获取accessToken信息
     * @param APPID
     * @param AppSecret
     * @param code
     * @return
     */
    public static Map<String,Object> accessTokenReq( String APPID ,String AppSecret , String  code  )throws Exception{
        String accessTokenUrlInfo = getRequestCodeUrl(  APPID ,AppSecret ,  code  , "authorization_code" ) ;
        // get请求开始
        HttpResponse httpResponse = HttpReqUtil.getObjectReq( accessTokenUrlInfo , null ) ;
        // 解析get请求
        Map<String,Object> responsemap = HttpReqUtil.parseHttpResponse( httpResponse ) ;
        // 解析超时时间,然后加入超时了，则调用刷新访问token
        Object expires_in = responsemap.get("expires_in") ;
        if( expires_in != null  ){
            if(  (int)expires_in < 1000 ){
                // 加入失效了，则需要刷新后再能用新的去做获取用户信息
                responsemap = refreshTokenReq( APPID ,  responsemap.get("refresh_token").toString() ) ;
            }
        }
        // 返回扫一扫确认登录的用户openid
        return  responsemap  ;
    }


    /****
     *  请求公共开放平台刷新accessToken信息
     * @param APPID
     * @param refresh_token
     * @return
     */
    public static Map<String,Object> refreshTokenReq( String APPID , String  refresh_token  )throws Exception{
        String accessTokenUrlInfo = String.format( refreshTokenUrl ,APPID, refresh_token, refresh_token );
        // get请求开始
        HttpResponse httpResponse = HttpReqUtil.getObjectReq( accessTokenUrlInfo , "" ) ;
        // 解析get请求
        Map<String,Object> responsemap = HttpReqUtil.parseHttpResponse( httpResponse ) ;
        logger.info( "刷新成功......." );
        // 因为access_token失效了，所以刷新，然后再返回即可，这个后面的获取微信用户的信息时还要使用
        // 理论上不需要这个返回值，因为上面的那个请求也是可以的
        return responsemap ;
    }

    /****
     *  请求公共开放平台获取 根据accessToken、openid 获取用户信息
     * @param access_token
     * @param openid
     * @return
     */
    public static Map<String,Object> userInfoReq( String access_token , String openid  )throws Exception{
        String accessTokenUrlInfo = String.format( snsapiUserinfo , access_token , openid );
        // get请求开始
        HttpResponse httpResponse = HttpReqUtil.getObjectReq( accessTokenUrlInfo , null ) ;
        // 解析get请求
        Map<String,Object> responsemap = HttpReqUtil.parseHttpResponse( httpResponse ) ;
        logger.info( "获取微信用户信息成功......." );
        // 因为access_token失效了，所以刷新，然后再返回即可，这个后面的获取微信用户的信息时还要使用
        // 理论上不需要这个返回值，因为上面的那个请求也是可以的
        return responsemap ;
    }

}
