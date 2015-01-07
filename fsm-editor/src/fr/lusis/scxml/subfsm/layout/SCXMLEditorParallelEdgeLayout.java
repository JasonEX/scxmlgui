package fr.lusis.scxml.subfsm.layout;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class SCXMLEditorParallelEdgeLayout extends mxParallelEdgeLayout {

	public SCXMLEditorParallelEdgeLayout(mxGraph graph) {
		super(graph);
	}

	public SCXMLEditorParallelEdgeLayout(mxGraph graph, int spacing) {
		super(graph, spacing);
	}
	
	public void execute(Object parent, int depth) {
		Map<String, List<Object>> lookup = findParallels(parent, depth);

		graph.getModel().beginUpdate();
		try {
			Iterator<List<Object>> it = lookup.values().iterator();

			while (it.hasNext()) {
				List<Object> parallels = it.next();

				if (parallels.size() > 1) {
					layout(parallels);
				}
			}
		} finally {
			graph.getModel().endUpdate();
		}
	}

	protected Map<String, List<Object>> findParallels(Object parent, int depth) {
		Map<String, List<Object>> lookup = new Hashtable<String, List<Object>>();
		findParallels(parent, lookup, depth);
		return lookup;
	}
	
	protected Map<String, List<Object>> findParallels(Object parent,
			Map<String, List<Object>> lookup, int depth) {
		mxIGraphModel model = graph.getModel();
		int childCount = model.getChildCount(parent);

		for (int i = 0; i < childCount; i++) {
			mxCell child = (mxCell) model.getChildAt(parent, i);

			if (!isEdgeIgnored(child)) {
				String id = getEdgeId(child);

				if (id != null) {
					if (!lookup.containsKey(id)) {
						lookup.put(id, new ArrayList<Object>());
					}

					lookup.get(id).add(child);
				}
			} else if (model.isVertex(child)) {
				if (depth != 0) {
					findParallels(child, lookup, depth - 1);
				}
			}
		}

		return lookup;
	}
}
