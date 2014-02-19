/*
 * #%L
 * BroadleafCommerce Amazon Integrations
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                .append(this.bucketSubDirectory, that.getAWSAccessKeyId)
                .build();
        }
        return false;
    }

}
