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
<%@ page import="org.fenixedu.academic.domain.organizationalStructure.Unit"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ page import="org.fenixedu.academic.domain.Person" %>
<%@ page import="org.fenixedu.academic.domain.person.RoleType" %>
<html:xhtml/>

<h2><bean:message key="title.person.welcome"/> - <bean:message key="label.identification.card"  bundle="APPLICATION_RESOURCES"/></h2>

<%
	final Person person = (Person) request.getAttribute("person");
	final String instructionKey;
	if (person.hasRole(RoleType.STUDENT)) {
	    instructionKey = "label.identification.card.instructions.student";
	} else {
	    instructionKey = "label.identification.card.instructions.teacher.employee";
	}
	final String institutionAcronym = Unit.getInstitutionAcronym();
	final String institutionName = Unit.getInstitutionName().getContent();
	final String username = person.getUsername() == null ? "" : person.getUsername();
	final String name = URLEncoder.encode(person.getNickname(), "UTF-8");
	final String mobile = person.getMobile();
	final String phone = URLEncoder.encode(mobile == null || mobile.isEmpty() ? person.getPhone() : mobile, "UTF-8");
	final String email = URLEncoder.encode(person.getInstitutionalEmailAddressValue(), "UTF-8");
	final String sibUrl = "https://portal.sibscartoes.pt/Fichas/index/card/rwcb/id/00042/formtype/rnc/" 
	        + "?userId=" + username
			+ "&userIdType=OU"
			+ "&userName=" + name
			+ "&userPhone=" + phone
			+ "&userEmail=" + email
			+ "&userPhoto=" + person.getProfile().getAvatarUrl();
%>
<pre style="font-family: Trebuchet MS, Arial, Helvetica, sans-serif; text-align: justify; width: 80%; white-space: pre-wrap; white-space: -moz-pre-wrap; white-space: -pre-wrap; white-space: -o-pre-wrap; word-wrap: break-word;"
><bean:message bundle="APPLICATION_RESOURCES" key="<%= instructionKey %>" arg0="<%= sibUrl %>" arg1="<%= institutionAcronym %>" arg2="<%= institutionName %>"/></pre>

<p class="cardProdutionStateSubtitle"><strong><bean:message bundle="CARD_GENERATION_RESOURCES" key="subtitle.santander.cards.dchp.state"/></strong></p>

<pre style="font-family: Trebuchet MS, Arial, Helvetica, sans-serif; text-align: justify; width: 80%; white-space: pre-wrap; white-space: -moz-pre-wrap; white-space: -pre-wrap; white-space: -o-pre-wrap; word-wrap: break-word;"
><bean:write name="state"/></pre>


 
