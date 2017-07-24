/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package minijavavisitor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import syntaxtree.TrueLiteral;
/**
 *
 * @author d3m
 */
public class SymbolTypeClass extends SymbolType{


    /* map from name to type */
    Map<String, SymbolTypePrimitive> _dataMembersPrimitive;
    Map<String, SymbolTypeClass> _dataMembersClass;

    /* map from name to method prototype */
    Map<String, List> _methods;

    SymbolTypeClass _motherClass;


    public SymbolTypeClass(String type) {
	_dataMembersPrimitive = new HashMap<String, SymbolTypePrimitive>();
	_dataMembersClass = new HashMap<String, SymbolTypeClass>();
	_methods = new HashMap<String, List>();
	_motherClass = null;
	_type = type;
    }

    public SymbolTypeClass(String type, String identifier) {
	_dataMembersPrimitive = new HashMap<String, SymbolTypePrimitive>();
	_dataMembersClass = new HashMap<String, SymbolTypeClass>();
	_methods = new HashMap<String, List>();
	_type = type;
	_motherClass = null;
	_identifier = identifier;
    }

    public SymbolTypeClass(String type, SymbolTypeClass motherClass) {
	_dataMembersPrimitive = new HashMap<String, SymbolTypePrimitive>();
	_dataMembersClass = new HashMap<String, SymbolTypeClass>();
	_methods = new HashMap<String, List>();
	_type = type;
	_motherClass = motherClass;
    }

    public SymbolTypeClass(String type, String identifier, SymbolTypeClass motherClass) {
	_dataMembersPrimitive = new HashMap<String, SymbolTypePrimitive>();
	_dataMembersClass = new HashMap<String, SymbolTypeClass>();
	_methods = new HashMap<String, List>();
	_type = type;
	_identifier = identifier;
	_motherClass = motherClass;
    }

    public void addDataMember(SymbolType dataMember) throws Exception{
	if (_methods.containsKey( dataMember.getIdentifier()) )
	    throw new Exception("Name " + dataMember.getIdentifier() + " already defined as a class method");
	if (dataMember instanceof SymbolTypeClass)
	    addDataMemberClass((SymbolTypeClass)dataMember);
	else if (dataMember instanceof SymbolTypePrimitive)
	    addDataMemberPrimitive((SymbolTypePrimitive)dataMember);
    }

    public void addDataMemberClass(SymbolTypeClass dataMember) throws Exception{
	if (_dataMembersClass.containsKey( dataMember.getIdentifier()) )
	    throw new Exception("Data Member " + dataMember.getIdentifier() + " already defined in class " + _type);
	_dataMembersClass.put(dataMember.getIdentifier(), dataMember);
    }

    public void addDataMemberPrimitive(SymbolTypePrimitive dataMember) throws Exception{
	if (_dataMembersPrimitive.containsKey( dataMember.getIdentifier()) )
	    throw new Exception("Data Member " + dataMember.getIdentifier() + " already defined in class " + _type);
	_dataMembersPrimitive.put(dataMember.getIdentifier(), dataMember);
    }


    public void addMethod(String name, List prototype, SymbolTable st) throws Exception {
	if ((_dataMembersClass.containsKey( name )) || (_dataMembersPrimitive.containsKey( name )) )
	    throw new Exception(name + " already defined as class field.");

	if (_methods.containsKey(name))
	    throw new Exception("Method " + name + " already exists in class " + _type);

	if (isSubClass()){
	    List upperPrototype = _motherClass.getMethodPrototype(name);

	    if (upperPrototype != null ) {
		if (upperPrototype.size() != prototype.size())
		    throw new Exception("Method " +_type + "." +name + " has different signature from " + _motherClass.getType()+ "." +name);

		for (int i=1; i<prototype.size(); i++){
		    SymbolType arg = (SymbolType)prototype.get(i);
		    SymbolType arg2 = (SymbolType)upperPrototype.get(i);

		    /* Compare types */
		    if ((arg instanceof SymbolTypeClass)&&(arg2 instanceof SymbolTypeClass)){
			    SymbolTypeClass ps = (SymbolTypeClass)arg;
			    SymbolTypeClass pu = (SymbolTypeClass)arg2;
			    if (ps.getType()!= pu.getType()){
				if (!st.isSubClassOf(ps.getType(), pu.getType()))
				    throw new Exception("Method " +_type + "." +name + " has different signature from " + _motherClass.getType()+ "." +name);
			    }

		    }
		    else {
			if (!arg.getType().equals(arg2.getType()))
			    throw new Exception("Method " +_type + "." +name + " has different signature from " + _motherClass.getType()+ "." +name);
		    }
		}
	    }
	    
	}
	_methods.put(name, prototype);
    }

    public List getMethodPrototype(String name){
	List p =  _methods.get(name);
	if ((p == null) && isSubClass()){
	    p = _motherClass.getMethodPrototype(name);
	}
	return p;
    }

    public SymbolTypeClass getMotherClass() {
	return _motherClass;
    }
    
    public Map<String, SymbolTypeClass> getDataMembersClass() {
	return _dataMembersClass;
    }

    public Map<String, SymbolTypePrimitive> getDataMembersPrimitive() {
	return _dataMembersPrimitive;
    }

    public Map<String, List> getMethods() {
	return _methods;
    }

    public boolean hasMethod(String methodName){
	if (_methods.containsKey(methodName))
	    return true;
	else
	    return false;
    }

    public boolean isSubClass(){
	if (_motherClass == null)
	    return false;
	else
	    return true;
    }

    public boolean isSubClassOf(String upper){
	if (!isSubClass())
	    return false;
	if (_motherClass.getType().equals(upper))
	    return true;
	else
	    return false;
    }

}
