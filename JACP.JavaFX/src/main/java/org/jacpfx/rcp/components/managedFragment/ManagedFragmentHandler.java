/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [ManagedFragmentHandler.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */
package org.jacpfx.rcp.components.managedFragment;

import javafx.scene.Node;

/**
 * A managedDialogHandler is the return value of a JACP managed dialog. It wraps
 * access to the managed controller class and the JavaFX root Node of the
 * dialog. A controller must be annotated with @Fragment and can either extend a
 * JavaFX node or specify the url to fxml in the @Fragment annotation. In both
 * cases the result is a controller and a JavaFX Node. if not using fxml the
 * controller and the root node are the same.
 * 
 * @author Andy Moncsek
 * 
 * @param <T> The type of the managed fragment
 */
public class ManagedFragmentHandler<T> {

	private final T controller;
	private final Node dialogNode;
	private final String id;

	public ManagedFragmentHandler(T controller, Node dialogNode, final String id) {
		this.controller = controller;
		this.dialogNode = dialogNode;
		this.id = id;
	}
	/**
	 * Returns the controller instance.
	 * @return the controller instance of your managed fragment
	 */
	public T getController() {
		return this.controller;
	}
	/**
	 * Returns the JavaFX Node
	 * @return the UI node of your managed fragment
	 */
	public Node getFragmentNode() {
		return this.dialogNode;
	}
	/**
	 * Returns the Id.
	 * @return the id of your managed fragment
	 */
	public String getId() {
		return id;
	}

}
