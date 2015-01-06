package fr.lusis.scxml.subfsm.swing.handler;

import java.awt.Color;
import java.awt.event.MouseEvent;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.view.mxCellState;

public class SCXMLCellMarker extends mxCellMarker {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5748827631837363843L;

	public SCXMLCellMarker(mxGraphComponent graphComponent) {
		super(graphComponent);
	}

	/**
	 * Processes the given event and marks the state returned by getStateAt with
	 * the color returned by getMarkerColor. If the markerColor is not null,
	 * then the state is stored in markedState. If isValidState returns true,
	 * then the state is stored in validState regardless of the marker color.
	 * The state is returned regardless of the marker color and valid state.
	 * 
	 * @param isValid
	 */
	public void process(mxCellState state, Color color, boolean isValid) {
		if (isValid) {
			validState = state;
		} else {
			validState = null;
		}

		if (state != markedState || color != currentColor) {
			currentColor = color;

			if (state != null && currentColor != null) {
				markedState = state;
				mark();
			} else if (markedState != null) {
				markedState = null;
				unmark();
			}
		}
	}

	/**
	 * Returns the valid- or invalidColor depending on the value of isValid. The
	 * given state is ignored by this implementation.
	 */
	public Color getMarkerColor(MouseEvent e, mxCellState state, boolean isValid) {
		return getMarkerColor(e, state, isValid);
	}
}
