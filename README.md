Amazon Integrations Module
==========================

This module contains Broadleaf integrations with Amazon AWS APIs.

The following functionality is covered by this module (could be a partial list):

S3 integration : Ability to store images and other assets produced by Broadleaf using Amazon's S3 storage
Fulfillment : Roadmap Item


## Steps to enable this module

1. Add the dependency management section to your **parent** `pom.xml`:
    ```xml
    <dependency>
        <groupId>org.broadleafcommerce</groupId>
        <artifactId>broadleaf-amazon</artifactId>
        <version>1.0.0-GA</version>
        <type>jar</type>
        <scope>compile</scope>
    </dependency>
    ```

2. Pull this dependency into your `core/pom.xml`:
    ```xml
    <dependency>
        <groupId>org.broadleafcommerce</groupId>
        <artifactId>broadleaf-amazon</artifactId>
    </dependency>
    ```

3. Include the necessary `patchConfigLocation` files in your `admin/web.xml`:
    ```xml
        classpath:/bl-amazon-applicationContext.xml
    ```
    > Note: These two lines should go before the `classpath:/applicationContext.xml` line, but after `classpath:/bl-admin-applicationContext.xml`

4. Include the necessary `patchConfigLocation` files in your `site/web.xml`:
    ```xml
        classpath:/bl-amazon-applicationContext.xml
    ```
    > Note: This line should go before the `classpath:/applicationContext.xml` line

## Steps to configure this module

There are several login mechanisms that can be used in AWS.

Configuration can be done by setting specific variables in `common.properties`. Three credentials mechanisms from the ![AWS credentials chain](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html) are available for usage and configuration.

### Amazon AWS S3 Key + Secret Authentication

```
aws.s3.accessKeyId=
aws.s3.secretKey=
```

### EC2 Instance Profile Authentication

```
aws.s3.useInstanceProfile=true
```

### ECS or Fargate Container Authentication 

Note that EC2 Instance Profile Authentication has precedence over ECS and Fargate. If both are set to true, Container Credentials will be ignored.

```
aws.s3.useContainerCredentials=true
```