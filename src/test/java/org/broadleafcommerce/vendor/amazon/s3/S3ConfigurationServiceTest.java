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

import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * Tests that error messages are returned for misconfigured amazon s3 properties. 
 * 
 * @author bpolster
 */
public class S3ConfigurationServiceTest extends AbstractS3Test {
    
    @Test
    public void checkForAllPropertiesSet() {
        boolean ok;
        try {
            configService.lookupS3Configuration();
            ok = true;
        } catch(IllegalArgumentException iae) {
            ok = false;
        }
        assertTrue("No exception thrown", ok);
    }

    @Test
    public void checkForBadBucketRegion() {
        propService.setProperty("aws.s3.defaultBucketRegion", "this-bucket-region-is-not-good");
        boolean ok;
        try {
            configService.lookupS3Configuration();
            ok = true;
        } catch (IllegalArgumentException iae) {
            ok = false;
        }
        assertTrue("Expected to get an exception.", !ok);
    }

    @Test
    public void checkForMissingAccessKeyId() {
        propService.setProperty("aws.s3.accessKeyId", "");
        boolean ok;
        try {
            configService.lookupS3Configuration();
            ok = true;
        } catch (IllegalArgumentException iae) {
            ok = false;
        }
        assertTrue("Expected to get an exception.", !ok);
    }

    @Test
    public void checkForMissingSecretKey() {
        propService.setProperty("aws.s3.secretKey", "");
        boolean ok;
        try {
            configService.lookupS3Configuration();
            ok = true;
        } catch (IllegalArgumentException iae) {
            ok = false;
        }
        assertTrue("Expected to get an exception.", !ok);
    }

    @Test
    public void checkForMissingCredential() {
        propService.setProperty("aws.s3.useInstanceProfile", "false");
        propService.setProperty("aws.s3.accessKeyId", "");
        propService.setProperty("aws.s3.secretKey", "");

        boolean ok;
        try {
            configService.lookupS3Configuration();
            ok = true;
        } catch (IllegalArgumentException iae) {
            ok = false;
        }
        assertTrue("Exepected to get an exception.", !ok);
    }
}
