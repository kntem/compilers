package minijavavisitor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author d3m
 */
public class IdentifierSymbolTable {

    IdentifierSymbolTable _parent;

    /* Map from Identifiers to Types*/
    Map<String, SymbolType> map;

    public IdentifierSymbolTable() {
	IdentifierSymbolTable parent = null;
	map = new HashMap<String, SymbolType>();
    }

    public IdentifierSymbolTable(IdentifierSymbolTable parent) {
	this._parent = parent;
	map = new HashMap<String, SymbolType>();
    }

    public IdentifierSymbolTable enter(){
	IdentifierSymbolTable idst = new IdentifierSymbolTable(this);
	return idst;
    }

    public void insert(String identifier, SymbolType symType) {
	map.put(identifier, symType);
    }

    public boolean lookup(String name){
	if (keyExists(name))
	    return true;
	else if (_parent != null)
	    return _parent.lookup(name);
	else
	    return false;
    }

    private boolean keyExists(String name){
	if (map.containsKey(name))
	    return true;
	else
	    return false;
    }

    public String getType(String identifier){
	SymbolType type = map.get(identifier);
	if (type == null){
	    if (_parent != null){
		String type2 = _parent.getType(identifier);
		return type2;
	    }
	    else
		return null;
	}
	else
	    return type.getType();
    }

    public boolean isClassType(String identifier){
	SymbolType type = map.get(identifier);
	if (type instanceof SymbolTypeClass)
	    return true;
	else if (type == null)
	    return _parent.isClassType(identifier);
	else
	    return false;
    }

    public IdentifierSymbolTable exit(){
	return _parent;
    }

}
