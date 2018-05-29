package function;

import java.util.ArrayList;
import java.util.List;

import soot.Local;
import soot.Value;

public class Remove extends Function{
	public List<Local> func(Local rop, Local lop, List<Local> rub){		
		List<Local> list = new ArrayList<Local>();
		if(rop != null && !rop.getName().equals(lop.getName())){
			list.add(rop);
		}
		return list;
	}
}
