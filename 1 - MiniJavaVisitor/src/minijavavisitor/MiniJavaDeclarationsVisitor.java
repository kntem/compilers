package minijavavisitor;
import syntaxtree.*;
import java.util.*;
import visitor.DepthFirstVisitor;


public class MiniJavaDeclarationsVisitor extends DepthFirstVisitor {
    SymbolTable _st;

    /* Indicates a class field declaration */
    boolean _classVarDeclarations;

    SymbolType _methodType;
    String _methodName;
    List<SymbolType> _parameterList;
    
    private int _currentLine;

    public SymbolTable getSymbolTable() {
	return _st;
    }

    private void displayError(String message){
	System.err.println("Line " + _currentLine + ": " + message);
	System.exit(0);
    }

    /* Type Checking of class fields */
    public void checkDataMembers(){
	try {
	    _st.checkDataMembers();
	} catch (Exception ex) {
	    System.err.println(ex.getMessage());
	    System.exit(0);
	}
    }

    /* Type Checking of methods' arguments and return types */
    public void checkMethods(){
	try {
	    _st.checkMethods();
	} catch (Exception ex) {
	    System.err.println(ex.getMessage());
	    System.exit(0);
	}
    }

    @Override
   public void visit(NodeList n) {
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
         e.nextElement().accept(this);
   }

    @Override
   public void visit(NodeListOptional n) {
      if ( n.present() )
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
            e.nextElement().accept(this);
   }

    @Override
   public void visit(NodeOptional n) {
      if ( n.present() )
         n.node.accept(this);
   }

    @Override
   public void visit(NodeSequence n) {
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
         e.nextElement().accept(this);
   }

    @Override
   public void visit(NodeToken n) {}

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
   public void visit(Goal n) {
      _st = new SymbolTable();
      _classVarDeclarations = false;
      
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
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
   public void visit(TypeDeclaration n) {
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
      n.f0.accept(this);
//      System.out.println("New Class with Name: " + n.f1.f0.tokenImage);

      if (_st.lookup(n.f1.f0.tokenImage)) {
	  displayError("Class name" + n.f1.f0.tokenImage + "already defined.");
      }
      _st.insert(n.f1.f0.tokenImage, new SymbolTypeClass(n.f1.f0.tokenImage));

      n.f1.accept(this);
      n.f2.accept(this);
      _classVarDeclarations = true;
      n.f3.accept(this);
      _classVarDeclarations = false;
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
      n.f0.accept(this);

      //System.out.println("New Class with Name: " + n.f1.f0.tokenImage + " extends " + n.f3.f0.tokenImage);
      if (_st.lookup(n.f1.f0.tokenImage)) {
	  displayError("Class name" + n.f1.f0.tokenImage + "already defined.");
	  System.exit(0);
      }
      String motherClassName = new String(n.f3.f0.tokenImage);
      SymbolTypeClass motherClass = null;
	try {
	    motherClass = _st.getClass(motherClassName);
	} catch (Exception ex) {
	    /* Upper class is not defined */
	    displayError("Class "+n.f1.f0.tokenImage+ " extends "+ motherClassName+ ": " + ex.getMessage());
	}
      SymbolTypeClass symbolClass = new SymbolTypeClass(n.f1.f0.tokenImage, motherClass);
      _st.insert(n.f1.f0.tokenImage, symbolClass );

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
	/* Add only class fields declarations */
      if (_classVarDeclarations) {
	    try {
		String varType = new String();
		Node nodeType = n.f0.f0.choice;
		String identifier = new String(n.f1.f0.tokenImage);
		if (nodeType instanceof IntegerType) {
		    _st.addDataMember(new SymbolTypePrimitive("int", identifier));
		} else if (nodeType instanceof BooleanType) {
		    _st.addDataMember(new SymbolTypePrimitive("boolean", identifier));
		} else if (nodeType instanceof ArrayType) {
		    _st.addDataMember(new SymbolTypePrimitive("array", identifier));
		} else if (nodeType instanceof Identifier) {
		    varType = ((Identifier) nodeType).f0.tokenImage;
		    _st.addDataMember(new SymbolTypeClass(varType, identifier));
		}
	    } catch (Exception ex) {
		displayError(ex.getMessage());
	    }
		
      }
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);

      Node nodeType = n.f1.f0.choice;
      if (nodeType instanceof IntegerType)
	    _methodType = new SymbolTypePrimitive("int");
      else if (nodeType instanceof BooleanType)
	    _methodType = new SymbolTypePrimitive("boolean");
      else if (nodeType instanceof ArrayType)
	    _methodType = new SymbolTypePrimitive("array");
      else if (nodeType instanceof Identifier)
	  _methodType = new SymbolTypeClass(((Identifier)nodeType).f0.tokenImage);

      _methodName = new String(n.f2.f0.tokenImage);
      //System.out.println("method name: "+_methodName + "method type: "+_methodType._type);
      _parameterList = new ArrayList<SymbolType>();
      n.f2.accept(this);
      n.f3.accept(this);
      _parameterList.add(_methodType);
      n.f4.accept(this);
	try {
	    _st.addMethod(_methodName, _parameterList);
	} catch (Exception ex) {
	    /* Method already declared or method has different signarure from mother class */
	    displayError(ex.getMessage());
	}
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      n.f11.accept(this);
      n.f12.accept(this);
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    @Override
   public void visit(FormalParameterList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    @Override
   public void visit(FormalParameter n) {
      n.f0.accept(this);

      String type = new String();
      String identifier = new String(n.f1.f0.tokenImage);
      Node nodeType = n.f0.f0.choice;
      
      if (nodeType instanceof IntegerType)
	    _parameterList.add(new SymbolTypePrimitive("int", identifier));
      else if (nodeType instanceof BooleanType)
	    _parameterList.add(new SymbolTypePrimitive("boolean", identifier));
      else if (nodeType instanceof ArrayType)
	    _parameterList.add(new SymbolTypePrimitive("array", identifier));
      else if (nodeType instanceof Identifier){
	  _parameterList.add( new SymbolTypeClass( ((Identifier)nodeType).f0.tokenImage, identifier) );
      }

      n.f1.accept(this);
   }

   /**
    * f0 -> ( FormalParameterTerm() )*
    */
    @Override
   public void visit(FormalParameterTail n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    @Override
   public void visit(FormalParameterTerm n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
   public void visit(Type n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
   public void visit(ArrayType n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> "boolean"
    */
    @Override
   public void visit(BooleanType n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "int"
    */
    @Override
   public void visit(IntegerType n) {
      n.f0.accept(this);
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
      n.f0.accept(this);
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
    @Override
   public void visit(Block n) {
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
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    @Override
   public void visit(WhileStatement n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    @Override
   public void visit(PrintStatement n) {
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
      n.f0.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(AndExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(CompareExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(PlusExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(MinusExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(TimesExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
   public void visit(ArrayLookup n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
   public void visit(ArrayLength n) {
      n.f0.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
   public void visit(ExpressionList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> ( ExpressionTerm() )*
    */
    @Override
   public void visit(ExpressionTail n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
    @Override
   public void visit(ExpressionTerm n) {
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
      n.f0.accept(this);
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
    @Override
   public void visit(IntegerLiteral n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "true"
    */
    @Override
   public void visit(TrueLiteral n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "false"
    */
    @Override
   public void visit(FalseLiteral n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> <IDENTIFIER>
    */
    @Override
   public void visit(Identifier n) {
      _currentLine = n.f0.beginLine;
      n.f0.accept(this);
   }

   /**
    * f0 -> "this"
    */
    @Override
   public void visit(ThisExpression n) {
      n.f0.accept(this);
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
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
   public void visit(AllocationExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
    @Override
   public void visit(NotExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
   public void visit(BracketExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }



}
