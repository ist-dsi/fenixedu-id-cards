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
package org.fenixedu.idcards;

import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

public class IdCardsConfiguration {

    @ConfigurationManager(description = "Identification Cards Configuration")
    public interface ConfigurationProperties {

        @ConfigurationProperty(
                key = "sibs.webService.username",
                description = "UserName used to communicate with the SIBS Web Service, which returns the ID card production state.")
        public String sibsWebServiceUsername();

        @ConfigurationProperty(
                key = "sibs.webService.password",
                description = "Password used to communicate with the SIBS Web Service, which returns the ID card production state.")
        public String sibsWebServicePassword();

        @ConfigurationProperty(key = "app.institution.AES128.secretKey",
                description = "Secret for Institution ID card generation", defaultValue = "aa0bbfaf79654df4")
        public String appInstitutionAES128SecretKey();

        @ConfigurationProperty(key = "app.institution.PIN", description = "PIN for Institution ID card generation",
                defaultValue = "0000")
        public String appInstitutionPIN();

    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

}
