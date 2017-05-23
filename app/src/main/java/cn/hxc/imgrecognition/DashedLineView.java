package cn.hxc.imgrecognition;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

public class DashedLineView extends View {

	public DashedLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		Paint paint = new Paint();
		 paint.setStyle(Paint.Style.STROKE);
		 paint.setColor(Color.BLUE);//颜色可以自己设置
		 Path path = new Path();
		 path.moveTo(10, 0);//起始坐标
		 path.lineTo(10,500);//终点坐标
		 PathEffect effects = new DashPathEffect(new float[]{8,8,8,8},1);//设置虚线的间隔和点的长度
		 paint.setPathEffect(effects);
		 canvas.drawPath(path, paint);		
	}
	public void drawDashedLine(float x){
		
	}
}
