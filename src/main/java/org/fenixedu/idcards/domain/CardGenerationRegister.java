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

import net.sourceforge.fenixedu.domain.Person;

import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.LocalDate;

public class CardGenerationRegister extends CardGenerationRegister_Base {

    public CardGenerationRegister() {
        super();
        setRootDomainObject(Bennu.getInstance());
    }

    public CardGenerationRegister(final Person person, final String linePrefix, final LocalDate emission,
            final Boolean withAccountInformation) {
        this();
        setPerson(person);
        setEmission(emission);
        setWithAccountInformation(withAccountInformation);
        setLinePrefix(linePrefix);
    }

    public void delete() {
        setPerson(null);
        setRootDomainObject(null);
        deleteDomainObject();
    }
}
