package io.github.openlg.graphlib;

import java.io.Serializable;

/**
 * @author lg
 * Create by lg on 4/24/20 10:41 PM
 */
public class Edge implements Serializable {
	private String source;
	private String target;
	private String name;

	public Edge(String source, String target) {
		this(source, target, null);
	}
	public Edge(String source, String target, String name) {
		this.source = source;
		this.target = target;
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(" [");
		sb.append(" name = ").append(getName()).append(",");
		sb.append(" source = ").append(getSource()).append(",");
		sb.append(" target = ").append(getTarget());
		sb.append(" ]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof Edge){
			Edge _edge = (Edge) obj;
			return _edge.toString().equals(toString());
		} else {
			return false;
		}
	}

}
