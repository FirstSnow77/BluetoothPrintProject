package com.suda.bluetoothprintproject.api.retrofitInterface;

import com.suda.bluetoothprintproject.api.model.AddressModel;
import com.suda.bluetoothprintproject.api.model.GetBCustomerProfile;
import com.suda.bluetoothprintproject.api.model.PostalNumber;
import com.suda.bluetoothprintproject.api.model.UserModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IAppHttpsApi
{
	@GET("api/tds") // 取得 7 碼郵號
	Call<PostalNumber> getHttpsAddressCode(@Query("CustID") String CustomerID, @Query("CustAddress") String CustomerAddress);
	
	@GET("API/Account") // 取得客代資料
	Call<GetBCustomerProfile> getBCustomerProfile(@Query("CustID") String custId, @Query("Token") String token);
	
	@GET("Address/AddressData") // 地址下拉選單所需的
	Call<AddressModel> getAddressList(@Query("level") int level, @Query("city") String city, @Query("district") String district);
	
	@GET("Employees/Employee_Verified") // 使用者帳密驗證
	Call<UserModel> getUserVerification(@Query("emp_id") String id, @Query("pwd") String pwd);
	
	@GET("Employees/AddUserInfo") // 傳送使用者資訊
	Call<String> postUserInfo(@Query("account")String account, @Query("deviceVersion") String osVersion,
			@Query("userDevice")String userDevice, @Query("userBehavior") String userBehavior);
	
	@GET("api/tds/GetSuda5AndAreaCode") // 取得理貨區碼
	Call<PostalNumber> getAreaCodeResult(@Query("custId") String custId, @Query("address") String address);
}
