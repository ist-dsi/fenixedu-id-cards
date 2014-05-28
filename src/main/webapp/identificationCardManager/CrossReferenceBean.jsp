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
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt"%>
<html:xhtml/>

<h2>
	<bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.croosRefNewBatch" />
</h2>

<fr:edit id="crossReferenceBean"
		name="crossReferenceBean"
		schema="CardInfoUpload"
		action="/manageCardGeneration.do?method=crossReferenceNewBatch">
	<fr:schema type="org.fenixedu.idcards.ui.ManageCardGenerationDA$CrossReferenceBean" bundle="CARD_GENERATION_RESOURCES">
		<fr:slot name="description" key="label.description" validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator"/>
		<fr:slot name="inputStream" key="label.file" validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator">
			<fr:property name="fileNameSlot" value="filename"/>
		</fr:slot>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="form"/>
		<fr:property name="columnClasses" value=",,tderror"/>
	</fr:layout>
	<fr:destination name="cancel" path="<%= "/manageCardGeneration.do?method=firstPage"%>"/>
</fr:edit>
