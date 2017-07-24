package minijavavisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author d3m
 */
public class SymbolTable {

    /* Map from types to SymbolType objects*/
    Map<String, SymbolType> map;

    String _lastClass;

    public SymbolTable() {
	map = new HashMap<String, SymbolType>();
	_lastClass = null;

	insert("int", new SymbolTypePrimitive("int"));
	insert("boolean", new SymbolTypePrimitive("boolean"));
	insert("int[]", new SymbolTypePrimitive("int[]"));
    }

    public Map<String, SymbolType> getSymbolTable(){
	return map;
    }


    public void insert(String name, SymbolType type) {
	map.put(name, type);
	if (type instanceof SymbolTypeClass)
	    _lastClass = name;
    }

    public void insert(String name, String motherClass, SymbolType type) {
	map.put(name, type);
	_lastClass = name;
    }


    public boolean lookup(String name){
	if (keyExists(name))
	    return true;
	else
	    return false;
    }

    private boolean keyExists(String name){
	if (map.containsKey(name))
	    return true;
	else
	    return false;
    }

    public void addDataMember(SymbolType dataMember) throws Exception{
	SymbolTypeClass c = (SymbolTypeClass) map.get(_lastClass);
	c.addDataMember(dataMember);
    }


    public void addMethod(String name, List prototype) throws Exception{
	SymbolTypeClass c = (SymbolTypeClass) map.get(_lastClass);
	c.addMethod(name, prototype, this);
    }

    public void addMethod(String className, String name, List prototype) throws Exception{
	SymbolTypeClass c = (SymbolTypeClass) map.get(className);
	c.addMethod(name, prototype, this);

    }

    public SymbolTypeClass getClass(String name) throws Exception {
	if (keyExists(name)){
	    SymbolType motherClass = map.get(name);
	    if (motherClass instanceof SymbolTypeClass)
		return (SymbolTypeClass)motherClass;
	}

	throw new Exception("Class " + name + " is not defined.");
    }

    /* checks if Data Members' Types are valid*/
    public void checkDataMembers() throws Exception{
	for( Map.Entry<String, SymbolType> entry : map.entrySet()){
	    if(entry.getValue() instanceof SymbolTypeClass){
		Map<String, SymbolTypeClass> fields = ((SymbolTypeClass)entry.getValue()).getDataMembersClass();

		for( Map.Entry<String, SymbolTypeClass> member : fields.entrySet()){
		    String type = member.getValue()._type;
		    if (!lookup(type))
			throw new Exception("Class name " + type + " is not defined");
		}
	    }
	}
    }

    public void checkMethods() throws Exception{
	for( Map.Entry<String, SymbolType> entry : map.entrySet()){
	    if(entry.getValue() instanceof SymbolTypeClass){
		Map<String, List> methods = ((SymbolTypeClass)entry.getValue()).getMethods();

		for( Map.Entry<String, List> method : methods.entrySet()){
		    List prototype = method.getValue();
		    SymbolType returnType = (SymbolType)prototype.get(0);
		    if (!lookup(returnType.getType()))
			throw new Exception("Return type " + returnType.getType() + " of Method "+entry.getKey()+"."+method.getKey() +" is not defined");

		    for (int i=1; i<prototype.size(); i++){
			SymbolType arg = (SymbolType)prototype.get(i);
			if (!lookup(arg.getType()))
			    throw new Exception("Argument type " + arg.getType() + " of Method "+entry.getKey()+"."+method.getKey() +" is not defined");
		    }
		}
	    }
	}
    }

    public boolean isSubClassOf(String sub, String upper){
	SymbolType subClass = map.get(sub);

	if (subClass instanceof SymbolTypeClass){
	     if ( ((SymbolTypeClass)subClass).isSubClassOf(upper) )
		     return true;
	}

	return false;
    }

    public boolean classHasMethod(String classType, String methodName){
	SymbolType symbol = map.get(classType);
	if(symbol instanceof SymbolTypeClass) {
	    if ( ((SymbolTypeClass) symbol).hasMethod(methodName) )
		    return true;
	    else {
		SymbolTypeClass upper = ((SymbolTypeClass) symbol).getMotherClass();
		if (upper.hasMethod(methodName))
		    return true;
		else
		    return false;
	    }
	}
	else
	    return false;

    }

}
