package fr.lusis.scxml.subfsm.utils;

import java.util.HashSet;

import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.util.mxUndoableEdit;

public class SCXMLEditorUndoableEdit extends mxUndoableEdit {
	
	private boolean transparent;
	private boolean undoable = true;

	public interface SCXMLEditorUndoableChange extends mxUndoableChange
	{
		String getInfoString();
	}
	
	public SCXMLEditorUndoableEdit(Object source) {
		super(source);
	}

	public SCXMLEditorUndoableEdit(Object source, boolean significant) {
		super(source, significant);
	}

	public void setTransparent(boolean t) {
		transparent = t;
	}

	public boolean getTransparent() {
		return transparent;
	}

	public void setUndoable(boolean u) {
		undoable = u;
	}

	public boolean getUndoable() {
		return undoable;
	}

	public HashSet<Object> getAffectedObjects() {
		HashSet<Object> modifiedObjects = new HashSet<Object>();
		for (mxUndoableChange c : getChanges()) {
			if (c instanceof mxChildChange) {
				Object o = ((mxChildChange) c).getChild();
				if (o != null)
					modifiedObjects.add(o);
			}
		}
		return modifiedObjects;
	}

}
