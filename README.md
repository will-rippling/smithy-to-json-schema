# Smithy to JSON Schema

This is a tool to convert Smithy models to JSON Schema.

## Prerequisites

- Gradle

## Usage

### One-time setup
```console
gradle wrapper
```

```
./gradlew run --args="-i src/test/resources/com/rippling/smithy/model.json -o $PWD/out"
```
