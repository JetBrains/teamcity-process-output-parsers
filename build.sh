#!/bin/bash
targets="${@:-clean test build publish}"
./gradlew -DteamcityVersion=SNAPSHOT ${targets}
