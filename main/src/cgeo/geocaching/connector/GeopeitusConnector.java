package cgeo.geocaching.connector;

import cgeo.geocaching.models.Geocache;

import org.apache.commons.lang3.StringUtils;
import android.support.annotation.NonNull;

class GeopeitusConnector extends AbstractConnector {

    @Override
    @NonNull
    public String getName() {
        return "geopeitus.ee";
    }

    @Override
    @NonNull
    public String getCacheUrl(@NonNull final Geocache cache) {
        return getCacheUrlPrefix() + StringUtils.stripStart(cache.getGeocode().substring(2), "0");
    }

    @Override
    @NonNull
    public String getHost() {
        return "www.geopeitus.ee";
    }

    @Override
    public boolean isOwner(@NonNull final Geocache cache) {
        return false;
    }

    @Override
    public boolean canHandle(@NonNull final String geocode) {
        return StringUtils.startsWith(geocode, "GE") && isNumericId(geocode.substring(2));
    }

    @Override
    @NonNull
    protected String getCacheUrlPrefix() {
        return "http://" + getHost() + "/aare/";
    }
}
