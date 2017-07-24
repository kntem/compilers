/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package minijavavisitor;

import java.util.logging.Level;
import java.util.logging.Logger;
import mini_java_parser.*;
import syntaxtree.*;
import visitor.*;
import java.io.*;

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
	    input = new FileInputStream("MyTest.java");
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
	}
	Node root = null;
        MiniJavaParser parser = new MiniJavaParser(input);
      try {
         root = parser.Goal();
	 System.err.println("Java program parsed successfully.");
      }
      catch (ParseException e) {
         System.err.println("Encountered errors during parse.");
	 System.exit(0);
      }
	MiniJavaDeclarationsVisitor mjdv = new MiniJavaDeclarationsVisitor();
	root.accept(mjdv);
	
	mjdv.checkDataMembers();
	mjdv.checkMethods();
	
	MiniJavaTypeCheckingVisitor mjv = new MiniJavaTypeCheckingVisitor((mjdv.getSymbolTable()));
	root.accept(mjv);
	
	System.err.println("Java program type checked successfully.");
    }

}
