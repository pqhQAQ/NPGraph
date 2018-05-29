package pegraph.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.ClassConstant;
import soot.jimple.ConcreteRef;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StringConstant;

public class PegIntra {
	
	private SootMethod soot_method;
	//remember to change to private
	public List<CallEdge> ceList = new ArrayList<CallEdge>();
	
	public PegIntra(SootMethod sm){
		this.soot_method = sm;
	}	
	
	public void exportIntraGraph(String file_name){
		//TODO
		
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		//--method signature
		
		//--formal parameters
		
		//--formal return
		
		//--call sites
		
		//--edges
		//local2local: Assign
		
		//obj2local: New
	
		return builder.toString();
	}

	public CallEdge addCallEdge(CallEdge calledge) {
		// TODO Auto-generated method stub
		ceList.add(calledge);
		return null;
	}	
	
}
