/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.cluster.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.cluster.ClusterException;
import net.kuujo.vertigo.cluster.Group;
import net.kuujo.vertigo.cluster.Node;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Default group client implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultGroup implements Group {
  private static final long DEFAULT_REPLY_TIMEOUT = 30000;
  private final String address;
  private final Vertx vertx;
  private final Container container;
  private final Map<Handler<Node>, Handler<Message<String>>> joinHandlers = new HashMap<>();
  private final Map<Handler<Node>, Handler<Message<String>>> leaveHandlers = new HashMap<>();

  public DefaultGroup(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public Group registerJoinHandler(final Handler<Node> handler) {
    return registerJoinHandler(handler, null);
  }

  @Override
  public Group registerJoinHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        handler.handle(new DefaultNode(message.body(), vertx, container));
      }
    };
    joinHandlers.put(handler, messageHandler);
    vertx.eventBus().registerHandler(String.format("%s.join", address), messageHandler, doneHandler);
    return this;
  }

  @Override
  public Group unregisterJoinHandler(final Handler<Node> handler) {
    return unregisterJoinHandler(handler, null);
  }

  @Override
  public Group unregisterJoinHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = joinHandlers.remove(handler);
    if (messageHandler != null) {
      vertx.eventBus().unregisterHandler(String.format("%s.join", address), messageHandler, doneHandler);
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public Group registerLeaveHandler(final Handler<Node> handler) {
    return registerLeaveHandler(handler, null);
  }

  @Override
  public Group registerLeaveHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        handler.handle(new DefaultNode(message.body(), vertx, container));
      }
    };
    leaveHandlers.put(handler, messageHandler);
    vertx.eventBus().registerHandler(String.format("%s.leave", address), messageHandler, doneHandler);
    return this;
  }

  @Override
  public Group unregisterLeaveHandler(final Handler<Node> handler) {
    return unregisterLeaveHandler(handler, null);
  }

  @Override
  public Group unregisterLeaveHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = leaveHandlers.remove(handler);
    if (messageHandler != null) {
      vertx.eventBus().unregisterHandler(String.format("%s.leave", address), messageHandler, doneHandler);
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public Group ping(final Handler<AsyncResult<Group>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "ping");
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Group>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Group>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("pong")) {
          if (result.result().body().getString("result").equals("cluster")) {
            new DefaultFutureResult<Group>(DefaultGroup.this).setHandler(resultHandler);
          } else {
            new DefaultFutureResult<Group>(new ClusterException("Not a valid group address.")).setHandler(resultHandler);
          }
        }
      }
    });
    return this;
  }

  @Override
  public Group getNode(String node, final Handler<AsyncResult<Node>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "find")
        .putString("type", "node")
        .putString("node", node);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Node>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Node>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Node>(new DefaultNode(result.result().body().getString("result"), vertx, container)).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Group getNodes(final Handler<AsyncResult<Collection<Node>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "list")
        .putString("type", "node");
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<Node>>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<Node>>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          JsonArray jsonNodes = result.result().body().getArray("result");
          List<Node> nodes = new ArrayList<>();
          for (Object jsonNode : jsonNodes) {
            nodes.add(new DefaultNode((String) jsonNode, vertx, container));
          }
          new DefaultFutureResult<Collection<Node>>(nodes).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Group selectNode(Object key, final Handler<AsyncResult<Node>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "select")
        .putString("type", "node")
        .putValue("key", key);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Node>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Node>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Node>(new DefaultNode(result.result().body().getString("result"), vertx, container)).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Group installModule(String moduleName) {
    return installModule(moduleName, null);
  }

  @Override
  public Group installModule(final String moduleName, final Handler<AsyncResult<Void>> doneHandler) {
    getNodes(new Handler<AsyncResult<Collection<Node>>>() {
      @Override
      public void handle(AsyncResult<Collection<Node>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(result.result().size()).setHandler(doneHandler);
          for (Node node : result.result()) {
            node.installModule(moduleName, counter);
          }
        }
      }
    });
    return this;
  }

  @Override
  public Group uninstallModule(String moduleName) {
    return uninstallModule(moduleName, null);
  }

  @Override
  public Group uninstallModule(final String moduleName, final Handler<AsyncResult<Void>> doneHandler) {
    getNodes(new Handler<AsyncResult<Collection<Node>>>() {
      @Override
      public void handle(AsyncResult<Collection<Node>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(result.result().size()).setHandler(doneHandler);
          for (Node node : result.result()) {
            node.uninstallModule(moduleName, counter);
          }
        }
      }
    });
    return this;
  }

  @Override
  public Group deployModule(String moduleName) {
    return deployModule(moduleName, null, 1, null);
  }

  @Override
  public Group deployModule(String moduleName, JsonObject config) {
    return deployModule(moduleName, config, 1, null);
  }

  @Override
  public Group deployModule(String moduleName, int instances) {
    return deployModule(moduleName, null, instances, null);
  }

  @Override
  public Group deployModule(String moduleName, JsonObject config, int instances) {
    return deployModule(moduleName, config, instances, null);
  }

  @Override
  public Group deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(moduleName, null, 1, doneHandler);
  }

  @Override
  public Group deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(moduleName, config, 1, doneHandler);
  }

  @Override
  public Group deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(moduleName, null, instances, doneHandler);
  }

  @Override
  public Group deployModule(String moduleName, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("type", "module")
        .putString("module", moduleName)
        .putObject("config", config != null ? config : new JsonObject())
        .putNumber("instances", instances);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<String>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Group deployVerticle(String main) {
    return deployVerticle(main, null, 1, null);
  }

  @Override
  public Group deployVerticle(String main, JsonObject config) {
    return deployVerticle(main, config, 1, null);
  }

  @Override
  public Group deployVerticle(String main, int instances) {
    return deployVerticle(main, null, instances, null);
  }

  @Override
  public Group deployVerticle(String main, JsonObject config, int instances) {
    return deployVerticle(main, config, instances, null);
  }

  @Override
  public Group deployVerticle(String main, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(main, null, 1, doneHandler);
  }

  @Override
  public Group deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(main, config, 1, doneHandler);
  }

  @Override
  public Group deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(main, null, instances, doneHandler);
  }

  @Override
  public Group deployVerticle(String main, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config != null ? config : new JsonObject())
        .putNumber("instances", instances);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<String>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Group deployWorkerVerticle(String main) {
    return deployWorkerVerticle(main, null, 1, false, null);
  }

  @Override
  public Group deployWorkerVerticle(String main, JsonObject config) {
    return deployWorkerVerticle(main, config, 1, false, null);
  }

  @Override
  public Group deployWorkerVerticle(String main, int instances) {
    return deployWorkerVerticle(main, null, instances, false, null);
  }

  @Override
  public Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded) {
    return deployWorkerVerticle(main, config, instances, false, null);
  }

  @Override
  public Group deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(main, null, 1, false, doneHandler);
  }

  @Override
  public Group deployWorkerVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(main, config, 1, false, doneHandler);
  }

  @Override
  public Group deployWorkerVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(main, null, instances, false, doneHandler);
  }

  @Override
  public Group deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config != null ? config : new JsonObject())
        .putNumber("instances", instances)
        .putBoolean("worker", true)
        .putBoolean("multi-threaded", multiThreaded);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<String>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Group undeployModule(String deploymentID) {
    return undeployModule(deploymentID, null);
  }

  @Override
  public Group undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "module")
        .putString("id", deploymentID);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Group undeployVerticle(String deploymentID) {
    return undeployVerticle(deploymentID, null);
  }

  @Override
  public Group undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "verticle")
        .putString("id", deploymentID);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public String toString() {
    return String.format("group:%s", address);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Group && ((Group) object).address().equals(address);
  }

}
