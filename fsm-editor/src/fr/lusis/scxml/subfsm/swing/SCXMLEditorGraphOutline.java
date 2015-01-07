package fr.lusis.scxml.subfsm.swing;

import java.awt.Dimension;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;

@SuppressWarnings("unused")
public class SCXMLEditorGraphOutline extends mxGraphOutline {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3698391956910373959L;
	private int height;
	private int width;
	
	public SCXMLEditorGraphOutline(mxGraphComponent graphComponent) {
		super(graphComponent);
	}
	
	/**
	 * 
	 */
	public SCXMLEditorGraphOutline(mxGraphComponent graphComponent, int h, int w) {
		super(graphComponent);
		height = h;
		width = w;
		addComponentListener(componentHandler);
		addMouseMotionListener(tracker);
		addMouseListener(tracker);
		setGraphComponent(graphComponent);
		setEnabled(true);
		setOpaque(true);
		setMaximumSize(new Dimension(2 * w, h));
		setPreferredSize(new Dimension(w, h));
		setMinimumSize(new Dimension(w / 2, h));
	}
	
	public void updateOutline() {
		if (updateScaleAndTranslate()) {
			repaintBuffer = true;
			updateFinder(false);
			repaint();
		} else {
			updateFinder(true);
		}
	}
}
