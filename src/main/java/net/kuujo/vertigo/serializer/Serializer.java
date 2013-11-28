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
package net.kuujo.vertigo.serializer;

import org.vertx.java.core.json.JsonObject;

/**
 * A data serializer.
 *
 * @author Jordan Halterman
 */
public interface Serializer {

  /**
   * Serializes an object.
   *
   * @param object
   *   The object to serialize.
   * @return
   *   The serialized object.
   * @throws SerializationException
   *   If the serialization fails.
   */
  public JsonObject serialize(Object object) throws SerializationException;

  /**
   * Deserializes an object.
   *
   * @param json
   *   The serialized object.
   * @param type
   *   The serialized type.
   * @return
   *   The deserialized object.
   * @throws SerializationException
   *   If the deserialization fails.
   */
  public <T> T deserialize(JsonObject json, Class<T> type) throws SerializationException;

}
