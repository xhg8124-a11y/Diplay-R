# DiPlay next release notes

Changes through DiPlay 0.2.22 are documented in [0.2.22 release notes](RELEASE-NOTES-0.2.22.md). Measured checks are in [VALIDATION.md](VALIDATION.md). Device acceptance and remaining failure families are tracked in [connection reliability validation](CONNECTION_RELIABILITY.md).

No unreleased changes are recorded yet. Add future unreleased changes here.

## Unreleased

- The CarPlay car button label defaults to 飞凡 under the Feifan preset (upstream default
  is BYD); a saved label always wins.
- Under the Feifan preset the CarPlay app-list car button now shows the Rising Auto R
  mark instead of the stock car glyph (a custom image still wins; changes apply at the
  next connection).
- Under the Feifan preset the audio-focus toggles now default on (Advanced: audio focus,
  navigation prompts request focus; auto-yield was already on). Field-tested on the F7:
  the volume knob follows the focus holder, so a speaking prompt gets the knob and calls
  duck the music. Saved choices win and other head units stay opt-in.
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
- Update check through a mirror now falls back to direct GitHub when the mirror's shared
  GitHub API quota is exhausted (intermittent 403 "API rate limit exceeded" observed on
  gh-proxy.com). Downloads keep the chosen mirror; its object proxy path is unaffected.
- A VPN consent dialog that answers "denied" on its own (measured twice on the Feifan F7's
  RISING OS, ~2 s without a tap) now also runs the ADB allowance and shows the
  `appops set … ACTIVATE_VPN allow` command on the CarPlay screen, instead of stopping at
  "VPN consent was denied".
- Wi-Fi scan pause now also covers manual-hotspot sessions while the Feifan preset is on.
  The F7's RISING OS runs its hotspot beside a disconnected, still-scanning Wi-Fi client on the
  same radio; field audio shows ~10 s-rhythm link stalls up to 1.9 s with packet loss on the
  5 GHz hotspot. The existing BYD Wi-Fi Direct scan pause runs for those hotspot sessions too,
  through the already-approved local ADB.
- The Feifan wireless music buffer default is 500 ms (0.2.28 had raised it to 1000 ms).
  A 1000 ms start level reports "received 750 ms, played 0" on the sender's first
  /feedback (~0.35 s in), and the sender then restarts the audio stream every few
  seconds - heard as constant play/pause cycling (measured: 24 stream rebuilds in two
  minutes). 500 ms held the car's 250–460 ms radio gaps in the same field reports.
- While the Feifan preset is on, an unchosen wireless music buffer now starts at 1000 ms
  instead of 300 ms: the F7's car hotspot shows 250–460 ms radio gaps in field reports, and
  the 500 ms buffer that held there sits too close to the peaks. A saved driver choice always
  wins.
