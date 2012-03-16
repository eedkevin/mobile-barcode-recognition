package ll.mbr;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CaptureActivityHandler extends Handler {
	private final static String TAG = "CaptureActivityHandler";

	private final CaptureActivity activity;
	private final Decoder decoder;

	CaptureActivityHandler(CaptureActivity activity, boolean beginScanning) {
		this.activity = activity;
		decoder = new Decoder(activity);
		decoder.start();

		CameraManager.get().startPreview();
		if (beginScanning) {
			restartPreview();
		}
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case R.id.auto_focus:
//				Log.v(TAG, "requestAuto_focus");
				CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
				break;
			case R.id.decode:
				//here is the place requesting previewCallback and then send the message to Decoder				
				Log.v(TAG, "requestDecode");
				CameraManager.get().requestPreview(decoder.getHandler(), R.id.decode);				
				break;
			case R.id.decode_succeeded:
				Log.v(TAG,"decode succeeded");
				activity.showResult((Result) message.obj);
			case R.id.restart_preview:
				Log.v(TAG,"restart preview");
				restartPreview();
				break;
		}

	}
	
	public void restartPreview(){
		CameraManager.get().requestPreview(decoder.getHandler(), R.id.decode);
		CameraManager.get().requestAutoFocus(this,R.id.auto_focus);
	}
	
	public void cancelRequest(){
		CameraManager.get().stopPreview();
		this.removeMessages(R.id.decode);
		this.removeMessages(R.id.decode_succeeded);
		Message cancel = Message.obtain(decoder.getHandler(),R.id.quit);
		cancel.sendToTarget();
		try{
			//Blocks the current Thread until the receiver finishes its execution and dies
			decoder.join();
		}catch(InterruptedException e){
			
		}
	}
}
