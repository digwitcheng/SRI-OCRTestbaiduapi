
package cn.hxc.imgrecognition;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings.System;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

import cn.hxc.imgrecognitionSRI_OCR.R;

public class MainActivity extends Activity implements OnTouchListener {
	

	private ImageButton button3;
	private ImageView iv_photo;
	private Matrix matrix = new Matrix();
	private int[] newpixels;
	private int[] pixels;
	private int width;
	private int height;

	// 触摸方框
	private float old_x;
	private float old_y;
	private float new_x;
	private float new_y;
	private boolean isInRect;// 判断手指按下时相对于矩形框的位置
	private boolean isInTop;
	private boolean isInBottom;
	private int newbottom;
	private int newtop;
	private Canvas canvas;
	private Paint paint;
	private Rect rect;
	private Bitmap paintbitMap;
	private int offset;
	private PreferencesService service;
	private int margain;

	private GuideUtil guideUtil = null;

	// static {
	// java.lang.System.loadLibrary("imageProcessing");
	// }	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// /**获取引导界面工具类的实例**/
		// guideUtil = GuideUtil.getInstance();
		// /**调用引导界面**/
		// guideUtil.initGuide(this, R.drawable.backgroud);
		// guideUtil.initGuide(MainActivity.this, R.drawable.backgroud);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.image_set);
		
		

		iv_photo = (ImageView) findViewById(R.id.imgView1);
		// Toast.makeText(context, text, duration)
		button3 = (ImageButton) findViewById(R.id.button3);
		// button3.setOnTouchListener(new btnOnTouchListener());
		service = new PreferencesService(this);
		Map<String, String> params = service.getPreferences("margain");// new
																		// Integer(params.get("margain")
		// if(params.get("margain")==null){
		// service.save("0","margain");
		// }
		SharedPreferences preferences = getSharedPreferences("set", MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt("zoom", 8);
		editor.commit();
		
		
		if (!preferences.contains("countSum")) {
			editor.putInt("countSum", 2000);
			editor.putInt("count", 1);
			editor.commit();
		}
		loadFile();
		
		
		Intent intent = new Intent(this, processActivity.class);
		startActivity(intent);
  
	}
	public  void loadFile(){		
		try {
			String filePath = Environment.getExternalStorageDirectory()
					+ File.separator + "KuaiDiBangShou" + File.separator
					+ "Data";
			assetsDataToSD(filePath, "win2.dat");
			assetsDataToSD(filePath, "whi2.dat");
			assetsDataToSD(filePath, "num2");
			assetsDataToSD(filePath, "model14.dat");

			assetsDataToSD(filePath, "win_h.dat");
			assetsDataToSD(filePath, "whi_h.dat");
			assetsDataToSD(filePath, "num_h");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// imageProcess.noequl("win2.dat read fail", 111);
		}
}

	/*
	 * 把数据文件写入手机sd卡
	 */
	public  void assetsDataToSD(String filePath, String fileName)
			throws IOException {
		InputStream myInput;
		myInput = getAssets().open(fileName);

		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		// 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = new File(filePath);// 获取根目录
		} else {
			return;
		}
		if (!sdDir.exists()) {
			sdDir.mkdirs();
		}
		// File dir_file=new
		// File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),fileName);
		OutputStream myOutput = new FileOutputStream(sdDir + "//" + fileName);

		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}
		myOutput.flush();
		myInput.close();
		myOutput.close();
	}

	public final class btnOnTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// 重新设置按下时的背景图片
				((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.set11_d));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				// 再修改为抬起时的正常图片
				((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.set11));
			}
			return false;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		switch (event.getAction()) {// &MotionEvent.ACTION_MASK
		case MotionEvent.ACTION_DOWN:
			// old_x=event.getX();
			old_y = event.getY();
			if (old_y > newtop && old_y < newbottom) {
				isInRect = true;
			}
			if (old_y <= newtop) {
				isInTop = true;
			}
			if (old_y >= newbottom) {
				isInBottom = true;
			}
			// imageProcess.noequl("down", (int) old_y);
			break;
		case MotionEvent.ACTION_MOVE:
			onTouchMove(event);
			break;
		case MotionEvent.ACTION_UP:
			if (isInRect == true) {
				newbottom += offset;
				newtop += offset;
				isInRect = false;
			}
			if (isInTop == true) {
				newtop += offset;
				isInTop = false;
			}
			if (isInBottom == true) {
				newbottom += offset;
				isInBottom = false;
			}
			iv_photo.postInvalidate();
			break;
		}
		return true;
	}

	private void onTouchMove(MotionEvent event) {
		new_x = event.getX();
		new_y = event.getY();
		// imageProcess.noequl("move", (int) new_y);
		offset = (int) (new_y - old_y);
		Bitmap newbitMap = Bitmap.createBitmap(paintbitMap);
		canvas.drawBitmap(newbitMap, matrix, paint);
		rect = canvas.getClipBounds();

		if (isInRect == true) {
			// 判断边界
			if (-offset >= newtop) {
				offset = (-newtop + 2);
			}
			if (offset > height - newbottom) {
				offset = height - newbottom - 1;
			}
			rect.bottom = (newbottom + offset);
			rect.top = (newtop + offset);

		} else if (isInTop == true) {
			if (-offset >= newtop) {
				offset = (-newtop + 2);
			}
			// 判断边界
			if (offset > newbottom - newtop - 30) {
				offset = newbottom - newtop - 30;
			}
			rect.bottom = (int) (newbottom);
			rect.top = (int) (newtop + offset);

		} else if (isInBottom == true) {
			// 判断边界
			if (-offset >= newbottom - newtop - 30) {
				offset = -(newbottom - newtop) + 30;
			}

			if (offset > height - newbottom) {
				offset = height - newbottom - 1;
			}
			rect.bottom = (int) (newbottom + offset);
			rect.top = (int) (newtop);

		}
		canvas.drawRect(rect, paint);
		iv_photo.postInvalidate();

	}
	private final static int SCANNIN_GREQUEST_CODE = 1;
	public void takephoto(View v) {
		// Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Uri imageUri = Uri.fromFile(new
		// File(Environment.getExternalStorageDirectory(),
		// "workupload.jpg"));
		// // 指定照片保存路径（SD卡），workupload.jpg为一个临时文件，每次拍照后这个图片都会被替换
		// cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		// startActivityForResult(cameraIntent, 1);
		SharedPreferences preferences = getSharedPreferences("set", MODE_PRIVATE);
			int count=preferences.getInt("count", 1);
			int sum=preferences.getInt("countSum", 0);
			if(count>sum){
				Toast.makeText(this, "使用次数已过,想继续使用，请购买", Toast.LENGTH_LONG).show();
				return ;
			}else{
				Intent intent = new Intent(this, takePhoto.class);
				startActivity(intent);

			}	

		// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//
		// 调用android自带的照相机
		// startActivityForResult(intent, 1);
	}

	/*
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { // 完成照相后回调用此方法
	 * super.onActivityResult(requestCode, resultCode, data); if (resultCode !=
	 * Activity.RESULT_OK) {// result is not correct return; } //
	 * 将保存在本地的图片取出并缩小后显示在界面上 ? Bitmap bitmap =
	 * BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() +
	 * "/workupload.jpg"); if (null != bitmap) { Map<String, String> params=
	 * service.getPreferences("margain");// new Integer(params.get("margain")
	 * 
	 * try{ int marg= new Integer(params.get("margain")); margain=marg%360;
	 * }catch(Exception e){ margain=0; service.save("0","margain");
	 * imageProcess.noequl("Exception=   ", margain); }
	 * 
	 * 
	 * 
	 * // 下面这两句是对图片按照一定的比例缩放，这样就可以完美地显示出来。 int mScreenWidth =
	 * this.getResources().getDisplayMetrics().widthPixels; //得到屏幕宽度 float scale
	 * = bitmap.getWidth() /(mScreenWidth+50); paintbitMap =
	 * ImageThumbnail.PicZoom(bitmap, bitmap.getWidth() / scale,
	 * bitmap.getHeight() / scale,margain); // paintbitMap =
	 * bitmap.createBitmap(bitmap); imageProcess.noequl("90000000   ", margain);
	 * Bitmap newbitMap = Bitmap.createBitmap(paintbitMap); // 将处理过的图片显示在界面上
	 * height = newbitMap.getHeight(); width = newbitMap.getWidth(); canvas =
	 * new Canvas(newbitMap); paint = new Paint(); paint.setColor(Color.YELLOW);
	 * paint.setStrokeWidth(2); paint.setStyle(Paint.Style.STROKE);
	 * 
	 * newbottom = newbitMap.getHeight() * 3 / 5; newtop = newbitMap.getHeight()
	 * * 2 / 5; canvas.drawBitmap(newbitMap, matrix, paint); rect =
	 * canvas.getClipBounds(); rect.bottom = (int) newbottom; rect.top = (int)
	 * newtop; canvas.drawRect(rect, paint);
	 * iv_photo.setVisibility(View.VISIBLE); iv_photo.setImageBitmap(newbitMap);
	 * iv_photo.setOnTouchListener(this);
	 * 
	 * // 由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常 bitmap.recycle();
	 * 
	 * } }
	 * 
	 * 
	 * public void recognize(View v) { try{ Bitmap screenBitmap = Bitmap
	 * .createBitmap(paintbitMap, 0, newtop, width, newbottom - newtop);
	 * 
	 * imageProcess imageprocess = new imageProcess(); // 转换为灰度图 // Bitmap
	 * bitmap = //
	 * BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() // +
	 * "/workupload.jpg"); Bitmap greyBitmap =
	 * imageprocess.RGBToGrey(screenBitmap); // iv_2.setImageBitmap(greyBitmap);
	 * // iv_2.setImageBitmap(greyBitmap);
	 * 
	 * // Bitmap greyBitmap = imageprocess.greyToArray(bitmap); // 存储灰度图 File
	 * greyFile = new File(Environment.getExternalStorageDirectory(),
	 * "greyTemp.jpg"); if (greyFile.exists()) { greyFile.delete(); }
	 * FileOutputStream fileout = new FileOutputStream(greyFile);
	 * greyBitmap.compress(CompressFormat.JPEG, 100, fileout); fileout.flush();
	 * fileout.close();
	 * 
	 * Intent intent = new Intent(this, processActivity.class);
	 * startActivity(intent); } catch(Exception e){ Toast.makeText(this, "请先拍照",
	 * 1).show(); } }
	 */
	public void setPrefer(View v) {
		Intent intent = new Intent(this, set.class);
		startActivity(intent);
	}

}
