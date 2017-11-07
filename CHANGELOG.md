# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## Unreleased

## [2.0.0] - 2017-11-07

### Added

* Subcommands
  * mkdir
  * rename
  * wait
* Status details to jobs list
* --long format for files list (#16)
* --verbose and --stacktrace arguments
* In `xenon --help`, added type column to list of adaptors

### Changed

* Upgraded to Xenon 2.2.0
* Renamed `--format cwljson` argument to `--json`
* Xenon CLI now has same major version as Xenon

## [1.0.3] - 2017-07-20

### Fixed

* Filter scheme properties based on XenonPropertyDescription.Component.SCHEDULER or XenonPropertyDescription.Component.FILESYSTEM (#12)

## [1.0.2] - 2017-05-08

### Changed

* Upgraded to Xenon 1.2.2

## Fixed

* Weird behavior with sftp (#10)

## [1.0.1] - 2017-03-02

### Changed

* Upgraded to Xenon 1.2.1

## [1.0.0] - 2017-02-23

Initial release
