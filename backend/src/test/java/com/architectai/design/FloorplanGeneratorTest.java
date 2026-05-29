package com.architectai.design;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FloorplanGeneratorTest {

    private final FloorplanGenerator generator = new FloorplanGenerator();

    @Test
    void generatesOffsetRoomsAndOutdoorVoidsInsteadOfUniformStrips() {
        DesignBrief brief = new DesignBrief(
                5,
                20,
                2,
                3,
                2,
                "modern",
                List.of("living", "kitchen", "bedroom"),
                List.of(),
                List.of()
        );

        Floorplan floorplan = generator.generate(brief);
        FloorplanLevel firstFloor = floorplan.floors().get(0);

        assertThat(firstFloor.rooms())
                .extracting(FloorplanRoom::label)
                .contains("Giếng trời / sân trong", "Sân sau / giặt");
        assertThat(firstFloor.rooms())
                .extracting(FloorplanRoom::width)
                .contains(5.0)
                .doesNotContainOnly(5.0);
        assertThat(firstFloor.rooms())
                .anySatisfy(room -> assertThat(room.x()).isGreaterThan(0));
    }

    @Test
    void addsElevatorCoreForFourOrMoreFloors() {
        DesignBrief brief = new DesignBrief(
                5,
                20,
                4,
                4,
                4,
                "modern",
                List.of("living", "kitchen", "bedroom"),
                List.of(),
                List.of()
        );

        Floorplan floorplan = generator.generate(brief);

        assertThat(floorplan.floors()).hasSize(4);
        assertThat(floorplan.floors())
                .allSatisfy(level -> assertThat(level.rooms())
                        .extracting(FloorplanRoom::label)
                        .contains("Thang máy"));
        assertThat(floorplan.floors().get(3).rooms())
                .extracting(FloorplanRoom::label)
                .contains("Vườn mái", "Sân phơi sau");
    }
}
