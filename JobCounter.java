public class JobCounter {
	private Integer n;
	private Integer max;
	
	public JobCounter(Integer max) {
		n = 0;
		this.max = max;
	}
	
	public void increment() {
		synchronized(n) {
			n++;
		}
	}
	
	public int getN() {
		return n;
	}
	
	public int getPercentOfMax() {
		return (int)java.lang.Math.floor((double) n / (double) max * 100.0);
	}
}