package com.suda.bluetoothprintproject.api.model;

// 郵號 || 理貨區碼(理貨區碼沒有 dash)
public class PostalNumber implements IModelInterface
{
	public String Code;
	public String Message;
	public String Data;
	
	
	/**
	 * 不適用於理貨區碼
	 *
	 * @return 取得沒有 dash 的郵號
	 */
	public String GetNoAddressDashData() {
		if(this.Data != null && !this.Data.trim().isEmpty())
			return String.format("%s%S", "+", this.Data.trim().replace("-", "")); // 前面需要加號
		return "";
	}
	
	
	/**
	 * 當是請求 理貨區碼 api 時，這個方法提供只取得 areaCode。 Ex: "-A"
	 *
	 * @return 理貨區碼的英文字(含 dash)
	 */
	public String GetAreaCode() {
		String areaCode = "";
		
		if(this.Data != null && !this.Data.trim().isEmpty()) {
			String addressAndAreaCode = this.Data.trim();
			String lastStr = addressAndAreaCode.substring(addressAndAreaCode.length() - 1);
			areaCode = lastStr.matches("[a-zA-Z]+") ? String.format("%s%s", "-", lastStr) : "";
		}
		
		return areaCode;
	}
}
