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
<%@ page language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt"%>
<%@ page import="org.fenixedu.idcards.domain.SantanderBatch" %>
<html:xhtml/>

<logic:present name="batch">
	<bean:define id="thisBatch" name="batch"/>
	<td>
		<dt:format pattern="yyyy-MM-dd HH:mm:ss"><bean:write name="batch" property="created.millis"/></dt:format>
	</td>
	<td>
		<bean:write name="batch" property="santanderBatchRequester.person.username" />
	</td>
	<td>
   		<logic:present name="batch" property="generated">
   			<dt:format pattern="yyyy-MM-dd HH:mm:ss"><bean:write name="batch" property="generated.millis"/></dt:format>
   		</logic:present>
   		<logic:notPresent name="batch" property="generated">
   			<span>--</span>
   		</logic:notPresent>		
	</td>
   	<td>
   		<logic:present name="batch" property="sent">
   			<dt:format pattern="yyyy-MM-dd HH:mm:ss"><bean:write name="batch" property="sent.millis"/></dt:format>
   		</logic:present>
   		<logic:notPresent name="batch" property="sent">
   			<span>--</span>
   		</logic:notPresent>		
	</td>
	<td>
   		<logic:present name="batch" property="santanderBatchSender">
   			<bean:write name="batch" property="santanderBatchSender.person.username" />
   		</logic:present>
   		<logic:notPresent name="batch" property="santanderBatchSender">
   			<span>--</span>
   		</logic:notPresent>		
	</td>
	<td>
   		<logic:present name="batch" property="sequenceNumber">
   			<bean:write name="batch" property="sequenceNumber" />
   		</logic:present>
   		<logic:notPresent name="batch" property="sequenceNumber">
   			<span>--</span>
   		</logic:notPresent>		
	</td>
   	<td>
   		<% 	SantanderBatch santanderBatch = ((SantanderBatch) thisBatch);
   			if (santanderBatch.getSantanderProblemsSet().size() > 0) { %>
   				<font color="red">
					<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.contains.problems"/>
				</font>
   		<%	} else if (santanderBatch.getGenerated() == null) { %>
   				<font color="orange">
					<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.creating"/>
				</font>
   		<%	} else if (santanderBatch.getSent() == null) { %>
   				<font color="green">
					<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.processed"/>
				</font>
   		<%	} else { %>
   				<font color="blue">
					<bean:message bundle="CARD_GENERATION_RESOURCES" key="message.card.generation.batch.sent"/>
				</font>
   		<%	} %>
	</td>
   	<td>
		<bean:size id="numberLines" name="batch" property="santanderEntriesSet"/>
   		<bean:write name="numberLines"/>
	</td>
   	<td>
		<bean:size id="numberProblems" name="batch" property="santanderProblemsSet"/>
   		<bean:write name="numberProblems"/>
	</td>	
</logic:present>
