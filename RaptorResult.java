public class RaptorResult {
	private static final RaptorResult emptyResult;
	public int arrivalTime;
	public int activeTime;
	
	public static RaptorResult EMPTY_RESULT() {
		if (emptyResult == null) {
			emptyResult = new RaptorResult();
		}
		
		return emptyResult;
	}
	
	public RaptorResult(int arrivalTime, int activeTime) {
		this.arrivalTime = arrivalTime;
		this.activeTime = activeTime;
	}
	
	public RaptorResult() {
		this.arrivalTime = Integer.MAX_VALUE;
		this.activeTime = Integer.MAX_VALUE;
	}
	
}