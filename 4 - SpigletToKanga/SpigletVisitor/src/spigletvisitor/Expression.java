/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spigletvisitor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author d3m
 */
public class Expression {
    private String _type;
    private String token1;
    private String token2;

    private List<String> args;

    public Expression() {
	args = new ArrayList<String>();
    }

    public void addArg(String arg){
	args.add(arg);
    }


    public void setToken1(String _address1) {
	this.token1 = _address1;
    }

    public void setToken2(String _address2) {
	this.token2 = _address2;
    }

    public String getType() {
	return _type;
    }

    public List<String> getArgs() {
	return args;
    }

    public int getArgsSize(){
	return args.size();
    }
    
    public void setType(String _type) {
	this._type = _type;
    }

    public String getToken1() {
	return token1;
    }

    public String getToken2() {
	return token2;
    }

}
