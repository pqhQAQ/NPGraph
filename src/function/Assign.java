package function;

import java.util.ArrayList;
import java.util.List;

import soot.Local;
import soot.Value;

public class Assign extends Function{
	public List<Local> func(Local rop, Local lop, List<Local> rub){		
		List<Local> list = new ArrayList<Local>();
		if(rop == null)
			return list;
		if(rub.contains(rop)){
			if(rop.getName().equals(lop.getName())){
				list.add(rop);				
			}else{
				list.add(lop);
				list.add(rop);
			}
		}else{
			if(!rop.getName().equals(lop.getName()))
				list.add(rop);							
		}
		return list;
	}
}
