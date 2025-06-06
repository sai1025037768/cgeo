package cgeo.geocaching.utils;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.R;
import cgeo.geocaching.connector.gc.GCConstants;
import cgeo.geocaching.enumerations.CacheListInfoItem;
import cgeo.geocaching.enumerations.CacheSize;
import cgeo.geocaching.enumerations.WaypointType;
import cgeo.geocaching.list.AbstractList;
import cgeo.geocaching.log.LogEntry;
import cgeo.geocaching.models.GCList;
import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.models.Waypoint;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.storage.extension.PocketQueryHistory;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.ImageSpan;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public final class Formatter {

    private static final int SHORT_GEOCODE_MAX_LENGTH = 8;

    /**
     * Text separator used for formatting texts
     */
    public static final String SEPARATOR = " · ";
    public static final int MINUTES_PER_DAY = 24 * 60;
    public static final float DAYS_PER_MONTH = 365F / 12; // on average

    private Formatter() {
        // Utility class
    }

    private static Context getContext() {
        return CgeoApplication.getInstance().getBaseContext();
    }

    /**
     * Generate a time string according to system-wide settings (locale, 12/24 hour)
     * such as "13:24".
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatTime(final long date) {
        return DateUtils.formatDateTime(getContext(), date, DateUtils.FORMAT_SHOW_TIME);
    }

    /**
     * Generate a date string according to system-wide settings (locale, date format)
     * such as "20 December" or "20 December 2010". The year will only be included when necessary.
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatDate(final long date) {
        return DateUtils.formatDateTime(getContext(), date, DateUtils.FORMAT_SHOW_DATE);
    }

    /**
     * Generate a date string according to system-wide settings (locale, date format)
     * such as "20 December 2010". The year will always be included, making it suitable
     * to generate long-lived log entries.
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatFullDate(final long date) {
        return DateUtils.formatDateTime(getContext(), date, DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_SHOW_YEAR);
    }

    /**
     * Generate a date string according to system-wide settings (locale, date format)
     * such as "Wednesday".
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatDayOfWeek(final long date) {
        return DateUtils.formatDateTime(getContext(), date, DateUtils.FORMAT_SHOW_WEEKDAY);
    }

    /**
     * Tries to get the date format pattern of the system short date.
     *
     * @return format pattern or empty String if it can't be retrieved
     */
    @NonNull
    public static String getShortDateFormat() {
        final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        if (dateFormat instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) dateFormat).toPattern();
        }
        return StringUtils.EMPTY; // should not happen
    }

    /**
     * Generate a numeric date string with date format "yyyy-MM"
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatDateYYYYMM(final long date) {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date);
    }

    /**
     * Generate a numeric date string with date format "yyyy-MM-dd HH-mm"
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatDateForFilename(final long date) {
        return new SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.getDefault()).format(date);
    }

    /**
     * Generate a numeric date string according to system-wide settings (locale, date format)
     * such as "10/20/2010".
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatShortDate(final long date) {
        final String dateFormatString = Settings.getShortDateFormat();
        if (!dateFormatString.isEmpty()) {
            return new SimpleDateFormat(dateFormatString, Locale.getDefault()).format(date);
        }
        return android.text.format.DateFormat.getDateFormat(getContext()).format(date);
    }

    private static String formatShortDateIncludingWeekday(final long time) {
        return DateUtils.formatDateTime(CgeoApplication.getInstance().getBaseContext(), time, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY) + ", " + formatShortDate(time);
    }

    /**
     * Generate a numeric date string according to system-wide settings (locale, date format)
     * such as "10/20/2010". Today and yesterday will be presented as strings "today" and "yesterday".
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatShortDateVerbally(final long date) {
        final String verbally = formatDateVerbally(date);
        if (verbally != null) {
            return verbally;
        }
        return formatShortDate(date);
    }

    private static String formatDateVerbally(final long date) {
        final int diff = CalendarUtils.daysSince(date);
        switch (diff) {
            case 0:
                return CgeoApplication.getInstance().getString(R.string.log_today);
            case 1:
                return CgeoApplication.getInstance().getString(R.string.log_yesterday);
            default:
                return null;
        }
    }

    /**
     * Generate a numeric date and time string according to system-wide settings (locale,
     * date format) such as "7 sept. at 12:35".
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatShortDateTime(final long date) {
        return DateUtils.formatDateTime(getContext(), date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL);
    }

    /**
     * Generate a numeric date and time string according to system-wide settings (locale,
     * date format) such as "7 september at 12:35".
     *
     * @param date milliseconds since the epoch
     * @return the formatted string
     */
    @NonNull
    public static String formatDateTime(final long date) {
        return DateUtils.formatDateTime(getContext(), date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
    }

    @NonNull
    public static SpannableStringBuilder formatCacheInfoLong(final Geocache cache, final @Nullable List<AbstractList> storedLists, final @Nullable String excludeList) {
        final SpannableStringBuilder sb = new SpannableStringBuilder();

        final ArrayList<SpannableString> infos = new ArrayList<>();
        addConfiguredInfoItems(cache, Settings.getInfoItems(R.string.pref_cacheListInfo, 2), storedLists, excludeList, infos);
        boolean newlineRequested = false;
        for (SpannableString s : infos) {
            if (s.length() > 0) {
                if (StringUtils.equals(s, "\n")) {
                    if (sb.length() > 0) {
                        newlineRequested = true;
                    }
                } else {
                    sb.append(sb.length() > 0 ? newlineRequested ? "\n" : SEPARATOR : "").append(s);
                    newlineRequested = false;
                }
            }
        }
        return sb;
    }

    private static void addConfiguredInfoItems(final Geocache cache, final List<Integer> configuredItems, final @Nullable List<AbstractList> storedLists, final @Nullable String excludeList, final List<SpannableString> infos) {
        for (int item : configuredItems) {
            if (item == CacheListInfoItem.VALUES.GCCODE.id) {
                if (StringUtils.isNotBlank(cache.getGeocode())) {
                    infos.add(new SpannableString(cache.getShortGeocode()));
                }
            } else if (item == CacheListInfoItem.VALUES.DIFFICULTY.id) {
                if (cache.hasDifficulty()) {
                    infos.add(new SpannableString("D " + formatDT(cache.getDifficulty())));
                }
            } else if (item == CacheListInfoItem.VALUES.TERRAIN.id) {
                if (cache.hasTerrain()) {
                    infos.add(new SpannableString("T " + formatDT(cache.getTerrain())));
                }
            } else if (item == CacheListInfoItem.VALUES.MEMBERSTATE.id) {
                if (cache.isPremiumMembersOnly()) {
                    infos.add(new SpannableString(CgeoApplication.getInstance().getString(R.string.cache_premium)));
                }
            } else if (item == CacheListInfoItem.VALUES.SIZE.id) {
                if (cache.getSize() != CacheSize.UNKNOWN && cache.showSize()) {
                    infos.add(new SpannableString(cache.getSize().getL10n()));
                }
            } else if (item == CacheListInfoItem.VALUES.LISTS.id) {
                formatCacheLists(cache, storedLists, excludeList, infos);
            } else if (item == CacheListInfoItem.VALUES.EVENTDATE.id) {
                if (cache.isEventCache()) {
                    final Date hiddenDate = cache.getHiddenDate();
                    if (hiddenDate != null) {
                        infos.add(new SpannableString(formatShortDateIncludingWeekday(hiddenDate.getTime())));
                    }
                }
            } else if (item == CacheListInfoItem.VALUES.HIDDEN_MONTH.id) {
                final Date hiddenDate = cache.getHiddenDate();
                if (hiddenDate != null) {
                    infos.add(new SpannableString(formatDateYYYYMM(hiddenDate.getTime())));
                }
            } else if (item == CacheListInfoItem.VALUES.RECENT_LOGS.id) {
                final List<LogEntry> logs = cache.getLogs();
                if (!logs.isEmpty()) {
                    int count = 0;
                    // mitigation to make displaying ImageSpans work even in wrapping lines, see #14163
                    // ImageSpans are separated by a zero-width space character (\u200b)
                    final SpannableString s = new SpannableString(" \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b");
                    for (int i = 0; i < Math.min(logs.size(), 8); i++) {
                        final ImageSpan is;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            is = new ImageSpan(getContext(), logs.get(i).logType.getLogOverlay(), ImageSpan.ALIGN_CENTER);
                        } else {
                            is = new ImageSpan(getContext(), logs.get(i).logType.getLogOverlay());
                        }
                        s.setSpan(is, i * 2, i * 2 + 1, 0);
                        count++;
                    }
                    infos.add(new SpannableString(s.subSequence(0, 2 * count)));
                }

            // newline items should be last in list
            } else if (item == CacheListInfoItem.VALUES.NEWLINE1.id || item == CacheListInfoItem.VALUES.NEWLINE2.id || item == CacheListInfoItem.VALUES.NEWLINE3.id || item == CacheListInfoItem.VALUES.NEWLINE4.id) {
                infos.add(new SpannableString("\n"));
            }
        }
    }

    public static void formatCacheLists(final @NonNull Geocache cache, final @Nullable List<AbstractList> storedLists, final @Nullable String excludeList, final List<SpannableString> infos) {
        if (null != storedLists) {
            final Set<Integer> lists = cache.getLists();
            for (final AbstractList temp : storedLists) {
                if (lists.contains(temp.id) && !temp.title.equals(excludeList)) {
                    infos.add(new SpannableString(temp.title));
                }
            }
        }
    }

    @NonNull
    public static String formatCacheInfoShort(final Geocache cache) {
        final List<String> infos = new ArrayList<>();
        addShortInfos(cache, infos);
        return StringUtils.join(infos, SEPARATOR);
    }

    private static void addShortInfos(final Geocache cache, final List<String> infos) {
        if (cache.hasDifficulty()) {
            infos.add("D " + formatDT(cache.getDifficulty()));
        }
        if (cache.hasTerrain()) {
            infos.add("T " + formatDT(cache.getTerrain()));
        }
        // don't show "not chosen" for events and virtuals, that should be the normal case
        if (cache.getSize() != CacheSize.UNKNOWN && cache.showSize()) {
            infos.add(cache.getSize().getL10n());
        } else if (cache.isEventCache()) {
            final Date hiddenDate = cache.getHiddenDate();
            if (hiddenDate != null) {
                infos.add(formatShortDateIncludingWeekday(hiddenDate.getTime()));
            }
        }
    }

    private static String formatDT(final float value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    @NonNull
    public static String formatFavCount(final int favCount) {
        return favCount >= 10000 ? (favCount / 1000) + "k" : favCount >= 0 ? Integer.toString(favCount) : "?";
    }

    @NonNull
    public static String formatCacheInfoHistory(final Geocache cache) {
        final List<String> infos = new ArrayList<>(3);
        infos.add(StringUtils.upperCase(cache.getShortGeocode()));
        infos.add(formatDate(cache.getVisitedDate()));
        infos.add(formatTime(cache.getVisitedDate()));
        return StringUtils.join(infos, SEPARATOR);
    }

    @NonNull
    public static String formatWaypointInfo(final Waypoint waypoint) {
        final List<String> infos = new ArrayList<>(3);
        final WaypointType waypointType = waypoint.getWaypointType();
        if (waypointType != WaypointType.OWN && waypointType != null) {
            infos.add(waypointType.getL10n());
        }
        if (waypoint.isUserDefined()) {
            infos.add(CgeoApplication.getInstance().getString(R.string.waypoint_custom));
        } else {
            if (StringUtils.isNotBlank(waypoint.getPrefix())) {
                infos.add(waypoint.getPrefix());
            }
            if (StringUtils.isNotBlank(waypoint.getLookup())) {
                infos.add(waypoint.getLookup());
            }
        }
        return StringUtils.join(infos, SEPARATOR);
    }

    @NonNull
    public static String formatDaysAgo(final long date) {
        final int days = CalendarUtils.daysSince(date);
        return CgeoApplication.getInstance().getResources().getQuantityString(R.plurals.days_ago, days, days);
    }

    @NonNull
    public static String formatStoredAgo(final long updatedTimeMillis) {
        final long minutes = (System.currentTimeMillis() - updatedTimeMillis) / MINUTE_IN_MILLIS;
        final long days = minutes / MINUTES_PER_DAY;

        final String ago;
        if (updatedTimeMillis == 0L) {
            ago = "";
        } else if (minutes < 15) {
            ago = CgeoApplication.getInstance().getString(R.string.cache_offline_time_mins_few);
        } else if (minutes < 60) {
            ago = CgeoApplication.getInstance().getResources().getQuantityString(R.plurals.cache_offline_about_time_mins, (int) minutes, (int) minutes);
        } else if (days < 2) {
            ago = CgeoApplication.getInstance().getResources().getQuantityString(R.plurals.cache_offline_about_time_hours, (int) (minutes / 60), (int) (minutes / 60));
        } else if (days < DAYS_PER_MONTH) {
            ago = CgeoApplication.getInstance().getResources().getQuantityString(R.plurals.cache_offline_about_time_days, (int) days, (int) days);
        } else if (days < 365) {
            ago = CgeoApplication.getInstance().getResources().getQuantityString(R.plurals.cache_offline_about_time_months, (int) (days / DAYS_PER_MONTH), (int) (days / DAYS_PER_MONTH));
        } else {
            ago = CgeoApplication.getInstance().getString(R.string.cache_offline_about_time_year);
        }

        return String.format(CgeoApplication.getInstance().getString(R.string.cache_offline_stored_ago), ago);
    }

    /**
     * Formatting of the hidden date of a cache
     *
     * @return {@code null} or hidden date of the cache (or event date of the cache) in human readable format
     */
    @Nullable
    public static String formatHiddenDate(final Geocache cache) {
        final Date hiddenDate = cache.getHiddenDate();
        if (hiddenDate == null) {
            return null;
        }
        final long time = hiddenDate.getTime();
        if (time <= 0) {
            return null;
        }
        String dateString = formatFullDate(time);
        if (cache.isEventCache()) {
            // use today and yesterday strings
            final String verbally = formatDateVerbally(time);
            if (verbally != null) {
                return verbally;
            }
            // otherwise use weekday and normal date
            dateString = DateUtils.formatDateTime(CgeoApplication.getInstance().getBaseContext(), time, DateUtils.FORMAT_SHOW_WEEKDAY) + ", " + dateString;
        }
        // use just normal date
        return dateString;
    }

    @NonNull
    public static String formatMapSubtitle(final Geocache cache) {
        final StringBuilder title = new StringBuilder();
        if (cache.hasDifficulty()) {
            title.append("D ").append(formatDT(cache.getDifficulty())).append(SEPARATOR);
        }
        if (cache.hasTerrain()) {
            title.append("T ").append(formatDT(cache.getTerrain())).append(SEPARATOR);
        }
        title.append(cache.getSize().getShortName()).append(SEPARATOR).append(cache.getShortGeocode());
        return title.toString();
    }

    @NonNull
    public static String formatPocketQueryInfo(final GCList pocketQuery) {
        if (!pocketQuery.isDownloadable()) {
            return StringUtils.EMPTY;
        }

        final List<String> infos = new ArrayList<>(3);
        final int caches = pocketQuery.getCaches();
        if (caches >= 0) {
            infos.add(CgeoApplication.getInstance().getResources().getQuantityString(R.plurals.cache_counts, caches, caches));
        }

        final long lastGenerationTime = pocketQuery.getLastGenerationTime();
        if (lastGenerationTime > 0) {
            infos.add(Formatter.formatShortDateVerbally(lastGenerationTime) + (PocketQueryHistory.isNew(pocketQuery) ? " (" + CgeoApplication.getInstance().getString(R.string.search_pocket_is_new) + ")" : ""));
        }

        final int daysRemaining = pocketQuery.getDaysRemaining();
        if (daysRemaining == 0) {
            infos.add(CgeoApplication.getInstance().getString(R.string.last_day_available));
        } else if (daysRemaining > 0) {
            infos.add(CgeoApplication.getInstance().getResources().getQuantityString(R.plurals.days_remaining, daysRemaining, daysRemaining));
        }

        if (pocketQuery.isBookmarkList()) {
            infos.add(CgeoApplication.getInstance().getResources().getString(R.string.search_bookmark_list));
        }

        return StringUtils.join(infos, SEPARATOR);
    }

    public static String formatBytes(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(1024));
        final String pre = Character.toString("KMGTPE".charAt(exp - 1));

        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static String formatDuration(final long milliseconds) {
        //currently only used for millisecond/second range, could be expanded to longer ranges later
        if (milliseconds > 1000) {
            return (milliseconds / 1000) + "." + (milliseconds % 1000) + "s";
        }
        return milliseconds + "ms";
    }

    public static String formatDurationInMinutesAndSeconds(final long milliseconds) {
        if (milliseconds < 0) {
            return "?:??";
        }
        final long minutes = ((milliseconds + 500) / 60000);
        final long seconds = ((milliseconds + 500) / 1000) - minutes * 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public static List<CharSequence> truncateCommonSubdir(@NonNull final List<CharSequence> directories) {
        if (directories.size() < 2) {
            return directories;
        }
        String commonEnding = directories.get(0).toString();
        for (int i = 1; i < directories.size(); i++) {
            final String directory = directories.get(i).toString();
            while (!directory.endsWith(commonEnding)) {
                final int offset = commonEnding.indexOf('/', 1);
                if (offset == -1) {
                    return directories;
                }
                commonEnding = commonEnding.substring(offset);
            }
        }
        final List<CharSequence> truncatedDirs = new ArrayList<>(directories.size());
        for (final CharSequence title : directories) {
            truncatedDirs.add(title.subSequence(0, title.length() - commonEnding.length() + 1) + "\u2026");
        }
        return truncatedDirs;
    }

    @NonNull
    public static String generateShortGeocode(final String fullGeocode) {
        return (fullGeocode.length() <= SHORT_GEOCODE_MAX_LENGTH) ? fullGeocode : (fullGeocode.substring(0, SHORT_GEOCODE_MAX_LENGTH) + "…");
    }

    /**
     * Format a numeric string into a 2-digit number with leading zeros
     */
    public static String formatNumberTwoDigits(final String number) {
        return String.format(Locale.getDefault(), "%02d", Integer.parseInt(number));
    }

    public static String formatNumberTwoDigits(final int number) {
        return String.format(Locale.getDefault(), "%02d", number);
    }

    public static String formatGCEventTime(final String tableInside) {
        final MatcherWrapper eventTimesMatcher = new MatcherWrapper(GCConstants.PATTERN_EVENTTIMES, tableInside);
        final StringBuilder sDesc = new StringBuilder();
        if (eventTimesMatcher.find()) {
            final boolean hour12mode = eventTimesMatcher.group(1).trim().equals("PM") || eventTimesMatcher.group(4).trim().equals("PM") || eventTimesMatcher.group(1).trim().equals("AM") || eventTimesMatcher.group(4).trim().equals("AM");
            sDesc.append(formatNumberTwoDigits(
                        Integer.parseInt(
                            eventTimesMatcher.group(2))
                            + (eventTimesMatcher.group(1).trim().equals("PM") ? 12 : 0)
                            + (eventTimesMatcher.group(4).trim().equals("PM") ? 12 : 0)
                            - ((hour12mode && "12".equals(eventTimesMatcher.group(2))) ? 12 : 0)))
                    .append(":")
                    .append(formatNumberTwoDigits(eventTimesMatcher.group(3)))
                    .append(" - ")
                    .append(formatNumberTwoDigits(
                            Integer.parseInt(
                                    eventTimesMatcher.group(6))
                                    + (eventTimesMatcher.group(5).trim().equals("PM") ? 12 : 0)
                                    + (eventTimesMatcher.group(8).trim().equals("PM") ? 12 : 0)
                                    - (hour12mode && ("12".equals(eventTimesMatcher.group(6))) ? 12 : 0)))
                    .append(":")
                    .append(formatNumberTwoDigits(eventTimesMatcher.group(7)));
        }
        return sDesc.toString();
    }

}
