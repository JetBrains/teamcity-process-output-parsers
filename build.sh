#!/bin/bash
targets="${@:-clean test build}"
./gradlew -DteamcityVersion=SNAPSHOT ${targets}
