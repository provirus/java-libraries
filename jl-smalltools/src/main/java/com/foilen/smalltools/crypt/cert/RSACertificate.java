/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.cert;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Date;

import org.spongycastle.asn1.x500.AttributeTypeAndValue;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.MiscPEMGenerator;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.ContentVerifierProvider;
import org.spongycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.spongycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemObjectGenerator;
import org.spongycastle.util.io.pem.PemReader;
import org.spongycastle.util.io.pem.PemWriter;

import com.foilen.smalltools.crypt.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.asymmetric.RSAKeyDetails;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.hash.HashSha1;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.FileTools;

/**
 * To create self-signed certificates and to sign other certificates.
 * 
 * <pre>
 * Usage:
 * 
 * // Root
 * AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
 * RSACertificate rootCertificate = new RSACertificate(rootKeys);
 * rootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));
 * 
 * // Node
 * AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
 * RSACertificate nodeCertificate = rootCertificate.signPublicKey(nodeKeys, new CertificateDetails().setCommonName("p001.node.foilen.org"));
 * 
 * // Fake Root
 * AsymmetricKeys fakeRootKeys = rsaCrypt.generateKeyPair(2048);
 * RSACertificate fakeRootCertificate = new RSACertificate(fakeRootKeys);
 * fakeRootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));
 * 
 * // Assert certificates
 * Assert.assertTrue(rootCertificate.isValidSignature(rootCertificate));
 * Assert.assertTrue(nodeCertificate.isValidSignature(rootCertificate));
 * Assert.assertTrue(fakeRootCertificate.isValidSignature(fakeRootCertificate));
 * 
 * Assert.assertFalse(rootCertificate.isValidSignature(nodeCertificate));
 * Assert.assertFalse(rootCertificate.isValidSignature(fakeRootCertificate));
 * Assert.assertFalse(nodeCertificate.isValidSignature(nodeCertificate));
 * Assert.assertFalse(nodeCertificate.isValidSignature(fakeRootCertificate));
 * Assert.assertFalse(fakeRootCertificate.isValidSignature(rootCertificate));
 * Assert.assertFalse(fakeRootCertificate.isValidSignature(nodeCertificate));
 * </pre>
 * 
 * <pre>
 * Dependencies:
 * compile 'com.madgag.spongycastle:prov:1.51.0.0'
 * compile 'com.madgag.spongycastle:pkix:1.51.0.0'
 * compile 'com.madgag.spongycastle:pg:1.51.0.0'
 * </pre>
 */
public class RSACertificate {

    static {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        if (Security.getProvider(provider.getName()) == null) {
            Security.addProvider(provider);
        }
    }

    private static String OID_COMMON_NAME = "2.5.4.3";
    private static RSACrypt rsaCrypt = new RSACrypt();

    /**
     * Load the certificate and keys (if present in the file).
     * 
     * @param fileName
     *            the full path of the file
     * @return the certificate
     */
    public static RSACertificate loadPemFromFile(String fileName) {
        String pem = FileTools.getFileAsString(fileName);
        return loadPemFromString(pem);
    }

    /**
     * Load the certificate and keys (if present in the string).
     * 
     * @param pem
     *            the pem
     * @return the certificate
     */
    public static RSACertificate loadPemFromString(String pem) {
        RSACertificate certificate = new RSACertificate();
        PemReader pemReader = null;
        try {
            // Keys if present
            certificate.keysForSigning = rsaCrypt.loadKeysPemFromString(pem);

            // Certificate
            pemReader = new PemReader(new StringReader(pem));
            PemObject pemObject;
            while ((pemObject = pemReader.readPemObject()) != null) {
                if ("CERTIFICATE".equals(pemObject.getType())) {
                    certificate.certificateHolder = new X509CertificateHolder(pemObject.getContent());
                }
            }

            return certificate;
        } catch (Exception e) {
            throw new SmallToolsException("Problem loading the certificate", e);
        } finally {
            CloseableTools.close(pemReader);
        }

    }

    private X509CertificateHolder certificateHolder;

    private AsymmetricKeys keysForSigning;

    public RSACertificate() {
    }

    public RSACertificate(AsymmetricKeys keysForSigning) {
        this.keysForSigning = keysForSigning;
    }

    public RSACertificate(X509CertificateHolder certificateHolder) {
        this.certificateHolder = certificateHolder;
    }

    public RSACertificate(X509CertificateHolder certificateHolder, AsymmetricKeys keysForSigning) {
        this.certificateHolder = certificateHolder;
        this.keysForSigning = keysForSigning;
    }

    public X509CertificateHolder getCertificateHolder() {
        return certificateHolder;
    }

    /**
     * Get the certificate's common name.
     * 
     * @return the common name
     */
    public String getCommonName() {
        AssertTools.assertNotNull(certificateHolder, "The certificate is not set");
        X500Name subject = certificateHolder.getSubject();
        for (RDN rdn : subject.getRDNs()) {
            AttributeTypeAndValue first = rdn.getFirst();
            if (OID_COMMON_NAME.equals(first.getType().toString())) {
                return first.getValue().toString();
            }
        }
        return null;
    }

    public AsymmetricKeys getKeysForSigning() {
        return keysForSigning;
    }

    /**
     * Compute the SHA1 thumbprint.
     * 
     * @return the SHA1 thumbprint
     */
    public String getThumbprint() {
        AssertTools.assertNotNull(certificateHolder, "The certificate is not set");
        try {
            return HashSha1.hashBytes(certificateHolder.getEncoded());
        } catch (IOException e) {
            throw new SmallToolsException("Problem getting the thumbprint", e);
        }
    }

    /**
     * Check if the current time is in the certificate dates range.
     * 
     * @return true if valid
     */
    public boolean isValidDate() {
        return isValidDate(new Date());
    }

    /**
     * Check if the specified time is in the certificate dates range.
     * 
     * @param date
     *            the time to check
     * @return true if valid
     */
    public boolean isValidDate(Date date) {
        AssertTools.assertNotNull(certificateHolder, "The certificate is not set");
        return DateTools.isAfter(date, certificateHolder.getNotBefore()) && DateTools.isBefore(date, certificateHolder.getNotAfter());
    }

    /**
     * Check if the certificate was signed by the specified public key.
     * 
     * @param signerPublicKey
     *            the signer's public key
     * @return true if signed by it
     */
    public boolean isValidSignature(AsymmetricKeyParameter signerPublicKey) {
        try {
            ContentVerifierProvider verifierProvider = new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(signerPublicKey);
            return certificateHolder.isSignatureValid(verifierProvider);
        } catch (Exception e) {
            throw new SmallToolsException("Problem validating the certificate", e);
        }
    }

    /**
     * Check if the certificate was signed by the specified public key.
     * 
     * @param signerPublicKey
     *            the signer's pair of keys that contains the public key
     * @return true if signed by it
     */
    public boolean isValidSignature(AsymmetricKeys signerPublicKey) {
        try {
            ContentVerifierProvider verifierProvider = new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(signerPublicKey.getPublicKey());
            return certificateHolder.isSignatureValid(verifierProvider);
        } catch (Exception e) {
            throw new SmallToolsException("Problem validating the certificate", e);
        }
    }

    /**
     * Check if the certificate was signed by the specified certificate.
     * 
     * @param signerCertificate
     *            the signer's certificate
     * @return true if signed by it
     */
    public boolean isValidSignature(RSACertificate signerCertificate) {
        try {
            ContentVerifierProvider verifierProvider = new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(signerCertificate.certificateHolder);
            return certificateHolder.isSignatureValid(verifierProvider);
        } catch (Exception e) {
            throw new SmallToolsException("Problem validating the certificate", e);
        }
    }

    /**
     * Save the certificate in a PEM file.
     * 
     * @param fileName
     *            the full path to the file
     */
    public void saveCertificatePem(String fileName) {
        try {
            saveCertificatePem(new FileWriter(fileName));
        } catch (IOException e) {
            throw new SmallToolsException("Could not save cert", e);
        }
    }

    /**
     * Save the certificate in a PEM writer.
     * 
     * @param writer
     *            the writer. Will be closed at the end
     */
    public void saveCertificatePem(Writer writer) {
        AssertTools.assertNotNull(certificateHolder, "The certificate is not set");
        PemWriter pemWriter = null;
        try {
            pemWriter = new PemWriter(writer);
            PemObjectGenerator pemObjectGenerator = new MiscPEMGenerator(certificateHolder);
            pemWriter.writeObject(pemObjectGenerator);
        } catch (Exception e) {
            throw new SmallToolsException("Could not save cert", e);
        } finally {
            CloseableTools.close(pemWriter);
        }
    }

    /**
     * Save the certificate in a PEM String.
     * 
     * @return the pem
     */
    public String saveCertificatePemAsString() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        saveCertificatePem(new OutputStreamWriter(result));
        return result.toString();
    }

    /**
     * Sign the {@link #setKeysForSigning(AsymmetricKeys)} with itself and put it in certificateHolder.
     * 
     * @param certificateDetails
     *            some information to store in the certificate
     * @return this
     */
    public RSACertificate selfSign(CertificateDetails certificateDetails) {

        AssertTools.assertNotNull(keysForSigning, "The keysForSigning is not set");
        AssertTools.assertNull(certificateHolder, "The certificate already exists");

        try {
            RSAKeyDetails keyDetails = rsaCrypt.retrieveKeyDetails(keysForSigning);
            PrivateKey privKey = keyDetails.getJcaPrivateKey();
            PublicKey publicKey = keyDetails.getJcaPublicKey();
            ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider("SC").build(privKey);
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());

            Date startDate = certificateDetails.getStartDate();
            Date endDate = certificateDetails.getEndDate();
            BigInteger serial = certificateDetails.getSerial();

            X500Name issuer = new X500Name("CN=" + certificateDetails.getCommonName());

            X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuer, serial, startDate, endDate, issuer, subPubKeyInfo);
            certificateHolder = certificateBuilder.build(sigGen);

            return this;
        } catch (Exception e) {
            throw new SmallToolsException("Problem signing the key", e);
        }
    }

    public RSACertificate setCertificateHolder(X509CertificateHolder certificateHolder) {
        this.certificateHolder = certificateHolder;
        return this;
    }

    public RSACertificate setKeysForSigning(AsymmetricKeys keysForSigning) {
        this.keysForSigning = keysForSigning;
        return this;
    }

    /**
     * Sign another public key.
     * 
     * @param publicKeyToSign
     *            the public key to sign
     * @param certificateDetails
     *            some information to store in the certificate
     * @return the new certificate
     */
    public RSACertificate signPublicKey(AsymmetricKeys publicKeyToSign, CertificateDetails certificateDetails) {
        try {
            PrivateKey privKey = rsaCrypt.retrieveKeyDetails(keysForSigning).getJcaPrivateKey();
            PublicKey publicKey = rsaCrypt.retrieveKeyDetails(publicKeyToSign).getJcaPublicKey();
            ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider("SC").build(privKey);
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());

            Date startDate = certificateDetails.getStartDate();
            Date endDate = certificateDetails.getEndDate();
            BigInteger serial = certificateDetails.getSerial();

            X500Name issuer = new X500Name("CN=" + getCommonName());
            X500Name subject = new X500Name("CN=" + certificateDetails.getCommonName());

            X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuer, serial, startDate, endDate, subject, subPubKeyInfo);
            X509CertificateHolder newCert = certificateBuilder.build(sigGen);

            return new RSACertificate(newCert, publicKeyToSign);
        } catch (Exception e) {
            throw new SmallToolsException("Problem signing the key", e);
        }
    }

}