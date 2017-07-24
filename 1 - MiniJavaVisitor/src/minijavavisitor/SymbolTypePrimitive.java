/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package minijavavisitor;

/**
 *
 * @author d3m
 */
public class SymbolTypePrimitive extends SymbolType{

    public SymbolTypePrimitive(String type) {
	_type = type;
    }

    public SymbolTypePrimitive(String type, String identifier) {
	_type = type;
	_identifier = identifier;
    }

        public boolean isTypeClass(){
	return false;
    }

    public boolean isTypePrimitive(){
	return true;
    }

}
