package cn.hxc.imgrecognition;

import java.util.Map;

import cn.hxc.imgrecognitionSRI_OCR.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class saveSmg extends Activity {

	 private EditText content;
	 private SharedPreferences preferences;
	   @Override
	protected void onCreate(Bundle savedInstanceState) {
	    // TODO Auto-generated method stub
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.savesms);
	    content=(EditText) findViewById(R.id.content); 
		
	    preferences=getSharedPreferences("set", MODE_PRIVATE); 
//	    if(!preferences.contains("content")){//	    	 
//	    	 Editor editor= preferences.edit();
//		        editor.putString("content", content.getHint().toString().trim());
//		        editor.commit(); 
//	    }
	    content.setText(preferences.getString("content",content.getHint().toString().trim() ));	
	    content.setSelection(content.getText().length());
	}	 
	   public void save(View v){ 
	       String smsContent=content.getText().toString().trim();
	        Editor editor= preferences.edit();
	        editor.putString("content", smsContent);
	        editor.commit(); 
	       Toast.makeText(this, "±£´æ³É¹¦", Toast.LENGTH_LONG).show();
	       Intent intent = new Intent(saveSmg.this,MainActivity.class);
	       startActivity(intent);
	   }
}
