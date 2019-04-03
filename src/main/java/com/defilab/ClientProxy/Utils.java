package com.defilab.ClientProxy;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.defilab.sdk.entities.DefiClient;
import com.defilab.sdk.entities.DefiServer;

public class Utils {
    public static String getAccountName() throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        FileInputStream is = new FileInputStream("cert.pem");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
        return cert.getSubjectDN().getName().split(",")[0].replaceAll("CN=", "");
    }
    
    public static String getEnvOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }
        return value;
    }
    
    public static Object getDefiEntity(String accountName, String entityType) throws Exception {
        String account = String.format("%s@Defi", accountName);
        String channelEndpoint = getEnvOrDefault("CHANNEL_ENDPOINT", "channel.test.defilab.com");
        String postmanEndpoint = getEnvOrDefault("POSTMAN_ENDPOINT", "https://postman.test.defilab.com");
        String registryEndpoint = getEnvOrDefault("REGISTRY_ENDPOINT", "https://registry.test.defilab.com");
        String channelName = getEnvOrDefault("CHANNEL_NAME", "defi-exchange");
        if (entityType.equals("client")) {
            return new DefiClient(
                account,
                "creds/" + accountName + "/fabric_network.json",
                "creds/" + accountName + "/cert.pem",
                "creds/" + accountName + "/key.pem");
        } else {
            return new DefiServer(
                    account,
                    "creds/" + accountName + "/fabric_network.json",
                    "creds/" + accountName + "/cert.pem",
                    "creds/" + accountName + "/key.pem");
                    
        }
    }


}
