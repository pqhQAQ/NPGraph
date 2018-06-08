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
	
	public String callStr(){
		return caller.getName()+"("+caller.getHashcode()+")";
	}
	
	public String receiveStr(){
		return receiver.getName()+"("+receiver.getHashcode()+")";
	}
	
	public String print(){
		String path = caller.getName()+"("+caller.getHashcode()+") -> "+receiver.getName()+"("+receiver.getHashcode()+") \r\n";
		return path;
	}
}