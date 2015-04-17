import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Tokenizer {
	
	//TOKENTYPE
	public static final String K = "KEYWORD";
	public static final String SYM = "SYMBOL";
	public static final String ID = "IDENTIFIER";
	public static final String INTC = "INT_CONST";
	public static final String STRC = "STRING_CONST";
	private Set<String> keyword;
	private Set<String> symbol;
	
	//KEYWORD
	private final String[] keywords = {"class", "method", "function", "constructor", "int", "boolean", "char", "void", "var",
			"static", "field", "let", "do", "if", "else", "while", "return", "true", "false", "null", "this"};
	private final char[] sym = {'{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'};
	
	public static String currToken;
	private int numLine;
	private String[] currLineTokens;
	private Token token;
	private int cltIdx;
	private Scanner sc;
	
	/*
	 * Opens the input file/stream and gets ready to tokenize it
	 */
	public Tokenizer(String filename)  throws FileNotFoundException {	
		sc = new Scanner(new FileReader(filename));
		cltIdx = 0;
		numLine = 1;
		token = null;
		setupKeyword();
		setupSymbol();
	}
	
	/*
	 * Do we have more tokens in the input?
	 */
	public boolean hasMoreTokens() throws IOException {
		return sc.hasNextLine();
	}
	
	/*
	 * Gets the next token from the input and makes it the current token. This method should only be called if hasMoreTokens()
	 * is true. Initially there is no current token
	 */
	public void advance() throws IOException {
		if(!hasMoreTokens())
			return;
		
		if(cltIdx == 0)
			getLine();
		
		do {
			currToken = currLineTokens[cltIdx];
			cltIdx++;
		}while(currToken.isEmpty());

		if(cltIdx == currLineTokens.length)
			cltIdx = 0;
	}
	
	public int getNumLine() {
		return numLine;
	}
	
	private void getLine() throws IOException {
		String currLine;
		do {
			currLine = sc.nextLine();
			numLine++;
			//Assume 1) /** comments start at the beginning of their own line, and 2) */ is at the end of its line 
			int d;
			if((d=currLine.indexOf("/**")) != -1) {
				int e;
				do {
					e=currLine.indexOf("*/");
					currLine = sc.nextLine();
				}while(e == -1);
			}
			
			if((d=currLine.indexOf("//")) != -1) {
				currLine = currLine.substring(0, d);
				//System.out.println("d:"+d);
			}
			
			currLine = handleQuotes(currLine);
			currLine = currLine.replaceAll("\\s+", " ");
			currLineTokens = currLine.split(" ");
		
		}while(currLine.isEmpty() | currLine.matches("\\s+"));
		
		Vector<String> v = new Vector<String>();
		for(int i=0; i < currLineTokens.length; i++)
		{
			if(currLineTokens[i].matches("[\\S]*[\"][\\S]*[\"][\\S]*"))
				currLineTokens[i] = currLineTokens[i].replaceAll("#s@", " ");
				
			String tok = currLineTokens[i];
			if(tok.isEmpty())
				continue;
			
			boolean hasSym = false;
			for(int j=0; j < sym.length; j++)
			{
				if(tok.equals(sym[j]+""))
					break;
				else if(tok.contains(sym[j]+""))
				{
					String tmp = "";
					hasSym = true;
					int lastIdx = 0;
					for(int k=0; k < tok.length(); k++)
					{
						for(int l=0; l < sym.length; l++)
						{
							if(tok.charAt(k) == sym[l])
							{
								if(lastIdx != k)
									v.add(tok.substring(lastIdx, k));
								v.add(sym[l] + "");
								lastIdx = k+1;
							}
						}
						
						if(lastIdx < tok.length() && k == tok.length()-1)
							v.add(tok.substring(lastIdx, tok.length()));
					}
					break;
				}
			}
			
			if(!hasSym)
				v.add(currLineTokens[i]);
			
			
		}
	
		String[] tmp = new String[v.size()];
		for(int i=0; i < v.size(); i++)
			tmp[i] = v.get(i);

		currLineTokens = tmp;
	}

	//Assume Quotes are on one line
	public String handleQuotes(String s) {
		String tmp = s;
		int c;
		if((c=s.indexOf("\"")) != -1) {
			tmp = "";
			c++;
			tmp += s.substring(0, c);
			char d;
			while((d=s.charAt(c)) != '\"') {
				if(d != ' ') 
					tmp += d;
				else
					tmp += "#s@";
				c++;
			}
			if(d == '\"') {
				tmp += "\"";
				c++;
				String tmp2 = s.substring(c, s.length());
				tmp += handleQuotes(tmp2);
			}
			else
				tmp += s;
		}
		return tmp;
	}
	
	/*
	 * Returns the type of the current token
	 */
	public Token tokenType() {
		if(currToken.isEmpty())
			return null;
		
		//keyword
		for(int i=0; i < keywords.length; i++)
			if(currToken.equals(keywords[i]))
				return Token.KEYWORD;
			
		//symbol
		for(int i=0; i < sym.length; i++)
			if(currToken.charAt(0) == sym[i])
				return Token.SYMBOL;
		
		//identifier
		if(currToken.matches("^[^\\d][\\w]*"))
			return Token.IDENTIFIER;
		
		//intc
		try {
			int i = Integer.parseInt(currToken);
			if(i >= 0 && i <= 32767)
				return Token.INT_CONST;
		}
		catch (NumberFormatException nfe) {
			//strc
			// Returns true if the string contains a arbitrary number of characters except b; regex: "([\\w&&[^b]])*"
			if(currToken.matches("^[\"]([(\\S| )&&[^\"]])*[\"]?"))
				return Token.STRING_CONST;
		}
		
		return null;
	}
	
	/*
	 * Returns the keyword which is the current token. Should be called only when tokenType() is KEYWORD
	 */
	public String keyWord() {
		if(!tokenType().equals(Token.KEYWORD)) 
			return null;

		return currToken;
	}
	
	/*
	 * Returns the character which is the current token. Should be called only when tokenType() is SYMBOL
	 */
	public char symbol() {
		if(!tokenType().equals(Token.SYMBOL))
			return '#';
		
		return currToken.charAt(0);
	}
	
	/*
	 * Returns the identifier which is the current token. Should be called only when tokenType() is IDENTIFIER
	 */
	public String identifier() {
		if(!tokenType().equals(Token.IDENTIFIER))
			return null;
		
		return currToken;
	}
	
	/*
	 * Returns the integer value of the current token. Should be called only when tokenType() is INT_CONST
	 */
	public int intVal() {
		if(!tokenType().equals(Token.INT_CONST))
			return Integer.MIN_VALUE;
		
		return Integer.parseInt(currToken);
	}
	
	/*
	 * Returns the string value of the current token, without the double quotes. Should be called only when tokenType()
	 * is STRING_CONST
	 */
	public String stringVal() {
		if(!tokenType().equals(Token.STRING_CONST))
			return null;
		//currToken.replaceAll("#s@", " ");
		return currToken.substring(1, currToken.length()-1);
	}
	
	public void setupKeyword() {
		keyword = new HashSet<String>();
		keyword.add("class");
		keyword.add("constructor");
		keyword.add("function");
		keyword.add("method");
		keyword.add("field");
		keyword.add("static");
		keyword.add("var");
		keyword.add("int");
		keyword.add("char");
		keyword.add("boolean");
		keyword.add("void");
		keyword.add("true");
		keyword.add("false");
		keyword.add("null");
		keyword.add("this");
		keyword.add("let");
		keyword.add("do");
		keyword.add("if");
		keyword.add("else");
		keyword.add("while");
		keyword.add("return");
	}
	
	public void setupSymbol() {
		symbol = new HashSet<String>();
		symbol.add("{");
		symbol.add("}");
		symbol.add("(");
		symbol.add(")");
		symbol.add("[");
		symbol.add("]");
		symbol.add(".");
		symbol.add(",");
		symbol.add(";");
		symbol.add("+");
		symbol.add("-");
		symbol.add("*");
		symbol.add("/");
		symbol.add("&");
		symbol.add("|");
		symbol.add("<");
		symbol.add(">");
		symbol.add("=");
		symbol.add("~");
	}
	
}