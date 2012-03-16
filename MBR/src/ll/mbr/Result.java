package ll.mbr;

public final class Result {
	private final String text;
	private final byte[] rawData;
	private final int rowNum;
	
	public Result(String text,byte[] rawData,int rowNum){
		this.text=text;
		this.rawData=rawData;
		this.rowNum=rowNum;
	}
	
	public String getText(){
		return text;
	}
	
	public byte[] getData(){
		return rawData;
	}
	
	public int getRowNum(){
		return rowNum;
	}
	
	public byte[] getRow(){
		byte[] row = new byte[rawData.length];
		int srcPos = getRowNum() * rawData.length;
		System.arraycopy(rawData, srcPos, row, 0, rawData.length);
		return row;
	}
	
	public Result getResult(){
		return this;
	}
}
