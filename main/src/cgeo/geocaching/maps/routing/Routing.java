package cgeo.geocaching.maps.routing;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.utils.Log;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Xml;

import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class Routing {
    private static final double UPDATE_MIN_DISTANCE_KILOMETERS = 0.005;
    private static final double MAX_ROUTING_DISTANCE_KILOMETERS = 10.0;
    private static final int UPDATE_MIN_DELAY_SECONDS = 3;
    private static BRouterServiceConnection brouter;
    private static Geopoint lastDirectionUpdatePoint;
    private static Geopoint[] lastRoutingPoints;
    private static Geopoint lastDestination;
    private static long timeLastUpdate;

    private Routing() {
        // utility class
    }

    public static void connect() {
        if (brouter != null && brouter.isConnected()) {
            //already connected
            return;
        }

        brouter = new BRouterServiceConnection();
        final Intent intent = new Intent();
        intent.setClassName("btools.routingapp", "btools.routingapp.BRouterService");

        if (!getContext().bindService(intent, brouter, Context.BIND_AUTO_CREATE)) {
            brouter = null;
        }
    }

    private static ContextWrapper getContext() {
        return CgeoApplication.getInstance();
    }

    public static void disconnect() {
        if (brouter != null && brouter.isConnected()) {
            getContext().unbindService(brouter);
            brouter = null;
        }
    }

    @Nullable
    public static Geopoint[] getTrack(final Geopoint start, final Geopoint destination) {
        if (brouter == null) {
            return null;
        }

        // avoid updating to frequently
        final long timeNow = System.currentTimeMillis();
        if ((timeNow - timeLastUpdate) < 1000 * UPDATE_MIN_DELAY_SECONDS) {
            return lastRoutingPoints;
        }

        // Disable routing for huge distances
        if (start.distanceTo(destination) > MAX_ROUTING_DISTANCE_KILOMETERS) {
            return null;
        }

        // Use cached route if current position has not changed more than 5m
        // TODO: Maybe adjust this to current zoomlevel
        if (lastDirectionUpdatePoint != null && destination == lastDestination && start.distanceTo(lastDirectionUpdatePoint) < UPDATE_MIN_DISTANCE_KILOMETERS) {
            return lastRoutingPoints;
        }

        // now really calculate a new route
        lastDestination = destination;
        lastRoutingPoints = calculateRouting(start, destination);
        lastDirectionUpdatePoint = start;
        timeLastUpdate = timeNow;
        return lastRoutingPoints;
    }

    private static Geopoint[] calculateRouting(final Geopoint start, final Geopoint dest) {
        final Bundle params = new Bundle();
        params.putString("trackFormat", "gpx");
        params.putDoubleArray("lats", new double[]{start.getLatitude(), dest.getLatitude()});
        params.putDoubleArray("lons", new double[]{start.getLongitude(), dest.getLongitude()});
        params.putString("v", Settings.getRoutingMode().parameterValue);

        final String gpx = brouter.getTrackFromParams(params);

        return parseGpxTrack(gpx);
    }

    @Nullable
    private static Geopoint[] parseGpxTrack(final String gpx) {
        try {
            final LinkedList<Geopoint> result = new LinkedList<>();
            Xml.parse(gpx, new DefaultHandler() {
                @Override
                public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                    if (qName.equalsIgnoreCase("trkpt")) {
                        final String lat = atts.getValue("lat");
                        if (lat != null) {
                            final String lon = atts.getValue("lon");
                            if (lon != null) {
                                result.add(new Geopoint(lat, lon));
                            }
                        }
                    }
                }
            });

            return result.toArray(new Geopoint[result.size()]);

        } catch (final SAXException e) {
            Log.e("cannot parse brouter output", e);
        }
        return null;
    }

    public static void invalidateRouting() {
        lastDirectionUpdatePoint = null;
        timeLastUpdate = 0;
    }

    public static boolean isAvailable() {
        return brouter != null;
    }
}
