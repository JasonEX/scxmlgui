package fr.lusis.scxml.subfsm.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import fr.lusis.scxml.subfsm.model.SCXMLEditorCell;
import fr.lusis.scxml.subfsm.model.SCXMLEditorGraphModel;
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
	
	public Object[] getEdgesForSwimlane(Object cell, Object parent,
			boolean incoming, boolean outgoing, boolean includeLoops,
			Set<Object> descendants) {
		// this function can get all edges coming and going from a node. If the
		// node is a swimlane (cluster)
		// it gets all edges coming and/or going to itself or to any of its'
		// included nodes (when the other
		// side of the edge is not among the included nodes)
		// ------------------------------
		// get loops on cell if includeLoops is true
		// get all incoming edges to cell if incoming is true
		// get all outgoing edges from cell if outgoing is true
		// if incoming || outgoing is true
		// find all descendants of cell
		// for each of them get the incoming edges (if incoming is true)
		// for each edge, if it comes NOT from a descendant, include it
		// get all outgoing edges (if outgoing is true)
		// for each edge, if it goes NOT to a descendant, include it
		HashSet<Object> result = new HashSet<Object>();

		for (Object edge : SCXMLEditorGraphModel.getEdges(getExtendModel(), cell, incoming,
				outgoing, includeLoops))
			if (doesEdgeSatisfyFilter(edge, cell, parent, incoming, outgoing,
					includeLoops)) {
				result.add(edge);
			}

		if (incoming || outgoing) {
			getAllDescendants(cell, descendants);

			for (Object d : descendants) {
				for (Object edge : SCXMLEditorGraphModel.getEdges(getExtendModel(), d, incoming,
						outgoing, false)) {
					Object source = view.getVisibleTerminal(edge, true);
					Object target = view.getVisibleTerminal(edge, false);
					if (incoming
							&& (target == d)
							&& !descendants.contains(source)
							&& ((parent == null) || hasThisAsAntecedent(source,
									parent)))
						result.add(edge);
					else if (outgoing
							&& (source == d)
							&& !descendants.contains(target)
							&& ((parent == null) || hasThisAsAntecedent(target,
									parent)))
						result.add(edge);
				}
			}
		}
		// System.out.println("edges found: "+result);
		return result.toArray();
	}

	public boolean doesEdgeSatisfyFilter(Object edge, Object cell,
			Object parent, boolean incoming, boolean outgoing,
			boolean includeLoops) {
		Object source = view.getVisibleTerminal(edge, true);
		Object target = view.getVisibleTerminal(edge, false);

		return (((source == target) && includeLoops) || ((source != target) && ((incoming
				&& (target == cell) && ((parent == null) || hasThisAsAntecedent(
				source, parent))) || (outgoing && (source == cell) && ((parent == null) || hasThisAsAntecedent(
				target, parent))))));
	}

	public void getAllDescendants(Object cell, Set<Object> set) {
		if (!set.contains(cell)) {
			set.add(cell);
			int nc = getExtendModel().getChildCount(cell);
			for (int i = 0; i < nc; i++) {
				Object child = getExtendModel().getChildAt(cell, i);
				getAllDescendants(child, set);
			}
		}
	}

	public boolean hasThisAsAntecedent(Object des, Object ant) {
		Object parent = des;
		if (ant == des)
			return true;
		while ((parent = getExtendModel().getParent(parent)) != null) {
			if (parent == ant)
				return true;
		}
		return false;
	}

	public Object[] getTerminalsOutsideSet(Object[] edges, Set<Object> set) {
		Collection<Object> terminals = new LinkedHashSet<Object>();

		if (edges != null) {
			for (int i = 0; i < edges.length; i++) {
				Object term = getTerminalOutsideSet(edges[i], set);
				if (term != null)
					terminals.add(term);
			}
		}
		return terminals.toArray();
	}

	public Object getTerminalOutsideSet(Object edge, Set<Object> set) {
		Object source = view.getVisibleTerminal(edge, true);
		Object target = view.getVisibleTerminal(edge, false);

		boolean sourceInSet = (source != null) && (set.contains(source));
		boolean targetInSet = (target != null) && (set.contains(target));

		if (!sourceInSet && (source != null) && (target != null) && targetInSet)
			return source;
		else if (!targetInSet && (source != null) && (target != null)
				&& sourceInSet)
			return target;
		else
			return null;
	}
	
	public class RootStrength {
		private boolean isRoot;
		private int strength;

		public RootStrength(boolean isRoot, int strength) {
			this.isRoot = isRoot;
			this.strength = strength;
		}

		public boolean isRoot() {
			return isRoot;
		}

		public int getStrength() {
			return strength;
		}
	}

	public RootStrength vertexShouldBeRoot(Object cell, Object parent,
			boolean invert) {
		HashSet<Object> descendants = new HashSet<Object>();
		Object[] conns = getEdgesForSwimlane(cell, parent, true, true, false,
				descendants);
		int fanOut = 0;
		int fanIn = 0;

		for (int j = 0; j < conns.length; j++) {
			Object src = getTerminalOutsideSet(conns[j], descendants);

			if (((SCXMLEditorCell) conns[j]).getSource() == src) {
				fanIn++;
			} else {
				fanOut++;
			}
		}
		int diff = (invert) ? fanIn - fanOut : fanOut - fanIn;

		return new RootStrength((invert && fanOut == 0)
				|| (!invert && fanIn == 0), diff);
	}

	public List<Object> findTreeRootsInSet(Set<Object> objects, Object parent,
			boolean invert) {
		List<Object> roots = new ArrayList<Object>();

		Object best = null;
		int maxDiff = Integer.MIN_VALUE;

		for (Object cell : objects) {
			if (getExtendModel().isVertex(cell) && isCellVisible(cell)) {

				RootStrength cellRoot = vertexShouldBeRoot(cell, parent, invert);
				if (cellRoot.isRoot)
					roots.add(cell);
				int strength = cellRoot.getStrength();

				if (strength > maxDiff) {
					maxDiff = strength;
					best = cell;
				}
			}
		}

		if (roots.isEmpty() && best != null) {
			roots.add(best);
		}

		return roots;
	}

	public List<Set<Object>> connectedComponents(Object parent, boolean isolate) {
		HashMap<Object, Integer> components = new HashMap<Object, Integer>();

		HashMap<Object, HashSet<Object>> descendants4vertex = new HashMap<Object, HashSet<Object>>();
		HashMap<Object, Object[]> connections4vertex = new HashMap<Object, Object[]>();
		HashMap<Object, Object> descendant2ancestor = new HashMap<Object, Object>();
		int childCount = getExtendModel().getChildCount(parent);
		for (int i = 0; i < childCount; i++) {
			Object childCell = getExtendModel().getChildAt(parent, i);
			if (getExtendModel().isVertex(childCell)) {
				HashSet<Object> descendants = new HashSet<Object>();
				Object[] conns = getEdgesForSwimlane(childCell, parent, true,
						true, false, descendants);
				assert (descendants.contains(childCell));
				descendants4vertex.put(childCell, descendants);
				connections4vertex.put(childCell, conns);
				for (Object d : descendants) {
					assert (!descendant2ancestor.containsKey(d));
					descendant2ancestor.put(d, childCell);
				}
			}
		}

		int currentComponent = 0;
		Stack<Object> cells = new Stack<Object>();
		for (int i = 0; i < childCount; i++) {
			Object childCell = getExtendModel().getChildAt(parent, i);
			cells.push(childCell);
			boolean added = false;
			while (!cells.isEmpty()) {
				Object cell = cells.pop();
				if (!components.containsKey(cell)) {
					if (getExtendModel().isVertex(cell) && isCellVisible(cell)) {
						added = true;
						components.put(cell, currentComponent);
						// add all nodes connected to cell
						HashSet<Object> descendants = descendants4vertex
								.get(cell);
						Object[] conns = connections4vertex.get(cell);
						for (int j = 0; j < conns.length; j++) {
							Object connCell = getTerminalOutsideSet(conns[j],
									descendants);
							connCell = descendant2ancestor.get(connCell);
							if (connCell != null)
								cells.push(connCell);
						}
					}
				}
			}
			if (added)
				currentComponent++;
		}
		@SuppressWarnings("unchecked")
		HashSet<Object>[] verticesInComponents = new HashSet[currentComponent];
		for (int i = 0; i < currentComponent; i++) {
			verticesInComponents[i] = new HashSet<Object>();
		}
		for (Object v : components.keySet()) {
			int i = components.get(v);
			// if (model.getParent(v)==parent)
			verticesInComponents[i].add(v);
		}
		ArrayList<Set<Object>> listVerticesInComponents = new ArrayList<Set<Object>>();
		for (int i = 0; i < currentComponent; i++) {
			listVerticesInComponents.add(verticesInComponents[i]);
		}
		return listVerticesInComponents;
	}
	
	public Object[] cloneCells(Object[] cells, boolean allowInvalidEdges,
			Map<Object, Object> mapping) {
		Object[] clones = null;

		if (cells != null) {
			Collection<Object> tmp = new LinkedHashSet<Object>(cells.length);
			tmp.addAll(Arrays.asList(cells));

			if (!tmp.isEmpty()) {
				double scale = view.getScale();
				mxPoint trans = view.getTranslate();
				clones = getExtendModel().cloneCells(cells, true, mapping);

				for (int i = 0; i < cells.length; i++) {
					if (!allowInvalidEdges
							&& getExtendModel().isEdge(clones[i])
							&& getEdgeValidationError(clones[i],
									getExtendModel().getTerminal(clones[i], true),
									getExtendModel().getTerminal(clones[i], false)) != null) {
						clones[i] = null;
					} else {
						mxGeometry g = getExtendModel().getGeometry(clones[i]);

						if (g != null) {
							mxCellState state = view.getState(cells[i]);
							mxCellState pstate = view.getState(getExtendModel()
									.getParent(cells[i]));

							if (state != null && pstate != null) {
								double dx = pstate.getOrigin().getX();
								double dy = pstate.getOrigin().getY();

								if (getExtendModel().isEdge(clones[i])) {
									// Checks if the source is cloned or sets
									// the terminal point
									Object src = getExtendModel().getTerminal(cells[i],
											true);

									while (src != null && !tmp.contains(src)) {
										src = getExtendModel().getParent(src);
									}

									if (src == null) {
										mxPoint pt = state.getAbsolutePoint(0);
										g.setTerminalPoint(
												new mxPoint(pt.getX() / scale
														- trans.getX(), pt
														.getY()
														/ scale
														- trans.getY()), true);
									}

									// Checks if the target is cloned or sets
									// the terminal point
									Object trg = getExtendModel().getTerminal(cells[i],
											false);

									while (trg != null && !tmp.contains(trg)) {
										trg = getExtendModel().getParent(trg);
									}

									if (trg == null) {
										mxPoint pt = state
												.getAbsolutePoint(state
														.getAbsolutePointCount() - 1);
										g.setTerminalPoint(
												new mxPoint(pt.getX() / scale
														- trans.getX(), pt
														.getY()
														/ scale
														- trans.getY()), false);
									}

									// Translates the control points
									List<mxPoint> points = g.getPoints();

									if (points != null) {
										Iterator<mxPoint> it = points
												.iterator();

										while (it.hasNext()) {
											mxPoint pt = it.next();
											pt.setX(pt.getX() + dx);
											pt.setY(pt.getY() + dy);
										}
									}
								} else {
									g.setX(g.getX() + dx);
									g.setY(g.getY() + dy);
								}
							}
						}
					}
				}
			} else {
				clones = new Object[] {};
			}
		}

		return clones;
	}
}
