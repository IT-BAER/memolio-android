# Changelog

All notable changes to Memolio are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

_Nothing yet._

## [1.0.2] - 2026-10-21

Changes from closed-test feedback.

### Added

- About: "Rate on Google Play", "Share Memolio" and "Send feedback". Rating opens
  the Play listing, sharing opens the share sheet with the Play link, and feedback
  opens a mail to the developer. Nothing is tracked.
- Back on the frame asks "Close Memolio?" before it closes the app, so one stray
  press no longer stops the slideshow.
- Transition speed (fast, normal, slow) in Playlist > Display, for every
  transition except Cut.

### Changed

- Longer Play Store description with the main features, translated into all ten
  languages. Each language had the English text before.

## [1.0.1] - 2026-08-26

Four rounds of end-to-end testing on a Samsung Galaxy Tab A7 Lite (Android 14).

### Added

- Move selected photos into an album (Pro), with an album picker, an
  empty-album hint, and photo counts on each album.

### Fixed

- Screen readers no longer read a raw icon codepoint on every icon. Photo tiles
  now announce their favorite and selected state, and wallpaper swatches
  announce their selection and Pro state.
- The pairing QR code follows a new DHCP lease or a Wi-Fi reconnect. It used to
  keep pointing at a dead address until the app restarted.
- "Add photos" now tells a lost Wi-Fi connection apart from a server that is
  still starting.
- Storage and About screen corrections found on the device.
- German copy no longer carries an English possessive apostrophe.

### Changed

- The bundled ML Kit usage-logging backend is removed from the manifest, so it
  cannot queue or send an event. Face detection is unaffected. The privacy
  policy documents the library and how the logging is disarmed.

## [1.0.0] - 2026-08-23

First stable release.

### Changed

- In-app purchases now use Google Play Billing Library 8.3.0, through
  RevenueCat 10.18.1. Play requires Billing Library 8 for new releases from
  31 August 2026.

## [0.3.0] - 2026-07-19

### Added

- Face-aware smart crop: in "Fill & crop" photo-fit mode the crop now centers on
  detected faces automatically (on-device, at import; existing photos are
  backfilled in the background).
- Custom wallpapers (Pro): pick any photo as the frame's background.
- Redesigned onboarding: five full-bleed steps on the ambient wallpaper with
  animated transitions and progress dots.

### Fixed

- Autostart on boot now works on Android 14+: the frame relaunches after a
  reboot (grant the "display over other apps" permission when prompted) and
  shows over the lock screen while autostart is on.
- Ambient dimming no longer flickers — brightness eases smoothly as the room
  light changes.
- Gesture navigation works in the Manage screens: system bars are shown there
  and hidden only on the frame itself (kiosk mode stays fully immersive).
- True edge-to-edge on phones with a camera cutout — no more grey strip beside
  the slideshow in landscape.
- Back from the Manage menu returns to the frame instead of leaving the app.
- Manage sections scroll fully clear of the floating play button; the About
  card is no longer covered or cut off.
- QR code is centered on the Add Photos screen in portrait.

## [0.2.0] - 2026-06-23

### Added

- Slideshow gestures: tap to pause and reveal the controls, swipe left or right
  for the next or previous photo, and a favorite (heart) button on the revealed
  controls. Long-press still opens the Manage screen.
- Transition style picker (Manage › Playlist › Display): choose how photos change
  — Ken Burns, Crossfade, Slide, or Cut. The Slide direction follows the swipe
  (swiping back slides in from the left).

## [0.1.0] - 2026-06-21

Initial pre-release. Not yet published to Google Play.

### Added

- Fullscreen photo frame: slideshow with crossfade and Ken Burns motion, idle
  home with live clock and date, optional captions, shuffle, and slideshow
  interval control.
- Clock styles: analog (default) and digital, with adjustable opacity and size.
- Photo fit modes: blurred fill, fill & crop, and fit bars.
- Face-aware smart crop: in fill & crop mode the crop biases toward a detected
  face instead of always center-cropping. Detection is on-device (bundled ML Kit
  model, no network), runs once per photo at import plus a one-time backfill for
  existing photos, and falls back to center when no face is found.
- Local Wi-Fi upload: a browser upload page served from the tablet, reachable by
  scanning a QR code shown on the frame.
- Upload security: every upload request requires a generated token that can be
  rotated in the app as a kill switch.
- Photo import: JPEG, PNG, HEIC, HEIF, and WebP, with SHA-256 deduplication and
  generated display caches and thumbnails.
- Private on-device storage: the app manages files in its own scoped storage and
  never requests broad media-library permissions.
- Library: an "All photos" pool, albums, full-screen photo preview, and
  long-press multi-select with bulk favorite, hide/show, and delete.
- Recently Deleted area with optional 30-day automatic cleanup.
- Built-in wallpapers and custom wallpaper import (Pro).
- Albums and album-based playlists (Pro).
- Appliance suite (Pro): auto-start on boot, kiosk lock, Home-app mode,
  scheduled sleep, and ambient dimming.
- Portrait-first frame with landscape support, and an adaptive list-detail
  Manage screen that collapses to a single pane in portrait.
- Localization of app and upload-page text into English, German, French,
  Italian, Dutch, Polish, Portuguese, Spanish, Russian, and Simplified Chinese.
- Pro unlock as a one-time purchase via RevenueCat over Google Play Billing; the
  entitlement is cached locally so the frame keeps running offline.
- Privacy policy, terms of use, and legal notice, surfaced in the About screen.
- Open-sourced under GPL-3.0.

### Fixed

- Library multi-select: the bulk action now toggles between Hide and Show based
  on the selection, instead of only hiding (which did nothing on already-hidden
  photos and offered no way to un-hide).

### Security

- No analytics, ads, crash-reporting SDKs, or cloud photo storage. The only
  outbound network calls are Pro purchase/restore at the time the user buys or
  restores; uploaded photos travel directly from the uploader's browser to the
  tablet over the local network.

[Unreleased]: https://github.com/IT-BAER/memolio-android/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/IT-BAER/memolio-android/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/IT-BAER/memolio-android/releases/tag/v0.1.0
