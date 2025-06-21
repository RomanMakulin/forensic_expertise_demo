package com.example.adminservice.service.impl;

import com.example.adminservice.integration.minio.MinioIntegration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileFileServiceImplTest {

    @Mock
    private MinioIntegration minioIntegration;

    @InjectMocks
    private ProfileFileServiceImpl profileFileServiceImpl;

    @Test
    void getFileLink() {
        String expected = "http://test-link";
        when(minioIntegration.getFileRequest("test", "test", "test", true, String.class))
                .thenReturn(expected);

        String result = profileFileServiceImpl.getFileLink("test", "test", "test");

        assert result.equals(expected);
    }

    @Test
    void getFileBytes() {
        byte[] expected = {1, 2, 3, 4};
        when(minioIntegration.getFileRequest("test", "test", "test", false, byte[].class))
                .thenReturn(expected);

        byte[] result = profileFileServiceImpl.getFileBytes("test", "test", "test");

        assert Arrays.equals(result, expected);
    }

    @Test
    void deleteFile() {
        minioIntegration.deleteFileRequest("fileName", "fileExtension", "fileBucket");
        verify(minioIntegration).deleteFileRequest("fileName", "fileExtension", "fileBucket");
    }
}