package fr.lusis.scxml.subfsm.model;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;

import fr.lusis.scxml.subfsm.utils.SCXMLEditorUndoableEdit;

public class SCXMLEditorGraphModel extends mxGraphModel implements SCXMLEditorIGraphModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1996089476093649758L;

	public SCXMLEditorGraphModel() {
		super();
	}

	public SCXMLEditorGraphModel(Object root) {
		super(root);
	}
	
	@Override
	public void endUpdate() {
		endUpdate(true);
	}

	@Override
	public void endUpdate(boolean validate) {
		updateLevel--;

		if (!endingUpdate) {
			endingUpdate = updateLevel == 0;
			fireEvent(new mxEventObject(mxEvent.END_UPDATE, "edit", currentEdit));

			try {
				if (endingUpdate && !currentEdit.isEmpty()) {
					fireEvent(new mxEventObject(mxEvent.BEFORE_UNDO, "edit",
							currentEdit));
					mxUndoableEdit tmp = currentEdit;
					currentEdit = createUndoableEdit();
					tmp.dispatch();
					fireEvent(new mxEventObject(mxEvent.UNDO, "edit", tmp));
				}
			} finally {
				endingUpdate = false;
			}
		}
	}

	@Override
	public void addChangeToCurrentEdit(mxUndoableChange change)
			throws Exception {
		if (getUpdateLevel() > 0) {
			currentEdit.add(change);
		} else {
			throw new Exception(
					"Error: attempting to add a change outside a model update session.");
		}
	}

	@Override
	public boolean isLoop(mxIGraphModel model, Object edge) {
		if (model.isEdge(edge)) {
			Object source = model.getTerminal(edge, true);
			Object target = model.getTerminal(edge, false);
			return source == target;
		} else
			return false;
	}

	@Override
	public void clearCells() {
		cells.clear();
	}
	
	@Override
	protected mxUndoableEdit createUndoableEdit()
	{
		return new SCXMLEditorUndoableEdit(this)
		{
			public void dispatch()
			{
				((mxGraphModel) source).fireEvent(new mxEventObject(
						mxEvent.CHANGE, "edit", this, "changes", changes));
			}
		};
	}
	
	public void executeCovert(mxAtomicGraphModelChange change)
	{
		assert(currentEdit.isEmpty());
		((SCXMLEditorUndoableEdit)currentEdit).setTransparent(true);
		change.execute();
		beginUpdate();
		currentEdit.add(change);
		fireEvent(new mxEventObject(mxEvent.EXECUTE, "change", change));
		endUpdate(false);
	}

}
