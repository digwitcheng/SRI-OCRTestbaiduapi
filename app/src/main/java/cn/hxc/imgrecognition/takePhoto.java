package cn.hxc.imgrecognition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import cn.hxc.imgrecognition.SensorControler.CameraFocusListener;
import cn.hxc.imgrecognitionSRI_OCR.R;

import android.R.string;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.ActionProvider.VisibilityListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class takePhoto extends Activity implements AutoFocusCallback,
		OnTouchListener {
	public static final String TAG = "takePhoto";
	private Button btn_takephoto;
	private Button btn_flash;
	private Button btn_flash_on;
	private SurfaceView surfaceView;
	private View rectView;
	private View MidLine;
	private View VerticalLine;
	private Camera camera;
	private boolean preview;
	private Activity activity;
	// private View nextLayout;
	// private View fisrtLayout;
	private Parameters parameters;
	private int zoomValue;
	private int oldZoomValue;
	// private SensorControler mSensorControler;
	private boolean isFlashon = false;

	private verticalSeekBar seekBar;
	private ImageView seekBar_imageview;
	private int displayOrientation;

	private static int sWidth;
	private static int sHight;
	static boolean isWriteRecogize;

	// 触摸方框
	private float mScreenWidth;
	private float mScreenHeight;
	private int width;
	private int height;

	// 触摸方框
	private float old_x;
	private float old_y;
	private float new_x;
	private float new_y;
	private float old_x1;
	private float old_y1;
	private float new_x1;
	private float new_y1;
	// private boolean isInRect;// 判断手指按下时相对于矩形框的位置
	// private boolean isInTop;
	// private boolean isInBottom;
	private boolean isLeft;
	private boolean isRight;
	private boolean isVerticalMove = false;
	private boolean isDown;
	private Rect rect;
	private Canvas canvas;
	private Paint paint;
	private int margain;
	private tiltImageView tilt;

	// 双手操作
	private int topBord;
	private int leftBord;
	// private int leftBord;
	private int offsetTop;
	private int offsetLeft;

	private AbsoluteLayout.LayoutParams lp;
	// private LinearLayout.LayoutParams lpLeft;
	// private LinearLayout.LayoutParams lpRight;

	private SharedPreferences preferences;
	static final float scaleLeft = (float) 1.0 / 8;
	static final float scaleTop = (float) ((float) 5.0 / 25 - 0.01);
	// private static int zoom;

	private final float minDistance = 10;
	private static final float maxLeftScale = (float) (2.0 / 5);
	private static final int minLeft = 3;
	private static final float maxBottomSacle = (float) (1.0 / 2);
	private static final float minTopSacle = (float) (1.0 / 4);
	private static final int minRectHeight = 20;

	private Rect recognizeRect;
	private Drawable picDrawable;

	private View view_focus = null;
	private PreviewFrameLayout frameLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.takephoto);

		WindowManager wm = (WindowManager) this
				.getSystemService(this.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWidth = outMetrics.widthPixels;
		mScreenHeight = outMetrics.heightPixels;

		height = (int) mScreenHeight;
		width = (int) mScreenWidth;
		rectView = findViewById(R.id.rectView);
		MidLine = findViewById(R.id.MidLine);
		VerticalLine = findViewById(R.id.VerticalLine);
		btn_takephoto = (Button) findViewById(R.id.btn_takephoto);
		btn_flash = (Button) findViewById(R.id.flash_btn);
		btn_flash_on = (Button) findViewById(R.id.flash_btn_on);
		// tilt=(tiltImageView) findViewById(R.id.tilt);
		frameLayout = (PreviewFrameLayout) findViewById(R.id.frame_layout);
		// frameLayout.setOnTouchListener(l);
		view_focus = findViewById(R.id.view_focus);

		imageProcess.noequl(scaleLeft + "/", 0);
		imageProcess.noequl(scaleTop + "/", 0);

		preferences = getSharedPreferences("set", MODE_PRIVATE);
		if (!preferences.contains("positionLeft")) {
			savePosition("positionLeft", scaleLeft);
			if (mScreenHeight < 600) {
				savePosition("positionTop", scaleTop - (float) 0.01);
			}
			if (mScreenHeight > 1600) {
				savePosition("positionTop", scaleTop + (float) 0.01);
			}

		}
		if (!preferences.contains("resultCount")) {
			savePosition("resultCount", 1);
		}
		// imageProcess.noequl("positionLeft=", getBottomPosition());
		// mSensorControler = new SensorControler(this);
		initTakephoto();
	}

	void savePosition(String string, float scale) {
		Editor editor = preferences.edit();
		editor.putFloat(string, scale);
		editor.commit();
	}

	float getLeftPosition() {
		return preferences.getFloat("positionLeft", scaleLeft);
	}

	float getBottomPosition() {
		return preferences.getFloat("positionTop", scaleTop);
	}

	int px(float pix) {

		return (int) ((pix + 0.5) * 1.5);
	}

	public void initTakephoto() {
		paint = new Paint();
		paint.setColor(Color.YELLOW);
		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.STROKE);
		paint.setPathEffect(new DashPathEffect(new float[] { 3, 2 }, 0));
		canvas = new Canvas();
		Path path = new Path();
		path.moveTo(10, 10);
		path.lineTo(10, 400);
		canvas.drawPath(path, paint);

		leftBord = (int) (getLeftPosition() * mScreenWidth);
		// rightBord = (int) (getRightPosition() * mScreenWidth);
		topBord = (int) (getBottomPosition() * mScreenHeight);

		// leftBord = width / 6;
		// topBord = (int) (height * minTopSacle - height / 24);
		imageProcess.noequl("getBottomPosition=", getBottomPosition());

		lp = new AbsoluteLayout.LayoutParams(width - 2 * leftBord,
				(int) ((height * minTopSacle - topBord) * 2), leftBord, topBord);
		// lp.setMargins(leftBord, topBord, 0, 0);
		rectView.setLayoutParams(lp);
		AbsoluteLayout.LayoutParams lpMid = new AbsoluteLayout.LayoutParams(
				(width - 2 * leftBord) / 2, 4, leftBord
						+ (width - 2 * leftBord) / 4,
				(int) (height * minTopSacle));
		MidLine.setLayoutParams(lpMid);

		AbsoluteLayout.LayoutParams lpVertical = new AbsoluteLayout.LayoutParams(
				4, 20, width / 2, (int) (height * minTopSacle - 20 / 2));
		VerticalLine.setLayoutParams(lpVertical);

		recognizeRect = new Rect(leftBord, topBord, width - leftBord, (int) (2
				* minTopSacle * height - topBord));

		// RectF rectF=new
		// RectF(rectView.getLeft(),rectView.getTop(),rectView.getRight(),rectView.getBottom());
		// tilt.rectF=rectF;
		// tilt.setBackgroundResource(R.drawable.tilt);

		// rectView.setLayoutParams(new
		// LinearLayout.LayoutParams((int)mScreenWidth, (int)mScreenHeight/10));
		isWriteRecogize = false;
		// (paintbitMap, 0, newtop, screenWidth, newbottom - newtop);
		activity = this;
		surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		// nextLayout = this.findViewById(R.id.nextlayout);
		// fisrtLayout = this.findViewById(R.id.firstLayout);
		/* 下面设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到用户面前 */
		surfaceView.getHolder()
				.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// surfaceView.getHolder().setFixedSize(176, 144); // 设置分辨率
		surfaceView.setFocusable(true);
		surfaceView.getHolder().setKeepScreenOn(true);
		surfaceView.getHolder().addCallback(new SufaceListener());// 添加监听
		// 进度条初始化设置
		// seekBar_imageview = (ImageView)
		// findViewById(R.id.seekbar_imageview_id);
		// seekBar = (verticalSeekBar) findViewById(R.id.seekbar_btn_id);
		// seekBar.setMax(100);
		// seekBar.setOnSeekBarChangeListener(new onseekBarListener());

		// mSensorControler.setmCameraFocusListener(new CameraFocusListener() {
		// @Override
		// public void onFocus() {
		// onMyFoucs();
		// }
		// });
		// mSensorControler.onStart();		
		
		
		surfaceView.setOnTouchListener(this);
		rectView.setOnTouchListener(this);

	}

	private final class SufaceListener implements SurfaceHolder.Callback {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// // 实现自动对焦
			// mSensorControler.onStart();
			// onMyFoucs();
			// // imageProcess.noequl("@@@@@@@@@", 1);
			// camera.startPreview();

		}

		@SuppressWarnings("deprecation")
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera = Camera.open();// 打开摄像头
				initCamera();
				// 启动预览
				try {
					camera.setPreviewDisplay(holder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("camera", "preview failed!");
					e.printStackTrace();
				}
				// nextLayout.setVisibility(ViewGroup.GONE);
				// fisrtLayout.setVisibility(ViewGroup.VISIBLE);
				camera.startPreview();
				
				preview = true;
				 onMyFoucs();

			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				if (preview)
					camera.stopPreview();
				// mSensorControler.onStop();
				camera.release();
				camera = null;
			}
		}
	}

	// private AutoFocusCallback autoCallback= new AutoFocusCallback() {
	// @Override
	// public void onAutoFocus(boolean success, final Camera camera) {
	//
	// }
	//
	// };

	public void onMyFoucs() {
		if (camera != null&&preview==true) {
			camera.autoFocus(this);
		}
		
	}

	@Override
	public void onAutoFocus(boolean success, final Camera camera) {
		// TODO Auto-generated method stub
		if (success) {
			// initCamera();// 实现相机的参数初始化
			// camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
			if (parameters.getSupportedFocusModes().contains(
					Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {

				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对
			}
			try {
				camera.setParameters(parameters);
			}catch (Exception e){

			}

			if(Build.VERSION.SDK_INT>=14) {
				int tempWidth = view_focus.getWidth();
				int tempHeight = view_focus.getHeight();
				view_focus.setX(mScreenWidth / 2 - (tempWidth / 2));
				view_focus.setY(mScreenHeight / 4 - (tempHeight / 2));

			}
			view_focus.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.ic_focus_focused));

		}
		else {
			view_focus.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.ic_focus_focusing));
		}		
		//setFocusedView();
		setFocusViewNull();
		
	}
	//隐藏聚焦框
	 private void setFocusViewNull() {
		    new Handler().postDelayed(new Runnable() {

		      @SuppressWarnings("deprecation")
		      @Override
		      public void run() {
		        view_focus.setBackgroundDrawable(null);

		      }
		    }, 1 * 1000);
		  }
	//跟换聚焦框
		 private void setFocusedView() {
			    new Handler().postDelayed(new Runnable() {

			      @SuppressWarnings("deprecation")
			      @Override
			      public void run() {
			    	  view_focus.setBackgroundDrawable(getResources().getDrawable(
			  				R.drawable.ic_focus_focused));

			      }
			    }, 1 * 1000);
			  }

	/* *
	 * 是否开启了闪光灯
	 * 
	 * @return
	 */
	public boolean isFlashlightOn() {
		try {
			Camera.Parameters parameters = camera.getParameters();
			String flashMode = parameters.getFlashMode();
			if (flashMode
					.equals(android.hardware.Camera.Parameters.FLASH_MODE_TORCH)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	// 相机参数的初始化设置
	private void initCamera() {
		try {

			btn_takephoto.setEnabled(true);
			parameters = camera.getParameters();// 摄像头的参数
			// List<int[]> range=parameters.getSupportedPreviewFpsRange();
			// parameters.setPreviewFrameRate(20);// 每秒20帧
			// List<Integer> formerate= parameters.getSupportedPreviewFormats();
			// parameters.setPreviewFormat(formerate.get(formerate.size()/2));
			if (camera.getParameters().isZoomSupported()) {
				int zoom = camera.getParameters().getMaxZoom();
				parameters.setZoom(zoom / 3);
				imageProcess.noequl("zoom---------", zoom);
			}
			List<String> flashModes = parameters.getSupportedFlashModes();
			// Check if camera flash exists
			if (flashModes == null) {
				// Use the screen as a flashlight (next best thing)
				return;
			}
			String flashMode = parameters.getFlashMode();
			if (isFlashon == true) {
				if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
					// Turn on the flash
					if (flashModes.contains(Parameters.FLASH_MODE_TORCH))
						parameters
								.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				}
				// Toast.makeText(this, "opened", 1).show();
			} else {
				if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
					// Turn on the flash
					if (flashModes.contains(Parameters.FLASH_MODE_OFF))
						parameters
								.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				}
			}

			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE); // 获取当前屏幕管理器对象
			Display display = wm.getDefaultDisplay(); // 获取屏幕信息的描述类
			// parameters.setPreviewSize(display.getWidth(),
			// display.getHeight());
			// // 设置

			List<Size> SupportedPictureSizes= parameters
					.getSupportedPictureSizes();// 获取支持预览照片的尺寸
//			List<Size> SupportedPictureSizes = parameters
//					.getSupportedPreviewSizes();// 获取支持预览照片的尺寸
			// Size previewSize = SupportedPreviewSizes.get(0);// 从List取出Size
			// sWidth = previewSize.width;
			// sHight = previewSize.height;
//			Size previewSize = getOptimalPreviewSize(SupportedPictureSizes,
//					display.getWidth(), display.getHeight());

//			Size previewSize = CameraUtil.getInstance().getPictureSize(SupportedPictureSizes,400);
//			sWidth = previewSize.width;
//			sHight = previewSize.height;

			Size picSize = CameraUtil.getInstance().getPictureSize(parameters
					.getSupportedPictureSizes(),800);
//
//			imageProcess.noequl("**********sWidth=", sWidth);
//			imageProcess.noequl("*********sHight=", sHight);
//			imageProcess.noequl("", mScreenHeight);
//			// Toast.makeText(takePhoto.this, ""+mScreenHeight, 1).show();
			//parameters.setPreviewSize(sWidth, sHight);
		//	parameters.setPictureSize(sWidth, sHight);
			parameters.setPictureSize(picSize.width, picSize.height);

			// 竖屏正常预览
			setCameraDisplayOrientation(activity,
					Camera.CameraInfo.CAMERA_FACING_BACK, camera);
			
			focosTouchRect();
			camera.cancelAutoFocus();
			camera.setParameters(parameters);

		} catch (Exception e) {
		}

	}

	public void focosTouchRect() {//MotionEvent event
		int[] location = new int[2];
		frameLayout.getLocationOnScreen(location);
		//聚焦框显示在触摸的地方
//		Rect focusRect = calculateTapArea(view_focus.getWidth(),
//				view_focus.getHeight(), 1f, event.getRawX(), event.getRawY(),
//				location[0], location[0] + frameLayout.getWidth(), location[1],
//				location[1] + frameLayout.getHeight());
//		Rect meteringRect = calculateTapArea(view_focus.getWidth(),
//				view_focus.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
//				location[0], location[0] + frameLayout.getWidth(), location[1],
//				location[1] + frameLayout.getHeight());
		//聚焦框显示在固定点（1/4*mscreenheight,1/2*mscreenwidth）
		Rect focusRect = calculateTapArea(view_focus.getWidth(),
				view_focus.getHeight(), 1f, mScreenWidth/2, mScreenHeight/4,
				location[0], location[0] + frameLayout.getWidth(), location[1],
				location[1] + frameLayout.getHeight());
		Rect meteringRect = calculateTapArea(view_focus.getWidth(),
				view_focus.getHeight(), 1.5f, mScreenWidth/2, mScreenHeight/4,
				location[0], location[0] + frameLayout.getWidth(), location[1],
				location[1] + frameLayout.getHeight());
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		// System.out.println("CustomCameraView getMaxNumFocusAreas = " +
		// parameters.getMaxNumFocusAreas());

		if(Build.VERSION.SDK_INT>=14) {
			if (parameters.getMaxNumFocusAreas() > 0) {
				List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
				focusAreas.add(new Camera.Area(focusRect, 1000));

				parameters.setFocusAreas(focusAreas);
			}

			// System.out.println("CustomCameraView getMaxNumMeteringAreas = " +
			// parameters.getMaxNumMeteringAreas());
			if (parameters.getMaxNumMeteringAreas() > 0) {
				List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
				meteringAreas.add(new Camera.Area(meteringRect, 1000));

				parameters.setMeteringAreas(meteringAreas);
			}
		}

		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
		}
		//onMyFoucs();
		onMyFoucs();
	}

	/**
	 * 计算焦点及测光区域
	 * 
	 * @param focusWidth
	 * @param focusHeight
	 * @param areaMultiple
	 * @param x
	 * @param y
	 * @param previewleft
	 * @param previewRight
	 * @param previewTop
	 * @param previewBottom
	 * @return Rect(left,top,right,bottom) : left、top、right、bottom是以显示区域中心为原点的坐标
	 */
	public Rect calculateTapArea(int focusWidth, int focusHeight,
			float areaMultiple, float x, float y, int previewleft,
			int previewRight, int previewTop, int previewBottom) {
		int areaWidth = (int) (focusWidth * areaMultiple);
		int areaHeight = (int) (focusHeight * areaMultiple);
		int centerX = (previewleft + previewRight) / 2;
		int centerY = (previewTop + previewBottom) / 2;
		double unitx = ((double) previewRight - (double) previewleft) / 2000;
		double unity = ((double) previewBottom - (double) previewTop) / 2000;
		int left = clamp((int) (((x - areaWidth / 2) - centerX) / unitx),
				-1000, 1000);
		int top = clamp((int) (((y - areaHeight / 2) - centerY) / unity),
				-1000, 1000);
		int right = clamp((int) (left + areaWidth / unitx), -1000, 1000);
		int bottom = clamp((int) (top + areaHeight / unity), -1000, 1000);

		return new Rect(left, top, right, bottom);
	}

	public int clamp(int x, int min, int max) {
		if (x > max)
			return max;
		if (x < min)
			return min;
		return x;
	}


	public void flash(View v) {
		try {
			if (isFlashon == false) {
				isFlashon = true;
				btn_flash.setVisibility(View.GONE);
				btn_flash_on.setVisibility(View.VISIBLE);

			} else {
				isFlashon = false;
				// btn_flash.setBackground(getResources().getDrawable(
				// R.drawable.flash_off));
				// btn_flash.setBackground(picDrawable);
				btn_flash.setVisibility(View.VISIBLE);
				btn_flash_on.setVisibility(View.GONE);
			}
			initCamera();
		} catch (Exception e) {
			Toast.makeText(this, "闪关灯不可用", Toast.LENGTH_LONG).show();
		}

	}

	public void takepicture(View v) {
		// nextLayout.setVisibility(ViewGroup.VISIBLE);
		// fisrtLayout.setVisibility(ViewGroup.GONE);
		btn_takephoto.setEnabled(false);
		if (camera != null) {// 拍照前判断，照相机对象不允许为null
			try {
				camera.takePicture(null, null, mJpegPictureCallback);

			} catch (Exception e) {
				System.out.println("takepicture failed...");
			}
		}
		// camera.takePicture(null, null,null,
		// mJpegPictureCallback);
		// camera.startPreview();//拍完照调用这个方法继续预览，这样是不可以的
		// camera.takePicture这个方法内部采用了异步保存照片,当camera.takePicture方法执行完后，摄像头可能还没有处理完照片
		// 所以直接在camera.takePicture方法后面调用camera.startPreview();方法是不对的
		// 这个方法开始拍照，这三个参数是：快门按下去后
		/*
		 * null,第一个当快门按下去之后，会调用这个对象中的回调方法
		 * null，第二个参数是当摄像头拍完后，照片数据分为照片的原始数据，和照片经过压缩后的数据，第二个参数就是指照片的原始数据
		 * 第三个参数就是照片经过压缩后的数据 new MyPictureCallback()这个方法就是得到摄像头拍摄完，并压缩后的数据
		 */
		// 摄像头拍照的时候是不可以预览的，因为摄像头在某一个时刻只能做一件事情，当拍完照后就没有预览画面了。

	}
	// public void comeBack(View v) {
	// nextLayout.setVisibility(ViewGroup.GONE);
	// fisrtLayout.setVisibility(ViewGroup.VISIBLE);
	// camera.startPreview();
	// }
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
		case KeyEvent.KEYCODE_5:
			takepicture(new View(this));
			break;
		case KeyEvent.KEYCODE_BACK:
			back(new View(this));
			break;
		}

		return true;

	};

	PictureCallback mJpegPictureCallback = new PictureCallback() {

		// Skipped 47 frames! The application may be doing too much work on its
		// main thread.
		@Override
		public void onPictureTaken(byte[] data, Camera arg1) {
			// TODO Auto-generated method stub
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length,options);

			BitmapFactory.Options opts=getOption(options);
			Bitmap bitmap1= BitmapFactory
					.decodeByteArray(data, 0, data.length,opts);
			preview = true;

			Size size1=camera.getParameters().getPreviewSize();
			Size size2=camera.getParameters().getPictureSize();

			if (bitmap1 != null) {

				FileOutputStream fos;
				Matrix matrix = new Matrix();
				matrix.postRotate(displayOrientation);
				Bitmap bitmap = Bitmap.createBitmap(bitmap1, 0, 0,
						bitmap1.getWidth(), bitmap1.getHeight(), matrix, false);

				float scaleX = bitmap.getWidth() / mScreenWidth;
				float scaleY = bitmap.getHeight() / mScreenHeight;
				Bitmap rotaBitmap = Bitmap.createBitmap(bitmap,
						(int) (scaleX * recognizeRect.left),
						(int) (recognizeRect.top * scaleY),
						(int) (recognizeRect.width() * scaleX),
						(int) (recognizeRect.height() * scaleY));
				try {
					File file = new File(
							Environment.getExternalStorageDirectory()
									+ File.separator + "KuaiDiBangShou");
					if (!file.exists()) {
						file.mkdirs();
					}
					fos = new FileOutputStream(file + File.separator
							+ "greyTemp.jpg");
					rotaBitmap.compress(CompressFormat.JPEG, 85, fos);
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				bitmap.recycle();
				bitmap=null;
				bitmap1.recycle();
				bitmap1=null;

				Intent intent = new Intent(takePhoto.this,
						processActivity.class);
				startActivity(intent);


			} else {
				// 重新浏览
				camera.stopPreview();
				camera.startPreview();
				btn_takephoto.setEnabled(true);

			}


		}


	};
public BitmapFactory.Options getOption(BitmapFactory.Options opts){
		//2.为位图设置100K的缓存
		//BitmapFactory.Options opts=new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
//4.设置图片可以被回收，创建Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
		opts.inPurgeable = true;
//5.设置位图缩放比例
//width，hight设为原来的四分一（该参数请使用2的整数倍）,这也减小了位图占用的内存大小；例如，一张//分辨率为2048*1536px的图像使用inSampleSize值为4的设置来解码，产生的Bitmap大小约为//512*384px。相较于完整图片占用12M的内存，这种方式只需0.75M内存(假设Bitmap配置为//ARGB_8888)。

	Size size1=camera.getParameters().getPreviewSize();
//	 opts.outHeight=size1.height;
//	 opts.outWidth=size1.width;

	opts.inSampleSize =(int) (opts.outHeight/mScreenHeight);
//6.设置解码位图的尺寸信息
		opts.inInputShareable = true;
		opts.inJustDecodeBounds=false;
		return  opts;
}
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;
		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void setCameraDisplayOrientation(Activity activity, int cameraId,
			android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;// compensate the mirror
		} else {// back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		displayOrientation = result;
		camera.setDisplayOrientation(result);
	}

	/** 记录是拖拉照片模式还是放大缩小照片模式 */

	private static final int MODE_INIT = 0;
	/** 放大缩小照片模式 */
	private static final int MODE_POINTER = 1;
	private int mode = MODE_INIT;// 初始状态

	/** 用于记录拖拉图片移动的坐标位置 */

	private float startDis;
	private float endDis;

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub

		/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 手指压下屏幕
		case MotionEvent.ACTION_DOWN:
			mode = MODE_INIT;
			// mSensorControler.onStop();
			isDown = true;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			old_x = event.getX(0);
			old_y = event.getY(0);
			old_x1 = event.getX(1);
			old_y1 = event.getY(1);

			// 移除token对象为mZoomSeekBar的延时任务
			// mZoomSeekBar.setVisibility(View.VISIBLE);
			mode = MODE_POINTER;
			/** 计算两个手指间的距离 */
			startDis = distance(event);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == MODE_POINTER) {
				// 只有同时触屏两个点的时候才执行
				if (event.getPointerCount() < 2) {
					return true;
				}
				onTouchMove(event);

				// startDis=endDis;
			}
			break;
		// 手指离开屏幕
		case MotionEvent.ACTION_UP:
			if (mode == MODE_POINTER) {
				topBord -= offsetTop / 2;
				imageProcess.noequl("topBord b　=", topBord);
				leftBord -= offsetLeft / 2;

			} else {
//				int tempWidth = view_focus.getWidth();
//				int tempHeight = view_focus.getHeight();
				view_focus.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.ic_focus_focusing));
////				view_focus.setX((event.getRawX() - (tempWidth / 2)));
////				view_focus.setY((event.getRawY() - (tempHeight / 2)));
//				view_focus.setX(mScreenWidth/2- (tempWidth / 2));
//				view_focus.setY(mScreenHeight/4- (tempHeight / 2));
				//focosTouchRect(event);
				focosTouchRect();
			}
			savePosition("positionLeft", (float) leftBord / mScreenWidth);
			savePosition("positionTop", (float) topBord / mScreenHeight);

			break;
		}

		return true;
	}

	private void onTouchMove(MotionEvent event) {

		endDis = distance(event);
		// if ((endDis - startDis)*(endDis - startDis) > 8) {
		// new_x = event.getX(0);
		// new_y = event.getY(0);
		// new_x1 = event.getX(1);
		// new_y1 = event.getY(1);
		float distance_PRENTER_y = (float) Math.sqrt((old_y1 - old_y)
				* (old_y1 - old_y));
		float distance_PRENTER_x = (float) Math.sqrt((old_x1 - old_x)
				* (old_x1 - old_x));
		// if(distance(event) > minDistance) {
		if (isDown == true) {
			if (distance_PRENTER_y > distance_PRENTER_x) {
				isVerticalMove = true;
			} else {
				isVerticalMove = false;
			}
			isDown = false;
		}
		if (isVerticalMove) {
			offsetLeft = 0;
			offsetTop = (int) ((endDis - startDis) / 2);
			if (offsetTop / 2 > topBord - height / 8) {
				offsetTop = (topBord - height / 8) * 2;
			}
			if (-offsetTop / 2 > -topBord + height * minTopSacle - 15) {
				offsetTop = (int) (topBord - height * minTopSacle + 15) * 2;
			}

		} else {
			offsetTop = 0;
			offsetLeft = (int) ((endDis - startDis) / 2);
			if (leftBord - offsetLeft / 2 < 5) {
				offsetLeft = (leftBord - 5) * 2;
			}
			if (leftBord - offsetLeft / 2 > 2 * width / 5) {
				offsetLeft = (leftBord - 2 * width / 5) * 2;
			}

		}

		// (int) mScreenHeight / 4 + (int) mScreenHeight / 12
		lp = new AbsoluteLayout.LayoutParams(width - 2 * leftBord + offsetLeft,
				(int) ((height * minTopSacle - topBord) * 2 + offsetTop),
				leftBord - offsetLeft / 2, topBord - offsetTop / 2);
		// lp.setMargins(leftBord - offsetLeft / 2, topBord - offsetTop / 2, 0,
		// 0);
		rectView.setLayoutParams(lp);		
	}

	// imageProcess.noequl("width=",width-2*newLeftBord );
	// imageProcess.noequl("height=", (int)
	// ((height*minTopSacle-topBord)*2+offsetTop));
	// imageProcess.noequl("disTop", topBord-offsetTop/2);
	// }

	// }

	/** 计算两个手指间的距离 */
	private float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		/** 使用勾股定理返回两点之间的距离 */
		return (float) Math.sqrt(dx * dx + dy * dy);
	}
	public void back(View v){
		this.finish();
		super.onBackPressed();
		destoryView();
	}
	void destoryView(){
		if(rect!=null){
			rect=null;
		}
		if(rectView!=null){
			rectView=null;
		}
	}


}
