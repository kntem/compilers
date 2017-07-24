package kangavisitor;
import syntaxtree.*;
import visitor.*;

/**
 *
 * @author d3m
 */
public class KangaVisitor extends DepthFirstVisitor{

    String _code="";

    int argsnum;
    int stackLocations;
    int maxArgs;
    int stackAlloc;

    public KangaVisitor() {}

    private void emitCode(String code){
	_code = _code + code;
	//System.out.print(code);
    }

    public String getCode(){
	return _code;
    }

    //
   // User-generated visitor methods below
   //

   /**
    * f0 -> "MAIN"
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    * f12 -> ( Procedure() )*
    * f13 -> <EOF>
    */
    public void visit(Goal n) {
	emitCode("\t.text\nmain:\n\n");
	argsnum = Integer.parseInt(n.f2.f0.tokenImage);
	stackLocations = Integer.parseInt(n.f5.f0.tokenImage);
	maxArgs = Integer.parseInt(n.f8.f0.tokenImage);
	if (argsnum > 4){
	    stackAlloc = (stackLocations - (argsnum-4))*4+4;
	}
	else
	    stackAlloc = stackLocations*4+4;
	emitCode("add $sp, $sp, -"+stackAlloc+"\n");
	emitCode("sw $ra, 4($sp)\n");
	
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	n.f3.accept(this);
	n.f4.accept(this);
	n.f5.accept(this);
	n.f6.accept(this);
	n.f7.accept(this);
	n.f8.accept(this);
	n.f9.accept(this);
	n.f10.accept(this);
	emitCode("lw $ra, 4($sp)\n");
	emitCode("add $sp, $sp, "+stackAlloc+"\n");
	emitCode("jr $ra\n");

	n.f11.accept(this);
	n.f12.accept(this);
	n.f13.accept(this);
	
	emitCode("\n.alloc:\n");
	emitCode("add $v0, $0, 9\n");
	emitCode("syscall\n");
	emitCode("jr $ra\n");
	emitCode("\n.print:\n");
	emitCode("add $v0, $0, 1\n");
	emitCode("syscall\n");
	emitCode("add $a0, $0, 10\n");
	emitCode("add $v0, $0, 11\n");
	emitCode("syscall\n");
	emitCode("jr $ra\n");
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
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    */
    public void visit(Procedure n) {
	String label = n.f0.f0.tokenImage;
	emitCode("\n"+label+":\n");

	argsnum = Integer.parseInt(n.f2.f0.tokenImage);
	stackLocations = Integer.parseInt(n.f5.f0.tokenImage);
	maxArgs = Integer.parseInt(n.f8.f0.tokenImage);
	if (argsnum > 4){
	    stackAlloc = (stackLocations - (argsnum-4))*4+4;
	}
	else
	    stackAlloc = stackLocations*4+4;
	emitCode("add $sp, $sp, -"+stackAlloc+"\n");

	emitCode("sw $ra, 4($sp)\n");
	//n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	n.f3.accept(this);
	n.f4.accept(this);
	n.f5.accept(this);
	n.f6.accept(this);
	n.f7.accept(this);
	n.f8.accept(this);
	n.f9.accept(this);
	n.f10.accept(this);
	
	emitCode("lw $ra, 4($sp)\n");
	emitCode("add $sp, $sp, "+stackAlloc+"\n");
	emitCode("jr $ra\n");
	n.f11.accept(this);
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
    *       | ALoadStmt()
    *       | AStoreStmt()
    *       | PassArgStmt()
    *       | CallStmt()
    */
   public void visit(Stmt n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "NOOP"
    */
   public void visit(NoOpStmt n) {
       emitCode("sll $0, $0, 0\n");
      n.f0.accept(this);
   }

   /**
    * f0 -> "ERROR"
    */
   public void visit(ErrorStmt n) {
       emitCode("syscall\n");
      n.f0.accept(this);
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Reg()
    * f2 -> Label()
    */
   public void visit(CJumpStmt n) {
      n.f0.accept(this);
      n.f1.accept(this);
      //n.f2.accept(this);
      String reg, label;
      reg = "$"+n.f1.f0.choice.toString();
      label = n.f2.f0.tokenImage;
      //System.out.println(label);
      emitCode("bne "+reg+", 1, "+label+"\n");
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public void visit(JumpStmt n) {
      n.f0.accept(this);
      //n.f1.accept(this);
      String label;
      label = n.f1.f0.tokenImage;
      emitCode("j "+label+"\n");
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Reg()
    * f2 -> IntegerLiteral()
    * f3 -> Reg()
    */
   public void visit(HStoreStmt n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      String reg1 = n.f1.f0.choice.toString();
      String i = n.f2.f0.tokenImage;
      String reg2 = n.f3.f0.choice.toString();
      //HSTORE t0 0 t2
      //sw $t2, 0($t0)
      emitCode("sw $"+reg2+", "+i+"($"+reg1+")\n");

   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Reg()
    * f2 -> Reg()
    * f3 -> IntegerLiteral()
    */
   public void visit(HLoadStmt n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      String reg1 = n.f1.f0.choice.toString();
      String reg2 = n.f2.f0.choice.toString();
      String i = n.f3.f0.tokenImage;
      emitCode("lw $"+reg1+" "+i+"($"+reg2+")\n");
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Reg()
    * f2 -> Exp()
    */
    public void visit(MoveStmt n) {

	String reg = n.f1.f0.choice.toString();
	if (n.f2.f0.choice instanceof HAllocate){
	    String size = null;
	    HAllocate h = (HAllocate)n.f2.f0.choice;
	    if (h.f1.f0.choice instanceof  IntegerLiteral)
		size = ((IntegerLiteral)h.f1.f0.choice).f0.tokenImage;
	    else if(h.f1.f0.choice instanceof Reg)
		size = "$" + ((Reg)h.f1.f0.choice).f0.choice.toString();
	    
	    emitCode("add $a0, $0, "+size+"\n");
	    emitCode("jal .alloc\n");
	    emitCode("add $"+reg+", $v0, 0\n");
	}
	else if (n.f2.f0.choice instanceof SimpleExp){
	    SimpleExp se = (SimpleExp)n.f2.f0.choice;
	    if ( se.f0.choice instanceof Label){
		String label = ((Label)se.f0.choice).f0.tokenImage;
		emitCode("la $"+reg+", "+label+"\n");
	    }
	    else if ( se.f0.choice instanceof IntegerLiteral){
		String i = ((IntegerLiteral)se.f0.choice).f0.tokenImage;
		emitCode("add $"+reg+", $0, "+i+"\n");

	    }
	    else if ( se.f0.choice instanceof Reg){
		String reg2 = ((Reg)se.f0.choice).f0.choice.toString();
		emitCode("add $"+reg+", $"+reg2+", 0\n");
	    }
	}
	else if(n.f2.f0.choice instanceof BinOp){
	    BinOp bo = (BinOp)n.f2.f0.choice;
	    String op = bo.f0.f0.choice.toString();
	    String reg2 = bo.f1.f0.choice.toString();

	    SimpleExp se = ((SimpleExp)((BinOp)n.f2.f0.choice).f2);
	    String r3 = null;
	    if ( se.f0.choice instanceof IntegerLiteral){
		r3 = ((IntegerLiteral)se.f0.choice).f0.tokenImage;

	    }
	    else if ( se.f0.choice instanceof Reg){
		r3 = "$"+((Reg)se.f0.choice).f0.choice.toString();
	    }

	    if (op.equals("LT"))
		emitCode("slt $"+reg+", $"+reg2+", "+r3+"\n");
	    else if (op.equals("MINUS"))
		emitCode("sub $"+reg+", $"+reg2+", "+r3+"\n");
	    else if (op.equals("PLUS"))
		emitCode("add $"+reg+", $"+reg2+", "+r3+"\n");
	    else if (op.equals("TIMES"))
		emitCode("mul $"+reg+", $"+reg2+", "+r3+"\n");
	}
   }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   public void visit(PrintStmt n) {
       //add $a0, $0, $t3
	//jal .print
	String simple_exp = null;
	if (n.f1.f0.choice instanceof Reg){
	    simple_exp = ((Reg) n.f1.f0.choice).f0.choice.toString();
	}
	else if(n.f1.f0.choice instanceof IntegerLiteral){
	    simple_exp = ((IntegerLiteral) n.f1.f0.choice).f0.tokenImage;
	}
	emitCode("add $a0, $0, ");
	if (simple_exp.matches("-?\\d+(.\\d+)?")) //if numeric
	    emitCode(simple_exp+"\n");
	else
	    emitCode("$"+simple_exp+"\n");

	emitCode("jal .print\n");
   }

   /**
    * f0 -> "ALOAD"
    * f1 -> Reg()
    * f2 -> SpilledArg()
    */
   public void visit(ALoadStmt n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);

      String reg = n.f1.f0.choice.toString();
      int i = Integer.parseInt(n.f2.f1.f0.tokenImage);
      int sp = (stackLocations - i + 1)*4;
      emitCode("lw $"+reg+", "+sp+"($sp)\n");
   }

   /**
    * f0 -> "ASTORE"
    * f1 -> SpilledArg()
    * f2 -> Reg()
    */
   public void visit(AStoreStmt n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
       String reg = n.f2.f0.choice.toString();
      int i = Integer.parseInt(n.f1.f1.f0.tokenImage);
      int sp = (stackLocations - i)*4+4;
      emitCode("sw $"+reg+", "+sp+"($sp)\n");
   }

   /**
    * f0 -> "PASSARG"
    * f1 -> IntegerLiteral()
    * f2 -> Reg()
    */
    public void visit(PassArgStmt n) {
	int i = Integer.parseInt(n.f1.f0.tokenImage);
	String reg = n.f2.f0.choice.toString();

	i = -(i-1)*4;
	emitCode("sw $"+reg+", "+i+"($sp)\n");
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
    }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    */
    public void visit(CallStmt n) {
	n.f0.accept(this);
	n.f1.accept(this);
	int a;
	if (maxArgs>4){
	    a = (maxArgs-4)*4;
	    emitCode("add $sp, $sp, -"+a+"\n");
	}
	if (n.f1.f0.choice instanceof Reg){
	    String reg = ((Reg)n.f1.f0.choice).f0.choice.toString();
	    emitCode("jalr $"+reg+"\n");
	}
	else if (n.f1.f0.choice instanceof Label){
	    String label = ((Label)n.f1.f0.choice).f0.tokenImage;
	    emitCode("jal $"+label+"\n");
	}

	if (maxArgs>4){
	    a = (maxArgs-4)*4;
	    emitCode("add $sp, $sp, "+a+"\n");
	}
    }

   /**
    * f0 -> HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
   public void visit(Exp n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public void visit(HAllocate n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> Operator()
    * f1 -> Reg()
    * f2 -> SimpleExp()
    */
   public void visit(BinOp n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> "LT"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    */
   public void visit(Operator n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "SPILLEDARG"
    * f1 -> IntegerLiteral()
    */
   public void visit(SpilledArg n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> Reg()
    *       | IntegerLiteral()
    *       | Label()
    */
   public void visit(SimpleExp n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "a0"
    *       | "a1"
    *       | "a2"
    *       | "a3"
    *       | "t0"
    *       | "t1"
    *       | "t2"
    *       | "t3"
    *       | "t4"
    *       | "t5"
    *       | "t6"
    *       | "t7"
    *       | "s0"
    *       | "s1"
    *       | "s2"
    *       | "s3"
    *       | "s4"
    *       | "s5"
    *       | "s6"
    *       | "s7"
    *       | "t8"
    *       | "t9"
    *       | "v0"
    *       | "v1"
    */
   public void visit(Reg n) {
      n.f0.accept(this);
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
	n.f0.accept(this);
	String label = n.f0.tokenImage;
        emitCode(label+": ");
    }

}
