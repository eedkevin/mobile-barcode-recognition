package ll.mbr;

import java.io.IOException;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public final class CameraManager {
	private final static String TAG = "CameraManager";


	private static CameraManager cameraManager;
	private Camera mCamera;
	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;
	private Handler previewHandler;
	private int previewMessage;
	private Handler autoFocusHandler;
	private int autoFocusMessage;
	private boolean initialized;
	private boolean previewing;
	private int previewFormat;
	private String previewFormatString;
	private boolean useOneShotPreviewCallback;

	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {
		this.context = context;
		mCamera = null;
		initialized = false;
		previewing = false;
		useOneShotPreviewCallback = true;
	}

	private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!useOneShotPreviewCallback) {
				camera.setPreviewCallback(null);
			}
			if (previewHandler != null) {
				Message message = previewHandler.obtainMessage(previewMessage,
						cameraResolution.x, cameraResolution.y, data);
				message.sendToTarget();
				previewHandler = null;
				Log.v(TAG,"preview message sent");
			}
		}
	};

	private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			if (autoFocusHandler != null) {
				Message message = autoFocusHandler.obtainMessage(
						autoFocusMessage, success);
				// Simulate continuous autofocus by sending a focus request every 1.0 seconds.
				autoFocusHandler.sendMessageDelayed(message, 1000L);
				autoFocusHandler = null;
			}
		}
	};

	public Camera openDriver(SurfaceHolder holder) throws IOException {
		if (mCamera == null) {
			mCamera = Camera.open();
			mCamera.setPreviewDisplay(holder);

			if (!initialized) {
				initialized = true;
				getScreenResolution();
			}

			setCameraParameters();
		}
		return mCamera;
	}

	public void closeDriver() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void startPreview() {
		if (mCamera != null && !previewing) {
			Log.v(TAG,"start preview");
			mCamera.startPreview();
			previewing = true;
		}
	}

	public void stopPreview() {
		if (mCamera != null && previewing) {
			if (!useOneShotPreviewCallback) {
				mCamera.setPreviewCallback(null);
			}
			Log.v(TAG,"stop preview");
			mCamera.stopPreview();
			previewHandler = null;
			autoFocusHandler = null;
			previewing = false;
		}
	}

	public void requestPreview(Handler handler, int message) {
		if (mCamera != null && previewing) {
			previewHandler = handler;
			previewMessage = message;

			mCamera.setOneShotPreviewCallback(previewCallback);

		}
	}

	public void requestAutoFocus(Handler handler, int message) {
		if (mCamera != null && previewing) {
			autoFocusHandler = handler;
			autoFocusMessage = message;
			mCamera.autoFocus(autoFocusCallback);
		}
	}

	private void setCameraParameters() {
		Camera.Parameters parameters = mCamera.getParameters();
		Camera.Size size = parameters.getPreviewSize();
		Log.v(TAG, "Default preview size: " + size.width + ", " + size.height);
		previewFormat = parameters.getPreviewFormat();
		previewFormatString = parameters.get("preview-format");
		Log.v(TAG, "Default preview format: " + previewFormat);

		// make sure that the camera resolution is a multiple of 8, as the screen
		// may not be.
		cameraResolution = new Point();
		cameraResolution.x = screenResolution.x;
		cameraResolution.y = screenResolution.y;
		Log.v(TAG, "Setting preview size: " + cameraResolution.x + ", "
				+ cameraResolution.y);
		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);

		parameters.set("flash-mode", "off");

		mCamera.setParameters(parameters);
	}

	private Point getScreenResolution() {
		if (screenResolution == null) {
			WindowManager manager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();
			screenResolution = new Point(display.getWidth(), display
					.getHeight());
		}
		return screenResolution;
	}

}
