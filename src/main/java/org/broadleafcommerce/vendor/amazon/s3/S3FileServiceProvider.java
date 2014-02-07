package org.broadleafcommerce.vendor.amazon.s3;

import org.broadleafcommerce.common.file.domain.FileWorkArea;
import org.broadleafcommerce.common.file.service.FileServiceProvider;
import org.broadleafcommerce.common.file.service.type.FileApplicationType;

import com.amazonaws.auth.AWSCredentials;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

public class S3FileServiceProvider implements FileServiceProvider {

    @Resource(name = "blS3ConfigurationService")
    protected S3ConfigurationService s3ConfigurationService;

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
                return s3ConfigurationService.lookupS3SecretKey();
            }

            @Override
            public String getAWSAccessKeyId() {
                return s3ConfigurationService.lookupS3AccessKeyId();
            }
        };
    }

}
