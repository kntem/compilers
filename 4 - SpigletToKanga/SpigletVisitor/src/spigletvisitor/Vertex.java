/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spigletvisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author d3m
 */
public class Vertex {
    Map<String, Vertex> _neighbors;
    Map<String, Vertex> _neighborsMirror;
    private String _var;
    private int _color;

    public Vertex(String temp) {
	_var = temp;
	_neighbors = new Hashtable<String, Vertex>();
	_neighborsMirror = new Hashtable<String, Vertex>();
	_color = -1;
    }


    void addNeighbor(Vertex v){

	_neighbors.put(v.getVar(), v);
	_neighborsMirror.put(v.getVar(), v);
    }

    //return the painted color
    int paintVertex(int colorsNo){
	int color = -1;

	if (_neighborsMirror.size() == 0)
	    return -1;

	for(Map.Entry<String, Vertex> entry: _neighborsMirror.entrySet()){
	    int c = entry.getValue().getColor();
	    if (c > color)
		color = c;
	}
	if ((color != -1) && (color<colorsNo)){
	    _color = color+1;
	    return _color;
	}
	else
	    _color = 0;
	return _color;

    }
    
    public String getVar() {
	return _var;
    }

    public int getColor() {
	return _color;
    }
    
    boolean equals(Vertex v){
	return v.getVar().equals(_var);
    }

    int getDegree(){
	return _neighbors.size();
    }

    void removeNeighbors(){
	for(Map.Entry<String, Vertex> entry: _neighbors.entrySet()){
	    entry.getValue().removeNeighbor(_var);
	}
	_neighbors.clear();
    }

    void removeNeighbor(String var){
	_neighbors.remove(var);
    }
}
