package minijavavisitor;
import java.util.logging.Level;
import java.util.logging.Logger;
import visitor.*;
import syntaxtree.*;
import java.util.*;

/**
 *
 * @author d3m
 */
public class MiniJavaPigletVisitor extends DepthFirstVisitor{

    private SymbolTable _st;
    private TempSymbolTable  _tempSt;
    private TempSymbolTable _tempArgSt;
    

    private Map<String, Vtable> _classVtables;
    private Map<String, ClassFields> _classFields;
    
    private int _labelCount;
    private int _tempCount;

    private int _argsCount;

    String _code;
    String _returnedCode;
    String _returnedTemp;
    String _returnedType;
    String _varDeclType;

    private String _lastClassname;
    private boolean _thisField;
    
    String new_Temp(){
	_tempCount++;
	return "TEMP "+_tempCount;
    }

    String new_Label(){
	_labelCount++;
	return "LABEL"+_labelCount;
    }

    public MiniJavaPigletVisitor(SymbolTable st) {
	_st= st;
	_labelCount = 0;
	_tempCount = 0;

	_code = "";

	_classVtables = new HashMap<String, Vtable>();
	_classFields = new HashMap<String, ClassFields>();
	tablesInit();
    }

    private void tablesInit(){
	Map<String, SymbolType> map = _st.getSymbolTable();
	String className;
	/* for every declared class */
	for( Map.Entry<String, SymbolType> symbol : map.entrySet()){
	    SymbolTypeClass thisClass = null;
	    if (symbol.getValue() instanceof SymbolTypeClass){
		className = symbol.getKey();
		thisClass = (SymbolTypeClass) symbol.getValue();
	    }
	    else
		continue;

	    SymbolTypeClass symbolClass = null;
	    try {
		symbolClass = _st.getClass(className);
	    } catch (Exception ex) {
		Logger.getLogger(MiniJavaPigletVisitor.class.getName()).log(Level.SEVERE, null, ex);
	    }

	    Map<String, List> methods = symbolClass.getMethods();
	    Vtable vt = new Vtable();

	    if (thisClass.isSubClass()){
		for( Map.Entry<String, List> method : thisClass.getMotherClass()._methods.entrySet()){
		    if(methods.containsKey(method.getKey()))
			continue;
		    vt.addMethod(method.getKey(), className + "_"+method.getKey(),method.getValue().size());
		    if (method.getValue().size() > _tempCount)
			_tempCount = method.getValue().size();
		}
	    }

	    /* create vtable for class */
	    for( Map.Entry<String, List> method : methods.entrySet()){
		vt.addMethod(method.getKey(), className + "_"+method.getKey(),method.getValue().size());
		if (method.getValue().size() > _tempCount)
		    _tempCount = method.getValue().size();
	    }

	    _classVtables.put(className, vt);

	    ClassFields thisClassfields = new ClassFields();

	    if (thisClass.isSubClass()){
		Map<String, SymbolTypePrimitive> pfields = symbolClass.getMotherClass().getDataMembersPrimitive();
		for( Map.Entry<String, SymbolTypePrimitive> pfield : pfields.entrySet()){
		    thisClassfields.addPrimitiveField(pfield.getKey());
		}

		Map<String, SymbolTypeClass> cfields = symbolClass.getMotherClass().getDataMembersClass();
		for( Map.Entry<String, SymbolTypeClass> cfield : cfields.entrySet()){
		    thisClassfields.addClassField(cfield.getKey());
		}

	    }

	    Map<String, SymbolTypePrimitive> pfields = symbolClass.getDataMembersPrimitive();
	    for( Map.Entry<String, SymbolTypePrimitive> pfield : pfields.entrySet()){
		if ((thisClass.isSubClass())&&(thisClass.getMotherClass().hasField(pfield.getKey()))){
		    continue;
		}
		thisClassfields.addPrimitiveField(pfield.getKey());
	    }

	    Map<String, SymbolTypeClass> cfields = symbolClass.getDataMembersClass();
	    for( Map.Entry<String, SymbolTypeClass> cfield : cfields.entrySet()){
		thisClassfields.addClassField(cfield.getKey());
	    }

	    _classFields.put(className, thisClassfields);
	}

    }

    public void emitCode(String code){
	_code = _code + code;
	//System.out.print(code);
    }

    @Override
    public void visit(NodeList n) {
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
         e.nextElement().accept(this);
   }

@Override   public void visit(NodeListOptional n) {
      if ( n.present() )
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
            e.nextElement().accept(this);
   }

@Override   public void visit(NodeOptional n) {
      if ( n.present() )
         n.node.accept(this);
   }

@Override   public void visit(NodeSequence n) {
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
         e.nextElement().accept(this);
   }

@Override   public void visit(NodeToken n) {}

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
@Override   public void visit(Goal n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    @Override
    public void visit(MainClass n) {
	emitCode("MAIN\n");
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
      n.f11.accept(this);
      n.f12.accept(this);
      n.f13.accept(this);
      n.f14.accept(this);
      n.f15.accept(this);
      n.f16.accept(this);
      n.f17.accept(this);
      emitCode("END\n\n");
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
@Override   public void visit(TypeDeclaration n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    @Override
    public void visit(ClassDeclaration n) {
	_lastClassname = n.f1.f0.tokenImage;

	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	n.f3.accept(this);

	n.f4.accept(this);
	n.f5.accept(this);
    }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    @Override
    public void visit(ClassExtendsDeclaration n) {
	_lastClassname = n.f1.f0.tokenImage;
	String _motherClassname = n.f1.f0.tokenImage;
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	n.f3.accept(this);
	n.f4.accept(this);
	n.f5.accept(this);
	n.f6.accept(this);
	n.f7.accept(this);
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public void visit(VarDeclaration n) {
	
	n.f0.accept(this);
	String type = _varDeclType;
	String id = n.f1.f0.tokenImage;
	String temp = new_Temp();
	if (_tempSt!= null) {
	    _tempSt.addIdentifier(id, temp, type);
	}
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
    @Override
    public void visit(MethodDeclaration n) {
	//System.out.println("Method\n");
	String methodID = n.f2.f0.tokenImage;

	Vtable cvt = _classVtables.get(_lastClassname);
	String methodLabel = cvt.getMethod(methodID);

	emitCode("\n\n"+methodLabel+"\n");
	emitCode("\nBEGIN\n");
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	n.f3.accept(this);

	_argsCount = 1;
	/* arguments symbol table */
	_tempArgSt = new TempSymbolTable();
	
	/* var declarations symbol table */
	_tempSt = new TempSymbolTable();

	
	n.f4.accept(this);
	n.f5.accept(this);
	n.f6.accept(this);
	
	n.f7.accept(this);

	n.f8.accept(this);
	n.f9.accept(this);
	_returnedTemp = "";
	String returnTemp = new_Temp();
	emitCode("MOVE "+returnTemp+" ");
	n.f10.accept(this);
	emitCode("\nRETURN\n"+returnTemp+"\n");
	n.f11.accept(this);
	n.f12.accept(this);
	emitCode("END\n");
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
@Override   public void visit(FormalParameterList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    @Override
    public void visit(FormalParameter n) {
	Node nodeType = n.f0.f0.choice;
	if (nodeType instanceof IntegerType) {
	    _varDeclType = "int";
	} else if (nodeType instanceof BooleanType) {
	    _varDeclType = "boolean";
	} else if (nodeType instanceof ArrayType) {
	    _varDeclType = "array";
	} else if (nodeType instanceof Identifier) {
	    _varDeclType = ((Identifier) nodeType).f0.tokenImage;
	}
	_tempArgSt.addIdentifier(n.f1.f0.tokenImage,String.valueOf(_argsCount) , _varDeclType);
	_argsCount++;
	return;
	//n.f0.accept(this);
	//n.f1.accept(this);
   }

   /**
    * f0 -> ( FormalParameterTerm() )*
    */
@Override   public void visit(FormalParameterTail n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
@Override   public void visit(FormalParameterTerm n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
@Override   public void visit(Type n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
@Override   public void visit(ArrayType n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      _varDeclType = "array";
   }

   /**
    * f0 -> "boolean"
    */
@Override   public void visit(BooleanType n) {
      n.f0.accept(this);
      _varDeclType = "boolean";
   }

   /**
    * f0 -> "int"
    */
@Override   public void visit(IntegerType n) {
      n.f0.accept(this);
      _varDeclType = "int";
   }

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    @Override
    public void visit(Statement n) {
	//System.out.println("Statement----------------------------------------------");
	n.f0.accept(this);
	//System.out.println("End of Statement----------------------------------------------");
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
@Override   public void visit(Block n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public void visit(AssignmentStatement n) {
	//System.out.println(" Assignment\n");
	String id = n.f0.f0.tokenImage;
	String tempID = null;
	String expr = new_Temp();
	
	emitCode("\nMOVE "+expr+" ");
	n.f2.accept(this);
	
	if (_tempArgSt.contains(id)) {
	    tempID = "TEMP "+_tempArgSt.getTempVar(id);
	    emitCode("\nMOVE "+tempID+" "+expr+"\n");
	}
	else if (_tempSt.contains(id)){
	    tempID = _tempSt.getTempVar(id);
	    emitCode("\nMOVE "+tempID+" "+expr+"\n");
	}
	else if (_classFields.get(_lastClassname).contains(n.f0.f0.tokenImage)){
	    int offset = _classFields.get(_lastClassname).getOffset(id);
	    emitCode("HSTORE TEMP 0 "+offset+" "+expr+"\n");
	    String temp = new_Temp();

	}
	else{
	    //System.out.println("ERROR!!!!!!!!!!!!!!!!!!!!!!!!!: "+id);
	}
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    @Override
    public void visit(ArrayAssignmentStatement n) {
	//System.out.println(" ArrayAssignmentStatement\n");
	///////////////////////////////////////////////////

	String array = new_Temp();
	emitCode("MOVE "+array+" ");
	n.f0.accept(this);

	n.f1.accept(this);

	String index = new_Temp();
	emitCode("MOVE "+index+" ");
	n.f2.accept(this);
	
	n.f3.accept(this);
	n.f4.accept(this);
	
	String value = new_Temp();
	emitCode("MOVE "+value+" ");
	n.f5.accept(this);
	String arrayItem = new_Temp();
	emitCode("HSTORE PLUS "+array+" PLUS 4 TIMES 4 "+index+" 0 "+value+"\n");
	
	n.f6.accept(this);
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    @Override
    public void visit(IfStatement n) {
	//System.out.println(" IfStatement\n");
	n.f0.accept(this);
	n.f1.accept(this);
	String expression = new_Temp();
	emitCode("MOVE "+expression+" ");
	String label1 = new_Label();
	String label2 = new_Label();

	n.f2.accept(this);//expression

	emitCode("CJUMP "+ expression +" "+ label1+"\n");
	n.f3.accept(this);
	n.f4.accept(this);
	emitCode("JUMP "+" "+label2+"\n");
	emitCode(label1+" NOOP\n");
	n.f5.accept(this);
	n.f6.accept(this);
	emitCode(label2+" NOOP\n");
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()e
    */
    @Override
    public void visit(WhileStatement n) {
	//System.out.println(" whilestatement\n");

	String expression = new_Temp();
	
	String label1 = new_Label();
	String label2 = new_Label();
	
	emitCode(label2+" NOOP\n");
	emitCode("MOVE "+expression);
	n.f2.accept(this);
	emitCode("CJUMP "+ expression +" "+ label1+"\n");
	
	n.f3.accept(this);
	n.f4.accept(this);
	emitCode("JUMP "+" "+label2+"\n");
	emitCode(label1+" NOOP\n");
   }

   /**
    * f0 -> "//System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    @Override
    public void visit(PrintStatement n) {
	emitCode(" PRINT ");
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	n.f3.accept(this);
	n.f4.accept(this);
   }

   /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | PrimaryExpression()
    */
    @Override
    public void visit(Expression n) {
	//System.out.println(" Expression\n");
	emitCode("\nBEGIN\n");
	String temp = new_Temp();
	emitCode("MOVE "+temp+"\n");
	n.f0.accept(this);
	emitCode(" RETURN\n");
	emitCode(" "+temp+"\n");
	emitCode("END\n");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
    @Override
    public void visit(AndExpression n) {
	//System.out.println(" AndExpression\n");
	String temp1 = new_Temp();
	String temp2 = new_Temp();
	String returnTemp = new_Temp();
	String label1 = new_Label();
	String label2 = new_Label();
	
	emitCode("\nBEGIN\n");

	emitCode("MOVE "+temp1+"\n");
	n.f0.accept(this);

	emitCode("CJUMP "+ temp1 +" "+ label1+"\n");
	n.f1.accept(this);

	emitCode("MOVE "+temp2+"\n");
	n.f2.accept(this);
	emitCode("MOVE "+returnTemp+" " + temp2 + "\n");
	emitCode("JUMP "+label2+"\n");

	emitCode(label1+" NOOP\n");
	emitCode("MOVE "+returnTemp+" " + temp1 + "\n");
	emitCode(label2+" NOOP\n");
	emitCode(" RETURN\n");
	emitCode(" "+returnTemp+"\n");
	emitCode("END\n");
	_returnedTemp = returnTemp;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public void visit(CompareExpression n) {
	//System.out.println(" CompareExpression\n");
	String temp1 = new_Temp();
	String temp2 = new_Temp();
	String temp = new_Temp();
	emitCode("\nBEGIN\n");
	emitCode("MOVE "+temp1+" ");
	n.f0.accept(this);
	n.f1.accept(this);
	emitCode("MOVE "+temp2+" ");
	n.f2.accept(this);
	emitCode("MOVE "+temp+" LT "+temp1+" "+temp2+"\n");
	emitCode("RETURN\n");
	emitCode(temp+"\n");
	emitCode("END\n");
	_returnedTemp = temp;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public void visit(PlusExpression n) {
	//System.out.println(" PlusExpression\n");
	String temp1 = new_Temp();
	String temp2 = new_Temp();
	String temp = new_Temp();
	emitCode("\nBEGIN\n");
	emitCode("MOVE "+temp1+" ");
	n.f0.accept(this);
	n.f1.accept(this);
	emitCode("MOVE "+temp2+" ");
	n.f2.accept(this);
	emitCode("MOVE "+temp+" PLUS "+temp1+" "+temp2+"\n");
	emitCode("RETURN\n");
	emitCode(temp);
	emitCode("END\n");
	_returnedTemp = temp;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public void visit(MinusExpression n) {
	//System.out.println(" MinusExpression\n");
	String temp1 = new_Temp();
	String temp2 = new_Temp();
	String temp = new_Temp();
	emitCode("\nBEGIN\n");
	emitCode("MOVE "+temp1+" ");
	n.f0.accept(this);
	n.f1.accept(this);
	emitCode("MOVE "+temp2+" ");
	n.f2.accept(this);
	emitCode("MOVE "+temp+" MINUS "+temp1+" "+temp2+"\n");
	emitCode("RETURN\n");
	emitCode(temp);
	emitCode("END\n");
	_returnedTemp = temp;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public void visit(TimesExpression n) {
	//System.out.println(" TimesExpression\n");
	String temp1 = new_Temp();
	String temp2 = new_Temp();
	String temp = new_Temp();
	emitCode("\nBEGIN\n");
	emitCode("MOVE "+temp1+" ");
	n.f0.accept(this);
	n.f1.accept(this);
	emitCode("MOVE "+temp2+" ");
	n.f2.accept(this);
	emitCode("MOVE "+temp+" TIMES "+temp1+" "+temp2+"\n");
	emitCode("RETURN\n");
	emitCode(temp+"\n");
	emitCode("END\n");
	_returnedTemp = temp;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public void visit(ArrayLookup n) {
	//System.out.println("ArrayLookup\n");
	emitCode("BEGIN\n");
	String array = new_Temp();
	emitCode("MOVE "+array+" ");
	n.f0.accept(this);
	

	n.f1.accept(this);

	String index = new_Temp();
	emitCode("MOVE "+index+" ");
	n.f2.accept(this);
	String size = new_Temp();
	String label = new_Label();
	emitCode("HLOAD "+size+" "+array+" 0\n");
	//to do
	//check out of bounds or null

	String value = new_Temp();
	emitCode("HLOAD "+value+" PLUS "+array+" TIMES 4 "+index+" 4\n");
	emitCode("RETURN\n");
	emitCode(" "+value+"\n");
	emitCode("END\n");
	n.f3.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public void visit(ArrayLength n) {
	//System.out.println("ArrayLength\n");

	emitCode("BEGIN\n");

	String array = new_Temp();
	emitCode("MOVE "+array+" ");
	n.f0.accept(this);

	String index = new_Temp();

	String length = new_Temp();
	//to do
	//check null

	emitCode("HLOAD "+length+" "+array+" 0\n");
	emitCode("RETURN\n");
	emitCode(" "+length+"\n");
	emitCode("END\n");
	
	n.f1.accept(this);
	n.f2.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    @Override
    public void visit(MessageSend n) {
	//System.out.println("Message Send-------------------------------------------------------------------------------\n");
	emitCode("\nCALL\n");
	emitCode("BEGIN\n");
	String object = new_Temp();
	emitCode("MOVE "+object+" ");
	n.f0.accept(this);

	n.f1.accept(this);
	String type = _returnedType;
	String methodID = n.f2.f0.tokenImage;
	String methodTemp = new_Temp();

	Vtable vt = _classVtables.get(type);
	String methodLoadCode = vt.getMethodLoadCode(object,methodID, new_Temp(), methodTemp);
	emitCode(methodLoadCode);
	emitCode(" RETURN\n");
	emitCode(" "+methodTemp+"\n");
	emitCode("END\n");

	if (n.f0.f0.choice instanceof ThisExpression)
	    _thisField = true;
	
	n.f2.accept(this);
	_thisField = false;

	String funcId = n.f2.f0.tokenImage;
	//to do
	//check null

	n.f3.accept(this);
	emitCode("( "+object+" ");
	n.f4.accept(this);
	emitCode(")\n");
	n.f5.accept(this);
	_returnedTemp = methodTemp;
	//System.out.println("Message Send END-------------------------------------------------------------------------------\n");
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
@Override   public void visit(ExpressionList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> ( ExpressionTerm() )*
    */
@Override   public void visit(ExpressionTail n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
@Override   public void visit(ExpressionTerm n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
    @Override
    public void visit(PrimaryExpression n) {
	//System.out.println("PrimaryExpression\n");
	_returnedCode = "";
	String temp = new_Temp();
	emitCode("\nBEGIN\n ");
	emitCode("MOVE "+temp+" ");

	n.f0.accept(this);
	
	//emitCode(_returnedCode+"\n");
	emitCode(" RETURN\n ");
	emitCode(" "+temp+"\n");
	emitCode("END\n ");
	_returnedTemp = temp;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public void visit(IntegerLiteral n) {
	//System.out.println(" INTEGER ");
      n.f0.accept(this);
      emitCode(" "+n.f0.tokenImage+" ");
      //_returnedCode = " "+n.f0.tokenImage+" ";
      _returnedTemp = n.f0.tokenImage;
      _returnedType = "int";
   }

   /**
    * f0 -> "true"
    */
    @Override
    public void visit(TrueLiteral n) {
	//System.out.println(" TRUE ");
      n.f0.accept(this);
      emitCode(" 1 ");
      _returnedTemp = "1";
      _returnedType = "boolean";
   }

   /**
    * f0 -> "false"
    */
    @Override
    public void visit(FalseLiteral n) {
	//System.out.println(" FALSE ");
	n.f0.accept(this);
	emitCode(" 0 ");
	_returnedTemp = "0";
	_returnedType = "boolean";
   }

   /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public void visit(Identifier n) {
	//System.out.println(" IDENTIFIER ");
	
	n.f0.accept(this);
	_varDeclType = n.f0.tokenImage;
	String temp = null;

	if ((_classFields!=null)&&(_lastClassname!=null)) {
	    if (_classFields.get(_lastClassname).contains(n.f0.tokenImage)){
		//_returnedCode = "PLUS TEMP 0 " + _classFields.get(_lastClassname).getOffset(n.f0.tokenImage);
		int offset = _classFields.get(_lastClassname).getOffset(n.f0.tokenImage);
		emitCode("BEGIN\n");
		String field = new_Temp();
		emitCode("HLOAD "+ field +" TEMP 0 " + offset+"\n");
		emitCode("RETURN\n"+field+"\n");//
		emitCode("END\n");
	    }
	}

	if ((_tempArgSt != null) && !_thisField) {//if we are inside a method, !_thisField indicates a field of class eg this.field
	    if (_tempArgSt.contains(n.f0.tokenImage)){
		temp = "TEMP "+_tempArgSt.getTempVar(n.f0.tokenImage);
		emitCode(" "+temp+" ");
		_thisField = false;
		_returnedType = _tempArgSt.getTempType(n.f0.tokenImage);
	    }
	    else if (_tempSt.contains(n.f0.tokenImage)){
		temp = _tempSt.getTempVar(n.f0.tokenImage);
		emitCode(" "+temp+" ");
		_thisField = false;
		_returnedType = _tempSt.getTempType(n.f0.tokenImage);
	    }
	    _returnedTemp = temp;
	    //_returnedCode = " "+temp+" ";
	}

	SymbolTypeClass stc;
	    try {
		stc = _st.getClass(n.f0.tokenImage);
		_returnedType = stc.getType();
	    } catch (Exception ex) {
	    }
	

   }

   /**
    * f0 -> "this"
    */
    @Override
    public void visit(ThisExpression n) {
	//System.out.println(" THIS ");
      n.f0.accept(this);
      emitCode(" TEMP 0 ");
      _returnedTemp = "TEMP 0";
      _returnedType = _lastClassname;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
@Override
    public void visit(ArrayAllocationExpression n) {
	//System.out.println(" ArrayAllocationExpression \n");
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	String size = new_Temp();
	String array = new_Temp();
	emitCode("BEGIN\n");
	emitCode("MOVE "+ size +" ");
	n.f3.accept(this);
	n.f4.accept(this);
	emitCode("MOVE "+array+" HALLOCATE  TIMES PLUS "+size+" 1 4\n");

	String temp2 = new_Temp();
	String label1 = new_Label();
	String label2 = new_Label();
	emitCode("MOVE "+temp2+" 4\n");
	emitCode(label1+" CJUMP LT "+temp2+" TIMES  PLUS "+size+" 1 4 "+label2+"\n");
	emitCode("HSTORE PLUS "+array+" "+temp2+" 0 0\n");
	emitCode("MOVE "+temp2+" PLUS "+temp2+" 4\n");
	emitCode("JUMP "+label1+"\n");
	emitCode(label2+" NOOP\n");
	emitCode("HSTORE "+array+" 0 TIMES "+size+" 4\n");
	emitCode("RETURN\n"+array+"\n");
	emitCode("END\n");
	_returnedType = "array";
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public void visit(AllocationExpression n) {
	//System.out.println(" ObjectAllocationExpression \n");
	n.f0.accept(this);
	String id = n.f1.f0.tokenImage;
	_returnedType = id;
	////System.out.print(" "+id+" \n");
	Vtable vt = _classVtables.get(id);
	//System.out.println("Allocation Code==============================");
	String tempVtable = new_Temp();
	String allocCode = vt.getAllocationCode(tempVtable);

	ClassFields cf = _classFields.get(id);
	String tempObject = new_Temp();
	String label = new_Label();
	String fieldsInitCode = cf.getInitCode(tempObject, new_Temp(), new_Label(), label);
	//MOVE TEMP 56 HALLOCATE  28
	String code = "\nBEGIN\n";
	int size = cf.getSize()+4;
	code += "MOVE "+tempObject+" HALLOCATE "+ size+"\n";
	code += allocCode;
	code += fieldsInitCode;
	if (!fieldsInitCode.equals(""))
	    code += label+" ";
	code += "HSTORE "+tempObject+" 0 "+tempVtable+"\n";
	code += "RETURN\n"+tempObject+"\nEND\n";
	//s//System.out.println(code);
	n.f1.accept(this);
	n.f2.accept(this);
	n.f3.accept(this);
	emitCode(code);
	//System.out.println("=============================================");
	//_returnedCode = code;

   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
    @Override
    public void visit(NotExpression n) {
	//System.out.println(" NotExpression\n");
	emitCode("BEGIN\n");
	String temp = new_Temp();
	emitCode("MOVE "+temp+" ");
	n.f0.accept(this);
	n.f1.accept(this);
	emitCode("MOVE "+temp+" LT "+temp+" 1\n");
	emitCode("RETURN\n");
	emitCode(" "+temp+"\n");
	emitCode("END\n");
	_returnedType = "boolean";
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public void visit(BracketExpression n) {
	//System.out.println(" BracketExpression\n");
	emitCode("BEGIN\n");
	String temp = new_Temp();
	emitCode("MOVE "+temp+" ");
	n.f0.accept(this);
	n.f1.accept(this);
	n.f2.accept(this);
	emitCode("RETURN\n");
	emitCode(" "+temp+"\n");
	emitCode("END\n");
   }
}
