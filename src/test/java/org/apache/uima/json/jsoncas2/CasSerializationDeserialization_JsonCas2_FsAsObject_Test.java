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
package org.apache.uima.json.jsoncas2;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.uima.cas.serdes.SerDesCasIOTestUtils.createCasMaybeWithTypesystem;
import static org.apache.uima.cas.serdes.SerDesCasIOTestUtils.CasLoadOptions.PRESERVE_ORIGINAL_TSI;
import static org.apache.uima.cas.serdes.TestType.ONE_WAY;
import static org.apache.uima.cas.serdes.TestType.ROUND_TRIP;
import static org.apache.uima.cas.serdes.TestType.SER_DES;
import static org.apache.uima.cas.serdes.TestType.SER_REF;
import static org.apache.uima.json.jsoncas2.Fixtures.readCasManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.serdes.SerDesCasIOTestUtils;
import org.apache.uima.cas.serdes.SerDesCasIOTestUtils.CasLoadOptions;
import org.apache.uima.cas.serdes.datasuites.MultiFeatureRandomCasDataSuite;
import org.apache.uima.cas.serdes.datasuites.MultiTypeRandomCasDataSuite;
import org.apache.uima.cas.serdes.datasuites.ProgrammaticallyCreatedCasDataSuite;
import org.apache.uima.cas.serdes.datasuites.XmiFileDataSuite;
import org.apache.uima.cas.serdes.scenario.DesSerTestScenario;
import org.apache.uima.cas.serdes.scenario.SerDesTestScenario;
import org.apache.uima.cas.serdes.scenario.SerRefTestScenario;
import org.apache.uima.cas.serdes.transitions.CasDesSerCycleConfiguration;
import org.apache.uima.cas.serdes.transitions.CasSerDesCycleConfiguration;
import org.apache.uima.json.jsoncas2.mode.FeatureStructuresMode;
import org.apache.uima.json.jsoncas2.mode.OffsetConversionMode;
import org.apache.uima.json.jsoncas2.mode.SofaMode;
import org.apache.uima.util.CasIOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CasSerializationDeserialization_JsonCas2_FsAsObject_Test {

  private static final String CAS_FILE_NAME = "data.json";

  private static final int RANDOM_CAS_ITERATIONS = 20;

  private static final List<CasSerDesCycleConfiguration> serDesCycles = asList( //
          new CasSerDesCycleConfiguration("DEFAULT (default offsets)", //
                  (a, b) -> serdes(a, b, null, PRESERVE_ORIGINAL_TSI)),
          new CasSerDesCycleConfiguration("DEFAULT (UTF-16 offsets)", //
                  (a, b) -> serdes(a, b, OffsetConversionMode.UTF_16, PRESERVE_ORIGINAL_TSI)),
          new CasSerDesCycleConfiguration("DEFAULT (UTF-8 offsets)", //
                  (a, b) -> serdes(a, b, OffsetConversionMode.UTF_8, PRESERVE_ORIGINAL_TSI)),
          new CasSerDesCycleConfiguration("DEFAULT (UTF-32 offsets)", //
                  (a, b) -> serdes(a, b, OffsetConversionMode.UTF_32, PRESERVE_ORIGINAL_TSI)));
  // new CasSerDesCycleConfiguration(FORMAT + " / LENIENT", //
  // (a, b) -> serdes(a, b, FORMAT, LENIENT)));

  private static final List<CasDesSerCycleConfiguration> desSerCycles = asList( //
          new CasDesSerCycleConfiguration("DEFAULT", //
                  (a, b) -> desser(createCasMaybeWithTypesystem(a), a, b)));

  private static void ser(CAS aSourceCas, Path aTargetCasFile) throws IOException {
    JsonCas2Serializer serializer = new JsonCas2Serializer();
    serializer.setFsMode(FeatureStructuresMode.AS_OBJECT);
    serializer.setSofaMode(SofaMode.AS_REGULAR_FEATURE_STRUCTURE);
    serializer.serialize(aSourceCas, aTargetCasFile.toFile());
  }

  private static void des(CAS aTargetCas, Path aSourceCasFile) throws IOException {
    JsonCas2Deserializer deserializer = new JsonCas2Deserializer();
    deserializer.setFsMode(FeatureStructuresMode.AS_OBJECT);
    deserializer.deserialize(aSourceCasFile.toFile(), aTargetCas);
  }

  public static void serdes(CAS aSourceCas, CAS aTargetCas, OffsetConversionMode aOcm,
          CasLoadOptions... aOptions) throws IOException {
    byte[] buffer;
    byte[] tsiBuffer;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
            ByteArrayOutputStream tsiTarget = new ByteArrayOutputStream()) {
      JsonCas2Serializer serializer = new JsonCas2Serializer();
      serializer.setFsMode(FeatureStructuresMode.AS_OBJECT);
      serializer.setSofaMode(SofaMode.AS_REGULAR_FEATURE_STRUCTURE);
      serializer.setOffsetConversionMode(aOcm);
      serializer.serialize(aSourceCas, os);
      buffer = os.toByteArray();
      // CasIOUtils.save only saves TSI data to the TSI stream if it is not already included in the
      // CAS stream (type system embedded). Thus, to ensure we always get the TSI info, we serialize
      // it separately.
      CasIOUtils.writeTypeSystem(aSourceCas, tsiTarget, true);
      tsiBuffer = tsiTarget.toByteArray();
    }

    Path targetFile = SER_DES.getTargetFolder(lookup().lookupClass()).resolve(CAS_FILE_NAME);
    Files.createDirectories(targetFile.getParent());
    try (OutputStream os = Files.newOutputStream(targetFile)) {
      os.write(buffer);
    }

    try (InputStream is = new ByteArrayInputStream(buffer);
            ByteArrayInputStream tsiSource = new ByteArrayInputStream(tsiBuffer)) {
      if (asList(aOptions).contains(CasLoadOptions.PRESERVE_ORIGINAL_TSI)) {
        ((CASImpl) aTargetCas).getBinaryCasSerDes()
                .setupCasFromCasMgrSerializer(readCasManager(tsiSource));
      }

      JsonCas2Deserializer deserializer = new JsonCas2Deserializer();
      deserializer.setFsMode(FeatureStructuresMode.AS_OBJECT);
      deserializer.deserialize(is, aTargetCas);
    }
  }

  public static void desser(CAS aBufferCas, Path aSourceCasPath, Path aTargetCasPath)
          throws Exception {
    des(aBufferCas, aSourceCasPath);
    ser(aBufferCas, aTargetCasPath);
  }

  private static List<SerRefTestScenario> serRefScenarios() {
    Class<?> caller = CasSerializationDeserialization_JsonCas2_FsAsObject_Test.class;
    return ProgrammaticallyCreatedCasDataSuite.builder().build().stream()
            .map(conf -> SerRefTestScenario.builder(caller, conf, SER_REF, CAS_FILE_NAME)
                    .withSerializer((cas, path) -> ser(cas, path)).build())
            .collect(toList());
  }

  private static List<SerRefTestScenario> oneWayDesSerScenarios() throws Exception {
    Class<?> caller = CasSerializationDeserialization_JsonCas2_FsAsObject_Test.class;
    return XmiFileDataSuite.configurations(Fixtures.materializeTestSuite()).stream()
            .map(conf -> SerRefTestScenario.builder(caller, conf, ONE_WAY, CAS_FILE_NAME)
                    .withSerializer((cas, path) -> ser(cas, path)).build())
            .collect(toList());
  }

  private static List<DesSerTestScenario> roundTripDesSerScenarios() throws Exception {
    Class<?> caller = CasSerializationDeserialization_JsonCas2_FsAsObject_Test.class;
    return desSerCycles.stream().flatMap(cycle -> {
      List<DesSerTestScenario> confs = new ArrayList<>();

      try (Stream<DesSerTestScenario.Builder> builders = DesSerTestScenario.builderCases(caller,
              cycle, ROUND_TRIP, CAS_FILE_NAME)) {

        builders.map(builder -> builder.withCycle(cycle::performCycle) //
                .withAssertion((targetCasFile, referenceCasFile) -> {
                  assertThat(contentOf(targetCasFile.toFile()))
                          .isEqualToIgnoringNewLines(contentOf(referenceCasFile.toFile()));
                }) //
                .build()) //
                .forEach(confs::add);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return confs.stream();
    }).collect(Collectors.toList());

    // FIXME: We cannot use roundTripDesSerScenariosComparingFileContents because it does a binary
    // comparison and JSON is a text format which can have different line endings on Windows/Unix.
    // We would need a line-ending normalizing comparison which is currently (3.3.0) not provided by
    // the UIMA Java SDK.
    // return SerDesCasIOTestUtils.roundTripDesSerScenariosComparingFileContents(desSerCycles,
    // CAS_FILE_NAME);
  }

  private static List<SerDesTestScenario> serDesScenarios() {
    return SerDesCasIOTestUtils.programmaticSerDesScenarios(serDesCycles);
  }

  private static List<SerDesTestScenario> randomSerDesScenarios() {
    return SerDesCasIOTestUtils.serDesScenarios(serDesCycles,
            MultiFeatureRandomCasDataSuite.builder().withIterations(RANDOM_CAS_ITERATIONS).build(),
            MultiTypeRandomCasDataSuite.builder().withIterations(RANDOM_CAS_ITERATIONS).build());
  }

  @ParameterizedTest
  @MethodSource("serRefScenarios")
  public void serializeAndCompareToReferenceTest(Runnable aScenario) throws Exception {
    aScenario.run();
  }

  @ParameterizedTest
  @MethodSource("oneWayDesSerScenarios")
  public void oneWayDeserializeSerializeTest(Runnable aScenario) throws Exception {
    aScenario.run();
  }

  @ParameterizedTest
  @MethodSource("serDesScenarios")
  public void serializeDeserializeTest(Runnable aScenario) throws Exception {
    aScenario.run();
  }

  @ParameterizedTest
  @MethodSource("randomSerDesScenarios")
  public void randomizedSerializeDeserializeTest(Runnable aScenario) throws Exception {
    aScenario.run();
  }

  @ParameterizedTest
  @MethodSource("roundTripDesSerScenarios")
  public void roundTripDeserializeSerializeTest(Runnable aScenario) throws Exception {
    aScenario.run();
  }
}
