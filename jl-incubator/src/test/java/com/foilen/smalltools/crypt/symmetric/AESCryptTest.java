/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.symmetric;

import org.junit.Assert;

import com.foilen.smalltools.crypt.symmetric.AESCrypt;
import com.foilen.smalltools.crypt.symmetric.AESKeyDetails;
import com.foilen.smalltools.crypt.symmetric.SymmetricKey;

/**
 * Tests for {@link AESCrypt}.
 */
public class AESCryptTest extends AbstractSymmetricCryptTest<AESCrypt, AESKeyDetails> {

    public AESCryptTest() {
        super(128, new AESCrypt());
    }

    @Override
    protected void assertGeneratedKeyPairInternals(SymmetricKey key) {
        AESKeyDetails keyDetails = crypt.retrieveKeyDetails(key);
        Assert.assertNotNull(keyDetails.getKey());
    }

    @Override
    protected void testCreateKeyPairImpl() {
        // Generate a key
        SymmetricKey key = crypt.generateKey(keySize);
        AESKeyDetails keyDetails = crypt.retrieveKeyDetails(key);
        byte[] theKey = keyDetails.getKey();

        // Create a new key
        SymmetricKey createdKey = crypt.createKey(new AESKeyDetails(theKey));

        // Validate
        keyDetails = crypt.retrieveKeyDetails(createdKey);
        Assert.assertArrayEquals(theKey, keyDetails.getKey());
    }

}
