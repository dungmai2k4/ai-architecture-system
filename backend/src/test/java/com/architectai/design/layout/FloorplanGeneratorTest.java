package com.architectai.design.layout;

import com.architectai.design.domain.DesignBrief;
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
                                List.of());

                Floorplan floorplan = generator.generate(brief);
                FloorplanLevel firstFloor = floorplan.floors().get(0);

                assertThat(firstFloor.rooms())
                                .extracting(FloorplanRoom::label)
                                .contains("Giếng trời / sân trong", "Sân sau / giặt");
                assertThat(firstFloor.rooms())
                                .extracting(FloorplanRoom::width)
                                .contains(5.0)
                                .satisfies(list -> assertThat(list).anyMatch(w -> w != 5.0));
                assertThat(firstFloor.rooms())
                                .anySatisfy(room -> assertThat(room.x()).isGreaterThan(0));
        }


        @Test
        void normalizesReversedTownhouseDimensionsBeforePlanning() {
                DesignBrief brief = new DesignBrief(
                                20,
                                5,
                                3,
                                3,
                                3,
                                "modern",
                                List.of("living", "kitchen", "bedroom"),
                                List.of(),
                                List.of());

                Floorplan floorplan = generator.generate(brief);

                assertThat(brief.siteWidthMeters()).isEqualTo(5);
                assertThat(brief.siteDepthMeters()).isEqualTo(20);
                assertThat(floorplan.siteWidth()).isEqualTo(5);
                assertThat(floorplan.siteDepth()).isEqualTo(20);
        }

        @Test
        void keepsLegendOutsidePlanCanvas() {
                DesignBrief brief = new DesignBrief(
                                5,
                                20,
                                2,
                                3,
                                2,
                                "modern",
                                List.of("living", "kitchen", "bedroom"),
                                List.of(),
                                List.of());

                String svg = generator.generate(brief).floors().get(0).svg();

                assertThat(svg).contains("viewBox='0 0 322 866'");
                assertThat(svg).contains("<rect x='56' y='803' width='200' height='38'");
                assertThat(svg).contains("class='opening-label'");
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
                                List.of());

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

        @Test
        void keepsStairAndElevatorCoreAlignedAcrossAllFloors() {
                DesignBrief brief = new DesignBrief(
                                7,
                                21,
                                4,
                                4,
                                4,
                                "modern",
                                List.of("living", "kitchen", "bedroom"),
                                List.of("warm wood"),
                                List.of());

                Floorplan floorplan = generator.generate(brief);
                FloorplanRoom firstStair = coreRoom(floorplan.floors().get(0), "stairs");
                FloorplanRoom firstElevator = coreRoom(floorplan.floors().get(0), "elevator");

                assertThat(floorplan.floors())
                                .allSatisfy(level -> {
                                        FloorplanRoom stair = coreRoom(level, "stairs");
                                        FloorplanRoom elevator = coreRoom(level, "elevator");
                                        assertThat(stair.x()).isEqualTo(firstStair.x());
                                        assertThat(stair.y()).isEqualTo(firstStair.y());
                                        assertThat(stair.width()).isEqualTo(firstStair.width());
                                        assertThat(elevator.x()).isEqualTo(firstElevator.x());
                                        assertThat(elevator.y()).isEqualTo(firstElevator.y());
                                        assertThat(elevator.width()).isEqualTo(firstElevator.width());
                                });
        }

        @Test
        void variesPlansWhenDesignSignalsChangeWithoutMovingVerticalCore() {
                DesignBrief calmBrief = new DesignBrief(
                                5,
                                20,
                                3,
                                3,
                                3,
                                "minimal",
                                List.of("living", "kitchen", "bedroom"),
                                List.of("quiet courtyard"),
                                List.of());
                DesignBrief tropicalBrief = new DesignBrief(
                                5,
                                20,
                                3,
                                3,
                                3,
                                "tropical",
                                List.of("living", "kitchen", "bedroom"),
                                List.of("bold garden", "variant:3-101"),
                                List.of());

                Floorplan calmPlan = generator.generate(calmBrief);
                Floorplan tropicalPlan = generator.generate(tropicalBrief);

                assertThat(coreRoom(calmPlan.floors().get(1), "stairs").x())
                                .isEqualTo(coreRoom(calmPlan.floors().get(0), "stairs").x());
                assertThat(coreRoom(tropicalPlan.floors().get(1), "stairs").x())
                                .isEqualTo(coreRoom(tropicalPlan.floors().get(0), "stairs").x());
                assertThat(room(calmPlan.floors().get(0), "living").depth())
                                .isNotEqualTo(room(tropicalPlan.floors().get(0), "living").depth());
        }

        private FloorplanRoom coreRoom(FloorplanLevel level, String type) {
                return level.rooms().stream()
                                .filter(room -> type.equals(room.type()))
                                .findFirst()
                                .orElseThrow();
        }

        private FloorplanRoom room(FloorplanLevel level, String id) {
                return level.rooms().stream()
                                .filter(room -> id.equals(room.name()))
                                .findFirst()
                                .orElseThrow();
        }

}
