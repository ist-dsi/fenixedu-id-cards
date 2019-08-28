package org.fenixedu.idcards.domain;

import org.fenixedu.santandersdk.dto.PickupAddress;

import java.time.LocalTime;

public enum PickupLocation {
    ALAMEDA_SANTANDER(
            "Santander",
            "Avenida Rovisco Pais 1",
            "Instituto Superior Técnico",
            "1049-001",
            "Lisboa",
            "Alameda",
            new TimeInterval(LocalTime.parse("11:00"), LocalTime.parse("12:00")),
            new TimeInterval(LocalTime.parse("15:00"), LocalTime.parse("16:00"))
    ),
    ALAMEDA_DRH(
            "DRH",
            "Avenida Rovisco Pais 1",
            "Instituto Superior Técnico",
            "1049-001",
            "Lisboa",
            "Alameda",
            new TimeInterval(LocalTime.parse("11:00"), LocalTime.parse("12:00")),
            new TimeInterval(LocalTime.parse("15:00"), LocalTime.parse("16:00"))
    ),
    TAGUS_NAGT(
            "NAGT",
            "Av. Prof. Doutor Aníbal Cavaco Silva",
            "Instituto Superior Técnico",
            "2744-016",
            "Lisboa",
            "Tagus",
            new TimeInterval(LocalTime.parse("11:00"), LocalTime.parse("12:00")),
            new TimeInterval(LocalTime.parse("15:00"), LocalTime.parse("16:00"))
    ),
    TAGUS_DRH(
            "DRH",
            "Av. Prof. Doutor Aníbal Cavaco Silva",
            "Instituto Superior Técnico",
            "2744-016",
            "Lisboa",
            "Tagus",
            new TimeInterval(LocalTime.parse("11:00"), LocalTime.parse("12:00")),
            new TimeInterval(LocalTime.parse("15:00"), LocalTime.parse("16:00"))
    ),
    CTN_RH(
            "Recursos Humanos",
            "Estrada Nacional 10 (ao Km 139,7)",
            "Campus Tecnológico e Nuclear",
            "2695-066",
            "Bobadela",
            "Campus Tecnológico e Nuclear",
            new TimeInterval(LocalTime.parse("11:00"), LocalTime.parse("12:00")),
            new TimeInterval(LocalTime.parse("15:00"), LocalTime.parse("16:00"))
    );


    private String pickupLocation;
    private String address1;
    private String address2;
    private String zipCode;
    private String zipDescriptive;
    private String campus;
    private TimeInterval morningHours;
    private TimeInterval afternoonHours;

    PickupLocation(String pickupLocation, String address1, String address2, String zipCode,
                   String zipDescriptive, String campus, TimeInterval morningHours, TimeInterval afternoonHours) {
        this.pickupLocation = pickupLocation;
        this.address1 = address1;
        this.address2 = address2;
        this.zipCode = zipCode;
        this.zipDescriptive = zipDescriptive;
        this.campus = campus;
        this.morningHours = morningHours;
        this.afternoonHours = afternoonHours;
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

    public TimeInterval getMorningHours() {
        return morningHours;
    }

    public TimeInterval getAfternoonHours() {
        return afternoonHours;
    }

    public PickupAddress toPickupAddress() {
        PickupAddress pickupAddress = new PickupAddress();
        pickupAddress.setAddress1(this.address1);
        pickupAddress.setAddress2(String.format("%s, %s", this.pickupLocation, this.address2));
        pickupAddress.setZipCode(this.zipCode);
        pickupAddress.setZipDescriptive(this.zipDescriptive);

        return pickupAddress;
    }

    public static class TimeInterval {
        private LocalTime startDate;
        private LocalTime endDate;

        TimeInterval(LocalTime startDate, LocalTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalTime getStartDate() {
            return startDate;
        }

        public LocalTime getEndDate() {
            return endDate;
        }

        @Override
        public String toString() {
            return String.format("%s - %s", getStartDate(), getEndDate());
        }
    }
}
