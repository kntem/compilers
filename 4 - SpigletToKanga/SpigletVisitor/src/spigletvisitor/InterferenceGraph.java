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
public class InterferenceGraph {
    Map<String, Vertex> G;

    public InterferenceGraph() {
	G = new Hashtable<String, Vertex>();
    }

    public void addVertex(Vertex v){
	if (!G.containsKey(v.getVar()))
	    G.put(v.getVar(),v);
    }

    public void addVertex(String var){
	if (!G.containsKey(var))
	    G.put(var, new Vertex(var));
    }

    public void addNeighbors(String var, String neighbor){
	Vertex v = G.get(var);
	if (v == null){
	    v = new Vertex(var);
	    G.put(var, v);
	}
	
	Vertex n = G.get(neighbor);
	if (n == null){
	    n = new Vertex(neighbor);
	    G.put(neighbor, n);
	}
	v.addNeighbor(n);
	n.addNeighbor(v);
    }

    public boolean contains(Vertex v){
	return G.containsKey(v.getVar());
    }

    Vertex getMaxVertex(int d){
	//picks the vertex with highest degree below d
	int max = 0;
	Vertex vmax = null;
	for(Map.Entry<String, Vertex> entry: G.entrySet()){
	    int degree = entry.getValue().getDegree();
	    if ((degree>=max)&&(degree<d))
		vmax = entry.getValue();
	}
	return vmax;
    }

    Vertex removeVertex(String var){
	Vertex v = G.get(var);
	if (v == null) return null;

	G.remove(var);
	v.removeNeighbors();
	return v;
    }

    boolean removeVertex(Vertex v){
	if (v == null) return false;
	if (G.remove(v.getVar()) == null)
	    return false;
	v.removeNeighbors();
	return true;
    }

    boolean isEmpty(){
	return G.isEmpty();
    }


}
