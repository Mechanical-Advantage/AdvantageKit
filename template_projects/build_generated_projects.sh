#!/bin/bash
for project in generated/*/; do
    echo "Building $project"
    cd $project
    ./gradlew build > /dev/null
    cd ../..
done