import java.util.Enumeration;
import java.util.Hashtable;

public class SymbolTable {

	public static final String STATIC = "STATIC";
	public static final String FIELD = "FIELD";
	public static final String ARG = "ARG";
	public static final String VAR = "VAR";
	private int sub_arg_index;
	private int sub_var_index;
	private int class_static_index;
	private int class_field_index;
	
	// symbol table values by scope: class, subroutine, current
	private Hashtable<String, Values_ST> class_scope;
	private Hashtable<String, Values_ST> sub_scope;
	private Hashtable<String, Values_ST> current_scope;
	
	// Constructor, create empty symbol table
	public SymbolTable() {
		class_scope = new Hashtable<String, Values_ST>();
		sub_scope = new Hashtable<String, Values_ST>();
		current_scope = new Hashtable<String, Values_ST>();
		sub_arg_index = 0;
		sub_var_index = 0;
		class_static_index = 0;
		class_field_index = 0;
	}
	
	public void beginSubroutine(String subtype) {
		sub_scope.clear(); // empty sub_scope
		current_scope = sub_scope;
		sub_arg_index = subtype.equals("method") ? 1 : 0; // instance of method
		sub_var_index = 0;
	}
	
	public void define(String name, String type, String kind) {
		int i = -1;
		Values_ST tmp = null;
		
		if(kind.equals(STATIC) || kind.equals(FIELD)) {
			switch(kind) {
			case STATIC:
				i = class_static_index++;
				break;
			case FIELD:
				i = class_field_index++;
				break;
			}
			tmp = class_scope.put(name, new Values_ST(type,kind, i));
			if(tmp != null) {
				// multiple declarations of class identifier
				return;
			}
		} else if(kind.equals(ARG) || kind.equals(VAR)) {
			switch(kind) {
			case ARG:
				i = sub_arg_index++;
				break;
			case VAR:
				i = sub_var_index++;
				break;
			}
			tmp = sub_scope.put(name, new Values_ST(type,kind, i));
			if(tmp != null) {
				// multiple declarations of sub_routine identifier
				return;
			}
		} else {
			System.out.println("invalid: " + name + ", " + kind);
		}
		
		System.out.println("class_scope: " + class_scope.toString());
		System.out.println("sub_scope: " + sub_scope.toString());
	}
	
	// count the number of variables in the proper scope
	public int variable_count(String kind) {
		int count = 0;
		Hashtable<String, Values_ST> temp_scope = null;
		Enumeration<String> e;
		
		if(kind.equals(SymbolTable.VAR) || kind.equals(SymbolTable.ARG))
			temp_scope = sub_scope;
		else if(kind.equals(SymbolTable.FIELD))
			temp_scope = class_scope;
		else {
			System.out.println("no kind");
		}
		e = temp_scope.keys();
		while(e.hasMoreElements()) {
			String k = e.nextElement();
			if(temp_scope.get(k) != null && temp_scope.get(k).getKind().equals(kind))
				count++;
		}
		
		return count;
	}
	
	
	// return kind of identifier
	public String kind_of(String name) {
		Values_ST temp = current_scope.get(name);
		String kind = null;
		
		if(temp != null) {
			return temp.getKind();
		}
		if(current_scope != class_scope) {
			temp = class_scope.get(name);
			if(temp != null) {
				return temp.getKind();
			}
		}
		return "0";
	}
	
	// return type of identifier
	public String type_of(String name) {
		Values_ST temp = current_scope.get(name);
		
		if(temp != null) {
			return temp.getType();
		}
		if(current_scope != class_scope) {
			temp = class_scope.get(name);
			if(temp != null) {
				return temp.getType();
			}
		}
		
		return "0";
	}
	
	// return index of indentifier
	public int index_of(String name) {
		Values_ST temp = current_scope.get(name);
		
		if(temp != null) {
			return temp.getIndex();
		}
		if(current_scope != class_scope) {
			temp = class_scope.get(name);
			if(temp != null) {
				return temp.getIndex();
			}
		}
		return -1;
	}
		
	
	
	
	
}
