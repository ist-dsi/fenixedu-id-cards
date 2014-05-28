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

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.degree.Category;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.domain.util.FactoryExecutor;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.idcards.service.ImportIdentificationCardDataFromFile;

import pt.ist.fenixframework.Atomic;

public class CardGenerationProblem extends CardGenerationProblem_Base {

    public static class CardGenerationProblemDeleter implements FactoryExecutor {

        private final CardGenerationProblem cardGenerationProblem;

        public CardGenerationProblemDeleter(final CardGenerationProblem cardGenerationProblem) {
            this.cardGenerationProblem = cardGenerationProblem;
        }

        @Override
        public Object execute() {
            if (cardGenerationProblem != null) {
                cardGenerationProblem.delete();
            }
            return null;
        }
    }

    public static Set<CardGenerationProblem> getCardGenerationProblems(final Person person,
            final CardGenerationBatch cardGenerationBatch) {
        final Set<CardGenerationProblem> cardGenerationProblems = new HashSet<CardGenerationProblem>();
        for (final CardGenerationProblem cardGenerationProblem : person.getCardGenerationProblemsSet()) {
            if (cardGenerationProblem.getCardGenerationBatch() == cardGenerationBatch) {
                cardGenerationProblems.add(cardGenerationProblem);
            }
        }
        return cardGenerationProblems;
    }

    public CardGenerationProblem(final CardGenerationBatch cardGenerationBatch, final String descriptionKey, final String arg,
            final Person person) {
        super();
        setRootDomainObject(Bennu.getInstance());
        setCardGenerationBatch(cardGenerationBatch);
        setDescriptionKey(descriptionKey);
        setArg(arg);
        setPerson(person);
    }

    public void delete() {
        setPerson(null);
        setCardGenerationBatch(null);
        setRootDomainObject(null);
        deleteDomainObject();
    }

    @Atomic
    public void setPersonForCardGenerationEntry(final CardGenerationEntry cardGenerationEntry, final Person person) {
        cardGenerationEntry.setPerson(person);
        final CardGenerationBatch cardGenerationBatchWithProblems = cardGenerationEntry.getCardGenerationBatch();
        final CardGenerationBatch cardGenerationBatch =
                findBatchUnderConstruction(cardGenerationBatchWithProblems.getExecutionYear());
        cardGenerationEntry.setCardGenerationBatch(cardGenerationBatch);
        crossReference(cardGenerationEntry);
        delete();
        if (!cardGenerationBatchWithProblems.hasAnyCardGenerationProblems()
                && !cardGenerationBatchWithProblems.hasAnyCardGenerationEntries()) {
            cardGenerationBatchWithProblems.delete();
        }
    }

    private void crossReference(final CardGenerationEntry cardGenerationEntry) {
        final CardGenerationBatch cardGenerationBatch = cardGenerationEntry.getCardGenerationBatch();
        final CardGenerationBatch cardGenerationBatchWithProblems = getCardGenerationBatch();
        final Person person = cardGenerationEntry.getPerson();
        final String line = cardGenerationEntry.getLine();

        final Category category = cardGenerationEntry.getCategory();
        if (category == Category.CODE_73 || category == Category.CODE_82 || category == Category.CODE_96) {
            final Category categoryForLine = CardGenerationEntry.readCategory(line);
            final String studentLine = ImportIdentificationCardDataFromFile.createNewLine(person, cardGenerationBatch);
            if (studentLine == null) {
                if (category != Category.CODE_96) {
                    cardGenerationEntry.setCardGenerationBatch(cardGenerationBatchWithProblems);
                    new CardGenerationProblem(cardGenerationBatchWithProblems, "multiple.user.information.type.not.crossed",
                            cardGenerationEntry.getDocumentID(), person);
                }
            } else {
                final Category categoryForStudentLine = CardGenerationEntry.readCategory(studentLine);
                final String newLine =
                        ImportIdentificationCardDataFromFile.merge(line, categoryForLine, studentLine, categoryForStudentLine);
                cardGenerationEntry.setLine(newLine);
            }
        } else if (cardGenerationEntry.getCardGenerationBatch() == cardGenerationBatch && person != null
                && person.hasRole(RoleType.STUDENT)) {
            final Category categoryForLine = CardGenerationEntry.readCategory(line);
            final String studentLine = ImportIdentificationCardDataFromFile.createNewLine(person, cardGenerationBatch);
            if (studentLine == null) {
//              cardGenerationEntry.setCardGenerationBatch(cardGenerationBatchUnmatched);
//              new CardGenerationProblem(cardGenerationBatchUnmatched, "person.has.student.role.but.cannot.generate.line", identificationId, person);
            } else {
                final Category categoryForStudentLine = CardGenerationEntry.readCategory(studentLine);
                final String newLine =
                        ImportIdentificationCardDataFromFile.merge(line, categoryForLine, studentLine, categoryForStudentLine);
                cardGenerationEntry.setLine(newLine);
            }
        }
    }

    private CardGenerationBatch findBatchUnderConstruction(final ExecutionYear executionYear) {
        for (final CardGenerationBatch cardGenerationBatch : executionYear.getCardGenerationBatchesSet()) {
            if (cardGenerationBatch.getSent() == null && !cardGenerationBatch.hasAnyCardGenerationProblems()) {
                return cardGenerationBatch;
            }
        }
        return new CardGenerationBatch("New Batch", executionYear, true);
    }

    @Deprecated
    public boolean hasCardGenerationBatch() {
        return getCardGenerationBatch() != null;
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
    public boolean hasPerson() {
        return getPerson() != null;
    }

}
