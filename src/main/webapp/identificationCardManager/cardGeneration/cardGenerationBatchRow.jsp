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

<logic:present name="cardGenerationBatch">
	<td>
		<bean:write name="cardGenerationBatch" property="executionYear.year"/>
	</td>
	<td>
		<logic:present name="cardGenerationBatch" property="created">
    		<dt:format pattern="yyyy-MM-dd HH:mm:ss"><bean:write name="cardGenerationBatch" property="created.millis"/></dt:format>
		</logic:present>
	</td>
	<td>
		<bean:write name="cardGenerationBatch" property="description"/>
	</td>
   	<td>
		<logic:present name="cardGenerationBatch" property="updated">
   			<dt:format pattern="yyyy-MM-dd HH:mm:ss"><bean:write name="cardGenerationBatch" property="updated.millis"/></dt:format>
		</logic:present>
	</td>
   	<td>
		<logic:present name="cardGenerationBatch" property="peopleForEntryCreation">
			<logic:notEmpty name="cardGenerationBatch" property="peopleForEntryCreation">
				<bean:define id="stateTitle" type="java.lang.String"><bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.creating.description"/></bean:define>
				<font color="orange" title="<%= stateTitle %>">
					<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.creating"/>
				</font>
			</logic:notEmpty>
		</logic:present>
		<logic:notPresent name="cardGenerationBatch" property="peopleForEntryCreation">
			<logic:notEmpty name="cardGenerationBatch" property="cardGenerationProblemsSet">
				<font color="red">
					<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.contains.problems"/>
				</font>
			</logic:notEmpty>
		</logic:notPresent>
		<logic:present name="cardGenerationBatch" property="sent">
			<font color="green">
				<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.sent"/>
			</font>
		</logic:present>
	</td>
   	<td>
		<logic:present name="cardGenerationBatch" property="sent">
   			<dt:format pattern="yyyy-MM-dd HH:mm:ss"><bean:write name="cardGenerationBatch" property="sent.millis"/></dt:format>
		</logic:present>
	</td>
   	<td>
		<bean:size id="numberEntries" name="cardGenerationBatch" property="cardGenerationEntriesSet"/>
   		<bean:write name="numberEntries"/>
	</td>
   	<td>
		<bean:size id="numberProblems" name="cardGenerationBatch" property="cardGenerationProblemsSet"/>
   		<bean:write name="numberProblems"/>
	</td>
	<td>
		<bean:write name="cardGenerationBatch" property="numberOfIssuedCards"/>
	</td>	
</logic:present>
