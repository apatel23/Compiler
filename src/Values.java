
public class Values {
	
	private int index;
	private String type, kind;

	// create behaviors for the Symbol Table entry
	public Values(String t, String k, int i) {
		type = t;
		kind = k;
		index = i;
	}
	
	public String getType() {
		return type;
	}
	
	public String getKind() {
		return kind;
	}
	
	public int getIndex() {
		return index;
	}
}