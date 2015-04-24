import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

public class Tokenizer {
	
	// keyword
	private final String[] keywords = {"class", "method", "function", "constructor",
			"int", "boolean", "char", "void", "var", "static", "field", "let", "do",
			"if", "else", "while", "return", "true", "false", "null", "this"};
	private final char[] sym = {'{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-',
			'*', '/', '&', '|', '<', '>', '=', '~'};

	// token type
	public static final String K = "KEYWORD";
	public static final String SYM = "SYMBOL";
	public static final String ID = "IDENTIFIER";
	public static final String INTC = "INT_CONST";
	public static final String STRC = "STRING_CONST";

	public static String currToken;
	private int numLine;
	private String[] currLineTokens;
	private int cltIdx;
	private BufferedReader br;
	
	// open input file, get ready to parse it
	public Tokenizer(String filename)  throws FileNotFoundException {	
		br = new BufferedReader(new FileReader(filename));
		cltIdx = 0;
		numLine = 1;
	}
	
	// check for more tokens
	public boolean hasMoreTokens() throws IOException {
		return br.ready();
	}
	
	// get next token, parse it
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
			currLine = br.readLine();
			numLine++;
			int d;
			if((d=currLine.indexOf("/**")) != -1) {
				int e;
				do {
					e=currLine.indexOf("*/");
					currLine = br.readLine();
				}while(e == -1);
			}
			
			if((d=currLine.indexOf("//")) != -1) {
				currLine = currLine.substring(0, d);
			}
			
			currLine = handleQuotes(currLine);
			currLine = currLine.replaceAll("\\s+", " ");
			currLineTokens = currLine.split(" ");

		}while(currLine.isEmpty() | currLine.matches("\\s+"));

		Vector<String> v = new Vector<String>();
		for(int i=0; i < currLineTokens.length; i++)
		{
			if(currLineTokens[i].isEmpty())
				continue;

			String tok = currLineTokens[i];
			
			if(tok.isEmpty())
				continue;
			
			// Checks if there are symbols in the current token. 
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
						if(tok.charAt(k) == '\"') {
							k++;
							while(tok.charAt(k) != '\"')
								k++;
							String str = tok.substring(lastIdx, ++k);
							str = str.replaceAll("#s@", " ");
							v.add(str);
							lastIdx = k;
						}
						else {
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
	
	// handle quotes in a line
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
	
	// return type of current token
	public String tokenType() {
		if(currToken.isEmpty())
			return null;
		
		//keyword
		for(int i=0; i < keywords.length; i++)
			if(currToken.equals(keywords[i]))
				return K;
			
		// symbol
		for(int i=0; i < sym.length; i++)
			if(currToken.charAt(0) == sym[i])
				return SYM;
		
		// identifier
		if(currToken.matches("^[^\\d][\\w]*"))
			return ID;
		
		// int constant
		try {
			int i = Integer.parseInt(currToken);
			if(i >= 0 && i <= 32767)
				return INTC;
		}
		catch (NumberFormatException nfe) {
			if(currToken.matches("^[\"]([(\\S| )&&[^\"]])*[\"]"))
				return STRC;
		}
		
		return null;
	}
	
	// return keyword of current token
	public String keyWord() {
		if(!tokenType().equals(K)) 
			return null;

		return currToken;
	}
	
	// return the current symbol
	public char symbol() {
		if(!tokenType().equals(SYM))
			return '#';
		
		return currToken.charAt(0);
	}
	
	// return identifier of current token
	public String identifier() {
		if(!tokenType().equals(ID))
			return null;
		
		return currToken;
	}
	
	// return int of current token
	public int intVal() {
		if(!tokenType().equals(INTC))
			return Integer.MIN_VALUE;
		
		return Integer.parseInt(currToken);
	}
	
	// return string of current token
	public String stringVal() {
		if(!tokenType().equals(STRC))
			return null;
		return currToken.substring(1, currToken.length()-1);
	}
	
}