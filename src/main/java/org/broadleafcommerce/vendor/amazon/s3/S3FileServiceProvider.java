package org.broadleafcommerce.vendor.amazon.s3;

import org.broadleafcommerce.common.file.domain.FileWorkArea;
import org.broadleafcommerce.common.file.service.FileServiceProvider;
import org.broadleafcommerce.common.file.service.type.FileApplicationType;

import com.amazonaws.auth.AWSCredentials;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class S3FileServiceProvider implements FileServiceProvider {

    @Override
    public File getResource(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getResource(String name, FileApplicationType fileApplicationType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOrUpdateResources(FileWorkArea workArea, List<File> files) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean removeResource(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    protected AWSCredentials getAWSCredentials() throws IOException {
        return new AWSCredentials() {

            @Override
            public String getAWSSecretKey() {
                return findProperty("aws.s3.secretKey");
            }

            @Override
            public String getAWSAccessKeyId() {
                return findProperty("aws.s3.accessKeyId");
            }
        };
    }

}
