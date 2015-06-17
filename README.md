TeamCity Process Output Parsers Plugin
=============

Features
--------

Provides custom process output parsers to be used within builds.

Parsers can be controlled from Java API and using service message commands:
- `RegexMessageParser.Enable` enables parser
- `RegexMessageParser.Disable` disables parser

Both commands can have next artuments:
- `file` - Path to file with parser definition. Either absolute or relative to checkout directory
- `name` - Name of one of predefined parsers (May be provided by another plugins)


Building
--------
- Download and install TeamCity distribution.
- Open project with IntelliJ IDEA.
- Set "TeamCityDistribution" path variable in IDEA Preferences as path to your TeamCity installation directory.
- Build 'plugin-zip' artifact
