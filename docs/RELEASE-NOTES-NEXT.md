# DiPlay next release notes

Changes through DiPlay 0.2.22 are documented in [0.2.22 release notes](RELEASE-NOTES-0.2.22.md). Measured checks are in [VALIDATION.md](VALIDATION.md). Device acceptance and remaining failure families are tracked in [connection reliability validation](CONNECTION_RELIABILITY.md).

No unreleased changes are recorded yet. Add future unreleased changes here.

## Unreleased

- Added a Feifan F7 preset (Settings → Vehicle, experimental): 325 mm CarPlay physical width for
  the 15.05" centre screen and passenger-screen discovery for the dashboard map, with automatic
  Build-based detection and a manual on/off override. See [Feifan F7 support](FEIFAN_F7.md).
- While a wired wait sees no iPhone, one sysfs probe over the head unit's local ADB now records
  in the diagnostic report whether the kernel lists the Apple device the framework does not —
  separating a port or cable problem from a head-unit framework restriction.
- The in-app updater now tracks this repository's releases instead of upstream DiPlay, so an
  upstream release can no longer replace the Feifan F7 support. First release: v0.2.23-r1
  (versionCode 42), signed with the fork's own key.
- About page offers an update source choice: direct GitHub or the gh-proxy.com mirror for
  mainland-China drivers. Checks and downloads follow the chosen route; the catalog still
  validates the GitHub URLs and the APK keeps its SHA-256 verification.
