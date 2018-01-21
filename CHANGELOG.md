# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Simple connection pooling in GelfTcpAppender & round robin host lookup in GelfUdpAppender
  [\#11](https://github.com/osiegmar/logback-gelf/issues/11)

### Changed
- Update dependency to logback 1.1.8
  [\#1](https://github.com/osiegmar/logback-gelf/issues/1)

### Fixed
- The reconnect interval could not be disabled
  [\#12](https://github.com/osiegmar/logback-gelf/issues/12)

## [1.0.4] - 2017-04-03
### Added
- Support for GELF console logging (appendNewline in GelfLayout)

### Fixed
- Fix interrupted flag in GelfTcpAppender (restore flag after catching InterruptedException)

## [1.0.3] - 2017-01-12
### Added
- Support for logback 1.1.8
  [\#6](https://github.com/osiegmar/logback-gelf/issues/6)

## [1.0.2] - 2016-10-18
### Fixed
- Fix possible infinite loop bug with exception root cause

## [1.0.1] - 2016-10-08
### Added
- Support for forwarding of exception root cause
- Support for TLS encryption (with TCP)

## 1.0.0 - 2016-03-20

- Initial release

[Unreleased]: https://github.com/osiegmar/logback-gelf/compare/v1.0.4...HEAD
[1.0.4]: https://github.com/osiegmar/logback-gelf/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/osiegmar/logback-gelf/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/osiegmar/logback-gelf/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/osiegmar/logback-gelf/compare/v1.0.0...v1.0.1
