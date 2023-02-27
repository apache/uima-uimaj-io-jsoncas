<!--
***************************************************************
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
***************************************************************
-->
   
# Apache UIMA Java SDK JSON CAS I/O (TM) v0.5.0

The JSON CAS I/O implementation for use with the UIMA Java SDK allows to serialize UIMA CAS data to
JSON and to de-serialize the data back from JSON again, loading it into a CAS object. The aim of
this library is to facilitate the data interoperability of UIMA data across different platform and 
programming languages. For example, the implementation contains functionality to deal with the 
different character offset counting strategies used by different languages such as Java and Python.
A Python-based implementation of the UIMA JSON CAS format is available as part of the third-party
[DKPro Cassis](https://github.com/dkpro/dkpro-cassis) library.

This is an early public release based on the JSON serialization of the Apache UIMA CAS draft
specification version 0.4.0. Please note that the implementation and the specification are not yet
final. For this reason, is not yet recommended to use this library in scenarios where data needs to
be stored or archived over an extended period of time as future versions of the implementation and
specification may introduce incompatibilities with the current version. Good usage scenarios are
for example short-term data exchange between different UIMA implementations such as for example in
network communication.

## What's Changed
* ⭐️ Issue #13: Implement TypeSystemMode.MINIMAL by @reckart in https://github.com/apache/uima-uimaj-io-jsoncas/pull/15
* 🦟 Issue #18: Fix Zip Slip Vulnerability in test code by @JLLeitschuh in https://github.com/apache/uima-uimaj-io-jsoncas/pull/10
* 🩹 Issue #14: Upgrade dependencies by @reckart in https://github.com/apache/uima-uimaj-io-jsoncas/pull/16, https://github.com/apache/uima-uimaj-io-jsoncas/pull/17

## New Contributors
* @JLLeitschuh made their first contribution in https://github.com/apache/uima-uimaj-io-jsoncas/pull/10

**Full Changelog**: https://github.com/apache/uima-uimaj-io-jsoncas/compare/rel/uimaj-io-json-0.4.0...uimaj-io-json-0.5.0

A [full list of issues](https://github.com/apache/uima-uimaj-io-jsoncas/issues?q=is%3Aissue+milestone%3A0.5.0+is%3Aclosed) addressed in this release can be found on issue tracker.

Please use the [mailing lists](https://uima.apache.org/mail-lists.html) for feedback and the [issue tracker](https://github.com/apache/uima-uimaj-io-jsoncas/issues) to report bugs.

## Supported Platforms

This version of UIMA Java SDK JSON CAS I/O should be used in combination with

- Java 1.8 or higher
- UIMA Java SDK 3.4.1 or higher
