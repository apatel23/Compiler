import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class VMOutput {

	FileWriter fw;
	
	// create new file, ready for writing
	public VMOutput(String output) throws IOException {
		File fe = new File(output);
		
		if(!fe.exists()) {
			fe.createNewFile();
		}
		
		fw = new FileWriter(fe.getAbsoluteFile());
	}

	// write a push command
	public void push(String segment, int index) throws IOException {
		String s = "";
		switch(segment) {
		case SymbolTable.ARG:
			s = "argument";
			break;
		case "CONST":
			s = "constant";
			break;
		case "LOCAL":
			s = "local";
			break;
		case SymbolTable.FIELD:
			s = "this";
			break;
		case "POINTER":
			s = "pointer";
			break;
		case SymbolTable.STATIC:
			s = "static";
			break;
		case "TEMP":
			s = "temp";
			break;
		case "THAT":
			s = "that";
			break;
		case "THIS":
			s = "this";
			break;
		}
		
		if(!s.isEmpty()) {
			fw.write("push " + s + " " + index + "\n");
		}
	}
	
	// writes a pop command
	public void pop(String segment, int index) throws IOException {
		String s = "";
		switch(segment) {
		case SymbolTable.ARG:
			s = "argument";
			break;
		case "CONST":
			s = "constant";
			break;
		case "LOCAL":
			s = "local";
			break;
		case SymbolTable.FIELD:
			s = "this";
			break;
		case "POINTER":
			s = "pointer";
			break;
		case SymbolTable.STATIC:
			s = "static";
			break;
		case "TEMP":
			s = "temp";
			break;
		case "THAT":
			s = "that";
			break;
		case "THIS":
			s = "this";
			break;
		}
		
		if(!s.isEmpty()) {
			fw.write("pop " + s + " " + index + "\n");
		}
	}
	
	
	public void arithmetic(String command) throws IOException {
		if(command.equals("ADD") || command.equals("SUB") || 
				command.equals("NEG") || command.equals("EQ") ||
				command.equals("GT") || command.equals("LT") ||
				command.equals("AND") || command.equals("OR") ||
				command.equals("NOT")) {
			command = command.toLowerCase();
			fw.write(command + "\n");
		} else {
			// invalid command
			return;
		}
	}
	
	public void label(String label) throws IOException {
		fw.write("label " + label + "\n");
	}
	
	public void goto_label(String label) throws IOException {
		fw.write("goto " + label + "\n");
	}
	
	public void if_label(String label) throws IOException {
		fw.write("if-goto " + label + "\n");
	}
	
	public void call(String name, int args) throws IOException {
		fw.write("call " + name + " " + args + "\n");
	}
	
	public void func(String name, int locals) throws IOException {
		fw.write("function " + name + " " + locals + "\n");
	}
	
	public void ret() throws IOException {
		fw.write("return\n");
	}
	
	public void comment(String msg) throws IOException {
		fw.write("// " + msg + "\n");
	}
	
	public void close() throws IOException {
		fw.close();
	}
}
