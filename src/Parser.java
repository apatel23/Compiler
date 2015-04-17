import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class Parser {
	private Tokenizer tk;
	FileWriter fw;
	private String indent;
	private int numIndent;
	
	/*
	 * Creates a new compilation engine with the given input and output. The next routine called must be compileClass()
	 */
	public Parser(String in, String out) throws FileNotFoundException, IOException {
		indent = "";
		numIndent = 0;
		
		File f = new File(out);
		
		if(!f.exists())
			f.createNewFile();
		
		fw = new FileWriter(f.getAbsoluteFile());
		
		tk = new Tokenizer(in);

		CompileClass();
		fw.close();
	}
	
	private void makeIndents() {
		indent = "";
		for(int i=0; i < numIndent; i++)
			indent += "  ";
	}
	
	/*
	 * Compiles a complete class
	 * 'class' className '{' classVarDec* subroutineDec* '}'
	 */
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
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
		System.out.println("</class>\n");
	}
	
	/*
	 * Compiles static declaration or a field declaration
	 * ('static' | 'field') type varName (',' varName)* ';'
	 * type: 'int' | 'char' | 'boolean' | className
	 * 
	 * Assumes CompileClassVarDec() will always advance to the next token in all cases. No tk.advance() necessary for compilations
	 * after it
	 */
	public void CompileClassVarDec() throws IOException {
		fw.write(indent + "<classVarDec>\n");
		System.out.println(indent + "<classVarDec>\n");
		numIndent++;
		makeIndents();

		if(tk.keyWord() != null && (tk.keyWord().equals("static") || tk.keyWord().equals("field"))) {
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
			fw.write(indent + "<keyword> " + t + " </keyword>\n");
			System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(tk.tokenType().equals(Token.IDENTIFIER)) {
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
				fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
	
	/*
	 * Compiles a complete method, function, or constructor
	 * subroutine: ('constructor' | 'function' | 'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
	 * type: 'int' | 'char' | 'boolean' | className
	 * subroutineName: identifier
	 * subroutineBody: '{' varDec* statements '}'
	 */
	public void CompileSubroutine() throws IOException {
		fw.write(indent + "<subroutineDec>\n");
		System.out.println(indent + "<subroutineDec>\n");
		numIndent++;
		makeIndents();
		
		//constructor | function | method
		String t = tk.keyWord();
		if(t != null && (t.equals("constructor") | t.equals("function") | t.equals("method"))) {
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
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//subroutineName
		tk.advance();
		if(tk.tokenType().equals(Token.IDENTIFIER)) {
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
		
		//subroutineBody: '{' varDec* statements '}'
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
		
		tk.advance();
		while(tk.keyWord() != null && tk.keyWord().equals("var")) {
			compileVarDec();
			tk.advance();
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
	
	/*
	 * Compiles a (possibly empty) parameter list, not including the enclosing "()"
	 * parameterList: ((type varName)(',' type varName)*)?
	 * 
	 * Assumption: Will advance to the next token automatically for any case;
	 */
	public void compileParameterList() throws IOException {
		fw.write(indent + "<parameterList>\n");
		System.out.println(indent + "<parameterList>\n");
		numIndent++;
		makeIndents();
		
		String t = tk.keyWord();
		Token type = tk.tokenType();
		if(t != null && type != null && (type.equals(Token.KEYWORD) || type.equals(Token.IDENTIFIER))) //Need to do it this way to handle ? operator in grammar
		{
			//type
			if(type.equals(Token.KEYWORD) && (t.equals("int") | t.equals("char") | t.equals("boolean"))) {
				fw.write(indent + "<keyword> " + t + "</keyword>\n");
				System.out.println(indent + "<keyword> " + t + "</keyword>\n");
			}
			else if(type.equals(Token.IDENTIFIER)) {
				fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			}
			else {
				System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
				System.exit(1);
			}
			
			//varName
			tk.advance();
			if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
				fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
					fw.write(indent + "<keyword> " + t + "</keyword>\n");
					System.out.println(indent + "<keyword> " + t + "</keyword>\n");
				}
				else if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
					fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
					System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				}
				else {
					System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
					System.exit(1);
				}
				
				//varName
				tk.advance();
				if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
					fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
	
	/*
	 * Compiles a var declaration
	 * varDec: 'var' type varName (',' varName)* ';'
	 * type: int | char | boolean | className
	 */
	public void compileVarDec() throws IOException {
		fw.write(indent + "<varDec>\n");
		System.out.println(indent + "<varDec>\n");
		numIndent++;
		makeIndents();
		
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
			fw.write(indent + "<keyword> " + t + " </keyword>\n");
			System.out.println(indent + "<keyword> " + t + " </keyword>\n");
		}
		else if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected variable type. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//varName
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
				fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
	
	/*
	 * Compiles a sequence of statements, not including the enclosing "{}"
	 * statements: statement*
	 * statement: letStatement | ifStatement | whileStatement | doStatement | returnStatement
	 * 
	 * Assume any time compileStatements() is used, that the compilation after it does not require an advance(). This method 
	 * automatically advances to the next token at the end
	 */
	public void compileStatements() throws IOException {
		fw.write(indent + "<statements>\n");
		System.out.println(indent + "<statements>\n");
		numIndent++;
		makeIndents();
		
		String t = tk.keyWord();
		while(t != null && (t.equals("let") || t.equals("if") || t.equals("while") || t.equals("do") || t.equals("return"))) {
			if(t.equals("let")) {
				compileLet();
				tk.advance();		//advance() at the end except for 'if' are necessary to match cases in 'if' when there's an 'else'. 'else' forces an advance to the next token
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
	
	/*
	 * Compiles a do statement
	 * 	doStatement: 'do' subroutineCall ';'
	 *	subroutineCall: subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
	 */
	public void compileDo() throws IOException {
		fw.write(indent + "<doStatement>\n");
		System.out.println(indent + "<doStatement>\n");
		numIndent++;
		makeIndents();
		
		//do
		if(tk.keyWord() != null && tk.keyWord().equals("do")) {
			fw.write(indent + "<keyword> do </keyword>\n");
			System.out.println(indent + "<keyword> do </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'do'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//subroutineCall
		//subroutineName
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {	//subroutineName | className | varName
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
		//.
		else if(tk.symbol() != '#' && tk.symbol() == '.') {
			fw.write(indent + "<symbol> . </symbol>\n");
			System.out.println(indent + "<symbol> . </symbol>\n");
			
			//subroutineName
			tk.advance();
			if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
				fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '(' or '.' . But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		CompileExpressionList();	//Make this method advance to next token for all cases. Just like ParameterList
		 
		//)
		if(tk.symbol() != '#' && tk.symbol() == ')') {
			fw.write(indent + "<symbol> ) </symbol>\n");
			System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			System.out.println("--Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
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
	
	/*
	 * Compiles a let statement
	 * letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
	 */
	public void compileLet() throws IOException {
		fw.write(indent + "<letStatement>\n");
		System.out.println(indent + "<letStatement>\n");
		numIndent++;
		makeIndents();
		
		//let
		if(tk.keyWord() != null && tk.keyWord().equals("let")) {
			fw.write(indent + "<keyword> let </keyword>\n");
			System.out.println(indent + "<keyword> let </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'let'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//varName
		tk.advance();
		if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
			fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
			System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected identifier. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//('[' expression ']')?
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '[') {
			//[
			fw.write(indent + "<symbol> [ </symbol>\n");
			System.out.println(indent + "<symbol> [ </symbol>\n");
			
			//expression
			tk.advance();
			CompileExpression();
			
			//]
			if(tk.symbol() != '#' && tk.symbol() == ']') {
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
			fw.write(indent + "<symbol> = </symbol>\n");
			System.out.println(indent + "<symbol> = </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol '='. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		//expression
		tk.advance();
		CompileExpression();
		
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
	
	/*
	 * Compiles a while statement
	 * whileStatement: 'while' '(' expression ')' '{' statements '}'
	 */
	public void compileWhile() throws IOException {
		fw.write(indent + "<whileStatement>\n");
		System.out.println(indent + "<whileStatement>\n");
		numIndent++;
		makeIndents();
		
		//while
		if(tk.keyWord() != null && tk.keyWord().equals("while")) {
			fw.write(indent + "<keyword> while </keyword>\n");
			System.out.println(indent + "<keyword> while </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'while'. But encountered: " + Tokenizer.currToken);
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
		
		//expression
		tk.advance();
		CompileExpression();
		
		//)
		if(tk.symbol() != '#' && tk.symbol() == ')') {
			fw.write(indent + "<symbol> ) </symbol>\n");
			System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
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
		
		//statements
		tk.advance();
		compileStatements();
		
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
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</whileStatement>\n");
		System.out.println(indent + "</whileStatement>\n");
	}
	
	/*
	 * Compiles a return statement
	 * returnStatement: 'return' expression? ';'
	 */
	public void compileReturn() throws IOException {
		fw.write(indent + "<returnStatement>\n");
		System.out.println(indent + "<returnStatement>\n");
		numIndent++;
		makeIndents();
		
		//return
		if(tk.keyWord() != null && tk.keyWord().equals("return")) {
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
				t.equals(Token.IDENTIFIER)) || (t.equals(Token.SYMBOL)) && (tk.symbol() == '(' || tk.symbol() == '-' || tk.symbol() == '~' ))
			CompileExpression();
			
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
	
	/*
	 * Compiles an if statement. possibly with a trailing else clause.
	 * ifStatement: 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
	 * 
	 * Assume that compileIf() automatically advances to the next token
	 */
	public void compileIf() throws IOException {
		fw.write(indent + "<ifStatement>\n");
		System.out.println(indent + "<ifStatement>\n");
		numIndent++;
		makeIndents();
		
		if(tk.keyWord() != null && tk.keyWord().equals("if")) {
			fw.write(indent + "<keyword> if </keyword>\n");
			System.out.println(indent + "<keyword> if </keyword>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected keyword 'if'. But encountered: " + Tokenizer.currToken);
			System.exit(1);
		}
		
		tk.advance();
		if(tk.symbol() != '#' && tk.symbol() == '(') {
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
			fw.write(indent + "<symbol> ) </symbol>\n");
			System.out.println(indent + "<symbol> ) </symbol>\n");
		}
		else {
			System.out.println("Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
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
		
		tk.advance();
		compileStatements();
		
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
			
			tk.advance();
			compileStatements();
			
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
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</ifStatement>\n");
		System.out.println(indent + "</ifStatement>\n");
	}
	
	/*
	 * Compiles an expression
	 * expression: term (op term)*
	 * op: '+' | '-' | '*' | '/' | '&' | '|' | '<' | '>' | '='
	 * 
	 * Assume that CompileExpression() advances to next token for all cases. Not like ExpressionList and ParameterList
	 * which prints its tags always even if the list is empty. Expression should not print if it's empty
	 */
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
			
			fw.write(indent + "<symbol> " + s + " </symbol>\n");
			System.out.println(indent + "<symbol> " + s + " </symbol>\n");
			
			tk.advance();
			CompileTerm();
			op = tk.symbol();
		}
		
		numIndent--;
		makeIndents();
		fw.write(indent + "</expression>\n");
		System.out.println(indent + "</expression>\n");
	}
	
	/*
	 * Compiles a term. This routine is faced with a slight difficulty when trying to decide between some of the alternative
	 * parsing rules. Specifically, if the current token is an identifier, the routine must distinguish between a variable, an
	 * array entry, and a subroutine call. A single look-ahead, which may be one of "[", "{", or "." suffices to distinguish
	 * between the three possibilities. Any other token is not part of this term and should not be advanced over.
	 * 
	 * term: integerConstant|stringConstant|keywordConstant|varName|varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
	 * 
	 * Assume CompileTerm() advances to the next token
	 */
	public void CompileTerm() throws IOException {
		fw.write(indent + "<term>\n");
		System.out.println(indent + "<term>\n");
		numIndent++;
		makeIndents();
		
		Token type = tk.tokenType();
		if(type != null) {
			//integerConstant
			if(type.equals(Token.INT_CONST)) {
				fw.write(indent + "<integerConstant> " + tk.intVal() + " </integerConstant>\n");
				System.out.println(indent + "<integerConstant> " + tk.intVal() + " </integerConstant>\n");
				tk.advance();
			}
			//stringConstant
			else if(type.equals(Token.STRING_CONST)) {
				fw.write(indent + "<stringConstant> " + tk.stringVal() + " </stringConstant>\n");
				System.out.println(indent + "<stringConstant> " + tk.stringVal() + " </stringConstant>\n");
				tk.advance();
			}
			//keywordConstant
			else if(type.equals(Token.KEYWORD)) {
				fw.write(indent + "<keyword> " + tk.keyWord() + " </keyword>\n");
				System.out.println(indent + "<keyword> " + tk.keyWord() + " </keyword>\n");
				tk.advance();
			}
			//varName
			else if(type.equals(Token.IDENTIFIER)) {
				//varName only
				fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
				
				//varName '[' expression ']'
				tk.advance(); //potentially skips these checks and advances to the next Tokenizer. Make the whole method skip to the next token
				if(tk.symbol() != '#' && tk.symbol() == '[') {
					fw.write(indent + "<symbol> [ </symbol>\n");
					System.out.println(indent + "<symbol> [ </symbol>\n");
					
					tk.advance();
					CompileExpression();
					
					if(tk.symbol() != '#' && tk.symbol() == ']') {
						fw.write(indent + "<symbol> ] </symbol>\n");
						System.out.println(indent + "<symbol> ] </symbol>\n");
					}
					else {
						System.out.println("Line " + tk.getNumLine() + ": Expected symbol ']'. But encountered: " + Tokenizer.currToken);
						System.exit(1);
					}
					
					tk.advance();
				}
				//subroutineCall: subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
				else if(tk.symbol() != '#' && (tk.symbol() == '(' || tk.symbol() == '.')) {
					if(tk.symbol() == '(') {	
						fw.write(indent + "<symbol> ( </symbol>\n");
						System.out.println(indent + "<symbol> ( </symbol>\n");
					}
					else if(tk.symbol() == '.') {
						fw.write(indent + "<symbol> . </symbol>\n");
						System.out.println(indent + "<symbol> . </symbol>\n");
						
						//subroutineName
						tk.advance();
						if(tk.tokenType() != null && tk.tokenType().equals(Token.IDENTIFIER)) {
							fw.write(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
							System.out.println(indent + "<identifier> " + tk.identifier() + " </identifier>\n");
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
					}	
					
					tk.advance();
					CompileExpressionList();	//Make this method advance to next token for all cases. Just like ParameterList

					//)
					if(tk.symbol() != '#' && tk.symbol() == ')') {
						fw.write(indent + "<symbol> ) </symbol>\n");
						System.out.println(indent + "<symbol> ) </symbol>\n");
					}
					else {
						System.out.println("Line " + tk.getNumLine() + ": Expected symbol ')'. But encountered: " + Tokenizer.currToken);
						System.exit(1);
					}
					
					tk.advance();
				}
				else {
					
				}
			}
			//'(' expression ')'
			else if(type.equals(Token.SYMBOL) && tk.symbol() == '(') {
				fw.write(indent + "<symbol> ( </symbol>\n");
				System.out.println(indent + "<symbol> ( </symbol>\n");
				
				tk.advance();
				CompileExpression();
				
				if(tk.symbol() != '#' && tk.symbol() == ')') {
					fw.write(indent + "<symbol> ) </symbol>\n");
					System.out.println(indent + "<symbol> ) </symbol>\n");
				}
				
				tk.advance();
			}
			//unaryOp term
			//unaryOp: '-' | '~'
			else if(type.equals(Token.SYMBOL) && (tk.symbol() == '-' || tk.symbol() == '~')) {
				fw.write(indent + "<symbol> " + tk.symbol() + " </symbol>\n");
				System.out.println(indent + "<symbol> " + tk.symbol() + " </symbol>\n");
				
				tk.advance();
				CompileTerm();
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
	
	/*
	 * Compiles a (possibly empty) comma-separated list of expressions
	 * expressionList: (expression (',' expression)* )?
	 * 
	 * Assume method advances to next token in all cases.
	 */
	public void CompileExpressionList() throws IOException {
		fw.write(indent + "<expressionList>\n");
		System.out.println(indent + "<expressionList>\n");
		numIndent++;
		makeIndents();
		
		Token t = tk.tokenType();
		if(t != null && (t.equals(Token.INT_CONST) || t.equals(Token.STRING_CONST) || t.equals(Token.KEYWORD) || 
				t.equals(Token.IDENTIFIER)) || (t.equals(Token.SYMBOL)) && (tk.symbol() == '(' || tk.symbol() == '-' || tk.symbol() == '~' )) {
		
			CompileExpression();
			
			while(tk.symbol() != '#' && tk.symbol() == ',') {
				fw.write(indent + "<symbol> , </symbol>\n");
				System.out.println(indent + "<symbol> , </symbol>\n");
				
				tk.advance();
				CompileExpression();
			}
		}	
		numIndent--;
		makeIndents();
		fw.write(indent + "</expressionList>\n");
		System.out.println(indent + "</expressionList>\n");
	}
}