import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class Parser {
	private Tokenizer tk;
	private FileWriter fw;
	private SymbolTable st;
	private VMOutput vmo;
	private String indent;
	private int numIndent;
	private String className;
	private String buff;
	
	private final String IF_TRUE = "IF_TRUE";
	private final String IF_FALSE = "IF_FALSE";
	private final String IF_END = "IF_END";
	private final String WHILE_EXP = "WHILE_EXP";
	private final String WHILE_END = "WHILE_END";
	private int parameters, if_counter, while_counter;
	
	/*
	 * Creates a new compilation engine with the given input and output. The next routine called must be compileClass()
	 */
	public Parser(String in, String out) throws FileNotFoundException, IOException {
		indent = "";
		numIndent = 0;
		parameters = 0;
		if_counter = 0;
		while_counter = 0;
		buff = "";
		className = "";
		
		File f = new File(out + ".xml");
		
		if(!f.exists())
			f.createNewFile();
		
		st = new SymbolTable();
		fw = new FileWriter(f.getAbsoluteFile());
		tk = new Tokenizer(in);
		fw = new FileWriter(f.getAbsoluteFile());
		vmo = new VMOutput(out + ".vm");
		

		CompileClass();
		fw.close();
	}
	
	private void makeIndents() {
		indent = "";
		for(int i=0; i < numIndent; i++)
			indent += "  ";
	}
	

	// 'class' className '{' classVarDec* subroutineDec* '}'
	public void CompileClass() throws IOException {
		tk.advance();
		fw.write("<class>\n");
		System.out.println("<class>\n");
		
		numIndent++;
		makeIndents();
		if(tk.keyWord() != null && tk.keyWord().equals("class")) {
			fw.write(indent + "<keyword> class </keyword>\n");
			System.out.println(indent + "<keyword> class </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'class'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			String extra = " (class, defined)";
			className = tk.identifier();
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '{') {
			fw.write(indent + "<symbol> { </symbol>\n");
			System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '{'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//classVarDec*
		tk.advance();
		while(tk.keyWord() != null && (tk.keyWord().equals("static") || tk.keyWord().equals("field"))) {
			CompileClassVarDec();
		}
		
		//subroutineDec*
		//no advance() needed before, by assumption of CompileClassVarDec()
		String t = tk.keyWord();
		while(t != null && (t.equals("constructor") | t.equals("function") | t.equals("method"))) {
			st.beginSubroutine(t);
			CompileSubroutine();
			tk.advance();
			t = tk.keyWord();
		}
		if(tk.symbol() != '#' && tk.symbol() == '}') {
			fw.write(indent + "<symbol> } </symbol>\n");
			System.out.println(indent + "<symbol> } </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '}'.  But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		numIndent--;
		makeIndents();
		fw.write("</class>\n");
		vmo.close();
		System.out.println("</class>\n");
	}
	

	 // ('static' | 'field') type varName (',' varName)* ';'
	 // Assumes CompileClassVarDec() will always advance to the next token in all cases. No tk.advance() necessary for compilations
	 // after it
	public void CompileClassVarDec() throws IOException {
		fw.write(indent + "<classVarDec>\n");
		System.out.println(indent + "<classVarDec>\n");
		numIndent++;
		makeIndents();
		
		String name, type, kind, d;
		name = "";
		type = "";
		kind = "";
		d = "";

		if(tk.keyWord() != null && (tk.keyWord().equals("static") || tk.keyWord().equals("field"))) {
			if(tk.keyWord().equals("static"))
				kind = SymbolTable.STATIC;
			else
				kind = SymbolTable.FIELD;
			
			fw.write(indent + "<keyword> " + tk.keyWord() + " </keyword>\n");	
			System.out.println(indent + "<keyword> " + tk.keyWord() + " </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'static' or 'field'.  But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		String t = tk.keyWord();
		if(t != null && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
			type = t;
			fw.write(indent + "<keyword> " + t + " </keyword>\n");
			System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(tk.tokenType().equals(Token.IDENTIFIER)) {
			String extra = "";
			if(st.index_of(tk.identifier()) == -1 &&
					st.kind_of(tk.identifier()).equals("NONE"))
				extra = " (class, used)";
			
			type = tk.identifier();
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			name = tk.identifier();
			
			if(st.index_of(name) == -1)
				st.define(name, type, kind);
			
			String extra = " (" + st.kind_of(name) + ", " + d + ", " + st.index_of(name) + ")";
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		while(tk.symbol() != '#' && tk.symbol() == ',') {
			fw.write(indent + "<symbol> , </symbol>\n");
			System.out.println(indent + "<symbol> , </symbol>\n");
			
			tk.advance();
			if(tk.tokenType().equals(Token.IDENTIFIER)) {
				name = tk.identifier();
				if(st.index_of(name) == -1)
					st.define(name, type, kind);
				String extra = " (" + st.kind_of(name) + ", " + d + ", " +
					st.index_of(name) + ")";
				fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			tk.advance();
		}
		
		if(tk.symbol() != '#' && tk.symbol() == ';') {
			fw.write(indent + "<symbol> ; </symbol>\n");
			System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ';'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</classVarDec>\n");
		System.out.println(indent + "</classVarDec>\n");
	}
	

	// subroutine: ('constructor' | 'function' | 'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
	// type: 'int' | 'char' | 'boolean' | className
	public void CompileSubroutine() throws IOException {
		fw.write(indent + "<subroutineDec>\n");
		System.out.println(indent + "<subroutineDec>\n");
		numIndent++;
		makeIndents();
		
		String name, STtype, kind, d, funcName, subType;
		name = "";
		STtype = "";
		kind = "";
		d = "defined";
		funcName = "";
		subType = "";
		
		//constructor | function | method
		String t = tk.keyWord();
		if(t != null && (t.equals("constructor") | t.equals("function") | t.equals("method"))) {
			subType = t;
			fw.write(indent + "<keyword> " + t + " </keyword>\n");
			System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'constructor', 'function', or 'method'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//void | type
		tk.advance();
		t = tk.keyWord();
		if(t != null && (t.equals("void") | t.equals("int") | t.equals("char") | t.equals("boolean"))) {
			fw.write(indent + "<keyword> " + t + " </keyword>\n");
			System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(tk.tokenType().equals(Token.IDENTIFIER)) {
			String extra = "";
			if(st.index_of(tk.identifier()) == -1 &&
					st.kind_of(tk.identifier()).equals("NONE"))
				extra = " (class, used)";
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//subroutineName
		tk.advance();
		if(tk.tokenType().equals(Token.IDENTIFIER)) {
			String extra = "";
			if(st.index_of(tk.identifier()) == -1 &&
					st.kind_of(tk.identifier()).equals("NONE"))
				extra = " (subroutine, defined)";
			
			funcName = tk.identifier();
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//(
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '(') {
			fw.write(indent + "<symbol> ( </symbol>\n");
			System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '('. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//parameterList
		tk.advance();
		compileParameterList();
		
		//). tk.advance() not needed b/c in compileParamList() in all cases it will go to next token, including the tk.adv() from before compileParamList()
		if(tk.symbol() != '#' && tk.symbol() == ')') {
			fw.write(indent + "<symbol> ) </symbol>\n");
			System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		// subroutineBody: '{' varDec* statements '}'
		fw.write(indent + "<subroutineBody>\n");
		System.out.println(indent + "<subroutineBody>\n");
		numIndent++;
		makeIndents();
		
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '{') {
			fw.write(indent + "<symbol> { </symbol>\n");
			System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '{'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		// varDec*
		tk.advance();
		while(tk.keyWord() != null && tk.keyWord().equals("var")) {
			compileVarDec();
			tk.advance();
		}
		
		vmo.func(className + "." + funcName, st.variable_count(SymbolTable.VAR));
		if(subType.equals("constructor")) {
			vmo.push("CONST", st.variable_count(SymbolTable.FIELD));
			vmo.call("Memory.alloc", 1);
			vmo.pop("POINTER", 0);
		} else if(subType.equals("method")) {
			vmo.push(SymbolTable.ARG, 0);
			vmo.pop("POINTER", 0);
		}
		
		compileStatements();
		
		//no advance() b/c of compileStatements() assumption
		if(tk.symbol() != '#' && tk.symbol() == '}') {
			fw.write(indent + "<symbol> } </symbol>\n");
			System.out.println(indent + "<symbol> } </symbol>\n");
		}	
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '}'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</subroutineBody>\n");
		System.out.println(indent + "</subroutineBody>\n");
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</subroutineDec>\n");
		System.out.println(indent + "</subroutineDec>\n");
	}
	
	// parameterList: ((type varName)(',' type varName)*)? 
	// Assumption: Will advance to the next token automatically for any case;
	public void compileParameterList() throws IOException {
		fw.write(indent + "<parameterList>\n");
		System.out.println(indent + "<parameterList>\n");
		numIndent++;
		makeIndents();
		
		String name, STtype, kind, d;
		name = "";
		STtype = "";
		kind = SymbolTable.ARG;
		d = "defined";
		
		String t = tk.keyWord();
		Token type = tk.tokenType();
		if(t != null && type != null && (type.equals(Token.KEYWORD) || type.equals(Token.IDENTIFIER))) //Need to do it this way to handle ? operator in grammar
		{
			//type
			if(type.equals(Token.KEYWORD) && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
				STtype = t;
				fw.write(indent + "<keyword> " + t + "</keyword>\n");
				System.out.println(indent + "<keyword> " + t + "</keyword>\n");
			}
			else if(type.equals(Token.IDENTIFIER)) {
				STtype = tk.identifier();
				String extra = "";
				if(st.index_of(tk.identifier()) == -1 && 
						st.kind_of(tk.identifier()).equals("NONE"))
					extra = " (classs, used)";
				fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			//varName
			tk.advance();
			if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
				name = tk.identifier();
				if(st.index_of(name) == -1)
					st.define(name, STtype, kind);
				String extra = " (" + st.kind_of(name) + ", " + d + ", " + st.index_of(name) + ")";
				fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			//,
			tk.advance();
			while(tk.symbol() != '#' && tk.symbol() == ',')
			{
				fw.write(indent + "<symbol> , </symbol>\n");
				System.out.println(indent + "<symbol> , </symbol>\n");
				
				//type
				tk.advance();
				t = tk.keyWord();
				if(t != null && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
					STtype = t;
					fw.write(indent + "<keyword> " + t + "</keyword>\n");
					System.out.println(indent + "<keyword> " + t + "</keyword>\n");
				}
				else if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
					STtype = tk.identifier();
					String extra = " (class, used)";
					fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
					System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				}
				else {
					System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
					System.exit(1);
				}
				
				//varName
				tk.advance();
				if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
					name = tk.identifier();
					if(st.index_of(name) == -1)
						st.define(name, STtype, kind);
					String extra = " (" + st.kind_of(name) + ", " + d + ", " + st.index_of(name) + ")";
					fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
					System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				}
				else {
					System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
					System.exit(1);
				}
				
				tk.advance();
			}

		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</parameterList>\n");
		System.out.println(indent + "</parameterList>\n");
	}
	
	// varDec: 'var' type varName (',' varName)* ';'
	// type: int | char | boolean | className
	public void compileVarDec() throws IOException {
		fw.write(indent + "<varDec>\n");
		System.out.println(indent + "<varDec>\n");
		numIndent++;
		makeIndents();
		
		String name, type, kind, d;
		name = "";
		type = "";
		kind = SymbolTable.VAR;
		d = "defined";
		
		//var
		if(tk.keyWord() != null && tk.keyWord().equals("var")) {
			fw.write(indent + "<keyword> var </keyword>\n");
			System.out.println(indent + "<keyword> var </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'var'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//type
		tk.advance();
		String t = tk.keyWord();
		if(t != null && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
			type = t;
			fw.write(indent + "<keyword> " + t + " </keyword>\n");
			System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			type = tk.identifier();
			String extra = "";
			if(st.index_of(tk.identifier()) == -1 &&
					st.kind_of(tk.identifier()).equals("NONE"))
				extra = " (class, used)";
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//varName
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			name = tk.identifier();
			if(st.index_of(name) == -1)
				st.define(name, type, kind);
			String extra = " (" + st.kind_of(name) + ", " + d + ", " + st.index_of(name) + ")";
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//(',' varName)*
		tk.advance();
		while(tk.symbol() != '#' && tk.symbol() == ',') {
			fw.write(indent + "<symbol> , </symbol>\n");
			System.out.println(indent + "<symbol> , </symbol>\n");
			
			tk.advance();
			if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
				name = tk.identifier();
				if(st.index_of(name) == -1)
					st.define(name, type, kind);
				String extra = " (" + st.kind_of(name) + ", " + d + ", " + st.index_of(name) + ")";
				fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			tk.advance();
		}
		
		//;
		if(tk.symbol() != '#' && tk.symbol() == ';') {
			fw.write(indent + "<symbol> ; </symbol>\n");
			System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ';'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</varDec>\n");
		System.out.println(indent + "</varDec>\n");
	}
	
	// statements: statement*
	// statement: letStatement | ifStatement | whileStatement | doStatement | returnStatement
	public void compileStatements() throws IOException {
		fw.write(indent + "<statements>\n");
		System.out.println(indent + "<statements>\n");
		numIndent++;
		makeIndents();
		
		String t = tk.keyWord();
		while(t != null && (t.equals("let") || t.equals("if") || t.equals("while") || t.equals("do") || t.equals("return"))) {
			if(t.equals("let")) {
				compileLet();
				tk.advance();		
				//advance() at the end except for 'if' are necessary to match cases in 'if' when there's an
				// 'else'. 'else' forces an advance to the next token
			}
			else if(t.equals("if")) {
				compileIf();
			}
			else if(t.equals("while")) {
				compileWhile();
				tk.advance();
			}
			else if(t.equals("do")) {
				compileDo();
				tk.advance();
			}
			else if(t.equals("return")) {
				compileReturn();
				tk.advance();
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'let', 'if', 'while', 'do', or 'return'. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			t = tk.keyWord();
		}
	
		numIndent--;
		makeIndents();
		fw.write(indent + "</statements>\n");
		System.out.println(indent + "</statements>\n");
	}
	

	// doStatement: 'do' subroutineCall ';'
	// subroutineCall: subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
	public void compileDo() throws IOException {
		fw.write(indent + "<doStatement>\n");
		System.out.println(indent + "<doStatement>\n");
		numIndent++;
		makeIndents();
		
		String d = "used";
		buff = "";
		
		//do
		if(tk.keyWord() != null && tk.keyWord().equals("do")) {
			buff += "do ";
			fw.write(indent + "<keyword> do </keyword>\n");
			System.out.println(indent + "<keyword> do </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'do'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		String vmSubName = "";
		
		//subroutineCall
		//subroutineName
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {	//subroutineName | className | varName
			String extra = "";
			if(st.index_of(tk.identifier()) == -1 &&
					st.kind_of(tk.identifier()).equals("NONE"))
				extra = " (subroutine or class, used) ";
			else
				extra = " (" +st.kind_of(tk.identifier()) + ", " + d + ", " + st.index_of(tk.identifier()) + ") ";
			vmSubName = tk.identifier();
			buff += tk.identifier();
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//(
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '(') {
			buff += "(";
			fw.write(indent + "<symbol> ( </symbol>\n");
			System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		//.
		else if(tk.symbol() != '#' && tk.symbol() == '.') {
			String o = vmSubName;
			vmSubName += ".";
			buff += ".";
			fw.write(indent + "<symbol> . </symbol>\n");
			System.out.println(indent + "<symbol> . </symbol>\n");
			
			// Methods outside of class
			if(st.index_of(o) != -1) {
				vmo.push(st.kind_of(o), st.index_of(o));
				parameters++;
				vmSubName = st.type_of(o) + ".";
			}
			
			//subroutineName
			tk.advance();
			if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
				String extra = "";
				if(st.index_of(tk.identifier()) == -1 && 
						st.kind_of(tk.identifier()).equals("NONE"))
					extra = " (subroutine, used)";
				vmSubName += tk.identifier();
				buff += tk.identifier();
				fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			//(
			tk.advance();
			if(tk.symbol() != '#' && tk.symbol() == '(') {
				buff += "(";
				fw.write(indent + "<symbol> ( </symbol>\n");
				System.out.println(indent + "<symbol> ( </symbol>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected symbol '('. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '(' or '.' . But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		// Methods in class
		int c;
		if((c = vmSubName.indexOf('.')) == -1) {
			vmo.push("POINTER", 0);
			vmSubName = className + "." + vmSubName;
			parameters++;
		}
		
		tk.advance();
		CompileExpressionList();// Make this method advance to next token for all cases. Just like ParameterList
		 
		//)
		if(tk.symbol() != '#' && tk.symbol() == ')') {
			buff += ")";
			fw.write(indent + "<symbol> ) </symbol>\n");
			System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			System.out.println("--Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vmo.comment(buff);
		vmo.call(vmSubName, parameters);
		vmo.pop("TEMP", 0);
		parameters = 0;
		
		//;
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == ';') {
			fw.write(indent + "<symbol> ; </symbol>\n");
			System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ';'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</doStatement>\n");
		System.out.println(indent + "</doStatement>\n");
	}
	

	// letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
	public void compileLet() throws IOException {
		fw.write(indent + "<letStatement>\n");
		System.out.println(indent + "<letStatement>\n");
		numIndent++;
		makeIndents();
		
		String d = "used";
		buff = "";
		boolean arr = false;
		
		//let
		if(tk.keyWord() != null && tk.keyWord().equals("let")) {
			buff += "let";
			fw.write(indent + "<keyword> let </keyword>\n");
			System.out.println(indent + "<keyword> let </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'let'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		String v = "";
		
		//varName
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			String extra = "";
			if(st.index_of(tk.identifier()) != -1 && !st.kind_of(tk.identifier()).equals("NONE"))
				extra = " (" + st.kind_of(tk.identifier()) + ", " + d + ", " + st.index_of(tk.identifier()) + ")";
			v = tk.identifier();
			buff += v + " ";
			
			fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//('[' expression ']')?
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '[') {
			arr = true;
			
			//[
			buff += "[";
			fw.write(indent + "<symbol> [ </symbol>\n");
			System.out.println(indent + "<symbol> [ </symbol>\n");
			vmo.push(st.kind_of(v), st.index_of(v));
			
			//expression
			tk.advance();
			CompileExpression();
			
			vmo.arithmetic("ADD");
			
			//]
			if(tk.symbol() != '#' && tk.symbol() == ']') {
				buff += "]";
				fw.write(indent + "<symbol> ] </symbol>\n");
				System.out.println(indent + "<symbol> ] </symbol>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected symbol ']'. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			tk.advance();
		}
		
		//no advance
		//=
		if(tk.symbol() != '#' && tk.symbol() == '=') {
			buff += "= ";
			if(arr) {
				//expression
				tk.advance();
				CompileExpression();
				vmo.pop("TEMP", 0);
				vmo.pop("POINTER", 1);
				vmo.push("TEMP", 0);
			} else {
				tk.advance();
				CompileExpression();
			}
			
			fw.write(indent + "<symbol> = </symbol>\n");
			System.out.println(indent + "<symbol> = </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '='. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		if(!st.kind_of(v).equals("NONE")) {
			if(arr) {
				vmo.pop("THAT", 0);
				vmo.comment(buff);
			} else {
				vmo.pop(st.kind_of(v), st.index_of(v));
				vmo.comment(buff);
			}
		}

		
		//;
		if(tk.symbol() != '#' && tk.symbol() == ';') {
			fw.write(indent + "<symbol> ; </symbol>\n");
			System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ';'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</letStatement>\n");
		System.out.println(indent + "</letStatement>\n");
	}
	
	// whileStatement: 'while' '(' expression ')' '{' statements '}'
	public void compileWhile() throws IOException {
		fw.write(indent + "<whileStatement>\n");
		System.out.println(indent + "<whileStatement>\n");
		numIndent++;
		makeIndents();
		
		buff = "";
		
		//while
		if(tk.keyWord() != null && tk.keyWord().equals("while")) {
			buff += "while";
			fw.write(indent + "<keyword> while </keyword>\n");
			System.out.println(indent + "<keyword> while </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'while'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vmo.label(WHILE_EXP + while_counter);
		
		//(
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '(') {
			buff += "(";
			fw.write(indent + "<symbol> ( </symbol>\n");
			System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '('. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//expression
		tk.advance();
		CompileExpression();
		
		//)
		if(tk.symbol() != '#' && tk.symbol() == ')') {
			buff += ")";
			fw.write(indent + "<symbol> ) </symbol>\n");
			System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vmo.comment(buff);
		vmo.arithmetic("NOT");
		
		//{
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '{') {
			fw.write(indent + "<symbol> { </symbol>\n");
			System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '{'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vmo.if_label(WHILE_END + while_counter);
		int while_counter2 = while_counter++;
		
		//statements
		tk.advance();
		compileStatements();
		
		vmo.goto_label(WHILE_EXP + while_counter2);
		
		//no advance b/c of compileStatements() assumption
		//}
		if(tk.symbol() != '#' && tk.symbol() == '}') {
			fw.write(indent + "<symbol> } </symbol>\n");
			System.out.println(indent + "<symbol> } </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '}'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}	
		
		vmo.label(WHILE_END + while_counter2);
		vmo.comment("END_WHILE " + while_counter2);
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</whileStatement>\n");
		System.out.println(indent + "</whileStatement>\n");
	}
	

	// returnStatement: 'return' expression? ';'
	public void compileReturn() throws IOException {
		fw.write(indent + "<returnStatement>\n");
		System.out.println(indent + "<returnStatement>\n");
		numIndent++;
		makeIndents();
		
		buff = "";
		
		//return
		if(tk.keyWord() != null && tk.keyWord().equals("return")) {
			buff += "return ";
			fw.write(indent + "<keyword> return </keyword>\n");
			System.out.println(indent + "<keyword> return </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'return'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		

		tk.advance();
		Token t = tk.tokenType();
		if(t != null && (t.equals(Token.INT_CONST) || t.equals(Token.STRING_CONST) || t.equals(Token.KEYWORD) || 
				t.equals(Token.IDENTIFIER)) || (t.equals(Token.SYMBOL)) && (tk.symbol() == '(' || tk.symbol() == '-' || tk.symbol() == '~' )) {
			CompileExpression();
			vmo.comment(buff);
			vmo.ret();
		} else {
			vmo.comment(buff);
			vmo.push("CONST", 0);
			vmo.ret();
		}
			
			
		//tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == ';') {
			fw.write(indent + "<symbol> ; </symbol>\n");
			System.out.println(indent + "<symbol> ; </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ';'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</returnStatement>\n");
		System.out.println(indent + "</returnStatement>\n");
	}
	
	// ifStatement: 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
	public void compileIf() throws IOException {
		fw.write(indent + "<ifStatement>\n");
		System.out.println(indent + "<ifStatement>\n");
		numIndent++;
		makeIndents();
		
		buff += "";
		
		if(tk.keyWord() != null && tk.keyWord().equals("if")) {
			buff += "if";
			fw.write(indent + "<keyword> if </keyword>\n");
			System.out.println(indent + "<keyword> if </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'if'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '(') {
			buff += "(";
			fw.write(indent + "<symbol> ( </symbol>\n");
			System.out.println(indent + "<symbol> ( </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '('. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//***CHECK ALL CASES OF EXPRESSION. Expression should advance for all cases
		tk.advance();
		CompileExpression();
		
		
		if(tk.symbol() != '#' && tk.symbol() == ')') {
			buff += ")";
			fw.write(indent + "<symbol> ) </symbol>\n");
			System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vmo.comment(buff);
		vmo.if_label(IF_TRUE + if_counter);
		vmo.goto_label(IF_FALSE + if_counter);
		int if_counter2 = if_counter++;
		
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '{') {
			fw.write(indent + "<symbol> { </symbol>\n");
			System.out.println(indent + "<symbol> { </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '{'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		vmo.label(IF_TRUE + if_counter2);
		tk.advance();
		compileStatements();
		vmo.comment("END_IF" + if_counter2);
		vmo.goto_label(IF_END + if_counter2);
		vmo.label(IF_FALSE + if_counter2); 
		
		//no advance b/c of compileStatements() assumption
		if(tk.symbol() != '#' && tk.symbol() == '}') {
			fw.write(indent + "<symbol> } </symbol>\n");
			System.out.println(indent + "<symbol> } </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '}'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance(); //Consider case when there's no else. token will advance regardless. Check for the rest of compileStatements()
		if(tk.keyWord() != null && tk.keyWord().equals("else")) {
			buff += "else";
			fw.write(indent + "<keyword> else </keyword>\n");
			System.out.println(indent + "<keyword> else </keyword>\n");
			
			tk.advance();
			if(tk.symbol() != '#' && tk.symbol() == '{') {
				fw.write(indent + "<symbol> { </symbol>\n");
				System.out.println(indent + "<symbol> { </symbol>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected symbol '{'. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			vmo.comment(buff);
			tk.advance();
			compileStatements();
			vmo.comment("END_ELSE" + if_counter2);
			
			//no advance b/c of compileStatements() assumption
			if(tk.symbol() != '#' && tk.symbol() == '}') {
				fw.write(indent + "<symbol> } </symbol>\n");
				System.out.println(indent + "<symbol> } </symbol>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected symbol '}'. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			tk.advance(); //To match case where there is no else;
		}
		
		vmo.label(IF_END + if_counter2); 
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</ifStatement>\n");
		System.out.println(indent + "</ifStatement>\n");
	}
	
	 // expression: term (op term)*
	 // op: '+' | '-' | '*' | '/' | '&' | '|' | '<' | '>' | '='	
	public void CompileExpression() throws IOException {
		fw.write(indent + "<expression>\n");
		System.out.println(indent + "<expression>\n");
		numIndent++;
		makeIndents();
		
		CompileTerm();
		
		char op = tk.symbol();
		while(op != '#' && (op == '+' || op == '-' || op == '*' || op == '/' || op == '&' || op == '|' || op == '<' ||
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
			
			buff += op;
			fw.write(indent + "<symbol> " + s + " </symbol>\n");
			System.out.println(indent + "<symbol> " + s + " </symbol>\n");
			tk.advance();
			CompileTerm();
			
			switch(op) {
			case '+':
				vmo.arithmetic("ADD");
				break;
			case '-':
				vmo.arithmetic("SUB");
				break;
			case '&':
				vmo.arithmetic("AND");
				break;
			case '|':
				vmo.arithmetic("OR");
				break;
			case '<':
				vmo.arithmetic("LT");
				break;
			case '>':
				vmo.arithmetic("GT");
				break;
			case '=':
				vmo.arithmetic("EQ");
				break;
			case '*':
				vmo.call("Math.multiply",2);
				break;
			case '/':
				vmo.call("Math.divide", 2);
				break;
			}
			

			op = tk.symbol();
		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</expression>\n");
		System.out.println(indent + "</expression>\n");
	}
	

	// term: integerConstant|stringConstant|keywordConstant|varName|varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
	public void CompileTerm() throws IOException {
		fw.write(indent + "<term>\n");
		System.out.println(indent + "<term>\n");
		numIndent++;
		makeIndents();
		
		String d = "used";
		
		Token type = tk.tokenType();
		if(type != null) {
			//integerConstant
			if(type.equals(Token.INT_CONST)) {
				buff += tk.intVal();
				fw.write(indent + "<integerConstant> " + tk.intVal() + " </integerConstant>\n");
				System.out.println(indent + "<integerConstant> " + tk.intVal() + " </integerConstant>\n");
				vmo.push("CONST", tk.intVal());
				tk.advance();
			}
			//stringConstant
			else if(type.equals(Token.STRING_CONST)) {
				String str = tk.stringVal();
				buff += "\"" + str + "\"";
				fw.write(indent + "<stringConstant> " + tk.stringVal() + " </stringConstant>\n");
				System.out.println(indent + "<stringConstant> " + tk.stringVal() + " </stringConstant>\n");
				tk.advance();
				
				vmo.push("CONST", str.length());
				vmo.call("String.new", 1);
				for(int i = 0; i < str.length(); i++) {
					vmo.push("CONST", (int)str.charAt(i));
					vmo.call("String.appendChar", 2);
				}
			}
			//keywordConstant
			else if(type.equals(Token.KEYWORD)) {
				buff += tk.keyWord();
				fw.write(indent + "<keyword> " + tk.keyWord() + " </keyword>\n");
				System.out.println(indent + "<keyword> " + tk.keyWord() + " </keyword>\n");
				
				if(tk.keyWord().equals("false"))
					vmo.push("CONST",0);
				else if(tk.keyWord().equals("true")) {
					vmo.push("CONST", 0);
					vmo.arithmetic("NOT");
				} else if(tk.keyWord().equals("null")) {
					vmo.push("CONST", 0);
				} else if(tk.keyWord().equals("this")) {
					vmo.push("POINTER", 0);
				}
				
				tk.advance();
			}
			//varName
			else if(type.equals(Token.IDENTIFIER)) {
				boolean array = false;
				String vmSubName = "";
				String o = tk.identifier();
				String extra = "";
				if(st.index_of(tk.identifier()) == -1 && st.kind_of(tk.identifier()).equals("NONE"))
					extra = " (class ot subroutine, " + d + ")";
				else 
					extra = " (" +st.kind_of(tk.identifier()) + ", " + d + ", " + 
								st.index_of(tk.identifier()) + ")"
;				
				//varName only
				fw.write(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + extra + " </identifier>\n");
				vmo.push(st.kind_of(tk.identifier()), st.index_of(tk.identifier()));
				vmSubName = tk.identifier();
				buff += tk.identifier();
				
				//varName '[' expression ']'
				tk.advance(); //potentially skips these checks and advances to the next Tokenizer. Make the whole method skip to the next token
				if(tk.symbol() != '#' && tk.symbol() == '[') {
					array = true;
					buff += "[";
					fw.write(indent + "<symbol> [ </symbol>\n");
					System.out.println(indent + "<symbol> [ </symbol>\n");
					
					tk.advance();
					CompileExpression();
					
					vmo.arithmetic("ADD");
					
					if(tk.symbol() != '#' && tk.symbol() == ']') {
						buff += "]";
						fw.write(indent + "<symbol> ] </symbol>\n");
						System.out.println(indent + "<symbol> ] </symbol>\n");
					}
					else {
						System.out.println("Line " + tk.getNumLine() + ": Expected symbol ']'. But encountered: " + Tokenizer.currToken);
						System.exit(1);
					}
					
					tk.advance();
					
					vmo.pop("POINTER", 1);
					vmo.push("THAT", 0);
				}
				//subroutineCall: subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
				else if(tk.symbol() != '#' && (tk.symbol() == '(' || tk.symbol() == '.')) {
					if(tk.symbol() == '(') {
						buff += "(";
						fw.write(indent + "<symbol> ( </symbol>\n");
						System.out.println(indent + "<symbol> ( </symbol>\n");
					}
					else if(tk.symbol() == '.') {
						buff += ".";
						if(st.index_of(vmSubName) != -1) {
							parameters++;
							vmSubName = st.type_of(vmSubName);
						}
						
						vmSubName += ".";
						fw.write(indent + "<symbol> . </symbol>\n");
						System.out.println(indent + "<symbol> . </symbol>\n");
						
						//subroutineName
						tk.advance();
						if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
							String extra2 = "";
							if(st.index_of(tk.identifier()) == -1 &&
									st.kind_of(tk.identifier()).equals("NONE"))
								extra2 = " (subroutine, used)";
//							else {
//								System.out.println("here");
//								System.out.println("Line " + tk.getNumLine() + ": Identifier '" + tk.identifier() + "' is already used as a field or variable.");
//								System.exit(1);
//							}
							
							vmSubName += tk.identifier();
							buff += tk.identifier();
							fw.write(indent + "<identifier> " + tk.identifier() + extra2 + " </identifier>\n");
							System.out.println(indent + "<identifier> " + tk.identifier() + extra2 + " </identifier>\n");
						}
						else {
							System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
							System.exit(1);
						}
						
						//(
						tk.advance();
						if(tk.symbol() != '#' && tk.symbol() == '(') {
							buff += "(";
							fw.write(indent + "<symbol> ( </symbol>\n");
							System.out.println(indent + "<symbol> ( </symbol>\n");
						}
						else {
							System.out.println("Line " + tk.getNumLine() + ": Expected symbol '('. But encountered: " + Tokenizer.currToken);
							System.exit(1);
						}
					}	
					
					tk.advance();
					CompileExpressionList();	//Make this method advance to next token for all cases

					//)
					if(tk.symbol() != '#' && tk.symbol() == ')') {
						buff += ")";
						fw.write(indent + "<symbol> ) </symbol>\n");
						System.out.println(indent + "<symbol> ) </symbol>\n");
					}
					else {
						System.out.println("Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
						System.exit(1);
					}
					
					vmo.call(vmSubName, parameters);
					parameters = 0;

					tk.advance();
				}

			}
			//'(' expression ')'
			else if(type.equals(Token.SYMBOL) && tk.symbol() == '(') {
				buff += "(";
				fw.write(indent + "<symbol> ( </symbol>\n");
				System.out.println(indent + "<symbol> ( </symbol>\n");
				
				tk.advance();
				CompileExpression();
				
				if(tk.symbol() != '#' && tk.symbol() == ')') {
					buff += ")";
					fw.write(indent + "<symbol> ) </symbol>\n");
					System.out.println(indent + "<symbol> ) </symbol>\n");
				}
				
				tk.advance();
			}
			//unaryOp term
			//unaryOp: '-' | '~'
			else if(type.equals(Token.SYMBOL) && (tk.symbol() == '-' || tk.symbol() == '~')) {
				buff += tk.symbol();
				fw.write(indent + "<symbol> " + tk.symbol() + " </symbol>\n");
				System.out.println(indent + "<symbol> " + tk.symbol() + " </symbol>\n");
				
				String command = "";
				if(tk.symbol() == '-')
					command = "NEG";
				else
					command = "NOT";
				
				tk.advance();
				CompileTerm();
				
				vmo.arithmetic(command);
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Invalid term. Encountered " + Tokenizer.currToken);
				System.exit(1);
			}
		}

		numIndent--;
		makeIndents();
		fw.write(indent + "</term>\n");
		System.out.println(indent + "</term>\n");
	}
	
	// expressionList: (expression (',' expression)* )?
	public void CompileExpressionList() throws IOException {
		fw.write(indent + "<expressionList>\n");
		System.out.println(indent + "<expressionList>\n");
		numIndent++;
		makeIndents();
		
		Token t = tk.tokenType();
		if(t != null && (t.equals(Token.INT_CONST) || t.equals(Token.STRING_CONST) || t.equals(Token.KEYWORD) || 
				t.equals(Token.IDENTIFIER)) || (t.equals(Token.SYMBOL)) && (tk.symbol() == '(' || tk.symbol() == '-' || tk.symbol() == '~' )) {
		
			CompileExpression();
			parameters++;
			
			while(tk.symbol() != '#' && tk.symbol() == ',') {
				buff += ", ";
				fw.write(indent + "<symbol> , </symbol>\n");
				System.out.println(indent + "<symbol> , </symbol>\n");
				
				tk.advance();
				CompileExpression();
				parameters++;
			}
		}	
		numIndent--;
		makeIndents();
		fw.write(indent + "</expressionList>\n");
		System.out.println(indent + "</expressionList>\n");
	}
}