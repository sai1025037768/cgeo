package cgeo.geocaching.models;

import cgeo.geocaching.location.Geopoint;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class RouteSegment implements Parcelable {
    private final RouteItem item;
    private float distance;
    private ArrayList<Geopoint> points;
    private ArrayList<Float> elevation;
    private boolean linkToPreviousSegment = true;

    public RouteSegment(final RouteItem item, final ArrayList<Geopoint> points, final boolean linkToPreviousSegment) {
        this.item = item;
        distance = 0.0f;
        this.points = points;
        this.elevation = null;
        this.linkToPreviousSegment = linkToPreviousSegment;
    }

    public RouteSegment(final RouteItem item, final ArrayList<Geopoint> points, final ArrayList<Float> elevation, final boolean linkToPreviousSegment) {
        this(item, points, linkToPreviousSegment);
        this.elevation = elevation;
    }

    public RouteSegment(final ArrayList<Geopoint> points, @Nullable final ArrayList<Float> elevation) {
        this(new RouteItem(points.get(points.size() - 1)), points, false);
        this.elevation = elevation;
    }

    public float calculateDistance() {
        distance = 0.0f;
        if (!points.isEmpty()) {
            Geopoint last = points.get(0);
            for (Geopoint point : points) {
                distance += last.distanceTo(point);
                last = point;
            }
        }
        return distance;
    }

    public RouteItem getItem() {
        return item;
    }

    public float getDistance() {
        return distance;
    }

    public ArrayList<Geopoint> getPoints() {
        if (null == points || points.isEmpty()) {
            this.points = new ArrayList<>();
            final Geopoint point = item.getPoint();
            if (null != point) {
                this.points.add(point);
            }
        }
        return points;
    }

    public int getSize() {
        return points.size();
    }

    public Geopoint getPoint() {
        return item.getPoint();
    }

    public boolean hasPoint() {
        return null != item.getPoint();
    }

    public void addPoint(final Geopoint geopoint) {
        addPoint(geopoint, Float.NaN);
    }

    public void addPoint(final Geopoint geopoint, final float elevation) {
        if (this.elevation != null && this.elevation.size() == points.size()) {
            this.elevation.add(elevation);
        }
        points.add(geopoint);
    }

    public void resetPoints() {
        points = new ArrayList<>();
        elevation = new ArrayList<>();
        distance = 0.0f;
    }

    public void setElevation(final ArrayList<Float> elevation) {
        this.elevation.clear();
        this.elevation.addAll(elevation);
    }

    public ArrayList<Float> getElevation() {
        return elevation;
    }

    public boolean getLinkToPreviousSegment() {
        return linkToPreviousSegment;
    }

    // Parcelable methods

    public static final Parcelable.Creator<RouteSegment> CREATOR = new Parcelable.Creator<RouteSegment>() {

        @Override
        public RouteSegment createFromParcel(final Parcel source) {
            return new RouteSegment(source);
        }

        @Override
        public RouteSegment[] newArray(final int size) {
            return new RouteSegment[size];
        }

    };

    private RouteSegment(final Parcel parcel) {
        item = parcel.readParcelable(RouteItem.class.getClassLoader());
        distance = parcel.readFloat();
        points = parcel.readArrayList(Geopoint.class.getClassLoader());
        elevation = parcel.readArrayList(Float.TYPE.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(item, flags);
        dest.writeFloat(distance);
        dest.writeList(points);
        dest.writeList(elevation);
    }

}
