
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
        resetAllProperties();
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
        resetAllProperties();
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
        resetAllProperties();
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
        resetAllProperties();
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
}
