package model;

import java.util.Map;

public class DominanceTestPair {
	
	Outcome first;
	Outcome second;
	
	public DominanceTestPair(Outcome first, Outcome second) {
		this.first = first;
		this.second = second;
	}
	
	public Outcome getFirst() {
		return first;
	}
	public void setFirst(Outcome first) {
		this.first = first;
	}
	public Outcome getSecond() {
		return second;
	}
	public void setSecond(Outcome second) {
		this.second = second;
	}
	
}
