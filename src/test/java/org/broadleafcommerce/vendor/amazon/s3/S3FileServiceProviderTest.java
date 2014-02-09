
package org.broadleafcommerce.vendor.amazon.s3;

import static org.junit.Assert.assertTrue;

import org.broadleafcommerce.common.file.domain.FileWorkArea;
import org.broadleafcommerce.common.file.service.BroadleafFileServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    @BeforeClass
    public static void setupProvider() {
        s3FileProvider.s3ConfigurationService = configService;
        s3FileProvider.setBroadleafFileService(new BroadleafFileServiceImpl());
    }
        
    @Test
    public void testFileProcesses() throws IOException {
        resetAllProperties();
        boolean ok;
        try {

            // Add the file to the amazon bucket.
            List<File> files = new ArrayList<File>();
            File sampleFile = createSampleFile("blcTestFile.txt");
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

        assertTrue("File added to s3 with no exception.", ok);

        try {
            File f = s3FileProvider.getResource("blcTestFile.txt");
            String content = new Scanner(f).useDelimiter("\\Z").next();
            int contentLength = content.length();
            if (contentLength > 10) { // TODO, exact check.        
                System.out.println("Returned file contents: " + content);
                ok = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }

        assertTrue("File retreived from s3 with no exception.", ok);

        try {
            s3FileProvider.removeResource("blcTestFile.txt");
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }

        assertTrue("File removed from s3 with no exception.", ok);
    }

    private static File createSampleFile(String fileName) throws IOException {
        File file = new File(fileName);
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("01234567890112345678901234\n");
        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
        writer.write("01234567890112345678901234\n");
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        return file;
    }

}
