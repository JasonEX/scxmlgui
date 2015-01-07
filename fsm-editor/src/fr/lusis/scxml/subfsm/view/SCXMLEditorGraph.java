package fr.lusis.scxml.subfsm.view;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import fr.lusis.scxml.subfsm.model.SCXMLEditorIGraphModel;

public class SCXMLEditorGraph extends mxGraph {
	
	protected SCXMLEditorIGraphModel extendModel;

	public SCXMLEditorGraph() {
		super();
	}

	public SCXMLEditorGraph(mxIGraphModel model) {
		super(model);
	}

	public SCXMLEditorGraph(mxStylesheet stylesheet) {
		super(stylesheet);
	}

	public SCXMLEditorGraph(mxIGraphModel model, mxStylesheet stylesheet) {
		super(model, stylesheet);
	}
	
	public SCXMLEditorIGraphModel getExtendModel() {
		return extendModel;
	}
	

	@Override
	public mxIGraphModel getModel() {
		return getExtendModel();
	}
	
	@Override
	public void setModel(mxIGraphModel value)
	{
		throw new UnsupportedOperationException();
	}
	
	public void setModel(SCXMLEditorIGraphModel value)
	{
		setExtendModel(value);
	}
	
	public void setExtendModel(SCXMLEditorIGraphModel value)
	{
		if (extendModel != null)
		{
			extendModel.removeListener(graphModelChangeHandler);
		}

		Object oldModel = extendModel;
		extendModel = value;

		if (view != null)
		{
			view.revalidate();
		}

		extendModel.addListener(mxEvent.CHANGE, graphModelChangeHandler);
		changeSupport.firePropertyChange("model", oldModel, extendModel);
		repaint();
	}
}
