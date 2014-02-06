package org.broadleafcommerce.vendor.amazon.s3;

/**
 * Service that returns the an S3 configuration object.
 * 
 * @author bpolster
 *
 */
public interface S3ConfigurationService {

    S3Configuration lookupS3Configuration();
}
