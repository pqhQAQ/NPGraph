package pegraph.datastructure;

import soot.Local;

public class CallEdge{
	
	//arg could be {Local or StringConstant or ClassConstant}
	//private List<Value> actual_args = new ArrayList<Value>();		
	private Point receiver;
	private Point caller;
	
	public void addReceiver(Point base) {
		// TODO Auto-generated method stub
		this.receiver = base;
	}
	
	public void addCaller(Point arg) {
		// TODO Auto-generated method stub
		this.caller = arg;
	}
	
}