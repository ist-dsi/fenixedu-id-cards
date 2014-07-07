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

<div class="mtop15 mbottom05">
	<bean:define id="urlDownloadBatchFile" type="java.lang.String">/manageCardGeneration.do?method=downloadCardGenerationBatch&amp;cardGenerationBatchID=<bean:write name="cardGenerationBatch" property="externalId"/></bean:define>
	<html:link page="<%= urlDownloadBatchFile %>">
		<bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.batch.download"/>
	</html:link>
	|
	<bean:define id="urlDownloadBatchFile" type="java.lang.String">/manageCardGeneration.do?method=editCardGenerationBatch&amp;cardGenerationBatchID=<bean:write name="cardGenerationBatch" property="externalId"/></bean:define>
	<html:link page="<%= urlDownloadBatchFile %>">
		<bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.batch.edit"/>
	</html:link>
	|
	<bean:define id="sentButNotIssued" type="java.lang.String">/manageCardGeneration.do?method=downloadCardGenerationBatchSentButNotIssued&amp;cardGenerationBatchID=<bean:write name="cardGenerationBatch" property="externalId"/></bean:define>
	<html:link page="<%= sentButNotIssued %>">
		<bean:message bundle="CARD_GENERATION_RESOURCES" key="label.card.generation.batch.sent.but.not.issued"/>
	</html:link>
	<logic:notPresent name="cardGenerationBatch" property="sent">
	|
		<bean:define id="setCardDate" type="java.lang.String">/manageCardGeneration.do?method=setCardDate&amp;cardGenerationBatchID=<bean:write name="cardGenerationBatch" property="externalId"/></bean:define>
		<html:link page="<%= setCardDate %>">
			<bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.batch.set.card"/>
		</html:link>
	</logic:notPresent>

	<logic:present role="role(MANAGER)">
		<logic:notEmpty name="cardGenerationBatch" property="peopleForEntryCreation">
			|
			<bean:define id="urlClearConstructionFlag" type="java.lang.String">/manageCardGeneration.do?method=clearConstructionFlag&amp;cardGenerationBatchID=<bean:write name="cardGenerationBatch" property="externalId"/></bean:define>
			<html:link page="<%= urlClearConstructionFlag %>">
				<bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.batch.clear.construction.flag"/>
			</html:link>
		</logic:notEmpty>
	</logic:present>

</div>


<table class="tstyle4 thlight tdcenter mtop05">
	<tr>
		<jsp:include page="cardGenerationBatchHeader.jsp"></jsp:include>
		<logic:notPresent name="cardGenerationBatch" property="peopleForEntryCreation">
			<logic:notEmpty name="cardGenerationBatch" property="cardGenerationProblemsSet">
				<th></th>
			</logic:notEmpty>
		</logic:notPresent>
	</tr>
	<tr>
		<jsp:include page="cardGenerationBatchRow.jsp"></jsp:include>
		<logic:notPresent name="cardGenerationBatch" property="peopleForEntryCreation">
			<logic:notEmpty name="cardGenerationBatch" property="cardGenerationProblemsSet">
		   		<td>
					<bean:define id="urlResolveProblems" type="java.lang.String">/manageCardGeneration.do?method=manageCardGenerationBatchProblems&amp;cardGenerationBatchID=<bean:write name="cardGenerationBatch" property="externalId"/></bean:define>
					<html:link page="<%= urlResolveProblems %>">
						<bean:message bundle="CARD_GENERATION_RESOURCES" key="link.manage.card.generation.batch.resolve.problems"/>
					</html:link>
				</td>
			</logic:notEmpty>
		</logic:notPresent>
	</tr>
</table>
