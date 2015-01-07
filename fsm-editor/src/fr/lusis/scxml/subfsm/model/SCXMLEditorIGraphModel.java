package fr.lusis.scxml.subfsm.model;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;

public interface SCXMLEditorIGraphModel extends mxIGraphModel {

	void endUpdate(boolean validate);
	
	void addChangeToCurrentEdit(mxUndoableChange change) throws Exception;
	
	public boolean isLoop(mxIGraphModel model, Object edge);
	
	public Object getNearestCommonAncestor(Object cell1, Object cell2);
	
	public void clearCells();
}
