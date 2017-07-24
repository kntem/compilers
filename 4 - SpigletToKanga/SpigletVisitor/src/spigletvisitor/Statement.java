/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spigletvisitor;

import java.util.ArrayList;
import java.util.List;
import org.omg.PortableInterceptor.USER_EXCEPTION;

/**
 *
 * @author d3m
 */
public class Statement {

    private String _stmnt;
    private String _type;
    private String _label;
    private String _address1;
    private String _address2;
    private String _integer;
    private Expression _expr;

    public Statement() {
	_stmnt = "";
	_type = "";
	_label = "";
	_address1 = "";
	_address2 = "";
    }
    
    public String getAddress1() {
	return _address1;
    }

    public String getAddress2() {
	return _address2;
    }

    public void setAddress1(String _address1) {
	this._address1 = _address1;
    }

    public void setAddress2(String _address2) {
	this._address2 = _address2;
    }

    public String getLabel() {
	return _label;
    }

    public void setLabel(String _label) {
	this._label = _label;
    }

    public void setType(String _type) {
	this._type = _type;
    }

    public String getType() {
	return _type;
    }

    public String getInteger() {
	return _integer;
    }

    public void setInteger(String _integer) {
	this._integer = _integer;
    }

    public Expression getExpr() {
	return _expr;
    }

    public void setExpr(Expression _expr) {
	this._expr = _expr;
    }

    public void buildStatement(String stmntRest){
	_stmnt += " "+stmntRest;
    }

    String getStatementCode(){
	String stmntStr = "";

    /* f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
	if (_type.equals("ERROR")){
	    return "ERROR\n";

	}
	else if (_type.equals("CJUMP")){
	    return "CJUMP "+_address1+" "+_label+"\n";

	}
	else if (_type.equals("JUMP")){
	    return "JUMP "+_label+"\n";
	}
	else if (_type.equals("HSTORE")){
	    return "HSTORE "+_address1+" "+_integer+" "+_address2+"\n";
	}
	else if (_type.equals("HLOAD")){
	    return "HLOAD "+_address1+" "+_address2+" "+_integer+"\n";
	}
	else if (_type.equals("MOVE")){
	    String stm = "MOVE "+_address1+" ";
	    /**
	    * f0 -> Call()
	    *       | HAllocate()
	    *       | BinOp()
	    *       | SimpleExp()
	    */
	    if (_expr.getType().equals("CALL")){
		stm += "CALL "+_expr.getToken1()+"(";
		List<String> args = _expr.getArgs();
		for(int i=0; i<args.size(); i++){
		    stm += " "+args.get(i);
		}
		stm += " )\n";

	    }
	    else if (_expr.getType().equals("HALLOCATE")){
		stm += "HALLOCATE "+_expr.getToken1()+"\n";
	    }
	    else if (_expr.getType().equals("SimpleExp")){
		stm += _expr.getToken1()+"\n";
	    }
	    else {/*Bin Op*/
		stm += _expr.getType()+" "+ _expr.getToken1()+" "+_expr.getToken2()+"\n";
	    }
	    return stm;
	}
	else if (_type.equals("PRINT")){
	    return "PRINT "+_address1+"\n";
	}
	else if (_type.equals("BEGIN")){
	    return "BEGIN\n";
	}
	else if (_type.equals("END")){
	    return "END\n";
	}
	else if (_type.equals("MAIN")){
	    return "MAIN\n";
	}
	else if (_type.equals("RETURN")){
	    String code = "RETURN ";
	    if (!_address1.equals("")){
		return code+_address1+"\n";
	    }
	    else if (!_label.equals("")){
		return code+_label+"\n";
	    }
	    if (!_integer.equals("")){
		return code+_integer+"\n";
	    }

	}
	else if (_type.equals("LABEL")){
	    return _label+" NOOP\n";
	}
	else if (_type.equals("FUNCTION")){
	    return _label+"["+_integer+"]\n";
	}

	return stmntStr;
    }

    List<String> getUse(){
	List<String> use = new ArrayList();

	if (_type.equals("CJUMP")){
	    use.add(_address1);
	}
	else if (_type.equals("HSTORE")){
	    use.add(_address1);
	}
	else if (_type.equals("HLOAD")){
	    use.add(_address2);
	}
	else if (_type.equals("MOVE")){
	    if (_expr.getType().equals("CALL")){
		use.add(_expr.getToken1());
		List<String> args = _expr.getArgs();
		for(int i=0; i<args.size(); i++){
		    if (args.get(i).contains("TEMP"))//if variable
			use.add(args.get(i));
		}
	    }
	    else if (_expr.getType().equals("HALLOCATE")){
		if (_expr.getToken1().contains("TEMP"))//if variable
		    use.add(_expr.getToken1());
	    }
	    else if (_expr.getType().equals("SimpleExp")){
		if (_expr.getToken1().contains("TEMP"))//if variable
		    use.add(_expr.getToken1());
	    }
	    else {/*Bin Op*/
		if (_expr.getToken1().contains("TEMP"))//if variable
		    use.add(_expr.getToken1());

		if (_expr.getToken2().contains("TEMP"))//if variable
		    use.add(_expr.getToken2());
	    }
	}
	else if (_type.equals("PRINT")){
	    use.add(_address1);
	}
	else if (_type.equals("RETURN")){
	    if (!_address1.equals("")){
		use.add(_address1);
	    }
	}
	return use;
    }

    List<String> getDef(){
	List<String> def = new ArrayList();

	if (_type.equals("HLOAD")){
	    def.add(_address1);
	}
	else if (_type.equals("MOVE")){
	    def.add(_address1);
	}

	return def;
    }
}
