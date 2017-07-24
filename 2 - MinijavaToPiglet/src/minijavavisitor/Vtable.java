package minijavavisitor;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author d3m
 */
public class Vtable {
    /* from names to method_label [args] */
    Map<String, String> _methods;
    Map<String, Integer> _methodsArgs;
    Map<String, Integer> _methodsOffsets;
    int _offset;

    public Vtable() {
	_methods = new HashMap<String, String>();
	_methodsOffsets = new HashMap<String, Integer>();
	_methodsArgs = new HashMap<String, Integer>();
	_offset = 0;
    }

    public int getSize(){
	return _offset;
    }

    public void addMethod(String name, String label, Integer argNum) {
	_methods.put(name, label);
	_methodsArgs.put(name, argNum);
	_methodsOffsets.put(name, _offset);
	_offset += 4;
    }

    public String getMethod(String name){
	return _methods.get(name)+" [ "+_methodsArgs.get(name)+" ] ";
    }

    public int getOffset(String name){
	return _methodsOffsets.get(name);
    }

    public String getAllocationCode(String temp){
	String code = "";
	//HSTORE TEMP 55  76 Tree_RecPrint
	code += "MOVE "+temp+" HALLOCATE "+getSize()+"\n";
	for( Map.Entry<String, String> method : _methods.entrySet()){
	    code += "HSTORE "+temp+" "+_methodsOffsets.get(method.getKey())+" "+method.getValue()+"\n";
	}

	return code;
    }

    public String getMethodLoadCode(String object,String method, String temp1, String temp2){
	String code="";
	//HLOAD TEMP 21 TEMP 23  0 load vtable
	//HLOAD TEMP 22 TEMP 21  0 load function
	code += "HLOAD "+temp1+" "+object+" 0\n";
	code += "HLOAD "+temp2+" "+temp1+" "+_methodsOffsets.get(method)+"\n";
	return code;
    }


}
