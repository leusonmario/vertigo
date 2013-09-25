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
package net.kuujo.vevent.java;

import net.kuujo.vevent.context.ComponentContext;
import net.kuujo.vevent.feeder.Feeder;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * An abstract feeder verticle.
 *
 * @author Jordan Halterman
 */
abstract class FeederVerticle extends Verticle implements Handler<Feeder> {

  protected ComponentContext context;

  protected Feeder feeder;

  /**
   * Creates a root feeder instance.
   *
   * @return
   *   A new root feeder instance.
   */
  abstract Feeder createFeeder(ComponentContext context);

  @Override
  public void start() {
    context = new ComponentContext(container.config());
    feeder = createFeeder(context);
    feeder.feedHandler(this);
    feeder.ackHandler(new Handler<JsonObject>() {
      @Override
      public void handle(JsonObject data) {
        ack(data);
      }
    });
    feeder.failHandler(new Handler<JsonObject>() {
      @Override
      public void handle(JsonObject data) {
        fail(data);
      }
    });
  }

  /**
   * Called when data is successfully processed
   */
  protected void ack(JsonObject data) {
  }

  /**
   * Called when data fails processing.
   */
  protected void fail(JsonObject data) {
  }

}