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

<em>Cartões de Identificação</em>
<h2><bean:message key="link.manage.card.generation" /></h2>

<p><html:link page="/manageCardGeneration.do?method=firstPage">« Voltar</html:link></p>

<br/>

<table class="tstyle4 thlight mtop05">
	<tr>
		<jsp:include page="cardGenerationBatchHeader.jsp"></jsp:include>
	</tr>
	<tr>
		<jsp:include page="cardGenerationBatchRow.jsp"></jsp:include>
	</tr>
</table>

<br/>

<table class="tstyle4 thlight mtop05">
	<tr>
		<th><bean:message bundle="CARD_GENERATION_RESOURCES" key="label.card.generation.batch.problem.description"/></th>
		<th></th>
	</tr>
	<logic:iterate id="cardGenerationBatchProblem" name="cardGenerationBatch" property="cardGenerationProblemsSet" length="100">
		<tr>
			<td>
				<bean:define id="arg" type="java.lang.String" name="cardGenerationBatchProblem" property="arg"/>
				<bean:message bundle="CARD_GENERATION_RESOURCES" name="cardGenerationBatchProblem" property="descriptionKey" arg0="<%= arg %>"/>
			</td>
	   		<td>
				<bean:define id="urlMarkAsResolved" type="java.lang.String">/manageCardGeneration.do?method=showCardGenerationProblem&amp;cardGenerationProblemID=<bean:write name="cardGenerationBatchProblem" property="externalId"/></bean:define>
				<html:link page="<%= urlMarkAsResolved %>">
					<bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.batch.problem.show"/>
				</html:link>
			</td>
		</tr>
	</logic:iterate>
</table>

<bean:size id="numberProblems" name="cardGenerationBatch" property="cardGenerationProblemsSet"/>
<logic:greaterThan name="numberProblems" value="100">
	<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.contains.more.problems"/>
</logic:greaterThan>
