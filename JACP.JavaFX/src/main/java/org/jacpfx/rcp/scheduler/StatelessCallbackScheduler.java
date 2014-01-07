/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [StatelessCallbackScheduler.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacpfx.rcp.scheduler;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.message.Message;
import org.jacpfx.api.component.IComponentHandle;
import org.jacpfx.api.component.IStatelessCallabackComponent;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.api.launcher.Launcher;
import org.jacpfx.api.scheduler.IStatelessComponentScheduler;
import org.jacpfx.rcp.component.AStatelessCallbackComponent;
import org.jacpfx.rcp.worker.StateLessComponentRunWorker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StatelessCallbackScheduler implements
        IStatelessComponentScheduler<EventHandler<Event>, Event, Object> {

    private final Launcher<?> launcher;

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    public StatelessCallbackScheduler(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    @Override
/**
 * {@inheritDoc }
 */
    public final void incomingMessage(
            final Message<Event, Object> message,
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        // TODO avoid locking of whole code block
        lock.writeLock().lock();
        try {
            // get active instance
            final ISubComponent<EventHandler<Event>, Event, Object> comp = this
                    .getActiveComponent(baseComponent);
            final List<ISubComponent<EventHandler<Event>, Event, Object>> componentInstances = baseComponent
                    .getInstances();
            if (comp != null) {
                if (componentInstances.size() < AStatelessCallbackComponent.MAX_INCTANCE_COUNT) {
                    // create new instance as buffer
                    IComponentHandle<?, Event, Object> handle = baseComponent.getComponent();
                    componentInstances.add(this.getCloneBean(baseComponent,
                            handle.getClass()));
                } // End inner if
                // run component in thread
                this.instanceRun(baseComponent, comp, message);
            } // End if
            else {
                // check if new instances can be created
                if (componentInstances.size() < AStatelessCallbackComponent.MAX_INCTANCE_COUNT) {
                    this.createInstanceAndRun(baseComponent, message);
                } // End if
                else {
                    this.seekAndPutMessage(baseComponent, message);
                } // End else
            } // End else

        } finally {
            lock.writeLock().unlock();
        } // End synchronized
    }

    /**
     * block component, put message to component's queue and run in thread
     *
     * @param baseComponent, the parent component
     * @param comp, the child component
     * @param message, the message
     */
    private void instanceRun(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final ISubComponent<EventHandler<Event>, Event, Object> comp,
            final Message<Event, Object> message) {
        comp.putIncomingMessage(message);
        // TODO switch to embedded worker!!
        baseComponent.getExecutorService().submit(new StateLessComponentRunWorker(
                comp, baseComponent));
    }

    /**
     * if max thread count is not reached and all available component instances
     * are blocked create a new one, block it an run in thread
     * @param baseComponent, the parent component
     * @param message, the current message
     */
    private void createInstanceAndRun(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final Message<Event, Object> message) {
        IComponentHandle<?, Event, Object> handle = baseComponent.getComponent();
        final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> comp = this
                .getCloneBean(baseComponent,
                        handle.getClass());
        baseComponent.getInstances().add(comp);
        this.instanceRun(baseComponent, comp, message);
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * {@inheritDoc }
     */
    public final <T extends IStatelessCallabackComponent<EventHandler<Event>, Event, Object>, H extends IComponentHandle> IStatelessCallabackComponent<EventHandler<Event>, Event, Object> getCloneBean(
            final T baseComponent,
            final Class<H> clazz) {
        return ((AStatelessCallbackComponent) baseComponent).init(this.launcher
                .getBean(clazz));
    }

    /**
     * Returns a component instance that is currently not blocked.
     *
     * @param baseComponent, the parent component
     * @return an available subcomponent
     */
    private ISubComponent<EventHandler<Event>, Event, Object> getActiveComponent(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        // TODO this solution is crappy and dangerous!
        for (final ISubComponent<EventHandler<Event>, Event, Object> comp : baseComponent
                .getInstances()) {
            if (!comp.isBlocked()) {
                return comp;
            } // End if
        } // End for

        return null;
    }

    /**
     * seek to first running component in instance list and add message to queue
     * of selected component
     *
     * @param baseComponent, the root component
     * @param message, the current message
     */
    private void seekAndPutMessage(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent,
            final Message<Event, Object> message) {
        // if max count reached, seek through components and add
        // message to queue of oldest component
        final ISubComponent<EventHandler<Event>, Event, Object> comp = baseComponent
                .getInstances().get(this.getSeekValue(baseComponent));
        // put message to queue
        comp.putIncomingMessage(message);
    }

    private int getSeekValue(
            final IStatelessCallabackComponent<EventHandler<Event>, Event, Object> baseComponent) {
        final AtomicInteger threadCount = baseComponent.getThreadCounter();
        final int seek = threadCount.incrementAndGet()
                % baseComponent.getInstances().size();
        threadCount.set(seek);
        return seek;
    }

}