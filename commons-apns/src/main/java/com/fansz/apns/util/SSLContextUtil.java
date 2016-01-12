package com.fansz.apns.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSL工具类
 */
public class SSLContextUtil {

    private static final String PROTOCOL = "TLS";

    private static final String DEFAULT_ALGORITHM = "SunX509";

    private static final Logger log = LoggerFactory.getLogger(SSLContextUtil.class);

    /**
     * 创建SSLContext
     * 
     * @param pathToPKCS12File PKCS12文件路径
     * @param keystorePassword PKCS12文件密码
     * @return an SSL context
     */
    public static SSLContext createDefaultSSLContext(final String pathToPKCS12File, final String keystorePassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException,
            KeyManagementException, IOException {
        final FileInputStream keystoreInputStream = new FileInputStream(pathToPKCS12File);
        try {
            return createDefaultSSLContext(keystoreInputStream, keystorePassword);
        }
        finally {
            try {
                keystoreInputStream.close();
            }
            catch (IOException e) {
                log.error("Failed to close keystore input stream.", e);
            }
        }
    }

    public static SSLContext createDefaultSSLContext(final InputStream keystoreInputStream,
            final String keystorePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException, KeyManagementException, IOException {
        final KeyStore keyStore = KeyStore.getInstance("PKCS12");
        final char[] password = keystorePassword != null ? keystorePassword.toCharArray() : null;

        keyStore.load(keystoreInputStream, password);

        return createDefaultSSLContext(keyStore, keystorePassword != null ? keystorePassword.toCharArray() : null);
    }

    public static SSLContext createDefaultSSLContext(final KeyStore keyStore, final char[] keyStorePassword)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");

        if (algorithm == null) {
            algorithm = DEFAULT_ALGORITHM;
        }

        if (keyStore.size() == 0) {
            throw new KeyStoreException(
                    "Keystore is empty; while this is legal for keystores in general, APNs clients must have at least one key.");
        }

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        trustManagerFactory.init((KeyStore)null);

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
        keyManagerFactory.init(keyStore, keyStorePassword);

        final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }
}
