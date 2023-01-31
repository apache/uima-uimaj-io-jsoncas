/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.uima.json.jsoncas2.model;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.cas.FeatureStructure;
import com.fasterxml.jackson.databind.DatabindContext;

public class FeatureStructures implements Iterable<FeatureStructure> {
  public static final String ALL_FEATURE_STRUCTURES = "UIMA.AllFeatureStructures";

  private final List<FeatureStructure> featureStructures;

  private final Set<String> typeNames;

  public FeatureStructures(Collection<FeatureStructure> aFeatureStructures) {
    typeNames = new HashSet<>();
    featureStructures = aFeatureStructures.stream() //
            .map(fs -> {
              typeNames.add(fs.getType().getName());
              return fs;
            }) //
            .sorted(comparing(fs -> fs.getType().getName())) //
            .collect(toList());
  }

  public boolean existsAnnotationOfType(String aTypeName) {
    return typeNames.contains(aTypeName);
  }

  @Override
  public Iterator<FeatureStructure> iterator() {
    return featureStructures.iterator();
  }

  public boolean isEmpty() {
    return featureStructures.isEmpty();
  }

  public static void set(DatabindContext aProvider, FeatureStructures aAllFs) {
    aProvider.setAttribute(ALL_FEATURE_STRUCTURES, aAllFs);
  }

  public static FeatureStructures get(DatabindContext aProvider) {
    return (FeatureStructures) aProvider.getAttribute(ALL_FEATURE_STRUCTURES);
  }
}
