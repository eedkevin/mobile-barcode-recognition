package ll.mbr;

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class CaptureActivity extends Activity implements SurfaceHolder.Callback{
	private final static String TAG = "CaptureActivity";

	private CaptureActivityHandler handler;

	private SurfaceView mSurfaceView;
	private ImageView mImageView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;

	private boolean surfaceRunning;
	private Context context;
	
	private boolean showingResult = false;
	
	private int n=0;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG,"onCreate called:- "+n++);
		super.onCreate(savedInstanceState);

		context = getApplication();
		CameraManager.init(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.main);
		mSurfaceView = (SurfaceView) findViewById(R.id.camera);
		mImageView = (ImageView) findViewById(R.id.image);
		mImageView.setVisibility(View.GONE);

		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}
	
//	@Override
//	protected void onRestart(){
//		Log.v(TAG,"onRestart called: -"+n++);
//		n++;
//		super.onRestart();
//		
//		context = getApplication();
//		CameraManager.init(context);
////		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFormat(PixelFormat.TRANSLUCENT);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		setContentView(R.layout.main);
//		mSurfaceView = (SurfaceView) findViewById(R.id.camera);
//		mImageView = (ImageView) findViewById(R.id.image);
//		mImageView.setVisibility(View.GONE);
//	}

	@Override
	protected void onStart(){
		Log.v(TAG,"onStart called:- "+n++);
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		Log.v(TAG,"onResume called:- "+n++);
		super.onResume();
		
		SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.camera);
		SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
		Log.v(TAG,"surfaceRunning:"+surfaceRunning);
		Log.v(TAG,"mSurfaceHolder:"+mSurfaceHolder);
		Log.v(TAG,"mCamera:"+mCamera);
	    if(surfaceRunning){
	    	initCamera(mSurfaceHolder);
	    }else{
	    	// add the Callback and wait for surfaceCreated()
	    	mSurfaceHolder.addCallback(this);
	    	mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }	    
	}
	
	@Override
	protected void onPause(){
		Log.v(TAG,"onPause called:- "+n++);
		super.onPause();
		if(handler != null){
			handler.cancelRequest();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		Log.v(TAG,"surfaceChanged called:- "+n++);
//		if(!surfaceRunning){
//			surfaceRunning = true;
//			initCamera(holder);
//		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.v(TAG,"surfaceCreated called:- "+n++);
		if(!surfaceRunning){
			surfaceRunning = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.v(TAG,"surfaceDestroyed called:- "+n++);
//		mCamera.stopPreview();
		surfaceRunning = false;
//		mCamera.release();
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
////			if(showingResult){
////				showingResult = false;
////				Log.v(TAG,"surfaceRunning:"+surfaceRunning+",mPreviewRunning:"+mPreviewRunning);
////				setContentView(R.layout.main);
////			    if(surfaceRunning){
////			    	initCamera(mSurfaceHolder);
////			    }
////			    SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.camera);
////			    SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
////			    mSurfaceHolder.addCallback(this);
////			    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
////				CameraManager.get().startPreview();
////				handler.sendEmptyMessage(R.id.restart);
////				return true;				
////			}else{
//				setResult(RESULT_CANCELED);
//				finish();
//				return true;
////			}
//		} else
//			return false;
//	}
			
	private void initCamera(SurfaceHolder holder) {
		try{
			mCamera = CameraManager.get().openDriver(holder);
		}catch(IOException ioe){
			Log.w(TAG,ioe);
			return;
		}
		if(handler==null){
			boolean beginScanning = true;
			handler = new CaptureActivityHandler(this,beginScanning);
		}
	}

	public Handler getHandler() {
		// TODO Auto-generated method stub
		return handler;
	}
	
	public void showResult(Result result){
		
		Intent intent = new Intent(CaptureActivity.this,ResultActivity.class);			
		intent.putExtra("studentId", result.getText());
		startActivity(intent);
		
		showingResult = true;					
	}

}