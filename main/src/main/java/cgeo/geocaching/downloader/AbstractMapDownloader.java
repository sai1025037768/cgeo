package cgeo.geocaching.downloader;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.R;
import cgeo.geocaching.maps.mapsforge.MapsforgeMapProvider;
import cgeo.geocaching.models.Download;
import cgeo.geocaching.storage.ContentStorage;
import cgeo.geocaching.storage.PersistableFolder;
import cgeo.geocaching.utils.FileUtils;
import static cgeo.geocaching.Intents.ACTION_INVALIDATE_MAPLIST;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

abstract class AbstractMapDownloader extends AbstractDownloader {

    public static final int ICONRES_MAP = R.drawable.ic_menu_mapmode;

    AbstractMapDownloader(final Download.DownloadType offlineMapType, final @StringRes int mapBase, final @StringRes int mapSourceName, final @StringRes int mapSourceInfo, final @StringRes int projectUrl, final @StringRes int likeItUrl) {
        super(offlineMapType, mapBase, mapSourceName, mapSourceInfo, projectUrl, likeItUrl, PersistableFolder.OFFLINE_MAPS);
        this.iconRes = ICONRES_MAP;
        this.forceExtension = FileUtils.MAP_FILE_EXTENSION;
    }

    @Override
    protected void onSuccessfulReceive(final Uri result) {
        // update list of offline maps
        MapsforgeMapProvider.getInstance().updateOfflineMaps(null);
        LocalBroadcastManager.getInstance(CgeoApplication.getInstance()).sendBroadcast(new Intent(ACTION_INVALIDATE_MAPLIST));
    }

    /**
     * Check if any of the given files already exists in the given path
     *
     * @param filenames
     *      Array of filenames to check for
     * @param baseUrl
     *      Base URL for the first filename
     * @param offlineMapType
     *      Descriptor type to return
     * @return
     *      Returns null, if any of the given files found.
     *      Returns a download descriptor for the first entry (so baseUrl must be valid for that entry)
     */
    @Nullable
    public DownloaderUtils.DownloadDescriptor getExtrafile(final String[] filenames, final String baseUrl, final Download.DownloadType offlineMapType) {
        final List<ContentStorage.FileInformation> dirContent = ContentStorage.get().list(PersistableFolder.OFFLINE_MAP_THEMES);
        for (ContentStorage.FileInformation fi : dirContent) {
            for (String filename : filenames) {
                if (fi.name.equals(filename)) {
                    return null;
                }
            }
        }
        return new DownloaderUtils.DownloadDescriptor(filenames[0], Uri.parse(baseUrl + filenames[0]), offlineMapType.id);
    }

}
