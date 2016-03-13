Logback GELF
============

Logback appender for sending GELF messages with zero additional dependencies.


Features
--------

- UDP (with chunking)
- Deflate compression in UDP mode
- Forwarding of MDC (Mapped Diagnostic Context)
- Forwarding of caller data
- Forwarding of static fields
- No runtime dependencies beside Logback


Requirements
------------

- Java 7
- Logback 1.1.3 (or later)


Example
-------

Simple:

```xml
<configuration>

    <appender name="GELF" class="de.siegmar.logbackgelf.GelfUDPAppender">
        <graylogHost>localhost</graylogHost>
        <graylogPort>12201</graylogPort>
    </appender>

    <root level="debug">
        <appender-ref ref="GELF" />
    </root>

</configuration>
```

Advanced:

```xml
<configuration>

    <appender name="GELF" class="de.siegmar.logbackgelf.GelfUDPAppender">
        <graylogHost>localhost</graylogHost>
        <graylogPort>12201</graylogPort>
        <layout class="de.siegmar.logbackgelf.GelfLayout">
            <originHost>localhost</originHost>
            <includeRawMessage>true</includeRawMessage>
            <includeMdcData>true</includeMdcData>
            <includeCallerData>true</includeCallerData>
            <shortPatternLayout class="ch.qos.logback.classic.PatternLayout">
                <pattern>%m%nopex</pattern>
            </shortPatternLayout>
            <fullPatternLayout class="ch.qos.logback.classic.PatternLayout">
                <pattern>%m</pattern>
            </fullPatternLayout>
            <staticField>app_name:backend</staticField>
            <staticField>os_arch:${os.arch}</staticField>
            <staticField>os_name:${os.name}</staticField>
            <staticField>os_version:${os.version}</staticField>
        </layout>
    </appender>

    <root level="debug">
        <appender-ref ref="GELF" />
    </root>

</configuration>
```

Configuration
-------------

## Appender

`de.siegmar.logbackgelf.GelfUDPAppender`

* **graylogHost**: IP or hostname of graylog server.
* **graylogPort**: Port of graylog server.
* **useCompression**: If true, compression of GELF messages is enabled. Default: true
* **maxChunkSize**: Maximum size of GELF chunks in bytes. Default chunk size is 508 - this prevents
  IP packet fragmentation. This is also the recommended minimum.
  Maximum supported chunk size is 65,467 bytes.
* **layout**: See Layout configuration below.


## Layout

`de.siegmar.logbackgelf.GelfLayout`

* **originHost**: Origin hostname - will be auto detected if not specified.
* **includeRawMessage**: If true, the raw message (with argument placeholders) will be sent, too. 
  Default: false
* **includeMarker**: If true, logback markers will be sent, too. Default: true
* **includeMdcData**: If true, MDC keys/values will be sent, too. Default: true
* **includeCallerData**: If true, caller data (source file-, method-, class name and line) will be 
  sent, too. Default: false
* **includeLevelName**: If true, the log level name (e.g. DEBUG) will be sent, too. Default: false
* **shortPatternLayout**: Short message format. Default: `"%m%nopex"` 
* **fullPatternLayout**: Full message format (Stacktrace). Default: `"%m"`
* **staticFields**: Additional, static fields to send to graylog. Defaults: none


Contribution
------------

- Fork
- Code
- Add test(s)
- Commit
- Send me a pull request


Copyright
---------

Copyright (C) 2016 Oliver Siegmar

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
