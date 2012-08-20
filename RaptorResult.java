public class RaptorResult {
	public int arrivalTime;
	public int activeTime;
	
	public RaptorResult(int arrivalTime, int activeTime) {
		this.arrivalTime = arrivalTime;
		this.activeTime = activeTime;
	}
	
	public RaptorResult() {
		this.arrivalTime = Integer.MAX_VALUE;
		this.activeTime = Integer.MAX_VALUE;
	}
	
}