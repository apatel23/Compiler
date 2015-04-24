import java.util.Hashtable;
import java.util.Enumeration;

// symbol table for an identifier w/ kind, type and index
// Uses subclass: Values for behavior
public class SymbolTable {
	public static final String STATIC = "STATIC";
	public static final String FIELD = "FIELD";
	public static final String ARG = "ARG";
	public static final String VAR = "VAR";
	
	private Hashtable<String, Values> classScope;
	private Hashtable<String, Values> subScope;
	private Hashtable<String, Values> currScope;
	private int subArgIdx, subVarIdx;
	private int classStaticIdx, classFieldIdx;
	
	// create empty symbol table
	public SymbolTable() {
		classScope = new Hashtable<String, Values>();
		subScope = new Hashtable<String, Values>();
		currScope = classScope;
		subArgIdx = 0;
		subVarIdx = 0;
		classStaticIdx = 0;
		classFieldIdx = 0;
	}
	
	// create identifier with name, type and kind. Gives it a scope
	public void Define(String name, String type, String kind) {
		int i = -1;
		Values tmp = null;
		
		if(kind.equals(STATIC) || kind.equals(FIELD)) {
			switch(kind) {
			case STATIC:	
				i = classStaticIdx++;
				break;
			case FIELD:				
				i = classFieldIdx++;
				break;
			}
			tmp = classScope.put(name, new Values(type, kind, i));
			
			if(tmp != null) {
				System.out.println("Multiple declarations of class identifier: " + name);
				System.exit(1);
			}
		}
		else if(kind.equals(ARG) || kind.equals(VAR)) {
			switch(kind) {
			case ARG:
				i = subArgIdx++;
				break;
			case VAR:
				i = subVarIdx++;
				break;
			}
			tmp = subScope.put(name, new Values(type, kind, i));

			if(tmp != null) {
				System.out.println("Multiple declarations of subroutine identifier: " + name);
				System.exit(1);
			}
		}
		else
			throw new IllegalArgumentException("Identifier '" + name + "' has an invalid 'kind': " + kind);
	}
	
	// return number of variables in the given kind
	public int VarCount(String kind) {
		int count = 0;
		Hashtable<String, Values> tmpScope = null;
		Enumeration<String> e;
		
		if(kind.equals(SymbolTable.VAR) || kind.equals(SymbolTable.ARG))
			tmpScope = subScope;
		else if(kind.equals(SymbolTable.FIELD) || kind.equals(SymbolTable.STATIC))
			tmpScope = classScope;
		else {
			System.out.println("Expected static, field, argument, or variable kind.");
			System.exit(1);
		}
		
		e = tmpScope.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			if(tmpScope.get(key) != null && tmpScope.get(key).getKind().equals(kind))
				count++;
		}
		return count;
	}
	
	// create subroutine scope
	public void startSubroutine(String subtype) {
		subScope.clear();
		currScope = subScope;
		subArgIdx = subtype.equals("method") ? 1 : 0;
		subVarIdx = 0;
	}
	
	// returns index of identifier
	public int IndexOf(String name) {
		Values tmp = currScope.get(name);
		if(tmp != null)
			return tmp.getIndex();

		if(currScope != classScope) {
			tmp = classScope.get(name);
			if(tmp != null)
				return tmp.getIndex();
		}
		return -1;
	}
	
	// returns the kind of the identifier
	public String KindOf(String name) {
		Values tmp = currScope.get(name);
		String kind = null;
		
		if(tmp != null)
			return tmp.getKind();
		
		if(currScope != classScope) {
			tmp = classScope.get(name);
			if(tmp != null)
				return tmp.getKind();
		}
		
		return "NONE";
	}
	
	// returns type of identifier
	public String TypeOf(String name) {
		Values tmp = currScope.get(name);
		
		if(tmp != null)
			return tmp.getType();
		
		if(currScope != classScope) {
			tmp = classScope.get(name);
			if(tmp != null)
				return tmp.getType();
		}
		
		return null;
	}
	
}