package model;


public class DominanceTestInstance {
	
	Outcome better;
	Outcome worse;
	
	public DominanceTestInstance(Outcome better, Outcome worse) {
		this.better = better;
		this.worse = worse;
	}
	
	public Outcome getBetter() {
		return better;
	}
	public void setFirst(Outcome better) {
		this.better = better;
	}
	public Outcome getWorse() {
		return worse;
	}
	public void setSecond(Outcome worse) {
		this.worse = worse;
	}
	
}
