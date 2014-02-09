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
