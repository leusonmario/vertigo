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
package net.kuujo.vertigo.cluster.data.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.kuujo.vertigo.cluster.data.AsyncMultiMap;
import net.kuujo.vertigo.cluster.data.DataException;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Default asynchronous multi-map implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public class DefaultAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {
  private final String address;
  private final String name;
  private final EventBus eventBus;

  public DefaultAsyncMultiMap(String address, String name, Vertx vertx) {
    this.address = address;
    this.name = name;
    this.eventBus = vertx.eventBus();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void put(K key, V value) {
    put(key, value, null);
  }

  @Override
  public void put(K key, V value, final Handler<AsyncResult<Boolean>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "put")
        .putString("type", "multimap")
        .putString("name", name)
        .putValue("key", key)
        .putValue("value", value);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void get(K key, final Handler<AsyncResult<Collection<V>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "get")
        .putString("type", "multimap")
        .putString("name", name)
        .putValue("key", key);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<V>>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<V>>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          JsonArray jsonValues = result.result().body().getArray("result");
          List<V> values = new ArrayList<>();
          for (Object jsonValue : jsonValues) {
            values.add((V) jsonValue);
          }
          new DefaultFutureResult<Collection<V>>(values).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void remove(K key) {
    remove(key, (Handler<AsyncResult<Collection<V>>>) null);
  }

  @Override
  public void remove(K key, final Handler<AsyncResult<Collection<V>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "remove")
        .putString("type", "multimap")
        .putString("name", name)
        .putValue("key", key);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<V>>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<V>>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          JsonArray jsonValues = result.result().body().getArray("result");
          List<V> values = new ArrayList<>();
          for (Object jsonValue : jsonValues) {
            values.add((V) jsonValue);
          }
          new DefaultFutureResult<Collection<V>>(values).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void remove(K key, V value) {
    remove(key, value, null);
  }

  @Override
  public void remove(K key, V value, final Handler<AsyncResult<Boolean>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "remove")
        .putString("type", "multimap")
        .putString("name", name)
        .putValue("key", key)
        .putValue("value", value);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void containsKey(K key, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "contains")
        .putString("type", "multimap")
        .putString("name", name)
        .putValue("key", key);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void containsValue(V value, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "contains")
        .putString("type", "multimap")
        .putString("name", name)
        .putValue("value", value);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void containsEntry(K key, V value, final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "contains")
        .putString("type", "multimap")
        .putString("name", name)
        .putValue("key", key)
        .putValue("value", value);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void keySet(final Handler<AsyncResult<Set<K>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "keys")
        .putString("type", "multimap")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Set<K>>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Set<K>>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          JsonArray jsonKeys = result.result().body().getArray("result");
          if (jsonKeys != null) {
            Set<K> keys = new HashSet<>();
            for (Object key : jsonKeys) {
              keys.add((K) key);
            }
            new DefaultFutureResult<Set<K>>(keys).setHandler(resultHandler);
          } else {
            new DefaultFutureResult<Set<K>>(new DataException("Invalid response.")).setHandler(resultHandler);
          }
        }
      }
    });
  }

  @Override
  public void values(final Handler<AsyncResult<Collection<V>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "values")
        .putString("type", "multimap")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<V>>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<V>>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          JsonArray jsonValues = result.result().body().getArray("result");
          if (jsonValues != null) {
            List<V> values = new ArrayList<>();
            for (Object value : jsonValues) {
              values.add((V) value);
            }
            new DefaultFutureResult<Collection<V>>(values).setHandler(resultHandler);
          } else {
            new DefaultFutureResult<Collection<V>>(new DataException("Invalid response.")).setHandler(resultHandler);
          }
        }
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "size")
        .putString("type", "multimap")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Integer>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Integer>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Integer>(result.result().body().getInteger("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "empty")
        .putString("type", "multimap")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(result.cause()).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Boolean>(new DataException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          new DefaultFutureResult<Boolean>(result.result().body().getBoolean("result")).setHandler(resultHandler);
        }
      }
    });
  }

  @Override
  public void clear() {
    clear(null);
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "clear")
        .putString("type", "multimap")
        .putString("name", name);
    eventBus.sendWithTimeout(address, message, 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new DataException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

}
