/*
* Copyright 2013 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.kuujo.vine.feeder;

import java.util.Collection;
import java.util.Iterator;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vine.context.ConnectionContext;
import net.kuujo.vine.context.VineContext;
import net.kuujo.vine.eventbus.ReliableEventBus;
import net.kuujo.vine.eventbus.WrappedReliableEventBus;
import net.kuujo.vine.messaging.ConnectionPool;
import net.kuujo.vine.messaging.CoordinatingOutputCollector;
import net.kuujo.vine.messaging.Dispatcher;
import net.kuujo.vine.messaging.EventBusConnection;
import net.kuujo.vine.messaging.EventBusConnectionPool;
import net.kuujo.vine.messaging.EventBusStream;
import net.kuujo.vine.messaging.OutputCollector;

/**
 * An abstract feeder implementation. This class handles setup of vine
 * input and output streams.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractFeeder {

  protected VineContext context;

  protected ReliableEventBus eventBus;

  protected OutputCollector output;

  public AbstractFeeder(VineContext context, Vertx vertx) {
    this(context, vertx, vertx.eventBus());
  }

  public AbstractFeeder(VineContext context, EventBus eventBus) {
    this.context = context;
    setEventBus(eventBus);
  }

  public AbstractFeeder(VineContext context, Vertx vertx, EventBus eventBus) {
    this.context = context;
    setEventBus(eventBus).setVertx(vertx);
  }

  private ReliableEventBus setEventBus(EventBus eventBus) {
    if (eventBus instanceof ReliableEventBus) {
      this.eventBus = (ReliableEventBus) eventBus;
    }
    else {
      this.eventBus = new WrappedReliableEventBus(eventBus);
    }
    return this.eventBus;
  }

  /**
   * Initializes the feeder, setting up input and output streams.
   */
  protected void init() {
    setupOutputs();
  }

  /**
   * Sets up seed outputs.
   */
  protected void setupOutputs() {
    output = new CoordinatingOutputCollector();

    Collection<ConnectionContext> connections = context.getConnectionContexts();
    Iterator<ConnectionContext> iter = connections.iterator();
    while (iter.hasNext()) {
      ConnectionContext connectionContext = iter.next();
      try {
        JsonObject grouping = connectionContext.getGrouping();
        Dispatcher dispatcher = (Dispatcher) Class.forName(grouping.getString("dispatcher")).newInstance();

        // Set options on the dispatcher. All non-"dispatcher" values
        // are considered to be dispatcher options.
        Iterator<String> fieldNames = grouping.getFieldNames().iterator();
        while (fieldNames.hasNext()) {
          String fieldName = fieldNames.next();
          if (fieldName != "dispatcher") {
            String value = grouping.getString(fieldName);
            dispatcher.setOption(fieldName, value);
          }
        }

        // Create a connection pool from which the dispatcher will dispatch messages.
        ConnectionPool<EventBusConnection> connectionPool = new EventBusConnectionPool();
        String[] addresses = connectionContext.getAddresses();
        for (String address : addresses) {
          connectionPool.add(new EventBusConnection(address, eventBus));
        }

        // Initialize the dispatcher and add a channel to the channels list.
        dispatcher.init(connectionPool);
        output.addStream(connectionContext.getSeedName(), new EventBusStream(dispatcher));
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      }
    }
  }

}
