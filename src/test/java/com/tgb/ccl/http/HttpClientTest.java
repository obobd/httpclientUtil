package com.tgb.ccl.http;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import com.tgb.ccl.http.common.HttpHeader;
import com.tgb.ccl.http.common.HttpHeader.Headers;
import com.tgb.ccl.http.exception.HttpProcessException;
import com.tgb.ccl.http.httpclient.HttpClientUtil;
import com.tgb.ccl.http.httpclient.HttpClientUtil.HttpMethods;
import com.tgb.ccl.http.httpclient.builder.HCB;

/** 
 * 
 * @author arron
 * @date 2015年11月1日 下午2:23:18 
 * @version 1.0 
 */
public class HttpClientTest {
	
	public static void testOne() throws HttpProcessException{
		
		System.out.println("--------简单方式调用（默认post）--------");
		String url = "http://tool.oschina.net/";
		//简单调用
		String resp = HttpClientUtil.send(url);
		System.out.println("请求结果内容长度："+ resp.length());
		
		System.out.println("\n#################################\n");
		
		System.out.println("--------加入header设置--------");
		url="http://blog.csdn.net/xiaoxian8023";
		//设置header信息
		Header[] headers=HttpHeader.custom().userAgent("Mozilla/5.0").build();
		//执行请求
		resp = HttpClientUtil.send(url, headers);
		System.out.println("请求结果内容长度："+ resp.length());

		System.out.println("\n#################################\n");
		
		System.out.println("--------代理设置（绕过证书验证）-------");
		url="https://www.facebook.com/";
		HttpClient client= HCB.custom().timeout(10000).proxy("127.0.0.1", 8087).ssl().build();//采用默认方式（绕过证书验证）
		//执行请求
		resp = HttpClientUtil.send(client,url);
		System.out.println("请求结果内容长度："+ resp.length());

		System.out.println("\n#################################\n");

		System.out.println("--------代理设置（自签名证书验证）+header+get方式-------");
		url = "https://sso.tgb.com:8443/cas/login";
		client= HCB.custom().timeout(10000).ssl("D:\\keys\\wsriakey","tomcat").build();
		headers=HttpHeader.custom().keepAlive("false").connection("close").contentType(Headers.APP_FORM_URLENCODED).build();
		//执行请求
		resp = HttpClientUtil.send(client, url, HttpMethods.GET, headers);
		System.out.println("请求结果内容长度："+ resp.length());

		System.out.println("\n#################################\n");
	}
	
	
	
	public static void testMutilTask(){
		// URL列表数组
		String[] urls = {
				"http://blog.csdn.net/xiaoxian8023/article/details/49883113",
				"http://blog.csdn.net/xiaoxian8023/article/details/49909359",
				"http://blog.csdn.net/xiaoxian8023/article/details/49910127",
				"http://blog.csdn.net/xiaoxian8023/article/details/49910885",
				
//				"http://blog.csdn.net/xiaoxian8023/article/details/49862725",
//				"http://blog.csdn.net/xiaoxian8023/article/details/49834643",
//				"http://blog.csdn.net/xiaoxian8023/article/details/49834615",
//				"http://blog.csdn.net/xiaoxian8023/article/details/49834589",
//				"http://blog.csdn.net/xiaoxian8023/article/details/49785417",
//				
//				"http://blog.csdn.net/xiaoxian8023/article/details/48679609",
//				"http://blog.csdn.net/xiaoxian8023/article/details/48681987",
//				"http://blog.csdn.net/xiaoxian8023/article/details/48710653",
//				"http://blog.csdn.net/xiaoxian8023/article/details/48729479",
//				"http://blog.csdn.net/xiaoxian8023/article/details/48733249",
//
//				"http://blog.csdn.net/xiaoxian8023/article/details/48806871",
//				"http://blog.csdn.net/xiaoxian8023/article/details/48826857",
//				"http://blog.csdn.net/xiaoxian8023/article/details/49663643",
//				"http://blog.csdn.net/xiaoxian8023/article/details/49619777",
//				"http://blog.csdn.net/xiaoxian8023/article/details/47335659",
//
//				"http://blog.csdn.net/xiaoxian8023/article/details/47301245",
//				"http://blog.csdn.net/xiaoxian8023/article/details/47057573",
//				"http://blog.csdn.net/xiaoxian8023/article/details/45601347",
//				"http://blog.csdn.net/xiaoxian8023/article/details/45569441",
//				"http://blog.csdn.net/xiaoxian8023/article/details/43312929", 
				};
		
		// 设置header信息
		Header[] headers = HttpHeader.custom().userAgent("Mozilla/5.0").from("http://blog.csdn.net/newest.html").build();
		HttpClient client= HCB.custom().timeout(10000).build();
		
		 long start = System.currentTimeMillis();        
	        try {
	            int pagecount = urls.length;
	            ExecutorService executors = Executors.newFixedThreadPool(pagecount);
	            CountDownLatch countDownLatch = new CountDownLatch(pagecount*50);         
	            for(int i = 0; i< pagecount*50;i++){
	                //启动线程抓取
	                executors.execute(new GetRunnable(urls[i%pagecount], headers, countDownLatch).setClient(client));
	            }
	            countDownLatch.await();
	            executors.shutdown();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        } finally {
	            System.out.println("线程" + Thread.currentThread().getName() + ", 所有线程已完成，开始进入下一步！");
	        }
	         
	        long end = System.currentTimeMillis();
	        System.out.println("总耗时（毫秒）： -> " + (end - start));
	        //(7715+7705+7616)/3= 23 036/3= 7 678.66---150=51.2
	        //(9564+8250+8038+7604+8401)/5=41 857/5=8 371.4--150
	        //(9803+8244+8188+8378+8188)/5=42 801/5= 8 560.2---150
	}
	
	 static class GetRunnable implements Runnable {
	        private CountDownLatch countDownLatch;
	        private String url;
	        private Header[] headers;
	        private HttpClient client = null;
	        
	        public GetRunnable setClient(HttpClient client){
	        	this.client = client;
	        	return this;
	        }

	        public GetRunnable(String url, Header[] headers,CountDownLatch countDownLatch){
	        	this.url = url;
	        	this.headers = headers;
	            this.countDownLatch = countDownLatch;
	        }
	        @Override
	        public void run() {
	            try {
	            	String response = null;
	            	if(client!=null){
	            		response = HttpClientUtil.send(client, url, headers);
	            	}else{
	            		response =  HttpClientUtil.send(url, headers);
	            	}
	            	System.out.println(Thread.currentThread().getName()+"--获取内容长度："+response.length());
	            } catch (HttpProcessException e) {
					e.printStackTrace();
				} finally {
	                countDownLatch.countDown();
	            }
	        }
	    }  
	
	public static void main(String[] args) throws Exception {
//		testOne();
		testMutilTask();
	}
}