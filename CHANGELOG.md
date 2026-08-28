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
- Added JsonBox to Toolbar for Quick Access
- Added clipboard auto-detect: editor pre-fills with valid JSON from clipboard when opening in Add mode
- Added full-text search in Quick List: search now matches inside JSON content, not just snippet titles

### Fixed

- Fixed Search on JsonBox not working
- Migrated from deprecated dependencies
- Fixed DocumentListener memory leak by using a real Disposable disposed on dialog close