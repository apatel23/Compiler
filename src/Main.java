import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

public class Main {

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
		
		// Create Parser object
		Parser p;
		
		try {
				for(File f : jFiles) {
					String file = f.getName();
					System.out.println("Jack file: " + file);
					System.out.println("VM code outputted to: src/" + arg + 
									file.substring(0, file.length() - 4) + "vm" );
					jackfile = home10path + f.getName();
					path = home10path + f.getName().substring(0, f.getName().indexOf('.'));
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