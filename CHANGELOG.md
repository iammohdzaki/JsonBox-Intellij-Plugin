<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# JsonBox Changelog

## [Unreleased]

## [0.0.9]

### Added

- Added multiple tags support: users can now tag JSON snippets for easy grouping
- Added tag-based dropdown filter to the Quick List
- Search bar now actively matches against tag names
- Visual tag pills added to both the Add/Edit dialog and the Quick List
- Tag filter state now persists as long as the IDE session is active
- Added sorting options (A-Z, Z-A, Newest, Oldest) via a convenient dropdown in the Quick List toolbar

## [0.0.8]

### Added

- Added JsonBox to Toolbar for Quick Access
- Added clipboard auto-detect: editor pre-fills with valid JSON from clipboard when opening in Add mode
- Added full-text search in Quick List: search now matches inside JSON content, not just snippet titles

### Fixed

- Fixed Search on JsonBox not working
- Migrated from deprecated dependencies
- Fixed DocumentListener memory leak by using a real Disposable disposed on dialog close

## [0.0.7]

### Added

- Added "Minify JSON" functionality
- Improved JSON formatting logic with efficient pretty-printing
- Refined UI components and JSON management capabilities
- Added CodeRabbit Pull Request Reviews badge

### Fixed

- Improved error handling in JSON operations with user-facing error dialogs and logging
- Refined threading logic to prevent UI blocking
- Ensured proper resource disposal in editors
- Streamlined project structure by removing obsolete JsonBoxPopup classes

## [0.0.6]

### Added

- Added Json Quick List to save and manage JSON snippets
- Added Notification support for important events
- Added Easy Key Bind to access (Ctrl Shift Q)
- Added Unit Test Cases for Validation
- Improved Json Parsing Formatting Performance

## [0.0.5]

### Added

- Added Diff Json Compare feature

## [0.0.4]

### Added

- Major UI overhaul and feature improvements
- Updated preview images in documentation

## [0.0.3]

### Added

- Added DeStringify feature to easily decode stringified JSON
- Minor bug fixes

## [0.0.2]

### Added

- Initial Release
- Added JsonParserDialog, JsonBoxNotificationProvider, and OpenJsonParserDialogAction
- Added Bug Report Template