package fr.lusis.scxml.subfsm.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraphView;

public class SCXMLEditorCellState extends mxCellState {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4685379739512603052L;

	public SCXMLEditorCellState() {
		super();
	}

	public SCXMLEditorCellState(mxGraphView view, Object cell,
			Map<String, Object> style) {
		super(view, cell, style);
	}
	
	public Point relativizePointToThisState(Point p, double s, mxPoint tr) {
		// p is already normalised to the scale
		return new Point((int) Math.round((p.x - getX()) / s),
				(int) Math.round((p.y - getY()) / s));
	}

	public mxRectangle relativizeRectangleToThisState(mxRectangle r, double s,
			mxPoint tr) {
		// r is not normalised to the scale
		return new mxRectangle((r.getX() - getX()) / s,
				(r.getY() - getY()) / s, r.getWidth() / s, r.getHeight() / s);
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
