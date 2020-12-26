package com.panderson.jpatool;

/**
 *
 * @author paul.anderson
 */
public class NodeObject {
	private NodeObject(){}
	private String name;
	private Object value;

	public NodeObject(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public Object getValue(){
		return value;
	}
	
	@Override
	public String toString() {
		return String.format("%s : %s", name, value == null ? "null" : value.toString());
	}

	
}
