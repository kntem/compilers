package kangavisitor;
import kanga_parser.*;
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
	    input = new FileInputStream("MoreThan4.kg");
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
	}
	Node root = null;
        KangaParser parser = new KangaParser(input);

	try {
	    root = parser.Goal();
	    System.err.println("Spiglet program parsed successfully.");
	}
	catch (ParseException e) {
	    System.err.println("Encountered errors during parse.");
	    System.exit(0);
	}

	KangaVisitor kv = new KangaVisitor();
	root.accept(kv);
	System.out.println("Code generated: ");
	System.out.println(kv.getCode());


    }

}
