/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package minijavavisitor;
import java.util.Map;
import java.util.HashMap;

import syntaxtree.ThisExpression;

/**
 *
 * @author d3m
 */
public class TempSymbolTable {
    /* identifier to temp var */
    Map<String, String> _idTemp;

    /* identifier to type */
    Map<String, String> _idTempType;

    TempSymbolTable _parent;

    public TempSymbolTable() {
	_parent = null;
	_idTemp = new HashMap<String, String>();
	_idTempType = new HashMap<String, String>();
    }

    public TempSymbolTable(TempSymbolTable p) {
	_parent = p;
	_idTemp = new HashMap<String, String>();
	_idTempType = new HashMap<String, String>();
    }

    public TempSymbolTable enter(){
	TempSymbolTable idst = new TempSymbolTable(this);
	return idst;
    }

    public TempSymbolTable exit(){
	return _parent;
    }

    public void addIdentifier(String id, String address, String type) {
	_idTemp.put(id, address);
	_idTempType.put(id, type);
    }

    public Map<String, String> getIdTemp() {
	return _idTemp;
    }
    public String getTempVar(String id){
	return _idTemp.get(id);
    }

    public String getTempType(String temp){
	return _idTempType.get(temp);
    }

    public boolean contains(String id){
	return _idTemp.containsKey(id);
    }
    

}
