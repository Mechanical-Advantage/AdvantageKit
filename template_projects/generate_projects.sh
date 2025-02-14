#!/bin/bash

# Constants
WPILIB_VERSION="2025.3.1"
PROJECTS=(kitbot_2025 diff_drive spark_swerve talonfx_swerve vision skeleton)
VENDORDEPS=(
    "Studica PathplannerLib Phoenix5 Phoenix6 REVLib URCL WPILibNewCommands" # kitbot_2025
    "Studica PathplannerLib Phoenix5 Phoenix6 REVLib URCL WPILibNewCommands" # diff_drive
    "Studica PathplannerLib Phoenix6 REVLib URCL WPILibNewCommands" # spark_swerve
    "Studica PathplannerLib Phoenix6 WPILibNewCommands" # talonfx_swerve
    "photonlib WPILibNewCommands" # vision
    "WPILibNewCommands" # skeleton
)

# Clear old projects
rm -r generated
mkdir generated

# Iterate over projects
I=-1
for project in "${PROJECTS[@]}"; do
    I=$(expr $I + 1)
    declare -a vendordeps=(${VENDORDEPS[I]})

    # Add template
    cp -r template generated/$project
    sed -i '' -e "s/WPILIB_VERSION/$WPILIB_VERSION/g" generated/$project/build.gradle

    # Add sources
    rsync -r sources/$project/ generated/$project/

    # Add AdvantageKit vendordep
    mkdir generated/$project/vendordeps
    cp ../akit/build/vendordep/AdvantageKit.json generated/$project/vendordeps/AdvantageKit.json

    # Copy additional vendordeps
    for name in "${vendordeps[@]}"; do
        cp vendordeps/$name.json generated/$project/vendordeps/$name.json
    done

    # Create zip
    cd generated/$project
    zip -r ../$project.zip . > /dev/null
    cd ../..

    # Print message
    echo "Generated $project"
done