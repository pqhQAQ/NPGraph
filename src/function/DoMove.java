package function;

import java.util.ArrayList;
import java.util.List;

import soot.Local;
import soot.Value;

public class DoMove extends Function{
	public List<Local> func(Local rop, Local lop, List<Local> rub){		
		List<Local> list = new ArrayList<Local>();
		if(rop != null)
			list.add(rop);
		return list;
	}
}
