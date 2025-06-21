package com.example.adminservice.service.impl;

import com.example.adminservice.dto.profile.ProfileDto;
import com.example.adminservice.dto.profile.original.OriginalProfileDto;
import com.example.adminservice.dto.profileCancel.ProfileCancelForProfile;
import com.example.adminservice.dto.profileCancel.ProfileCancelFromFront;
import com.example.adminservice.integration.mail.MailIntegration;
import com.example.adminservice.integration.profile.ProfileIntegration;
import com.example.adminservice.mapper.ProfileMapper;
import com.example.adminservice.service.ProfileFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private ProfileIntegration profileIntegration;
    @Mock
    private ProfileMapper profileMapper;
    @Mock
    private ProfileFileService profileFileService;
    @Mock
    private MailIntegration mailIntegration;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getUnverifiedProfiles() {
        List<OriginalProfileDto> originalProfiles = List.of(new OriginalProfileDto());
        List<ProfileDto> expectedDtos = List.of(new ProfileDto());

        when(profileIntegration.requestForUnverifiedProfiles()).thenReturn(originalProfiles);
        when(profileMapper.toProfileDtoList(originalProfiles)).thenReturn(expectedDtos);

        // when
        List<ProfileDto> result = adminService.getUnverifiedProfiles();

        // then
        assertEquals(expectedDtos, result);
        verify(profileIntegration).requestForUnverifiedProfiles();
        verify(profileMapper).toProfileDtoList(originalProfiles);
    }

    @Test
    void getAllProfiles() {
        List<OriginalProfileDto> originalProfiles = List.of(new OriginalProfileDto());
        List<ProfileDto> expectedDtos = List.of(new ProfileDto());

        when(profileIntegration.requestForAllProfiles()).thenReturn(originalProfiles);
        when(profileMapper.toProfileDtoList(originalProfiles)).thenReturn(expectedDtos);

        // when
        List<ProfileDto> result = adminService.getAllProfiles();

        // then
        assertEquals(expectedDtos, result);
        verify(profileIntegration).requestForAllProfiles();
        verify(profileMapper).toProfileDtoList(originalProfiles);
    }

    @Test
    void verifyProfile() {
        String profileId = "test-id";

        // when
        adminService.verifyProfile(profileId);

        // then
        verify(profileIntegration).requestForVerifyProfile(profileId);
    }

    @Test
    void cancelValidationProfile() {
        // given
        ProfileCancelFromFront profileCancelFromFront = new ProfileCancelFromFront();
        profileCancelFromFront.setProfileId(UUID.randomUUID().toString());
        profileCancelFromFront.setUserMail("test@example.com");
        profileCancelFromFront.setNeedDiplomaDelete(true);
        profileCancelFromFront.setNeedPassportDelete(true);

        // when
        adminService.cancelValidationProfile(profileCancelFromFront);

        // then
        verify(profileIntegration).requestForCancelVerifyProfile(any(ProfileCancelForProfile.class));
        verify(mailIntegration).sendMail(any());
    }

}