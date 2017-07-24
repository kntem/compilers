/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package minijavavisitor;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author d3m
 */
public class ClassFields {
    /* Map from field name to offset */
    Map<String, Integer> _classFields;
    Map<String, Integer> _primitiveFields;

    int _offset;

    public ClassFields() {
	_classFields = new HashMap<String, Integer>();
	_primitiveFields = new HashMap<String, Integer>();
	_offset = 4;//after the VTable
    }

    public void addClassField(String name) {
	_classFields.put(name, _offset);
	_offset += 4;
    }

    public void addPrimitiveField(String name) {
	_primitiveFields.put(name, _offset);
	_offset += 4;
    }

    public int getOffset(String name){
	if (_classFields.containsKey(name))
	    return _classFields.get(name);
	
	return _primitiveFields.get(name);
    }

    public int getSize(){
	return _offset-4;
    }

    public boolean contains(String field){
	return _classFields.containsKey(field)||_primitiveFields.containsKey(field);
    }

    public String getInitCode(String tempObject, String tempI, String label1, String label2){
	//TEMP 55 = vtable
	//TEMP 56 = object
	//28 = 4*fieldsNum
//		MOVE TEMP 57  4
//L2 	CJUMP  LT TEMP 57  28 L3
//	HSTORE  PLUS TEMP 56 TEMP 57  0  0
//	MOVE TEMP 57  PLUS TEMP 57  4
//	JUMP L2
//outside:L3 	HSTORE TEMP 56  0 TEMP 55
	if (getSize()==0)
	    return " ";
	int size = getSize()+4;
	String code = "";
	code += "MOVE "+ tempI +" 4\n";
	code += label1+" CJUMP LT "+tempI+" "+size+" "+label2+"\n";
	code += "HSTORE PLUS "+tempObject+" "+tempI+" 0 0"+"\n";
	code += "MOVE "+tempI+" PLUS "+tempI+" 4\n";
	code += "JUMP "+label1+"\n";
	return code;
    }

    
}
