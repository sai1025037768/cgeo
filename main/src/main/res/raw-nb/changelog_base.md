### General release notes

**Edge to Edge**

Due to Play Store policies we have updated the Android API level this version of c:geo targets + we have changed some of the screen layout routines. This may come with some unwanted side effects, especially on newer Android versions. If you experience any problems with this version of c:geo, please report either on [GitHub](https://github.com/cgeo/cgeo) or via email to [support@cgeo.org](mailto:support@cgeo.org)

**Legacy Maps**

As announced with 2025.07.17 and 2025.12.01 releases, we have finally removed the legacy implementations for our maps. You will be switched to our new UnifiedMap automatically and should notice no differences except a couple of new features, some of which are
- Map rotation for OpenStreetMap based maps (online + offline)
- Cluster popup for Google Maps
- Hide map sources you don't need
- Elevation chart for routes and tracks
- Switch between lists directly from map
- "Driving mode" for OpenStreetMap based maps
- Long-tap on track / individual route for further options

### Kart
- New: Route optimization caches calculated data
- New: Enabling live mode keeps waypoints of currently set target visible
- New: Long-tap on navigation line opens elevation chart (UnifiedMap)
- New: Show generated waypoints on map
- New: Download caches ordered by distance
- Fix: Doubling of individual route items
- New: Support for Motorider theme (VTM only)
- New: NoMap tile provider (don't show map, just caches etc.)
- Change: Max distance to connect points on history track lowered to 500m (configurable)
- New: Allow importing KML files as tracks (eg: trackable itinerary)
- New: Offer to set cache icon even if cache is not yet stored
- New: Infobox for elevation chart showing remaining distance, ascent, descent
- New: Display coordinates of waypoints in waypoint popup
- Fix: Map quick settings may show buttons "1"/"2" for empty routing profiles after switching language
- New: Calculate missing elevation data on importing tracks (if elevation data is downloaded)
- Fix: Tile downloader stopping under certain conditions (OpenStreetMap online maps only)
- New: Conditional cache markers
- New: Show navigation hint (arrow + distance)

### Cachedetaljer
<<<<<<< HEAD
- New: Detect additional characters in formulas: –, ⋅, ×
- New: Preserve timestamp of own logs on refreshing a cache
- New: Optional compass mini view (see settings => cache details => Show direction in cache detail view)
- New: Show owners' logs on "friends/own" tab
- Change: "Friends/own" tab shows log counts for that tab instead of global counters
- Change: Improved header in variable and waypoint tabs
- Fix: Two "delete log" items shown
- Fix: c:geo crashing in cache details when rotating screen
- Change: More compact layout for "adding new waypoint"
- New: Option to load images for geocaching.com caches in "unchanged" size
- New: Variables view can be filtered
- New: Visualize calculated coordinates overflow in waypoint list
- New: Menu entry in waypoint list to mark certain waypoint types as visited
- New: Placeholders for trackable logging (geocache name, geocache code, user)
- Change: Removed the link to outdated WhereYouGo player. Integrated Wherigo player is now default for Wherigos.
- Fix: Missing quick toggle in guided mode of waypoint calculator
- New: Aggregate functions with range support: add/sum, min/minimum, max/maximum, cnt/count, avg/average, multiply/product/prod
- Fix: Incorrect handling of DNF status for opencaching platforms
- New: Delete offline log after merge with online log
- New: Show confirmation when deleting caches with offline logs
- New: Show confirmation when deleting all caches from "All" list
- New: Allow Markdown formatting for listing text in user-defined caches
- Change: Store cache before adding user image
- Fix: Crash on loading images embedded directly in listing text
- New: Show own favorites in log view (Geocaching.com + offline logs)

### Wherigo player
- New: Offline translation for Wherigos
- New: Improved button handling
- New: Status auto-save
- New: Option to create shortcout to Wherigo player on your mobile's home screen

### Generelt
- New: Share option after logging a cache
- Change: Do not show "needs maintenance" or "needs archived" options for own caches
- Fix: Restoring a backup may duplicate track files in internal storage and subsequent backups
- Change: Removed references to Twitter
- New: Delete orphaned trackfiles on clean up and restore backup
- New: Warning on trying to add too many caches to a bookmark list
- New: Watch/unwatch list functions
- New: Offer offline translation with Google Translate or DeepL apps (if installed)
- New: Delete items from search history
- Change: Remove GCVote (service discontinued)
- New: Colored toolbar on cache details pages
- New: Select multiple bookmark lists / pocket queries to download
- New: Preview bookmark lists
- Change: Increase minimum required Android version to Android 8
- New: Default quick buttons for new installations
- Fix: Titles in range input dialogs cut off
- Fix: Notification for nightly update points to regular APK even for FOSS variant
- New: "Ignore year" option for date filters
- New: Make remote URI clickable in pending downloads
- Change: Use system-settings as default theme for new installations
- New: GPX export: Write GSAK Lat/LonBeforeCorrect annotations when exporting original waypoints
- New: Show undo bar when deleting caches from list from map
- Fix: Crahs in percentage favorite filter
- New: Make it easier to use simple lists as parent lists
- Change: Use local timezone (of device, not event) for calendar entries (instead of UTC)
- Fix: Some texts ignore language switching
- Fix: "Use imperial settings" not initialized correctly on fresh installs
- Change: Bergamot open source offline translation module replacing closed-source Google ML Kit translator
- Change: New emoji selector
=======
- New: Offline translation of listing text and logs (experimental)
- New: Option to share cache with user data (coordinates, personal note)
- Fix: Speech service interrupted on screen rotation
- Fix: Cache details: Lists for cache not updated after tapping on list name an removing that cache from that list
- Fix: User note gets lost on refreshing a lab adventure
- Change: Log-date related placeholders will use chosen date instead of current date
- Nytt: Forkort lange loggtekster som standard

### Wherigo-spiller
- New: Integrated Wherigo player checking for missing credentials
- Change: Removed Wherigo bug report (as errors are mostly cartridge-related, need to be fixed by cartridge owner)
- New: Ability to navigate to a zone using compass
- New: Ability to copy zone center coordinates to clipboard
- New: Set zone center as target when opening map (to get routing and distance info for it)
- New: Support opening local Wherigo files
- Change: Long-tap on a zone on map is no longer recognized. This allows users to do other stuff in map zone area available on long-tap, eg: create user-defined cache
- New: Display warning if wherigo.com reports missing EULA (which leads to failing download of cartridge)

### Generelt
- New: Redesigned search page
- New: Inventory count filter
- New: Support for coordinates in DD,DDDDDDD format
- New: Show last used filter name in filter dialog
- New: Coordinate calculator: Function to replace "x" with multiplication symbol
- Fix: Incorrect altitude (not using mean above sea level)
- Fix: Nearby distance limit setting not working properly for small values
- Fix: Sorting of cache lists by distance descending not working correctly
- Fix: Lab caches excluded by D/T filter even with active "include uncertain"
- Fiks: Fargeproblemer med menyikoner i lys modus
- New: Add "Remove past events" to list "all"
- New: Show connector for "user-defined caches" as active in source filter
- New: GPX export: exporting logs / trackables made optional
- New: Added button to delete log templates
- Fix: Importing local map file gets random map name
- Fix: Map downloader offering broken (0 bytes) files for download
- New: Added mappings for some missing OC cache types
- New: Move "recently used" lists in list selection dialog to the top on pressing "recently used" button
- New: Share list of geocodes from cache list
- Change: "Navigation (car)" etc. use "q=" parameter instead of outdated "ll=" parameter
>>>>>>> release
