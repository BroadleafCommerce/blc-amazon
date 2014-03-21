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

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((awsSecretKey == null) ? 0 : awsSecretKey.hashCode());
        result = prime * result + ((defaultBucketName == null) ? 0 : defaultBucketName.hashCode());
        result = prime * result + ((defaultBucketRegion == null) ? 0 : defaultBucketRegion.hashCode());
        result = prime * result + ((getAWSAccessKeyId == null) ? 0 : getAWSAccessKeyId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        S3Configuration other = (S3Configuration) obj;
        if (awsSecretKey == null) {
            if (other.awsSecretKey != null) return false;
        } else if (!awsSecretKey.equals(other.awsSecretKey)) return false;
        if (defaultBucketName == null) {
            if (other.defaultBucketName != null) return false;
        } else if (!defaultBucketName.equals(other.defaultBucketName)) return false;
        if (defaultBucketRegion == null) {
            if (other.defaultBucketRegion != null) return false;
        } else if (!defaultBucketRegion.equals(other.defaultBucketRegion)) return false;
        if (getAWSAccessKeyId == null) {
            if (other.getAWSAccessKeyId != null) return false;
        } else if (!getAWSAccessKeyId.equals(other.getAWSAccessKeyId)) return false;
        return true;
    }

}
