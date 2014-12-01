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
package org.fenixedu.idcards.ui;

import java.io.Serializable;
import java.util.List;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.idcards.domain.SantanderBatch;

@SuppressWarnings("serial")
public class ManageSantanderCardGenerationBean implements Serializable {

    private ExecutionYear executionYear;
    private List<SantanderBatch> santanderBatches;
    private Boolean allowNewCreation;

    public ManageSantanderCardGenerationBean() {
        super();
        allowNewCreation = true;
    }

    public ManageSantanderCardGenerationBean(ExecutionYear executionYear) {
        this();
        setExecutionYear(executionYear);
    }

    public ManageSantanderCardGenerationBean(List<SantanderBatch> batches, ExecutionYear executionYear) {
        this();
        setSantanderBatches(batches);
        setExecutionYear(executionYear);
    }

    public ExecutionYear getExecutionYear() {
        return executionYear;
    }

    public void setExecutionYear(ExecutionYear executionYear) {
        this.executionYear = executionYear;
    }

    public List<SantanderBatch> getSantanderBatches() {
        return santanderBatches;
    }

    public void setSantanderBatches(List<SantanderBatch> santanderBatches) {
        this.santanderBatches = santanderBatches;
    }

    public Boolean getAllowNewCreation() {
        return allowNewCreation;
    }

    public void setAllowNewCreation(Boolean allowNewCreation) {
        this.allowNewCreation = allowNewCreation;
    }

}
