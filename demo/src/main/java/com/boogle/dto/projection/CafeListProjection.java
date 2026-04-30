package com.boogle.dto.projection;

public interface CafeListProjection {

    Long getId();
    String getName();
    String getAddress();
    Double getLatitude();
    Double getLongitude();
    String getThumbnail();
    Long getReviewCount();
}
