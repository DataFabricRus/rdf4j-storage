/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.AST;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclSailConnection;
import org.eclipse.rdf4j.sail.shacl.planNodes.ExternalTypeFilterNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.LoggingNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.Select;
import org.eclipse.rdf4j.sail.shacl.planNodes.TrimTuple;

import java.util.stream.Stream;

/**
 * The AST (Abstract Syntax Tree) node
 *
 * @author Heshan Jayasinghe
 */
public class TargetClass extends NodeShape {

	Resource targetClass;

	TargetClass(Resource id, ShaclSailConnection connection) {
		super(id, connection);

		try (Stream<? extends Statement> stream = Iterations.stream(connection.getStatements(id, SHACL.TARGET_CLASS, null, true, ShaclSail.SHACL_GRAPH))) {
			targetClass = stream.map(Statement::getObject).map(v -> (Resource) v).findAny().orElseThrow(() -> new RuntimeException("Expected to find sh:targetClass on " + id));
		}

	}

	@Override
	public PlanNode getPlan(ShaclSailConnection shaclSailConnection, NodeShape nodeShape) {
		return new TrimTuple(new LoggingNode(new Select(shaclSailConnection, getQuery())), 1);
	}

	@Override
	public PlanNode getPlanAddedStatements(ShaclSailConnection shaclSailConnection, NodeShape nodeShape) {
		return new TrimTuple(new LoggingNode(new Select(shaclSailConnection.getAddedStatements(), getQuery())), 1);

	}

	@Override
	public PlanNode getPlanRemovedStatements(ShaclSailConnection shaclSailConnection, NodeShape nodeShape) {
		return new Select(shaclSailConnection.getRemovedStatements(), getQuery());
	}

	@Override
	public boolean requiresEvaluation(Repository addedStatements, Repository removedStatements) {
		boolean requiresEvalutation;
		try (RepositoryConnection addedStatementsConnection = addedStatements.getConnection()) {
			requiresEvalutation = addedStatementsConnection.hasStatement(null, RDF.TYPE, targetClass, false);
		}

		return super.requiresEvaluation(addedStatements, removedStatements) || requiresEvalutation;
	}

	@Override
	public String getQuery() {
		return "BIND(rdf:type as ?b) \n BIND(<" + targetClass + "> as ?c) \n ?a ?b ?c.";
	}

	public PlanNode getTypeFilterPlan(NotifyingSailConnection shaclSailConnection, PlanNode parent) {
		return new ExternalTypeFilterNode(shaclSailConnection, targetClass, parent);
	}

}
