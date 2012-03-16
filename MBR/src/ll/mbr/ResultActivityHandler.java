package ll.mbr;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ResultActivityHandler extends Handler{
	private final static String TAG = "ResultActivityHandler";
	
	private final ResultActivity activity;

	ResultActivityHandler(ResultActivity activity){
		this.activity = activity;
	}
	
	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case R.id.set_detail:
				Log.v(TAG,"set detail");
				activity.setDetail((StudentDetail) message.obj);
				break;
			case R.id.login_fail:
				Log.v(TAG,"login fail");
				activity.setLoginFailAlert();
				break;
			case R.id.server_offline:
				Log.v(TAG,"server offline");
				activity.setServerOfflineAlert();
		}
	}
}
