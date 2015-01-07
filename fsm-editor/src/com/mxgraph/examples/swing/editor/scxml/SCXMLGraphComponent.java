// Patch for jgraphx migration
// Yuqian YANG @ LUSIS
// 01/06/2015

package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.w3c.dom.Document;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.handler.SCXMLConnectionHandler;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.validation.Validator;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import fr.lusis.scxml.subfsm.utils.SCXMLEditorEvent;

/**
 * 
 */
//implementsComponentListener
public class SCXMLGraphComponent extends mxGraphComponent 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6833603133512882012L;
	
	protected SCXMLEditorGraphControl extendGraphControl; 

	private Validator validator = null;

	private Semaphore zoomInProgress = new Semaphore(1);
	
	private boolean repaintEnabled = true;

	/**
	 * 
	 * @param graph
	 */
	public SCXMLGraphComponent(mxGraph graph) {
		super(graph);

		setWheelScrollingEnabled(false);

		// addComponentListener(this);
		setDragEnabled(true);

		// Sets switches typically used in an editor
		setPageVisible(false);
		setGridVisible(true);
		setToolTips(true);
		getConnectionHandler().setCreateTarget(true);

		// Loads the defalt stylesheet from an external file
		mxCodec codec = new mxCodec();
		Document doc = mxUtils.loadDocument(SCXMLGraphEditor.class.getResource(
				"/com/mxgraph/examples/swing/resources/default-style.xml")
				.toString());
		codec.decode(doc.getDocumentElement(), graph.getStylesheet());

		// Sets the background to white
		getViewport().setOpaque(false);
		setBackground(Color.WHITE);
	}

	@Override
	// disable double click editing event
	public boolean isEditEvent(MouseEvent e) {
		return false;
	}

	@Override
	public SCXMLGraph getGraph() {
		return (SCXMLGraph) graph;
	}

	@Override
	protected mxConnectionHandler createConnectionHandler() {
		return new SCXMLConnectionHandler(this);
	}

	/**
	 * Overrides drop behaviour to set the cell style if the target is not a
	 * valid drop target and the cells are of the same type (eg. both vertices
	 * or both edges).
	 */
	public Object[] importCells(Object[] cells, double dx, double dy,
			Object target, Point location) {
		/*
		 * if (target == null && cells.length == 1 && location != null) { target
		 * = getCellAt(location.x, location.y);
		 * 
		 * if (target instanceof mxICell && cells[0] instanceof mxICell) {
		 * mxICell targetCell = (mxICell) target; mxICell dropCell = (mxICell)
		 * cells[0];
		 * 
		 * if (targetCell.isVertex() == dropCell.isVertex() ||
		 * targetCell.isEdge() == dropCell.isEdge()) { mxIGraphModel model =
		 * graph.getModel(); model.setStyle(target, model.getStyle(cells[0]));
		 * graph.setSelectionCell(target);
		 * 
		 * return null; } } }
		 */
		mxIGraphModel model = getGraph().getModel();
		for (Object cell : cells) {
			Object value = model.getValue(cell);
			if (value instanceof SCXMLNode) {
				model.setStyle(cell, ((SCXMLNode) value).getStyle());
			} else if (value instanceof SCXMLEdge) {
				model.setStyle(cell,
						((SCXMLEdge) value).getStyle((mxCell) cell));
			}
		}
		return super.importCells(cells, dx, dy, target, location);
	}

	private HashMap<String, mxCell> scxmlNodes = new HashMap<String, mxCell>();

	public void addSCXMLNode(SCXMLNode n, mxCell node) {
		scxmlNodes.put(n.getID(), node);
	}

	public boolean isSCXMLNodeAlreadyThere(SCXMLNode n) {
		return scxmlNodes.containsKey(n.getID());
	}

	public mxCell getSCXMLNodeForID(String id) {
		return scxmlNodes.get(id);
	}

	public void clearSCXMLNodes() {
		scxmlNodes.clear();
	}

	public String validateGraph() {
		mxGraphModel model = (mxGraphModel) graph.getModel();
		model.fireEvent(new mxEventObject(SCXMLEditorEvent.REQUEST_VALIDATION, "root",
				model.getRoot()));
		
		return super.validateGraph();
	}
	
	public void setValidator(Validator v) {
		validator = v;
	}

	public Validator getValidator() {
		return validator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mxgraph.swing.mxGraphComponent#getSiblingsOfCell(java.lang.Object)
	 */
	public Collection<Object> getSiblingsOfCell(Object c) {
		ArrayList<Object> ret = new ArrayList<Object>();
		ret.add(c);
		mxCell cell = (mxCell) c;
		SCXMLEdge value = null;
		ArrayList<String> targets = null;
		if (cell.isEdge() && ((value = (SCXMLEdge) cell.getValue()) != null)
				&& ((targets = value.getSCXMLTargets()) != null)
				&& (targets.size() > 1)) {
			mxICell source = cell.getSource();
			if (source != null) {
				int numEdges = source.getEdgeCount();
				for (int i = 0; i < numEdges; i++) {
					mxICell child = source.getEdgeAt(i);
					if ((child != c) && (child.getValue() == value)) {
						ret.add(child);
					}
				}
			}
		}
		return ret;
	}

	@Override
	protected TransferHandler createTransferHandler() {
		return new SCXMLTransferHandler();
	}

	@Override
	protected mxGraphHandler createGraphHandler() {
		return new SCXMLGraphHandler(this);
	}

	public Point unscaledGraphCoordinates(Point p) {
		mxGraphView view = graph.getView();
		double scale = view.getScale();
		return new Point((int)Math.round(p.x/scale), (int)Math.round(p.y/scale));
	}
	
	public Point mouseCoordToGraphCoord(Point mouse) {
		mxGraphView view = graph.getView();
		double scale = view.getScale();
		mxPoint trans = view.getTranslate();
		int dx = getHorizontalScrollBar().getValue();
		int dy = getVerticalScrollBar().getValue();
		return new Point((int) Math.round(((dx + mouse.x) / scale)
				- trans.getX()), (int) Math.round(((dy + mouse.y) / scale)
				- trans.getY()));
	}

	public Point mouseCoordToGraphMouseCoord(Point mouse) {
		int dx = getHorizontalScrollBar().getValue();
		int dy = getVerticalScrollBar().getValue();
		return new Point(dx + mouse.x, dy + mouse.y);
	}
	
//	public void zoomIn(Point centerPoint) {
//		zoom(zoomFactor, centerPoint);
//	}
//
//	public void zoomOut(Point centerPoint) {
//		zoomTO(1 / zoomFactor, centerPoint);
//	}
	
	protected int computeDeltaForZoomCenterScroolbar(int v, double factor,
			int centerPos, double extentRatio) {
		return (int) Math.round((centerPos - v) * (factor - extentRatio));
	}

	protected void maintainScrollBar(boolean horizontal, final int oldValue,
			double factor, boolean center) {
		maintainScrollBar(horizontal, oldValue, factor, center, null);
	}

	protected void maintainScrollBar(boolean horizontal, final int oldValue,
			final double factor, boolean center, Point centerPoint) {
		JScrollBar scrollBar = (horizontal) ? getHorizontalScrollBar()
				: getVerticalScrollBar();
		final int pos;

		if (scrollBar != null) {
			final BoundedRangeModel model = scrollBar.getModel();
			final int ex = model.getExtent();
			if (centerPoint != null) {
				pos = (int) Math.round((horizontal) ? centerPoint.getX()
						: centerPoint.getY());
			} else {
				if (center) {
					pos = oldValue + Math.round((model.getExtent() / 2));
				} else {
					pos = oldValue;
				}
			}

			// System.out.println("v="+v+" ex="+ex+" pos="+pos+" f="+factor+" nex="+model.getExtent());
			final int newValue = (int) Math.round((double) oldValue * factor)
					+ computeDeltaForZoomCenterScroolbar(oldValue, factor, pos,
							(double) model.getExtent() / (double) ex);
			// System.out.println("nv="+newValue+" delta="+computeDeltaForZoomCenterScroolbar(v,factor,pos,(double)model.getExtent()/(double)ex));
			model.setValue(newValue);

			// model.setRangeProperties(newValue,model.getExtent(),0,(int)Math.round(model.getMaximum()*factor),true);
		}
	}

	public void zoom(final double factor, final Point centerPoint) {
		if (zoomInProgress.tryAcquire()) {
			mxGraphView view = graph.getView();
			double newScale;
			if (factor > 1)
				newScale = (double) (Math.ceil(view.getScale() * 100 * factor)) / 100;
			else
				newScale = (double) (Math.floor(view.getScale() * 100 * factor)) / 100;
			if (newScale != view.getScale() && newScale > 0.01) {
				mxPoint translate = (pageVisible && centerPage) ? getPageTranslate(newScale)
						: new mxPoint(0, 0);

				final int oldHorizontalValue = getHorizontalScrollBar()
						.getModel().getValue();
				final int oldVerticalValue = getVerticalScrollBar().getModel()
						.getValue();

				repaintEnabled = false;
				graph.getView().scaleAndTranslate(newScale, translate.getX(),
						translate.getY());

				if (keepSelectionVisibleOnZoom && !graph.isSelectionEmpty()) {
					getGraphControl().scrollRectToVisible(
							view.getBoundingBox(graph.getSelectionCells())
									.getRectangle());
					zoomInProgress.release();
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							maintainScrollBar(true, oldHorizontalValue, factor,
									centerZoom, centerPoint);
							maintainScrollBar(false, oldVerticalValue, factor,
									centerZoom, centerPoint);
							repaintEnabled = true;
							graphControl.repaint();
							zoomInProgress.release();
						}
					});
				}
			} else {
				zoomInProgress.release();
			}
		}
	}
	
	public void zoomIn(Point centerPoint) {
		zoom(zoomFactor, centerPoint);
	}

	public void zoomOut(Point centerPoint) {
		zoom(1 / zoomFactor, centerPoint);
	}
	
	/**
	 * Creates the inner control that handles tooltips, preferred size and can
	 * draw cells onto a canvas.
	 */
	protected SCXMLEditorGraphControl createGraphControl()
	{
		return new SCXMLEditorGraphControl();
	}

	/**
	 * 
	 * @return Returns the control that renders the graph.
	 */
	public SCXMLEditorGraphControl getGraphControl()
	{
		return extendGraphControl;
	}

	public class SCXMLEditorGraphControl extends mxGraphControl {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6264909868916126272L;

		public SCXMLEditorGraphControl() {
			super();
		}
		
		@Override
		public void paintComponent(Graphics g) {
			if (repaintEnabled) {
				super.paintComponent(g);
			}
		}
	}
}
