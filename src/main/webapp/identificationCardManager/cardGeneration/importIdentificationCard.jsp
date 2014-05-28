<%--

    Copyright © 2014 Instituto Superior Técnico

    This file is part of FenixEdu Identification Cards.

    FenixEdu Identification Cards is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu Identification Cards is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu Identification Cards.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

<h2><bean:message key="title.import.card.generation" bundle="CARD_GENERATION_RESOURCES"/></h2>

<fr:edit id="import.data" name="importIdentificationCardDataBean" schema="importIdentificationCardData" action="/importIdentificationCardData.do?method=importIdentificationCardDataFromFile">
	<fr:layout name="tabular">
		<fr:destination name="loadCardGenerationBatches" path="/importIdentificationCardData.do?method=prepareIdentificationCardDataImportation"/>
		<fr:destination name="invalid" path="/importIdentificationCardData.do?method=prepareIdentificationCardDataImportation"/>
	</fr:layout>
</fr:edit>
