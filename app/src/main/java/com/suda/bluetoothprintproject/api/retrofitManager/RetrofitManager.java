package com.suda.bluetoothprintproject.api.retrofitManager;

import com.google.gson.GsonBuilder;
import com.suda.bluetoothprintproject.api.retrofitInterface.IAppHttpApi;
import com.suda.bluetoothprintproject.api.retrofitInterface.IAppHttpsApi;
import com.suda.bluetoothprintproject.api.retrofitInterface.IEgsApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


// 集中管理每個 api 透過 retrofit 所實例出的介面.(單例模式)
public class RetrofitManager
{
	//	private static RetrofitManager mInstance = new RetrofitManager();
	
	private static IAppHttpsApi iAppHttpsGsonApi = null; // https: appService
	private static IAppHttpsApi iAppHttpsStringApi = null; // https: appService
	
	private static IAppHttpApi iAppHttpGsonApi = null; // http: appService
	private static IAppHttpApi iAppHttpFileApi = null; // http: appService
	
	private static IEgsApi iEgsStringApi = null; // egs
	private static IEgsApi iEgsSimpleApi = null; // egs
//	private static ITestAppHttpApi iTestAppHttpGsonApi = null; // ezcattest
	
	// Json 轉接器, 可讀取 json, 將 json 轉成物件.
	private final static GsonConverterFactory mGsonConverterFactory = GsonConverterFactory.create(new GsonBuilder().setLenient().create());
	// 轉換 text/html 的工廠
	private final static ScalarsConverterFactory mScalarsConverterFactory = ScalarsConverterFactory.create();
	
	// apiUrl
	private final static String mHttpsAppServiceUrl = "https://appservice.ezcat.com.tw/";
	private final static String mHttpAppServiceUrl = "http://tw2.ezcat.com.tw/";
	private final static String mEgsUrl = "http://tw3.ezcat.com.tw:8800/";
//	private final static String mEzcattestUrl = "http://ezcattest.cloudapp.net:8080/";
	
	//	private RetrofitManager() {
	//		// Json 轉接器, 可讀取 json, 將 json 轉成物件.
	//		final GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(new GsonBuilder().setLenient().create());
	//		// 轉換 text/html 的工廠
	//		final ScalarsConverterFactory scalarsConverterFactory = ScalarsConverterFactory.create();
	//
	////		final String httpsAppServuceUrl = "https://appservice.ezcat.com.tw/";
	////		final String httpAppServuceUrl = "http://tw2.ezcat.com.tw/";
	////		final String egsUrl = "http://tw3.ezcat.com.tw:8800/";
	////		final String ezcattestUrl = "http://ezcattest.cloudapp.net:8080/";
	//
	//		Retrofit.Builder retrofit2 = new Retrofit.Builder();
	//		Retrofit retrofit = new Retrofit.Builder().baseUrl(httpsAppServuceUrl).addConverterFactory(gsonConverterFactory).build();
	//
	//		// https: appService
	////		this.iAppHttpsGsonApi = retrofit.create(IAppHttpsApi.class);
	//		retrofit = new Retrofit.Builder().baseUrl(httpsAppServuceUrl).addConverterFactory(scalarsConverterFactory).build();
	////		this.iAppHttpsStringApi = retrofit.create(IAppHttpsApi.class);
	//
	//		// http: appService
	//		retrofit = new Retrofit.Builder().baseUrl(httpAppServuceUrl).addConverterFactory(gsonConverterFactory).build();
	////		this.iAppHttpGsonApi = retrofit.create(IAppHttpApi.class);
	//		retrofit = new Retrofit.Builder().baseUrl(httpAppServuceUrl).build();
	//		this.iAppHttpFileApi = retrofit.create(IAppHttpApi.class);
	//
	//		// egs
	//		retrofit = new Retrofit.Builder().baseUrl(egsUrl).addConverterFactory(scalarsConverterFactory).build();
	////		this.iEgsStringApi = retrofit.create(IEgsApi.class);
	//
	//		// ezcattest
	//		retrofit = new Retrofit.Builder().baseUrl(ezcattestUrl).addConverterFactory(gsonConverterFactory).build();
	////		this.iTestAppHttpGsonApi = retrofit.create(ITestAppHttpApi.class);
	//	}
	
	/**
	 * @return https: appService for Gson
	 */
	public static IAppHttpsApi getAppHttpsGsonApi() {
		if(iAppHttpsGsonApi == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(mHttpsAppServiceUrl).addConverterFactory(mGsonConverterFactory).build();
			iAppHttpsGsonApi = retrofit.create(IAppHttpsApi.class);
		}
		return iAppHttpsGsonApi;
	}
	
	/**
	 * @return https: appService for String
	 */
	public static IAppHttpsApi getAppHttpsStringApi() {
		if(iAppHttpsStringApi == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(mHttpsAppServiceUrl).addConverterFactory(mScalarsConverterFactory).build();
			iAppHttpsStringApi = retrofit.create(IAppHttpsApi.class);
		}
		return iAppHttpsStringApi;
	}
	
	/**
	 * @return http: appService for Gson
	 */
	public static IAppHttpApi getAppHttpGsonApi() {
		if(iAppHttpGsonApi == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(mHttpAppServiceUrl).addConverterFactory(mGsonConverterFactory).build();
			iAppHttpGsonApi = retrofit.create(IAppHttpApi.class);
		}
		return iAppHttpGsonApi;
	}
	
	/**
	 * @return http: appService for file
	 */
	public static IAppHttpApi getAppHttpFileApi() {
		if(iAppHttpFileApi == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(mHttpAppServiceUrl).build();
			iAppHttpFileApi = retrofit.create(IAppHttpApi.class);
		}
		return iAppHttpFileApi;
	}
	
	/**
	 * @return egs for string
	 */
	public static IEgsApi getEgsStringApi() {
		if(iEgsStringApi == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(mEgsUrl).addConverterFactory(mScalarsConverterFactory).build();
			iEgsStringApi = retrofit.create(IEgsApi.class);
		}
		return iEgsStringApi;
	}
	
	/**
	 * @return egs for void
	 */
	public static IEgsApi getEgsSimpleApi() {
		if(iEgsSimpleApi == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(mEgsUrl).build();
			iEgsSimpleApi = retrofit.create(IEgsApi.class);
		}
		return iEgsSimpleApi;
	}
	
//	/**
//	/* 在 6/27 號已經沒有使用測試台的 api
//	 * @return ezcattest for Gson
//	 */
//	public static ITestAppHttpApi getTestAppHttpGsonApi() {
//		if(iTestAppHttpGsonApi == null) {
//			Retrofit retrofit = new Retrofit.Builder().baseUrl(mEzcattestUrl).addConverterFactory(mGsonConverterFactory).build();
//			iTestAppHttpGsonApi = retrofit.create(ITestAppHttpApi.class);
//		}
//		return iTestAppHttpGsonApi;
//	}
}
