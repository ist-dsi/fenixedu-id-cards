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

        setField("Cliente", person.getName());
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

        setField("Nome completo_1", person.getName());
        setField("NIF", person.getSocialSecurityNumber());
        setField("Nº", person.getDocumentIdNumber());

        setField("Nacionalidade", person.getCountryOfBirth().getCountryNationality().toString());
        setField("Naturalidade", person.getCountryOfBirth().getName());

        setField("Distrito", person.getDistrictOfBirth());
        setField("Concelho", person.getDistrictSubdivisionOfBirth());
        setField("Freguesia", person.getParishOfBirth());
        setField("Nome do Pai", person.getNameOfFather());
        setField("Nome da Mãe", person.getNameOfMother());
        setField("Morada de Residencia_1", person.getAddress());
        setField("Localidade", person.getAreaOfAreaCode());
        setField("Designação Postal", person.getAreaOfAreaCode());
        setField("País", person.getCountryOfResidence().getName());

        String postalCode = person.getPostalCode();
        int dashIndex = postalCode.indexOf('-');
        setField("Código Postal4", postalCode.substring(0, 4));
        String last3Numbers = postalCode.substring(dashIndex + 1, dashIndex + 4);
        setField("Código Postal_5", last3Numbers);
        setField("Móvel", person.getDefaultMobilePhoneNumber());
        setField("E-mail", getMail(person));

        YearMonthDay emissionDate = person.getEmissionDateOfDocumentIdYearMonthDay();
        if (emissionDate != null) {
            setField("Dia_1", String.valueOf(emissionDate.getDayOfMonth()));
            setField("Mês_1", String.valueOf(emissionDate.getMonthOfYear()));
            setField("Ano_1", String.valueOf(emissionDate.getYear()));
        }

        YearMonthDay expirationDate = person.getExpirationDateOfDocumentIdYearMonthDay();
        setField("Dia_2", String.valueOf(expirationDate.getDayOfMonth()));
        setField("Mês_2", String.valueOf(expirationDate.getMonthOfYear()));
        setField("Ano_2", String.valueOf(expirationDate.getYear()));

        YearMonthDay birthdayDate = person.getDateOfBirthYearMonthDay();
        setField("Dia3", String.valueOf(birthdayDate.getDayOfMonth()));
        setField("Mês3", String.valueOf(birthdayDate.getMonthOfYear()));
        setField("Ano_3", String.valueOf(birthdayDate.getYear()));

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
