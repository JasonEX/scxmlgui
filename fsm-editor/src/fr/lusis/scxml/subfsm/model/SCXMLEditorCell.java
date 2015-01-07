package fr.lusis.scxml.subfsm.model;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class SCXMLEditorCell extends mxCell {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7631135504158748799L;

	public SCXMLEditorCell() {
		super();
	}

	public SCXMLEditorCell(Object value) {
		super(value);
	}

	public SCXMLEditorCell(Object value, mxGeometry geometry, String style) {
		super(value, geometry, style);
	}

	public boolean hasAVertexAsChild() {
		int l = getChildCount();
		for (int i = 0; i < l; i++) {
			mxCell c = (mxCell) getChildAt(i);
			if (c.isVertex())
				return true;
		}
		return false;
	}
}
