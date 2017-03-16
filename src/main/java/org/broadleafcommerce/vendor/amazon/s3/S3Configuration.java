/*
 * #%L
 * BroadleafCommerce Amazon Integrations
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.vendor.amazon.s3;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class that holds the configuration for connecting to AmazonS3.
 * 
 * @author bpolster
 *
 */
public class S3Configuration {

    private String awsSecretKey;
    private String getAWSAccessKeyId;
    private String defaultBucketName;
    private String defaultBucketRegion;
    private String endpointURI;
    private String bucketSubDirectory;

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public String getGetAWSAccessKeyId() {
        return getAWSAccessKeyId;
    }

    public void setGetAWSAccessKeyId(String getAWSAccessKeyId) {
        this.getAWSAccessKeyId = getAWSAccessKeyId;
    }

    public String getDefaultBucketName() {
        return defaultBucketName;
    }

    public void setDefaultBucketName(String defaultBucketName) {
        this.defaultBucketName = defaultBucketName;
    }

    public String getDefaultBucketRegion() {
        return defaultBucketRegion;
    }

    public void setDefaultBucketRegion(String defaultBucketRegion) {
        this.defaultBucketRegion = defaultBucketRegion;
    }
        
    public String getEndpointURI() {
        return endpointURI;
    }

    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
    }

    public String getBucketSubDirectory() {
        return bucketSubDirectory;
    }

    public void setBucketSubDirectory(String bucketSubDirectory) {
        this.bucketSubDirectory = bucketSubDirectory;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(awsSecretKey)
            .append(awsSecretKey)
            .append(defaultBucketRegion)
            .append(defaultBucketRegion)
            .append(endpointURI)
            .append(bucketSubDirectory)
            .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof S3Configuration) {
            S3Configuration that = (S3Configuration) obj;
            return new EqualsBuilder()
                .append(this.awsSecretKey, that.awsSecretKey)
                .append(this.defaultBucketName, that.defaultBucketName)
                .append(this.defaultBucketRegion, that.defaultBucketRegion)
                .append(this.getAWSAccessKeyId, that.getAWSAccessKeyId)
                .append(this.endpointURI, that.endpointURI)
                .append(this.bucketSubDirectory, that.bucketSubDirectory)
                .build();
        }
        return false;
    }

}
