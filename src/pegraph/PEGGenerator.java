package pegraph;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import pegraph.datastructure.PegIntra;
import pegraph.datastructure.CallEdge;
import pegraph.datastructure.Point;
import function.*;
import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
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
	
	private PegIntra intra_graph;
	private List<Local> objectlocal = new ArrayList<Local>();
	private Map<SootMethod, List<Value>> recordReturn = new HashMap<SootMethod, List<Value>>();

	
	@Override
	protected void internalTransform(Body arg0, String arg1, Map arg2) {
		// TODO Auto-generated method stub
		sm = arg0.getMethod();
		intra_graph = new PegIntra(sm);
		
		if (!sm.hasActiveBody()) {
			sm.retrieveActiveBody();
		}
		initEntry();
		// first of all, flow edges are added by inspecting the statements in the
		// method one by one
		Stmt nowst,succst;
		Iterator stmts = sm.getActiveBody().getUnits().iterator();
		if(stmts.hasNext()){
			nowst = (Stmt) stmts.next();
			succst = nowst;
			for ( ;stmts.hasNext();) {
				succst = (Stmt) stmts.next();
			
				processStmt(nowst, succst);
				nowst = succst;
			}
			processStmt(succst, succst);
		}
		
	}
	
	private void initEntry(){
		Body body = sm.getActiveBody();
		objectlocal.clear();
		Chain<Local> locals = body.getLocals();
		CallEdge calledge0 = new CallEdge();
		calledge0.addCaller(new Point("0", sm.hashCode()));
		calledge0.addReceiver(new Point("0", sm.hashCode()));
		intra_graph.addCallEdge(calledge0);
		for(Local local : locals){
			if(isTypeofInterest(local)){
				objectlocal.add(local);
				CallEdge calledge = new CallEdge();
				Stmt st = (Stmt)body.getUnits().iterator().next();
				if(sm.getName().equals("main")){
					calledge.addCaller(new Point("0", sm.hashCode()));
					calledge.addReceiver(new Point(local.getName(), st.hashCode()));
					intra_graph.addCallEdge(calledge);
				}else {
					List<Local> par = getPar();
					if(par.contains(local)){
						calledge.addCaller(new Point(local.getName(), sm.hashCode()));
					}else{
						calledge.addCaller(new Point("0", sm.hashCode()));
					}
					calledge.addReceiver(new Point(local.getName(), st.hashCode()));
					intra_graph.addCallEdge(calledge);
				}
			}
		}
	}
	
	private List<Local> getPar(){
		List<Local> par = new ArrayList<Local>();
		int parnum = sm.getParameterCount();
		for(int i = 0; i < parnum; i++){
			par.add(sm.getActiveBody().getParameterLocal(i));
		}
		return par;
	}
	
	private void RF(Local lop, List<Local> rub, Function f, Stmt st, Stmt succst){
		List<String> result = new ArrayList<String>();
		result.add("0,0");
		List<Local> fnull = f.func(null, lop, rub);
		for(Local l : fnull){
			result.add("0,"+l.getName());
		}
		for(Local ol:objectlocal){
			List<Local> fy = f.func(ol, lop, rub);
			for(Local y : fy){
				if(!fnull.contains(y))
					result.add(ol.getName()+","+y.getName());
			}
		}
		for(String s : result){
			String s1 = s.split(",")[0];
			String s2 = s.split(",")[1];
			CallEdge calledge = new CallEdge();
			calledge.addCaller(new Point(s1, st.hashCode()));
			calledge.addReceiver(new Point(s2, succst.hashCode()));
			intra_graph.addCallEdge(calledge);
		}
	}
	
	private void interFunc(){
		//to complete later
	}
	
	private void domytest(Stmt s, Stmt succst) {
		if (s.containsInvokeExpr()) {
			interFunc();
			return;
		}

		// case 1: ReturnStmt
		if (s instanceof ReturnStmt) {
			Value v = ((ReturnStmt) s).getOp();
			doAllMove(s.hashCode(), sm.getActiveBody().hashCode());
			if(recordReturn.containsKey(sm)){
				List<Value> ret = recordReturn.get(sm);
				ret.add(v);
				recordReturn.put(sm, ret);
			}else{
				List<Value> ret = new ArrayList<Value>();
				ret.add(v);
				recordReturn.put(sm, ret);
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
				//intra_graph.addFormalParameter((Local) lhs);
				doAllMove(s.hashCode(), succst.hashCode());
			}
			return;
		}

		// case 4: AssignStmt
		if (s instanceof AssignStmt) {
			// case 4.1: lhs is array access
			if (lhs instanceof ArrayRef) {
				// if rhs is local
				ArrayRef arl = (ArrayRef)lhs;
				Local lopbase = (Local)arl.getBase();
				if (rhs instanceof Local && objectlocal.contains(rhs)) {
					//intra_graph.addLocal2ArrayRef((Local) rhs, (ArrayRef) lhs);
					List<Local> rub = new ArrayList<Local>();
					rub.add((Local)rhs);
					Function f = new Assign();
					RF(lopbase, rub, f, s, succst);
					return;
				}
				if(rhs instanceof BinopExpr){
					BinopExpr ber = (BinopExpr)rhs;
					List<Local> rub = new ArrayList<Local>();
					if(objectlocal.contains((Local)ber.getOp1()))
						rub.add((Local)ber.getOp1());
					if(objectlocal.contains((Local)ber.getOp2()))
						rub.add((Local)ber.getOp2());
					Function f = new Assign();
					RF(lopbase, rub, f, s, succst);
					return;
				}
				doAllMove(s.hashCode(), succst.hashCode());
				return;
			}

			// case 4.2: lhs is a field access
			if (lhs instanceof InstanceFieldRef) {
				Local lopbase = (Local)((InstanceFieldRef)lhs).getBase();
				if (rhs instanceof Local && objectlocal.contains(rhs)) {
					//intra_graph.addLocal2FieldRef((Local) rhs, (FieldRef) lhs);
					List<Local> rub = new ArrayList<Local>();
					rub.add((Local)rhs);
					Function f = new Assign();
					RF(lopbase, rub, f, s, succst);
					return;
				}
				if(rhs instanceof BinopExpr){
					BinopExpr ber = (BinopExpr)rhs;
					List<Local> rub = new ArrayList<Local>();
					if(objectlocal.contains((Local)ber.getOp1()))
						rub.add((Local)ber.getOp1());
					if(objectlocal.contains((Local)ber.getOp2()))
						rub.add((Local)ber.getOp2());
					Function f = new Assign();
					RF(lopbase, rub, f, s, succst);
					return;
				}
				doAllMove(s.hashCode(), succst.hashCode());
				return;
			}

			
			// case 4.3: local := local
			if(!objectlocal.contains(lhs)){
				doAllMove(s.hashCode(), succst.hashCode());
				return;
			}				
			if (rhs instanceof Local && objectlocal.contains(rhs)) {
				//intra_graph.addLocal2Local((Local) rhs, (Local) lhs);
				List<Local> rub = new ArrayList<Local>();
				if(rhs instanceof Local && objectlocal.contains(rhs)){
					rub.add((Local)rhs);
				}
				Function f = new Assign();
				RF((Local)lhs, rub, f, s, succst);
				return;
			}
			if(rhs instanceof BinopExpr){
				BinopExpr ber = (BinopExpr)rhs;
				List<Local> rub = new ArrayList<Local>();
				if(objectlocal.contains((Local)ber.getOp1()))
					rub.add((Local)ber.getOp1());
				if(objectlocal.contains((Local)ber.getOp2()))
					rub.add((Local)ber.getOp2());
				Function f = new Assign();
				RF((Local)lhs, rub, f, s, succst);
				return;
			}
			// case 4.4.1: local := string const
			if (rhs instanceof StringConstant) {
				//intra_graph.addStringConst2Local((StringConstant) rhs, (Local) lhs);
				doAllMove(s.hashCode(), succst.hashCode());
				return;
			}
			// case 4.4.2: local := class const
			if (rhs instanceof ClassConstant) {
				//intra_graph.addClassConst2Local((ClassConstant) rhs, (Local) lhs);
				doAllMove(s.hashCode(), succst.hashCode());
				return;
			}

			// case 4.5: local := new X
			if (rhs instanceof NewExpr) {
				//intra_graph.addNewExpr2Local((NewExpr) rhs, (Local) lhs);
				Function f = new Remove();
				RF((Local)lhs, null, f, s, succst);
				return;
			}

			// case 4.6: new array: e.g. x := new Y[5];
			if (rhs instanceof NewArrayExpr) {
				//intra_graph.addNewArrayExpr2Local((NewArrayExpr) rhs, (Local) lhs);
				Function f = new Remove();
				RF((Local)lhs, null, f, s, succst);
				return;
			}

			// case 4.7: new multi-dimensional array
			if (rhs instanceof NewMultiArrayExpr) {
				//intra_graph.addNewMultiArrayExpr2Local((NewMultiArrayExpr) rhs, (Local) lhs);
				Function f = new Remove();
				RF((Local)lhs, null, f, s, succst);
				return;
			}

			// case 4.8: rhs is field access x.f; X.f is StaticFieldRef, not consider
			if (rhs instanceof InstanceFieldRef && objectlocal.contains(rhs)) {
				//intra_graph.addField2Local((FieldRef) rhs, (Local) lhs);
				Local ropbase = (Local)((InstanceFieldRef)rhs).getBase();
				List<Local> rub = new ArrayList<Local>();
				rub.add(ropbase);
				Function f = new Assign();
				RF((Local)lhs, rub, f, s, succst);
				return;
			}

			// case 4.9: cast
			if (rhs instanceof CastExpr) {
				Value y = ((CastExpr) rhs).getOp();
				// possibleTypes.add(lhs.getType());
				if (y instanceof Local && isTypeofInterest(y)) {
					//intra_graph.addLocal2Local((Local) y, (Local) lhs);
					List<Local> rub = new ArrayList<Local>();
					if(rhs instanceof Local && objectlocal.contains(rhs)){
						rub.add((Local)y);
					}
					Function f = new Assign();
					RF((Local)lhs, rub, f, s, succst);
					return;
				}
				doAllMove(s.hashCode(), succst.hashCode());
				return;
			}

			// case 4.10: rhs is array reference
			if (rhs instanceof ArrayRef && isTypeofInterest(rhs)) {
				//intra_graph.addArrayRef2Local((ArrayRef) rhs, (Local) lhs);
				return;
			}

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
		if (s instanceof ReturnVoidStmt){
			doAllMove(s.hashCode(), sm.getActiveBody().hashCode());
			if(recordReturn.containsKey(sm)){
				List<Value> ret = recordReturn.get(sm);
				ret.add(null);
				recordReturn.put(sm, ret);
			}else{
				List<Value> ret = new ArrayList<Value>();
				ret.add(null);
				recordReturn.put(sm, ret);
			}
			return;
		}			
		if (s instanceof GotoStmt){
			GotoStmt gos = (GotoStmt)s;
			Unit target = gos.getTarget();
			doAllMove(s.hashCode(), target.hashCode());
			return;
		}
		if (s instanceof IfStmt){
			IfStmt ifs = (IfStmt)s;
			Stmt target = ifs.getTarget();
			doAllMove(s.hashCode(), target.hashCode());
			return;
		}
		if (s instanceof TableSwitchStmt){
			return;
		}
		if (s instanceof LookupSwitchStmt)
			return;
		if (s instanceof MonitorStmt)
			return;
		if (s instanceof RetStmt)
			return;
		if (s instanceof NopStmt)
			return;
		//this function is mine
		domytest(s, succst);
		//addFlowEdges(s);
	}
	
	private void doAllMove(int start, int end){
		CallEdge calledge0 = new CallEdge();
		calledge0.addCaller(new Point("0", start));
		calledge0.addReceiver(new Point("0", end));
		for(Local l : objectlocal){
			CallEdge calledge = new CallEdge();
			calledge.addCaller(new Point(l.getName(), start));
			calledge.addReceiver(new Point(l.getName(), end));
		}
	}

	private boolean isJavaObjectNew(InvokeExpr invoke){
		SootMethod static_target = invoke.getMethod();
		String sig = static_target.getSubSignature();
		String cls = static_target.getDeclaringClass().getName();
		
		return (sig.equals("java.lang.Object newInstance()")
				&& cls.equals("java.lang.Class")) ||
				(sig.equals("java.lang.Object newInstance(java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Constructor")) ||
				(static_target.getSignature().equals("<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>")) ||
				(sig.equals("java.lang.Object invoke(java.lang.Object,java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Method")) ||
				(sig.equals("java.lang.Object newProxyInstance(java.lang.ClassLoader,java.lang.Class[],java.lang.reflect.InvocationHandler)")
						&& cls.equals("java.lang.reflect.Proxy"));
				
	}	
	
	public static boolean isTypeofInterest(Value v) {
		return (v.getType() instanceof RefType || v.getType() instanceof ArrayType);
	}
}
