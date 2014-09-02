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

import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.person.IDDocumentType;
import net.sourceforge.fenixedu.presentationTier.candidacydocfiller.PdfFiller;

import org.fenixedu.idcards.domain.SantanderPhotoEntry;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Jpeg;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class SantanderPdfFiller extends PdfFiller {

    private static final String SANTANDER_APPLICATION_CARD_PDF_PATH = "/SANTANDER_APPLICATION_CARD_FORM.pdf";
    private static final String SANTANDER_APPLICATION_PDF_PATH = "/SANTANDER_APPLICATION_FORM.pdf";

    private final Logger logger = LoggerFactory.getLogger(SantanderPdfFiller.class);

    @Override
    public ByteArrayOutputStream getFilledPdf(Person person) throws IOException, DocumentException {
        ByteArrayOutputStream concatenatedBPIPdf = new ByteArrayOutputStream();
        PdfCopyFields copy = new PdfCopyFields(concatenatedBPIPdf);

        copy.addDocument(new PdfReader(getFilledPdfSantanderApplication(person).toByteArray()));
        copy.addDocument(new PdfReader(getFilledPdfSantanderCard(person).toByteArray()));

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

        setField("topmostSubform[0].Page1[0].Paisnacionalidade", person.getCountry().getCountryNationality().getPreferedContent());
        setField("topmostSubform[0].Page1[0].Paisnascimento", person.getCountryOfBirth().getName());
        setField("topmostSubform[0].Page1[0].Paisresidencia", person.getCountryOfResidence().getName());
        
        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

    private ByteArrayOutputStream getFilledPdfSantanderCard(Person person) throws IOException, DocumentException,
            BadElementException {
        InputStream istream = getClass().getResourceAsStream(SANTANDER_APPLICATION_CARD_PDF_PATH);
        PdfReader reader = new PdfReader(istream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("StudentIdentification", person.getIstUsername());
        setField("Phone", person.getDefaultMobilePhoneNumber());
        setField("Email", getMail(person));
        setField("CurrentDate", new DateTime().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm")));

        SantanderPhotoEntry photoEntryForPerson = SantanderPhotoEntry.getOrCreatePhotoEntryForPerson(person);
        if (photoEntryForPerson != null) {
            setField("Sequence", photoEntryForPerson.getPhotoIdentifier());

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BarcodeImageHandler.writeJPEG(BarcodeFactory.createCode39(photoEntryForPerson.getPhotoIdentifier(), false), baos);
                Jpeg sequenceBarcodeImg = new Jpeg(baos.toByteArray());
                float[] sequenceFieldPositions = form.getFieldPositions("SequenceBarcode"); // 1-lowerleftX, 2-lly, 3-upperRightX, 4-ury
                sequenceBarcodeImg.setAbsolutePosition(sequenceFieldPositions[1], sequenceFieldPositions[2]);
                sequenceBarcodeImg.scalePercent(45);
                stamper.getOverContent(1).addImage(sequenceBarcodeImg);
            } catch (OutputException e) {
                logger.error(e.getMessage(), e);
            } catch (BarcodeException be) {
                logger.error(be.getMessage(), be);
            }

            Jpeg photo = new Jpeg(photoEntryForPerson.getPhotoAsByteArray());
            float[] photoFieldPositions = form.getFieldPositions("Photo"); // 1-lowerleftX, 2-lly, 3-upperRightX, 4-ury
            photo.setAbsolutePosition(photoFieldPositions[1], photoFieldPositions[2]);
            photo.scalePercent(95);
            stamper.getOverContent(1).addImage(photo);
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BarcodeImageHandler.writeJPEG(BarcodeFactory.createCode128(person.getIstUsername()), baos);
            Jpeg studentIdBarcodeImg = new Jpeg(baos.toByteArray());
            float[] studentIdFieldPositions = form.getFieldPositions("StudentIdentificationBarcode"); // 1-lowerleftX, 2-lly, 3-upperRightX, 4-ury
            studentIdBarcodeImg.setAbsolutePosition(studentIdFieldPositions[1], studentIdFieldPositions[2]);
            studentIdBarcodeImg.scalePercent(45);
            stamper.getOverContent(1).addImage(studentIdBarcodeImg);
        } catch (OutputException e) {
            logger.error(e.getMessage(), e);
        } catch (BarcodeException be) {
            logger.error(be.getMessage(), be);
        }

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }

}
