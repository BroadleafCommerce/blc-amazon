package org.broadleafcommerce.vendor.amazon.s3;

import org.broadleafcommerce.common.config.service.SystemPropertiesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Service that returns the an S3 configuration object.
 * 
 * @author bpolster
 *
 */
@Service("blS3ConfigurationService")
public class S3ConfigurationServiceImpl implements S3ConfigurationService {

    @Resource(name = "blSystemPropertiesService")
    protected SystemPropertiesService systemPropertiesService;

    protected S3Configuration s3Configuration = new S3Configuration();
    
    public S3Configuration lookupS3Configuration() {
        return s3Configuration;
    }

    public String lookupS3SecretKey() {
        if (s3Configuration.getAwsSecretKey() != null) {
            return s3Configuration.getAwsSecretKey();
        }
        return systemPropertiesService.resolveSystemProperty("aws.s3.secretKey");
    }

    public String lookupS3AccessKeyId() {
        if (s3Configuration.getGetAWSAccessKeyId() != null) {
            return s3Configuration.getGetAWSAccessKeyId();
        }
        return systemPropertiesService.resolveSystemProperty("aws.s3.accessKeyId");
    }

    public String lookupS3DefaultBucketName() {
        if (s3Configuration.getDefaultBucketName() != null) {
            return s3Configuration.getDefaultBucketName();
        }
        return systemPropertiesService.resolveSystemProperty("defaultBucketName");
    }

    public String lookupS3DefaultRegionName() {
        if (s3Configuration.getDefaultBucketRegion() != null) {
            return s3Configuration.getDefaultBucketRegion();
        }
        return systemPropertiesService.resolveSystemProperty("defaultBucketRegion");
    }

}
