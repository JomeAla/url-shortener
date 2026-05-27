package com.jomea.urlshortener.dto;

import java.util.List;

public record TimeSeriesResponse(
    String interval,
    List<DataPoint> dataPoints
) {
    public record DataPoint(
        String timestamp,
        long count
    ) {}
}
