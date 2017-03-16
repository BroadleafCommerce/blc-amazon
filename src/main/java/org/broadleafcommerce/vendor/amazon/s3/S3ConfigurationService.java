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


/**
 * Service that returns the an S3 configuration object.
 * S3 requires two keys to be set.   This service allows you to determine the appropriate keys.
 * 
 * The default implementation uses system properties to determine the settings for Amazon S3
 *  
 * This includes properties that allow access to your companies amazon account ...
 *    aws.s3.accessKeyId
 *    aws.s3.secretKey
 * See <a href="http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSGettingStartedGuide/AWSCredentials.html">
 * About Amazon Credentials</a> for more information on these two properties.
 *   
 *  The system also requires you to specify where the files will be stored in the form of an Amazon region and bucket.   
 *  These two values have defaults that might work for US customers.   For others, see the Amazon documentation at
 *  <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/LocationSelection.html">Amazon S3 Location Selection</a> 
 *  
 *  aws.s3.defaultBucketName (defaults to "broadleaf-commerce-files")
 *  aws.s3.defaultBucketRegion - (defaults to "us-west-2")
 * 
 * @author bpolster
 *
 */
public interface S3ConfigurationService {

    S3Configuration lookupS3Configuration();
}
