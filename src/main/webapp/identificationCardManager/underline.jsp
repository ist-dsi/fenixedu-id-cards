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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<bean:define id="textToUnderline" name="textToUnderline" type="java.lang.String"/>
<%
	for (final char c : textToUnderline.toCharArray()) {
%>
		<% if (c == ' ') { %>
			<u style="font-weight: bolder;">&nbsp;</u>
		<% } else { %>
			<u style="font-weight: bolder;"><%= c %></u>
		<% } %>
		&nbsp;
<%
	}
%>
