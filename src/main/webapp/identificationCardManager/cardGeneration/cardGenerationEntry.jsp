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

		<br/>
		<bean:message bundle="CARD_GENERATION_RESOURCES" key="label.card.generation.entry.line"/>
		<strong>
			<bean:write name="cardGenerationEntry" property="cardGenerationBatch.executionYear.name"/>
			-
			<bean:write name="cardGenerationEntry" property="cardGenerationBatch.description"/>
		</strong>

		<table class="tstyle1 thlight tdcenter">
			<tr>
				<th>Nome</th>
				<th>Conteúdo</th>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.campusCode"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="campusCode"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.courseCode"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="courseCode"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.entityCode"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="entityCode"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.categoryCode"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="categoryCode"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.memberNumber"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="memberNumber"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.registerPurpose"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="registerPurpose"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.expirationDate"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="expirationDate"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.reservedField1"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="reservedField1"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.reservedField2"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="reservedField2"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.subClassCode"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="subClassCode"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.cardViaNumber"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="cardViaNumber"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.courseCode2"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="courseCode2"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.secondaryCategoryCode"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="secondaryCategoryCode"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.secondaryMemberNumber"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="secondaryMemberNumber"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.course"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="course"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.editedStudentNumber"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="editedStudentNumber"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.editedSecondaryMemberNumber"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="editedSecondaryMemberNumber"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.levelOfEducation"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="levelOfEducation"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.registrationYear"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="registrationYear"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.issueDate"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="issueDate"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.secondaryCategory"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="secondaryCategory"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.workPlace"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="workPlace"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.extraInformation"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="extraInformation"/></td>
			</tr>
			<tr>
				<td class="aleft"><bean:message key="cardGeneration.studentCompleteName"/></td>
				<td class="aleft"><bean:write name="cardGenerationEntry" property="studentCompleteName"/></td>
			</tr>
		</table>
