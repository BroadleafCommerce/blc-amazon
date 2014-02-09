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
