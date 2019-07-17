package com.suda.bluetoothprintproject.api.model;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OBTModel implements IModelInterface
{
	public final String cmd = "transfer_waybill";
	public final String customer_id = "0000000000";
	public String tracking_number;
	public String order_no;
	public String receiver_name;
	public String receiver_address;
	public String receiver_suda5;
	public String receiver_mobile;
	public String receiver_phone;
	
	public String sender_name;
	public String sender_address;
	public String sender_suda5;
	public String sender_phone;
	public String product_price;
	public String product_name;
	public String comment;
	public final String package_size = "0001";
	public String temperature;
	public final String distance = "01";
	public String delivery_date;
	public String delivery_timezone;
	public final String create_time = getNowDateTime(new Date(), "yyyy-MM-dd HH:mm:ss");
	public final String print_time = getNowDateTime(new Date(), "yyyy-MM-dd HH:mm:ss");
	public String account_id;
	public final String member_no = "";
	public final String taxin = "";
	public String insurance;
	public final String upload_type = "APP";
	
	@SuppressLint("SimpleDateFormat") public String getNowDateTime(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
}
