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
import org.broadleafcommerce.common.config.service.SystemPropertiesServiceImpl;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sf.ehcache.CacheManager;

/**
 * Tests that error messages are returned for misconfigured amazon s3 properties.
 *
 * @author bpolster
 */
public abstract class AbstractS3Test {

    protected static TestSystemPropertiesService propService = new TestSystemPropertiesService();
    protected static S3ConfigurationServiceImpl configService = new S3ConfigurationServiceImpl();

    @BeforeClass
    public static void setup() {
        CacheManager.getInstance().addCacheIfAbsent("blSystemPropertyElements");
        configService.setSystemPropertiesService(propService);
    }
    
    @Before
    public void reset() {
        resetAllProperties();
    }

    protected void resetAllProperties() {
        propService.setProperty("aws.s3.accessKeyId", findProperty("aws.s3.accessKeyId", "testKeyId"));
        propService.setProperty("aws.s3.secretKey", findProperty("aws.s3.secretKey", "secretKey"));
        propService.setProperty("aws.s3.defaultBucketName", findProperty("aws.s3.defaultBucketName", "bucketName"));
        propService.setProperty("aws.s3.defaultBucketRegion", findProperty("aws.s3.defaultBucketRegion", "us-west-2"));
        propService.setProperty("aws.s3.endpointURI", findProperty("aws.s3.endpointURI", "https://s3.amazonaws.com"));
        propService.setProperty("aws.s3.bucketSubDirectory", findProperty("aws.s3.bucketSubDirectory", ""));
        propService.setProperty("aws.s3.useInstanceProfile", findProperty("aws.s3.useInstanceProfile", "false"));
        propService.setProperty("aws.s3.sse", findProperty("aws.s3.sse", "false"));
    }

    public static class TestSystemPropertiesService extends SystemPropertiesServiceImpl {

        public void setProperty(String propertyName, String value) {
            super.addPropertyToCache(propertyName, value);
        }
    }

    protected String findProperty(String propertyName, String defaultValue) {
        String returnName = null;
        try {
            final Properties properties = new Properties();

            returnName = System.getProperty(propertyName);
            if (returnName == null) {
                InputStream path = this.getClass().getResourceAsStream("/config/bc/override/common.properties");
                if (path != null) {
                    properties.load(path);
                }
                returnName = properties.getProperty(propertyName);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (StringUtils.isEmpty(returnName)) {
            return defaultValue;
        }
        return returnName;
    }
}
