#!/bin/bash
targets="${@:-clean test package}"
./gradlew -DteamcityVersion=SNAPSHOT ${targets}
