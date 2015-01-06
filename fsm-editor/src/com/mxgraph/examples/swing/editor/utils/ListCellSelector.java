package com.mxgraph.examples.swing.editor.utils;

//Patch for jgraphx migration
//Yuqian YANG @ LUSIS
//01/06/2015

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel.mxStyleChange;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import fr.lusis.scxml.subfsm.swing.handler.SCXMLCellMarker;

@SuppressWarnings("rawtypes")
public class ListCellSelector {
	private JList list;
	protected DefaultListModel listModel;
	protected HashMap<mxCell, SCXMLCellMarker> currentSelectedCells = new HashMap<mxCell, SCXMLCellMarker>();
	protected mxGraphComponent gc;
	private mxGraph graph;
	private mxGraphView view;
	private mxIGraphModel model;
	private boolean withScroll;
	private boolean selectSetAsValid = true;

	public ListCellSelector(final mxGraphComponent gc, final boolean withScroll) {
		this.gc = gc;
		this.graph = gc.getGraph();
		this.view = graph.getView();
		this.model = graph.getModel();
		this.withScroll = withScroll;
		mxIEventListener updateListener = new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				// System.out.println("Updating marker because of event: "+evt.getName()+" change: "+evt.getProperties());

				Object changes = evt.getProperty("changes");
				if (changes != null && changes instanceof List) {
					for (Object change : ((List) changes)) {
						if (change != null && change instanceof mxStyleChange) {
							Object cell = ((mxStyleChange) change).getCell();
							mxCellState state = view.getState(cell, false);
							if (currentSelectedCells.containsKey(cell)) {
								SCXMLCellMarker selector = currentSelectedCells
										.get(cell);
								selector.unmark();
								selector.process(state, selector
										.getMarkerColor(null, state,
												selectSetAsValid),
										selectSetAsValid);

								selector.mark();
							}
						}
					}
				}
				for (Entry<mxCell, SCXMLCellMarker> el : currentSelectedCells
						.entrySet()) {
					el.getValue().unmark();
					el.getValue().mark();
				}
			}
		};

		view.addListener(mxEvent.SCALE_AND_TRANSLATE, updateListener);
		view.addListener(mxEvent.SCALE, updateListener);
		view.addListener(mxEvent.TRANSLATE, updateListener);
		model.addListener(mxEvent.CHANGE, updateListener);
	}

	public ListCellSelector(mxGraphComponent gc) {
		this(gc, true);
	}

	public ListCellSelector(JList list, SCXMLGraphComponent gc) {
		this(gc);
		this.list = list;
		listModel = (DefaultListModel) list.getModel();
	}

	public mxCell getCellFromListElement(int selectedIndex) {
		return (mxCell) listModel.get(selectedIndex);
	}

	public void handleSelectEvent(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			int selectedIndex = list.getSelectedIndex();

			if ((selectedIndex >= 0) && (selectedIndex < listModel.size())) {
				mxCell c = getCellFromListElement(selectedIndex);
				unselectAll();
				selectCell(c);
			} else {
				unselectAll();
			}
		}
	}

	public void selectCell(mxCell c) {
		if (c != null) {
			SCXMLCellMarker selector = currentSelectedCells.get(c);
			mxCellState state = view.getState(c);
			if (selector == null) {
				selector = new SCXMLCellMarker(gc);
				currentSelectedCells.put(c, selector);
				selector.process(state,
						selector.getMarkerColor(null, state, selectSetAsValid),
						selectSetAsValid);
				selector.mark();
				if (withScroll)
					gc.scrollCellToVisible(c, true);
			} else {
				selector.process(state,
						selector.getMarkerColor(null, state, selectSetAsValid),
						selectSetAsValid);
				selector.mark();
			}
		}
	}

	public void unselectCell(mxCell c) {
		mxCellMarker thisCellSelector = currentSelectedCells.get(c);
		if (thisCellSelector != null) {
			thisCellSelector.unmark();
			currentSelectedCells.remove(c);
		}
	}

	public void toggleSelection(mxCell c) {
		if ((c != null) && (!currentSelectedCells.containsKey(c)))
			selectCell(c);
		else
			unselectCell(c);
	}

	public void unselectAll() {
		for (Entry<mxCell, SCXMLCellMarker> el : currentSelectedCells
				.entrySet()) {
			el.getValue().unmark();
		}
		currentSelectedCells.clear();
	}
}
