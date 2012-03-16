package ll.mbr;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ResultActivity extends Activity implements SurfaceHolder.Callback{
	private static final String TAG = "ResultActivity";
	
	private static final String SERVER_URL = "http://192.168.0.102:8080/StudentService/"; 

	private Bundle extras;
	private String studentId;
	private Context context;
	private ProgressDialog progressDialog;
	private MBRHttpClient httpClient;
	private StudentDetail sd;
	
	private Handler handler;
	
	// UI components	
	private TextView barcode_TextView;
	private TextView username_Tag;
	private EditText userName_EditText;
	private TextView password_Tag;
	private EditText password_EditText;
	private Button getDetail_Button;
	
	private TextView notFound_TextView;
	
	// UI components: student details
	private TextView firstname_Tag;
	private TextView firstname_TextView;
	private TextView surname_Tag;
	private TextView surname_TextView;
	private TextView gender_Tag;
	private TextView gender_TextView;
	private TextView dateOfBirth_Tag;
	private TextView dateOfBirth_TextView;
	private TextView course_Tag;
	private TextView course_TextView;
	private TextView courseCode_Tag;
	private TextView courseCode_TextView; 
	
//	private boolean synLock = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplication();
		handler = new ResultActivityHandler(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.detail);		
		
		barcode_TextView = (TextView) findViewById(R.id.barcode_TextView);
		username_Tag = (TextView) findViewById(R.id.username_Tag);
		userName_EditText = (EditText) findViewById(R.id.userName_EditText);
		password_Tag = (TextView) findViewById(R.id.password_Tag);
		password_EditText = (EditText) findViewById(R.id.password_EditText);
		
		notFound_TextView = (TextView) findViewById(R.id.notFound_TextView);
		
		// student details
		firstname_Tag =	(TextView) findViewById(R.id.firstname_Tag);
		firstname_TextView = (TextView) findViewById(R.id.firstname_TextView);
		surname_Tag = (TextView) findViewById(R.id.surname_Tag);
		surname_TextView = (TextView) findViewById(R.id.surname_TextView);
		gender_Tag = (TextView) findViewById(R.id.gender_Tag);
		gender_TextView = (TextView) findViewById(R.id.gender_TextView);
		dateOfBirth_Tag = (TextView) findViewById(R.id.dateOfBirth_Tag);
		dateOfBirth_TextView = (TextView) findViewById(R.id.dateOfBirth_TextView);
		course_Tag = (TextView) findViewById(R.id.course_Tag);
		course_TextView = (TextView) findViewById(R.id.course_TextView);
		courseCode_Tag = (TextView) findViewById(R.id.courseCode_Tag);
		courseCode_TextView = (TextView) findViewById(R.id.courseCode_TextView);
		
		// set details invisible when start 
//		firstname_Tag.setVisibility(View.GONE);
//		firstname_TextView.setVisibility(View.GONE);
//		surname_Tag.setVisibility(View.GONE);
//		surname_TextView.setVisibility(View.GONE);
//		gender_Tag.setVisibility(View.GONE);
//		gender_TextView.setVisibility(View.GONE);
//		course_Tag.setVisibility(View.GONE);
//		course_TextView.setVisibility(View.GONE);
//		courseCode_Tag.setVisibility(View.GONE);
//		courseCode_TextView.setVisibility(View.GONE);		
		
		extras = getIntent().getExtras();
		studentId = extras.getString("studentId");
		barcode_TextView.setText(studentId);	
		getDetail_Button = (Button) findViewById(R.id.getDetail_Button);
		getDetail_Button.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String userName = userName_EditText.getText().toString().trim();
				String password = password_EditText.getText().toString().trim();
				Log.v(TAG,"userName:"+userName+",password:"+password);
				if(!userName.equals("") && !password.equals("")){
					doLogin(userName,password);
					Log.v(TAG,"doLogin really finish");
				}else{
					new AlertDialog.Builder(ResultActivity.this).setMessage("Please ensure user name and password are not empty.").setPositiveButton("OK", null).show();
				}
//				if(synLock==true){
//					if(sd.getLoginState().equals("false")){					
//						new AlertDialog.Builder(ResultActivity.this).setMessage("Login failed, Incorrect user name or password.").setPositiveButton("OK", null).show();
//					}else{
//						setDetail(sd);
//					}
//				}
			}
		});
	}	

	@Override
	protected void onResume() {
		super.onResume();
//		Log.v(TAG,"onResume called");
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}
	
	public Handler getHandler(){
		return handler;
	}
	
	private void doLogin(final String userName,final String password){		
		Log.v(TAG,"doLogin");
		progressDialog = ProgressDialog.show(ResultActivity.this, "Hold On a second", "Connecting to Server", true);		
		
		final String url = SERVER_URL + "login.json";
		
		// start a new thread to connect to web server
		new Thread(){
			public void run(){
				HashMap<String,String> paramMap = new HashMap<String,String>();
				paramMap.put("userName", userName);
				paramMap.put("password", password);
				paramMap.put("studentId", studentId);
				Log.v(TAG,"userName:"+paramMap.get("userName")+", password:"+paramMap.get("password")+",student id:"+paramMap.get("studentId"));
				
				sd = MBRHttpClient.doRequest(url, paramMap);
				
				progressDialog.dismiss();				
//				synLock = true;
				
				if(sd.getLoginState().equals("serveroffline")){
					// send message to handler if server off line
					Message message = Message.obtain(handler,R.id.server_offline);
					message.sendToTarget();
				}else if(sd.getLoginState().equals("fail")){
					// send message to handler to deal login fail
					Message message = Message.obtain(handler, R.id.login_fail);
					message.sendToTarget();
				}else{
					// send message to handler to deal setDetail
					Message message = Message.obtain(handler, R.id.set_detail, sd);
					message.sendToTarget();
				}
			}
		}.start();
		Log.v(TAG,"doLogin finished");
	}
	
	public void setLoginFailAlert(){
		new AlertDialog.Builder(ResultActivity.this).setMessage("Login failed,\nuser name and password invalid.").setPositiveButton("OK", null).show();
		userName_EditText.setText("");
		password_EditText.setText("");
	}
	
	public void setServerOfflineAlert(){
		new AlertDialog.Builder(ResultActivity.this).setMessage("Login failed, Server offline.").setPositiveButton("OK", null).show();
		userName_EditText.setText("");
		password_EditText.setText("");
	}
	
	public void setDetail(StudentDetail sd){
		
//		barcode_TextView.setVisibility(View.GONE);
		username_Tag.setVisibility(View.GONE);
		userName_EditText.setVisibility(View.GONE);
		password_Tag.setVisibility(View.GONE);
		password_EditText.setVisibility(View.GONE);
		getDetail_Button.setVisibility(View.GONE);
				
		barcode_TextView.setText(studentId);
		
//		if(sd.getNotFound().equals("true")){
		if(sd.getId().equals("Student Not Found")){
			notFound_TextView.setVisibility(View.VISIBLE);
			notFound_TextView.setText(sd.getId());
		}else{
			firstname_Tag.setVisibility(View.VISIBLE);
			firstname_TextView.setVisibility(View.VISIBLE);
			surname_Tag.setVisibility(View.VISIBLE);
			surname_TextView.setVisibility(View.VISIBLE);
			gender_Tag.setVisibility(View.VISIBLE);
			gender_TextView.setVisibility(View.VISIBLE);
			dateOfBirth_Tag.setVisibility(View.VISIBLE);
			dateOfBirth_TextView.setVisibility(View.VISIBLE);
			course_Tag.setVisibility(View.VISIBLE);
			course_TextView.setVisibility(View.VISIBLE);
			courseCode_Tag.setVisibility(View.VISIBLE);
			courseCode_TextView.setVisibility(View.VISIBLE);	
			
			firstname_TextView.setText(sd.getFirstname());
			surname_TextView.setText(sd.getSurname());
			gender_TextView.setText(sd.getGender());
			dateOfBirth_TextView.setText(sd.getDateOfBirth());
			course_TextView.setText(sd.getCourse());
			courseCode_TextView.setText(sd.getCourseCode());
		}
	}
}
