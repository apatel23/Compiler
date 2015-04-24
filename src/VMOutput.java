import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

// Writes Virtual Machine code into a .vm file
public class VMOutput {
	
	private BufferedWriter bw;
	
	// create new file
	public VMOutput(String out) throws IOException {
		File f = new File(out);
		
		if(!f.exists())
			f.createNewFile();
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		
		bw = new BufferedWriter(fw);
	}
	
	// write a push command
	public void writePush(String segment, int index) throws IOException {
		String seg = "";
		switch(segment) {
		case "CONST":			
			seg = "constant";
			break;
		case SymbolTable.ARG:		
			seg = "argument";
			break;
		case "LOCAL":			
			seg = "local";
			break;
		case SymbolTable.VAR:		
			seg = "local";
			break;
		case SymbolTable.STATIC:	
			seg = "static";
			break;
		case SymbolTable.FIELD:	 	
			seg = "this";
			break;
		case "THIS":			
			seg = "this";
			break;
		case "THAT":			
			seg = "that";
			break;
		case "POINTER":			
			seg = "pointer";
			break;
		case "TEMP":			
			seg = "temp";
			break;
		}
		
		if(!seg.isEmpty())
			bw.write("push " + seg + " " + index + "\n");
	}
	
	// write a pop command
	public void writePop(String segment, int index) throws IOException {
		String seg = "";
		switch(segment) {
		case "CONST":			
			seg = "constant";
			break;
		case SymbolTable.ARG:		
			seg = "argument";
			break;
		case "LOCAL":			
			seg = "local";
			break;
		case SymbolTable.VAR:		
			seg = "local";
			break;
		case SymbolTable.STATIC:	
			seg = "static";
			break;
		case SymbolTable.FIELD:	 	
			seg = "this";
			break;
		case "THIS":			
			seg = "this";
			break;
		case "THAT":			
			seg = "that";
			break;
		case "POINTER":			
			seg = "pointer";
			break;
		case "TEMP":			
			seg = "temp";
			break;
		}

		if(!seg.isEmpty())
			bw.write("pop " + seg + " " + index + "\n");
	}
	
	// write arithmetic command
	public void WriteArithmetic(String command) throws IOException {
		if(command.equals("ADD") || command.equals("SUB") || command.equals("NEG") ||
				command.equals("EQ") || command.equals("GT") || command.equals("LT") || 
				command.equals("AND") || command.equals("OR") || command.equals("NOT")) {
			command = command.toLowerCase(); //may not be necessary
			bw.write(command + "\n");
		}
		else {
			System.out.println("Invalid VM command: " + command);
			System.exit(1);
		}
	}
	
	// write label command
	public void WriteLabel(String label) throws IOException {
		bw.write("label " + label + "\n");
	}
	
	// write goto command
	public void WriteGoto(String label) throws IOException {
		bw.write("goto " + label + "\n");
	}
	
	// write if-goto command
	public void WriteIf(String label) throws IOException {
		bw.write("if-goto " + label + "\n");
	}
	
	// write call command
	public void writeCall(String name, int nArgs) throws IOException {
		bw.write("call " + name + " " + nArgs + "\n");
	}
	
	// write function command
	public void writeFunction(String name, int nLocals) throws IOException {
		bw.write("function " + name + " " + nLocals + "\n");
	}
	
	// write return command
	public void writeReturn() throws IOException {
		bw.write("return\n");
	}
	
	// write comment
	public void writeComment(String msg) throws IOException {
		bw.write("//" + msg + "\n");
	}
	
	// close the output file
	public void close() throws IOException {
		bw.close();
	}
}