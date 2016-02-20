#!/bin/bash
targets="${@:-clean test build publishToMavenLocal}"
./gradlew -DteamcityVersion=SNAPSHOT ${targets}
