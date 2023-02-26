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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.UIMAFramework.getResourceSpecifierFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

import java.io.File;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.json.jsoncas2.JsonCas2Serializer;
import org.apache.uima.json.jsoncas2.mode.TypeSystemMode;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CasSerializerTest {
  private final static String USED_ANNOTATION_TYPE = "custom.UsedAnnotationType";

  private final static String ANNOTATION_TYPE_USED_FROM_RANGE = "custom.AnnotationTypeUsedFromRange";

  private final static String ANNOTATION_TYPE_USED_FROM_COMPONENT = "custom.AnnotationTypeUsedFromComponent";

  private final static String ANNOTATION_TYPE_USED_FROM_PARENT = "custom.AnnotationTypeUsedFromParent";

  private final static String UNUSED_ANNOTATION_TYPE = "custom.UnusedAnnotationType";

  private JsonCas2Serializer sut;

  @BeforeEach
  void setup() {
    sut = new JsonCas2Serializer();
  }

  @Test
  void thatUnusedTypesAreNotSerializedInMinimalTypeSystemMode(@TempDir
  File aTemp) throws Exception {
    CAS cas = CasCreationUtils.createCas(makeTypeSystem(), null, null);
    createFeatureStructure(USED_ANNOTATION_TYPE, cas);

    File out = new File(aTemp, "out.json");
    sut.setTypeSystemMode(TypeSystemMode.MINIMAL);
    sut.serialize(cas, out);

    assertThat(contentOf(out, UTF_8)).isEqualTo(
            contentOf(getClass().getResource("/CasSerializerTest/minimalTypeSystem.json"), UTF_8));
  }

  @Test
  void thatAllTypesAreSerializedInFullTypeSystemMode(@TempDir
  File aTemp) throws Exception {
    CAS cas = CasCreationUtils.createCas(makeTypeSystem(), null, null);
    createFeatureStructure(USED_ANNOTATION_TYPE, cas);

    File out = new File(aTemp, "out.json");
    sut.setTypeSystemMode(TypeSystemMode.FULL);
    sut.serialize(cas, out);

    assertThat(contentOf(out, UTF_8)).isEqualTo(
            contentOf(getClass().getResource("/CasSerializerTest/fullTypeSystem.json"), UTF_8));
  }

  @Test
  void thatNoTypesAreSerializedInNoTypeSystemMode(@TempDir
  File aTemp) throws Exception {
    CAS cas = CasCreationUtils.createCas(makeTypeSystem(), null, null);
    createFeatureStructure(USED_ANNOTATION_TYPE, cas);

    File out = new File(aTemp, "out.json");
    sut.setTypeSystemMode(TypeSystemMode.NONE);
    sut.serialize(cas, out);

    assertThat(contentOf(out, UTF_8)).isEqualTo(
            contentOf(getClass().getResource("/CasSerializerTest/noTypeSystem.json"), UTF_8));
  }

  private TypeSystemDescription makeTypeSystem() {
    TypeSystemDescription tsd = getResourceSpecifierFactory().createTypeSystemDescription();
    tsd.addType(ANNOTATION_TYPE_USED_FROM_PARENT, null, CAS.TYPE_NAME_ANNOTATION);
    tsd.addType(ANNOTATION_TYPE_USED_FROM_RANGE, null, CAS.TYPE_NAME_ANNOTATION);
    tsd.addType(ANNOTATION_TYPE_USED_FROM_COMPONENT, null, CAS.TYPE_NAME_ANNOTATION);
    tsd.addType(UNUSED_ANNOTATION_TYPE, null, CAS.TYPE_NAME_ANNOTATION);

    TypeDescription td = tsd.addType(USED_ANNOTATION_TYPE, null, ANNOTATION_TYPE_USED_FROM_PARENT);
    td.addFeature("feat1", null, ANNOTATION_TYPE_USED_FROM_RANGE);
    td.addFeature("feat2", null, CAS.TYPE_NAME_FS_ARRAY, ANNOTATION_TYPE_USED_FROM_COMPONENT,
            false);
    return tsd;
  }

  private FeatureStructure createFeatureStructure(String usedAnnotationType, CAS cas) {
    FeatureStructure fs = cas.createAnnotation(cas.getTypeSystem().getType(usedAnnotationType), 0,
            0);
    cas.addFsToIndexes(fs);
    return fs;
  }
}
