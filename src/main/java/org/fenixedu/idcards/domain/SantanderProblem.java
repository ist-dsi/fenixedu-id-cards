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

public class SantanderProblem extends SantanderProblem_Base {

    public SantanderProblem() {
        super();
        setRootDomainObject(Bennu.getInstance());
    }

    public SantanderProblem(SantanderBatch batch, Person person, String cause) {
        this();
        setArg(person.getUsername() + "\t" + cause);
        setSantanderBatch(batch);
    }

    public void delete() {
        setSantanderBatch(null);
        setRootDomainObject(null);
        super.deleteDomainObject();
    }

    @Deprecated
    public boolean hasArg() {
        return getArg() != null;
    }

    @Deprecated
    public boolean hasBennu() {
        return getRootDomainObject() != null;
    }

    @Deprecated
    public boolean hasDescriptionKey() {
        return getDescriptionKey() != null;
    }

    @Deprecated
    public boolean hasSantanderBatch() {
        return getSantanderBatch() != null;
    }

}
