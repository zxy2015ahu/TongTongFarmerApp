package com.tongtong.purchaser.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import com.tongtong.purchaser.widget.StyleableToast;

public class BitmapUtil {

	public static Bitmap stringtoBitmap(String string) {

		Bitmap bitmap = null;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(string, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static String bitmaptoString(Bitmap bitmap) {
		String string = null;
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, bStream);
		byte[] bytes = bStream.toByteArray();
		string = Base64.encodeToString(bytes, Base64.DEFAULT);
		return string;
	}
	
	public static Bitmap fitBitmap(Bitmap target, int newWidth)  
	 {  
	  int width = target.getWidth();  
	  int height = target.getHeight();  
	  Matrix matrix = new Matrix();  
	  float scaleWidth = ((float) newWidth) / width;  
	  
	  
	  matrix.postScale(scaleWidth, scaleWidth);  
	    
	  Bitmap bmp = Bitmap.createBitmap(target, 0, 0, width, height, matrix,  
	    true);  
	  if (target != null && !target.equals(bmp) && !target.isRecycled())  
	  {  
	   target.recycle();  
	   target = null;  
	  }  
	  return bmp; 
	 } 
	
	public  static File saveBitmap(Bitmap bitmap, String path) {
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}

		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
			bitmap.compress(CompressFormat.PNG, 100, fOut);
			fOut.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (fOut != null)
					fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return f;
	}
	public  static File saveBitmap(Bitmap bitmap, String path,int newWidth) {
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}

		FileOutputStream fOut = null;
		try {
			bitmap=fitBitmap(bitmap,newWidth);
			f.createNewFile();
			fOut = new FileOutputStream(f);
			bitmap.compress(CompressFormat.PNG, 100, fOut);
			fOut.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (fOut != null)
					fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return f;
	}
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,int newWidth) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),  
                bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        final RectF rectF = new RectF(rect);  
        final float roundPx = bitmap.getWidth() / 2;  
  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);
        output = fitBitmap(output,newWidth);
        return output;  
    }
	public static void saveBmp2Gallery(Bitmap bmp, Context mContext) {

		String fileName = null;
		//系统相册目录
		String galleryPath= Environment.getExternalStorageDirectory()
				+ File.separator + Environment.DIRECTORY_DCIM
				+File.separator+"Camera"+File.separator;


		// 声明文件对象
		File file = null;
		// 声明输出流
		FileOutputStream outStream = null;

		try {
			// 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
			file = new File(galleryPath, System.currentTimeMillis()+ ".jpg");

			// 获得文件相对路径
			fileName = file.toString();
			// 获得输出流，如果文件中有内容，追加内容
			outStream = new FileOutputStream(fileName);
			if (null != outStream) {
				bmp.compress(CompressFormat.JPEG, 100, outStream);
			}

		} catch (Exception e) {
			e.getStackTrace();
		}finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
         }
             //通知相册更新
       MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
				bmp, fileName, null);
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(file);
		intent.setData(uri);
		mContext.sendBroadcast(intent);

		StyleableToast.info(mContext,"已保存至系统相册");

	}
}
