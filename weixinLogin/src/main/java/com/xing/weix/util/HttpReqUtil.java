package com.xing.weix.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/***
 * *
 * 类名称：		HttpService.java 
 * 类描述：   		http 请求工具类，莫有那么多什么异常什么异常，分分钟搞定的事情，搞那么复杂
 * 创建人：		
 * 创建时间：		2016-11-16下午5:14:37 
 * 修改人：		liuxing
 * 修改时间：		2016-11-16下午5:14:37 
 * 修改备注：   		API Code:http://hc.apache.org/httpcomponents-core-4.3.x/examples.html
 * @version
 */
public class HttpReqUtil  extends AsyncHttp {

	protected static Logger logger = Logger.getLogger(HttpReqUtil.class);
	private static String userAgent = "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36";

	private HttpReqUtil() {}

	/**
	 * post 请求直接请求
	 * @param url 			请求地址
	 * @param param  		请求参数，字符串
	 * @return 				相应结果
	 * @throws
	 */
	public static HttpResponse postReq(String url, String param) throws Exception {
		HttpPost post = new HttpPost(url);
		HttpResponse httpResponse = null ;
		try {
//			post.setConfig(requestConfig); // 父类定义的超时信息，在每个请求都要置入配置参数

			post.setEntity(new StringEntity(param, ContentType.create(
					"application/x-www-form-urlencoded", Consts.UTF_8)));

			httpResponse = createHttpClient().execute(post); // 将post请求交给HttpClient请求去执行

		} catch (IOException e) {
			return null ;
		}
		return httpResponse;
	}
	
	/** 
     * get请求 
     * @return 
     */  
    public static String doGet(String url) {  
        try {  
            HttpClient client = createHttpClient();  
            //发送get请求  
            HttpGet request = new HttpGet(url);  
            HttpResponse response = client.execute(request);  
   
            /**请求发送成功，并得到响应**/  
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
                /**读取服务器返回过来的json字符串数据**/  
                String strResult = EntityUtils.toString(response.getEntity());  
                  
                return strResult;  
            }  
        }catch (IOException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  

	/**
	 * post 对象请求
	 * @param httpUri		请求地址
	 * @param param   	参数对象
	 * @return			请求结果
	 * @throws
	 */
	public static HttpResponse postObjectReq(String httpUri, Object param)throws Exception {
		HttpPost post = new HttpPost( httpUri );
		String result = "";
		HttpResponse httpResponse = null ;
		try {
//			post.setConfig(requestConfig); // 父类定义的超时信息，在每个请求都要置入配置参数

			post.setHeader("User-Agent",userAgent);					//需要加这个参数，否则返回403 状态吗
			post.addHeader("Content-type", "application/json; charset=utf-8");		//设置请求头部
			post.setHeader("Accept", "application/json");

			/*	post.addHeader("Content-type", "application/text");	
				post.addHeader("Charset", "UTF-8"); */
			ObjectMapper objectMapper = new ObjectMapper();
			String josnParm = objectMapper.writeValueAsString(param);
			//String josnParm = JSONObject.toJSONString(param).toString();			//将请求对象由java对象置为json行字符对象。因为请求内设定了请求数据类型为json类型
			post.setEntity(new StringEntity(josnParm));
			
			httpResponse = createHttpClient().execute(post);					//将post请求交给HttpClient 请求去执行
		} catch (IOException e) {
			return null ;
		}

		return httpResponse ;
	}
	
	/**
	 * post 对象请求
	 * @param httpUri		请求地址
	 * @param param   	参数对象
	 * @return			请求结果
	 * @throws
	 */
	public static HttpResponse postObjectReqForm(String httpUri, Object param)throws Exception {
		HttpPost post = new HttpPost( httpUri );
		HttpResponse httpResponse = null ;
		try {
//			post.setConfig(requestConfig); // 父类定义的超时信息，在每个请求都要置入配置参数
			post.setHeader("User-Agent",userAgent);					//需要加这个参数，否则返回403 状态吗
			post.addHeader("Content-type", "application/form-data; charset=UTF-8");		//设置请求头部
			post.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");

			ObjectMapper objectMapper = new ObjectMapper();
			String josnParm = objectMapper.writeValueAsString(param);
			//String josnParm = JSONObject.toJSONString(param).toString();			//将请求对象由java对象置为json行字符对象。因为请求内设定了请求数据类型为json类型
			josnParm = josnParm.substring(1, josnParm.length()) ;
			josnParm = josnParm.substring(0, josnParm.length()-1) ;
			josnParm = josnParm.replaceAll(",", "/n");
			post.setEntity(new StringEntity(josnParm));
			
			httpResponse = createHttpClient().execute(post);					//将post请求交给HttpClient 请求去执行
		} catch (IOException e) {
			return null ;
		}

		return httpResponse ;
	}
	
	/** 
     * 模拟请求 
     *  
     * @param httpUri     资源地址
     * @param param   参数列表
     * @return 
     * @throws ParseException 
     * @throws IOException 
     */  
    public static HttpResponse postSendReqForm( String httpUri,Object param ) throws Exception{  
        HttpPost httpPost = new HttpPost( httpUri );
		HttpResponse httpResponse = null ;
		try {
			//将请求对象由java对象置为json行字符对象。因为请求内设定了请求数据类型为json类型
			ObjectMapper objectMapper = new ObjectMapper();
			String josnParm = objectMapper.writeValueAsString(param);
			//String josnParm = JSONObject.toJSONString(param).toString();
			@SuppressWarnings("unchecked")
			Map<String,String> map = objectMapper.readValue( josnParm, Map.class ); //json转换成map
			//Map<String,String> map = (Map<String,String>) JSON.parseObject( josnParm , Map.class ) ;
	        //装填参数  ---不同的协议传输的数据不一样，很坑爹的，所以大家一定要定义好
	        List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
	        if(map!=null){  
	            for (Entry<String, String> entry : map.entrySet()) {  
	                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));  
	            }  
	        }  
	        //设置参数到请求对象中  
	        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));  
	  
	        System.out.println("请求地址："+httpUri );  
	        logger.info("请求参数："+nvps.toString());  
	          
	        //设置header信息  
	        //指定报文头【Content-type】、【User-Agent】  
	        httpPost.setHeader("User-Agent",userAgent);					//需要加这个参数，否则返回403 状态吗
	        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");  
	        httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
	        //httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
	        
	        httpResponse = createHttpClient().execute( httpPost );					//将post请求交给HttpClient 请求去执行
		} catch (IOException e) {
			logger.error( "商会信息修改后，调用失败 请关注....." ) ;
			return null ;
		}
	
		return httpResponse ;
    }  
	
	/**
	 * get 对象请求
	 * @param httpUri		请求地址
	 * @param param   	参数对象
	 * @return			请求结果
	 * @throws
	 */
	public static HttpResponse getObjectReq(String httpUri, Object param)throws Exception {
		StringBuffer httpUriStr = new StringBuffer() ;
		httpUriStr.append( httpUri ) ;
		if( param != null ){
			ObjectMapper objectMapper = new ObjectMapper();
			String josnParm = objectMapper.writeValueAsString(param);
			//String josnParm = JSONObject.toJSONString(param).toString();
			@SuppressWarnings("unchecked")
			Map<String,String> map = objectMapper.readValue( josnParm, Map.class ); //json转换成map
			//Map<String,String> map = (Map<String,String>) JSON.parseObject( josnParm , Map.class ) ;
			String paramStr = urlParamterStringer( "?" , map ) ;
			httpUriStr.append( paramStr ) ;
		}
		logger.info( httpUriStr.toString() ) ;
		HttpGet httpGet = new HttpGet( httpUriStr.toString() );
		String result = "";
		HttpResponse httpResponse = null ;
		try {
//			httpGet.setConfig(requestConfig); // 父类定义的超时信息，在每个请求都要置入配置参数
			
			httpGet.addHeader("Content-type", "application/text");		//设置请求头部
			httpGet.setHeader("Accept-Charset", "utf-8");
			httpGet.setHeader("User-Agent",userAgent);					//需要加这个参数，否则返回403 状态吗
			
			httpResponse = createHttpClient().execute( httpGet );				//将get请求交给HttpClient 请求去执行
		} catch (IOException e) {
			logger.error("HTTPGET 请求出现异常：" + e.getMessage() ) ;
			return null ;
		}

		return httpResponse ;
	}

	/****
	 * 解析http请求返回
	 * @param httpResponse
	 * @return
	 */
	public static Map parseHttpResponse( HttpResponse httpResponse ){
		logger.info( "请求处理的结果----》parseHttpResponse 格式化 ." );
		Map<String,Object> responsemap = new HashMap<>() ;
		try{
			ObjectMapper objectMapper = new ObjectMapper();
			String result = "" ;
			int statusCode = httpResponse.getStatusLine().getStatusCode() ;
			if ( statusCode == 200) { // 请求得到响应后，分析只有200的时候才去分析得到的数据，否认都任务失败
				result = EntityUtils.toString(httpResponse.getEntity(),
						Charset.forName("UTF-8")); // 将返回的数据解析成一个字符串
				logger.info( "请求处理的结果----》" + result );
				responsemap = objectMapper.readValue(  result , Map.class ); //json转换成map
			}else{
				logger.info( "请求处理的结果状态码----》" + statusCode );
			}
		}catch ( Exception e ){
			logger.error( "解析HTTPResponse出现异常：异常如下----" + e.getMessage() );
		}
		return responsemap ;
	}

	/**
	 * 将map中的数据格式化成服务端所需的表单String(www.baidu.com/login?userName=lambdroid&
	 * password=123456的“？”以及之后的数据)
	 * @param head
	 *            url头部字串，一般为“？”，在表单方式中分隔URL和请求参数map
	 * @param map
	 *            请求参数map
	 * @return 格式化完成后的表单数据
	 */
	public static <K, V> String urlParamterStringer(String head, Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			return "";
		}
		int capacity = map.size() * 30; // 设置表单长度30字节*N个请求参数
		// 参数不为空，在URL后面添加head（“？”）
		StringBuilder buffer = new StringBuilder(capacity);
		if (!map.isEmpty()) {
			buffer.append(head);
		}
		// 取出Map里面的请求参数，添加到表单String中。每个参数之间键值对之间用“=”连接，参数与参数之间用“&”连接
		Iterator<Entry<K, V>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<K, V> entry = it.next();
			Object key = entry.getKey();
			if( key.equals("class") ){
				continue ;
			}
			Object value = entry.getValue();
			if( value == null ){
				continue ;
			}
			buffer.append(key);
			buffer.append('=');
			buffer.append(value);
			if (it.hasNext()) {
				buffer.append("&");
			}
		}
		return buffer.toString();
	}

	/***
	 * 模拟与   图灵图书的机器人  聊天
	 * @param args
	 */
	public static void main(String[] args) {
		//请求对象数据
		Order order = new Order();
		order.setKey("c0ceaf30e9f255801810c6b810a4a139");
		order.setInfo("你好");
		//将请求的网络地址，和对象传入，就能得到响应
		try {
			String result = "" ;
			HttpResponse httpResponse = HttpReqUtil.postObjectReq("http://www.tuling123.com/openapi/api", order) ;
			/*if (httpResponse.getStatusLine().getStatusCode() == 200) { // 请求得到响应后，分析只有200的时候才去分析得到的数据，否认都任务失败
				result = EntityUtils.toString(httpResponse.getEntity(),
						Charset.forName("UTF-8")); // 将返回的数据解析成一个字符串
			}
			System.out.println( result );
			
			//不同的方式，同样的结果
			httpResponse = HttpReqUtil.postReq(	"http://www.tuling123.com/openapi/api", "key=c0ceaf30e9f255801810c6b810a4a139&info=hello" ) ;
			if (httpResponse.getStatusLine().getStatusCode() == 200) { // 请求得到响应后，分析只有200的时候才去分析得到的数据，否认都任务失败
				result = EntityUtils.toString(httpResponse.getEntity(),
						Charset.forName("UTF-8")); // 将返回的数据解析成一个字符串
			}
			System.out.println( result );*/
			

			//httpResponse = HttpReqUtil.postObjectReqForm("http://api.shangbangmall.com/shanghui/modify", notify ) ;
			httpResponse = HttpReqUtil.	postSendReqForm("http://api.shangbangmall.com/shanghui/modify" ,null ) ;
			System.out.println( httpResponse.getStatusLine().getStatusCode() );
			if (httpResponse.getStatusLine().getStatusCode() == 200) { // 请求得到响应后，分析只有200的时候才去分析得到的数据，否认都任务失败
				result = EntityUtils.toString(httpResponse.getEntity(),
						Charset.forName("UTF-8")); // 将返回的数据解析成一个字符串
				System.out.println( result );
			}
		} catch ( Exception e) {
			e.printStackTrace();
		}

	}
}

class Order {
	private String info;
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}


}