package com.suda.bluetoothprintproject.businessManagers.inputA4Note;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.suda.bluetoothprintproject.api.dataManager.ApiDataManager;
import com.suda.bluetoothprintproject.api.model.AddressItem;
import com.suda.bluetoothprintproject.api.model.AddressModel;
import com.suda.bluetoothprintproject.api.model.GetBCustomerProfile;
import com.suda.bluetoothprintproject.api.model.GetBCustomerToken;
import com.suda.bluetoothprintproject.api.model.OBTModel;
import com.suda.bluetoothprintproject.api.model.PostalNumber;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.BaseManager;
import com.suda.bluetoothprintproject.businessManagers.bluetoothPrint.BluetoothPrintManager;
import com.suda.bluetoothprintproject.businessManagers.bluetoothPrint.IBlePrintApiManager;
import com.suda.bluetoothprintproject.businessManagers.businessService.IPDFService;
import com.suda.bluetoothprintproject.businessManagers.businessService.printerControl.PDFService;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.BCustomerProfile;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4AllData;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InputA4NoteManager extends BaseManager implements IAddressListApiManager, IInputA4NoteApiManager, IPdfPrintManager
{
	private static String TAG = InputA4NoteManager.class.getSimpleName();
	private final IBlePrintApiManager mBlePrintApiManager;
	private final IPDFService<InputA4AllData> mPdfService;
	
	public InputA4NoteManager(Context context) {
		super(context);
		this.mBlePrintApiManager = new BluetoothPrintManager(context);
		this.mPdfService = new PDFService(context);
	}
	
	/**
	 * 產生對應的地址下拉式選單資料
	 *
	 * @param level    欲查詢層級
	 * @param city     城市
	 * @param district 區域
	 * @param callback callback
	 */
	@Override
	public void getAddressList(int level, String city, String district, final AllBusinessCallback.onDAOCallback<ArrayList<String>> callback) {
		super.mApiDataManager.getAddressList(level, city, district, new ApiDataManager.onDataCallback<AddressModel>()
		{
			@Override public void onGetDataSuccess(AddressModel data) {
				ArrayList<String> result = new ArrayList<>();
				result.add("");
				if(data != null && data.Data.size() > 0) {
					for(AddressItem item : data.Data)
						result.add(item.Name);
				}
				
				callback.onSuccess(result);
			}
			
			@Override public void onGetDataFailure() {
				callback.onFailure();
			}
		});
	}
	
	/**
	 * 依客戶代號取得客代相關資料
	 *
	 * @param bCustomerId 契客代號
	 * @param callback    callback
	 */
	@Override
	public void getBCustomerProfile(String bCustomerId, final AllBusinessCallback.onDAOCallback<BCustomerProfile> callback) {
		StringBuilder builder = new StringBuilder(bCustomerId);
		int checkLength = 12 - bCustomerId.length(); // 客代需要 "補 0" 到 12 碼, 才能查詢相關資訊.
		if(checkLength > 0) {
			for(int i = 0; i < checkLength; i++)
				builder.append("0");
		}
		final String newCustomerId = builder.toString();
		// 查詢 token & 運算後取得 hash
		super.mApiDataManager.getBCustomerToken(newCustomerId, new ApiDataManager.onDataCallback<GetBCustomerToken>()
		{
			@Override public void onGetDataSuccess(GetBCustomerToken data) {
				if(data != null && !data.STATUS.isEmpty() && data.STATUS.contentEquals("OK")) {
					try {
						MessageDigest md = MessageDigest.getInstance("MD5");
						md.reset();
						md.update((newCustomerId + data.W100_PASSWORD).getBytes(Charset.forName("UTF8")));
						final byte[] resultByte = md.digest();
						BigInteger bigInt = new BigInteger(1, resultByte);
						String hashText = bigInt.toString(16);
						while(hashText.length() < 32)
							hashText = String.format("%s%s", "0", hashText);
						
						// 取得客代資料
						InputA4NoteManager.super.mApiDataManager.getBCustomerProfile(newCustomerId, hashText, new ApiDataManager.onDataCallback<GetBCustomerProfile>()
						{
							@Override public void onGetDataSuccess(GetBCustomerProfile data) {
								if(data != null && data.Code.contentEquals("0")) {
									BCustomerProfile profile = new BCustomerProfile();
									profile.newBCustomerId = newCustomerId;
									profile.customerFullName = InputA4NoteManager.this.mGetLinkedTreeMapData(data.Data, "W100_CUSTOMER_FULLNAME");
									profile.address = InputA4NoteManager.this.mGetLinkedTreeMapData(data.Data, "W100_ADDRESS");
									profile.telephone = InputA4NoteManager.this.mGetLinkedTreeMapData(data.Data, "W100_TELEPHONE");
									profile.W100_IS_CLC = InputA4NoteManager.this.mGetLinkedTreeMapData(data.Data, "W100_IS_CLC");
									callback.onSuccess(profile);
								}
							}
							
							@Override public void onGetDataFailure() {
								callback.onFailure();
							}
						});
					}
					catch(NoSuchAlgorithmException ex) { //  MessageDigest.getInstance("MD5")
						Log.e(TAG, "取客代資料發生錯誤", ex);
						callback.onFailure();
					}
				}
				else callback.onFailure();
			}
			
			@Override public void onGetDataFailure() {
				callback.onFailure();
			}
		});
	}
	
	/**
	 * 目前 step1 取得所需的資料 (未來會將此方法拆分得更細, 以滿足未來 step 異動時的需求)
	 *
	 * @param inputA4AllData 以驗證過的資料 (並且直接使用 reference 去修改物件中的資料)
	 * @param callback       callback
	 */
	@Override
	public void setStep1NextData(final InputA4AllData inputA4AllData, final AllBusinessCallback.onCallback callback) {
		// 先取得郵號 (收件人)
		this.mBlePrintApiManager.get7AddressResult("000000000000", inputA4AllData.receivePeople.address,
				new AllBusinessCallback.onDAOCallback<PostalNumber>()
				{
					@Override public void onSuccess(PostalNumber daoData1) {
						inputA4AllData.receivePeople.poNumber = daoData1.Data;
						inputA4AllData.receivePeople.poNotDashNumber = daoData1.GetNoAddressDashData();
						inputA4AllData.receivePeople.baseNumber = daoData1.Data.isEmpty() ? "" : daoData1.Data.substring(0, 2); // base
						this.onFailure();
					}
					
					@Override public void onFailure() {
						// egs OBT 取得郵號 (寄件人)
						InputA4NoteManager.this.mBlePrintApiManager.get7AddressResult("000000000000", inputA4AllData.sendPeople.address,
								new AllBusinessCallback.onDAOCallback<PostalNumber>()
								{
									@Override public void onSuccess(PostalNumber daoData2) {
										inputA4AllData.sendPeople.poNumber = daoData2.Data;
										inputA4AllData.sendPeople.poNotDashNumber = daoData2.GetNoAddressDashData();
										this.onFailure();
									}
									
									@Override public void onFailure() {
										// 取得理貨區碼
										InputA4NoteManager.this.mBlePrintApiManager.getAreaCode("000000000000"
												, inputA4AllData.receivePeople.address, new AllBusinessCallback.onDAOCallback<PostalNumber>()
												{
													@Override public void onSuccess(PostalNumber daoData3) {
														inputA4AllData.receivePeople.areaCode = daoData3.GetAreaCode();
														this.onFailure();
													}
													
													@Override public void onFailure() {
														callback.onSuccess();
													}
												});
									}
								});
					}
				});
	}
	
	/**
	 * 取得託運單號
	 *
	 * @param customerId  客戶代號 (目前都用 all 0 )
	 * @param waybillType 託運單類型 A | B (一般 | 代收)
	 * @param count       託運單號數量
	 * @param callback    callback
	 */
	@Override
	public void getWaybillNumber(String customerId, String waybillType, int count
			, final AllBusinessCallback.onDAOCallback<ArrayList<String>> callback) {
		super.mApiDataManager.getWaybillData("query_waybill_id_range", "0000000000", waybillType, count
				, new ApiDataManager.onDataCallback<String>()
				{
					@Override public void onGetDataSuccess(String data) {
						String[] str = data.split("&");
						String status = (str[ 0 ].split("="))[ 1 ].trim();
						final String getWaybill = (str[ 2 ].split("=", 2))[ 1 ].trim();
						if(status.equals("OK") && !getWaybill.isEmpty()) {
							final String[] dataList = getWaybill.split(",");
							final ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(dataList));
							callback.onSuccess(arrayList);
						}
					}
					
					@Override public void onGetDataFailure() {
						callback.onFailure();
					}
				});
	}
	
	/**
	 * 依據相關資料組出對應的 QR CODE 集合
	 *
	 * @param inputA4AllData 以驗證過的資料(包含託運單號) (並且直接使用 reference 去修改物件中的資料)
	 * @param printInvoice   是否需要列印?
	 * @param printDonation  是否捐贈?
	 * @param invoiceUni     統編
	 * @param common         載具
	 * @param callback       callback
	 */
	@Override
	public synchronized void getQRCodeBitmap(final InputA4AllData inputA4AllData, final String printInvoice, final String printDonation,
			final String invoiceUni, final String common, final AllBusinessCallback.onDAOCallback<HashMap<String, Bitmap>> callback) {
		
		super.runUiThreadSetNewLooper(new runUiThreadAndDoSomething()
		{
			@Override public void doSomething() {
				final String customer_id = inputA4AllData.sendPeople.B_CustomerID == null ||
						                           inputA4AllData.sendPeople.B_CustomerID.trim().isEmpty() ?
						                           "000000000000" : inputA4AllData.sendPeople.B_CustomerID;
				
				final String product_price = String.valueOf(inputA4AllData.carryPrice);
				final String insurance = String.valueOf(inputA4AllData.productPrice);
				
				@SuppressLint("SimpleDateFormat") final String delivery_date = new SimpleDateFormat("yyyyMMdd").format(inputA4AllData.hopeReceiveDate);
				
				final String customer_postcode = inputA4AllData.receivePeople.poNotDashNumber.trim().isEmpty() ? "" :
						                                 String.format("%s%s", inputA4AllData.receivePeople.poNotDashNumber.substring(3), inputA4AllData.receivePeople.areaCode);
				
				final String temperatureCode = transferTemperatureCode(inputA4AllData.temperate);
				final String hopeArriveTimeCode = transferHopeArriveTimeCode(inputA4AllData.hopeArriveTime);
				
				final HashMap<String, Bitmap> bitmaps = new HashMap<>();
				final boolean[] checkCallback = { true };
				for(final String waybillNo : inputA4AllData.getWaybillNumber()) {
					InputA4NoteManager.super.mApiDataManager.getQRCodeBitmap(customer_id, waybillNo, product_price, temperatureCode, customer_postcode,
							delivery_date, hopeArriveTimeCode, insurance, printInvoice, printDonation, invoiceUni, common, new ApiDataManager.onFileCallback<Bitmap>()
							{
								@Override public void onGetFileSuccess(Bitmap data) {
									bitmaps.put(waybillNo, data);
								}
								
								@Override public void onGetFileFailure() {
									checkCallback[ 0 ] = false;
								}
							});
				}
				
				while(bitmaps.size() != inputA4AllData.getWaybillNumber().size()) {
					// 等待
				}
				
				if(checkCallback[ 0 ])
					callback.onSuccess(bitmaps);
				else
					callback.onFailure();
			}
		});
	}
	
	/**
	 * 透過 EGS 傳 OBT 資料
	 *
	 * @param inputA4AllData 以驗證過的資料(包含託運單號)
	 */
	@SuppressLint("SimpleDateFormat") @Override
	public void sendOBTData(final InputA4AllData inputA4AllData) {
		
		
		super.runUiThreadSetNewLooper(new runUiThreadAndDoSomething()
		{
			@Override public void doSomething() {
				final OBTModel obtModel = new OBTModel();
				obtModel.order_no = inputA4AllData.orderId;
				obtModel.receiver_name = inputA4AllData.receivePeople.name;
				obtModel.receiver_address = inputA4AllData.receivePeople.address;
				obtModel.receiver_suda5 = inputA4AllData.receivePeople.poNumber.replace("-", "");
				obtModel.receiver_mobile = inputA4AllData.receivePeople.cellphone;
				obtModel.receiver_phone = inputA4AllData.receivePeople.telephone;
				obtModel.sender_name = inputA4AllData.sendPeople.name;
				obtModel.sender_address = inputA4AllData.sendPeople.address;
				obtModel.sender_suda5 = inputA4AllData.sendPeople.poNumber.replace("-", "");
				obtModel.sender_phone = inputA4AllData.sendPeople.telephone;
				obtModel.product_price = String.valueOf(inputA4AllData.carryPrice);
				obtModel.product_name = inputA4AllData.goodsName;
				obtModel.comment = inputA4AllData.note;
				obtModel.temperature = transferTemperatureCode(inputA4AllData.temperate);
				obtModel.delivery_date = new SimpleDateFormat("yyyy-MM-dd").format(inputA4AllData.hopeReceiveDate);
				obtModel.delivery_timezone = transferHopeArriveTimeCode(inputA4AllData.hopeArriveTime);
				obtModel.account_id = inputA4AllData.sendPeople.B_CustomerID.trim().isEmpty() ? "000000000000" : inputA4AllData.sendPeople.B_CustomerID;
				obtModel.insurance = String.valueOf(inputA4AllData.productPrice);
				
				for(String waybillNo : inputA4AllData.getWaybillNumber()) {
					obtModel.tracking_number = waybillNo;
					InputA4NoteManager.super.mApiDataManager.postOBTData(obtModel);
				}
			}
		});
	}
	
	
	/**
	 * 依據 key, 取得 linkedTreeMap 裡 node 的資料
	 *
	 * @param linkedTreeMap linkedTreeMap
	 * @param key           key
	 * @return value
	 */
	private String mGetLinkedTreeMapData(LinkedTreeMap linkedTreeMap, String key) {
		Object object = linkedTreeMap.get(key);
		if(object == null)
			return "";
		return object.toString();
	}
	
	/**
	 * 轉換溫層
	 *
	 * @param temperatureStr 溫層對應的中文
	 * @return 對應的代碼
	 */
	private static String transferTemperatureCode(String temperatureStr) {
		switch(temperatureStr.trim()) {
			case "常溫":
				return "0001";
			case "冷藏":
				return "0002";
			case "冷凍":
				return "0003";
			default: {
				Log.e(TAG, "轉換 溫層 發生錯誤: ", null);
				return "";
			}
		}
	}
	
	/**
	 * 轉換希望配達時間
	 *
	 * @param hopeArriveTimeStr 希望配達時間對應的中文
	 * @return 對應的代碼
	 */
	private static String transferHopeArriveTimeCode(String hopeArriveTimeStr) {
		switch(hopeArriveTimeStr.trim()) {
			case "不指定":
				return "4";
			case "13時前":
				return "1";
			case "14-18時":
				return "2";
			default: {
				Log.e(TAG, "轉換 希望配達時間 發生錯誤: ", null);
				return "";
			}
		}
	}
	
	/**
	 * 依據資料創造出 Pdf 文檔
	 *
	 * @param context        當前的 context
	 * @param inputA4AllData 資料
	 * @param onDAOCallback  callback
	 */
	@Override @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public void drawPdfDocument(final Context context, final InputA4AllData inputA4AllData,
			final AllBusinessCallback.onDAOCallback<File> onDAOCallback) {
		super.runUiThreadSetNewLooper(new runUiThreadAndDoSomething() {
			@Override public void doSomething() {
				File file = InputA4NoteManager.this.mPdfService.drawPdfDocument(context, inputA4AllData);
				onDAOCallback.onSuccess(file);
			}
		});
	}
	
	/**
	 * 將 pdf 文檔, 顯示於安卓原生的瀏覽畫面
	 *
	 * @param context 當前的 context
	 * @param file    pdf 文檔
	 */
	@Override @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public void doPdfViewToPrint(final Context context, final File file) {
		super.runUiThreadSetNewLooper(new runUiThreadAndDoSomething() {
			@Override public void doSomething() {
				InputA4NoteManager.this.mPdfService.doPdfViewToPrint(context, file);
			}
		});
	}
}
