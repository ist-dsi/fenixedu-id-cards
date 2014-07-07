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

import java.util.Comparator;

import net.sourceforge.fenixedu.domain.Person;

import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;

public class SantanderEntry extends SantanderEntry_Base {

    static final public Comparator<SantanderEntry> COMPARATOR_BY_MOST_RECENTLY_CREATED = new Comparator<SantanderEntry>() {

        @Override
        public int compare(SantanderEntry o1, SantanderEntry o2) {
            return o1.getCreated().isAfter(o2.getCreated()) ? 1 : 0;
        }

    };

    public SantanderEntry() {
        super();
        setRootDomainObject(Bennu.getInstance());
        setCreated(new DateTime());
    }

    public SantanderEntry(SantanderBatch batch, Person person, String line) {
        this();
        setSantanderBatch(batch);
        setPerson(person);
        setSantanderPhotoEntry(person.getSantanderPhotoEntry());
        setLine(line);
    }

    public void delete() {
        setPerson(null);
        setSantanderBatch(null);
        setSantanderPhotoEntry(null);
        setRootDomainObject(null);
        deleteDomainObject();
    }

    public static SantanderEntry readByUsernameAndCategory(String username, String category) {
        for (SantanderEntry entry : Bennu.getInstance().getSantanderEntriesSet()) {
            if (entry
                    .getLine()
                    .subSequence(1 + 10 + 15 + 15 + 40 + 50 + 50 + 8 + 30 + 10 + 10 + 9 + 16 + 10,
                            1 + 10 + 15 + 15 + 40 + 50 + 50 + 8 + 30 + 10 + 10 + 9 + 16 + 10 + 10).equals(username)
                    && entry.getLine()
                            .subSequence(
                                    1 + 10 + 15 + 15 + 40 + 50 + 50 + 8 + 30 + 10 + 10 + 9 + 16 + 10 + 10 + 1 + 2 + 8 + 11 + 1
                                            + 4 + 4 + 10 + 5,
                                    1 + 10 + 15 + 15 + 40 + 50 + 50 + 8 + 30 + 10 + 10 + 9 + 16 + 10 + 10 + 1 + 2 + 8 + 11 + 1
                                            + 4 + 4 + 10 + 5 + 1).equals(category)) {
                return entry;
            }
        }
        return null;
    }
}
