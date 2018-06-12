package client;

import java.io.File;
import java.util.List;

import pegraph.PEGGenerator;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;

public class IntraGenerator {
	// generator
	public static int Generator_ID = 0;

	public static void main(String[] args) {
		// parse arguments
		// TODO

		String sp = "D:/project/workspace/NPGraph/lib/";
		//String path = sp + "bin" + File.pathSeparator + sp + "lib/soot-2.5.0.jar" + File.pathSeparator + sp
		//		+ "lib/sootsources-trunk.jar" + File.pathSeparator + sp + "lib/rt.jar" + File.pathSeparator + sp
		//		+ "lib/test" + File.pathSeparator + sp + "lib/xmlgraphics-commons-1.3.1.jar";
		File dirFile = new File(sp);
		String path = "";
		if (!dirFile.isDirectory()) {   
            return ;  
        }
		String[] fileList = dirFile.list();
		for (int i = 0; i < fileList.length; i++) {  
            //遍历文件目录  
            String string = fileList[i];  
            if(string.endsWith(".jar")){
            	path  += sp+string+";";
            }
		}
		path += sp+"test;";
		Scene.v().setSootClassPath(path);

		// set options
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().setPhaseOption("tag", "off");
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);

		// add phase
		Transform trans = null;
		switch (Generator_ID) {
		case 0:
			PEGGenerator peggenerator = new PEGGenerator();
			trans = new Transform("jtp.peggenerator", peggenerator);
			break;
		case 1:

			break;
		case 2:
			break;
		default:
			System.err.println("wrong generator!!!");
			System.exit(0);
		}

		PackManager.v().getPack("jtp").add(trans);

		// run
		soot.Main.main(args);
		PEGGenerator peg = new PEGGenerator();
		peg.createCall();
		// List<SootMethod> am = peg.allMethod;
		// for(SootMethod method : am){
		// peg.createIntra();
		// }

	}

}
