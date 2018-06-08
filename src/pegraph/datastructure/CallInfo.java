package pegraph.datastructure;

import java.util.ArrayList;
import java.util.List;

import soot.Local;

public class CallInfo {
	public Point base;
	public List<Point> args = new ArrayList<Point>();
	public List<Point> rargs = new ArrayList<Point>();
	public int receiverHash;
	
	public CallInfo(Point base, List<Point> args, List<Point> rargs, int hashcode){
		this.base = base;
		this.args = args;
		this.rargs = rargs;
		this.receiverHash = hashcode;
	}
}
