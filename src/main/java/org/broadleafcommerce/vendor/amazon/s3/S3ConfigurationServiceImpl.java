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

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.config.service.SystemPropertiesService;
import org.springframework.stereotype.Service;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

import javax.annotation.Resource;

/**
 * Service that returns the an S3 configuration object.   Returns a configuration object with values
 * that are defined as system properties.
 *
 * @author bpolster
 *
 */
@Service("blS3ConfigurationService")
public class S3ConfigurationServiceImpl implements S3ConfigurationService {

    @Resource(name = "blSystemPropertiesService")
    protected SystemPropertiesService systemPropertiesService;

    @Override
    public S3Configuration lookupS3Configuration() {
        S3Configuration s3config = new S3Configuration();
        s3config.setAwsSecretKey(lookupProperty("aws.s3.secretKey"));
        s3config.setDefaultBucketName(lookupProperty("aws.s3.defaultBucketName"));
        s3config.setDefaultBucketRegion(lookupProperty("aws.s3.defaultBucketRegion"));
        s3config.setGetAWSAccessKeyId(lookupProperty("aws.s3.accessKeyId"));
        s3config.setEndpointURI(lookupProperty("aws.s3.endpointURI"));
        s3config.setBucketSubDirectory(lookupProperty("aws.s3.bucketSubDirectory"));
        s3config.setUseInstanceProfileCredentials(Boolean.parseBoolean(lookupProperty("aws.s3.useInstanceProfile")));
        s3config.setEnableSSE(Boolean.parseBoolean(lookupProperty("aws.s3.sse")));

        boolean accessSecretKeyBlank = StringUtils.isEmpty(s3config.getAwsSecretKey());
        boolean accessKeyIdBlank = StringUtils.isEmpty(s3config.getGetAWSAccessKeyId());
        boolean bucketNameBlank = StringUtils.isEmpty(s3config.getDefaultBucketName());
        boolean useInstanceProfile = s3config.getUseInstanceProfileCredentials();
        Region region = RegionUtils.getRegion(s3config.getDefaultBucketRegion());
        boolean canRetrieveCredentials = !(accessSecretKeyBlank || accessKeyIdBlank) || useInstanceProfile;

        if (region == null || !canRetrieveCredentials || bucketNameBlank) {
            StringBuilder errorMessage = new StringBuilder("Amazon S3 Configuration Error : ");

            if (accessSecretKeyBlank) {
                errorMessage.append("aws.s3.secretKey was blank,");
            }

            if (accessKeyIdBlank) {
                errorMessage.append("aws.s3.accessKeyId was blank,");
            }

            if (bucketNameBlank) {
                errorMessage.append("aws.s3.defaultBucketName was blank,");
            }

            if (!useInstanceProfile) {
                errorMessage.append("aws.s3.useInstanceProfile was blank or false,");
            }

            if (region == null) {
                errorMessage.append("aws.s3.defaultBucketRegion was set to an invalid value of "
                        + s3config.getDefaultBucketRegion());
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }

        return s3config;
    }

    protected String lookupProperty(String propertyName) {
        return systemPropertiesService.resolveSystemProperty(propertyName);
    }

    protected void setSystemPropertiesService(SystemPropertiesService systemPropertiesService) {
        this.systemPropertiesService = systemPropertiesService;
    }

}
