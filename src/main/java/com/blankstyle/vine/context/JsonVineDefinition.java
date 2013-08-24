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
package com.blankstyle.vine.context;


/**
 * A default vine context implementation.
 *
 * @author Jordan Halterman
 */
public class JsonVineDefinition extends AbstractDefinition<VineDefinition> implements VineDefinition {

  @Override
  public VineDefinition setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public SeedDefinition feed(SeedDefinition definition) {
    if (!connections.contains(definition)) {
      connections.add(definition);
    }
    return definition;
  }

  @Override
  public SeedDefinition feed(String address) {
    return feed(address, null, 1);
  }

  @Override
  public SeedDefinition feed(String address, String main) {
    return feed(address, main, 1);
  }

  @Override
  public SeedDefinition feed(String address, String main, int workers) {
    return (SeedDefinition) addConnection(new JsonSeedDefinition().setAddress(address).setMain(main).setWorkers(workers));
  }

}