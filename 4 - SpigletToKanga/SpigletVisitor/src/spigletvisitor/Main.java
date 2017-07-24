/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spigletvisitor;
import spiglet_parser.*;
import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author d3m
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FileInputStream input = null;
	try {
	    input = new FileInputStream("Factorial.spg");
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
	}
	Node root = null;
        SpigletParser parser = new SpigletParser(input);

	try {
	    root = parser.Goal();
	    System.err.println("Spiglet program parsed successfully.");
	}
	catch (ParseException e) {
	    System.err.println("Encountered errors during parse.");
	    System.exit(0);
	}

	SpigletVisitor sv = new SpigletVisitor();
	root.accept(sv);
	//System.out.println("Code parsed: ");
	//System.out.println(sv.getCode());

	sv.addEdges();
	//System.out.println("Edges added");
	sv.computeSets();
	//System.out.println("Sets Computed");
	//System.out.println(sv.getCodeWithSets());
	sv.allocateRegisters();
	//System.out.println("Kanga Code:");
	System.out.println(sv.getKangaCode());


    }

}
