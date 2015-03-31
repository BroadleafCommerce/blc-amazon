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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.file.domain.FileWorkArea;
import org.broadleafcommerce.common.file.service.BroadleafFileServiceExtensionManager;
import org.broadleafcommerce.common.file.service.BroadleafFileServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This test will connect to AmazonS3, create a bucket, create a file, delete the file, and delete the bucket.
 * 
 * This test requires an S3 keys and S3 accessId which can be passed in as -D arguments or stored 
 * in a property file in your classpath named.   
 * 
 * "/config/bc/override/common.properties."
 * @author bpolster
 *
 */
public class S3FileServiceProviderTest extends AbstractS3Test {

    private static S3FileServiceProvider s3FileProvider = new S3FileServiceProvider();
    
    private static final String TEST_FILE_CONTENTS = "abcdefghijklmnopqrstuvwxyz\n"
                                                    + "01234567890112345678901234\n"
                                                    + "!@#$%^&*()-=[]{};':',.<>/?\n"
                                                    + "01234567890112345678901234\n"
                                                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public class S3BroadleafFileService extends BroadleafFileServiceImpl {
        public S3BroadleafFileService() {
            extensionManager = new BroadleafFileServiceExtensionManager();
        }
    }
    
    @BeforeClass
    public static void setupProvider() {
        s3FileProvider.s3ConfigurationService = configService;
        s3FileProvider.setBroadleafFileService(new S3FileServiceProviderTest().new S3BroadleafFileService());
    }

    @Test
    public void testFileProcesses() throws IOException {
        resetAllProperties();
        //AbstractS3Test.propService.setProperty("aws.s3.bucketSubDirectory", "img");
        String filename = "blcTestFile.txt";
        boolean ok = uploadTestFile(filename);
        assertTrue("File added to s3 with no exception.", ok);

        ok = checkTestFileExists(filename);
        assertTrue("File retrieved from s3 with no exception.", ok);

        ok = deleteTestFile(filename);
        
        // The file should not exist on S3
        ok = !checkTestFileExists(filename);
        assertTrue("File removed from s3 with no exception.", ok);
    }

    @Test
    public void testSubDirectory() throws IOException {
        resetAllProperties();
        String filename = "blcTestFile.txt";
        String subDirectory = "img";
        verifyFileUploadRaw(filename, subDirectory);
    }
  
    @Test
    public void testSubDirectoryWithSlashes() throws IOException {
        resetAllProperties();
        String filename = "/blcTestFile.txt";
        String subDirectory = "/img/";
        verifyFileUploadRaw(filename, subDirectory);
    }
    
    public void testSubDirectoryTree() throws IOException {
        resetAllProperties();
        String filename = "/blcTestFile.txt";
        String subDirectory = "/img/sub1/sub2";
        verifyFileUploadRaw(filename, subDirectory);
    }
    
    /**
     * Differs from {@link #checkTestFileExists(String)} in that this uses the S3 client directly and does
     * not go through the Broadleaf file service API. This will create a test file, upload it to S3 via the
     * Broadleaf file service API, verify that the file exists via the raw S3 client, and then delete the file
     * from the buck via the file service API again
     * 
     * @param filename the name of the file to upload
     * @param directoryName directory that the file should be stored in on S3
     */
    protected void verifyFileUploadRaw(String filename, String directoryName) throws IOException {
        propService.setProperty("aws.s3.bucketSubDirectory", directoryName);
        
        boolean ok = uploadTestFile(filename);
        assertTrue("File added to s3 with no exception.", ok);
        
        // Use the S3 client directly to ensure that it was uploaded to the sub-directory
        S3Configuration s3config = configService.lookupS3Configuration();
        AmazonS3Client s3 = s3FileProvider.getAmazonS3Client(s3config);
        s3.setRegion(RegionUtils.getRegion(s3config.getDefaultBucketRegion()));
        String s3Key = filename;
        if (StringUtils.isNotEmpty(directoryName)) {
            s3Key = directoryName + "/" + filename;
        }
        
        // Replace the starting slash and remove the double-slashes if the directory ended with a slash
        s3Key = s3Key.startsWith("/") ? s3Key.substring(1) : s3Key;
        s3Key = FilenameUtils.normalize(s3Key);
        S3Object object = s3.getObject(new GetObjectRequest(s3config.getDefaultBucketName(), s3Key));
      
        InputStream inputStream = object.getObjectContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String fileContents = writer.toString();
        inputStream.close();
        writer.close();
        
        assertEquals("Retrieved the file successfully from S3", fileContents, TEST_FILE_CONTENTS);
        
        ok = deleteTestFile(filename);
        assertTrue("File removed from s3 with no exception.", ok);
    }
    
    protected boolean deleteTestFile(String filename) {
        boolean ok;
        try {
            s3FileProvider.removeResource(filename);
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }

    protected boolean checkTestFileExists(String filename) {
        boolean ok = false;
        try {
            File f = s3FileProvider.getResource(filename);
            if (f.exists()) {
                String content = new Scanner(f).useDelimiter("\\Z").next();
                int contentLength = content.length();
                if (contentLength > 10) {
                    System.out.println("Returned file contents: " + content);
                    ok = TEST_FILE_CONTENTS.equals(content);
                }
            }
        } catch (Exception e) {
            ok = false;
        }
        return ok;
    }

    protected boolean uploadTestFile(String filename) {
        boolean ok;
        try {

            // Add the file to the amazon bucket.
            List<File> files = new ArrayList<File>();
            File sampleFile = createSampleFile(filename);
            FileWorkArea workArea = new FileWorkArea();
            File parentFile = sampleFile.getAbsoluteFile().getParentFile();
            workArea.setFilePathLocation(parentFile.getAbsolutePath());
            files.add(sampleFile);
            s3FileProvider.addOrUpdateResources(workArea, files, false);
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }
    
    private static File createSampleFile(String fileName) throws IOException {
        File file = new File(fileName.startsWith("/") ? fileName.substring(1) : fileName);
        file.deleteOnExit();
        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(TEST_FILE_CONTENTS);
        writer.close();
        return file;
    }

}
