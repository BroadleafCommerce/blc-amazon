# Module Installation 
The Broadleaf Amazon module requires [configuration](#configuration-changes) and [third-party property configuration](#third-party-property-configuration)


## Configuration Changes
**Step 1.**  Add the dependency management section to your **parent** `pom.xml`:
    
```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-amazon</artifactId>
    <version>1.0.0-GA</version>
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
Broadleaf requires access to your Amazon AWS account.   See <a href="http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSGettingStartedGuide/AWSCredentials.html">About Amazon Credentials</a> for more information on these two properties.  

When you create the access keys on amazon you will need to copy the values and add the following properties to your `common-shared.properties` file located in your core project.
 
aws.s3.accessKeyId=
aws.s3.secretKey=

### Amazon File Storage Location Information 
Broadleaf needs to know the specific location within your S3 account to store the files.   You will need to set the bucket name and the bucket location.  These properties are described on the <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/LocationSelection.html">Amazon S3 Location Selection</a> page.

_The bucket name must be unique across all of amazon_
aws.s3.defaultBucketName=

_Broadleaf will default to the "us-west-2" region of S3.    You can ovverride the region by setting the following property. _
aws.s3.defaultBucketRegion=us-west-2
