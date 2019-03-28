/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Identification Cards.
 *
 * FenixEdu Identification Cards is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Identification Cards is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Identification Cards.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.idcards.domain;

import java.util.Collection;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.IdCardsConfiguration;

import pt.ist.fenixframework.Atomic;

public class SantanderSequenceNumberGenerator extends SantanderSequenceNumberGenerator_Base {

    private SantanderSequenceNumberGenerator() {
        super();
        setRootDomainObject(Bennu.getInstance());
        setSequenceNumber(0);
        setPhotoSequenceNumber(0);
    }

    private static SantanderSequenceNumberGenerator getInstance() {
        final Collection<SantanderSequenceNumberGenerator> instances =
                Bennu.getInstance().getSantanderSequenceNumberGeneratorsSet();
        if (instances.size() > 1) {
            throw new DomainException("santanderSequenceNumberGenerator.more.than.one.instances.exist");
        }
        return instances.size() == 0 ? new SantanderSequenceNumberGenerator() : instances.iterator().next();
    }

    public static int getNewSequenceNumber() {
        Integer seqNumber = getInstance().getSequenceNumber();
        getInstance().setSequenceNumber(++seqNumber);
        return seqNumber;
    }

    public static int getNewPhotoSequenceNumber() {
        Integer seqNumber = getInstance().getPhotoSequenceNumber();
        getInstance().setPhotoSequenceNumber(++seqNumber);
        return seqNumber;
    }

    private static String generatePIN(int numberOfDigits) {
        int min = 1;
        int max = ((int) Math.pow(10, numberOfDigits)) - 1;
        int pin = min + (int) (Math.random() * ((max - min) + 1));
        return String.format("%0" + numberOfDigits + "d", pin);
    }

    @Atomic
    public static SantanderPIN generateSantanderPIN(Person person) {
        String pin = generatePIN(4);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
            byte[] secret = IdCardsConfiguration.getConfiguration().appInstitutionAES128SecretKey().getBytes("UTF-8");
            SecretKey key = new SecretKeySpec(secret, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] cipheredPIN = cipher.doFinal(pin.getBytes("UTF-8"));
            return new SantanderPIN(person, cipheredPIN);
        } catch (Exception e) {
            throw new DomainException("santanderPINGenerator.errorCipheringPIN", e);
        }
    }

    public static String decodeSantanderPIN(SantanderPIN santanderPIN) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
            byte[] secret = IdCardsConfiguration.getConfiguration().appInstitutionAES128SecretKey().getBytes("UTF-8");
            SecretKey key = new SecretKeySpec(secret, "AES");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] decipheredPIN = cipher.doFinal(santanderPIN.getEncryptedPINAsByteArray());
            return new String(decipheredPIN, "UTF-8");
        } catch (Exception e) {
            throw new DomainException("santanderPINGenerator.errorDecipheringPIN", e);
        }
    }
}
