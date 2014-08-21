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
package org.fenixedu.idcards.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import net.sourceforge.fenixedu.applicationTier.Servico.person.SearchPerson;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.person.HumanName;

import org.apache.commons.collections.Predicate;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.idcards.domain.CardGenerationEntry;

import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.CollectionPager;

public class SearchPersonWithCard extends SearchPerson {

    @Override
    public CollectionPager<Person> run(SearchParameters searchParameters, Predicate predicate) {
        CollectionPager<Person> acquiredPersons = super.run(searchParameters, predicate);

        if (searchParameters.emptyParameters()) {
            return acquiredPersons;
        }

        final Collection<Person> persons;

        if (searchParameters.getName() != null) {
            persons = new ArrayList<Person>();
            persons.addAll(findPersons(searchParameters.getName()));
        } else {
            persons = new ArrayList<Person>(0);
        }

        acquiredPersons.getCollection().addAll(persons);
        TreeSet<Person> result = new TreeSet<Person>(Person.COMPARATOR_BY_NAME_AND_ID);
        result.addAll(acquiredPersons.getCollection());
        return new CollectionPager<Person>(result, acquiredPersons.getMaxElementsPerPage());
    }

    private Collection<Person> findPersons(String searchName) {
        Collection<Person> persons = new ArrayList<Person>();

        for (CardGenerationEntry cardGenerationEntry : Bennu.getInstance().getCardGenerationEntriesSet()) {
            if (cardGenerationEntry.getPerson() != null) {
                if (matchableSearchNameAndCardName(searchName, cardGenerationEntry.getLine().substring(178).trim().toLowerCase())) {
                    persons.add(cardGenerationEntry.getPerson());
                }
            }
        }
        return persons;
    }

    private boolean matchableSearchNameAndCardName(String searchName, String cardName) {
        return HumanName.namesMatch(cardName, searchName);
    }

    // Service Invokers migrated from Berserk

    private static final SearchPersonWithCard serviceInstance = new SearchPersonWithCard();

    @Atomic
    public static CollectionPager<Person> runSearchPersonWithCard(SearchParameters searchParameters, Predicate predicate) {
        return serviceInstance.run(searchParameters, predicate);
    }

}