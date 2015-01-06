package fr.lusis.scxml.subfsm.view;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraphView;

public class SCXMLCellEditorState extends mxCellState {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7524863891777140315L;

	public SCXMLCellEditorState(mxGraphView view, Object cell,
			Map<String, Object> style) {
		super(view, cell, style);
	}
	
	public int getIndexOfEdgePointAt(int x, int y, int tol) {
		Rectangle rect = new Rectangle(x - tol / 2, y - tol / 2, tol, tol);
		List<mxPoint> pts = getAbsolutePoints();

		int i = 0;
		for (mxPoint p : pts) {
			// System.out.println("point="+p.getX()+" "+p.getY());
			if (rect.contains(p.getPoint()))
				return i;
			i++;
		}
		return -1;
	}

	public int getIndexOfNewPoint(int x, int y, int tol) {
		Rectangle rect = new Rectangle(x - tol / 2, y - tol / 2, tol, tol);
		List<mxPoint> pts = getAbsolutePoints();

		int length = pts.size();
		mxPoint start, end;
		start = pts.get(0);
		for (int i = 1; i < length; i++) {
			end = pts.get(i);
			if (rect.intersectsLine(start.getX(), start.getY(), end.getX(),
					end.getY()))
				return i;
			start = end;
		}
		return -1;
	}

}
