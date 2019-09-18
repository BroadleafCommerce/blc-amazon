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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.file.FileServiceException;
import org.broadleafcommerce.common.file.domain.FileWorkArea;
import org.broadleafcommerce.common.file.service.BroadleafFileService;
import org.broadleafcommerce.common.file.service.FileServiceProvider;
import org.broadleafcommerce.common.file.service.type.FileApplicationType;
import org.broadleafcommerce.common.io.ConcurrentFileOutputStream;
import org.broadleafcommerce.common.site.domain.Site;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@Service("blS3FileServiceProvider")
/**
 * Provides an Amazon S3 compatible implementation of the FileServiceProvider interface.
 *
 * Uses the <code>blS3ConfigurationService</code> component to provide the amazon connection details.   Once a
 * resource is retrieved from Amazon, the resulting input stream is written to a File on the local file system using
 * <code>blFileService</code> to determine the local file path.
 *
 * @author bpolster
 * @author Mike Garrett
 * @author Ezequiel Gorbatik
 *
 */
public class S3FileServiceProvider implements FileServiceProvider {

    private static final Log LOG = LogFactory.getLog(S3FileServiceProvider.class);

    @Resource(name = "blS3ConfigurationService")
    protected S3ConfigurationService s3ConfigurationService;

    @Resource(name = "blFileService")
    protected BroadleafFileService blFileService;

    protected Map<S3Configuration, AmazonS3> configClientMap = new HashMap<>();

    @Resource(name = "blConcurrentFileOutputStream")
    protected ConcurrentFileOutputStream concurrentFileOutputStream;

    protected static String BUCKET_PREFIX="bucket://";

    protected static String SITE_PREFIX="site-";

    protected static String MULTITENANT_SITE_CLASSNAME= "com.broadleafcommerce.tenant.domain.MultiTenantSite";

    protected static String MULTITENANTSITE_GETPARENTID_METHODNAME= "getParentSiteId";

    /**
     * Entry point to retrieve resources from this module.
     */
    @Override
    public File getResource(String name) {
        return getResource(name, FileApplicationType.ALL);
    }

    /**
     * Returns the bucket name or the default one.
     * @param name
     * @param defaultBucketName
     * @return
     */
    protected String getBucketName(String name, String defaultBucketName) {
        if(name!=null && name.startsWith(BUCKET_PREFIX)){
            return name.substring(BUCKET_PREFIX.length(), name.indexOf("/", BUCKET_PREFIX.length()));
        }
        return defaultBucketName;
    }

     /**
      * Retrieves the resourceName from its rawname.
      * @param s3config
      * @param bucketName
      * @param rawName
      * @return
      */
     protected String getResourceName(S3Configuration s3config, String bucketName, String rawName) {
        String name= rawName;
        if(!bucketName.equals(s3config.getDefaultBucketName())){
            name = name.substring((BUCKET_PREFIX+bucketName).length()+1);
        }
        return name;
    }

    /**
     * Ensures the file creation
     * @param returnFile
     * @param name
     * @throws RuntimeException
     */
    protected void ensureFileCreation(File returnFile,String name) throws RuntimeException {
        if (!returnFile.getParentFile().exists()) {
            if (!returnFile.getParentFile().mkdirs()) {
                // Other thread could have created - check one more time.
                if (!returnFile.getParentFile().exists()) {
                    throw new RuntimeException("Unable to create parent directories for file: " + name);
                }
            }
        }

    }

    /**
     * Handles the logic for resource retrieval it accepts if is a child resource or parent, only differs in
     * in tha name generation.
     * @param rawName
     * @param fileApplicationType
     * @param isParent
     * @return
     */
    public File getResource(String rawName, FileApplicationType fileApplicationType, boolean isParent) {
        String resourceName=(isParent?buildResourceParentName(rawName):buildResourceName(rawName));
        LOG.debug("Local Resource name: "+ resourceName);
        File returnFile = blFileService.getLocalResource(resourceName);
        InputStream inputStream = null;
        String name;

        try {
            S3Configuration s3config = s3ConfigurationService.lookupS3Configuration();
            AmazonS3 s3 = getAmazonS3Client(s3config);

            String bucketName = getBucketName(rawName, s3config.getDefaultBucketName());
            name = getResourceName(s3config,bucketName, rawName);
            
            String resourceNameS3=(isParent?buildResourceParentName(name):buildResourceName(name)); 
            LOG.debug("Resource name in S3: "+ resourceName);
            //No parent present
            if ((!isParent) || (!resourceName.contains(SITE_PREFIX+"0/"))) {
                S3Object object = s3.getObject(new GetObjectRequest(bucketName, resourceNameS3));
                inputStream = object.getObjectContent();

                ensureFileCreation(returnFile, rawName);

                concurrentFileOutputStream.write(inputStream, returnFile);
            }

        } catch (IOException ioe) {
            throw new RuntimeException("Error writing s3 file to local file system", ioe);
        } catch (AmazonS3Exception s3Exception) {
            if ("NoSuchKey".equals(s3Exception.getErrorCode())) {
                return new File("this/path/should/not/exist/" + UUID.randomUUID());
            } else {
                throw s3Exception;
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error closing input stream while writing s3 file to file system", e);
                }
            }
        }
        return returnFile;
    }

    @Override
    public void addOrUpdateResources(FileWorkArea workArea, List<File> files, boolean removeFilesFromWorkArea) {
        addOrUpdateResourcesForPaths(workArea, files, removeFilesFromWorkArea);
    }

    /**
     * Writes the resource to S3.   If the bucket returns as "NoSuchBucket" then will attempt to create the bucket
     * and try again.
     */
    @Override
    public List<String> addOrUpdateResourcesForPaths(FileWorkArea workArea, List<File> files, boolean removeFilesFromWorkArea) {
        S3Configuration s3config = s3ConfigurationService.lookupS3Configuration();
        AmazonS3 s3 = getAmazonS3Client(s3config);

        try {
            return addOrUpdateResourcesInternal(s3config, s3, workArea, files, removeFilesFromWorkArea);
        } catch (AmazonServiceException ase) {
            if ("NoSuchBucket".equals(ase.getErrorCode())) {
                s3.createBucket(s3config.getDefaultBucketName());
                return addOrUpdateResourcesInternal(s3config, s3, workArea, files, removeFilesFromWorkArea);
            } else {
                throw new RuntimeException(ase);
            }
        }
    }

    protected List<String> addOrUpdateResourcesInternal(S3Configuration s3config, AmazonS3 s3, FileWorkArea workArea, List<File> files, boolean removeFilesFromWorkArea) {
        List<String> resourcePaths = new ArrayList<>();
        for (File srcFile : files) {
            if (!srcFile.getAbsolutePath().startsWith(workArea.getFilePathLocation())) {
                throw new FileServiceException("Attempt to update file " + srcFile.getAbsolutePath() +
                        " that is not in the passed in WorkArea " + workArea.getFilePathLocation());
            }

            String fileName = srcFile.getAbsolutePath().substring(workArea.getFilePathLocation().length());
            String resourceName = buildResourceName(fileName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(s3config.getDefaultBucketName(), resourceName, srcFile);

            // maybe put a handler here instead.
            // If server-side encryption is enabled
            if(s3config.getEnableSSE()) {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
                putObjectRequest.setMetadata(objectMetadata);
            }

            s3.putObject(putObjectRequest);
            resourcePaths.add(fileName);
        }
        return resourcePaths;
    }

    @Override
    public boolean removeResource(String name) {
        S3Configuration s3config = s3ConfigurationService.lookupS3Configuration();
        AmazonS3 s3 = getAmazonS3Client(s3config);
        s3.deleteObject(s3config.getDefaultBucketName(), buildResourceName(name));

        File returnFile = blFileService.getLocalResource(buildResourceName(name));
        if (returnFile != null) {
            returnFile.delete();
        }
        return true;
    }

    /**
     * hook for overriding name used for resource in S3
     * @param name
     * @return
     */
    protected String buildResourceName(String name) {
        // Strip the starting slash to prevent empty directories in S3 as well as required references by // in the
        // public S3 URL
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        String baseDirectory = s3ConfigurationService.lookupS3Configuration().getBucketSubDirectory();
        if (StringUtils.isNotEmpty(baseDirectory)) {
            if (baseDirectory.startsWith("/")) {
                baseDirectory = baseDirectory.substring(1);
            }
        } else {
            // ensure subDirectory is non-null
            baseDirectory = "";
        }

        String siteSpecificResourceName = getSiteSpecificResourceName(name);
        String concat = FilenameUtils.concat(baseDirectory, siteSpecificResourceName);
        return concat.replaceAll("\\\\","/");
    }

    protected String getSiteSpecificResourceName(String resourceName) {
        BroadleafRequestContext brc = BroadleafRequestContext.getBroadleafRequestContext();
        if (brc != null) {
            Site site = brc.getNonPersistentSite();
            if (site != null) {
                String siteDirectory = getSiteDirectory(site);
                if (resourceName.startsWith("/")) {
                    resourceName = resourceName.substring(1);
                }
                return FilenameUtils.concat(siteDirectory, resourceName);
            }
        }

        return resourceName;
    }

    protected String getSiteDirectory(Site site) {
        return getSiteDirectory(site.getId());
    }

    protected String getSiteDirectory(Long id) {
        String siteDirectory = SITE_PREFIX + id.toString();
        return siteDirectory;
    }

    protected AmazonS3 getAmazonS3Client(S3Configuration s3config) {
        AmazonS3 client = configClientMap.get(s3config);
        if (client == null) {
            client = getAmazonS3ClientFromConfiguration(s3config);
            configClientMap.put(s3config, client);
        }
        return client;
    }

    /**
     * Creates an instance of the S3 Client based on the 'use instance profile' property.
     * If it exists, it retrieves credentials from the IAM role (valid only on EC2 instances).
     * Otherwise, a secret key and access key ID must be provided.
     * @param s3config Configuration object
     * @return an authenticated AmazonS3Client
     */
    protected AmazonS3 getAmazonS3ClientFromConfiguration(S3Configuration s3config) {
        AmazonS3ClientBuilder builder;

        if(s3config.getUseInstanceProfileCredentials()) {
            builder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new InstanceProfileCredentialsProvider(false));
        } else {
            builder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(getAWSCredentials(s3config)));
        }

        builder.setRegion(s3config.getDefaultBucketRegion());

        return builder.build();
    }


    protected AWSCredentials getAWSCredentials(final S3Configuration s3configParam) {
        return new AWSCredentials() {

            private final S3Configuration s3ConfigVar = s3configParam;

            @Override
            public String getAWSSecretKey() {
                return s3ConfigVar.getAwsSecretKey();
            }

            @Override
            public String getAWSAccessKeyId() {
                return s3ConfigVar.getGetAWSAccessKeyId();
            }
        };
    }

    public void setBroadleafFileService(BroadleafFileService bfs) {
        this.blFileService = bfs;
    }

    public void setConcurrentFileOutputStream(ConcurrentFileOutputStream concurrentFileOutputStream) {
        this.concurrentFileOutputStream = concurrentFileOutputStream;
    }

    /**
     * Handles the resource gathering, starts as regular retrieval, if it fails (and the conditions allow it) it will try to retrieve the image from the parent site
     */
    @Override
    public File getResource(String name, FileApplicationType fileApplicationType) {
        File file = getResource(name, fileApplicationType, false);
        if (file.getPath().contains("this/path/should/not/exist/")  && isMultiTenantEnvironment()) {
            file = getResource(name, fileApplicationType, true);
        }
        return file;
    }

    /**
     * Builds the resourceName for it's parent.
     * @param name
     * @return
     */
    protected String buildResourceParentName(String name) {
        name= StringUtils.removeStart(name, "/");

        String baseDirectory = s3ConfigurationService.lookupS3Configuration().getBucketSubDirectory();
        if (StringUtils.isNotEmpty(baseDirectory)) {
            baseDirectory= StringUtils.removeStart(baseDirectory, "/");
        } else {
            // ensure subDirectory is non-null
            baseDirectory = "";
        }

        String siteSpecificResourceName = getMultiTenantSiteSpecificResourceNameParent(name);
        return FilenameUtils.concat(baseDirectory, siteSpecificResourceName);
    }


    /**
     * Check if the module is call with MultiTenant module
     * @return
     */
    protected boolean isMultiTenantEnvironment() {
        return ClassUtils.isPresent(MULTITENANT_SITE_CLASSNAME ,getClass().getClassLoader());
    }

    /**
     * Get MultiTenantSite Class by Reflection 
     * @return
     * @throws Throwable 
     */
    protected Class<?> getMultiTenantClass() throws ClassNotFoundException, LinkageError  {
        try {
            Class<?> c = ClassUtils.forName(MULTITENANT_SITE_CLASSNAME, getClass().getClassLoader());
            return c;
        } catch (ClassNotFoundException | LinkageError e) {
            throw e;

        }
    }


    /**
     * Gets getParentId method by reflection   
     * @return
     * @throws NoSuchMethodException, SecurityException,ClassNotFoundException, LinkageError 
     */
    protected Method getMultiTenantGetParentSiteIdMethod() throws NoSuchMethodException, SecurityException,ClassNotFoundException, LinkageError {
        try {
            return getMultiTenantClass().getMethod(MULTITENANTSITE_GETPARENTID_METHODNAME);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | LinkageError  e) {
            // TODO Auto-generated catch block
            throw e;
        }
    }


    /**
     * Reflection getParentSiteId Invocation 
     * @param site
     * @return
     */
    protected Long invokeMultiTenantGetParentSiteIdMethod(Object site) {
        Long result = 0L;
        try {
            String parentSiteId =ReflectionUtils.invokeMethod(getMultiTenantGetParentSiteIdMethod(), site).toString();
            if (parentSiteId!=null) {
                result =Long.parseLong(parentSiteId);
            }        
            return result;
        } catch ( SecurityException | IllegalArgumentException | NoSuchMethodException | ClassNotFoundException | LinkageError e) {
            LOG.error("Problem invoking getParentSiteId method from reflected Multitenant class", e);
            return 0L;

        }
        
    }


    /**
     * Get the parent from a MultiTentantSite.
     * The MultiTenantSite environment is detected by reflection.
     * @param resourceName
     * @return
     */
    protected String getMultiTenantSiteSpecificResourceNameParent(String resourceName) {
        BroadleafRequestContext brc = BroadleafRequestContext.getBroadleafRequestContext();
        if (brc != null) {
            //getMultiTenantClass retrieves the class by reflection, the casts.
            Object site="";
            try {
                site = (getMultiTenantClass().cast(brc.getNonPersistentSite()));
                String siteDirectory = getSiteDirectory((invokeMultiTenantGetParentSiteIdMethod(site)));
                return FilenameUtils.concat(siteDirectory, resourceName);
            } catch (ClassNotFoundException | LinkageError e) {
                //The MultitenantEnvironment should be checked before reaching this point so
                //this exception should never occur if the MultiTenant environment is ok
               LOG.error("Problem reflecting Multitenant Class", e);
            }
          }
        return resourceName;
    }

}
