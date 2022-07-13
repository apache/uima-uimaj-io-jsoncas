# JSON serialization of the Apache UIMA CAS

The JSON CAS I/O implementation for use with the UIMA Java SDK allows to serialize UIMA CAS data to
JSON and to de-serialize the data back from JSON again, loading it into a CAS object. The aim of
this library is to facilitate the data interoperability of UIMA data across different platform and 
programming languages. For example, the implementation contains functionality to deal with the 
different character offset counting strategies used by different languages such as Java and Python.

## Example

Here is a small example of a JSON CAS document from the unit test data. It is not meant to
be a meaningful example, just one that shows some of the elements of the JSON format and how they
are encoded. Please refer to the [specification](SPECIFICATION.adoc) for an in-depth description.

```
{
  "%TYPES" : { },
  "%FEATURE_STRUCTURES" : [ {
    "%ID" : 1,
    "%TYPE" : "uima.cas.Sofa",
    "sofaNum" : 1,
    "sofaID" : "_InitialView",
    "mimeType" : "text",
    "sofaString" : "This is a test"
  }, {
    "%ID" : 2,
    "%TYPE" : "uima.tcas.Annotation",
    "@sofa" : 1,
    "begin" : 0,
    "end" : 4
  }, {
    "%ID" : 3,
    "%TYPE" : "uima.tcas.Annotation",
    "@sofa" : 1,
    "begin" : 5,
    "end" : 7
  }, {
    "%ID" : 4,
    "%TYPE" : "uima.tcas.Annotation",
    "@sofa" : 1,
    "begin" : 8,
    "end" : 9
  }, {
    "%ID" : 5,
    "%TYPE" : "uima.tcas.Annotation",
    "@sofa" : 1,
    "begin" : 10,
    "end" : 14
  }, {
    "%ID" : 6,
    "%TYPE" : "uima.tcas.DocumentAnnotation",
    "@sofa" : 1,
    "begin" : 0,
    "end" : 14,
    "language" : "x-unspecified"
  } ],
  "%VIEWS" : {
    "_InitialView" : {
      "%SOFA" : 1,
      "%MEMBERS" : [ 2, 3, 4, 5, 6 ]
    }
  }
}
```

## Reading and writing JSON CAS

**Serializing a CAS to JSON**

```
import org.apache.uima.json.jsoncas2.JsonCas2Serializer

CAS cas = ...;
new JsonCas2Serializer().serialize(cas, new File("cas.json"));
```

**De-serializing a CAS from JSON**

```
import org.apache.uima.json.jsoncas2.JsonCas2Deserializer;

CAS cas = ...; // The CAS must already be prepared with the type system used by the CAS JSON file
new JsonCas2Deserializer().deserialize(new File("cas.json"), cas);
```

## Format specification

For the format specification, please refer [here](SPECIFICATION.adoc).

## Other UIMA JSON CAS implementations

A Python-based implementation of the UIMA JSON CAS format is available as part of the third-party
[DKPro Cassis](https://github.com/dkpro/dkpro-cassis) library.
