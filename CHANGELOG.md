<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# JsonBox Changelog

## [Unreleased]

## [0.0.8] - 2026-06-13

### Added

- Added JsonBox to Toolbar for Quick Access
- Added clipboard auto-detect: editor pre-fills with valid JSON from clipboard when opening in Add mode
- Added full-text search in Quick List: search now matches inside JSON content, not just snippet titles

### Fixed

- Fixed Search on JsonBox not working
- Migrated from deprecated dependencies
- Fixed DocumentListener memory leak by using a real Disposable disposed on dialog close

[Unreleased]: https://github.com/iammohdzaki/JsonBox/compare/v0.0.8...HEAD
[0.0.8]: https://github.com/iammohdzaki/JsonBox/commits/v0.0.8
