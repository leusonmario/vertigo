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

package net.kuujo.vertigo.input.connection.impl;

import net.kuujo.vertigo.input.connection.InputConnectionInfo;
import net.kuujo.vertigo.connection.SourceInfo;
import net.kuujo.vertigo.connection.TargetInfo;
import net.kuujo.vertigo.input.port.InputPortInfo;
import net.kuujo.vertigo.connection.impl.BaseConnectionInfoImpl;
import net.kuujo.vertigo.util.Args;

/**
 * Input connection info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputConnectionInfoImpl extends BaseConnectionInfoImpl<InputConnectionInfo> implements InputConnectionInfo {
  private InputPortInfo port;

  @Override
  public InputPortInfo port() {
    return port;
  }

  /**
   * Input connection info builder.
   */
  public static class Builder implements InputConnectionInfo.Builder {
    private InputConnectionInfoImpl connection;

    public Builder() {
      connection = new InputConnectionInfoImpl();
    }

    public Builder(InputConnectionInfoImpl connection) {
      this.connection = connection;
    }

    @Override
    public Builder setAddress(String address) {
      Args.checkNotNull(address, "address cannot be null");
      connection.address = address;
      return this;
    }

    @Override
    public Builder setSource(SourceInfo source) {
      Args.checkNotNull(source, "source cannot be null");
      connection.source = source;
      return this;
    }

    @Override
    public Builder setTarget(TargetInfo target) {
      Args.checkNotNull(target, "target cannot be null");
      connection.target = target;
      return this;
    }

    @Override
    public Builder setPort(InputPortInfo port) {
      Args.checkNotNull(port, "port cannot be null");
      connection.port = port;
      return this;
    }

    @Override
    public InputConnectionInfo build() {
      return connection;
    }
  }

}