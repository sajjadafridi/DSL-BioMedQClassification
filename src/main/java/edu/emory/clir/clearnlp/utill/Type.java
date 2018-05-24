package edu.emory.clir.clearnlp.utill;

import java.io.Serializable;

public class Type implements Serializable
{
	private String name;
	private String abb;
	String id;
	
	public Type()
	{
		id=null;
		name=null;
		abb=null;
	}
	public Type(String name, String abb) {
		super();
		this.name = name;
		this.abb = abb;
	}
	public Type( String id, String name, String abb) {
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
