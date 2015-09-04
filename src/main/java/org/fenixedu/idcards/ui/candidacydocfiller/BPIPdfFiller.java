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
import org.fenixedu.academic.servlet.PdfFiller;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class BPIPdfFiller extends PdfFiller {

    private static final String BPI_AEIST_CARD_PDF_PATH = "/BPI_AEIST_CARD_FORM.pdf";
    private static final String BPI_DIGITAL_DOCUMENTATION_PDF_PATH = "/BPI_DIGITAL_DOCUMENTATION_FORM.pdf";
    private static final String BPI_PERSONAL_INFORMATION_PDF_PATH = "/BPI_PERSONAL_INFORMATION_FORM.pdf";
    private static final String BPI_PRODUCTS_SERVICES_PDF_PATH = "/BPI_PRODUCTS_SERVICES_FORM.pdf";

    @Override
    public ByteArrayOutputStream getFilledPdf(Person person) throws IOException, DocumentException {
        ByteArrayOutputStream concatenatedBPIPdf = new ByteArrayOutputStream();
        PdfCopyFields copy = new PdfCopyFields(concatenatedBPIPdf);

        copy.addDocument(new PdfReader(getFilledPdfBPICardAEIST(person).toByteArray()));
        copy.addDocument(new PdfReader(getFilledPdfBPIDigitalDoc(person).toByteArray()));
        copy.addDocument(new PdfReader(getFilledPdfBPIPersonalInformation(person).toByteArray()));
        copy.addDocument(new PdfReader(getFilledPdfBPIProductsandServices(person).toByteArray()));

        copy.close();

        return concatenatedBPIPdf;
    }

    private ByteArrayOutputStream getFilledPdfBPICardAEIST(Person person) throws IOException, DocumentException {
        InputStream istream = getClass().getResourceAsStream(BPI_AEIST_CARD_PDF_PATH);
        PdfReader reader = new PdfReader(istream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("BI/CC", person.getDocumentIdNumber());
        setField("Nome", person.getName());
        setField("topmostSubform[0].Page1[0].Datavalidade[0]",
                person.getExpirationDateOfDocumentIdYearMonthDay().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
        setField("dia", String.format("%02d", person.getExpirationDateOfDocumentIdYearMonthDay().getDayOfMonth()));
        setField("Mês", String.format("%02d", person.getExpirationDateOfDocumentIdYearMonthDay().getMonthOfYear()));
        setField("Ano", String.valueOf(person.getExpirationDateOfDocumentIdYearMonthDay().getYear()));

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

    private ByteArrayOutputStream getFilledPdfBPIDigitalDoc(Person person) throws IOException, DocumentException {
        InputStream istream = getClass().getResourceAsStream(BPI_DIGITAL_DOCUMENTATION_PDF_PATH);
        PdfReader reader = new PdfReader(istream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        //reader.selectPages("1");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("Text1", person.getName());

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

    private ByteArrayOutputStream getFilledPdfBPIPersonalInformation(Person person) throws IOException, DocumentException {
        InputStream istream = getClass().getResourceAsStream(BPI_PERSONAL_INFORMATION_PDF_PATH);
        PdfReader reader = new PdfReader(istream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        reader.selectPages("1,2");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("NomeCompleto_1", person.getName());

        if (person.isFemale()) {
            setField("Sexo_F", "Yes"); // female
        } else {
            setField("Sexo_M", "Yes"); // male
        }

        setField("NIF", person.getSocialSecurityNumber());
        setField("Numero", person.getDocumentIdNumber());

        setField("Nacionalidade", person.getCountryOfBirth().getCountryNationality().toString());
        setField("Naturalidade", person.getCountryOfBirth().getName());

        setField("Distrito", person.getDistrictOfBirth());
        setField("Concelho", person.getDistrictSubdivisionOfBirth());
        setField("Freguesia", person.getParishOfBirth());
        setField("NomedoPai", person.getNameOfFather());
        setField("NomedaMae", person.getNameOfMother());

        switch (person.getMaritalStatus()) {
        case CIVIL_UNION:
            setField("EstadoCivil_UniaodeFacto", "Yes");
            break;
        case DIVORCED:
            setField("EstadoCivil_Divorciado", "Yes");
            break;
        case MARRIED:
            setField("EstadoCivil_Casado", "Yes");
            break;
        case SEPARATED:
            setField("EstadoCivil_Separado Judicialmente", "Yes");
            break;
        case SINGLE:
            setField("EstadoCivil_Solteiro", "Yes");
            break;
        case WIDOWER:
            setField("EstadoCivil_Viuvo", "Yes");
            break;
        }

        setField("MoradadeResidenciaPermanente_1", person.getAddress());                 
        setField("MoradadeResidenciaPermanente_Localidade", person.getAreaOfAreaCode());
        setField("MoradadeResidenciaPermanente_DesignacaoPostal", person.getAreaOfAreaCode());
        setField("MoradadeResidenciaPermanente_Pais", person.getCountryOfResidence().getName());

        String postalCode = person.getPostalCode();
        int dashIndex = postalCode.indexOf('-');
        setField("CodigoPostal_1_1", postalCode.substring(0, 4));
        String last3Numbers = postalCode.substring(dashIndex + 1, dashIndex + 4);
        setField("CodigoPostal_1_2", last3Numbers);
        setField("Contactos_Movel_Pessoal", person.getDefaultMobilePhoneNumber());
        setField("Contactos_Email", getMail(person));

        YearMonthDay emissionDate = person.getEmissionDateOfDocumentIdYearMonthDay();
        if (emissionDate != null) {
            setField("DataEmissao_1", String.format("%02d", emissionDate.getDayOfMonth()));
            setField("DataEmissao_2", String.format("%02d", emissionDate.getMonthOfYear()));
            setField("DataEmissao_3", String.valueOf(emissionDate.getYear()));
        }

        YearMonthDay expirationDate = person.getExpirationDateOfDocumentIdYearMonthDay();
        setField("Valido_1", String.format("%02d", expirationDate.getDayOfMonth()));
        setField("Valido_2", String.format("%02d", expirationDate.getMonthOfYear()));
        setField("Valido_3", String.valueOf(expirationDate.getYear()));

        YearMonthDay birthdayDate = person.getDateOfBirthYearMonthDay();
        setField("DataNascimento_1", String.format("%02d", birthdayDate.getDayOfMonth()));
        setField("DataNascimento_2", String.format("%02d", birthdayDate.getMonthOfYear()));
        setField("DataNascimento_3", String.valueOf(birthdayDate.getYear()));

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

    private ByteArrayOutputStream getFilledPdfBPIProductsandServices(Person person) throws IOException, DocumentException {
        InputStream istream = getClass().getResourceAsStream(BPI_PRODUCTS_SERVICES_PDF_PATH);
        PdfReader reader = new PdfReader(istream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        reader.selectPages("1");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("IdentificacaoIntervenientes_1ºTitular", person.getName());
        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

}
