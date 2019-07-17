package com.suda.bluetoothprintproject.businessManagers.businessService.printerControl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;

import com.suda.bluetoothprintproject.BuildConfig;
import com.suda.bluetoothprintproject.businessManagers.businessService.IPDFService;
import com.suda.bluetoothprintproject.businessManagers.businessService.adapter.PrintPdfAdapter;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4AllData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 依據對應的物件產生對應的 PDF 文檔
 */
public class PDFService implements IPDFService<InputA4AllData>
{
	private final String mTemplatePdfFileName = "6-15.pdf";
	
	public PDFService(Context context) {
		// 將 樣板 從 assets 拷貝到 app 資料夾下
		String rootPath = context.getFilesDir().getPath();
		try {
			File file = new File(rootPath + "/" + this.mTemplatePdfFileName); // 指定資料夾路徑
			if(!file.exists()) file.createNewFile();
			InputStream is = context.getAssets().open(this.mTemplatePdfFileName);
			FileOutputStream out = new FileOutputStream(file);
			
			byte[] buffer = new byte[ 1024 ];
			int byteCount;
			while((byteCount = is.read(buffer)) != -1) // 複製檔案至輸出流
				out.write(buffer, 0, byteCount);
			
			out.flush(); // 儲存.
			is.close();
			out.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override public File drawPdfDocument(Context context, InputA4AllData inputA4AllData) {
		{
			PrintAttributes attributes = new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4)
					                             // 這個是打印機的分辨綠設定. 例如，具有600 DPI的打印機可以生成具有300 DPI分辨率的高質量圖像。
					                             //	.setResolution(new PrintAttributes.Resolution("kevinPrintAdmin", "print", 600, 600))
					                             //	.setResolution(new PrintAttributes.Resolution("kevinPrintAdmin", "print", 2400, 600))
					                             .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
					                             //	.setColorMode(PrintAttributes.COLOR_MODE_COLOR)
					                             //	.setDuplexMode(PrintAttributes.DUPLEX_MODE_NONE) // 設置雙面列印, 需要 api > 23
					                             .build();
			PrintedPdfDocument pdfDocument = new PrintedPdfDocument(context, attributes);
			PdfDocument.Page page;
			Canvas canvas;
			TextPaint textPaint = mMyTextPaint(8);
			Bitmap bitmap = this.mGetTemplateFromPdf(context);
			
			for(int i = 0; i < inputA4AllData.getWaybillNumber().size(); i++) {
				page = pdfDocument.startPage(i);
				canvas = page.getCanvas();
				canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG)); //設置抗鋸齒相關的
				canvas.drawBitmap(bitmap, 0, 0, null); // 印模
				
				// 資料來源
				final String waybillDashNumber = inputA4AllData.getWaybillNumber().isEmpty() ? "" // 幫託運單號加 dash
						                                 : String.format("%s-%s-%s", inputA4AllData.getWaybillNumber().get(i).substring(0, 4)
						, inputA4AllData.getWaybillNumber().get(i).substring(4, 8), inputA4AllData.getWaybillNumber().get(i).substring(8, 12));
				final String receiveAddressUsePoNumber = inputA4AllData.receivePeople.poNotDashNumber.trim().isEmpty() ? // 收件人地址使用的郵號(七碼扣掉前兩碼).
						                                         "" : inputA4AllData.receivePeople.poNotDashNumber.trim().substring(3, 8);
				
				final String sendAddressUsePoNumber = inputA4AllData.sendPeople.poNumber.trim().isEmpty() ? // 寄件人地址使用的郵號
						                                      "" : inputA4AllData.sendPeople.poNumber.trim().replace("-", "").substring(2, 7);
				
				// 左上角區塊
				int leftBaseLine = 20;
				int topBaseLine = 62;
				mDrawText(canvas, textPaint, 12, inputA4AllData.receivePeople.baseNumber, leftBaseLine + 10, topBaseLine - 15);
				mDrawCode39Bitmap(context, canvas, true, inputA4AllData.getWaybillNumber().get(i), 8, leftBaseLine + 45, topBaseLine - 20);
				mDrawText(canvas, textPaint, "包裹查詢碼 " + waybillDashNumber, leftBaseLine + 45, topBaseLine - 13);
				mDrawText(canvas, textPaint, "收貨日", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, "希望配達日", leftBaseLine + 45, topBaseLine);
				mDrawText(canvas, textPaint, "希望配達時段", leftBaseLine + 87, topBaseLine);
				mDrawText(canvas, textPaint, "發貨所", leftBaseLine + 140, topBaseLine);
				topBaseLine += 12;
				@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				mDrawText(canvas, textPaint, sdf.format(inputA4AllData.sendDate), leftBaseLine - 5, topBaseLine);
				mDrawText(canvas, textPaint, sdf.format(inputA4AllData.hopeReceiveDate), leftBaseLine + 42, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.hopeArriveTime, leftBaseLine + 90, topBaseLine);
				topBaseLine += 15;
				mDrawText(canvas, textPaint, "收", leftBaseLine, topBaseLine);
				ArrayList<String> receiveAddressList = this.mGetMultiLineAddress(receiveAddressUsePoNumber + inputA4AllData.receivePeople.address, 20);
				mDrawText(canvas, textPaint, receiveAddressList.get(0), leftBaseLine + 15, topBaseLine - 4);
				mDrawText(canvas, textPaint, receiveAddressList.size() > 1 ? receiveAddressList.get(1) : "", leftBaseLine + 15, topBaseLine + 5);
				mDrawText(canvas, textPaint, "件", leftBaseLine, topBaseLine + 10);
				mDrawText(canvas, textPaint, inputA4AllData.receivePeople.name, leftBaseLine + 15, topBaseLine + 14);
				mDrawText(canvas, textPaint, "人", leftBaseLine, topBaseLine + 20);
				mDrawText(canvas, textPaint, inputA4AllData.receivePeople.cellphone.trim() + "    " + inputA4AllData.receivePeople.telephone.trim(), leftBaseLine + 15, topBaseLine + 24);
				topBaseLine += 40;
				mDrawText(canvas, textPaint, "寄", leftBaseLine, topBaseLine);
				ArrayList<String> sandAddressList = this.mGetMultiLineAddress(sendAddressUsePoNumber + inputA4AllData.sendPeople.address, 20);
				mDrawText(canvas, textPaint, sandAddressList.get(0), leftBaseLine + 15, topBaseLine - 5);
				mDrawText(canvas, textPaint, sandAddressList.size() > 1 ? sandAddressList.get(1) : "", leftBaseLine + 15, topBaseLine + 4);
				mDrawText(canvas, textPaint, "件", leftBaseLine, topBaseLine + 10);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.name, leftBaseLine + 15, topBaseLine + 13);
				mDrawText(canvas, textPaint, "人", leftBaseLine, topBaseLine + 20);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.cellphone.trim() + "    " + inputA4AllData.sendPeople.telephone.trim(), leftBaseLine + 15, topBaseLine + 23);
				topBaseLine += 35;
				mDrawText(canvas, textPaint, "品名", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, "代收貨款", leftBaseLine + 90, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.goodsName, leftBaseLine + 15, topBaseLine + 15);
				if(inputA4AllData.carryPrice > 0) { // 判斷是否為 代收
					mDrawText(canvas, textPaint, 12, String.valueOf(inputA4AllData.carryPrice), leftBaseLine + 100, topBaseLine + 15);
				}
				else {
					mDrawText(canvas, textPaint, 12, "不收款", leftBaseLine + 100, topBaseLine + 15);
					// if(data.productPrice == 0)
					// mDrawText(canvas, textPaint, 12, "不收款", leftBaseLine + 100, topBaseLine + 15);
					// else
					// mDrawText(canvas, textPaint, 12, String.valueOf(data.productPrice), leftBaseLine + 100, topBaseLine + 15);
				}
				topBaseLine += 35;
				mDrawText(canvas, textPaint, "訂單編號", leftBaseLine, topBaseLine);
				// mDrawCode39Bitmap(context, canvas, false, data.orderId, 12, leftBaseLine + 40, topBaseLine + 10);
				
				// 右上角區塊
				mDrawCode39Bitmap(context, canvas, true, inputA4AllData.receivePeople.poNotDashNumber, 12, 220, 49);
				mDrawText(canvas, textPaint, 24, String.format("%s%s", inputA4AllData.receivePeople.poNumber, inputA4AllData.receivePeople.areaCode), 353, 35);
				mDrawText(canvas, textPaint, "b" + BuildConfig.VERSION_NAME, 435, 45);
				mDrawText(canvas, textPaint, "希望配達日", 495, 33);
				mDrawText(canvas, textPaint, 24, inputA4AllData.temperate.substring(0, 1), 560, 24);
				mDrawText(canvas, textPaint, 24, inputA4AllData.temperate.substring(1, 2), 560, 49);
				if(inputA4AllData.carryPrice > 0) { // 判斷是否 代收
					mDrawText(canvas, textPaint, 24, "代", 560, 79);
					mDrawText(canvas, textPaint, 24, "收", 560, 104);
				}
				if(inputA4AllData.productPrice > 0) { // 判斷是否 報值
					mDrawText(canvas, textPaint, 24, "報", 560, 79);
					mDrawText(canvas, textPaint, 24, "值", 560, 104);
				}
				if(inputA4AllData.easyBrokeItem) {
					mDrawText(canvas, textPaint, 24, "易", 560, 136);
					mDrawText(canvas, textPaint, 24, "碎", 560, 159);
				}
				if(inputA4AllData.precisionItem) {
					mDrawText(canvas, textPaint, 24, "精", 560, 189);
					mDrawText(canvas, textPaint, 24, "密", 560, 214);
				}
				final String[] shortDateArray = sdf.format(inputA4AllData.hopeReceiveDate).split("/");
				mDrawText(canvas, textPaint, 12, shortDateArray[ 1 ] + "-" + shortDateArray[ 2 ], 498, 47);
				// mDrawText(canvas, textPaint, 10, shortDateArray[ 2 ], 505, 34);
				mDrawText(canvas, textPaint, "希望配達時段", 490, 60);
				mDrawText(canvas, textPaint, 12, inputA4AllData.hopeArriveTime, 496, 75);
				mDrawText(canvas, textPaint, "溫層/尺寸", 495, 90);
				mDrawText(canvas, textPaint, 12, inputA4AllData.temperate, 501, 105);
				Bitmap qrCode = inputA4AllData.getBitmap_QR_CODE().get(inputA4AllData.getWaybillNumber().get(i));
				if(qrCode != null) {
					canvas.drawBitmap(qrCode, 485, 113, null);
				}
				leftBaseLine = 215;
				topBaseLine = 62;
				mDrawText(canvas, textPaint, "收", leftBaseLine, topBaseLine);
				receiveAddressList = this.mGetMultiLineAddress(inputA4AllData.receivePeople.address, 28);
				mDrawText(canvas, textPaint, 10, receiveAddressList.get(0), leftBaseLine + 15, topBaseLine);
				mDrawText(canvas, textPaint, 10, receiveAddressList.size() > 1 ? receiveAddressList.get(1) : "", leftBaseLine + 15, topBaseLine + 10);
				mDrawText(canvas, textPaint, "件", leftBaseLine, topBaseLine + 10);
				mDrawText(canvas, textPaint, 10, inputA4AllData.receivePeople.name, leftBaseLine + 15, topBaseLine + 19);
				mDrawText(canvas, textPaint, "人", leftBaseLine, topBaseLine + 20);
				mDrawText(canvas, textPaint, 10, inputA4AllData.receivePeople.cellphone.trim() + "    " + inputA4AllData.receivePeople.telephone.trim(), leftBaseLine + 15, topBaseLine + 28);
				topBaseLine += 40;
				mDrawText(canvas, textPaint, "寄", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, sendAddressUsePoNumber + inputA4AllData.sendPeople.address, leftBaseLine + 15, topBaseLine);
				mDrawText(canvas, textPaint, "件", leftBaseLine, topBaseLine + 10);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.name, leftBaseLine + 15, topBaseLine + 10);
				mDrawText(canvas, textPaint, "人", leftBaseLine, topBaseLine + 20);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.cellphone.trim() + "    " + inputA4AllData.sendPeople.telephone.trim(), leftBaseLine + 15, topBaseLine + 20);
				topBaseLine += 35;
				mDrawText(canvas, textPaint, "備註", leftBaseLine, topBaseLine);
				String note;
				if(inputA4AllData.productPrice > 0)
					note = "報值金額: " + inputA4AllData.productPrice + " 元. " + inputA4AllData.note;
				else
					note = inputA4AllData.note;
				mDrawText(canvas, textPaint, note, leftBaseLine + 30, topBaseLine);
				topBaseLine += 15;
				mDrawText(canvas, textPaint, "品名", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.goodsName, leftBaseLine + 30, topBaseLine);
				topBaseLine += 15;
				mDrawText(canvas, textPaint, "訂單編號", leftBaseLine - 2, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.orderId, leftBaseLine + 50, topBaseLine);
				topBaseLine += 10;
				mDrawText(canvas, textPaint, "客代", leftBaseLine, topBaseLine + 2);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.B_CustomerID.length() > 10 ? inputA4AllData.sendPeople.B_CustomerID.substring(0, 10) : inputA4AllData.sendPeople.B_CustomerID
						, leftBaseLine + 30, topBaseLine + 2);
				mDrawText(canvas, textPaint, "收貨日:" + sdf.format(inputA4AllData.sendDate), leftBaseLine + 180, topBaseLine + 2);
				topBaseLine += 13;
				mDrawText(canvas, textPaint, "單號", leftBaseLine, topBaseLine - 3);
				mDrawText(canvas, textPaint, waybillDashNumber, leftBaseLine + 20, topBaseLine - 3);
				mDrawCode39Bitmap(context, canvas, true, inputA4AllData.getWaybillNumber().get(i), 12, leftBaseLine - 5, topBaseLine + 35);
				
				mDrawText(canvas, textPaint, "代", leftBaseLine + 182, topBaseLine);
				mDrawText(canvas, textPaint, "收", leftBaseLine + 182, topBaseLine + 10);
				if(inputA4AllData.carryPrice > 0) { // 判斷是否為 代收
					mDrawText(canvas, textPaint, 12, String.valueOf(inputA4AllData.carryPrice), leftBaseLine + 195, topBaseLine + 15);
				}
				else {
					mDrawText(canvas, textPaint, 12, "不收款", leftBaseLine + 195, topBaseLine + 15);
				}
				mDrawText(canvas, textPaint, "貨", leftBaseLine + 182, topBaseLine + 20);
				mDrawText(canvas, textPaint, "款", leftBaseLine + 182, topBaseLine + 30);
				mDrawText(canvas, textPaint, "收", leftBaseLine + 245, topBaseLine);
				mDrawText(canvas, textPaint, "件", leftBaseLine + 245, topBaseLine + 8);
				mDrawText(canvas, textPaint, "人", leftBaseLine + 245, topBaseLine + 16);
				mDrawText(canvas, textPaint, "簽", leftBaseLine + 245, topBaseLine + 24);
				mDrawText(canvas, textPaint, "名", leftBaseLine + 245, topBaseLine + 32);
				
				// 左下角區塊
				leftBaseLine = 20;
				topBaseLine = 242;
				mDrawText(canvas, textPaint, "託運單號", leftBaseLine, topBaseLine + 3);
				mDrawText(canvas, textPaint, waybillDashNumber, leftBaseLine + 45, topBaseLine + 3);
				topBaseLine += 18;
				mDrawText(canvas, textPaint, "收貨日", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, "希望配達日", leftBaseLine + 45, topBaseLine);
				mDrawText(canvas, textPaint, "希望配達時段", leftBaseLine + 87, topBaseLine);
				mDrawText(canvas, textPaint, "代收貨款", leftBaseLine + 140, topBaseLine);
				topBaseLine += 15;
				mDrawText(canvas, textPaint, sdf.format(inputA4AllData.sendDate), leftBaseLine - 5, topBaseLine - 3);
				mDrawText(canvas, textPaint, sdf.format(inputA4AllData.hopeReceiveDate), leftBaseLine + 42, topBaseLine - 3);
				mDrawText(canvas, textPaint, inputA4AllData.hopeArriveTime, leftBaseLine + 90, topBaseLine - 3);
				if(inputA4AllData.carryPrice > 0) { // 判斷是否為 代收
					mDrawText(canvas, textPaint, String.valueOf(inputA4AllData.carryPrice), leftBaseLine + 140, topBaseLine - 3);
				}
				else {
					mDrawText(canvas, textPaint, "不收款", leftBaseLine + 140, topBaseLine - 3);
				}
				topBaseLine += 15;
				mDrawText(canvas, textPaint, "收", leftBaseLine, topBaseLine);
				receiveAddressList = this.mGetMultiLineAddress(receiveAddressUsePoNumber + inputA4AllData.receivePeople.address, 20);
				mDrawText(canvas, textPaint, receiveAddressList.get(0), leftBaseLine + 15, topBaseLine - 2);
				mDrawText(canvas, textPaint, receiveAddressList.size() > 1 ? receiveAddressList.get(1) : "", leftBaseLine + 15, topBaseLine + 7);
				mDrawText(canvas, textPaint, "件", leftBaseLine, topBaseLine + 10);
				mDrawText(canvas, textPaint, inputA4AllData.receivePeople.name, leftBaseLine + 15, topBaseLine + 18);
				mDrawText(canvas, textPaint, "人", leftBaseLine, topBaseLine + 20);
				mDrawText(canvas, textPaint, inputA4AllData.receivePeople.cellphone.trim() + "    " + inputA4AllData.receivePeople.telephone.trim(), leftBaseLine + 15, topBaseLine + 28);
				topBaseLine += 45;
				mDrawText(canvas, textPaint, "寄", leftBaseLine, topBaseLine);
				sandAddressList = this.mGetMultiLineAddress(sendAddressUsePoNumber + inputA4AllData.sendPeople.address, 20);
				mDrawText(canvas, textPaint, sandAddressList.get(0), leftBaseLine + 15, topBaseLine - 7);
				mDrawText(canvas, textPaint, sandAddressList.size() > 1 ? sandAddressList.get(1) : "", leftBaseLine + 15, topBaseLine + 2);
				mDrawText(canvas, textPaint, "件", leftBaseLine, topBaseLine + 10);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.name, leftBaseLine + 15, topBaseLine + 13);
				mDrawText(canvas, textPaint, "人", leftBaseLine, topBaseLine + 20);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.cellphone.trim() + "    " + inputA4AllData.sendPeople.telephone.trim(), leftBaseLine + 15, topBaseLine + 23);
				topBaseLine += 40;
				mDrawText(canvas, textPaint, "訂單編號", leftBaseLine, topBaseLine);
				// mDrawCode39Bitmap(context, canvas, false, data.orderId, 12, leftBaseLine + 40, topBaseLine);
				topBaseLine += 20;
				mDrawText(canvas, textPaint, "品名", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.goodsName, leftBaseLine + 30, topBaseLine);
				
				// 右下角區塊
				leftBaseLine = 212;
				topBaseLine = 245;
				mDrawText(canvas, textPaint, "收貨日", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, sdf.format(inputA4AllData.sendDate), leftBaseLine + 50, topBaseLine);
				mDrawText(canvas, textPaint, "發貨所", leftBaseLine + 130, topBaseLine);
				mDrawText(canvas, textPaint, "希望配達日", 490, topBaseLine);
				topBaseLine += 15;
				mDrawText(canvas, textPaint, "收件人", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, "寄件人", leftBaseLine + 130, topBaseLine);
				mDrawText(canvas, textPaint, 10, shortDateArray[ 1 ] + "月", 495, topBaseLine);
				topBaseLine += 10;
				receiveAddressList = this.mGetMultiLineAddress(receiveAddressUsePoNumber + inputA4AllData.receivePeople.address, 15);
				mDrawText(canvas, textPaint, receiveAddressList.get(0), leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, receiveAddressList.size() > 1 ? receiveAddressList.get(1) : "", leftBaseLine, topBaseLine + 10);
				sandAddressList = this.mGetMultiLineAddress(sendAddressUsePoNumber + inputA4AllData.sendPeople.address, 20);
				mDrawText(canvas, textPaint, sandAddressList.get(0), leftBaseLine + 130, topBaseLine);
				mDrawText(canvas, textPaint, sandAddressList.size() > 1 ? sandAddressList.get(1) : "", leftBaseLine + 130, topBaseLine + 9);
				topBaseLine += 15;
				mDrawText(canvas, textPaint, inputA4AllData.receivePeople.name, leftBaseLine, topBaseLine + 7);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.name, leftBaseLine + 130, topBaseLine + 7);
				mDrawText(canvas, textPaint, 10, shortDateArray[ 2 ] + "日", 510, topBaseLine);
				topBaseLine += 10;
				mDrawText(canvas, textPaint, inputA4AllData.receivePeople.cellphone.trim() + "    " + inputA4AllData.receivePeople.telephone.trim(), leftBaseLine, topBaseLine + 7);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.cellphone.trim() + "    " + inputA4AllData.sendPeople.telephone.trim(), leftBaseLine + 130, topBaseLine + 7);
				mDrawText(canvas, textPaint, "希望配達時段", 487, topBaseLine - 2);
				mDrawText(canvas, textPaint, 10, inputA4AllData.hopeArriveTime, 490, topBaseLine + 30);
				topBaseLine += 25;
				mDrawText(canvas, textPaint, "訂單編號", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.orderId, leftBaseLine + 50, topBaseLine);
				topBaseLine += 15;
				mDrawText(canvas, textPaint, "品名", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.goodsName, leftBaseLine + 30, topBaseLine);
				topBaseLine += 17;
				mDrawText(canvas, textPaint, "客代", leftBaseLine, topBaseLine);
				mDrawText(canvas, textPaint, inputA4AllData.sendPeople.B_CustomerID.length() > 10 ? inputA4AllData.sendPeople.B_CustomerID.substring(0, 10) : inputA4AllData.sendPeople.B_CustomerID
						, leftBaseLine + 60, topBaseLine - 3);
				mDrawCode39Bitmap(context, canvas, false, inputA4AllData.sendPeople.B_CustomerID.length() > 10 ? inputA4AllData.sendPeople.B_CustomerID.substring(0, 10) : inputA4AllData.sendPeople.B_CustomerID
						, 12, leftBaseLine + 27, topBaseLine + 8);
				mDrawText(canvas, textPaint, "代收貨款", leftBaseLine + 170, topBaseLine);
				if(inputA4AllData.carryPrice > 0) { // 判斷是否為 代收
					mDrawText(canvas, textPaint, 12, String.valueOf(inputA4AllData.carryPrice), leftBaseLine + 170, topBaseLine + 30);
				}
				else {
					mDrawText(canvas, textPaint, 12, "不收款", leftBaseLine + 170, topBaseLine + 30);
				}
				mDrawText(canvas, textPaint, "寄件人簽名", leftBaseLine + 225, topBaseLine);
				topBaseLine += 10;
				mDrawText(canvas, textPaint, "單號", leftBaseLine, topBaseLine + 7);
				mDrawText(canvas, textPaint, waybillDashNumber, leftBaseLine + 50, topBaseLine + 7);
				mDrawCode39Bitmap(context, canvas, true, inputA4AllData.getWaybillNumber().get(i), 10, leftBaseLine + 10, topBaseLine + 39);
				mDrawText(canvas, textPaint, "溫層/尺寸", 495, topBaseLine);
				mDrawText(canvas, textPaint, 10, inputA4AllData.temperate, 495, topBaseLine + 13);
				
				canvas.save();
				canvas.restore();
				pdfDocument.finishPage(page);
			}
			
			File file = null;
			try {
				String rootPath = context.getFilesDir().getPath();
				String _WaitPrintPdfFileName = "Template_V3_print.pdf";
				file = new File(rootPath, _WaitPrintPdfFileName);
				FileOutputStream outputStream = new FileOutputStream(file);
				pdfDocument.writeTo(outputStream);
				outputStream.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			finally {
				pdfDocument.close();
				bitmap.recycle();
			}
			return file;
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override public void doPdfViewToPrint(Context context, File file) {
		PrintAttributes attributes = new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4)
				                             // 這個是打印機的分辨綠設定. 例如，具有600 DPI的打印機可以生成具有300 DPI分辨率的高質量圖像。
				                             // .setResolution(new PrintAttributes.Resolution("kevinPrintAdmin", "print", 2400, 600))
				                             .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
				                             //	.setColorMode(PrintAttributes.COLOR_MODE_COLOR)
				                             //	.setDuplexMode(PrintAttributes.DUPLEX_MODE_NONE) // 設置雙面列印, 需要 api > 23
				                             .build();
		PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
		PrintPdfAdapter myPrintAdapter = new PrintPdfAdapter(file.getPath());
		printManager.print("printJob", myPrintAdapter, attributes);
	}
	
	// 寫字於畫布上
	private void mDrawText(Canvas canvas, TextPaint paint, String textString, int x, int y) {
		canvas.drawText(textString, 0, textString.length(), x, y, paint);
	}
	
	// 寫字於畫布上, 可調整字形大小
	private void mDrawText(Canvas canvas, TextPaint paint, int textSize, String textString, int x, int y) {
		final float size = paint.getTextSize();
		paint.setTextSize(textSize);
		canvas.drawText(textString, 0, textString.length(), x, y, paint);
		paint.setTextSize(size);
	}
	
	// 設字畫比顏色
	private TextPaint mMyTextPaint(int fontSize) {
		TextPaint textPaint = new TextPaint();
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(fontSize);
		//		textPaint.setTextAlign(Paint.Align.LEFT);
		Typeface textTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
		textPaint.setTypeface(textTypeface);
		return textPaint;
	}
	
	// 使用 Code39 字形
	private void mDrawCode39Bitmap(Context context, Canvas canvas, boolean IsHeightFont, String barcodeText, int textSize, int x, int y) {
		TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		Typeface typeface;
		if(IsHeightFont)
			typeface = Typeface.createFromAsset(context.getAssets(), "IDAutomationC39M.ttf");
		else
			typeface = Typeface.createFromAsset(context.getAssets(), "3of9.ttf");
		paint.setTypeface(typeface);
		paint.setTextSize(textSize);
		paint.setDither(true);
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		String code39Str = "*" + barcodeText + "*";
		mDrawText(canvas, paint, code39Str, x, y);
	}
	
	// 地址切行
	private ArrayList<String> mGetMultiLineAddress(String addressStr, int lineSize) {
		ArrayList<String> result = new ArrayList<>();
		if(addressStr.length() <= lineSize) {
			result.add(addressStr);
			return result;
		}
		result.add(addressStr.substring(0, lineSize));
		result.add(addressStr.substring(lineSize));
		return result;
	}
	
	// 將 pdf 樣板轉為 bitmap
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private Bitmap mGetTemplateFromPdf(Context context) {
		String rootPath = context.getFilesDir().getPath();
		ParcelFileDescriptor parcelFileDescriptor = null;
		PdfRenderer pdfRenderer = null;
		PdfRenderer.Page currentPage = null;
		Bitmap bitmap = null;
		try {
			parcelFileDescriptor = ParcelFileDescriptor.open(new File(rootPath + "/" + this.mTemplatePdfFileName),
					ParcelFileDescriptor.MODE_READ_WRITE);
			
			pdfRenderer = new PdfRenderer(parcelFileDescriptor); // 創建 pdfRender
			currentPage = pdfRenderer.openPage(0);
			pdfRenderer.shouldScaleForPrinting();
			bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
					Bitmap.Config.ARGB_8888); // 第三個位數代表壓縮層級. 8888 加起來就是 32 位元品質的壓縮.
			// Rect 是繪製矩形的物件, matrix 是動畫效果. 可上網查. 第三個參數是要點: 顯示於螢幕上 或者 列印.
			// 渲染
			currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			
			try // Objects.requireNonNull(...) 可調式若為 null 的物件.
			{
				Objects.requireNonNull(currentPage).close();
				Objects.requireNonNull(pdfRenderer).close();
				Objects.requireNonNull(parcelFileDescriptor).close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}
}
