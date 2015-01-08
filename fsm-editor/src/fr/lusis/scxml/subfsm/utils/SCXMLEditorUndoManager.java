package fr.lusis.scxml.subfsm.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;

public class SCXMLEditorUndoManager extends mxUndoManager {

	private boolean enabled = true;
	private boolean collection = false;
	private boolean notUndoableEdits = false;
	long timestampOfLastEdit = new Date().getTime();
	int unmodifiedPosition;
	private List<SCXMLEditorUndoableEdit> collected = new ArrayList<SCXMLEditorUndoableEdit>();

	public SCXMLEditorUndoManager() {
		super();
	}

	public SCXMLEditorUndoManager(int size) {
		super(size);
		resetUnmodifiedState();
	}

	/**
	 * Maximum command history size. 0 means unlimited history. Default is 100.
	 */
	protected int size;

	/**
	 * List that contains the steps of the command history.
	 */
	protected List<List<SCXMLEditorUndoableEdit>> history;

	/**
	 * Index of the element to be added next.
	 */
	protected int indexOfNextAdd;

	/**
	 * 
	 */
	public boolean isEmpty() {
		return history.isEmpty();
	}

	/**
	 * Clears the command history.
	 */
	public void clear() {
		history = new ArrayList<List<SCXMLEditorUndoableEdit>>(size);
		indexOfNextAdd = 0;
		fireEvent(new mxEventObject(mxEvent.CLEAR));
	}

	/**
	 * Returns true if an undo is possible.
	 */
	public boolean canUndo() {
		return indexOfNextAdd > 0;
	}
	
	public Collection<Object> undoWithReturn() {
		HashSet<Object> modifiedObjects = null;
		boolean done = false;
		while ((indexOfNextAdd > 0) && !done) {
			List<SCXMLEditorUndoableEdit> edits = history.get(--indexOfNextAdd);
			for (int i = edits.size() - 1; i >= 0; i--) {
				SCXMLEditorUndoableEdit edit = edits.get(i);
				edit.undo();
				modifiedObjects = edit.getAffectedObjects();

				if (edit.isSignificant()) {
					fireEvent(new mxEventObject(mxEvent.UNDO, "edit", edit));
					done = true;
				}
			}
		}
		return modifiedObjects;
	}



	/**
	 * Returns true if a redo is possible.
	 */
	public boolean canRedo() {
		return indexOfNextAdd < history.size();
	}

	/**
	 * Redoes the last change.
	 */
	public Collection<Object> redoWithReturn() {
		HashSet<Object> modifiedObjects = new HashSet<Object>();
		int n = history.size();
		boolean done = false;

		while ((indexOfNextAdd < n) && !done) {
			List<SCXMLEditorUndoableEdit> edits = history.get(indexOfNextAdd++);
			for (SCXMLEditorUndoableEdit edit : edits) {
				edit.redo();
				for (mxUndoableChange c : edit.getChanges()) {
					if (c instanceof mxChildChange) {
						Object o = ((mxChildChange) c).getChild();
						if (o != null)
							modifiedObjects.add(o);
					}
				}

				if (edit.isSignificant()) {
					fireEvent(new mxEventObject(mxEvent.REDO, "edit", edit));
					done = true;
				}
			}
		}
		return modifiedObjects;
	}

	public void setEnabled(boolean e) {
		enabled = e;
	}

	public void setCollectionMode(boolean e) {
		collection = e;
		if (!collection && (collected.size() > 0)) {
			addEventList();
		}
	}

	/**
	 * Method to be called to add new undoable edits to the history.
	 */
	public void undoableEditHappened(SCXMLEditorUndoableEdit undoableEdit) {
		if (enabled) {
			if (undoableEdit.getTransparent()) {
			} else if (!undoableEdit.getUndoable()) {
				notUndoableEditHappened();
			} else if (collection) {
				collected.add(undoableEdit);
				fireEvent(new mxEventObject(mxEvent.ADD, "edit", undoableEdit));
			} else {
				collected.add(undoableEdit);
				addEventList();
				fireEvent(new mxEventObject(mxEvent.ADD, "edit", undoableEdit));
			}
		}
	}

	private void addEventList() {
		if (collected.size() > 0) {
			timestampOfLastEdit = new Date().getTime();
			trim();

			if (size > 0 && size == history.size()) {
				history.remove(0);
				unmodifiedPosition--;
			}

			history.add(collected);
			indexOfNextAdd = history.size();
			collected = new ArrayList<SCXMLEditorUndoableEdit>();
		}
	}

	public void notUndoableEditHappened() {
		notUndoableEdits = true;
	}

	/**
	 * Removes all pending steps after indexOfNextAdd from the history, invoking
	 * die on each edit. This is called from undoableEditHappened.
	 */
	protected void trim() {
		while (history.size() > indexOfNextAdd) {
			List<SCXMLEditorUndoableEdit> edits = (List<SCXMLEditorUndoableEdit>) history
					.remove(indexOfNextAdd);
			for (SCXMLEditorUndoableEdit edit : edits)
				edit.die();
		}
	}

	public long getTimeOfMostRecentUndoEvent() {
		return timestampOfLastEdit;
	}

	public void resetUnmodifiedState() {
		// System.out.println("reset= "+indexOfNextAdd+" "+unmodifiedPosition);
		unmodifiedPosition = indexOfNextAdd;
		notUndoableEdits = false;
	}

	public boolean isUnmodifiedState() {
		// System.out.println("check= "+indexOfNextAdd+" "+unmodifiedPosition);
		return (!notUndoableEdits && (indexOfNextAdd == unmodifiedPosition));
	}
	
	@Override
	public void undoableEditHappened(mxUndoableEdit undoableEdit)
	{
		if (undoableEdit instanceof SCXMLEditorUndoableEdit)
			undoableEditHappened((SCXMLEditorUndoableEdit) undoableEdit);
		else
			System.out.println("Error call from SCXMLEditorUndoableManager.undoableEditHappened(mxUndoableEdit)");
	}
	
}
