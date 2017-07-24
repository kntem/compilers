/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spigletvisitor;
import com.sun.org.apache.bcel.internal.generic.LASTORE;
import java.util.ArrayList;
import java.util.List;
import syntaxtree.*;
import visitor.*;
/**
 *
 * @author d3m
 */
public class SpigletVisitor extends DepthFirstVisitor{

    private ControlFlowGraph _cfg;
    private List<ControlFlowGraph> _cfgs;

    private Expression _lastExpression;
    private boolean _callArgs;

    public SpigletVisitor() {
	_cfgs = new ArrayList<ControlFlowGraph>();
	_cfgs.add(new ControlFlowGraph());
	_cfg = _cfgs.get(0);
	_lastExpression = new Expression();
	_callArgs = false;
    }

    String getCode(){
	String code = "";
	for(int i=0; i<_cfgs.size(); i++){
	    code += _cfgs.get(i).getCode()+"\n\n";
	}
	return code;
    }

    String getCodeWithSets(){
	String code = "";
	for(int i=0; i<_cfgs.size(); i++){
	    code += _cfgs.get(i).getCodeWithSets()+"\n\n";
	}
	return code;
    }

    String getKangaCode(){
	String code = "";
	for(int i=0; i<_cfgs.size(); i++){
	    code += _cfgs.get(i).getKangaCode()+"\n\n";
	}
	return code;
    }

    void addEdges(){
	for(int i=0; i<_cfgs.size(); i++){
	    _cfgs.get(i).addEdges();
	}
    }

    void computeSets(){
	for(int i=0; i<_cfgs.size(); i++){
	    _cfgs.get(i).computeSets();
	}
    }

    void allocateRegisters(){
	for(int i=0; i<_cfgs.size(); i++){
	    _cfgs.get(i).initializeGraph();
	    _cfgs.get(i).runChaitinAlgorithm();
	}
    }
    

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
    @Override
    public void visit(Goal n) {
	Statement stmnt = new Statement();
	stmnt.setType("MAIN");
	_cfg.addStatement(stmnt);
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	stmnt = new Statement();
	stmnt.setType("END");
	_cfg.addStatement(stmnt);
	n.f3.accept(this);
	n.f4.accept(this);
    }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public void visit(StmtList n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public void visit(Procedure n) {
       _cfgs.add(new ControlFlowGraph());
       _cfg = _cfgs.get(_cfgs.size()-1);
       Statement stmnt = new Statement();
       stmnt.setType("FUNCTION");
       String argsNo = n.f2.f0.tokenImage;
       String stmStr = n.f0.f0.tokenImage;
       stmnt.setLabel(stmStr);
       stmnt.setInteger(argsNo);
       _cfg.addStatement(stmnt);
      //n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
   }

   /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
   public void visit(Stmt n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "NOOP"
    */
   public void visit(NoOpStmt n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "ERROR"
    */
    @Override
    public void visit(ErrorStmt n) {
	Statement stmnt = new Statement();
	stmnt.setType("ERROR");
	_cfg.addStatement(stmnt);
	n.f0.accept(this);
    }

   /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
    public void visit(CJumpStmt n) {
	Statement stmnt = new Statement();
	stmnt.setType("CJUMP");
	stmnt.setAddress1(n.f1.f0.tokenImage+" "+n.f1.f1.f0.tokenImage);
	stmnt.setLabel(n.f2.f0.tokenImage);
	_cfg.addStatement(stmnt);
	_cfg.newBlock();
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
    public void visit(JumpStmt n) {
	Statement stmnt = new Statement();
	stmnt.setType("JUMP");
	stmnt.setLabel(n.f1.f0.tokenImage);
	_cfg.addStatement(stmnt);
	_cfg.newBlock();
    }

   /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
    @Override
    public void visit(HStoreStmt n) {
	Statement stmnt = new Statement();
	stmnt.setType("HSTORE");
	stmnt.setAddress1(n.f1.f0.tokenImage+" "+n.f1.f1.f0.tokenImage);
	stmnt.setInteger(n.f2.f0.tokenImage);
	stmnt.setAddress2(n.f3.f0.tokenImage+" "+n.f3.f1.f0.tokenImage);
	_cfg.addStatement(stmnt);
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
    public void visit(HLoadStmt n) {
	Statement stmnt = new Statement();
	stmnt.setType("HLOAD");
	stmnt.setAddress1(n.f1.f0.tokenImage+" "+n.f1.f1.f0.tokenImage);
	stmnt.setAddress2(n.f2.f0.tokenImage+" "+n.f2.f1.f0.tokenImage);
	stmnt.setInteger(n.f3.f0.tokenImage);
	_cfg.addStatement(stmnt);
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
    public void visit(MoveStmt n) {
	Statement stmnt = new Statement();
	stmnt.setType("MOVE");
	stmnt.setAddress1(n.f1.f0.tokenImage+" "+n.f1.f1.f0.tokenImage);
	_lastExpression = new Expression();
	n.f0.accept(this);
	n.f1.accept(this);
	if (n.f2.f0.choice instanceof SimpleExp){
	    _lastExpression.setType("SimpleExp");
	    SimpleExp se = (SimpleExp)n.f2.f0.choice;
	    if (se.f0.choice instanceof Temp){
		String temp = ((Temp)se.f0.choice).f0.tokenImage +" "+((Temp)se.f0.choice).f1.f0.tokenImage;
		_lastExpression.setToken1(temp);
	    }
	    else if (se.f0.choice instanceof IntegerLiteral){
		String integer = ((IntegerLiteral)se.f0.choice).f0.tokenImage;
		_lastExpression.setToken1(integer);
	    }
	    else if (se.f0.choice instanceof Label){
		String integer = ((Label)se.f0.choice).f0.tokenImage;
		_lastExpression.setToken1(integer);
	    }
	}
	else
	    n.f2.accept(this);
	
	stmnt.setExpr(_lastExpression);

	_cfg.addStatement(stmnt);
   }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
    @Override
    public void visit(PrintStmt n) {
	Statement stmnt = new Statement();
	stmnt.setType("PRINT");
	if (n.f1.f0.choice instanceof Temp){
	    String temp = ((Temp)n.f1.f0.choice).f0.tokenImage+" "+((Temp)n.f1.f0.choice).f1.f0.tokenImage;
	    stmnt.setAddress1(temp);
	}
	else if (n.f1.f0.choice instanceof IntegerLiteral){
	    String integer = ((IntegerLiteral)n.f1.f0.choice).f0.tokenImage;
	    stmnt.setAddress1(integer);
	}
	//n.f0.accept(this);
	//n.f1.accept(this);
	_cfg.addStatement(stmnt);
    }

   /**
    * f0 -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
   public void visit(Exp n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
    public void visit(StmtExp n) {
	Statement stmt = new Statement();
	stmt.setType("BEGIN");
	_cfg.addStatement(stmt);
	//n.f0.accept(this);
	n.f1.accept(this);
	stmt = new Statement();
	stmt.setType("RETURN");
	n.f2.accept(this);
	//n.f3.accept(this);

	if (n.f3.f0.choice instanceof Temp){
	    String temp = ((Temp)n.f3.f0.choice).f0.tokenImage +" "+((Temp)n.f3.f0.choice).f1.f0.tokenImage;
	    stmt.setAddress1(temp);
	}
	else if (n.f3.f0.choice instanceof Label){
	    String label = ((Temp)n.f3.f0.choice).f0.tokenImage;
	    stmt.setLabel(label);
	}
	else if (n.f3.f0.choice instanceof IntegerLiteral){
	    String i = ((IntegerLiteral)n.f3.f0.choice).f0.tokenImage;
	    stmt.setInteger(i);
	}
	_cfg.addStatement(stmt);

	n.f4.accept(this);

	stmt = new Statement();
	stmt.setType("END");
	_cfg.addStatement(stmt);
    }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
    @Override
    public void visit(Call n) {
	_lastExpression.setType("CALL");

	if (n.f1.f0.choice instanceof Temp){
	    String temp = ((Temp)n.f1.f0.choice).f0.tokenImage +" "+((Temp)n.f1.f0.choice).f1.f0.tokenImage;
	    _lastExpression.setToken1(temp);
	}
	else if (n.f1.f0.choice instanceof Label){
	    String label = ((Temp)n.f1.f0.choice).f0.tokenImage;
	    _lastExpression.setToken1(label);
	}

	n.f0.accept(this);
	//n.f1.accept(this);
	n.f2.accept(this);

	_callArgs = true;
	n.f3.accept(this);
	_callArgs = false;
	
	n.f4.accept(this);
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public void visit(HAllocate n) {
       _lastExpression.setType("HALLOCATE");

       if (n.f1.f0.choice instanceof Temp){
	    String temp = ((Temp)n.f1.f0.choice).f0.tokenImage +" "+((Temp)n.f1.f0.choice).f1.f0.tokenImage;
	    _lastExpression.setToken1(temp);
	}
	else if (n.f1.f0.choice instanceof IntegerLiteral){
	    String integer = ((IntegerLiteral)n.f1.f0.choice).f0.tokenImage;
	    _lastExpression.setToken1(integer);
	}
   }

   /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
    @Override
    public void visit(BinOp n) {

	n.f0.accept(this);
	String temp = n.f1.f0.tokenImage +" "+n.f1.f1.f0.tokenImage;
	_lastExpression.setToken1(temp);

	if (n.f2.f0.choice instanceof Temp){
	    String temp1 = ((Temp)n.f2.f0.choice).f0.tokenImage +" "+((Temp)n.f2.f0.choice).f1.f0.tokenImage;
	    _lastExpression.setToken2(temp1);
	}
	else if (n.f2.f0.choice instanceof IntegerLiteral){
	    String integer = ((IntegerLiteral)n.f2.f0.choice).f0.tokenImage;
	    _lastExpression.setToken2(integer);
	}

	//n.f1.accept(this);
	//n.f2.accept(this);
    }

   /**
    * f0 -> "LT"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    */
    @Override
    public void visit(Operator n) {
	String op = ((NodeToken)n.f0.choice).tokenImage;
	_lastExpression.setType(op);
    }

   /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
    @Override
    public void visit(SimpleExp n) {
	n.f0.accept(this);
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
    @Override
    public void visit(Temp n) {
       	if (_callArgs){
	    _lastExpression.addArg("TEMP "+n.f1.f0.tokenImage);
	}
    }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public void visit(IntegerLiteral n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public void visit(Label n) {
       _cfg.newBlock();
       Statement stmnt = new Statement();
       stmnt.setLabel(n.f0.tokenImage);
       stmnt.setType("LABEL");
       _cfg.addStatement(stmnt);
      n.f0.accept(this);
   }
}
