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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.extension.ExtensionResultHolder;
import org.broadleafcommerce.common.extension.ExtensionResultStatusType;
import org.broadleafcommerce.common.file.FileServiceException;
import org.broadleafcommerce.common.file.domain.FileWorkArea;
import org.broadleafcommerce.common.file.service.BroadleafFileService;
import org.broadleafcommerce.common.file.service.BroadleafFileServiceExtensionManager;
import org.broadleafcommerce.common.file.service.FileServiceProvider;
import org.broadleafcommerce.common.file.service.type.FileApplicationType;
import org.broadleafcommerce.common.site.domain.Site;
import org.broadleafcommerce.common.site.service.SiteService;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

@Service("blS3FileServiceProvider")
/**
 * Provides an Amazon S3 compatible implementation of the FileServiceProvider interface.
 *
 * Uses the <code>blS3ConfigurationService</code> component to provide the amazon connection details.   Once a 
 * resource is retrieved from Amazon, the resulting input stream is written to a File on the local file system using
 * <code>blFileService</code> to determine the local file path.
 *    
 * @author bpolster
 *
 */
public class S3FileServiceProvider implements FileServiceProvider {

    @Resource(name = "blS3ConfigurationService")
    protected S3ConfigurationService s3ConfigurationService;

    @Resource(name = "blFileService")
    protected BroadleafFileService blFileService;
    
    protected Map<S3Configuration, AmazonS3Client> configClientMap = new HashMap<S3Configuration, AmazonS3Client>();

    @Resource(name = "blBroadleafFileServiceExtensionManager")
    protected BroadleafFileServiceExtensionManager extensionManager;

    @Resource(name = "blSiteService")
    protected SiteService siteService;

    @Override
    public File getResource(String name) {
        return getResource(name, FileApplicationType.ALL);
    }

    @Override
    public File getResource(String name, FileApplicationType fileApplicationType) {
        File returnFile = blFileService.getLocalResource(getResourceName(name));
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            S3Configuration s3config = s3ConfigurationService.lookupS3Configuration();
            AmazonS3Client s3 = getAmazonS3Client(s3config);
            String resourceName = getResourceName(name);
            S3Object object = null;
            // If we could not resolve the resource locally, we will then attempt to find on the S3 server
            // using all possible resource names.
            if (StringUtils.isEmpty(resourceName)) {
                List<String> possibleResourceNames = getPossibleResourceNames(name);
                for (String path : possibleResourceNames) {
                    try {
                        object = s3.getObject(new GetObjectRequest(s3config.getDefaultBucketName(),path));
                        if (object != null) {
                            break;
                        }
                    }
                    catch (AmazonS3Exception s3Exception) {
                        if ("NoSuchKey".equals(s3Exception.getErrorCode())) {
                            continue;
                        } else {
                            throw s3Exception;
                        }
                    }
                }
            } else {
                object = s3.getObject(new GetObjectRequest(s3config.getDefaultBucketName(),resourceName));
            }

            if (object == null) {
                return new File("this/path/should/not/exist/" + UUID.randomUUID());
            }

            inputStream = object.getObjectContent();

            if (!returnFile.getParentFile().exists()) {
                if (!returnFile.getParentFile().mkdirs()) {
                    // Other thread could have created - check one more time.
                    if (!returnFile.getParentFile().exists()) {
                        throw new RuntimeException("Unable to create parent directories for file: " + name);
                    }
                }
            }

            outputStream = new FileOutputStream(returnFile);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
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
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error closing output stream while writing s3 file to file system", e);
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
        AmazonS3Client s3 = getAmazonS3Client(s3config);
        
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
    
    protected List<String> addOrUpdateResourcesInternal(S3Configuration s3config, AmazonS3Client s3, FileWorkArea workArea, List<File> files, boolean removeFilesFromWorkArea) {
        List<String> resourcePaths = new ArrayList<String>();
        for (File srcFile : files) {
            if (!srcFile.getAbsolutePath().startsWith(workArea.getFilePathLocation())) {
                throw new FileServiceException("Attempt to update file " + srcFile.getAbsolutePath() +
                        " that is not in the passed in WorkArea " + workArea.getFilePathLocation());
            }

            String fileName = srcFile.getAbsolutePath().substring(workArea.getFilePathLocation().length());
            String resourceName = buildResourceName(fileName);
            s3.putObject(new PutObjectRequest(s3config.getDefaultBucketName(), resourceName, srcFile));
            resourcePaths.add(fileName);
        }
        return resourcePaths;
    }    

    @Override
    public boolean removeResource(String name) {
        S3Configuration s3config = s3ConfigurationService.lookupS3Configuration();
        AmazonS3Client s3 = getAmazonS3Client(s3config);
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
        String resourceName = FilenameUtils.concat(baseDirectory, siteSpecificResourceName);

        return resourceName;
    }

    /**
     * hook for finding the resource name used for resource in S3
     * First we look for file on local fileSystem; If we can't find it locally,
     * then we make at most 2 calls API call to Amazon.
     * @param name
     * @return
     */
    public String getResourceName(String name) {
        // Strip the starting slash to prevent empty directories in S3 as well as required references by // in the
        // public S3 URL
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        String resourceName = null;
        String baseDirectory = s3ConfigurationService.lookupS3Configuration().getBucketSubDirectory();
        if (StringUtils.isNotEmpty(baseDirectory)) {
            if (baseDirectory.startsWith("/")) {
                baseDirectory = baseDirectory.substring(1);
            }
        } else {
            // ensure subDirectory is non-null
            baseDirectory = "";
        }

        ExtensionResultHolder<String> holder = new ExtensionResultHolder<String>();
        if (extensionManager != null) {
            ExtensionResultStatusType result = extensionManager.getProxy().processPathForSite(baseDirectory, name, holder);
            // First we look for file on local fileSystem.
            if (!ExtensionResultStatusType.NOT_HANDLED.equals(result)) {
                resourceName = holder.getResult();
            }
        }

        return resourceName;
    }

    /**
     * Helper method to build a list of possible variations of the resource path.
     * we will have to strip out any extraneous path data since this provider only
     * builds path names with a certain way. {@see S3FileServiceProvider#buildResourceName}
     * @param name
     * @return
     */
    protected List<String> getPossibleResourceNames(String name) {
        List<String> possibleNames = new ArrayList<>();
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

        ExtensionResultHolder<List<String>> holder = new ExtensionResultHolder<List<String>>();
        List<String> returnedNames = new ArrayList<>();
        if (extensionManager != null) {
            ExtensionResultStatusType result = extensionManager.getProxy().retrieveAllSitePaths(baseDirectory, name, holder);
            // return a list of all possible paths
            if (!ExtensionResultStatusType.NOT_HANDLED.equals(result)) {
                returnedNames = holder.getResult();
            }
        }

        for(String possibleName : returnedNames) {
            int prefixLocation = possibleName.indexOf(baseDirectory);
            String prefix = possibleName.substring(prefixLocation,prefixLocation + baseDirectory.length());
            int siteLocation = possibleName.lastIndexOf("site-");
            String siteURL  = possibleName.substring(siteLocation);
            possibleName =  FilenameUtils.concat(prefix, siteURL);
            possibleNames.add(possibleName);
        }

        return possibleNames;
    }

    /**
     * helper method to get the site specific resource-name.
     * @param resourceName
     * @param site
     * @return
     */
    protected String getSiteSpecificResourceName(String resourceName,Site site) {
        if (site != null) {
            String siteDirectory = getSiteDirectory(site);
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }
            return FilenameUtils.concat(siteDirectory, resourceName);
        }
        return resourceName;
    }

    @Deprecated
    protected String getSiteSpecificResourceName(String resourceName) {
        BroadleafRequestContext brc = BroadleafRequestContext.getBroadleafRequestContext();
        if (brc != null) {
            Site site = brc.getNonPersistentSite();
           return getSiteSpecificResourceName(resourceName,site);
        }
        return resourceName;
    }

    protected String getSiteDirectory(Site site) {
        String siteDirectory = "site-" + site.getId();
        return siteDirectory;
    }
    
    protected AmazonS3Client getAmazonS3Client(S3Configuration s3config) {
        AmazonS3Client client = configClientMap.get(s3config);
        if (client == null) {
            client = new AmazonS3Client(getAWSCredentials(s3config));
            client.setRegion(RegionUtils.getRegion(s3config.getDefaultBucketRegion()));
            if (s3config.getEndpointURI() != null) {
                client.setEndpoint(s3config.getEndpointURI());
            }
            configClientMap.put(s3config, client);
        }
        return client;
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

}
