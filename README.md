# SDV parser
Java parser for parsing SDV format (Semicolon Delimited Values).
This format seems to be popular among European electricity system participants (e.g. NordPool price data).

[![Build Status](https://travis-ci.org/nemecec/sdv-parser.svg?branch=master)](https://travis-ci.org/nemecec/sdv-parser)

# Requirements

* Java 8 or higher
* Gradle for building (includes Gradle wrapper)

Has no runtime (transitive) dependencies.

# Add to your project

Maven dependency:
```xml
<dependency>
    <groupId>net.sympower</groupId>
    <artifactId>sdv-parser</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Gradle dependency:
```gradle
compile("net.sympower:sdv-parser:1.0.0-SNAPSHOT")
```

# Basic usage

The parser maps SDV rows to Java classes. The classes have to be annotated with `@SdvRow` annotation.
The class also has to have a constructor with the same number of arguments as there are data columns
(for specific row type).

The fields in rows (columns) can have the following types:

 * basic Java types (primitive or wrapper types)
 * `String` (duh!)
 * `BigInteger`
 * `BigDecimal`
 * `java.time` date/time types (`LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`)
 * any custom type which has a type converter registered via `registerConverter()` method

Date and time fields can have a custom format specified on the field by annotating it with `@SdvColumnFormat` annotation.
Decimal fields can have `@SdvDecimalFormat` annotation.

A default decimal format can be configured via `setDefaultDecimalFormat()` method (JVM default is used by default).

A default locale can be configured via `setDefaultLocale()` method (JVM default is used by default).

A character set for reading the text can be configured via `setCharset()` method (by default, UTF-8 is used).

## Basic example (single row type)

SDV file we want to parse:
```
AL;1
AL;2
```

Declare a class to represent the rows:
```java
@SdvRow("AL")
public class LineCountRow {

  public final int count;

  public LineCountRow(int count) {
    this.count = count;
  }

}
```

Parse the file:
```java
SdvReader reader = new SdvReader();
// Make the parser aware of the row type
reader.registerRowType(LineCountRow.class);
// Parse the text file and return list of LineCountRow instances
List<LineCountRow> results = reader.parse(pathToSdvFile, LineCountRow.class);
```

# Usage with multiple row types

SDV format supports mixed row types - same file can contain more than one type of rows.
In order to support this common scenario in a convenient manner, the parser can make use of user
defined "documents" which are used to automatically register relevant row types and to collect parsed rows.

The document class does not need to be annotated in any way. It should have a no-args constructor.

The rules that the parser follows when collecting rows to a document:

* if there is a field with collection type, that is used (if the field has no initial value, it is initialized with `ArrayList`)
* if there is a `set*()` or `add*()` method with a single parameter, that is used
* if there is a field with non-collection type, that is used

In case there are multiple rows of same type but the document can contain only single value of that type
(e.g. a field or a setter method), the last row in the file wins.

## Example (multiple row types)

SDV file we want to parse:
```
BE;SP1;SYSTEMPRICE
BE;EE;Estonia
BE;FI;Finland
AL;4
```

Declare classes to represent the rows:
```java
@SdvRow("BE")
public class AreaDescriptionRow {

  public final String alias;
  public final String description;

  public AreaDescriptionRow(String alias, String description) {
    this.alias = alias;
    this.description = description;
  }

}

@SdvRow("AL")
public class LineCountRow {

  public final int count;

  public LineCountRow(int count) {
    this.count = count;
  }

}
```

Declare a class to represent the whole file (a document):
```java
import java.util.ArrayList;
import java.util.List;

public class SpotPriceDocument {

  List<AreaDescriptionRow> areas = new ArrayList<>();
  LineCountRow lineCount;

  public void setLineCount(LineCountRow lineCount) {
    this.lineCount = lineCount;
  }

  public void addArea(AreaDescriptionRow areaRow) {
    this.areas.add(areaRow);
  }

}
```

Parse the file into a document:
```java
SdvReader reader = new SdvReader();
// Parse the text file and return a document
SpotPriceDocument result = reader.parseDocument(pathToSdvFile, SpotPriceDocument.class);
```

# Building

It uses Gradle (wrapper) for building.

Build JAR:
```bash
gradlew jar
```

Install in local Maven repository:
```bash
gradlew install
```
