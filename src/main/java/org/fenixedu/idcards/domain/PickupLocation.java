package org.fenixedu.idcards.domain;

import org.fenixedu.santandersdk.dto.PickupAddress;

public enum PickupLocation {
    ALAMEDA_SANTANDER(
            "Santander",
            "Avenida Rovisco Pais 1",
            "Instituto Superior Técnico",
            "1049-001",
            "Lisboa",
            "Alameda"
    ),
    ALAMEDA_DRH(
            "DRH",
            "Avenida Rovisco Pais 1",
            "Instituto Superior Técnico",
            "1049-001",
            "Lisboa",
            "Alameda"
    ),
    TAGUS_NAGT(
            "NAGT",
            "Av. Prof. Doutor Aníbal Cavaco Silva",
            "Instituto Superior Técnico",
            "2744-016",
            "Lisboa",
            "Tagus"
    ),
    TAGUS_DRH(
            "DRH",
            "Av. Prof. Doutor Aníbal Cavaco Silva",
            "Instituto Superior Técnico",
            "2744-016",
            "Lisboa",
            "Tagus"
    ),
    TAGUS_AGRHA(
            "AGRHA",
            "Av. Prof. Doutor Aníbal Cavaco Silva",
            "Instituto Superior Técnico",
            "2744-016",
            "Lisboa",
            "Tagus"
    ),
    CTN_RH(
            "Recursos Humanos",
            "Estrada Nacional 10 (ao Km 139,7)",
            "Campus Tecnológico e Nuclear",
            "2695-066",
            "Bobadela",
            "Campus Tecnológico e Nuclear"
    );


    private final String pickupLocation;
    private final String address1;
    private final String address2;
    private final String zipCode;
    private final String zipDescriptive;
    private final String campus;

    PickupLocation(final String pickupLocation, final String address1, final String address2, final String zipCode,
                   final String zipDescriptive, final String campus) {
        this.pickupLocation = pickupLocation;
        this.address1 = address1;
        this.address2 = address2;
        this.zipCode = zipCode;
        this.zipDescriptive = zipDescriptive;
        this.campus = campus;
    }

    public String getPickupLocation() {
        return pickupLocation;
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

    public String getCampus() {
        return campus;
    }

    public PickupAddress toPickupAddress() {
        final PickupAddress pickupAddress = new PickupAddress();
        pickupAddress.setAddress1(getAddress1());
        pickupAddress.setAddress2(String.format("%s, %s", getPickupLocation(), getAddress2()));
        pickupAddress.setZipCode(getZipCode());
        pickupAddress.setZipDescriptive(getZipDescriptive());
        return pickupAddress;
    }
}
