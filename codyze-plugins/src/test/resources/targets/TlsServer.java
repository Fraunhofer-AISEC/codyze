package de.fraunhofer.aisec.codyze.medina.demo.jsse;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;

public class TlsServer {

    private static final boolean DEBUG = true;
    private static final int PORT = 8443;

    private SSLServerSocket socket;

    private void configure(int port, String keystore, String keystorePwd) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException, NoSuchProviderException {
        // overview of functionality provided by BCJSSE
        if (DEBUG) {
            System.out.println("Services provided by BCJSSE security Provider");

            for (Provider.Service s : Security.getProvider("BCJSSE").getServices()) {
                System.out.println(s);
            }
            System.out.println();
        }

        // get default from most prioritized securtiy provider -> BCJSSE
        SSLContext sslCtx = SSLContext.getInstance("TLS", "BCJSSE");

        // initialize sslContext with a KeyManager and dummy TrustManager
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(keystore), keystorePwd.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePwd.toCharArray());
        TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                // Trust any client
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                // Trust any  server
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslCtx.init(kmf.getKeyManagers(), new TrustManager[]{tm}, null);

        // verify correct selection
        if(DEBUG) {
            System.out.println("Current provider:");
            System.out.println(sslCtx.getProvider());
            System.out.println();

            System.out.println("Selected protocol:");
            System.out.println(sslCtx.getProtocol());
            System.out.println();

            SSLParameters sslParams = sslCtx.getSupportedSSLParameters();

            System.out.println("Protocols:");
            Arrays.stream(sslParams.getProtocols()).forEach(System.out::println);
            System.out.println();

            System.out.println("Use cipher suites order: ");
            System.out.println(sslParams.getUseCipherSuitesOrder());
            System.out.println();

            System.out.println("Cipher suites:");
            Arrays.stream(sslParams.getCipherSuites()).forEach(System.out::println);
            System.out.println();
        }

        // create the SSLServerSocket
        SSLServerSocketFactory socketFactory = sslCtx.getServerSocketFactory();
        socket = (SSLServerSocket) socketFactory.createServerSocket(port);

        // set protocol versions and cipher suites
        socket.setEnabledProtocols(new String[]{
                "TLSv1.1", // FORBIDDEN
                "TLSv1.2"
        });
        socket.setEnabledCipherSuites(new String[]{
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_AES_128_CCM_SHA256", // FORBIDDEN
                "TLS_AES_128_GCM_SHA256", // FORBIDDEN
                "TLS_AES_256_GCM_SHA384"  // FORBIDDEN
        });

        if (DEBUG) {
            System.out.println("Enabled by the Socket:");

            System.out.println("Protocols:");
            Arrays.stream(socket.getEnabledProtocols()).forEach(System.out::println);
            System.out.println();

            System.out.println("Cipher suites:");
            Arrays.stream(socket.getEnabledCipherSuites()).forEach(System.out::println);
            System.out.println();
        }
    }

    private void start() {
        while(true) {
            try (Socket sock = socket.accept()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                while (in.readLine() != null) {
                    Runtime r = Runtime.getRuntime();
                    r.exec("/bin/sh " + in.readLine());
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // ensure Bouncy Castle is first provider queried when retrieving implementations
        Security.insertProviderAt(new BouncyCastleJsseProvider(), 1);
        Security.insertProviderAt(new BouncyCastleProvider(), 2);

        if (DEBUG) {
            System.out.println("Registered security providers:");

            Provider[] ps = Security.getProviders();
            for (int i = 0; i < ps.length; i++) {
                System.out.println(i + " : " + ps[i]);
            }
            System.out.println();
        }

        // try to get the path of the needed Keystore:
        File jks = null;
        // 1: absolute path via program argument
        if (args.length != 0) {
            jks = new File(args[0]);
        }
        // 2: absolute path via environment variable
        if (args.length == 0 || !jks.exists()) {
            String env = System.getenv("KEYSTORE_PATH");
            if (env != null)
                jks = new File(env);
            //3: load via class loader
            if (env == null || !jks.exists()) {
                // Throws a NPE if Keystore could not be found up until this point
                jks = new File(TlsServer.class.getClassLoader().getResource("keystore.jks").getPath());
            }
        }

        String keyStorePwd = Optional.ofNullable(System.getenv("KEYSTORE_PWD")).orElse("demo-password");

        // create TLS server
        TlsServer server = new TlsServer();
        try {
            // the keystore is expected to be generated with the script in the resource folder
            server.configure(PORT, jks.getAbsolutePath(), keyStorePwd);
        } catch (Exception e) {
            e.getLocalizedMessage();
            System.exit(1);
        }
        server.start();
    }
}
