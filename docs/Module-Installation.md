# Module Installation 
The Broadleaf Amazon module requires [configuration](#configuration-changes) and [third-party property configuration](#third-party-property-configuration)

## Broadleaf Dependency

Version 1.1.0-GA requires Broadleaf 4.0 or later.

## Configuration Changes
**Step 1.**  Add the dependency management section to your **parent** `pom.xml`:
    
```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-amazon</artifactId>
    <version>1.1.0-GA</version>
    <type>jar</type>
    <scope>compile</scope>
</dependency>
```

**Step 2.**  Add the dependency into your `core/pom.xml`:
    
```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-amazon</artifactId>
</dependency>
```

**Step 3.** Include the necessary `patchConfigLocation` files in your `admin/web.xml`:

```xml
classpath:/bl-amazon-applicationContext.xml
```
>Note: This line should go before the `classpath:/applicationContext.xml` line


**Step 4.** Include the necessary `patchConfigLocation` files in your `site/web.xml`:

```xml
classpath:/bl-amazon-applicationContext.xml
```
>Note: This line should go before the `classpath:/applicationContext.xml` line


## Third Party Property Configuration
This module requires you to configure properties specific to your amazon account.   

### Amazon Credentials
Broadleaf requires access to your Amazon AWS account.   See [About Amazon Credentials](http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSGettingStartedGuide/AWSCredentials.html) for more information on these two properties.  

When you create the access keys on amazon you will need to copy the values and add the following properties to your `common-shared.properties` file located in your core project.
 
aws.s3.accessKeyId=
aws.s3.secretKey=

### Amazon File Storage Location Information 
Broadleaf needs to know the specific location within your S3 account to store the files.   You will need to set the bucket name and the bucket location.  These properties are described on the [Amazon S3 Location Selection page](http://docs.aws.amazon.com/AmazonS3/latest/dev/LocationSelection.html).

_The bucket name must be unique across all of Amazon_

    aws.s3.defaultBucketName=

_If you would like to store files inside of a folder within the bucket (like 'img'), set the following property:_

    aws.s3.bucketSubDirectory=img

> Starting and trailing slashes will be stripped from this value to build the file path so they are not necessary

_The Amazon module will default to the "us-west-2" region of S3. You can override the region by setting the following property._

    aws.s3.defaultBucketRegion=us-west-2

_The Amazon module will default to utilize the default US endpoint "https://s3.amazonaws.com". You can override the endpoint with the following property:_

    aws.s3.endpointURI=https://s3.amazonaws.com


