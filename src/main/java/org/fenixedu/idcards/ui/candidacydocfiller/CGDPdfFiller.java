package org.fenixedu.idcards.ui.candidacydocfiller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sourceforge.fenixedu.domain.Person;

import org.apache.commons.lang.StringUtils;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class CGDPdfFiller {

    private static final String CGD_PERSONAL_INFORMATION_PDF_PATH = "/CGD_PERSONAL_INFORMATION_FORM.pdf";

    private static final String MARITAL_STATUS_CIVIL_UNION = "União de facto";
    private static final String MARITAL_STATUS_DIVORCED = "Divorciado";
    private static final String MARITAL_STATUS_SEPARATED = "Sep.judicialmente";
    private static final String MARITAL_STATUS_SINGLE = "Solteiro";
    private static final String MARITAL_STATUS_WIDOWER = "Víuvo";

    /*
     * PdfFiller variables and methods
     * Can not extend PdfFiller, since this class doesn't belong in the first candidacy report
     * */
    private AcroFields form;

    private String getMail(Person person) {
        if (person.hasInstitutionalEmailAddress()) {
            return person.getInstitutionalEmailAddressValue();
        } else {
            String emailForSendingEmails = person.getEmailForSendingEmails();
            return emailForSendingEmails != null ? emailForSendingEmails : StringUtils.EMPTY;
        }
    }

    private void setField(String fieldName, String fieldContent) throws IOException, DocumentException {
        if (fieldContent != null) {
            form.setField(fieldName, fieldContent);
        }
    }

    /*
     * End PdfFiller variables and methods
     * */

    public ByteArrayOutputStream getFilledPdf(Person person) throws IOException, DocumentException {
        ByteArrayOutputStream concatenatedCGDPdf = new ByteArrayOutputStream();
        PdfCopyFields copy = new PdfCopyFields(concatenatedCGDPdf);

        copy.addDocument(new PdfReader(getFilledPdfCGDPersonalInformation(person).toByteArray()));

        copy.close();

        return concatenatedCGDPdf;
    }

    private ByteArrayOutputStream getFilledPdfCGDPersonalInformation(Person person) throws IOException, DocumentException {
        InputStream istream = getClass().getResourceAsStream(CGD_PERSONAL_INFORMATION_PDF_PATH);
        PdfReader reader = new PdfReader(istream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        reader.selectPages("1,3");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("T_NomeComp", person.getName());
        setField("T_Email", getMail(person));

        if (person.isFemale()) {
            setField("CB_0_1", "Yes"); // female
        } else {
            setField("CB_0_0", "Yes"); // male
        }

        setField("Cod_data_1", person.getDateOfBirthYearMonthDay().toString(DateTimeFormat.forPattern("yyyy/MM/dd")));

        setField("NIF1", person.getSocialSecurityNumber());
        setField("T_DocIdent", person.getDocumentIdNumber());

        switch (person.getMaritalStatus()) {
        case CIVIL_UNION:
            setField("CB_EstCivil01", MARITAL_STATUS_CIVIL_UNION);
            break;
        case DIVORCED:
            setField("CB_EstCivil01", MARITAL_STATUS_DIVORCED);
            break;
        case MARRIED:
            setField("CB_EstCivil01", "");
            break;
        case SEPARATED:
            setField("CB_EstCivil01", MARITAL_STATUS_SEPARATED);
            break;
        case SINGLE:
            setField("CB_EstCivil01", MARITAL_STATUS_SINGLE);
            break;
        case WIDOWER:
            setField("CB_EstCivil01", MARITAL_STATUS_WIDOWER);
            break;
        }
        YearMonthDay emissionDate = person.getEmissionDateOfDocumentIdYearMonthDay();
        if (emissionDate != null) {
            setField("Cod_data_2", emissionDate.toString(DateTimeFormat.forPattern("yyyy/MM/dd")));
        }

        YearMonthDay expirationDate = person.getExpirationDateOfDocumentIdYearMonthDay();
        if (expirationDate != null) {
            setField("Cod_data_3", expirationDate.toString(DateTimeFormat.forPattern("yyyy/MM/dd")));
        }

        setField("T_NomePai", person.getNameOfFather());
        setField("T_NomeMae", person.getNameOfMother());

        setField("T_NatPais", person.getCountryOfBirth().getName());
        setField("T_Naturali", person.getDistrictOfBirth());
        setField("T_NatConc", person.getDistrictSubdivisionOfBirth());
        setField("T_NatFreg", person.getParishOfBirth());
        setField("T_PaisRes", person.getCountryOfBirth().getCountryNationality().toString());

        setField("T_Morada01", person.getAddress());
        setField("T_Localid01", person.getAreaOfAreaCode());
        setField("T_Telef", person.getDefaultMobilePhoneNumber());

        String postalCode = person.getPostalCode();
        int dashIndex = postalCode.indexOf('-');
        setField("T_CodPos01", postalCode.substring(0, 4));
        String last3Numbers = postalCode.substring(dashIndex + 1, dashIndex + 4);
        setField("T_CodPos03_1", last3Numbers);
        setField("T_Localid02_1", person.getAreaOfAreaCode());

        setField("T_Distrito", person.getDistrictOfResidence());
        setField("T_Conc", person.getDistrictSubdivisionOfResidence());
        setField("T_Freguesia", person.getParishOfResidence());
        setField("T_PaisResid", person.getCountryOfResidence().getName());

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }
}
