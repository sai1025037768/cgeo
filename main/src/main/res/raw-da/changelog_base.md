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

### Kort
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

### Wherigo afspiller
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
- Nyt: Koordinatberegner: Funktion til at erstatte "x" med multiplikationssymbol
- Fix: Incorrect altitude (not using mean above sea level)
- Fix: Nearby distance limit setting not working properly for small values
- Fix: Sorting of cache lists by distance descending not working correctly
- Fix: Lab caches excluded by D/T filter even with active "include uncertain"
- Fix: Color issues with menu icons in light mode
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
