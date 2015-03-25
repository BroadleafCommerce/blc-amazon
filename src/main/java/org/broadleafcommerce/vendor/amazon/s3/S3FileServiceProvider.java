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

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.file.FileServiceException;
import org.broadleafcommerce.common.file.domain.FileWorkArea;
import org.broadleafcommerce.common.file.service.BroadleafFileService;
import org.broadleafcommerce.common.file.service.FileServiceProvider;
import org.broadleafcommerce.common.file.service.type.FileApplicationType;
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

    @Override
    public File getResource(String name) {
        return getResource(name, FileApplicationType.ALL);
    }

    @Override
    public File getResource(String name, FileApplicationType fileApplicationType) {
        File returnFile = blFileService.getLocalResource(name);
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            S3Configuration s3config = s3ConfigurationService.lookupS3Configuration();
            AmazonS3Client s3 = getAmazonS3Client(s3config);
            S3Object object = s3.getObject(new GetObjectRequest(s3config.getDefaultBucketName(), buildResourceName(name)));
            
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
                return returnFile;
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
            s3.putObject(new PutObjectRequest(s3config.getDefaultBucketName(), resourceName,
                    srcFile));
            resourcePaths.add(resourceName);
        }
        return resourcePaths;
    }    
    
    

    @Override
    public boolean removeResource(String name) {
        S3Configuration s3config = s3ConfigurationService.lookupS3Configuration();
        AmazonS3Client s3 = getAmazonS3Client(s3config);
        s3.deleteObject(s3config.getDefaultBucketName(), buildResourceName(name));
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
        String subDirectory = s3ConfigurationService.lookupS3Configuration().getBucketSubDirectory();
        if (StringUtils.isNotEmpty(subDirectory)) {
            if (subDirectory.startsWith("/")) {
                subDirectory = subDirectory.substring(1);
            }
            name = (subDirectory.endsWith("/")) ? (subDirectory + name) : (subDirectory + "/" + name);
        }
        
        return name;
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
