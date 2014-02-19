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

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.config.service.SystemPropertiesServiceImpl;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
    
    protected void resetAllProperties() {
        propService.setProperty("aws.s3.accessKeyId", findProperty("aws.s3.accessKeyId", "testKeyId"));
        propService.setProperty("aws.s3.secretKey", findProperty("aws.s3.secretKey", "secretKey"));
        propService.setProperty("aws.s3.defaultBucketName", findProperty("aws.s3.defaultBucketName", "bucketName"));
        propService.setProperty("aws.s3.defaultBucketRegion", findProperty("aws.s3.defaultBucketRegion", "us-west-2"));
        propService.setProperty("aws.s3.bucketSubDirectory", findProperty("aws.s3.bucketSubDirectory", ""));
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
