import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.Vector;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 1) return;
		String arg = args[0];
		String home10path = arg+"//";
		String jackfile = "";
		String path = "";
		
		File home10 = new File(home10path);
		File[] listOfFiles = home10.listFiles();
		Vector<File> jFiles = new Vector<File>();
		
		for(File files : listOfFiles) {
			String name = files.getName();
			String ext = "";
			
			int c;
			if((c = name.indexOf('.')) != -1)
				ext = name.substring(c, name.length());
			if(ext.equals(".jack"))
				jFiles.add(files);
		}

		Parser p;
		//VMOutput vmo;
		
		try {
				for(File f : jFiles) {
				System.out.println("------" + f.getName() + "------");
				jackfile = home10path + f.getName();
				path = home10path + f.getName().substring(0, f.getName().indexOf('.')) + "T";
				p = new Parser(jackfile, path);	
			}
				
		} 
		catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
