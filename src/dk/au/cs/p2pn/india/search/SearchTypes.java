package dk.au.cs.p2pn.india.search;


public enum SearchTypes {
	FLOOD_SEARCH(0),
	K_WALKER_SEARCH(1),
	AK_WALKER_SEARCH(2);

	private int value;
	private SearchTypes(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	static SearchTypes getType( int intVal ) throws Exception
	{
		for (SearchTypes type : SearchTypes.values())
			if (type.getValue() == intVal)
				return type;

		return null;
	}
}
