package pk.edu.kics.utill;

import java.io.Serializable;

public class SemanticType implements Serializable
{

	private static final long serialVersionUID = 2048164256415117528L;
	private String name;
	private String abb;
	String id;
	
	public SemanticType()
	{
		id=null;
		name=null;
		abb=null;
	}
	public SemanticType(String name, String abb) {
		super();
		this.name = name;
		this.abb = abb;
	}
	public SemanticType( String id, String name, String abb) {
		super();
		this.id=id;
		this.name = name;
		this.abb = abb;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAbb() {
		return abb;
	}
	public void setAbb(String abb) {
		this.abb = abb;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
	}
