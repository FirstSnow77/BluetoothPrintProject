package com.suda.bluetoothprintproject.api.retrofitInterface;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

// 向 egs 訪問的 api
public interface IEgsApi
{
	@GET("/egs") // 取得託運單號碼
	Call<String> getWaybillCode(@Query("cmd") String cmd, @Query("customer_id") String customer_id,
			@Query("waybill_type") String waybillType, @Query("count") int count);
	
	@GET("/egs") // 取得 base 碼
	Call<String> getBaseCode(@Query("cmd") String cmd, @Query("suda5_1") String address);
	
	@POST("/egs") // 傳送 OBT 資料
	Call<Void> postOBTData(@Body RequestBody params);
}
