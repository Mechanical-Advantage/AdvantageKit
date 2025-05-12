#!/bin/bash

# Copyright (c) 2021-2025 Littleton Robotics
# http://github.com/Mechanical-Advantage
#
# Use of this source code is governed by a BSD
# license that can be found in the LICENSE file
# at the root directory of this project.

rm -rf akit/src/main/generated

flatc --gen-compare --no-includes --gen-mutable --reflect-names --cpp-ptr-type flatbuffers::unique_ptr \
      -o akit/src/main/generated/include --cpp akit/src/main/fbs/conduit_schema.fbs

flatc --gen-compare --no-includes --gen-mutable --reflect-names \
      -o akit/src/main/generated/java --java akit/src/main/fbs/conduit_schema.fbs