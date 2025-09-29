package com.spmorangle.config;

import com.spmorangle.crm.fileupload.service.StorageClientService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public AwsCredentialsProvider mockAwsCredentialsProvider() {
        return Mockito.mock(AwsCredentialsProvider.class);
    }

    @Bean
    @Primary
    public CognitoIdentityProviderClient mockCognitoIdentityProviderClient() {
        return Mockito.mock(CognitoIdentityProviderClient.class);
    }

    @Bean
    @Primary
    public JwtDecoder mockJwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }

    @Bean
    @Primary
    public StorageClientService mockStorageClientService() {
        return Mockito.mock(StorageClientService.class);
    }
}