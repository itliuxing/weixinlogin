package com.xing.weix.confi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 
 * @Class 	SendNote.java
 * @Author 	作者姓名:Liuxing
 * @Version	1.0
 * @Date	创建时间：2018年8月29日 下午3:16:29
 * @Copyright Copyright by Liuxing
 * @Direction 类说明
 */
@Component
public class SendNote {

    private final Logger logger = LoggerFactory.getLogger(SendNote.class);

    @Value("#{appConfig['weixin.appid']}")
    public String weixinAppid;  //微信APPID

    @Value("#{appConfig['weixin.appSecret']}")
    public String weixinAppSecret;  //微信AppSecret





}
