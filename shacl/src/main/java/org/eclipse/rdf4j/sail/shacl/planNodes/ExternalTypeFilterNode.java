/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.planNodes;


import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailException;

import java.util.Arrays;

/**
 * @author Håvard Ottestad
 */
public class ExternalTypeFilterNode implements PlanNode {

	NotifyingSailConnection shaclSailConnection;
	Resource filterOnType;
	PlanNode parent;
	int index = 0;
	private final boolean returnMatching;


	public ExternalTypeFilterNode(NotifyingSailConnection shaclSailConnection, Resource filterOnType, PlanNode parent, int index, boolean returnMatching) {
		this.shaclSailConnection = shaclSailConnection;
		this.filterOnType = filterOnType;
		this.parent = parent;
		this.index = index;
		this.returnMatching = returnMatching;
	}

	@Override
	public CloseableIteration<Tuple, SailException> iterator() {
		return new CloseableIteration<Tuple, SailException>() {


			Tuple next = null;


			CloseableIteration<Tuple, SailException> parentIterator = parent.iterator();


			void calculateNext() {
				while (next == null && parentIterator.hasNext()) {
					Tuple temp = parentIterator.next();

					Resource subject = (Resource) temp.line.get(index);

					if (returnMatching) {
						if (shaclSailConnection.hasStatement(subject, RDF.TYPE, filterOnType, true)) {
							next = temp;
							next.addHistory(new Tuple(Arrays.asList(subject, RDF.TYPE, filterOnType)));
						}
					}else{
						if (!shaclSailConnection.hasStatement(subject, RDF.TYPE, filterOnType, true)) {
							next = temp;
							next.addHistory(new Tuple(Arrays.asList(subject, RDF.TYPE, filterOnType)));
						}
					}

				}
			}

			@Override
			public void close() throws SailException {
				parentIterator.close();
			}

			@Override
			public boolean hasNext() throws SailException {
				calculateNext();
				return next != null;
			}

			@Override
			public Tuple next() throws SailException {
				calculateNext();

				Tuple temp = next;
				next = null;

				return temp;
			}

			@Override
			public void remove() throws SailException {

			}
		};
	}

	@Override
	public int depth() {
		return parent.depth() + 1;
	}

	@Override
	public void getPlanAsGraphvizDot(StringBuilder stringBuilder) {
		stringBuilder.append(getId() + " [label=\"" + StringEscapeUtils.escapeJava(this.toString()) + "\"];").append("\n");
		stringBuilder.append(parent.getId() + " -> " + getId()).append("\n");

		if (shaclSailConnection != null) {
			stringBuilder.append(System.identityHashCode(shaclSailConnection) + " -> " + getId() + " [label=\"filter source\"]").append("\n");
		}
		parent.getPlanAsGraphvizDot(stringBuilder);
	}

	@Override
	public String toString() {
		return "ExternalTypeFilterNode{" +
			"filterOnType=" + filterOnType +
			'}';
	}

	@Override
	public String getId() {
		return System.identityHashCode(this) + "";
	}

	@Override
	public IteratorData getIteratorDataType() {
		return parent.getIteratorDataType();
	}
}
