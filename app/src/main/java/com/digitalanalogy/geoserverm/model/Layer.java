package com.digitalanalogy.geoserverm.model;

public class Layer {
    private final String name;
    private final String title;
    private final String _abstract;
    private final String SRS;
    private final double minXBound;
    private final double minYBound;
    private final double maxXBound;
    private final double maxYBound;
    private final String getMapUrlSuffix;

    public Layer(String name, String title, String _abstract, String SRS, double minXBound, double minYBound, double maxXBound, double maxYBound) {
        this.name = name;
        this.title = title;
        this._abstract = _abstract;
        this.SRS = SRS;
        this.minXBound = minXBound;
        this.minYBound = minYBound;
        this.maxXBound = maxXBound;
        this.maxYBound = maxYBound;
        this.getMapUrlSuffix = "";
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String get_abstract() {
        return _abstract;
    }

    public String getSRS() {
        return SRS;
    }

    public double getMinXBound() {
        return minXBound;
    }

    public double getMinYBound() {
        return minYBound;
    }

    public double getMaxXBound() {
        return maxXBound;
    }

    public double getMaxYBound() {
        return maxYBound;
    }

    public String getGetMapUrlSuffix() {
        return getMapUrlSuffix;
    }
}