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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A channel publisher which publishes a single message to multiple channels.
 *
 * @author Jordan Halterman
 */
public interface ChannelPublisher<T extends Channel<?>> {

  /**
   * Adds a channel to the publisher.
   *
   * @param channel
   *   The channel to add.
   */
  public ChannelPublisher<T> addChannel(T channel);

  /**
   * Removes a channel from the publisher.
   *
   * @param channel
   *   The channel to remove.
   */
  public ChannelPublisher<T> removeChannel(T channel);

  /**
   * Publishes a message.
   *
   * @param message
   *   The message to publish.
   */
  public void publish(JsonMessage message);

  /**
   * Publishes a message.
   *
   * @param message
   *   The message to publish.
   * @param doneHandler
   *   A handler to be invoked once the message has been received.
   */
  public void publish(JsonMessage message, Handler<AsyncResult<Void>> doneHandler);

}
