/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra.evaluation.function.geosparql;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class GeometricUnaryFunctionTest {

	Literal amsterdam = SimpleValueFactory.getInstance().createLiteral("POINT(4.9 52.37)", GEO.WKT_LITERAL);

	protected abstract GeometricUnaryFunction testedFunction();

	@Test(expected = ValueExprEvaluationException.class)
	public void testRelationExceptionHandling() {
		GeometricUnaryFunction testedFunction = Mockito.spy(testedFunction());
		Mockito.doThrow(new RuntimeException("forsooth!")).when(testedFunction).operation(Mockito.any());
		testedFunction.evaluate(SimpleValueFactory.getInstance(), amsterdam);
	}

}
