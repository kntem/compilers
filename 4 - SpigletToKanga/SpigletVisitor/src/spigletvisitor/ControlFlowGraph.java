/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spigletvisitor;

import com.sun.org.apache.bcel.internal.generic.LLOAD;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import sun.security.krb5.internal.KDCOptions;

/**
 *
 * @author d3m
 */
public class ControlFlowGraph {

    private List<BasicBlock> bb;
    private int _maxArgs;

    private InterferenceGraph G;
    private int _colors;

    private List<String> _spilledVars;
    private Map<String, Integer> _paintedVars;//from temp to color
    private Map<String, String> _registers;//from temp to register
    private Map<String, Integer> _stackRegisters;

    public ControlFlowGraph() {
	bb = new ArrayList<BasicBlock>();
	bb.add(new BasicBlock());
	G = new InterferenceGraph();
	_paintedVars = new HashMap<String, Integer>();
	_spilledVars = new ArrayList<String>();
	_stackRegisters = new HashMap<String, Integer>();
	_registers = new HashMap<String, String>();
	_colors = 18;//number of general purpose registers
    }


    void newBlock(){
	if (bb.get(bb.size()-1).getStatementsNo() != 0) //in case of label after a jump statement
	    bb.add(new BasicBlock());
    }

    void addStatement(Statement stmt){
	int lastblock = bb.size()-1;
	bb.get(lastblock).addStatement(stmt);
	if (stmt.getExpr()!=null){
	    if (stmt.getExpr().getType().equals("CALL")){
		setMaxArgs(stmt.getExpr().getArgsSize());
	    }
	}
    }

    void setMaxArgs(int n){
	if (n>_maxArgs)
	    _maxArgs = n;
    }

    String getCode(){
	String code = "";
	for(int i=0; i<bb.size(); i++){
	    String blockCode = bb.get(i).getBlockCode();
	    code += blockCode;
	}
	return code;
    }

    String getCodeWithSets(){
	String code = "";
	for(int i=0; i<bb.size(); i++){
	    String blockCode = bb.get(i).getBlockCodeWithSets();
	    code += blockCode;
	    code += "++++++++++++++++++++++\n";
	}
	return code;
    }


    
    String getKangaCode(){

	Map<Integer, String> colReg = new HashMap<Integer,String>();
	int tr=0;
	int sr=0;
	for (int i=0; i<19; i=i+2 ){
	    colReg.put(i, "t"+tr);tr++;
	    if (sr<8){
	    colReg.put(i+1, "s"+sr);sr++;}
	}
	colReg.put(-1, "v0");

	List<String> sregs = new ArrayList<String>();
	for(Map.Entry<String, Integer> entry: _paintedVars.entrySet()){
	    String temp = entry.getKey();
	    String reg = colReg.get(entry.getValue());
	    if (reg.contains("s") && !sregs.contains(reg))
		sregs.add(reg);
	    _registers.put(temp, reg);
	}

	String code = "";
	String functionLabel = "";
	int maxSpills = 0;
	int argsNum = 0;
	int spillNum = -1;
	boolean isFunction = false;
	for(int i=0; i<bb.size(); i++){
	    BasicBlock b = bb.get(i);
	    List<Statement> stmnts = b.getStmnts();
	    for(int j=0; j<stmnts.size(); j++){
		Statement s = stmnts.get(j);
		if (s.getType().equals("MAIN")){
		    code = "MAIN [0]["+_spilledVars.size()+"]["+_maxArgs+"]\n";
		}
		else if (s.getType().equals("FUNCTION")){
		    isFunction = true;
		    argsNum = Integer.parseInt(s.getInteger());

//		    for(int k=0; k<_spilledVars.size(); k++){
//			String temp = _spilledVars.get(k);
//			String reg = _registers.get("TEMP "+k);
//			code += "ASTORE SPILLEDARG "+k+" "+reg+"\n";
//		    }

		    functionLabel = s.getLabel()+" ["+argsNum+"][";
		    int ii;
//		    for(ii=0; ii<sregs.size();ii++){
//			code += "ASTORE SPILLEDARG "+ii+" "+sregs.get(ii)+"\n";
//			_stackRegisters.put(sregs.get(ii), ii);
//		    }
//		    spillNum = ii-1;
		    if (argsNum<=4) {
			for(ii=0; ii<sregs.size();ii++){
			    spillNum++;
			    code += "ASTORE SPILLEDARG "+spillNum+" "+sregs.get(ii)+"\n";
			    _stackRegisters.put(sregs.get(ii), spillNum);
			}
		    }
		    else if (argsNum>4){
			//spillNum = argsNum-5;
			for(ii=4; ii<argsNum;ii++) {
			    String reg = _registers.get("TEMP "+ii);
			    if (reg!=null){
				spillNum++;
				_stackRegisters.put("TEMP "+ii, spillNum);
			    }
			}

			for(ii=0; ii<sregs.size();ii++) {
				spillNum++;
				code += "ASTORE SPILLEDARG "+spillNum+" "+sregs.get(ii)+"\n";
				_stackRegisters.put(sregs.get(ii), spillNum);
			    }
			}
		    for(ii=4; ii<_spilledVars.size();ii++) {
			spillNum++;
			_stackRegisters.put(_spilledVars.get(ii), spillNum);
		    }

		    for (int k=0; k<4; k++){
			String reg = _registers.get("TEMP "+k);
			if (reg!=null)
			    code += "MOVE "+reg+" "+"a"+k+"\n";
		    }
		}
		else if (s.getType().equals("ERROR")){
		    code += "ERROR\n";

		}
		else if (s.getType().equals("CJUMP")){
		    String temp = s.getAddress1();
		    String reg = "";
		    if (_stackRegisters.containsKey(temp)){
			code += "ALOAD v1 SPILLEDARG "+_stackRegisters.get(temp)+"\n";
			reg = "v1";
		    }
		    else
			reg = _registers.get(temp);
		    code += "CJUMP "+reg+" "+s.getLabel()+"\n";

		}
		else if (s.getType().equals("JUMP")){
		    code += "JUMP "+s.getLabel()+"\n";
		}
		else if (s.getType().equals("HSTORE")){
		    String temp1 = s.getAddress1();
		    String reg1 = "";
		    String temp2 = s.getAddress2();
		    String reg2 = "";
		    String offset = s.getInteger();
		    if (_stackRegisters.containsKey(temp1)){
			code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp1)+"\n";
			reg1 = "v0";
		    }
		    else
			reg1 = _registers.get(temp1);

		    if (_stackRegisters.containsKey(temp2)){
			code += "ALOAD v1 SPILLEDARG "+_stackRegisters.get(temp2)+"\n";
			reg2 = "v1";
		    }
		    else
			reg2 = _registers.get(temp2);
		    code += "HSTORE "+reg1+" "+offset+" "+reg2+"\n";
		}
		else if (s.getType().equals("HLOAD")){
		    String temp1 = s.getAddress1();
		    String reg1 = "";
		    String temp2 = s.getAddress2();
		    String reg2 = "";
		    String offset = s.getInteger();
		    if (_stackRegisters.containsKey(temp1)){
			code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp1)+"\n";
			reg1 = "v0";
		    }
		    else
			reg1 = _registers.get(temp1);

		    if (_stackRegisters.containsKey(temp2)){
			code += "ALOAD v1 SPILLEDARG "+_stackRegisters.get(temp2)+"\n";
			reg2 = "v1";
		    }
		    else
			reg2 = _registers.get(temp2);
		    code += "HLOAD "+reg1+" "+reg2+" "+offset+"\n";
		}
		else if (s.getType().equals("MOVE")){

		    if (s.getExpr().getType().equals("CALL")) {
			List<String> out = b.getOutSets().get(i);

			for (int ii=0; ii<out.size();ii++){
			    String reg = _registers.get(out.get(ii));
			    //if (reg.contains("t")){
				spillNum++;
				code += "ASTORE SPILLEDARG "+spillNum+" "+reg+"\n";
				_stackRegisters.put(reg, spillNum);
			    //}
			}

			List<String> args = s.getExpr().getArgs();
			if (args.size()<=4) {
			    for(int k=0; k<args.size(); k++){
				String arg = args.get(k);
				String reg = _registers.get(arg);
				code += "MOVE a"+k+" "+reg+"\n";
			    }
			}
			else {
			    for(int k=0; k<4; k++){
				String arg = args.get(k);
				String reg = _registers.get(arg);
				code += "MOVE a"+k+" "+reg+"\n";
			    }
			    for(int k=1; k<=args.size()-4; k++){
				String arg = args.get(k+3);
				String reg = _registers.get(arg);
				code += "PASSARG "+k+" "+reg+"\n";
			    }
			}
			String temp = s.getExpr().getToken1();
			String reg = _registers.get(temp);
			code += "CALL "+reg+"\n";
			temp = s.getAddress1();
			reg = _registers.get(temp);

			//sp=sregs.size();
			for (int ii=0; ii<out.size();ii++){
			    String reg1 = _registers.get(out.get(ii));
			    //if (reg1.contains("t")){
				code += "ALOAD "+reg1+" SPILLEDARG "+_stackRegisters.get(reg1)+"\n";
			    //}
			}
			code += "MOVE "+reg+" v0\n";

		    }
		    else if (s.getExpr().getType().equals("HALLOCATE")){
			String temp = s.getAddress1();
			String reg="";
			if (_stackRegisters.containsKey(temp)){
			    code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp)+"\n";
			    reg = "v0";
			}
			else
			    reg = _registers.get(temp);
			code += "MOVE "+reg+" HALLOCATE "+s.getExpr().getToken1()+"\n";
		    }
		    else if (s.getExpr().getType().equals("SimpleExp")){
			String temp = s.getAddress1();
			String reg="";
			if (_stackRegisters.containsKey(temp)){
			    code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp)+"\n";
			    reg = "v0";
			}
			else
			    reg = _registers.get(temp);

			String temp1 = s.getExpr().getToken1();
			String reg1 = "";
			if (temp1.contains("TEMP"))
			    if (_stackRegisters.containsKey(temp1)){
				code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp1)+"\n";
				reg1 = "v0";
			    }
			    else
				reg1 = _registers.get(temp1);
			else
			    reg1 = temp1;
			code += "MOVE "+reg+" "+reg1+"\n";
		    }
		    else {/*Bin Op*/
			String temp = s.getAddress1();
			String reg="";
			if (_stackRegisters.containsKey(temp)){
			    code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp)+"\n";
			    reg = "v0";
			}
			else
			    reg = _registers.get(temp);

			String temp1 = s.getExpr().getToken1();
			String reg1;
			if (temp1.contains("TEMP")){
			    if (_stackRegisters.containsKey(temp1)){
				code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp1)+"\n";
				reg1 = "v0";
			    }
			    else
				reg1 = _registers.get(temp1);
			}
			else
			    reg1 = temp1;

			String temp2 = s.getExpr().getToken2();
			String reg2;
			if (temp2.contains("TEMP")){
			    if (_stackRegisters.containsKey(temp2)){
				code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp2)+"\n";
				reg2 = "v1";
			    }
			    else
				reg2 = _registers.get(temp2);
			}
			else
			    reg2 = temp2;
			code += "MOVE v0 "+s.getExpr().getType()+" "+reg1+" "+reg2+"\n";
			code += "MOVE "+reg+" v0\n";
		    }
		}
		else if (s.getType().equals("PRINT")){
		    String temp = s.getAddress1();
		    String reg = "";
		    if (_stackRegisters.containsKey(temp)){
			code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp)+"\n";
			reg = "v0";
		    }
		    else
			reg = _registers.get(temp);
		    code += "PRINT "+reg+"\n";
		}
		else if (s.getType().equals("RETURN")){
		    String temp;
		    String reg;
		    if (!s.getAddress1().equals("")){
			temp = s.getAddress1();
			if (_stackRegisters.containsKey(temp)){
			    code += "ALOAD v0 SPILLEDARG "+_stackRegisters.get(temp)+"\n";
			    reg = "v0";
			}
			else
			    reg = _registers.get(temp);
			code += "MOVE v0 "+reg+"\n";
		    }
		    else if (!s.getInteger().equals("")){
			code += "MOVE v0 "+s.getInteger()+"\n";
		    }

		}
		else if (s.getType().equals("LABEL")){
		    code += s.getLabel()+" NOOP\n";
		}
		else if (s.getType().equals("END")){

		    if (isFunction){

			for(int ii=0; ii<sregs.size();ii++){
			    code += "ALOAD "+sregs.get(ii)+" SPILLEDARG "+_stackRegisters.get(sregs.get(ii))+"\n";
			}
			code = functionLabel +(spillNum+1)+"]["+_maxArgs+"]\n"+code;
		    }
		    code += "END\n\n";

		}
	    }
	}
	return code;
    }



    void addEdges(){

	for(int i=0; i<bb.size(); i++){
	    BasicBlock currentBlock = bb.get(i);

	    if (currentBlock.getLastStatementType().equals("JUMP")){
		String label = currentBlock.getLastStatementLabel();
		BasicBlock labelBlock;
		for(int j=0; j<bb.size(); j++){
		    labelBlock = bb.get(j);
		    if (labelBlock.getFirstStatementLabel().equals(label)){
			currentBlock.setChild1(labelBlock);
			break;
		    }
		}
	    }
	    else if (currentBlock.getLastStatementType().equals("CJUMP")){
		currentBlock.setChild1(bb.get(i+1));
		String label = currentBlock.getLastStatementLabel();
		BasicBlock labelBlock;
		for(int j=0; j<bb.size(); j++){
		    labelBlock = bb.get(j);
		    if (labelBlock.getFirstStatementLabel().equals(label)){
			currentBlock.setChild2(labelBlock);
			break;
		    }
		}
	    }
	    else if (!currentBlock.getLastStatementType().equals("END")) {
		currentBlock.setChild1(bb.get(i+1));
	    }
	}
    }

    void computeSets(){
	boolean changed = true;
	
	while (changed){//until no new changes in sets
	    changed = false;
	    for(int i=0; i<bb.size(); i++){ //for each instuction
		if ( bb.get(i).computeSets() )
		    changed = true;
	    }
	    
	    for(int i=0; i<bb.size(); i++){ //for each basic block
		if ( bb.get(i).computeBlockOutSet() )
		    changed = true;
	    }

	}
    }

    void initializeGraph() {
	//for every in and out set build the interference graph
	int size = bb.size();
	for(int bi=0; bi<bb.size(); bi++){
	    BasicBlock b = bb.get(bi);
	    List< List<String> > ins = b.getInSets();
	    for (int i=0; i<ins.size();i++){
		List<String> in = ins.get(i);
		for (int j=0; j<in.size();j++){
		    G.addVertex(in.get(j));
		}
		for (int j=0; j<in.size();j++){
		    for (int k=j; k<in.size();k++){
			if (j != k)
			    G.addNeighbors(in.get(j), in.get(k));
		    }
		}
	    }

	    List< List<String> > outs = b.getOutSets();
	    for (int i=0; i<outs.size();i++){
		List<String> out = outs.get(i);
		for (int j=0; j<out.size();j++){
		    G.addVertex(out.get(j));
		}
		for (int j=0; j<out.size();j++){
		    for (int k=j; k<out.size();k++){
			if (j != k)
			    G.addNeighbors(out.get(j), out.get(k));
		    }
		}
	    }
	}
    }

    void runChaitinAlgorithm() {
	Stack<Vertex> s = new Stack();
	int k = _colors;
	while(true) {
	    while(true) {
		Vertex v = G.getMaxVertex(k);
		if (v == null)
		    break;
		G.removeVertex(v);
		s.push(v);
	    }

	    if (G.isEmpty()){
		break;
	    }
	    else {//spill the live range
		k++;
		while(true) {
		    Vertex v = G.getMaxVertex(k);
		    if (v == null) {
			k++;
			continue;
		    }
		    G.removeVertex(v);
		    _spilledVars.add(v.getVar());
		}
	    }
	}

	while(!s.empty()){
	    Vertex ver = s.pop();
	    int c = ver.paintVertex(_colors);
	    _paintedVars.put(ver.getVar(), c);
	}

//	System.out.println("\nRegisters:\n");
//	System.out.println(_paintedVars.toString());
//	System.out.println("\nSpilled Vars:\n");
//	System.out.println(_spilledVars.toString());
//	System.out.println("\n");
    }

}
