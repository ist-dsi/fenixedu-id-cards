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
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>
<html:xhtml/>

<h2><bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.edit.degree" /></h2>
<p><html:link page="/manageCardGeneration.do?method=showDegreeCodesAndLabels">« Voltar</html:link></p>

<bean:define id="url" type="java.lang.String">/manageCardGeneration.do?method=showDegreeCodesAndLabels&amp;degreeType=<bean:write name="degree" property="degreeType.name"/></bean:define>
<fr:edit id="net.sourceforge.fenixedu.domain.Degree.card.generation.edit"
		 name="degree"
		 schema="net.sourceforge.fenixedu.domain.Degree.card.generation.edit"
		 type="net.sourceforge.fenixedu.domain.Degree"
		 action="<%= url %>">
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle5 thlight thright thmiddle mtop1"/>
		<fr:property name="columnClasses" value=",,tderror1 tdclear"/>
	</fr:layout>
</fr:edit>
