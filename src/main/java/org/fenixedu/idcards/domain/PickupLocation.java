package org.fenixedu.idcards.domain;

import org.fenixedu.santandersdk.dto.PickupAddress;

public enum PickupLocation {
    ALAMEDA_SANTANDER(
            "Avenida Rovisco Pais 1",
            "Santander, Instituto Superior Técnico",
            "1049-001",
            "Lisboa"
    ),
    ALAMEDA_DRH(
            "Avenida Rovisco Pais 1",
            "DRH, Instituto Superior Técnico",
            "1049-001",
            "Lisboa"
    ),
    TAGUS_NAGT(
            "Av. Prof. Doutor Aníbal Cavaco Silva",
            "NAGT, Instituto Superior Técnico",
            "2744-016",
            "Lisboa"
    ),
    TAGUS_DRH(
            "Av. Prof. Doutor Aníbal Cavaco Silva",
            "DRH, Instituto Superior Técnico",
            "2744-016",
            "Lisboa"
    ),
    CTN_RH(
            "Estrada Nacional 10 (ao Km 139,7)",
            "Recursos Humanos, Campus Tecnológico e Nuclear",
            "2695-066",
            "Bobadela"
    );


    private String address1;
    private String address2;
    private String zipCode;
    private String zipDescriptive;
    private String description;

    PickupLocation(String address1, String address2, String zipCode, String zipDescriptive) {
        this.address1 = address1;
        this.address2 = address2;
        this.zipCode = zipCode;
        this.zipDescriptive = zipDescriptive;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getZipDescriptive() {
        return zipDescriptive;
    }

    public String getDescription() {
        return description;
    }

    public PickupAddress toPickupAddress() {
        PickupAddress pickupAddress = new PickupAddress();
        pickupAddress.setAddress1(this.address1);
        pickupAddress.setAddress2(this.address2);
        pickupAddress.setZipCode(this.zipCode);
        pickupAddress.setZipDescriptive(this.zipDescriptive);

        return pickupAddress;
    }
}
