#!/bin/bash

rm -rf akit/src/main/generated

flatc --gen-compare --no-includes --gen-mutable --reflect-names --cpp-ptr-type flatbuffers::unique_ptr \
      -o akit/src/main/generated/include --cpp akit/src/main/fbs/conduit_schema.fbs

flatc --gen-compare --no-includes --gen-mutable --reflect-names \
      -o akit/src/main/generated/java --java akit/src/main/fbs/conduit_schema.fbs