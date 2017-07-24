/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package minijavavisitor;
import java.util.logging.Level;
import java.util.logging.Logger;
import syntaxtree.*;
import visitor.DepthFirstVisitor;
import java.util.*;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class MiniJavaTypeCheckingVisitor extends DepthFirstVisitor {
   //
   // Auto class visitors--probably don't need to be overridden.
   //
    private SymbolTable _st;
    private IdentifierSymbolTable _idst;

    /* Store the last type visited */
    Stack _typeStack;

    Stack _methodArguments;

    private String _lastClass;
    private boolean _classVarDeclaration;

    private String _identifierType;
    private String _identifier;

    private boolean _identifierNotFound;

    private boolean _typeCheckingEnabled;

    private int _currentLine;


    public MiniJavaTypeCheckingVisitor(SymbolTable symbolTable){
	_st = symbolTable;
	_typeStack = new Stack();
	_methodArguments = null;
    }

    private void displayError(String message){
	System.err.println("Line " + _currentLine + ": " + message);
	System.exit(0);
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
	_lastClass = null;
	//System.out.println("Main started!");
	_idst = new IdentifierSymbolTable();
      n.f0.accept(this);
      /* Disable main class name and main arguments type checking */
      _typeCheckingEnabled = false;
      n.f1.accept(this);
      _typeCheckingEnabled = true;
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      /* Disable argument identifier checking */
      _typeCheckingEnabled = false;
      n.f11.accept(this);
      _typeCheckingEnabled = true;
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
      _lastClass = new String(n.f1.f0.tokenImage);
      _idst = new IdentifierSymbolTable();
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      _classVarDeclaration = true;
      _typeCheckingEnabled = false;
      /* Class var declarations checking not needed because of the previous visitor*/
      n.f3.accept(this);
      _typeCheckingEnabled = true;
      _classVarDeclaration = false;
      n.f4.accept(this);
      n.f5.accept(this);
      _lastClass = null;
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
      _lastClass = new String(n.f1.f0.tokenImage);
      _idst = new IdentifierSymbolTable();
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      /* Class var declarations checking not needed because of the previous visitor*/
      _classVarDeclaration = true;
      _typeCheckingEnabled = false;
      n.f3.accept(this);

      /* Insert fields of extended class into current symbol table */
      String upperClass = n.f3.f0.tokenImage;
      SymbolTypeClass upperClassType = null;
      Map<String, SymbolTypeClass> fields = null;
	try {
	    upperClassType = _st.getClass(upperClass);
	    fields = _st.getClass(upperClass).getDataMembersClass();
	} catch (Exception ex) {
	    displayError(ex.getMessage());
	}
      for( Map.Entry<String, SymbolTypeClass> entry : fields.entrySet()){
	    SymbolTypeClass field = entry.getValue();
	    //System.out.println("Inserting: "+_lastClass+"."+field.getIdentifier()+" type " + field.getType());
	    _idst.insert(field.getIdentifier(), field );
      }

      Map<String, SymbolTypePrimitive> fields2 = null;
	try {
	    fields2 = _st.getClass(upperClass).getDataMembersPrimitive();
	} catch (Exception ex) {
	    displayError(ex.getMessage());
	}
      for( Map.Entry<String, SymbolTypePrimitive> entry : fields2.entrySet()){
	    SymbolTypePrimitive field = entry.getValue();
	    //System.out.println("Inserting: "+_lastClass+"."+field.getIdentifier());
	    _idst.insert(field.getIdentifier(), upperClassType);
      }

      _typeCheckingEnabled = true;
      _classVarDeclaration = false;
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      _lastClass = null;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
   public void visit(VarDeclaration n) {
	/* Class fields declarations checked by previous visitor */
      if (!_classVarDeclaration){
	  Node n2 = n.f0.f0.choice;
	  SymbolType type = null;
	  String identifier = new String(n.f1.f0.tokenImage);
	  if(n2 instanceof IntegerType)
	      type = new SymbolTypePrimitive("int", identifier);
	  else if(n2 instanceof BooleanType)
	      type = new SymbolTypePrimitive("boolean", identifier);
	  else if(n2 instanceof ArrayType)
	      type = new SymbolTypePrimitive("array", identifier);
	  else if(n2 instanceof Identifier)
	      type = new SymbolTypeClass(((Identifier) n2).f0.tokenImage, identifier);


	  _idst.insert(identifier, type);
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

      String methodName = new String(n.f2.f0.tokenImage);
      _idst = _idst.enter();
      List signature = null;
	try {
	    signature = _st.getClass(_lastClass).getMethodPrototype(methodName);
	} catch (Exception ex) {
	    /* Because getClass throws exception */
	    displayError(ex.getMessage());
	}

      /* Insert arguments to symbol table as variable declarations */
      for (int i=1; i<signature.size(); i++){
	  SymbolType arg = (SymbolType)signature.get(i);
	    _idst.insert(arg.getIdentifier(), arg);
      }

      Map<String, SymbolTypeClass> classFields = null;
	try {
	    classFields = _st.getClass(_lastClass).getDataMembersClass();
	} catch (Exception ex) {
	    /* Because getClass throws exception */
	    displayError(ex.getMessage());
	}
      /* Insert class data members */
      for( Map.Entry<String, SymbolTypeClass> cField : classFields.entrySet()){
	    _idst.insert(cField.getKey(), cField.getValue());
      }

      Map<String, SymbolTypePrimitive> primitiveFields = null;
	try {
	    primitiveFields = _st.getClass(_lastClass).getDataMembersPrimitive();
	} catch (Exception ex) {
	    /* Because getClass throws exception */
	    displayError(ex.getMessage());
	}
      /* Insert class data members */
      for( Map.Entry<String, SymbolTypePrimitive> pField : primitiveFields.entrySet()){
	    _idst.insert(pField.getKey(), pField.getValue());
      }

      /* Avoid class name to be treated as a variable */
      _typeCheckingEnabled = false;
      n.f2.accept(this);
      _typeCheckingEnabled = true;
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);

      /* Check return type */
      String type = (String)_typeStack.pop();
      SymbolType returnType = (SymbolType)signature.get(0);
      if (!type.equals(returnType.getType())){
	  if (!_st.isSubClassOf(returnType.getType(), type))
		displayError("Wrong return type.");
      }

      n.f11.accept(this);
      n.f12.accept(this);
      _idst = _idst.exit();
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
      _identifier = n.f0.f0.tokenImage;
      _typeStack.pop();
      if (!_idst.lookup(_identifier))
	  displayError("Identifier '" + _identifier + "' not found");
      
      _identifierType = _idst.getType(n.f0.f0.tokenImage);
      
     
      n.f0.accept(this);
      n.f1.accept(this);
      
      n.f2.accept(this);
      String type = (String)_typeStack.pop();
      if (!type.equals(_identifierType)){
	  if(!_st.isSubClassOf(type, _identifierType))
		displayError("Expression type '"+ type +"' does not match identifier's type '"+ _identifierType +"' in assignment.");
      }
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
      _identifier = n.f0.f0.tokenImage;
      _typeStack.pop();
      if (!_idst.lookup(_identifier))
	  displayError("Identifier " + _identifier + " not found.");
      
      if (!_idst.getType(_identifier).equals("array"))
	  displayError("Identifier " + _identifier + " is not an array.");
      
      n.f0.accept(this);
      n.f1.accept(this);

    
      n.f2.accept(this);
    
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      String type = (String)_typeStack.pop();
      if(!type.equals("int"))
	    displayError("Expression type does not match identifier's type in assignment.");
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
      String type = (String)_typeStack.pop();
      if(!type.equals("boolean"))
	    displayError("Expected boolean expression.");
      
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
      String type = (String)_typeStack.pop();
      if(!type.equals("boolean"))
	    displayError("Expected boolean expression.");
      
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
      String type = (String)_typeStack.pop();
      if(!type.equals("int"))
	    displayError("Expected int");
     
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
      String type = new String();
      Node n2 = n.f0.f0.choice;
      type = (String)_typeStack.pop();
      
      if (n2 instanceof IntegerLiteral)
	  displayError("AND expression. Expecting boolean instead of integer literal");
      else if (n2 instanceof Identifier){
	  
	  String id = new String(((Identifier)n2).f0.tokenImage);
	  type = _idst.getType(id);
	  if (!type.equals("boolean"))
		displayError("AND expression. Expecting boolean instead of "+type);
      }
      else if (n2 instanceof ThisExpression){
	if (!type.equals("boolean"))
		displayError("AND expression. Expecting boolean instead of "+type);
      }
      else if (n2 instanceof ArrayAllocationExpression ){
	  displayError("AND expression. Expecting boolean instead of array allocation");
      }
      else if (n2 instanceof AllocationExpression ){
	  displayError("AND expression. Expecting boolean instead of allocation");
      }


      n.f1.accept(this);


      n.f2.accept(this);

      n2 = n.f2.f0.choice;
      type = (String)_typeStack.pop();
      if (n2 instanceof IntegerLiteral)
	  displayError("AND expression. Expecting boolean instead of integer literal");
      else if (n2 instanceof Identifier){

	  String id = new String(((Identifier)n2).f0.tokenImage);
	  type = _idst.getType(id);
	  if (!type.equals("boolean"))
		displayError("AND expression. Expecting boolean instead of "+type);
      }
      else if (n2 instanceof ThisExpression){
	if (!type.equals("boolean"))
		displayError("AND expression. Expecting boolean instead of "+type);
      }
      else if (n2 instanceof ArrayAllocationExpression ){
	  displayError("AND expression. Expecting boolean instead of array allocation");
      }
      else if (n2 instanceof AllocationExpression ){
	  displayError("AND expression. Expecting boolean instead of allocation");
      }

      _typeStack.push("boolean");
      
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(CompareExpression n) {

      n.f0.accept(this);
      String type = new String();
      type = (String)_typeStack.pop();

      if (!type.equals("int"))
	  displayError("Compare expression, expecting int.");
      
      n.f1.accept(this);


      n.f2.accept(this);
      type = (String)_typeStack.pop();
      if (!type.equals("int"))
	    displayError("Compare expression, expecting int.");

      _typeStack.push("boolean");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(PlusExpression n) {
      
      n.f0.accept(this);
      String type = new String();
      type = (String)_typeStack.pop();

      if (!type.equals("int"))
	  displayError("Arithmetic expression, expecting int.");

      n.f1.accept(this);

      n.f2.accept(this);
      
      type = (String)_typeStack.pop();

      if (!type.equals("int"))
	  displayError("Arithmetic expression, expecting int.");

      _typeStack.push("int");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(MinusExpression n) {
	
      n.f0.accept(this);
      String type = new String();
      type = (String)_typeStack.pop();

      if (!type.equals("int"))
	  displayError("Arithmetic expression, expecting int.");

      n.f1.accept(this);

      n.f2.accept(this);

      type = (String)_typeStack.pop();

      if (!type.equals("int"))
	  displayError("Arithmetic expression, expecting int.");

      _typeStack.push("int");

   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
   public void visit(TimesExpression n) {

      n.f0.accept(this);
      String type = new String();
      type = (String)_typeStack.pop();

      if (!type.equals("int"))
	  displayError("Arithmetic expression, expecting int.");

      n.f1.accept(this);

      n.f2.accept(this);

      type = (String)_typeStack.pop();

      if (!type.equals("int"))
	  displayError("Arithmetic expression, expecting int.");

      _typeStack.push("int");
      
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
      Node n2 = n.f0.f0.choice;
      String id = new String();
      String type = new String();
      if (n2 instanceof Identifier){
	    id = ((Identifier)n2).f0.tokenImage;
	    type = _idst.getType(id);
	    if (!type.equals("array"))
		displayError("Identifier "+id+" not an array.");
      }
      else
	  displayError("Expecting identifier of array type.");

      _typeStack.pop();
      n.f1.accept(this);

      n.f2.accept(this);
      type = (String)_typeStack.pop();
      if (!type.equals("int"))
	  displayError("Index of array is not int type.");

      n.f3.accept(this);

      _typeStack.push("int");
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
   public void visit(ArrayLength n) {

      Node n2 = n.f0.f0.choice;
      n.f0.accept(this);
      String type = (String)_typeStack.pop();
      String id = new String();
      if (n2 instanceof Identifier){
	    id = ((Identifier)n2).f0.tokenImage;
	    type = _idst.getType(id);
	    if (!type.equals("array"))
		displayError("Expecting array type instead of "+type);
      }
      else if (!type.equals("array"))
	    displayError("Expecting array type instead of "+type);

      n.f1.accept(this);
      n.f2.accept(this);

      _typeStack.push("int");
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
	//to do
	//look up primary expression type
	//this: look class type
	//look identifier(method) type


      Node n2 = n.f0.f0.choice;
      String classType = new String();
      String methodName = new String();
      if(n2 instanceof Identifier){
	  String id = ((Identifier)n2).f0.tokenImage;
	  if (!_idst.lookup(id))
	      displayError("Unknown Identifier '" + id + "'");

	  if (!_idst.isClassType(id))
	      displayError("Identifier '" + id + "' is not an Object Instance.");

	  n.f0.accept(this);
	  n.f1.accept(this);
	  classType = (String)_typeStack.pop();
	  _typeCheckingEnabled = false;
	  n.f2.accept(this);
	  _typeCheckingEnabled = true;
	  methodName = n.f2.f0.tokenImage;
	  if (!_st.classHasMethod(classType, methodName))
	      displayError("Class '"+classType+"' has no method named '" + methodName + "'");
      }
      else if((n2 instanceof ThisExpression) || (n2 instanceof AllocationExpression) || (n2 instanceof BracketExpression)){
	  n.f0.accept(this);
	  classType = (String)_typeStack.pop();
	  /* Avoid method name to be treated as variable id */
	  _typeCheckingEnabled = false;
	  n.f1.accept(this);
	  n.f2.accept(this);
	  _typeCheckingEnabled = true;
	  methodName = n.f2.f0.tokenImage;
	  if (!_st.classHasMethod(classType, methodName))
	      displayError("Class '"+classType+"' has no method named '" + methodName + "'");
      }
      else {
	  displayError("Expecting object instance.");
      }
      
      n.f3.accept(this);
      _methodArguments = new Stack();
      List sig = null;
	try {
	    sig = _st.getClass(classType).getMethodPrototype(methodName);
	} catch (Exception ex) {
	    /* getClass throws Exception*/
	   displayError(ex.getMessage());
	}
      /* Insert arguments in reverse order (excluding return type)*/
      for (int i = sig.size()-1; i>0; i--)
	    _methodArguments.push(sig.get(i));

      //System.out.println("Method name: "+ methodName);


      n.f4.accept(this);
      /* If all method arguments haven't been checked */
      if (!_methodArguments.empty())
	  displayError("Wrong number of arguments");
      n.f5.accept(this);
      _typeStack.push(((SymbolType)sig.get(0)).getType());
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
   public void visit(ExpressionList n) {
      n.f0.accept(this);
      String type = (String)_typeStack.pop();
      if (!_methodArguments.empty()){/* for methods without arguments */
	  SymbolType argType = (SymbolType)_methodArguments.pop();
	  if (!type.equals(argType.getType())) {
	      if (!_st.isSubClassOf(type, argType.getType()))
		    displayError("Incompatible argument type '"+ type +"'");
	  }
      }
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
      String type = (String)_typeStack.pop();
      SymbolType argType = (SymbolType)_methodArguments.pop();
      if (!type.equals(argType.getType()))
	  displayError("Incompatible argument type.");
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
	_currentLine = n.f0.beginLine;
      n.f0.accept(this);
      _typeStack.push("int");
   }

   /**
    * f0 -> "true"
    */
    @Override
   public void visit(TrueLiteral n) {
	_currentLine = n.f0.beginLine;
      n.f0.accept(this);
      _typeStack.push("boolean");
   }

   /**
    * f0 -> "false"
    */
    @Override
   public void visit(FalseLiteral n) {
	_currentLine = n.f0.beginLine;
      n.f0.accept(this);
      _typeStack.push("boolean");
   }

   /**
    * f0 -> <IDENTIFIER>
    */
    @Override
   public void visit(Identifier n) {
	_currentLine = n.f0.beginLine;
      String id = n.f0.tokenImage;
//      System.out.println("Identifier: "+id);
      String type = _idst.getType(id);
      n.f0.accept(this);
      if (_typeCheckingEnabled){
	  if (type != null)
		_typeStack.push(type);
	  else if (!_st.lookup(id))
	    displayError("Unknown Identifier '"+ id +"'");
	  
      }
   }

   /**
    * f0 -> "this"
    */
    @Override
   public void visit(ThisExpression n) {
	_currentLine = n.f0.beginLine;
      if (_lastClass == null)
	  displayError("Use of 'this' in non-class scope.");
      _typeStack.push(_lastClass);
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
      String type = (String)_typeStack.pop();
      if (!type.equals("int"))
	  displayError("Index of array is not int type.");
      n.f4.accept(this);

      _typeStack.push("array");
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
      String type = n.f1.f0.tokenImage;
      
      if (!_st.lookup(type))
	  displayError("Class " + type + " is not defined.");
      _typeStack.push(type);
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
      String type = (String)_typeStack.pop();
      if (!type.equals("boolean"))
	  displayError("Expecting boolean type in logic expression");

      _typeStack.push("boolean");
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
