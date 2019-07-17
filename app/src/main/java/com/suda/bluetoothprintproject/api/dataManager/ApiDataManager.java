package com.suda.bluetoothprintproject.api.dataManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;

import com.suda.bluetoothprintproject.api.model.AddressModel;
import com.suda.bluetoothprintproject.api.model.GetBCustomerProfile;
import com.suda.bluetoothprintproject.api.model.GetBCustomerToken;
import com.suda.bluetoothprintproject.api.model.OBTModel;
import com.suda.bluetoothprintproject.api.model.PostalNumber;
import com.suda.bluetoothprintproject.api.model.UserModel;
import com.suda.bluetoothprintproject.api.retrofitManager.RetrofitManager;
import com.suda.bluetoothprintproject.utils.DialogUtil;
import com.suda.bluetoothprintproject.utils.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * api 管理者, 只給 Business 層使用.
 */
public class ApiDataManager  // implements INetworkChangeReceiver
{
	//	private boolean mInternetIsNotConnected = true; // 是否不能連線? 這是接收於 NetworkChangeReceiver
	
	private Context mContext;
	private final String TAG = ApiDataManager.class.getSimpleName();
	
	public ApiDataManager(@NotNull Context mContext) {
		this.mContext = mContext;
	}
	
	/**
	 * 取得 7 碼郵號
	 *
	 * @param customerId      客代
	 * @param customerAddress 客戶地址
	 * @param onDataCallback  httpCallback
	 */
	public void getAddressResult(String customerId, String customerAddress, onDataCallback<PostalNumber> onDataCallback) {
		if(ifCanConnected()) {
			Call<PostalNumber> call = RetrofitManager.getAppHttpsGsonApi().getHttpsAddressCode(customerId, customerAddress);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	
	/**
	 * 取得客代資料
	 *
	 * @param custId         客代
	 * @param hashText       客代對應的 token (須加密)
	 * @param onDataCallback httpCallback
	 */
	public void getBCustomerProfile(String custId, String hashText, onDataCallback<GetBCustomerProfile> onDataCallback) {
		if(ifCanConnected()) {
			Call<GetBCustomerProfile> call = RetrofitManager.getAppHttpsGsonApi().getBCustomerProfile(custId, hashText);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	
	/**
	 * 取得下拉式所需的地址清單
	 *
	 * @param level          對應層級
	 * @param city           城市名稱
	 * @param district       街路名稱
	 * @param onDataCallback httpCallback
	 */
	public void getAddressList(int level, String city, String district, onDataCallback<AddressModel> onDataCallback) {
		if(ifCanConnected()) {
			Call<AddressModel> call = RetrofitManager.getAppHttpsGsonApi().getAddressList(level, city, district);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	
	/**
	 * 取得使用者驗證的資料
	 *
	 * @param id             帳號
	 * @param pwd            密碼
	 * @param onDataCallback httpCallback
	 */
	public void getUserVerification(String id, String pwd, onDataCallback<UserModel> onDataCallback) {
		if(ifCanConnected()) {
			Call<UserModel> call = RetrofitManager.getAppHttpsGsonApi().getUserVerification(id, pwd);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	/**
	 * 推送使用者資訊. 目前這隻只推送不管成功失敗.
	 *
	 * @param account      當前帳號
	 * @param osVersion    os 版本
	 * @param userDevice   Mobile OS
	 * @param userBehavior 使用者行為
	 */
	public void postUserInfo(String account, String osVersion, String userDevice, String userBehavior) {
		if(ifCanConnected()) {
			Call<String> call = RetrofitManager.getAppHttpsStringApi().postUserInfo(account, osVersion, userDevice, userBehavior);
			call.enqueue(new Callback<String>()
			{
				@Override public void onResponse(Call<String> call, Response<String> response) {}
				
				@Override public void onFailure(Call<String> call, Throwable t) { }
			});
		}
	}
	
	/**
	 * 取得指定客代對應的 token
	 *
	 * @param customerId     客代
	 * @param onDataCallback httpCallback
	 */
	public void getBCustomerToken(String customerId, onDataCallback<GetBCustomerToken> onDataCallback) {
		if(ifCanConnected()) {
			Call<GetBCustomerToken> call = RetrofitManager.getAppHttpGsonApi().getBCustomerToken(customerId);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	/**
	 * 取得 qrCode 的 bitmap. 為了讓取得可依序所以使用 synchronized
	 *
	 * @param senderCode       客代
	 * @param trackingNo       託運單號
	 * @param collectPrice     代收金額
	 * @param temperature      溫層代碼
	 * @param customerPostcode 收件人地址對應郵號(noDash)
	 * @param deliverDate      希望送達日期
	 * @param deliverTime      希望送達時間
	 * @param insurance        包裹實質金額
	 * @param printInvoice     是否需要列印?
	 * @param printDonation    是否捐贈?
	 * @param invoiceUni       統編
	 * @param common           載具
	 * @param onFileCallback   httpCallback
	 */
	public synchronized void getQRCodeBitmap(String senderCode, String trackingNo, String collectPrice, String temperature,
			String customerPostcode, String deliverDate, String deliverTime, String insurance, String printInvoice,
			String printDonation, String invoiceUni, String common, final onFileCallback<Bitmap> onFileCallback) {
		if(ifCanConnected()) {
			Call<ResponseBody> call = RetrofitManager.getAppHttpFileApi().getQRcode(senderCode, trackingNo, collectPrice,
					temperature, customerPostcode, deliverDate, deliverTime, insurance, printInvoice, printDonation,
					invoiceUni, common);
			call.enqueue(new Callback<ResponseBody>()
			{
				@Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					if(response.isSuccessful()) {
						if(response.body() != null) {
							byte[] bytes = new byte[ 0 ];
							try {
								bytes = response.body().bytes();
							}
							catch(IOException e) {
								e.printStackTrace();
								ToastUtil.showToast(ApiDataManager.this.mContext, "QR CODE 轉譯失敗");
								onFileCallback.onGetFileFailure();
							}
							Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
							onFileCallback.onGetFileSuccess(bitmap);
						}
					}
					else {
						ToastUtil.showApiResponseErrorToast(ApiDataManager.this.mContext, response);
						onFileCallback.onGetFileFailure();
					}
					
				}
				
				@Override public void onFailure(Call<ResponseBody> call, Throwable t) {
					DialogUtil.showNetworkErrorAlert(ApiDataManager.this.mContext);
					onFileCallback.onGetFileFailure();
				}
			});
		}
		else onFileCallback.onGetFileFailure();
	}
	
	/**
	 * 取得理貨區碼(包含五碼郵號, 不含 dash)
	 *
	 * @param customerId     客代
	 * @param address        地址
	 * @param onDataCallback httpCallback
	 */
	public void getAreaCodeResult(String customerId, String address, onDataCallback<PostalNumber> onDataCallback) {
		if(ifCanConnected()) {
			Call<PostalNumber> call = RetrofitManager.getAppHttpsGsonApi().getAreaCodeResult(customerId, address);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	/**
	 * 取得託運單號
	 *
	 * @param cmd            執行指令
	 * @param customerId     客代
	 * @param waybillType    一般單 || 到付單
	 * @param count          取得總量
	 * @param onDataCallback httpCallback
	 */
	public void getWaybillData(String cmd, String customerId, String waybillType, int count, onDataCallback<String> onDataCallback) {
		if(ifCanConnected()) {
			Call<String> call = RetrofitManager.getEgsStringApi().getWaybillCode(cmd, customerId, waybillType, count);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	/**
	 * 取得 base 碼
	 *
	 * @param cmd            執行指令
	 * @param address        地址
	 * @param onDataCallback httpCallback
	 */
	public void getBaseCode(String cmd, String address, onDataCallback<String> onDataCallback) {
		if(ifCanConnected()) {
			Call<String> call = RetrofitManager.getEgsStringApi().getBaseCode(cmd, address);
			call.enqueue(this.getGenericCallback(onDataCallback));
		}
		else onDataCallback.onGetDataFailure();
	}
	
	/**
	 * 傳送 OBT 資料. 目前這隻只推送不管成功失敗.
	 *
	 * @param obtModel obt Data
	 */
	public void postOBTData(OBTModel obtModel) {
		if(ifCanConnected()) {
			RequestBody body = new FormBody.Builder().add("cmd", obtModel.cmd).add("customer_id", obtModel.customer_id)
					                   .add("tracking_number", obtModel.tracking_number).add("order_no", obtModel.order_no)
					                   .add("receiver_name", obtModel.receiver_name).add("receiver_address", obtModel.receiver_address)
					                   .add("receiver_suda5", obtModel.receiver_suda5).add("receiver_mobile", obtModel.receiver_mobile)
					                   .add("receiver_phone", obtModel.receiver_phone)
					                   .add("sender_name", obtModel.sender_name).add("sender_address", obtModel.sender_address)
					                   .add("sender_suda5", obtModel.sender_suda5).add("sender_phone", obtModel.sender_phone)
					                   .add("product_price", obtModel.product_price).add("product_name", obtModel.product_name)
					                   .add("comment", obtModel.comment).add("package_size", obtModel.package_size)
					                   .add("temperature", obtModel.temperature).add("distance", obtModel.distance)
					                   .add("delivery_date", obtModel.delivery_date).add("delivery_timezone", obtModel.delivery_timezone)
					                   .add("create_time", obtModel.create_time).add("print_time", obtModel.print_time)
					                   .add("account_id", obtModel.account_id).add("member_no", obtModel.member_no)
					                   .add("taxin", obtModel.taxin).add("insurance", obtModel.insurance)
					                   .add("upload_type", obtModel.upload_type).build();
			Call<Void> call = RetrofitManager.getEgsSimpleApi().postOBTData(body);
			call.enqueue(new Callback<Void>() {
				@Override public void onResponse(Call<Void> call, Response<Void> response) {
//					Log.d(TAG, "postOBTData Success: ");
				}
				
				@Override public void onFailure(Call<Void> call, Throwable t) {
//					Log.d(TAG, "postOBTData Failure: ");
				}
			});
		}
	}
	
	/**
	 * 抽出 retrofit2 的 http 請求 callback.
	 * Json 轉換使用
	 *
	 * @param onDataCallback callback
	 * @param <model>        指定類別需繼承 Serializable
	 * @return 對應型別的 callback
	 */
	private <model extends Serializable> Callback<model> getGenericCallback(final onDataCallback<model> onDataCallback) {
		return new Callback<model>()
		{
			@Override public void onResponse(Call<model> call, Response<model> response) {
				if(response.isSuccessful())
					onDataCallback.onGetDataSuccess(response.body());
				else {
					ToastUtil.showApiResponseErrorToast(ApiDataManager.this.mContext, response);
					onDataCallback.onGetDataFailure();
				}
			}
			
			@Override public void onFailure(Call<model> call, Throwable t) {
				DialogUtil.showNetworkErrorAlert(ApiDataManager.this.mContext);
				onDataCallback.onGetDataFailure();
			}
		};
	}
	
	//	/**
	//	 * 抽出 retrofit2 的 http 請求 callback.
	//	 * file 轉換使用
	//	 *
	//	 * @param onFileCallback callback
	//	 * @param <model>        指定類別需繼承 Parcelable
	//	 * @return 對應型別的 callback
	//	 */
	//	private <model extends Parcelable> Callback<model> getGenericCallback(final onFileCallback<model> onFileCallback) {
	//		return new Callback<model>()
	//		{
	//			@Override public void onResponse(Call<model> call, Response<model> response) {
	//				if(response.isSuccessful())
	//					onFileCallback.onGetFileSuccess(response.body());
	//				else {
	//					ToastUtil.showApiResponseErrorToast(ApiDataManager.this.mContext, response);
	//					onFileCallback.onGetFileFailure();
	//				}
	//			}
	//
	//			@Override public void onFailure(Call<model> call, Throwable t) {
	//				DialogUtil.showNetworkErrorAlert(ApiDataManager.this.mContext);
	//				onFileCallback.onGetFileFailure();
	//			}
	//		};
	//	}
	
	/**
	 * 如果不能連線則跳出一個 alert 提醒使用者
	 *
	 * @return 可否連線?
	 */
	private boolean ifCanConnected() {
		ConnectivityManager connManager = (ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		boolean internetIsNotConnected = true;
		if(info != null && info.isConnected())
			internetIsNotConnected = false;
		
		if(internetIsNotConnected)
			DialogUtil.showNetworkErrorAlert(this.mContext);
		
		return !internetIsNotConnected;
	}
	
	//	// 可連線
	//	@Override public void isConnected() {
	//		this.mInternetIsNotConnected = false;
	//	}
	//
	//	// 不可連線
	//	@Override public void isNotConnected() {
	//		this.mInternetIsNotConnected = true;
	//	}
	
	public interface onDataCallback<T extends Serializable>
	{
		void onGetDataSuccess(T data);
		
		void onGetDataFailure();
	}
	
	public interface onFileCallback<T extends Parcelable>
	{
		void onGetFileSuccess(T data);
		
		void onGetFileFailure();
	}
}
