/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [AStatelessCallbackComponent.java]
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
package org.jacpfx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.ComponentHandle;
import org.jacpfx.api.component.StatelessCallabackComponent;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.rcp.context.Context;
import org.jacpfx.rcp.context.InternalContext;
import org.jacpfx.rcp.util.HandlerThreadFactory;
import org.jacpfx.rcp.util.ShutdownThreadsHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * represents a abstract stateless background component
 * 
 * @author Andy Moncsek
 * 
 */

public abstract class AStatelessCallbackComponent extends ASubComponent
		implements
        StatelessCallabackComponent<EventHandler<Event>, Event, Object> {
	public static final int MAX_INCTANCE_COUNT = getNrOfProcessors()+1;



	private final AtomicInteger threadCount = new AtomicInteger(0);

	private final List<SubComponent<EventHandler<Event>, Event, Object>> componentInstances = new CopyOnWriteArrayList<>();

	private final ExecutorService executor = Executors
			.newFixedThreadPool(AStatelessCallbackComponent.MAX_INCTANCE_COUNT,new HandlerThreadFactory("AStatelessCallbackComponent:"));


	private static int getNrOfProcessors() {
		final Runtime runtime = Runtime.getRuntime();
		return runtime.availableProcessors();
	}

	public AStatelessCallbackComponent() {
		ShutdownThreadsHandler.registerExecutor(executor);
	}






	/**
	 * init cloned instance with values of blueprint
	 * 
	 * @param handler, the component handler which represents the implemented component
	 * @param context, the jacpfx context
	 * @return a statless callback component
	 */
	public final StatelessCallabackComponent<EventHandler<Event>, Event, Object> init(
			final ComponentHandle<Object,Event, Object> handler, final Context context) {

        final StatelessCallabackComponent<EventHandler<Event>, Event, Object> comp = new EmbeddedStatelessCallbackComponent(handler);
        comp.initEnv(this.getContext().getParentId(), this.globalMessageQueue);
        initContextObject(comp,context);
		return comp;
	}

    private void initContextObject(final StatelessCallabackComponent<EventHandler<Event>, Event, Object> comp,final Context context) {
        InternalContext currentContext = InternalContext.class.cast(comp.getContext());
        currentContext.setId(context.getId());
        currentContext.setParentId(context.getParentId());
        currentContext.updateExecutionTarget(InternalContext.class.cast(context).getExecutionTarget());
    }

	@Override
	public final List<SubComponent<EventHandler<Event>, Event, Object>> getInstances() {
		return this.componentInstances;
	}

	@Override
	public final AtomicInteger getThreadCounter() {
		return this.threadCount;
	}

	@Override
	public final ExecutorService getExecutorService() {
		return this.executor;
	}

}
