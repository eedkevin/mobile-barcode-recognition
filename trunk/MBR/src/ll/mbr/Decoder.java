package ll.mbr;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class Decoder extends Thread{
	private static final String TAG="Decoder";
	public static final String BARCODE_BITMAP = "barcode_bitmap";
	
	private static final int BLACK = 0;
	private static final int WHITE = 255;
	private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%";	
	private static final int[] ALPHABET_ENCODINGS = {
	    0x034, 0x121, 0x061, 0x160, 0x031, 0x130, 0x070, 0x025, 0x124, 0x064, // 0123456789
	    0x109, 0x049, 0x148, 0x019, 0x118, 0x058, 0x00D, 0x10C, 0x04C, 0x01C, // ABCDEFGHIJ
	    0x103, 0x043, 0x142, 0x013, 0x112, 0x052, 0x007, 0x106, 0x046, 0x016, // KLMNOPQRST
	    0x181, 0x0C1, 0x1C0, 0x091, 0x190, 0x0D0, 0x085, 0x184, 0x0C4, 0x094, // UVWXYZ-. *
	    0x0A8, 0x0A2, 0x08A, 0x02A // $/+%
	};	
	private static final int ASTERISK_ENCODING = 0x094;  //0x02A
	
	static int n = 0;
	static int blackValue;
	
	private Handler handler;
	private final CaptureActivity activity;
	
	Decoder(CaptureActivity activity){
		this.activity=activity;
	}
	
	public Handler getHandler(){
		return handler;
	}
	
	@Override
	public void run(){
		Looper.prepare();
		handler = new Handler(){
			@Override
			public void handleMessage(Message message){
				switch(message.what){
				case R.id.decode:
					if(message.obj!=null) Log.v(TAG,"preview data recieved");
//					if(decode((byte[])message.obj,message.arg1,message.arg2)){
//						Log.v(TAG,"decode succeed");
//						CameraManager.get().stopPreview();
//					}else{
//						CameraManager.get().requestPreviewFrame(handler, R.id.decode);
//					}
					decode((byte[])message.obj,message.arg1,message.arg2);
					break;
				case R.id.quit:
					Looper.myLooper().quit();
					break;
				}
			}
		};
		Looper.loop();
	}

	protected void decode(byte[] data, int width, int height) {
		// TODO Auto-generated method stub
		long start = System.currentTimeMillis();
		boolean succeed;
		Result result = null;
		ImageSource source = new ImageSource(data,width,height);
		try{
		      result = doDecode(source);
		      succeed = true;
		    } catch (BarcodeException be) {
		      succeed = false;
		    }
		    long end = System.currentTimeMillis();
		    
//		    return succeed;

		    if (succeed) {
		    	Log.v(TAG,"decode succeed");
		    	Log.v(TAG, "Found barcode (" + (end - start) + " ms):\n" + result.toString());
		    	CameraManager.get().stopPreview();
		    	Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, result);
//		      	Bundle bundle = new Bundle();
//		      	bundle.putParcelable(BARCODE_BITMAP, source.greyScale());
//		      	message.setData(bundle);
		    	message.sendToTarget();
		    } else {
		    	Message message = Message.obtain(activity.getHandler(), R.id.decode);
		    	message.sendToTarget();
		    }
	}
	
	public Result doDecode(ImageSource source) throws BarcodeException{
		int w = source.getWidth();
		int h = source.getHeight();
		byte[] data = source.getData();
		int[] row = new int[w];
		int middle = h/2;  // the most middle row
		int jump = 30;  // jump 30 rows each time
		int moveStep = 0;  // number of times to move over
		boolean isBelow = false;  // use to determine if is looking below
		
		for(int i=0;i<h;i++){
			// look at the middle row first, then the rows above, and then the rows below
			int rowNum = middle + jump * (isBelow? moveStep: -moveStep);
			// if reach the top, move to the part below
			if(rowNum < 0){
				moveStep = 1;
				isBelow = true;
				rowNum = middle + jump * (isBelow? moveStep: -moveStep);			
			}			
			moveStep++;
			
			// if run out of the image boundary
			if(rowNum<0 || rowNum >= h){ 
				break;
			}
			n=0;
			
			try{
				row = source.doThresholding(rowNum,row,w,data);
			} catch(BarcodeException be){
				continue;
			}
			
			for(int doReverse=0;doReverse <= 1;doReverse++){	
				// determine if will reverse or not
				if(doReverse == 1){
					row = reverse(row);
				}					
				try{												
					// decode row by row to get a barcode
					// if can't decode this row, the function decodeRow will throw a BarcodeException
					String text = decodeRow(row);
					// return null if cannot decode
					Result result = new Result(text,data,rowNum);
					Log.v(TAG,"result:"+result.getText());
					return result;
				} catch(BarcodeException be){
				// do nothing if cannot decode this row, and continue
				}
			}
		}

//		Log.v(TAG,"doDecode exception, will throw BarcodeException");
		throw new BarcodeException();
	}
	
	public int[] reverse(int[] row){
		int temp;
		for(int i=0;i<row.length/2;i++){
			temp = row[i];
			row[i] = row[row.length-1-i];
			row[row.length-1-i] = temp;
		}
		return row;
	}
	
	public String decodeRow(int[] row) throws BarcodeException{
		Log.v(TAG,"decoding row");
		int[] startAsterisk = findAsterisk(row,0);
		
		if(startAsterisk.length==0){
			Log.v(TAG,"didn't find asterisk. will throw BarcodeException");
			throw new BarcodeException();
		}
		
		int next = startAsterisk[1] + 1;
		StringBuffer text = new StringBuffer();
		
		while(next < row.length && row[next]!=BLACK){
			next++;
		}
		
		int[] narrowWide = new int[9];
		char charecter;
		int last;
				
		/*
		 * 
		 * other characters decoding 
		 * 
		 */
		do{
			narrowWide = recordNarrowWide(row,next);
			int pattern = patternToInt(convertToPattern(narrowWide));
			Log.v(TAG,"find alphabet encoding:"+pattern);
			charecter = intToChar(pattern);
			Log.v(TAG,"find alphabet:"+charecter);
			if(charecter != '*'){
				text.append(charecter);
			}
			last = next;
			
			for(int i=0;i < narrowWide.length;i++){
				next += narrowWide[i];
			}
			
			while(next < row.length && row[next]!=BLACK){
				next++;
			}
		}while(charecter != '*');			
		
		return text.toString();
	}
	
	public char intToChar(int pattern) throws BarcodeException{
		for(int i=0;i < ALPHABET_ENCODINGS.length;i++){
			if(pattern == ALPHABET_ENCODINGS[i]){
//				Log.v(TAG,"find alphabet:"+ALPHABET.charAt(i));
				return ALPHABET.charAt(i);
			}
		}
		Log.v(TAG,"patternToChar exception, will throw BarcodeException");
		throw new BarcodeException();
	}
	
	public static int[] recordNarrowWide(int[] row,int offset) throws BarcodeException{
		
		if(offset >= row.length){
			Log.v(TAG,"out of row.length, will throw BarcodeException");
			throw new BarcodeException();
		}
		
		int[] narrowWide = new int[9];
		
		int isWhiteOrBlack = 255 - row[offset]; // implies the colour of next pixel. 0 means is black, 255 means is white
		int index = 0;
		int i = offset;
		for(;i < row.length;i++){
			int pixel = row[i];
			if(pixel!=isWhiteOrBlack){
				narrowWide[index]++;
			}
			else{
				index++;
				if(index == narrowWide.length){
					break;
				} else{
					narrowWide[index] = 1;
					if(isWhiteOrBlack==0){
						isWhiteOrBlack=255;				
					} else isWhiteOrBlack=0;
				}
			}
		}
	    // If we read fully the last section of pixels and filled up our counters -- or filled
	    // the last counter but ran off the side of the image, OK. Otherwise, a problem.
		if(!(index == narrowWide.length || (index == narrowWide.length - 1 && i == row.length))){
			Log.v(TAG,"didn't read fully, will throw BarcodeException");
			throw new BarcodeException();
		}
		StringBuffer sb = new StringBuffer();
		for(int j=0;j < narrowWide.length;j++){
			sb.append(narrowWide[j]);
		}
		Log.v(TAG,"find pattern:"+sb+":"+offset+"-"+i);
		return narrowWide;
	}
	
	public static int[] findAsterisk(int[] row,int offset) throws BarcodeException{
		int w = row.length;
		int barcodeStartAt = offset;

		// find the start index of pixel of barcode hopefully,
		// if not, we can still find a way to get it,
		// by attempting to decode it and throw it away if get meaningless value
		// and move to the next pixel
		// but more expensive in that way.
		for(;barcodeStartAt < w;barcodeStartAt++){
			if(row[barcodeStartAt]==BLACK){
				break;
			}
		}
		
		int index = 0;
		int[] narrowWide = new int[9];
		int asteriskStartAt = barcodeStartAt;
		int isWhiteOrBlack = 255; // implies the colour of next pixel. 0 means is black, 255 means is white
		int len = narrowWide.length;
		
		for(int i=barcodeStartAt;i < w;i++){
			int pixel = row[i];
			// if the current pixel has the same colour as the previous one has
			// then they are a part of the same bar
			if(pixel!=isWhiteOrBlack){
				narrowWide[index]++;
			}
			// otherwise
			else{
				// if reach the length of the narrowWide (9)
				if(index == len - 1){
					try{
						// and if found asterisk
						if(patternToInt(convertToPattern(narrowWide))== ASTERISK_ENCODING){
							Log.v(TAG,"asterisk found,position:"+asteriskStartAt+" - "+i+":"+n);
							n++;
//							CameraManager.get().stopPreview();
							return new int[]{asteriskStartAt,i};
						}
					}catch(BarcodeException be){
						// continue
					}
					// if didn't get asterisk, move the array forward for 2 units
					asteriskStartAt += narrowWide[0] + narrowWide[1];
					for(int j=2;j < len;j++){
						narrowWide[j-2] = narrowWide[j];
					}
					// and set the last two 0, so we can start a new attempt
					narrowWide[len-2] = 0;
					narrowWide[len-1] = 0;
					index--;
				}else{
					index++;
				}
				// the current pixel has a different colour than the previous one has
				// start counting the width of a new bar(the next bar)
				narrowWide[index] = 1;
				// set the colour of the next pixel to the opposite				
				if(isWhiteOrBlack==0){
					isWhiteOrBlack=255;				
				} else isWhiteOrBlack=0;
			}
		}
		// if cannot find the asterisk when reaching the end of the row, throw exception and attempt a new row then
		Log.v(TAG,"find asterisk exception, will throw BarcodeException");
		throw new BarcodeException();
	}
	
	
	public static int[] convertToPattern(int[] narrowWide) throws BarcodeException{
		int len = narrowWide.length;
		int max = 0;  // max width of narrow bars
		int wideNum = 0;  // number of wide bars
		
		do{
			// looking for a suitable value for max value of narrow bars
			int x = Integer.MAX_VALUE;
			for(int i=0;i < len;i++){
				if(narrowWide[i] < x && narrowWide[i] > max){
					x = narrowWide[i];
				}
			}
			max = x;	
			wideNum = 0;
			int[] pattern = new int[9];
			for(int i=0;i < len;i++){
				// set it 1 if > max, otherwise leave it(it was initialized 0)
				// 1 means wide bar, while 0 means narrow bar
				if(narrowWide[i] > max){
					pattern[i] = 1;
					wideNum++;
				}
			}
			
			// if found 3 wide bars
			if(wideNum == 3){
				// if the max width of narrow bars is too big, treat it an error and throw exception
				if(max == Integer.MAX_VALUE){
					Log.v(TAG,"convertToNarrowWide exception, will throw BarcodeException");
					throw new BarcodeException();
				}
				// otherwise, return the found pattern
				return pattern;
			}
		// if wideNum > 3, re-do it with making max a bit bigger
		}while(wideNum > 3);

		// otherwise, this piece of narrowWide is meaningless because it doesn't have 3 wide bars, and throw exception
		Log.v(TAG,"convertToNarrowWide exception, will throw BarcodeException");
		throw new BarcodeException();
	}
	
	public static int patternToInt(int[] pattern){
		int weight = pattern.length-1;
		int x = 0;
		for(int i=0;i < pattern.length;i++){
			// convert array expression to int expression, for easier in comparing
			// e.g. pattern:{0,1,0,1,0,1,0,0,0} -> x:=010101000 (binary)= 168 (decimal)= 0x0A8;
            x += (pattern[i] << weight);
			weight--;
		}
//		Log.v(TAG,"find alphabet encoding:"+x);
		return x;
	}		
	
}
