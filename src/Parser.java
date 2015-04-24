import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

// Parse the given Jack file, use the sub call-like structure to convert to VM code
public class Parser {
	private final String IF_T = "IF_TRUE";
	private final String IF_F = "IF_FALSE";
	private final String IF_E = "IF_END";
	private final String W_EX = "WHILE_EXP";
	private final String W_EN = "WHILE_END";
	private Tokenizer jt;
	private SymbolTable st;
	private VMOutput vw;
	private BufferedWriter bw;
	private String indent, className, buffer;
	private int numIndent, numParams, ifctr, whctr;
	
	// create a parser with input file and output file names
	public Parser(String in, String out) throws FileNotFoundException, IOException {
		indent = "";
		className = "";
		buffer = "";
		numIndent = 0;
		numParams = 0;
		ifctr = 0;
		whctr = 0;
		File f = new File(out + ".xml");
		
		if(!f.exists())
			f.createNewFile();
		
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		
		st = new SymbolTable();
		jt = new Tokenizer(in);
		bw = new BufferedWriter(fw);
		vw = new VMOutput(out + ".vm");
		
		CompileClass();
		bw.close();
	}
	
	private void makeIndents() {
		indent = "";
		for(int i=0; i < numIndent; i++)
			indent += "  ";
	}
	
	
	// Compile a class
	// 'class' className '{' classVarDec* subroutineDec* '}'
	public void CompileClass() throws IOException {
		jt.advance();
		bw.write("<class>\n");
		//System.out.println("<class>\n");
		
		numIndent++;
		makeIndents();
		if(jt.keyWord() != null && jt.keyWord().equals("class")) {
			bw.write(indent + "<keyword> class </keyword>\n");
			//System.out.println(indent + "<keyword> class </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword
			//'class'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		jt.advance();
		if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
			String extra = " (class, defined)";
			className = jt.identifier();
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra + 
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected identifier. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '{') {
			bw.write(indent + "<symbol> { </symbol>\n");
			//System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '{'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//classVarDec*
		jt.advance();
		while(jt.keyWord() != null && (jt.keyWord().equals("static") || 
				jt.keyWord().equals("field")))
			CompileClassVarDec();
		
		//subroutineDec*
		// no advance() needed before, by assumption of CompileClassVarDec()
		String t = jt.keyWord();
		while(t != null && (t.equals("constructor") | t.equals("function") | 
				t.equals("method"))) {
			st.startSubroutine(t);
			CompileSubroutine();
			jt.advance();
			t = jt.keyWord();
		}

		//jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '}') {
			bw.write(indent + "<symbol> } </symbol>\n");
			//System.out.println(indent + "<symbol> } </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '}'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		numIndent--;
		makeIndents();
		bw.write("</class>\n");
		//System.out.println("</class>\n");
		vw.close();
	}
	
	
	// Compile static or field variables
	// ('static' | 'field') type varName (',' varName)* ';'
	public void CompileClassVarDec() throws IOException {
		bw.write(indent + "<classVarDec>\n");
		//System.out.println(indent + "<classVarDec>\n");
		numIndent++;
		makeIndents();
		
		String name, type, kind, DorU;
		name = "";
		type = "";
		kind = "";
		DorU = "defined";
		
		//static|field
		if(jt.keyWord() != null && (jt.keyWord().equals("static") || 
				jt.keyWord().equals("field"))) {
			if(jt.keyWord().equals("static"))
				kind = SymbolTable.STATIC;
			else
				kind = SymbolTable.FIELD;
			
			bw.write(indent + "<keyword> " + jt.keyWord() + " </keyword>\n");	
			//System.out.println(indent + "<keyword> " + jt.keyWord() + " </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 'static' 
			//or 'field'.  But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//type
		jt.advance();
		String t = jt.keyWord();
		if(t != null && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
			type = t;
			bw.write(indent + "<keyword> " + t + " </keyword>\n");
			//System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(jt.tokenType().equals(Tokenizer.ID)) {
			String extra = "";
			if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
				extra = " (class, used)";
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Identifier '" + 
				//jt.identifier() + "' is already used as a field or variable.");
				System.exit(1);
			}
			
			type = jt.identifier();
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra + 
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected variable type. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//varName
		jt.advance();
		if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
			name = jt.identifier();
			
			//If the varName used is not found in the table, it is a valid name for a variable.
			if(st.IndexOf(name) == -1)
				st.Define(name, type, kind);
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Identifier '" + name +
				//"' is already used as a field or variable.");
				System.exit(1);
			}
			
			String extra = " (" + st.KindOf(name) + ", " + DorU + ", " + st.IndexOf(name) + ")";
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected identifier. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//,
		jt.advance();
		while(jt.symbol() != '#' && jt.symbol() == ',') {
			bw.write(indent + "<symbol> , </symbol>\n");
			//System.out.println(indent + "<symbol> , </symbol>\n");
			
			//varName
			jt.advance();
			if(jt.tokenType().equals(Tokenizer.ID)) {
				name = jt.identifier();
				if(st.IndexOf(name) == -1)	
					st.Define(name, type, kind);
				else {
					//System.out.println("Line " + jt.getNumLine() + ": Identifier '" 
					//+ name + "' is already used as a field or variable.");
					System.exit(1);
				}
				String extra = " (" + st.KindOf(name) + ", " + DorU + ", " + st.IndexOf(name) + ")";
				bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
				//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
				//" </identifier>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected identifier. 
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			jt.advance();
		}
		
		if(jt.symbol() != '#' && jt.symbol() == ';') {
			bw.write(indent + "<symbol> ; </symbol>\n");
			//System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ';'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		jt.advance();
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</classVarDec>\n");
		//System.out.println(indent + "</classVarDec>\n");
	}
	
	// Compile a constructor, function, or method
	// subroutine: ('constructor' | 'function' | 'method') ('void' | type) subroutineName 
	// '(' parameterList ')' subroutineBody
	public void CompileSubroutine() throws IOException {
		bw.write(indent + "<subroutineDec>\n");
		//System.out.println(indent + "<subroutineDec>\n");
		numIndent++;
		makeIndents();
		
		String name, STtype, kind, DorU, funcName, subType;
		name = "";
		STtype = "";
		kind = "";
		DorU = "defined";
		funcName = "";
		subType = "";
		
		//constructor | function | method
		String t = jt.keyWord();
		if(t != null && (t.equals("constructor") || t.equals("function") || t.equals("method"))) {
			subType = t;
			bw.write(indent + "<keyword> " + t + " </keyword>\n");
			//System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 
			//'constructor', 'function', or 'method'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//void | type
		jt.advance();
		t = jt.keyWord();
		if(t != null && (t.equals("void") | t.equals("int") | t.equals("char") |
				t.equals("boolean"))) {
			bw.write(indent + "<keyword> " + t + " </keyword>\n");
			//System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(jt.tokenType().equals(Tokenizer.ID)) {
			String extra = "";
			if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
				extra = " (class, used)";
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Identifier '" + 
				//jt.identifier() + "' is already used as a field or variable.");
				System.exit(1);
			}
			
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra + 
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected variable type. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//subroutineName
		jt.advance();
		if(jt.tokenType().equals(Tokenizer.ID)) {
			String extra = "";
			if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
				extra = " (subroutine, defined)";
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Identifier '" + 
				//jt.identifier() + "' is already used as a field or variable.");
				System.exit(1);
			}
			
			funcName = jt.identifier();
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected identifier.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//(
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '(') {
			bw.write(indent + "<symbol> ( </symbol>\n");
			//System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '('.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		
		//parameterList
		jt.advance();
		compileParameterList();
		
		//). 
		if(jt.symbol() != '#' && jt.symbol() == ')') {
			bw.write(indent + "<symbol> ) </symbol>\n");
			//System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ')'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//subroutineBody: '{' varDec* statements '}'
		bw.write(indent + "<subroutineBody>\n");
		//System.out.println(indent + "<subroutineBody>\n");
		numIndent++;
		makeIndents();
		
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '{') {
			bw.write(indent + "<symbol> { </symbol>\n");
			//System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '{'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//varDec*
		jt.advance();
		while(jt.keyWord() != null && jt.keyWord().equals("var")) {
			compileVarDec();
			jt.advance();
		}
				
		vw.writeFunction(className + "." + funcName, st.VarCount(SymbolTable.VAR));
		if(subType.equals("constructor")) {
			vw.writePush("CONST", st.VarCount(SymbolTable.FIELD));
			vw.writeCall("Memory.alloc", 1);
			vw.writePop("POINTER", 0);
		}
		else if(subType.equals("method")) {
			vw.writePush(SymbolTable.ARG, 0);
			vw.writePop("POINTER", 0);
		}
		
		compileStatements();
		
		if(jt.symbol() != '#' && jt.symbol() == '}') {
			bw.write(indent + "<symbol> } </symbol>\n");
			//System.out.println(indent + "<symbol> } </symbol>\n");
		}	
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '}'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</subroutineBody>\n");
		//System.out.println(indent + "</subroutineBody>\n");
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</subroutineDec>\n");
		//System.out.println(indent + "</subroutineDec>\n");
	}
	
	// Compile a parameter list
	// parameterList: ((type varName)(',' type varName)*)?
	public void compileParameterList() throws IOException {
		bw.write(indent + "<parameterList>\n");
		//System.out.println(indent + "<parameterList>\n");
		numIndent++;
		makeIndents();
		
		String name, STtype, kind, DorU;
		name = "";
		STtype = "";
		kind = SymbolTable.ARG;
		DorU = "defined";
		
		String t = jt.keyWord();
		String type = jt.tokenType();
		if((t != null && type != null && (type.equals(Tokenizer.K)) ||
				type.equals(Tokenizer.ID))) {
			//type
			if(type.equals(Tokenizer.K) && (t.equals("int") | t.equals("char") |
					t.equals("boolean"))) {
				STtype = t;
				bw.write(indent + "<keyword> " + t + "</keyword>\n");
				//System.out.println(indent + "<keyword> " + t + "</keyword>\n");
			}
			else if(type.equals(Tokenizer.ID)) {
				STtype = jt.identifier();
				String extra = "";
				
				if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
					extra = " (class, used)";
				else {
					//System.out.println("Line " + jt.getNumLine() + ": Identifier '" + 
					//jt.identifier() + "' is already used as a field or variable.");
					System.exit(1);
				}
				
				bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
				//System.out.println(indent + "<identifier> " + jt.identifier() +
				//extra + " </identifier>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected variable type. 
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			//varName
			jt.advance();
			if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
				name = jt.identifier();
				if(st.IndexOf(name) == -1)
					st.Define(name, STtype, kind);
				else {
					//System.out.println("Line " + jt.getNumLine() + ": Identifier '" +
					//name + "' is already used as a field or variable.");
					System.exit(1);
				}
				String extra = " (" + st.KindOf(name) + ", " + DorU + ", " + st.IndexOf(name) + ")";
				bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
				//System.out.println(indent + "<identifier> " + jt.identifier() + extra + 
				//" </identifier>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected identifier. 
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			//,
			jt.advance();
			while(jt.symbol() != '#' && jt.symbol() == ',')
			{
				bw.write(indent + "<symbol> , </symbol>\n");
				//System.out.println(indent + "<symbol> , </symbol>\n");
				
				//type
				jt.advance();
				t = jt.keyWord();
				if(t != null && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
					STtype = t;
					bw.write(indent + "<keyword> " + t + "</keyword>\n");
					//System.out.println(indent + "<keyword> " + t + "</keyword>\n");
				}
				else if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
					STtype = jt.identifier();
					String extra = " (class, used)";
					bw.write(indent + "<identifier> " + jt.identifier() + extra + 
							" </identifier>\n");
					//System.out.println(indent + "<identifier> " + jt.identifier() + 
					//extra + " </identifier>\n");
				}
				else {
					//System.out.println("Line " + jt.getNumLine() + ": Expected variable type.
					//But encountered: " + Tokenizer.currToken);
					System.exit(1);
				}
				
				//varName
				jt.advance();
				if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
					name = jt.identifier();
					if(st.IndexOf(name) == -1)
						st.Define(name, STtype, kind);
					else {
						//System.out.println("Line " + jt.getNumLine() + ": Identifier '" +
						//name + "' is already used as a field or variable.");
						System.exit(1);
					}
					String extra = " (" + st.KindOf(name) + ", " + DorU + ", " + st.IndexOf(name) + ")";
					bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
					//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
					//" </identifier>\n");
				}
				else {
					//System.out.println("Line " + jt.getNumLine() + ": Expected identifier. 
					//But encountered: " + Tokenizer.currToken);
					System.exit(1);
				}
				
				jt.advance();
			}

		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</parameterList>\n");
		//System.out.println(indent + "</parameterList>\n");
	}

	// Compile a variable declaration
	// varDec: 'var' type varName (',' varName)* ';'
	public void compileVarDec() throws IOException {
		bw.write(indent + "<varDec>\n");
		//System.out.println(indent + "<varDec>\n");
		numIndent++;
		makeIndents();
		
		String name, type, kind, DorU;
		name = "";
		type = "";
		kind = SymbolTable.VAR;
		DorU = "defined";
		
		//var
		if(jt.keyWord() != null && jt.keyWord().equals("var")) {
			bw.write(indent + "<keyword> var </keyword>\n");
			//System.out.println(indent + "<keyword> var </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 'var'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//type
		jt.advance();
		String t = jt.keyWord();
		if(t != null && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
			type = t;
			bw.write(indent + "<keyword> " + t + " </keyword>\n");
			//System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
			type = jt.identifier();
			String extra = "";
			if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
				extra = " (class, used)";
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Identifier '" +
				//jt.identifier() + "' is already used as a field or variable.");
				System.exit(1);
			}
			
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected variable type.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//varName
		jt.advance();
		if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
			name = jt.identifier();
			if(st.IndexOf(name) == -1)
				st.Define(name, type, kind);
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Identifier '" +
				//name + "' is already used as a field or variable.");
				System.exit(1);
			}
			
			String extra = " (" + st.KindOf(name) + ", " + DorU + ", " + st.IndexOf(name) + ")";
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected identifier.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//(',' varName)*
		jt.advance();
		while(jt.symbol() != '#' && jt.symbol() == ',') {
			bw.write(indent + "<symbol> , </symbol>\n");
			//System.out.println(indent + "<symbol> , </symbol>\n");
			
			jt.advance();
			if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
				name = jt.identifier();
				if(st.IndexOf(name) == -1)
					st.Define(name, type, kind);
				else {
					//System.out.println("Line " + jt.getNumLine() + ": Identifier '" +
					//name + "' is already used as a field or variable.");
					System.exit(1);
				}
				String extra = " (" + st.KindOf(name) + ", " + DorU + ", " + st.IndexOf(name) + ")";
				bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
				//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
				//" </identifier>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected identifier.
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			jt.advance();
		}
		
		//;
		if(jt.symbol() != '#' && jt.symbol() == ';') {
			bw.write(indent + "<symbol> ; </symbol>\n");
			//System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ';'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</varDec>\n");
		//System.out.println(indent + "</varDec>\n");
	}
	

	// Compile a sequence of statements
	// statements: statement*
	// statement: letStatement | ifStatement | whileStatement | doStatement | returnStatement
	public void compileStatements() throws IOException {
		bw.write(indent + "<statements>\n");
		//System.out.println(indent + "<statements>\n");
		numIndent++;
		makeIndents();
		
		String t = jt.keyWord();
		while(t != null && (t.equals("let") || t.equals("if") || t.equals("while") ||
				t.equals("do") || t.equals("return"))) {
			if(t.equals("let")) {
				compileLet();
				jt.advance();		
			}
			else if(t.equals("if"))
				compileIf();
			else if(t.equals("while")) {
				compileWhile();
				jt.advance();
			}
			else if(t.equals("do")) {
				compileDo();
				jt.advance();
			}
			else if(t.equals("return")) {
				compileReturn();
				jt.advance();
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 
				//'let', 'if', 'while', 'do', or 'return'. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			t = jt.keyWord();
		}
	
		numIndent--;
		makeIndents();
		bw.write(indent + "</statements>\n");
		//System.out.println(indent + "</statements>\n");
	}
	

	// Compile a do statement
	// doStatement: 'do' subroutineCall ';'
	public void compileDo() throws IOException {
		bw.write(indent + "<doStatement>\n");
		//System.out.println(indent + "<doStatement>\n");
		numIndent++;
		makeIndents();
		
		String DorU = "used";
		buffer = "";
		
		//do
		if(jt.keyWord() != null && jt.keyWord().equals("do")) {
			buffer += "do ";
			bw.write(indent + "<keyword> do </keyword>\n");
			//System.out.println(indent + "<keyword> do </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 'do'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		String vmSubName = "";
		jt.advance();
		
		if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {	
			String extra = "";
			if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
				extra = " (subroutine or class, used) ";
			else
				extra = " (" + st.KindOf(jt.identifier()) + ", " + DorU + ", " +
							st.IndexOf(jt.identifier()) + ") ";
			
			vmSubName = jt.identifier();
			buffer += jt.identifier();
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected identifier.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//(
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '(') {
			buffer += "(";
			bw.write(indent + "<symbol> ( </symbol>\n");
			//System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		//.
		else if(jt.symbol() != '#' && jt.symbol() == '.') {
			String obj = vmSubName;
			vmSubName += ".";
			buffer += ".";
			bw.write(indent + "<symbol> . </symbol>\n");
			//System.out.println(indent + "<symbol> . </symbol>\n");
			
			// methods outside of class
			if(st.IndexOf(obj) != -1) {
				vw.writePush(st.KindOf(obj), st.IndexOf(obj));
				numParams++;
				vmSubName = st.TypeOf(obj) + ".";
			}
			
			//subroutineName
			jt.advance();
			if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
				String extra = "";
				if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
					extra = " (subroutine, used)";
				else {
					//System.out.println("Line " + jt.getNumLine() + ": Identifier '" + 
					//jt.identifier() + "' is already used as a field or variable.");
					System.exit(1);
				}
				
				vmSubName += jt.identifier();
				buffer += jt.identifier();
				bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
				//System.out.println(indent + "<identifier> " + jt.identifier() +
				//extra + " </identifier>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected identifier.
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			//(
			jt.advance();
			if(jt.symbol() != '#' && jt.symbol() == '(') {
				buffer += "(";
				bw.write(indent + "<symbol> ( </symbol>\n");
				//System.out.println(indent + "<symbol> ( </symbol>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '('. 
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '(' or '.' .
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		// Methods in the class
		int c;
		if((c=vmSubName.indexOf('.')) == -1) {
			vw.writePush("POINTER", 0);
			vmSubName = className + "." + vmSubName;
			numParams++;
		}
		
		jt.advance();
		CompileExpressionList();	
		
		//)
		if(jt.symbol() != '#' && jt.symbol() == ')') {
			buffer += ")";
			bw.write(indent + "<symbol> ) </symbol>\n");
			//System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ')'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//Assume at this point the parameter/expression is evaluated and pushed onto
		//the stack already
		vw.writeComment(buffer);
		vw.writeCall(vmSubName, numParams);
		vw.writePop("TEMP", 0);
		numParams = 0;
		
		//;
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == ';') {
			bw.write(indent + "<symbol> ; </symbol>\n");
			//System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ';'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</doStatement>\n");
		//System.out.println(indent + "</doStatement>\n");
	}
	
	// Compile a let statement
	// letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
	public void compileLet() throws IOException {
		bw.write(indent + "<letStatement>\n");
		//System.out.println(indent + "<letStatement>\n");
		numIndent++;
		makeIndents();
		
		String DorU = "used";
		buffer = "";
		boolean array = false;
		
		//let
		if(jt.keyWord() != null && jt.keyWord().equals("let")) {
			buffer += "let ";
			bw.write(indent + "<keyword> let </keyword>\n");
			//System.out.println(indent + "<keyword> let </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 'let'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		String vName = "";
		
		//varName
		jt.advance();
		if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
			String extra = "";
			if(st.IndexOf(jt.identifier()) != -1 && !st.KindOf(jt.identifier()).equals("NONE"))
				extra = " (" + st.KindOf(jt.identifier()) + ", " + DorU + ", " +
							st.IndexOf(jt.identifier()) + ")";
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Identifier '" +
				//jt.identifier() + "' is not declared.");
				System.exit(1);
			}
			vName = jt.identifier();
			buffer += vName + " ";
			bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
			//System.out.println(indent + "<identifier> " + jt.identifier() + extra + 
			//" </identifier>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected identifier.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//('[' expression ']')?
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '[') {
			array = true;
			
			//[
			buffer += "[";
			bw.write(indent + "<symbol> [ </symbol>\n");
			//System.out.println(indent + "<symbol> [ </symbol>\n");
			vw.writePush(st.KindOf(vName), st.IndexOf(vName));			
			
			//expression
			jt.advance();
			CompileExpression();
			
			vw.WriteArithmetic("ADD");
			
			//]
			if(jt.symbol() != '#' && jt.symbol() == ']') {
				buffer += "]";
				bw.write(indent + "<symbol> ] </symbol>\n");
				//System.out.println(indent + "<symbol> ] </symbol>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ']'. 
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}	
			jt.advance();
		}
		
		//=
		if(jt.symbol() != '#' && jt.symbol() == '=') {
			buffer += "= ";
			
			if(array) {
				//expression
				jt.advance();
				CompileExpression();
				vw.writePop("TEMP", 0);
				vw.writePop("POINTER", 1);
				vw.writePush("TEMP", 0);
			}
			else {
				jt.advance();
				CompileExpression();
			}
			
			bw.write(indent + "<symbol> = </symbol>\n");
			//System.out.println(indent + "<symbol> = </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '='. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		

		//Assume the right-hand side value is pushed onto the stack already
		if(!st.KindOf(vName).equals("NONE")) {
			if(array) {
				vw.writePop("THAT", 0);
				vw.writeComment(buffer);
			}
			else {
				vw.writePop(st.KindOf(vName), st.IndexOf(vName));
				vw.writeComment(buffer);
			}
			//System.out.println(vName+" kind: " + st.KindOf(vName) + " index: " + 
			//st.IndexOf(vName) + "************");
		}
			
		//;
		if(jt.symbol() != '#' && jt.symbol() == ';') {
			bw.write(indent + "<symbol> ; </symbol>\n");
			//System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ';'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</letStatement>\n");
		//System.out.println(indent + "</letStatement>\n");
	}
	
	// Compile a while statement
	// whileStatement: 'while' '(' expression ')' '{' statements '}'
	public void compileWhile() throws IOException {
		bw.write(indent + "<whileStatement>\n");
		//System.out.println(indent + "<whileStatement>\n");
		numIndent++;
		makeIndents();
		
		buffer = "";
		
		//while
		if(jt.keyWord() != null && jt.keyWord().equals("while")) {
			buffer += "while";
			bw.write(indent + "<keyword> while </keyword>\n");
			//System.out.println(indent + "<keyword> while </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 'while'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vw.WriteLabel(W_EX + whctr);
		
		//(
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '(') {
			buffer += "(";
			bw.write(indent + "<symbol> ( </symbol>\n");
			//System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '('.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//expression
		jt.advance();
		CompileExpression();
		
		//)
		if(jt.symbol() != '#' && jt.symbol() == ')') {
			buffer += ")";
			bw.write(indent + "<symbol> ) </symbol>\n");
			//System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ')'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}

		vw.writeComment(buffer);
		vw.WriteArithmetic("NOT"); // complement
		
		//{
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '{') {
			bw.write(indent + "<symbol> { </symbol>\n");
			//System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '{'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vw.WriteIf(W_EN + whctr);
		int whctr2 = whctr++;
		
		//statements
		jt.advance();
		compileStatements();
		
		vw.WriteGoto(W_EX + whctr2);
		
		//}
		if(jt.symbol() != '#' && jt.symbol() == '}') {
			bw.write(indent + "<symbol> } </symbol>\n");
			//System.out.println(indent + "<symbol> } </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '}'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vw.WriteLabel(W_EN + whctr2);
		vw.writeComment("END_WHILE"+whctr2);
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</whileStatement>\n");
		//System.out.println(indent + "</whileStatement>\n");
	}
	
	// Compile a return statement
	public void compileReturn() throws IOException {
		bw.write(indent + "<returnStatement>\n");
		//System.out.println(indent + "<returnStatement>\n");
		numIndent++;
		makeIndents();
		
		buffer = "";
		
		//return
		if(jt.keyWord() != null && jt.keyWord().equals("return")) {
			buffer += "return ";
			bw.write(indent + "<keyword> return </keyword>\n");
			//System.out.println(indent + "<keyword> return </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 'return'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		jt.advance();
		String t = jt.tokenType();
		if(t != null && (t.equals(Tokenizer.INTC) || t.equals(Tokenizer.STRC) ||
				t.equals(Tokenizer.K) || 
				t.equals(Tokenizer.ID)) || (t.equals(Tokenizer.SYM)) &&
				(jt.symbol() == '(' || jt.symbol() == '-' || jt.symbol() == '~' )) {
			CompileExpression();
			vw.writeComment(buffer);
			vw.writeReturn();
		}
		else {
			//void subroutine case
			vw.writeComment(buffer);
			vw.writePush("CONST", 0);
			vw.writeReturn();
		}
			
		//jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == ';') {
			bw.write(indent + "<symbol> ; </symbol>\n");
			//System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ';'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</returnStatement>\n");
		//System.out.println(indent + "</returnStatement>\n");
	}
	
	// Compile an if statement
	// ifStatement: 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
	public void compileIf() throws IOException {
		bw.write(indent + "<ifStatement>\n");
		//System.out.println(indent + "<ifStatement>\n");
		numIndent++;
		makeIndents();
		
		buffer = "";
		
		if(jt.keyWord() != null && jt.keyWord().equals("if")) {
			buffer += "if";
			bw.write(indent + "<keyword> if </keyword>\n");
			//System.out.println(indent + "<keyword> if </keyword>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected keyword 'if'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '(') {
			buffer += "(";
			bw.write(indent + "<symbol> ( </symbol>\n");
			//System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '('.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		// check expression
		jt.advance();
		CompileExpression();
		
		if(jt.symbol() != '#' && jt.symbol() == ')') {
			buffer += ")";
			bw.write(indent + "<symbol> ) </symbol>\n");
			//System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ')'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vw.writeComment(buffer);
		vw.WriteIf(IF_T + ifctr);
		vw.WriteGoto(IF_F + ifctr);
		int ifctr2 = ifctr++;		// Need this to handle nested if-else cases
		
		jt.advance();
		if(jt.symbol() != '#' && jt.symbol() == '{') {
			bw.write(indent + "<symbol> { </symbol>\n");
			//System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '{'. 
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vw.WriteLabel(IF_T + ifctr2);
		jt.advance();
		compileStatements();
		vw.writeComment("END_IF"+ifctr2);
		vw.WriteGoto(IF_E + ifctr2);
		vw.WriteLabel(IF_F + ifctr2);
		
		if(jt.symbol() != '#' && jt.symbol() == '}') {
			bw.write(indent + "<symbol> } </symbol>\n");
			//System.out.println(indent + "<symbol> } </symbol>\n");
		}
		else {
			//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '}'.
			//But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		jt.advance(); 
		if(jt.keyWord() != null && jt.keyWord().equals("else")) {
			buffer = "else";
			bw.write(indent + "<keyword> else </keyword>\n");
			//System.out.println(indent + "<keyword> else </keyword>\n");
			
			jt.advance();
			if(jt.symbol() != '#' && jt.symbol() == '{') {
				bw.write(indent + "<symbol> { </symbol>\n");
				//System.out.println(indent + "<symbol> { </symbol>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '{'.
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			vw.writeComment(buffer);
			jt.advance();
			compileStatements();
			vw.writeComment("END_ELSE" + ifctr2);

			if(jt.symbol() != '#' && jt.symbol() == '}') {
				bw.write(indent + "<symbol> } </symbol>\n");
				//System.out.println(indent + "<symbol> } </symbol>\n");
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '}'.
				//But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			jt.advance(); //To match case where there is no else;
		}
		
		vw.WriteLabel(IF_E + ifctr2);
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</ifStatement>\n");
		//System.out.println(indent + "</ifStatement>\n");
	}
	
	// Compile an expression
	// expression: term (op term)*
	// op: '+' | '-' | '*' | '/' | '&' | '|' | '<' | '>' | '='
	public void CompileExpression() throws IOException {
		bw.write(indent + "<expression>\n");
		//System.out.println(indent + "<expression>\n");
		numIndent++;
		makeIndents();
		
		CompileTerm();
		
		char op = jt.symbol();
		while(op != '#' && (op == '+' || op == '-' || op == '*' || op == '/' ||
				op == '&' || op == '|' || op == '<' ||
				op == '>' || op == '=')) {
			String s = "";
			
			if(op == '<')
				s = "&lt;";
			else if(op == '>')
				s = "&gt;";
			else if(op == '&')
				s = "&amp;";
			else
				s = op + "";
			
			buffer += op;
			bw.write(indent + "<symbol> " + s + " </symbol>\n");
			//System.out.println(indent + "<symbol> " + s + " </symbol>\n");
			
			jt.advance();
			CompileTerm();
						
			
			// binary operations
			switch(op) {
			case '+':		
				vw.WriteArithmetic("ADD");
				break;
			case '-':		
				vw.WriteArithmetic("SUB");
				break;
			case '&':	
				vw.WriteArithmetic("AND");
				break;
			case '|':
				vw.WriteArithmetic("OR");
				break;
			case '<':
				vw.WriteArithmetic("LT");
				break;
			case '>':
				vw.WriteArithmetic("GT");
				break;
			case '=': {
				vw.WriteArithmetic("EQ");
				break;
								
			}
			case '*':		
				vw.writeCall("Math.multiply", 2);
				break;
			case '/':	
				vw.writeCall("Math.divide", 2);
				break;
			}
			
			op = jt.symbol();
		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</expression>\n");
		//System.out.println(indent + "</expression>\n");
	}
	
	// Compiles a term
	// term: integerConstant|stringConstant|keywordConstant|varName|varName 
	// '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
	public void CompileTerm() throws IOException {
		bw.write(indent + "<term>\n");
		//System.out.println(indent + "<term>\n");
		numIndent++;
		makeIndents();
		
		String DorU = "used";
		
		String type = jt.tokenType();
		if(type != null) {
			//integerConstant
			if(type.equals(Tokenizer.INTC)) {
				buffer += jt.intVal();
				bw.write(indent + "<integerConstant> " + jt.intVal() + " </integerConstant>\n");
				//System.out.println(indent + "<integerConstant> " + jt.intVal() +
				//" </integerConstant>\n");
				vw.writePush("CONST", jt.intVal());
				jt.advance();
			}
			//stringConstant
			else if(type.equals(Tokenizer.STRC)) {
				String str = jt.stringVal();
				buffer += "\"" + jt.stringVal() + "\"";
				bw.write(indent + "<stringConstant> " + jt.stringVal() + " </stringConstant>\n");
				//System.out.println(indent + "<stringConstant> " + jt.stringVal() +
				//" </stringConstant>\n");
				jt.advance();
				
				vw.writePush("CONST", str.length());
				vw.writeCall("String.new", 1);
				for(int i=0; i < str.length(); i++) {
					vw.writePush("CONST", (int)str.charAt(i));
					vw.writeCall("String.appendChar", 2);
				}
			}
			//keywordConstant
			else if(type.equals(Tokenizer.K)) {
				buffer += jt.keyWord();
				bw.write(indent + "<keyword> " + jt.keyWord() + " </keyword>\n");
				//System.out.println(indent + "<keyword> " + jt.keyWord() + " </keyword>\n");
				
				if(jt.keyWord().equals("false"))
					vw.writePush("CONST", 0);
				else if(jt.keyWord().equals("true")) {
					vw.writePush("CONST", 0);
					vw.WriteArithmetic("NOT");
				}
				else if(jt.keyWord().equals("null"))
					vw.writePush("CONST", 0);
				else if(jt.keyWord().equals("this"))
					vw.writePush("POINTER", 0);
				
				jt.advance();
			}
			//varName | subroutineName | className
			else if(type.equals(Tokenizer.ID)) {
				boolean array = false;
				String vmSubName = "";
				String obj = jt.identifier();
				String extra = "";
				if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
					extra = " (class or subroutine, " + DorU + ")";
				else 
					extra = " (" + st.KindOf(jt.identifier()) + ", " + DorU + ", " + 
							st.IndexOf(jt.identifier()) + ")";
				
				//varName only
				bw.write(indent + "<identifier> " + jt.identifier() + extra + " </identifier>\n");
				//System.out.println(indent + "<identifier> " + jt.identifier() + extra +
				//" </identifier>\n");
				vw.writePush(st.KindOf(jt.identifier()), st.IndexOf(jt.identifier()));
				vmSubName = jt.identifier();
				buffer += jt.identifier();
				
				//varName '[' expression ']'
				jt.advance(); // skip to next token
				if(jt.symbol() != '#' && jt.symbol() == '[') {
					array = true;
					buffer += "[";
					bw.write(indent + "<symbol> [ </symbol>\n");
					//System.out.println(indent + "<symbol> [ </symbol>\n");
					
					jt.advance();
					CompileExpression();
					
					vw.WriteArithmetic("ADD");
					
					if(jt.symbol() != '#' && jt.symbol() == ']') {
						buffer += "]";
						bw.write(indent + "<symbol> ] </symbol>\n");
						//System.out.println(indent + "<symbol> ] </symbol>\n");
					}
					else {
						//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ']'.
						//But encountered: " + Tokenizer.currToken);
						System.exit(1);
					}
					
					jt.advance();
					
					vw.writePop("POINTER", 1);
					vw.writePush("THAT", 0);
				}
				// subroutineCall: subroutineName '(' expressionList ')' | (className | varName)
				// '.' subroutineName '(' expressionList ')'
				else if(jt.symbol() != '#' && (jt.symbol() == '(' || jt.symbol() == '.')) {
					if(jt.symbol() == '(') {
						buffer += "(";
						bw.write(indent + "<symbol> ( </symbol>\n");
						//System.out.println(indent + "<symbol> ( </symbol>\n");
					}
					else if(jt.symbol() == '.') {
						buffer += ".";
						if(st.IndexOf(vmSubName) != -1) {
							numParams++;
							vmSubName = st.TypeOf(vmSubName);
						}
						
						vmSubName += ".";
						bw.write(indent + "<symbol> . </symbol>\n");
						//System.out.println(indent + "<symbol> . </symbol>\n");
						
						//subroutineName
						jt.advance();
						if(jt.tokenType() != null && jt.tokenType().equals(Tokenizer.ID)) {
							String extra2 = "";
							if(st.IndexOf(jt.identifier()) == -1 && st.KindOf(jt.identifier()).equals("NONE"))
								extra2 = " (subroutine, used)";
							else {
								//System.out.println("Line " + jt.getNumLine() + ": Identifier '" +
								//jt.identifier() + "' is already used as a field or variable.");
								System.exit(1);
							}
							
							vmSubName += jt.identifier();
							buffer += jt.identifier();
							bw.write(indent + "<identifier> " + jt.identifier() + extra2 + " </identifier>\n");
							//System.out.println(indent + "<identifier> " + jt.identifier() +
							//extra2 + " </identifier>\n");
						}
						else {
							//System.out.println("Line " + jt.getNumLine() + ": Expected identifier.
							//But encountered: " + Tokenizer.currToken);
							System.exit(1);
						}
						
						//(
						jt.advance();
						if(jt.symbol() != '#' && jt.symbol() == '(') {
							buffer += "(";
							bw.write(indent + "<symbol> ( </symbol>\n");
							//System.out.println(indent + "<symbol> ( </symbol>\n");
						}
						else {
							//System.out.println("Line " + jt.getNumLine() + ": Expected symbol '('. 
							//But encountered: " + Tokenizer.currToken);
							System.exit(1);
						}
					}	
					
					jt.advance();
					CompileExpressionList();	
					
					//)
					if(jt.symbol() != '#' && jt.symbol() == ')') {
						buffer += ")";
						bw.write(indent + "<symbol> ) </symbol>\n");
						//System.out.println(indent + "<symbol> ) </symbol>\n");
					}
					else {
						//System.out.println("Line " + jt.getNumLine() + ": Expected symbol ')'. 
						//But encountered: " + Tokenizer.currToken);
						System.exit(1);
					}
					
					//Assume CompileExpressionList() pushes all params onto the stack.
					vw.writeCall(vmSubName, numParams);
					numParams = 0;
					
					jt.advance();
				}

			}
			//'(' expression ')'
			else if(type.equals(Tokenizer.SYM) && jt.symbol() == '(') {
				buffer += "(";
				bw.write(indent + "<symbol> ( </symbol>\n");
				//System.out.println(indent + "<symbol> ( </symbol>\n");
				
				jt.advance();
				CompileExpression();
				
				if(jt.symbol() != '#' && jt.symbol() == ')') {
					buffer += ")";
					bw.write(indent + "<symbol> ) </symbol>\n");
					//System.out.println(indent + "<symbol> ) </symbol>\n");
				}
				
				jt.advance();
			}
			
			//unaryOp term
			//unaryOp: '-' | '~'
			else if(type.equals(Tokenizer.SYM) && (jt.symbol() == '-' || jt.symbol() == '~')) {
				buffer += jt.symbol();
				bw.write(indent + "<symbol> " + jt.symbol() + " </symbol>\n");
				//System.out.println(indent + "<symbol> " + jt.symbol() + " </symbol>\n");
				
				String cmd = "";
				if(jt.symbol() == '-')
					cmd = "NEG";
				else
					cmd = "NOT";
				
				jt.advance();
				CompileTerm();
				
				vw.WriteArithmetic(cmd);
			}
			else {
				//System.out.println("Line " + jt.getNumLine() + ": Invalid term. 
				//Encountered " + Tokenizer.currToken);
				System.exit(1);
			}
		}
	
		numIndent--;
		makeIndents();
		bw.write(indent + "</term>\n");
		//System.out.println(indent + "</term>\n");
	}
	
	// Compile expression list
	// expressionList: (expression (',' expression)* )?
	public void CompileExpressionList() throws IOException {
		bw.write(indent + "<expressionList>\n");
		//System.out.println(indent + "<expressionList>\n");
		numIndent++;
		makeIndents();
		
		String t = jt.tokenType();
		if(t != null && (t.equals(Tokenizer.INTC) || t.equals(Tokenizer.STRC) ||
				t.equals(Tokenizer.K) || t.equals(Tokenizer.ID)) || 
				(t.equals(Tokenizer.SYM)) && (jt.symbol() == '(' || 
				jt.symbol() == '-' || jt.symbol() == '~' )) {
			
			CompileExpression();
			numParams++;
			
			while(jt.symbol() != '#' && jt.symbol() == ',') {
				buffer += ", ";
				bw.write(indent + "<symbol> , </symbol>\n");
				//System.out.println(indent + "<symbol> , </symbol>\n");
				
				jt.advance();
				CompileExpression();
				numParams++;
			}
		}
		
		numIndent--;
		makeIndents();
		bw.write(indent + "</expressionList>\n");
		//System.out.println(indent + "</expressionList>\n");
	}
}