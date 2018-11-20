/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Identification Cards.
 *
 * FenixEdu Identification Cards is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Identification Cards is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Identification Cards.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.idcards.ui.candidacydocfiller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.organizationalStructure.UniversityUnit;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.servlet.PdfFiller;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class SantanderPdfFiller extends PdfFiller {

    private static final String SANTANDER_APPLICATION_PDF_PATH = "/SANTANDER_APPLICATION_FORM.pdf";

    @Override
    public ByteArrayOutputStream getFilledPdf(Person person) throws IOException, DocumentException {
        ByteArrayOutputStream concatenatedBPIPdf = new ByteArrayOutputStream();
        PdfCopyFields copy = new PdfCopyFields(concatenatedBPIPdf);

        copy.addDocument(new PdfReader(getFilledPdfSantanderApplication(person).toByteArray()));

        copy.close();

        return concatenatedBPIPdf;
    }

    private ByteArrayOutputStream getFilledPdfSantanderApplication(Person person) throws IOException, DocumentException {
        InputStream istream = getClass().getResourceAsStream(SANTANDER_APPLICATION_PDF_PATH);
        PdfReader reader = new PdfReader(istream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        reader.selectPages("1,2");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("topmostSubform[0].Page1[0].Nomecompleto[0]", person.getName());
        String documentIdNumber = person.getDocumentIdNumber();
        if (person.getIdDocumentType().equals(IDDocumentType.CITIZEN_CARD)
                || person.getIdDocumentType().equals(IDDocumentType.IDENTITY_CARD)) {
            setField("topmostSubform[0].Page1[0].NumBICartaoCidadaooutro[0]", documentIdNumber);
            setField("topmostSubform[0].Page1[0].Checkdigit[0]", person.getIdentificationDocumentSeriesNumberValue());
        } else {
            setField("topmostSubform[0].Page1[0].Outrotipodocidentificacao[0]", documentIdNumber);
        }

        YearMonthDay emissionDate = person.getEmissionDateOfDocumentIdYearMonthDay();
        if (emissionDate != null) {
            setField("topmostSubform[0].Page1[0].Dataemissao[0]", emissionDate.toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
        }
        setField("topmostSubform[0].Page1[0].Datavalidade[0]",
                person.getExpirationDateOfDocumentIdYearMonthDay().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
        setField("topmostSubform[0].Page1[0].NIF[0]", person.getSocialSecurityNumber());
        setField("topmostSubform[0].Page1[0].Datanascimento[0]",
                person.getDateOfBirthYearMonthDay().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
        YearMonthDay dateOfBirthYearMonthDay = person.getDateOfBirthYearMonthDay();
        Period periodBetween = new Period(dateOfBirthYearMonthDay, new YearMonthDay());
        setField("topmostSubform[0].Page1[0].Idadeactual[0]", String.valueOf(periodBetween.get(DurationFieldType.years())));
        if (person.isFemale()) {
            setField("topmostSubform[0].Page1[0].Sexo[0]", "F"); // female
        } else if (person.isMale()) {
            setField("topmostSubform[0].Page1[0].Sexo[0]", "M"); // male
        }

        switch (person.getMaritalStatus()) {
        case CIVIL_UNION:
            setField("topmostSubform[0].Page1[0].Uniaofacto[0]", "1");
            break;
        case DIVORCED:
            setField("topmostSubform[0].Page1[0].Divorciado[0]", "1");
            break;
        case MARRIED:
            setField("topmostSubform[0].Page1[0].Casado[0]", "1");
            break;
        case SEPARATED:
            setField("topmostSubform[0].Page1[0].Separado[0]", "1");
            break;
        case SINGLE:
            setField("topmostSubform[0].Page1[0].Solteiro[0]", "1");
            break;
        case WIDOWER:
            setField("topmostSubform[0].Page1[0].Viuvo[0]", "1");
            break;
        }
        setField("topmostSubform[0].Page1[0].Telemovel[0]", person.getDefaultMobilePhoneNumber());
        setField("topmostSubform[0].Page1[0].E-mail[0]", getMail(person));

        setField("topmostSubform[0].Page1[0].Moradaresidenciapermanente[0]", person.getAddress());
        setField("topmostSubform[0].Page1[0].localidade[0]", person.getAreaOfAreaCode());
        String postalCode = person.getPostalCode();
        int dashIndex = postalCode.indexOf('-');
        setField("topmostSubform[0].Page1[0].CodPostal[0]", postalCode.substring(0, 4));
        String last3Numbers = person.getPostalCode().substring(dashIndex + 1, dashIndex + 4);
        setField("topmostSubform[0].Page1[0].ExtensaoCodPostal[0]", last3Numbers);

        setField("topmostSubform[0].Page1[0].Paisnacionalidade", person.getCountry().getCountryNationality().getContent());
        setField("topmostSubform[0].Page1[0].Paisnascimento", person.getCountryOfBirth().getName());
        setField("topmostSubform[0].Page1[0].Paisresidencia", person.getCountryOfResidence().getName());

        setField("topmostSubform[0].Page2[0].InstituiçãoEnsinoSuperior[0]", UniversityUnit.getInstitutionsUniversityUnit()
                .getName());
        setField("topmostSubform[0].Page2[0].FaculdadeEscola[0]", Bennu.getInstance().getInstitutionUnit().getName());
        Registration registration = getRegistration(person);
        if (registration != null) {
            setField("topmostSubform[0].Page2[0].Curso[0]", registration.getDegree().getSigla());
            setField("topmostSubform[0].Page2[0].AnoIncioCurso[0]", String.valueOf(registration.getStartDate().getYear()));
        }

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

    private Registration getRegistration(Person person) {
        if (person.getStudent().getActiveRegistrations().size() > 1) {
            return null;
        } else {
            return person.getStudent().getActiveRegistrations().iterator().next();
        }
    }
}
