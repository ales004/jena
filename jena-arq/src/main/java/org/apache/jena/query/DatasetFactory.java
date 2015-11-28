/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.query;

import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.util.DatasetUtils;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.util.FileManager;

/**
 * Makes {@link Dataset}s in various ways.
 *
 */
public class DatasetFactory {

	/**
	 * @return an in-memory, modifiable Dataset
	 * @deprecated Prefer {@link #createGeneral()} or {@link #createTxnMem()}
	 */
	@Deprecated
	public static Dataset createMem() {
		return create(DatasetGraphFactory.createMem());
	}

	/**
     * Create an in-memory. transactional Dataset.
     * <p> 
     * This fully supports transactions, including abort to roll-back changes.
     * It provides "autocommit" if operations are performed
     * outside a transaction but with a performance impact
     * (the implementation adds a begin/commit around each add or delete
     * so overheads can accumulate).
     * 
     * @return a transactional, in-memory, modifiable Dataset which
     * 
     */
	public static Dataset createTxnMem() {
		return create(DatasetGraphFactory.createTxnMem());
	}

	/**
	 * @return a general-purpose, in-memory, modifiable Dataset
	 */
	public static Dataset createGeneral() {
		return createMem();
	}

	/**
	 * @return an in-memory, modifiable Dataset. New graphs must be explicitly added using .addGraph.
	 */
	public static Dataset createMemFixed() {
		return create(DatasetGraphFactory.createMemFixed());
	}

	/**
	 * @param model The model for the default graph
	 * @return a dataset with the given model as the default graph
	 */
	public static Dataset create(final Model model) {
		return new DatasetImpl(model);
	}

	/**
	 * @param dataset Dataset to clone structure from.
	 * @return a dataset: clone the dataset structure of named graohs, and share the graphs themselves.
	 */
	public static Dataset create(final Dataset dataset) {
		return new DatasetImpl(dataset);
	}

	/**
	 * Wrap a {@link DatasetGraph} to make a mutable dataset
	 *
	 * @param dataset DatasetGraph
	 * @return Dataset
	 */
	public static Dataset create(final DatasetGraph dataset) {
		return DatasetImpl.wrap(dataset);
	}

	/**
	 * @param uriList URIs merged to form the default dataset
	 * @return a dataset based on a list of URIs : these are merged into the default graph of the dataset.
	 */
	public static Dataset create(final List<String> uriList) {
		return create(uriList, null, null);
	}

	/**
	 * @param uri URIs merged to form the default dataset
	 * @return a dataset with a default graph and no named graphs
	 */

	public static Dataset create(final String uri) {
		return create(uri, null, null);
	}

	/**
	 * @param namedSourceList
	 * @return a named graph container of graphs based on a list of URIs.
	 */

	public static Dataset createNamed(final List<String> namedSourceList) {
		return create((List<String>) null, namedSourceList, null);
	}

	/**
	 * Create a dataset based on two list of URIs. The first lists is used to create the background (unnamed graph) by
	 * merging, the second is used to create the collection of named graphs.
	 *
	 * (Jena calls graphs "Models" and triples "Statements")
	 *
	 * @param uriList graphs to be loaded into the unnamed, default graph
	 * @param namedSourceList graphs to be atatched as named graphs
	 * @return Dataset
	 */

	public static Dataset create(final List<String> uriList, final List<String> namedSourceList) {
		return create(uriList, namedSourceList, null);
	}

	/**
	 * Create a dataset container based on two list of URIs. The first is used to create the background (unnamed graph),
	 * the second is used to create the collection of named graphs.
	 *
	 * (Jena calls graphs "Models" and triples "Statements")
	 *
	 * @param uri graph to be loaded into the unnamed, default graph
	 * @param namedSourceList graphs to be attached as named graphs
	 * @return Dataset
	 */

	public static Dataset create(final String uri, final List<String> namedSourceList) {
		return create(uri, namedSourceList, null);
	}

	/**
	 * Create a named graph container based on two list of URIs. The first is used to create the background (unnamed
	 * graph), the second is used to create the collection of named graphs.
	 *
	 * (Jena calls graphs "Models" and triples "Statements")
	 *
	 * @param uri graph to be loaded into the unnamed, default graph
	 * @param namedSourceList graphs to be atatched as named graphs
	 * @param baseURI baseURI for relative URI expansion
	 * @return Dataset
	 */

	public static Dataset create(final String uri, final List<String> namedSourceList, final String baseURI) {
		return DatasetUtils.createDataset(uri, namedSourceList, baseURI);
	}

	/**
	 * Create a named graph container based on two list of URIs. The first is used to create the background (unnamed
	 * graph), the second is used to create the collection of named graphs.
	 *
	 * (Jena calls graphs "Models" and triples "Statements")
	 *
	 * @param uriList graphs to be loaded into the unnamed, default graph
	 * @param namedSourceList graphs to be atatched as named graphs
	 * @param baseURI baseURI for relative URI expansion
	 * @return Dataset
	 */

	public static Dataset create(final List<String> uriList, final List<String> namedSourceList, final String baseURI) {
		return DatasetUtils.createDataset(uriList, namedSourceList, baseURI);
	}

	public static Dataset make(final Dataset ds, final Model defaultModel) {
		final Dataset ds2 = new DatasetImpl(ds);
		ds2.setDefaultModel(defaultModel);
		return ds2;
	}

	// Assembler-based Dataset creation.

	/**
	 * Assemble a dataset from the model in a file
	 *
	 * @param filename The filename
	 * @return Dataset
	 */
	public static Dataset assemble(final String filename) {
		final Model model = FileManager.get().loadModel(filename);
		return assemble(model);
	}

	/**
	 * Assemble a dataset from the model in a file
	 *
	 * @param filename The filename
	 * @param resourceURI URI for the dataset to assembler
	 * @return Dataset
	 */
	public static Dataset assemble(final String filename, final String resourceURI) {
		final Model model = FileManager.get().loadModel(filename);
		final Resource r = model.createResource(resourceURI);
		return assemble(r);
	}

	/**
	 * Assemble a dataset from the model
	 *
	 * @param model
	 * @return Dataset
	 */
	public static Dataset assemble(final Model model) {
		final Resource r = GraphUtils.findRootByType(model, DatasetAssembler.getType());
		if (r == null) throw new ARQException("No root found for type <" + DatasetAssembler.getType() + ">");

		return assemble(r);
	}

	/**
	 * Assemble a dataset from a resource
	 *
	 * @param resource The resource for the dataset
	 * @return Dataset
	 */

	public static Dataset assemble(final Resource resource) {
		final Dataset ds = (Dataset) Assembler.general.open(resource);
		return ds;
	}
}
