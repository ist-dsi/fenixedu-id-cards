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

import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.presentationTier.candidacydocfiller.PdfFiller;

import org.joda.time.LocalDate;
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
        setField("dia", String.valueOf(person.getExpirationDateOfDocumentIdYearMonthDay().getDayOfMonth()));
        setField("Mês", String.valueOf(person.getExpirationDateOfDocumentIdYearMonthDay().getMonthOfYear()));
        setField("Ano", String.valueOf(person.getExpirationDateOfDocumentIdYearMonthDay().getYear()));

        LocalDate today = new LocalDate();
        setField("dia_1", String.valueOf(today.getDayOfMonth()));
        setField("Mês_1", String.valueOf(today.getMonthOfYear()));
        setField("Ano_1", String.valueOf(today.getYear()));

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

        setField("undefined_2", person.getName());
        setField("NIF", person.getSocialSecurityNumber());
        setField("N", person.getDocumentIdNumber());

        setField("Nacionalidade", person.getCountryOfBirth().getCountryNationality().toString());
        setField("Naturalidade", person.getCountryOfBirth().getName());

        setField("Distrito", person.getDistrictOfBirth());
        setField("Concelho", person.getDistrictSubdivisionOfBirth());
        setField("Freguesia", person.getParishOfBirth());
        setField("Nome do Pai", person.getNameOfFather());
        setField("Nome da Mãe", person.getNameOfMother());
        setField("Morada de Residência", person.getAddress());
        setField("Localidade", person.getAreaOfAreaCode());
        setField("Designação Postal", person.getAreaOfAreaCode());
        setField("País", person.getCountryOfResidence().getName());

        String postalCode = person.getPostalCode();
        int dashIndex = postalCode.indexOf('-');
        setField("Código Postal", postalCode.substring(0, 4));
        String last3Numbers = postalCode.substring(dashIndex + 1, dashIndex + 4);
        setField("undefined_14", last3Numbers);
        setField("undefined_17", person.getDefaultMobilePhoneNumber());
        setField("undefined_19", getMail(person));

        YearMonthDay emissionDate = person.getEmissionDateOfDocumentIdYearMonthDay();
        if (emissionDate != null) {
            setField("Data de Emissão", String.valueOf(emissionDate.getDayOfMonth()));
            setField("undefined_5", String.valueOf(emissionDate.getMonthOfYear()));
            setField("undefined_6", String.valueOf(emissionDate.getYear()));
        }

        YearMonthDay expirationDate = person.getExpirationDateOfDocumentIdYearMonthDay();
        setField("Válido até", String.valueOf(expirationDate.getDayOfMonth()));
        setField("undefined_7", String.valueOf(expirationDate.getMonthOfYear()));
        setField("undefined_8", String.valueOf(expirationDate.getYear()));

        YearMonthDay birthdayDate = person.getDateOfBirthYearMonthDay();
        setField("Data de Nascimento", String.valueOf(birthdayDate.getDayOfMonth()));
        setField("undefined_9", String.valueOf(birthdayDate.getMonthOfYear()));
        setField("undefined_10", String.valueOf(birthdayDate.getYear()));

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

        setField("Nome_1", person.getName());
        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

}
