package pegraph.datastructure;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pegraph.PEGGenerator;
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
	
	public void exportIntraGraph(String path){
		//TODO
		String file_path = path + soot_method.getName() + ".txt";
		String regEx="[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try{
			if(!file.exists())
				file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("method: "+soot_method.getDeclaringClass().getName()+"."+soot_method.getName()+":"+soot_method.hashCode()+"\r\n");
			for(CallEdge edge : ceList){
				fileWriter.write(edge.print());
			}
			fileWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void exportMapGraph(String path){
		//TODO
		String file_path = path + soot_method.getName() + "_map.txt";
		String regEx="[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try{
			if(!file.exists())
				file.createNewFile();
			PEGGenerator peg = new PEGGenerator();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("method: "+soot_method.getDeclaringClass().getName()+"."+soot_method.getName()+":"+soot_method.hashCode()+"\r\n");
			for(CallEdge edge : ceList){
				fileWriter.write(peg.mapTable.get(edge.callStr())+" -> "+peg.mapTable.get(edge.receiveStr())+"\r\n");
			}
			fileWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
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
	
	public boolean contains(CallEdge calledge){
		if(ceList.contains(calledge))
			return true;
		return false;
	}

	public CallEdge addCallEdge(CallEdge calledge) {
		// TODO Auto-generated method stub
		ceList.add(calledge);
		return null;
	}	
	
}
