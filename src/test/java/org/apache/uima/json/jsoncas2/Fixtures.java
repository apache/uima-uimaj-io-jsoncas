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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.impl.CASMgrSerializer;

public class Fixtures
{
    public static CASMgrSerializer readCasManager(InputStream tsiInputStream) throws IOException
    {
        try {
            if (null == tsiInputStream) {
                return null;
            }
            ObjectInputStream is = new ObjectInputStream(tsiInputStream);
            return (CASMgrSerializer) is.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    public static Path materializeTestSuite()
    {
        URL readmeUrl = Fixtures.class.getClassLoader().getResource("XmiFileDataSuite/README.md");
        if ("file".equals(readmeUrl.getProtocol())) {
            try {
                return new File(readmeUrl.toURI()).getParentFile().toPath();
            }
            catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        if ("jar".equals(readmeUrl.getProtocol())) {
            Path targetBase = Paths.get("target/test-data");
            Path dataBase = targetBase.resolve("XmiFileDataSuite");
            String jarFilePath = readmeUrl.getFile();
            jarFilePath = jarFilePath.substring(0, jarFilePath.indexOf("!"));

            if (!jarFilePath.startsWith("file:")) {
                throw new IllegalStateException(
                        "Expected that test data JAR is a file, but it seems not to be: ["
                                + jarFilePath + "]");
            }

            jarFilePath = jarFilePath.substring(5);

            try {
                if (Files.exists(dataBase)) {
                  FileUtils.forceDelete(dataBase.toFile());
                }

                try (ZipFile zipFile = new ZipFile(jarFilePath)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();

                        String name = entry.getName();
                        if (!name.startsWith("XmiFileDataSuite") || name.endsWith("/")) {
                            continue;
                        }

                        Path target = targetBase.resolve(name);

                      if (!target.normalize().startsWith(targetBase.normalize())) {
                        throw new RuntimeException("Bad zip entry");
                      }
                        Files.createDirectories(target.getParent());

                        try (InputStream eis = zipFile.getInputStream(entry);
                                OutputStream os = Files.newOutputStream(target)) {
                            IOUtils.copyLarge(eis, os);
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            return dataBase;
        }

        throw new IllegalStateException("Unable to materialize test data");
    }

}
