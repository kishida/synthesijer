package synthesijer.hdl;

import java.util.ArrayList;
import java.util.HashMap;

import synthesijer.hdl.expr.HDLPreDefinedConstant;
import synthesijer.hdl.expr.HDLValue;
import synthesijer.hdl.sequencer.SequencerState;

public class HDLSequencer implements HDLTree{

	private final HDLModule module;

	private final HDLSignal stateKey;
	private final HDLSignal prevStateKey;
	private final HDLUserDefinedType stateType;
	private ArrayList<SequencerState> states;
	private ArrayList<Pair> seqExprList = new ArrayList<>();
	private ArrayList<Triple> seqCondExprList = new ArrayList<>();
	private ArrayList<Quatro> seqCondExprList2 = new ArrayList<>();
	private SequencerState idle;
	private int timestep = -1;
	private HashMap<String, SequencerState> map = new HashMap<>();

	private final HDLSignal delayCounter;

	public HDLSequencer(HDLModule module, String key){
		this.module = module;
		this.stateType = module.newUserDefinedType(key, null, 0);
		this.stateKey = module.newSignal(key, stateType);
		this.prevStateKey = module.newSignal(key + "_prev", stateType);
		HDLValue idleId = stateType.addItem(stateKey.getName() + "_IDLE");
		this.idle = new SequencerState(this, stateKey, idleId);
		states = new ArrayList<>();
		states.add(idle);
		map.put(idleId.getValue(), this.idle);
		delayCounter = module.newSignal(key + "_delay", HDLPrimitiveType.genSignedType(32), HDLSignal.ResourceKind.REGISTER);
		delayCounter.setDefaultValue(HDLPreDefinedConstant.VECTOR_ZERO);
		delayCounter.setIgnore(true);
	}

	private int id = 0;
	private int genUniqId(){ int tmp = id; id++; return tmp; }

	public void setTransitionTime(int step){
		this.timestep = step;
	}

	public int getTransitionTime(){
		return timestep;
	}

	public boolean hasTransitionTime(){
		return (timestep > 0);
	}

	public SequencerState addSequencerState(String id, boolean flag){
		String n = flag ? stateKey.getName()+"_"+id : id;
		if(map.containsKey(n)){
			return map.get(n);
		}
		HDLValue value = stateType.addItem(n);
		SequencerState s = new SequencerState(this, stateKey, value);
		states.add(s);
		map.put(value.getValue(), s);
		return s;
	}

	public SequencerState addSequencerState(String id){
		return addSequencerState(id, true);
	}

	public SequencerState addSequencerState(){
		String id = String.format("sjr_tmp_state_%04d", genUniqId());
		return addSequencerState(id, true);
	}

	public HDLModule getModule(){
		return module;
	}

	public HDLSignal getStateKey(){
		return stateKey;
	}
	
	public HDLSignal getPrevStateKey(){
		return prevStateKey;
	}

	public ArrayList<SequencerState> getStates(){
		return states;
	}

	public SequencerState getIdleState(){
		return idle;
	}

	public HDLSignal getDelayCounter(){
		return delayCounter;
	}

	@Override
	public void accept(HDLTreeVisitor v) {
		v.visitHDLSequencer(this);
	}

	public void addSeqExpr(HDLSignal dest, HDLExpr expr){
		seqExprList.add(new Pair(dest, expr));
	}

	public void addSeqCondExpr(HDLSignal dest, HDLExpr cond, HDLExpr expr){
		seqCondExprList.add(new Triple(dest, cond, expr));
	}
	
	public void addSeqCondExpr(HDLSignal dest, HDLExpr a, HDLExpr b, HDLExpr expr){
		seqCondExprList2.add(new Quatro(dest, a, b, expr));
	}

	public Pair[] getSeqExprList(){
		return seqExprList.toArray(new Pair[0]);
	}

	public Triple[] getSeqCondExprList(){
		return seqCondExprList.toArray(new Triple[0]);
	}

	public Quatro[] getSeqCondExprList2(){
		return seqCondExprList2.toArray(new Quatro[0]);
	}

	public class Pair{
		public final HDLSignal dest;
		public final HDLExpr expr;
		public Pair(HDLSignal d, HDLExpr e){
			this.dest = d;
			this.expr = e;
		}
	}

	public class Triple{
		public final HDLSignal dest;
		public final HDLExpr cond;
		public final HDLExpr expr;
		public Triple(HDLSignal d, HDLExpr c, HDLExpr e){
			this.dest = d;
			this.cond = c;
			this.expr = e;
		}
	}
	
	public class Quatro{
		public final HDLSignal dest;
		public final HDLExpr a;
		public final HDLExpr b;
		public final HDLExpr expr;
		public Quatro(HDLSignal d, HDLExpr a, HDLExpr b, HDLExpr e){
			this.dest = d;
			this.a = a;
			this.b = b;
			this.expr = e;
		}
	}
}
