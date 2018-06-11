package pegraph;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pegraph.datastructure.PegIntra;
import pegraph.datastructure.CallEdge;
import pegraph.datastructure.CallInfo;
import pegraph.datastructure.Point;
import function.*;
import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UnopExpr;
import soot.util.Chain;

public class PEGGenerator extends BodyTransformer {

	private SootMethod sm;
	private boolean call;
	private int callhash;
	private PegIntra intra_graph;
	private static List<CallEdge> inter_graph = new ArrayList<CallEdge>();
	private List<Local> objectlocal = new ArrayList<Local>();
	private static Map<Integer, List<Local>> recordReturn = new HashMap<Integer, List<Local>>();
	private static List<SootMethod> allMethod = new ArrayList<SootMethod>();
	private static Map<Integer, List<Local>> methodPar = new HashMap<Integer, List<Local>>();
	private static List<CallInfo> callInfoList = new ArrayList<CallInfo>();
	public static Map<String, Long> mapTable = new HashMap<String, Long>();

	@Override
	protected void internalTransform(Body arg0, String arg1, Map arg2) {
		// TODO Auto-generated method stub
		sm = arg0.getMethod();
		intra_graph = new PegIntra(sm);

		if (!sm.hasActiveBody()) {
			sm.retrieveActiveBody();
		}

		allMethod.add(sm);
		List<Local> par = getPar();
		if (!par.isEmpty())
			methodPar.put(sm.hashCode(), par);
		String file_path = "D:/project/workspace/NPGraph/sootOutput/" + sm.getDeclaringClass().getName() + "_"
				+ sm.getName() + "_jimple.txt";
		String regEx = "[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(
					"method: " + sm.getDeclaringClass().getName() + "." + sm.getName() + ":" + sm.hashCode() + "\r\n");
			call = false;
			initEntry();
			// first of all, flow edges are added by inspecting the statements
			// in the
			// method one by one
			Stmt nowst, succst;
			Iterator stmts = sm.getActiveBody().getUnits().iterator();
			if (stmts.hasNext()) {
				nowst = (Stmt) stmts.next();
				succst = nowst;
				for (; stmts.hasNext();) {
					succst = (Stmt) stmts.next();
					fileWriter.write(nowst.hashCode() + ":" + nowst.toString() + "\r\n");
					processStmt(nowst, succst);
					nowst = succst;
				}
				fileWriter.write(succst.hashCode() + ":" + succst.toString() + "\r\n");
				processStmt(succst, succst);
			}
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// createCall();
		for (CallEdge ce : intra_graph.ceList) {
			if (!mapTable.containsKey(ce.callStr()))
				mapTable.put(ce.callStr(), (long) mapTable.size());
			if (!mapTable.containsKey(ce.receiveStr()))
				mapTable.put(ce.receiveStr(), (long) mapTable.size());
		}
		intra_graph.exportMapGraph("D:/project/workspace/NPGraph/sootOutput/");
		intra_graph.exportIntraGraph("D:/project/workspace/NPGraph/sootOutput/");
	}

	public boolean callContains(int hashcode) {
		for (SootMethod m : allMethod) {
			if (m.hashCode() == hashcode)
				return true;
		}
		return false;
	}

	private void printCall(String path) {
		String file_path = path + "CallSite.txt";
		String regEx = "[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			for (CallEdge edge : inter_graph) {
				fileWriter.write(edge.print());
			}
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printMapCall(String path) {
		String file_path = path + "MapCallSite.txt";
		String regEx = "[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			for (CallEdge edge : inter_graph) {
				if (!mapTable.containsKey(edge.callStr()))
					mapTable.put(edge.callStr(), (long) mapTable.size());
				if (!mapTable.containsKey(edge.receiveStr()))
					mapTable.put(edge.receiveStr(), (long) mapTable.size());
				fileWriter.write(mapTable.get(edge.callStr()) + " -> " + mapTable.get(edge.receiveStr()) + "\r\n");
			}
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printMap(String path) {
		String file_path = path + "MapTable.txt";
		String regEx = "[`~!@#$%^&*()+=|{}';',\\[\\]<>?~£¡@#£¤%¡­¡­&*£¨£©¡ª¡ª+|{}¡¾¡¿¡®£»£º¡±¡°¡¯¡££¬¡¢£¿]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(file_path);
		file_path = m.replaceAll("").trim();
		File file = new File(file_path);
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			for (Map.Entry<String, Long> entry : mapTable.entrySet()) {
				fileWriter.write(entry.getKey() + " <-> " + entry.getValue() + "\r\n");
			}
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createCall() {
		for (CallInfo callInfo : callInfoList) {
			if (callContains(callInfo.receiverHash)) {
				Point base = callInfo.base;
				int receiverhash = callInfo.receiverHash;
				CallEdge calledge0 = new CallEdge();
				calledge0.addCaller(new Point("0", base.getHashcode()));
				calledge0.addReceiver(new Point("0", receiverhash));
				if (!inter_graph.contains(calledge0))
					inter_graph.add(calledge0);
				for (int i = 0; i < callInfo.args.size(); i++) {
					Point call = callInfo.args.get(i);
					Point receive = callInfo.rargs.get(i);
					CallEdge calledge = new CallEdge();
					calledge.addCaller(new Point(call.getName(), base.getHashcode()));
					calledge.addReceiver(new Point(receive.getName(), receiverhash));
					if (!inter_graph.contains(calledge))
						inter_graph.add(calledge);
				}
				

				CallEdge calledge = new CallEdge();
				calledge.addCaller(new Point(".return", -receiverhash));
				calledge.addReceiver(new Point(base.getName(), -base.getHashcode()));
				if (!inter_graph.contains(calledge))
					inter_graph.add(calledge);

//				List<Local> ret = recordReturn.get(receiverhash);
//				CallEdge calledge1 = new CallEdge();
//				calledge1.addCaller(new Point("0", -receiverhash));
//				calledge1.addReceiver(new Point("0", -base.getHashcode()));
//				if (!inter_graph.contains(calledge1))
//					inter_graph.add(calledge1);
//				for (Local l : ret) {
//					if (l == null) {
//						CallEdge calledge = new CallEdge();
//						calledge.addCaller(new Point("0", -receiverhash));
//						calledge.addReceiver(new Point(base.getName(), -base.getHashcode()));
//						if (!inter_graph.contains(calledge))
//							inter_graph.add(calledge);
//					} else if (l instanceof Local) {
//						CallEdge calledge = new CallEdge();
//						calledge.addCaller(new Point(l.getName(), -receiverhash));
//						calledge.addReceiver(new Point(base.getName(), -base.getHashcode()));
//						if (!inter_graph.contains(calledge))
//							inter_graph.add(calledge);
//					}
//				}
			}
		}

		printCall("D:/project/workspace/NPGraph/sootOutput/");
		printMapCall("D:/project/workspace/NPGraph/sootOutput/");
		printMap("D:/project/workspace/NPGraph/sootOutput/");
	}

	private void initEntry() {
		Body body = sm.getActiveBody();
		Stmt st = (Stmt) body.getUnits().iterator().next();
		objectlocal.clear();
		Chain<Local> locals = body.getLocals();
		CallEdge calledge0 = new CallEdge();
		calledge0.addCaller(new Point("0", sm.hashCode()));
		calledge0.addReceiver(new Point("0", st.hashCode()));
		intra_graph.addCallEdge(calledge0);
		for (Local local : locals) {
			if (isTypeofInterest(local)) {
				objectlocal.add(local);
				CallEdge calledge = new CallEdge();
				if (sm.getName().equals("main")) {
					calledge.addCaller(new Point("0", sm.hashCode()));
					calledge.addReceiver(new Point(local.getName(), st.hashCode()));
					intra_graph.addCallEdge(calledge);
				} else {
					List<Local> par = getPar();
					if (par.contains(local) || local.getName().equals("this")) {
						calledge.addCaller(new Point(local.getName(), sm.hashCode()));
					} else {
						calledge.addCaller(new Point("0", sm.hashCode()));
					}
					calledge.addReceiver(new Point(local.getName(), st.hashCode()));
					intra_graph.addCallEdge(calledge);
				}
			}
		}
	}

	private List<Local> getPar() {
		List<Local> par = new ArrayList<Local>();
		int parnum = sm.getParameterCount();
		for (int i = 0; i < parnum; i++) {
			par.add(sm.getActiveBody().getParameterLocal(i));
		}
		return par;
	}

	private void RF(Local lop, List<Local> rub, Function f, int caller, int receiver) {
		List<String> result = new ArrayList<String>();
		result.add("0,0");
		List<Local> fnull = f.func(null, lop, rub);
		for (Local l : fnull) {
			result.add("0," + l.getName());
		}
		for (Local ol : objectlocal) {
			List<Local> fy = f.func(ol, lop, rub);
			for (Local y : fy) {
				if (!fnull.contains(y))
					result.add(ol.getName() + "," + y.getName());
			}
		}
		for (String s : result) {
			String s1 = s.split(",")[0];
			String s2 = s.split(",")[1];
			CallEdge calledge = new CallEdge();
			calledge.addCaller(new Point(s1, caller));
			calledge.addReceiver(new Point(s2, receiver));
			intra_graph.addCallEdge(calledge);
		}
	}

	private void interFunc(Stmt st, Stmt succst) {
		// to complete later
		InvokeExpr ie = st.getInvokeExpr();
		SootMethod receiver = ie.getMethod();
		if(!receiver.hasActiveBody()){
			return;
		}
		if (st instanceof AssignStmt) {
			Value lhs = ((DefinitionStmt) st).getLeftOp();
			Value rhs = ((DefinitionStmt) st).getRightOp();
			if (!(lhs instanceof Local) || !contains(((Local) lhs).getName())) {
				return;
			}
			List<Point> args = new ArrayList<Point>();
			List<Point> rargs = new ArrayList<Point>();
			for (Local local : receiver.getActiveBody().getLocals()) {
				if (local.getName().equals("this") && ie instanceof InstanceInvokeExpr) {
					args.add(new Point(((Local) ((InstanceInvokeExpr) st.getInvokeExpr()).getBase()).getName(),
							st.hashCode()));
					rargs.add(new Point("this", receiver.hashCode()));
					break;
				}
			}
			for (int i = 0; i < ie.getArgCount(); i++) {
				Value arg = ie.getArg(i);
				if (arg instanceof Local && contains(((Local) arg).getName())) {
					Local rarg = receiver.getActiveBody().getParameterLocal(i);
					args.add(new Point(((Local) arg).getName(), st.hashCode()));
					rargs.add(new Point(rarg.getName(), receiver.hashCode()));
				}
			}

			// if(!args.isEmpty())
			callInfoList.add(new CallInfo(new Point(((Local) lhs).getName(), st.hashCode()), args, rargs,
					ie.getMethod().hashCode()));
		}
		// if(ie instanceof InstanceInvokeExpr){
		// InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
		// List<Local> u = iie.getUseBoxes();
		// Local base = (Local) ((InstanceInvokeExpr) ie).getBase();
		// Local last = sm.getActiveBody().getLocals().getLast();
		// List<Point> args = new ArrayList<Point>();
		// List<Point> rargs = new ArrayList<Point>();
		// for(int i = 0; i<ie.getArgCount(); i++){
		// Value arg = ie.getArg(i);
		// if(arg instanceof Local && contains(((Local)arg).getName())){
		// Local rarg = receiver.getActiveBody().getParameterLocal(i);
		// args.add(new Point(((Local)arg).getName(), st.hashCode()));
		// rargs.add(new Point(rarg.getName(), receiver.hashCode()));
		// }
		// }
		//
		// //if(!args.isEmpty())
		// callInfoList.add(new CallInfo(new Point(last.getName(),
		// st.hashCode()), args, rargs, ie.getMethod().hashCode()));
		// }
	}

	private void domytest(Stmt s, Stmt succst) {
		// case inter: local := function()
		if (s.containsInvokeExpr()) {
			if (s instanceof AssignStmt) {
				Local lhs = (Local) ((AssignStmt) s).getLeftOp();
				interFunc(s, succst);
				call = true;
				// doAllMove(s.hashCode(), -s.hashCode());
				CallEdge calledge0 = new CallEdge();
				calledge0.addCaller(new Point("0", s.hashCode()));
				calledge0.addReceiver(new Point("0", -s.hashCode()));
				intra_graph.addCallEdge(calledge0);
				for (Local l : objectlocal) {
					if (!l.getName().equals(lhs.getName())) {
						CallEdge calledge = new CallEdge();
						calledge.addCaller(new Point(l.getName(), s.hashCode()));
						calledge.addReceiver(new Point(l.getName(), -s.hashCode()));
						intra_graph.addCallEdge(calledge);
					}
				}
				doAllMove(-s.hashCode(), succst.hashCode());
				return;
			}
			doAllMove(s.hashCode(), succst.hashCode());
			return;
		}
		// case 1: ReturnStmt
		if (s instanceof ReturnStmt) {
			Value v = ((ReturnStmt) s).getOp();
			doAllMove(callhash, -sm.hashCode());
			CallEdge calledge = new CallEdge();
			if (recordReturn.containsKey(sm.hashCode())) {
				List<Local> ret = recordReturn.get(sm.hashCode());
				if (v instanceof NullConstant || !(v instanceof Local) || !(contains(((Local) v).getName()))) {
					calledge.addCaller(new Point("0", callhash));
					calledge.addReceiver(new Point(".return", -sm.hashCode()));
					intra_graph.addCallEdge(calledge);
					ret.add(null);
				}
				else{
					calledge.addCaller(new Point(((Local) v).getName(), callhash));
					calledge.addReceiver(new Point(".return", -sm.hashCode()));
					intra_graph.addCallEdge(calledge);
					ret.add((Local) v);
				}
				recordReturn.put(sm.hashCode(), ret);
			} else {
				List<Local> ret = new ArrayList<Local>();
				if (v instanceof NullConstant || !(v instanceof Local) || !(contains(((Local) v).getName()))) {
					calledge.addCaller(new Point("0", callhash));
					calledge.addReceiver(new Point(".return", -sm.hashCode()));
					intra_graph.addCallEdge(calledge);
					ret.add(null);
				}
				else{
					calledge.addCaller(new Point(((Local) v).getName(), callhash));
					calledge.addReceiver(new Point(".return", -sm.hashCode()));
					intra_graph.addCallEdge(calledge);
					ret.add((Local) v);
				}
				recordReturn.put(sm.hashCode(), ret);
			}
			return;
		}

		// case 2: ThrowStmt
		if (s instanceof ThrowStmt) {
			return;
		}

		Value lhs = ((DefinitionStmt) s).getLeftOp();
		Value rhs = ((DefinitionStmt) s).getRightOp();

		// case 3: IdentityStmt
		if (s instanceof IdentityStmt) {
			if ((rhs instanceof ThisRef || rhs instanceof ParameterRef)) {
				// intra_graph.addFormalParameter((Local) lhs);
				doAllMove(callhash, succst.hashCode());
			}
			return;
		}

		// case 4: AssignStmt
		if (s instanceof AssignStmt) {
			// case 4.1: lhs is array access
			if (lhs instanceof ArrayRef) {
				// array will be complete later
				// if rhs is local
				/*
				 * ArrayRef arl = (ArrayRef) lhs; Local lopbase = (Local)
				 * arl.getBase(); if (rhs instanceof Local &&
				 * objectlocal.contains(rhs)) { //
				 * intra_graph.addLocal2ArrayRef((Local) rhs, (ArrayRef) //
				 * lhs); List<Local> rub = new ArrayList<Local>();
				 * rub.add((Local) rhs); Function f = new Assign(); RF(lopbase,
				 * rub, f, callhash, succst.hashCode()); return; } if (rhs
				 * instanceof BinopExpr) { BinopExpr ber = (BinopExpr) rhs;
				 * List<Local> rub = new ArrayList<Local>(); if
				 * (objectlocal.contains((Local) ber.getOp1())) rub.add((Local)
				 * ber.getOp1()); if (objectlocal.contains((Local)
				 * ber.getOp2())) rub.add((Local) ber.getOp2()); Function f =
				 * new Assign(); RF(lopbase, rub, f, callhash,
				 * succst.hashCode()); return; }
				 */
				doAllMove(callhash, succst.hashCode());
				return;
			}

			// case 4.2: lhs is a field access
			if (lhs instanceof FieldRef) {
				// field will be complete later
				// Local lopbase = (Local) ((InstanceFieldRef) lhs).getBase();
				// if (rhs instanceof Local && objectlocal.contains(rhs)) {
				// // intra_graph.addLocal2FieldRef((Local) rhs, (FieldRef)
				// // lhs);
				// List<Local> rub = new ArrayList<Local>();
				// rub.add((Local) rhs);
				// Function f = new Assign();
				// RF(lopbase, rub, f, callhash, succst.hashCode());
				// return;
				// }
				// if (rhs instanceof BinopExpr) {
				// BinopExpr ber = (BinopExpr) rhs;
				// List<Local> rub = new ArrayList<Local>();
				// if (objectlocal.contains((Local) ber.getOp1()))
				// rub.add((Local) ber.getOp1());
				// if (objectlocal.contains((Local) ber.getOp2()))
				// rub.add((Local) ber.getOp2());
				// Function f = new Assign();
				// RF(lopbase, rub, f, callhash, succst.hashCode());
				// return;
				// }
				doAllMove(callhash, succst.hashCode());
				return;
			}

			// case 4.3: local := local
			if (!(lhs instanceof Local) || !contains(((Local) lhs).getName())) {
				doAllMove(callhash, succst.hashCode());
				return;
			}
			if (rhs instanceof Local && contains(((Local) rhs).getName())) {
				// intra_graph.addLocal2Local((Local) rhs, (Local) lhs);
				List<Local> rub = new ArrayList<Local>();
				rub.add((Local) rhs);
				Function f = new Assign();
				RF((Local) lhs, rub, f, callhash, succst.hashCode());
				return;
			}
			if (rhs instanceof BinopExpr) {
				BinopExpr ber = (BinopExpr) rhs;
				List<Local> rub = new ArrayList<Local>();
				if (objectlocal.contains((Local) ber.getOp1()))
					rub.add((Local) ber.getOp1());
				if (objectlocal.contains((Local) ber.getOp2()))
					rub.add((Local) ber.getOp2());
				Function f = new Assign();
				RF((Local) lhs, rub, f, callhash, succst.hashCode());
				return;
			}
			// case 4.4.1: local := string const
			if (rhs instanceof StringConstant) {
				// intra_graph.addStringConst2Local((StringConstant) rhs,
				// (Local) lhs);
				doAllMove(callhash, succst.hashCode());
				return;
			}
			// case 4.4.2: local := class const
			if (rhs instanceof ClassConstant) {
				// intra_graph.addClassConst2Local((ClassConstant) rhs, (Local)
				// lhs);
				doAllMove(callhash, succst.hashCode());
				return;
			}

			// case 4.5: local := new X
			if (rhs instanceof NewExpr) {
				// intra_graph.addNewExpr2Local((NewExpr) rhs, (Local) lhs);
				Function f = new Remove();
				RF((Local) lhs, null, f, callhash, succst.hashCode());
				return;
			}

			// case 4.6: new array: e.g. x := new Y[5];
			if (rhs instanceof NewArrayExpr) {
				// intra_graph.addNewArrayExpr2Local((NewArrayExpr) rhs, (Local)
				// lhs);
				Function f = new Remove();
				RF((Local) lhs, null, f, callhash, succst.hashCode());
				return;
			}

			// case 4.7: new multi-dimensional array
			if (rhs instanceof NewMultiArrayExpr) {
				// intra_graph.addNewMultiArrayExpr2Local((NewMultiArrayExpr)
				// rhs, (Local) lhs);
				Function f = new Remove();
				RF((Local) lhs, null, f, callhash, succst.hashCode());
				return;
			}
			if (rhs instanceof NullConstant) {
				Function f = new Add();
				RF((Local) lhs, null, f, callhash, succst.hashCode());
				return;
			}
			// case 4.8: rhs is field access x.f; X.f is StaticFieldRef, not
			// consider
			if (rhs instanceof FieldRef) {
				// will be completed later
				// intra_graph.addField2Local((FieldRef) rhs, (Local) lhs);
				// Local ropbase = (Local) ((InstanceFieldRef) rhs).getBase();
				// List<Local> rub = new ArrayList<Local>();
				// rub.add(ropbase);
				// Function f = new Assign();
				// RF((Local) lhs, rub, f, callhash, succst.hashCode());
				doAllMove(callhash, succst.hashCode());
				return;
			}
			// case4.11: rhs is arrayref
			if (rhs instanceof ArrayRef) {
				// will be completed later
				// if rhs is local
				// ArrayRef arr = (ArrayRef) rhs;
				// Local ropbase = (Local) arr.getBase();
				// if (contains(ropbase.getName())) {
				// // intra_graph.addLocal2ArrayRef((Local) rhs, (ArrayRef)
				// // lhs);
				// List<Local> rub = new ArrayList<Local>();
				// rub.add(ropbase);
				// Function f = new Assign();
				// RF((Local) lhs, rub, f, callhash, succst.hashCode());
				// return;
				// }
				doAllMove(callhash, succst.hashCode());
				return;
			}
			// case 4.9: cast
			if (rhs instanceof CastExpr) {
				Value y = ((CastExpr) rhs).getOp();
				// possibleTypes.add(lhs.getType());
				if (y instanceof Local && contains(((Local) y).getName())) {
					// intra_graph.addLocal2Local((Local) y, (Local) lhs);
					List<Local> rub = new ArrayList<Local>();
					rub.add((Local) y);

					Function f = new Assign();
					RF((Local) lhs, rub, f, callhash, succst.hashCode());
					return;
				}
				doAllMove(callhash, succst.hashCode());
				return;
			}

			// case 4.10: rhs is array reference
			if (rhs instanceof ArrayRef && isTypeofInterest(rhs)) {
				// intra_graph.addArrayRef2Local((ArrayRef) rhs, (Local) lhs);
				doAllMove(callhash, succst.hashCode());
				return;
			}
			doAllMove(s.hashCode(), succst.hashCode());
			return;

		} // AssignStmt

	}

	/**
	 * Ignores certain types of statements, and calls addFlowEdges()
	 * 
	 * @param s
	 * @param sm
	 */
	private void processStmt(Stmt s, Stmt succst) {
		// if (call) {
		// callhash = -s.hashCode();
		// call = false;
		// } else
		// callhash = s.hashCode();
		callhash = s.hashCode();
		if (s instanceof ReturnVoidStmt) {
			doAllMove(callhash, -sm.hashCode());
			CallEdge calledge = new CallEdge();
			calledge.addCaller(new Point("0", callhash));
			calledge.addReceiver(new Point(".return", -sm.hashCode()));
			intra_graph.addCallEdge(calledge);
			if (recordReturn.containsKey(sm.hashCode())) {
				List<Local> ret = recordReturn.get(sm.hashCode());
				ret.add(null);
				recordReturn.put(sm.hashCode(), ret);
			} else {
				List<Local> ret = new ArrayList<Local>();
				ret.add(null);
				recordReturn.put(sm.hashCode(), ret);
			}
			return;
		}
		if (s instanceof GotoStmt) {
			GotoStmt gos = (GotoStmt) s;
			Unit target = gos.getTarget();
			doAllMove(callhash, target.hashCode());
			return;
		}
		if (s instanceof IfStmt) {
			IfStmt ifs = (IfStmt) s;
			Stmt target = ifs.getTarget();
			doAllMove(callhash, target.hashCode());
			doAllMove(callhash, succst.hashCode());
			return;
		}
		if (s instanceof TableSwitchStmt) {
			TableSwitchStmt tst = (TableSwitchStmt) s;
			Unit defaulttarget = tst.getDefaultTarget();
			doAllMove(callhash, defaulttarget.hashCode());
			Iterator targets = tst.getTargets().iterator();
			for (; targets.hasNext();) {
				Unit target = (Unit) targets.next();
				doAllMove(callhash, target.hashCode());
			}
			return;
		}
		if (s instanceof LookupSwitchStmt){
			doAllMove(callhash, -sm.hashCode());
			return;
		}
		if (s instanceof MonitorStmt){
			doAllMove(callhash, -sm.hashCode());
			return;
		}
		if (s instanceof RetStmt) {
			doAllMove(callhash, -sm.hashCode());
			CallEdge calledge = new CallEdge();
			calledge.addCaller(new Point("0", callhash));
			calledge.addReceiver(new Point(".return", -sm.hashCode()));
			intra_graph.addCallEdge(calledge);
			if (recordReturn.containsKey(sm.hashCode())) {
				List<Local> ret = recordReturn.get(sm.hashCode());
				ret.add(null);
				recordReturn.put(sm.hashCode(), ret);
			} else {
				List<Local> ret = new ArrayList<Local>();
				ret.add(null);
				recordReturn.put(sm.hashCode(), ret);
			}
			return;
		}
		if (s instanceof NopStmt) {
			doAllMove(callhash, -sm.hashCode());
			CallEdge calledge = new CallEdge();
			calledge.addCaller(new Point("0", callhash));
			calledge.addReceiver(new Point(".return", -sm.hashCode()));
			intra_graph.addCallEdge(calledge);
			if (recordReturn.containsKey(sm.hashCode())) {
				List<Local> ret = recordReturn.get(sm.hashCode());
				ret.add(null);
				recordReturn.put(sm.hashCode(), ret);
			} else {
				List<Local> ret = new ArrayList<Local>();
				ret.add(null);
				recordReturn.put(sm.hashCode(), ret);
			}
			return;
		}
		// this function is mine
		domytest(s, succst);
		// addFlowEdges(s);
	}

	private void doAllMove(int start, int end) {
		CallEdge calledge0 = new CallEdge();
		calledge0.addCaller(new Point("0", start));
		calledge0.addReceiver(new Point("0", end));
		intra_graph.addCallEdge(calledge0);
		for (Local l : objectlocal) {
			CallEdge calledge = new CallEdge();
			calledge.addCaller(new Point(l.getName(), start));
			calledge.addReceiver(new Point(l.getName(), end));
			intra_graph.addCallEdge(calledge);
		}
	}

	private boolean contains(String name) {
		for (Local local : objectlocal) {
			if (local.getName().equals(name))
				return true;
		}
		return false;
	}

	private boolean isJavaObjectNew(InvokeExpr invoke) {
		SootMethod static_target = invoke.getMethod();
		String sig = static_target.getSubSignature();
		String cls = static_target.getDeclaringClass().getName();

		return (sig.equals("java.lang.Object newInstance()") && cls.equals("java.lang.Class"))
				|| (sig.equals("java.lang.Object newInstance(java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Constructor"))
				|| (static_target.getSignature()
						.equals("<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>"))
				|| (sig.equals("java.lang.Object invoke(java.lang.Object,java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Method"))
				|| (sig.equals(
						"java.lang.Object newProxyInstance(java.lang.ClassLoader,java.lang.Class[],java.lang.reflect.InvocationHandler)")
						&& cls.equals("java.lang.reflect.Proxy"));

	}

	public static boolean isTypeofInterest(Value v) {
		return (v.getType() instanceof RefType || v.getType() instanceof ArrayType);
	}
}
