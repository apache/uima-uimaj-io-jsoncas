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
package org.apache.uima.json.jsoncas2.ser;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.json.jsoncas2.mode.TypeSystemMode;
import org.apache.uima.json.jsoncas2.model.FeatureStructures;
import org.apache.uima.json.jsoncas2.ref.ReferenceCache;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TypeSystemSerializer extends StdSerializer<TypeSystem> {
  private static final long serialVersionUID = -4369127219437592227L;

  private static final Set<String> BUILT_IN_TYPES = unmodifiableSet(new HashSet<>(asList(
          CAS.TYPE_NAME_ANNOTATION, CAS.TYPE_NAME_ANNOTATION_BASE, CAS.TYPE_NAME_ARRAY_BASE,
          CAS.TYPE_NAME_BOOLEAN, CAS.TYPE_NAME_BOOLEAN_ARRAY, CAS.TYPE_NAME_BYTE,
          CAS.TYPE_NAME_BYTE_ARRAY, CAS.TYPE_NAME_DOCUMENT_ANNOTATION, CAS.TYPE_NAME_DOUBLE,
          CAS.TYPE_NAME_DOUBLE_ARRAY, CAS.TYPE_NAME_EMPTY_FLOAT_LIST, CAS.TYPE_NAME_EMPTY_FS_LIST,
          CAS.TYPE_NAME_EMPTY_INTEGER_LIST, CAS.TYPE_NAME_EMPTY_STRING_LIST, CAS.TYPE_NAME_FLOAT,
          CAS.TYPE_NAME_FLOAT_ARRAY, CAS.TYPE_NAME_FLOAT_LIST, CAS.TYPE_NAME_FS_ARRAY,
          CAS.TYPE_NAME_FS_LIST, CAS.TYPE_NAME_INTEGER, CAS.TYPE_NAME_INTEGER_ARRAY,
          CAS.TYPE_NAME_INTEGER_LIST, CAS.TYPE_NAME_LIST_BASE, CAS.TYPE_NAME_LONG,
          CAS.TYPE_NAME_LONG_ARRAY, CAS.TYPE_NAME_NON_EMPTY_FLOAT_LIST,
          CAS.TYPE_NAME_NON_EMPTY_FS_LIST, CAS.TYPE_NAME_NON_EMPTY_INTEGER_LIST,
          CAS.TYPE_NAME_NON_EMPTY_STRING_LIST, CAS.TYPE_NAME_SHORT, CAS.TYPE_NAME_SHORT_ARRAY,
          CAS.TYPE_NAME_SOFA, CAS.TYPE_NAME_STRING, CAS.TYPE_NAME_STRING_ARRAY,
          CAS.TYPE_NAME_STRING_LIST, CAS.TYPE_NAME_TOP)));

  public TypeSystemSerializer() {
    super(TypeSystem.class);
  }

  @Override
  public void serialize(TypeSystem aTypeSystem, JsonGenerator jg, SerializerProvider aProvider)
          throws IOException {
    ReferenceCache refCache = ReferenceCache.get(aProvider);

    List<Type> types = findTypesToSerialize(aTypeSystem, aProvider);

    jg.writeStartObject(aTypeSystem);

    for (Type type : types) {
      jg.writeFieldName(refCache.typeRef(type));

      aProvider.defaultSerializeValue(type, jg);
    }

    jg.writeEndObject();
  }

  private List<Type> findTypesToSerialize(TypeSystem aTypeSystem, SerializerProvider aProvider) {
    Iterable<Type> typesSource = aTypeSystem;

    if (TypeSystemMode.get(aProvider) == TypeSystemMode.MINIMAL) {
      Deque<Type> queue = collectTypesUsedByFeatureStructures(aTypeSystem, aProvider);
      typesSource = collectTypesUsedByTypes(aTypeSystem, typesSource, queue);
    }

    return StreamSupport.stream(typesSource.spliterator(), false) //
            .filter(type -> !type.isArray()) //
            .filter(type -> !BUILT_IN_TYPES.contains(type.getName())) //
            .sorted(comparing(Type::getName)) //
            .collect(toList());
  }

  private Set<Type> collectTypesUsedByTypes(TypeSystem aTypeSystem, Iterable<Type> typesSource, Deque<Type> queue) {
    Set<Type> typeSet = new HashSet<>();
    while (!queue.isEmpty()) {
      Type t = queue.poll();

      if (typeSet.contains(t)) {
        continue;
      }

      for (Feature f : t.getFeatures()) {
        Type parent = aTypeSystem.getParent(t);
        while (parent != null) {
          if (!typeSet.contains(parent)) {
            queue.add(parent);
          }
          parent = aTypeSystem.getParent(parent);
        }
        
        Type range = f.getRange();
        if (!typeSet.contains(range)) {
          queue.add(range);
        }
        
        Type componentType = range.getComponentType();
        if (componentType != null && !typeSet.contains(typesSource)) {
          queue.add(componentType);
        }
      }
      
      typeSet.add(t);
    }
    return typeSet;
  }

  private Deque<Type> collectTypesUsedByFeatureStructures(TypeSystem aTypeSystem,
          SerializerProvider aProvider) {
    FeatureStructures allFs = FeatureStructures.get(aProvider);
    Deque<Type> queue = new ArrayDeque<>();
    aTypeSystem.forEach(type -> {
      if (allFs.existsAnnotationOfType(type.getName())) {
        queue.add(type);
      }
    });
    return queue;
  }
}
