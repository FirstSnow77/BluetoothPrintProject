package com.suda.bluetoothprintproject.api.retrofitInterface;

import com.suda.bluetoothprintproject.api.model.GetBCustomerToken;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IAppHttpApi
{
	@GET("Customers/GetCustomerPwd") // 取得查詢客代所需的 token
	Call<GetBCustomerToken> getBCustomerToken(@Query("login") String login);
	
	@GET("QR_Code") // 取得 qrCode
	Call<ResponseBody> getQRcode(@Query("sender_code") String sender_code, @Query("tracking_no") String tracking_no, @Query("collect_price") String collect_price,
			@Query("temperature") String temperature, @Query("customer_postcode") String customer_postcode, @Query("deliver_date") String deliver_date,
			@Query("deliver_time") String deliver_time, @Query("insurance") String insurance, @Query("print_invoice") String print_invoice,
			@Query("print_donation") String print_donation, @Query("invoice_uni") String invoice_uni, @Query("common") String common);
}
