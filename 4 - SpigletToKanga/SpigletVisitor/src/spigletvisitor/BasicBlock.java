/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spigletvisitor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author d3m
 */
public class BasicBlock {

    private List<Statement> _stmnts;
    private BasicBlock child1;
    private BasicBlock child2;
    private String _label;
    private List<BasicBlock> _ancestorBlocks;

    private List< List<String> > _inSets;
    private List< List<String> > _outSets;
    
    private List<String> _blockInSet;
    private List<String> _blockOutSet;


    public BasicBlock() {
	_stmnts = new ArrayList<Statement>();
	child1 = null;
	child2 = null;
	_ancestorBlocks = new ArrayList<BasicBlock>();

	_inSets = new ArrayList<List<String>>();
	_outSets = new ArrayList<List<String>>();

	_blockInSet = new ArrayList<String>();
	_blockOutSet = new ArrayList<String>();
    }

    void addStatement(Statement stmt){
	_stmnts.add(stmt);
	_inSets.add(new ArrayList<String>());
	_outSets.add(new ArrayList<String>());
    }

    String getBlockCode(){
	String code = "";
	for(int i=0; i<_stmnts.size(); i++){
	    code += _stmnts.get(i).getStatementCode();
	}
	return code;
    }

    String getBlockCodeWithSets(){
	String code = "";
	for(int i=0; i<_stmnts.size(); i++){
	    code += "\nIn: "+_inSets.get(i).toString()+"\n";
	    code += _stmnts.get(i).getStatementCode();
	    code += "Out: "+_outSets.get(i).toString()+"\n";
	}
	return code;
    }

    String getFirstStatementLabel(){
	return _stmnts.get(0).getLabel();
    }

    String getLastStatementLabel(){
	Statement lastStmnt = _stmnts.get(_stmnts.size()-1);
	if (lastStmnt.getType().equals("CJUMP") || lastStmnt.getType().equals("JUMP")){
	    return lastStmnt.getLabel();
	}
	return "";
    }

    public List<Statement> getStmnts() {
	return _stmnts;
    }

    String getFirstStatementType(){
	return _stmnts.get(0).getType();
    }

    String getLastStatementType(){
	return _stmnts.get(_stmnts.size()-1).getType();
    }

    public BasicBlock getChild1() {
	return child1;
    }

    public void setChild1(BasicBlock child1) {
	this.child1 = child1;
	child1.addAncestor(this);
    }

    public BasicBlock getChild2() {
	return child2;
    }

    public void addAncestor(BasicBlock anc){
	_ancestorBlocks.add(anc);
    }

    public void setChild2(BasicBlock child2) {
	this.child2 = child2;
	child2.addAncestor(this);
    }

    public int getStatementsNo(){
	return _stmnts.size();
    }

    boolean computeSets() {
	boolean changed = false;

	Statement lastStmnt = _stmnts.get(_stmnts.size()-1);

	List<String> lastInSet = _inSets.get(_stmnts.size()-1);
	List<String> use = lastStmnt.getUse();
	for(int j=0; j<use.size(); j++){
	    if (!lastInSet.contains(use.get(j))){
		changed = true;
		lastInSet.add(use.get(j));
	    }
	}
	_inSets.add(_stmnts.size()-1, lastInSet);

	//////////////////////////////////////////



	List<String> outSet = _outSets.get(_stmnts.size()-1);
	List<String> def = lastStmnt.getDef();
	for(int j=0; j<def.size(); j++){
	    if (!outSet.contains(def.get(j))){
		changed = true;
		outSet.add(def.get(j));
	    }
	}
	_outSets.add(_stmnts.size()-1, outSet);

	for(int i=_stmnts.size()-2; i>=0; i--){
	    Statement currentInstruction = _stmnts.get(i);
	    Statement succInstruction = _stmnts.get(i+1);
	    List<String> outset = _outSets.get(i);
	    List<String> nextInSet = _inSets.get(i+1);
	    if (outset == null){
		outset = new ArrayList<String>();
	    }

	    List<String> defset = _stmnts.get(i).getDef();
	    for(int j=0; j<defset.size(); j++){
		if (!outset.contains(defset.get(j))){
		    changed = true;
		    outset.add(defset.get(j));
		}
	    }

	    if (nextInSet == null){
		nextInSet = new ArrayList<String>();
	    }
	    /* out[I] = in[succ I] */
	    for(int j=0; j<nextInSet.size(); j++){
		if (!outset.contains(nextInSet.get(j))){
		    changed = true;
		    outset.add(nextInSet.get(j));
		}
	    }
	    _outSets.set(i, outset);


	    /* in[I] = ( out[I]-def[I] ) v use[I] */
	    List<String> tempIn = new ArrayList<String>();
	    for(int j=0; j<outset.size(); j++){
		tempIn.add(outset.get(j));
	    }
	    
	    def = currentInstruction.getDef();
	    for(int j=0; j<def.size(); j++){
		tempIn.remove(def.get(j));
	    }

	    use = currentInstruction.getUse();
	    for(int j=0; j<use.size(); j++){
		if (!tempIn.contains(use.get(j)))
		    tempIn.add(use.get(j));
	    }

	    List<String> inset = _inSets.get(i);
	    
	    for(int j=0; j<tempIn.size(); j++){
		if (!inset.contains(tempIn.get(j))){
		    changed = true;
		    inset.add(tempIn.get(j));
		}
	    }
	}
	/* re-compute block inset */
	for(int j=0; j<_inSets.get(0).size(); j++){
	    if (!_blockInSet.contains(_inSets.get(0).get(j))){
		changed = true;
		_blockInSet.add(_inSets.get(0).get(j));
	    }
	}


	return changed;
    }

    
    boolean computeBlockOutSet(){
	boolean changed = false;
	
	if (child1 != null){
	    List<String> succB1 = child1.getBlockInSet();
	    for(int j=0; j<succB1.size(); j++){
		if (!_blockOutSet.contains(succB1.get(j))){
		    changed = true;
		    _blockOutSet.add(succB1.get(j));
		}
	    }
	}

	if (child2 != null){
	    List<String> succB1 = child2.getBlockInSet();
	    for(int j=0; j<succB1.size(); j++){
		if (!_blockOutSet.contains(succB1.get(j))){
		    changed = true;
		    _blockOutSet.add(succB1.get(j));
		}
	    }
	}
	
	return changed;
    }

    public List<String> getBlockOutSet() {
	return _blockOutSet;
    }

    public List<String> getBlockInSet(){
	return _blockInSet;
    }

    public List<List<String>> getInSets() {
	return _inSets;
    }

    public List<List<String>> getOutSets() {
	return _outSets;
    }


}
