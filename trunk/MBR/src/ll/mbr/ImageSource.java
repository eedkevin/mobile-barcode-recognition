package ll.mbr;

public final class ImageSource{
	private byte[] yuvData;
	private int dataWidth;
	private int dataHeight;
	private int left;
	private int top;

	public ImageSource(byte[] yuvData, int dataWidth, int dataHeight, int left,
			int top, int width, int height) {
		this.yuvData = yuvData;
		this.dataWidth = dataWidth;
		this.dataHeight = dataHeight;
		this.left = left;
		this.top = top;
	}
	
	public ImageSource(byte[] yuvData,int dataWidth, int dataHeight){
		this.yuvData = yuvData;
		this.dataWidth = dataWidth;
		this.dataHeight = dataHeight;
		this.left = 0;	//no use in this case
		this.top = 0;	//no use in this case
	}
	
	public int getWidth(){
		return dataWidth;
	}
	
	public int getHeight(){
		return dataHeight;
	}
	
	public ImageSource getSource(){
		return this;
	}
	
	public byte[] getData(){
		return yuvData;
	}
	
	public byte[] getRow(int rowNum){
		byte[] row = new byte[dataWidth];
		int srcPos = rowNum * dataWidth;
		System.arraycopy(yuvData, srcPos, row, 0, dataWidth);
		return row;
	}
	
//	public Bitmap greyScale(){
//		return null;
//	}
	
	// grey scaling and thresholding are combined in this function
	public int[] doThresholding(int rowNum, int[] row, int width, byte[] data) throws BarcodeException{
		int w = width;	
		int srcPos = rowNum * w;
		byte[] imgData = new byte[w];
		// Copies the number of length elements of the Array data starting at the offset srcPos into the Array row at the position 0.
		System.arraycopy(data, srcPos, imgData, 0, w);  
		
		int[] colourBaskets = new int[256/8];  // divide 256 colours into 32 colour baskets, 32 = 256/8
		// make sure colourBaskets is initialized
		for(int i=0;i < colourBaskets.length;i++){
			colourBaskets[i] = 0;
		}
		
		// the first 2/3 of imgData are grey values
		for(int i=0;i<(w*2/3);i++){
			int pixel = imgData[i] & 0xff; // convert byte to decimal(int), 0xff = 11111111, java doesn't have unsigned type, so need convert to it by hand
			colourBaskets[pixel >> 3]++;  // put the pixel into the relative colour basket			
		}
		// calculate the threshold value
		int thresholdValue = calcThresholdValue(colourBaskets); 
		
		// will use a filter to optimize the colour of pixels
		int leftPixel = imgData[0] & 0xff;
		int middlePixel = imgData[1] & 0xff;		
		for(int i=0;i<(w-1);i++){
			int rightPixel = imgData[i+1] & 0xff;
//			int pixel = ((middlePixel*4) + leftPixel + rightPixel) / 6;
			int pixel = (middlePixel*4 - leftPixel - rightPixel) / 2;
			// if colour of the pixel < criticalBlack, set it black
			if(pixel < thresholdValue){
				row[i] = setBlack(i,row);
			}
			// otherwise, set it white
			else{
				row[i] = setWhite(i,row);
			}
			leftPixel = middlePixel;
			middlePixel = rightPixel;
		}
		return row;
	}
	
	public int setBlack(int i,int[] row){
		return row[i]=0;
	}
	
	public int setWhite(int i,int[] row){
		return row[i]=255;
	}
	
	public int calcThresholdValue(int[] colourBuckets){
		// Find the tallest peak in the colourBuckets
		int numOfBuckets = colourBuckets.length;
		int max = 0; // max bucket count
		int firstPeakIndex = 0;
		int firstPeakSize = 0;
		for(int i=0;i < numOfBuckets;i++){
			if(colourBuckets[i] > firstPeakSize){
				firstPeakIndex = i;
				firstPeakSize = colourBuckets[i];
			}
			if(colourBuckets[i] > max){
				max = colourBuckets[i];
			}
		}
		
		// Find the second tallest peak
		int secondPeakIndex = 0;
		int secondPeakSize = 0;
		int secondPeakScore = 0;
		for(int i=0;i < numOfBuckets;i++){
			int distance = i - firstPeakIndex;
			int score = colourBuckets[i] * distance * distance;
			if(score > secondPeakScore){
				secondPeakIndex = i;
				secondPeakScore = score;
			}
		}		
		
		// Guarantee firstPeak relates to the black peak
		// since colour.black(0 in int) and colour.white(255 in int),
		// black peak always comes first, then white usually comes the last one
		if(firstPeakIndex > secondPeakIndex){
			int temp = secondPeakIndex;
			secondPeakIndex = firstPeakIndex;
			firstPeakIndex = temp;
		}
		
		// Find a suitable threshold value
		int thresholdValue = secondPeakIndex - 1;
		int criticalScore = -1;
		for(int i=secondPeakIndex -1;i > firstPeakIndex;i--){
			int fromFirst = i - firstPeakIndex;
			// to find a index that is closer to the white peak
			// and makes the value of colourBuckets[index] small
			int score = fromFirst * fromFirst * (secondPeakIndex - i) * (max - colourBuckets[i]);
			if(score > criticalScore){
				thresholdValue = i;
				criticalScore = score;
			}
		}
		
		// as we reduce the range by 1/8 times before(in the case of colourBaskets), now we revert it to the original range
		return thresholdValue * 8;
//		return 10 * 8;
	}
	
}
