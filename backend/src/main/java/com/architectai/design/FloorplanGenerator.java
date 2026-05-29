package com.architectai.design;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FloorplanGenerator {

    private static final int SCALE = 34;
    private static final int MARGIN = 48;

    public Floorplan generate(DesignBrief brief) {
        double siteWidth = brief.siteWidthMeters();
        double siteDepth = brief.siteDepthMeters();
        int floorCount = Math.max(1, brief.floors());

        FloorplanLevel firstFloor = createFirstFloor(siteWidth, siteDepth, floorCount);
        List<FloorplanLevel> floors = new ArrayList<>();
        floors.add(firstFloor);
        for (int level = 2; level <= floorCount; level++) {
            floors.add(createUpperFloor(siteWidth, siteDepth, level, floorCount, brief.bedrooms(), brief.bathrooms()));
        }

        return new Floorplan(
                siteWidth,
                siteDepth,
                firstFloor.rooms(),
                firstFloor.walls(),
                firstFloor.doors(),
                firstFloor.windows(),
                firstFloor.furniture(),
                firstFloor.svg(),
                floors
        );
    }

    private FloorplanLevel createFirstFloor(double siteWidth, double siteDepth, int floorCount) {
        boolean multiFloor = floorCount > 1;
        boolean hasElevator = floorCount >= 4;
        boolean wideSite = siteWidth >= 6.0;
        return wideSite
                ? createWideFirstFloor(siteWidth, siteDepth, multiFloor, hasElevator)
                : createTownhouseFirstFloor(siteWidth, siteDepth, multiFloor, hasElevator);
    }

    private FloorplanLevel createTownhouseFirstFloor(
            double siteWidth,
            double siteDepth,
            boolean multiFloor,
            boolean hasElevator
    ) {
        double frontYardDepth = siteDepth >= 15 ? round(Math.min(2.6, Math.max(1.2, siteDepth * 0.1))) : 0;
        double rearGardenDepth = siteDepth >= 17 ? round(Math.min(2.2, Math.max(1.0, siteDepth * 0.08))) : 0;
        double bodyY = frontYardDepth;
        double bodyDepth = round(siteDepth - frontYardDepth - rearGardenDepth);
        double frontDepth = round(Math.min(5.0, Math.max(3.4, bodyDepth * 0.28)));
        double coreDepth = round(Math.min(5.4, Math.max(3.8, bodyDepth * 0.28)));
        double rearDepth = round(bodyDepth - frontDepth - coreDepth);
        if (rearDepth < 3.4) {
            rearDepth = 3.4;
            coreDepth = round(bodyDepth - frontDepth - rearDepth);
        }

        double coreWidth = round(Math.min(hasElevator ? 2.65 : 2.05, Math.max(hasElevator ? 2.2 : 1.45, siteWidth * (hasElevator ? 0.48 : 0.38))));
        double mainWidth = round(siteWidth - coreWidth);
        double pocketWidth = round(Math.max(1.15, Math.min(siteWidth * 0.36, mainWidth * 0.48)));
        double serviceX = mainWidth;
        double livingY = bodyY;
        double coreY = round(livingY + frontDepth);
        double rearY = round(coreY + coreDepth);
        double stairDepth = round(Math.min(2.6, Math.max(1.9, coreDepth * 0.48)));
        double elevatorDepth = hasElevator ? 1.6 : 0;
        double wcY = round(coreY + stairDepth + (hasElevator ? elevatorDepth : 0));
        double wcDepth = round(Math.max(1.2, coreDepth - stairDepth - elevatorDepth));

        List<FloorplanRoom> rooms = new ArrayList<>();
        if (frontYardDepth > 0) {
            rooms.add(room("front_yard", "Sân trước / để xe", "outdoor", 0, 0, siteWidth, frontYardDepth, "#dcfce7"));
        }
        rooms.add(room("living", "Phòng khách", "living", 0, livingY, mainWidth, frontDepth, "#fef3c7"));
        rooms.add(room("foyer_green", "Hiên xanh", "outdoor", mainWidth, livingY, coreWidth, round(frontDepth * 0.55), "#bbf7d0"));
        rooms.add(room("stairs", hasElevator ? "Thang bộ" : "Thang", "stairs", serviceX, coreY, coreWidth, stairDepth, "#e0f2fe"));
        if (hasElevator) {
            rooms.add(room("elevator", "Thang máy", "elevator", serviceX, round(coreY + stairDepth), coreWidth, elevatorDepth, "#f1f5f9"));
        }
        rooms.add(room("wc", "WC", "bathroom", serviceX, wcY, coreWidth, wcDepth, "#dbeafe"));
        rooms.add(room("lightwell", "Giếng trời / sân trong", "void", 0, coreY, pocketWidth, coreDepth, "#ccfbf1"));
        rooms.add(room("dining", "Bàn ăn + sảnh", "living", pocketWidth, coreY, round(mainWidth - pocketWidth), coreDepth, "#cffafe"));
        rooms.add(room("kitchen", "Bếp", "kitchen", 0, rearY, mainWidth, rearDepth, "#fde68a"));
        if (multiFloor) {
            rooms.add(room("laundry_yard", "Sân sau / giặt", "service", mainWidth, rearY, coreWidth, rearDepth, "#e2e8f0"));
            if (rearGardenDepth > 0) {
                rooms.add(room("rear_garden", "Vườn sau", "outdoor", 0, round(siteDepth - rearGardenDepth), siteWidth, rearGardenDepth, "#bbf7d0"));
            }
        } else {
            rooms.add(room("bedroom", "Phòng ngủ", "bedroom", mainWidth, rearY, coreWidth, rearDepth, "#ede9fe"));
        }

        List<FloorplanWall> walls = createWalls(siteWidth, siteDepth, rooms);
        List<FloorplanDoor> doors = new ArrayList<>();
        doors.add(new FloorplanDoor("Cửa chính", round(mainWidth / 2 - 0.65), frontYardDepth > 0 ? frontYardDepth : livingY, 1.3, "horizontal", "in"));
        doors.add(new FloorplanDoor("Lối sảnh", mainWidth, round(livingY + frontDepth * 0.45), 0.85, "vertical", "in"));
        doors.add(new FloorplanDoor("Cửa ăn", pocketWidth, coreY, 0.95, "vertical", "in"));
        doors.add(new FloorplanDoor("Cửa WC", serviceX, wcY, 0.75, "vertical", "in"));
        doors.add(new FloorplanDoor(multiFloor ? "Cửa sân giặt" : "Cửa phòng ngủ", mainWidth, round(rearY + 0.65), 0.85, "vertical", "in"));
        doors.add(new FloorplanDoor("Cửa bếp", round(mainWidth / 2 - 0.45), rearY, 0.9, "horizontal", "in"));

        List<FloorplanWindow> windows = new ArrayList<>();
        windows.add(new FloorplanWindow("Cửa sổ khách", round(mainWidth * 0.18), frontYardDepth > 0 ? frontYardDepth : livingY, round(mainWidth * 0.46), "horizontal"));
        windows.add(new FloorplanWindow("Thoáng giếng trời", 0, round(coreY + 0.55), round(Math.min(2.2, coreDepth * 0.45)), "vertical"));
        windows.add(new FloorplanWindow("Cửa bếp ra sau", round(mainWidth * 0.35), siteDepth - rearGardenDepth, 1.5, "horizontal"));
        windows.add(new FloorplanWindow("Thoáng WC", siteWidth, round(wcY + 0.25), 0.75, "vertical"));
        if (hasElevator) {
            windows.add(new FloorplanWindow("Thoáng thang máy", siteWidth, round(coreY + stairDepth + 0.2), 0.8, "vertical"));
        }

        List<FloorplanFurniture> furniture = new ArrayList<>();
        furniture.add(new FloorplanFurniture("Sofa", "sofa", 0.45, round(livingY + 0.75), round(Math.min(2.4, mainWidth * 0.62)), 0.85, 0, "#94a3b8"));
        furniture.add(new FloorplanFurniture("TV", "media", round(mainWidth - 0.28), round(livingY + 0.9), 0.12, 1.65, 90, "#334155"));
        furniture.add(new FloorplanFurniture("Bậc thang", "stairs", round(serviceX + 0.2), round(coreY + 0.35), round(coreWidth - 0.4), round(stairDepth - 0.55), 0, "#38bdf8"));
        if (hasElevator) {
            furniture.add(new FloorplanFurniture("Cabin", "elevator", round(serviceX + 0.35), round(coreY + stairDepth + 0.18), round(coreWidth - 0.7), 1.2, 0, "#94a3b8"));
        }
        furniture.add(new FloorplanFurniture("Cây", "plant", round(pocketWidth * 0.35), round(coreY + 0.9), 0.55, 0.55, 0, "#16a34a"));
        furniture.add(new FloorplanFurniture("Bàn ăn", "dining", round(pocketWidth + (mainWidth - pocketWidth) / 2 - 0.85), round(coreY + coreDepth / 2 - 0.45), 1.7, 0.9, 0, "#a16207"));
        furniture.add(new FloorplanFurniture("Bếp chữ L", "kitchen_counter", 0.35, round(rearY + 0.45), round(Math.min(2.8, mainWidth - 0.7)), 0.62, 0, "#f97316"));
        furniture.add(new FloorplanFurniture("Bồn cầu", "toilet", round(serviceX + coreWidth * 0.25), round(wcY + Math.max(0.25, wcDepth - 0.9)), 0.48, 0.7, 0, "#60a5fa"));
        if (!multiFloor) {
            furniture.add(new FloorplanFurniture("Giường", "bed", round(mainWidth + coreWidth / 2 - 0.9), round(rearY + 0.65), 1.8, 2.0, 90, "#c4b5fd"));
        }

        return floor(1, "Tầng 1", siteWidth, siteDepth, rooms, walls, doors, windows, furniture);
    }

    private FloorplanLevel createWideFirstFloor(
            double siteWidth,
            double siteDepth,
            boolean multiFloor,
            boolean hasElevator
    ) {
        double frontYardDepth = siteDepth >= 14 ? round(Math.min(3.4, Math.max(1.8, siteDepth * 0.14))) : 0;
        double rearGardenDepth = siteDepth >= 17 ? round(Math.min(2.4, Math.max(1.1, siteDepth * 0.09))) : 0;
        double bodyY = frontYardDepth;
        double bodyDepth = round(siteDepth - frontYardDepth - rearGardenDepth);
        double leftWidth = round(siteWidth * 0.56);
        double rightWidth = round(siteWidth - leftWidth);
        double frontDepth = round(Math.min(5.0, Math.max(3.6, bodyDepth * 0.3)));
        double midDepth = round(Math.min(5.3, Math.max(3.6, bodyDepth * 0.32)));
        double rearDepth = round(bodyDepth - frontDepth - midDepth);
        if (rearDepth < 3.4) {
            rearDepth = 3.4;
            midDepth = round(bodyDepth - frontDepth - rearDepth);
        }
        double coreWidth = round(Math.min(hasElevator ? 2.8 : 2.1, Math.max(hasElevator ? 2.35 : 1.55, rightWidth * 0.62)));
        double sideGardenWidth = round(rightWidth - coreWidth);
        double midY = round(bodyY + frontDepth);
        double rearY = round(midY + midDepth);
        double coreX = round(leftWidth + sideGardenWidth);
        double stairDepth = round(Math.min(2.55, Math.max(1.9, midDepth * 0.46)));
        double elevatorDepth = hasElevator ? 1.55 : 0;
        double wcY = round(midY + stairDepth + elevatorDepth);
        double wcDepth = round(Math.max(1.2, midDepth - stairDepth - elevatorDepth));

        List<FloorplanRoom> rooms = new ArrayList<>();
        if (frontYardDepth > 0) {
            rooms.add(room("front_yard", "Sân trước / đậu xe", "outdoor", 0, 0, siteWidth, frontYardDepth, "#dcfce7"));
        }
        rooms.add(room("living", "Phòng khách", "living", 0, bodyY, leftWidth, frontDepth, "#fef3c7"));
        rooms.add(room("guest_bed", "Phòng ngủ nhỏ", "bedroom", leftWidth, bodyY, rightWidth, frontDepth, "#ede9fe"));
        rooms.add(room("dining", "Bếp + ăn", "kitchen", 0, midY, leftWidth, midDepth, "#fde68a"));
        rooms.add(room("side_court", "Sân trong", "void", leftWidth, midY, sideGardenWidth, midDepth, "#ccfbf1"));
        rooms.add(room("stairs", hasElevator ? "Thang bộ" : "Thang", "stairs", coreX, midY, coreWidth, stairDepth, "#e0f2fe"));
        if (hasElevator) {
            rooms.add(room("elevator", "Thang máy", "elevator", coreX, round(midY + stairDepth), coreWidth, elevatorDepth, "#f1f5f9"));
        }
        rooms.add(room("wc", "WC", "bathroom", coreX, wcY, coreWidth, wcDepth, "#dbeafe"));
        rooms.add(room("master", multiFloor ? "Phòng đa năng" : "Phòng ngủ master", multiFloor ? "living" : "bedroom", 0, rearY, leftWidth, rearDepth, multiFloor ? "#cffafe" : "#ddd6fe"));
        rooms.add(room("rear_yard", "Sân sau / giặt", "service", leftWidth, rearY, rightWidth, rearDepth, "#e2e8f0"));
        if (rearGardenDepth > 0) {
            rooms.add(room("rear_garden", "Vườn sau", "outdoor", 0, round(siteDepth - rearGardenDepth), siteWidth, rearGardenDepth, "#bbf7d0"));
        }

        List<FloorplanWall> walls = createWalls(siteWidth, siteDepth, rooms);
        List<FloorplanDoor> doors = new ArrayList<>();
        doors.add(new FloorplanDoor("Cửa chính", round(leftWidth / 2 - 0.7), frontYardDepth > 0 ? frontYardDepth : bodyY, 1.4, "horizontal", "in"));
        doors.add(new FloorplanDoor("Cửa ngủ khách", leftWidth, round(bodyY + frontDepth * 0.55), 0.85, "vertical", "in"));
        doors.add(new FloorplanDoor("Cửa bếp", round(leftWidth / 2 - 0.45), midY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor("Cửa sân trong", leftWidth, round(midY + midDepth * 0.45), 0.95, "vertical", "in"));
        doors.add(new FloorplanDoor("Lối thang", coreX, round(midY + 0.35), 0.85, "vertical", "in"));
        doors.add(new FloorplanDoor("Cửa WC", coreX, round(wcY + 0.25), 0.75, "vertical", "in"));
        doors.add(new FloorplanDoor("Cửa sau", round(leftWidth / 2 - 0.55), rearY, 1.1, "horizontal", "in"));

        List<FloorplanWindow> windows = new ArrayList<>();
        windows.add(new FloorplanWindow("Cửa sổ khách", round(leftWidth * 0.18), frontYardDepth > 0 ? frontYardDepth : bodyY, round(leftWidth * 0.5), "horizontal"));
        windows.add(new FloorplanWindow("Cửa sổ ngủ", round(leftWidth + rightWidth * 0.18), frontYardDepth > 0 ? frontYardDepth : bodyY, round(rightWidth * 0.5), "horizontal"));
        windows.add(new FloorplanWindow("Thoáng sân trong", leftWidth, round(midY + 0.55), round(Math.min(2.0, midDepth * 0.44)), "vertical"));
        windows.add(new FloorplanWindow("Thoáng WC", siteWidth, round(wcY + 0.25), 0.75, "vertical"));
        windows.add(new FloorplanWindow("Cửa mở vườn", round(leftWidth * 0.32), siteDepth - rearGardenDepth, round(Math.min(2.1, leftWidth * 0.42)), "horizontal"));

        List<FloorplanFurniture> furniture = new ArrayList<>();
        furniture.add(new FloorplanFurniture("Sofa", "sofa", 0.55, round(bodyY + 0.75), round(Math.min(2.8, leftWidth * 0.55)), 0.9, 0, "#94a3b8"));
        furniture.add(new FloorplanFurniture("TV", "media", round(leftWidth - 0.28), round(bodyY + 0.95), 0.12, 1.8, 90, "#334155"));
        furniture.add(new FloorplanFurniture("Giường", "bed", round(leftWidth + rightWidth / 2 - 0.95), round(bodyY + 0.75), 1.9, 2.0, 0, "#c4b5fd"));
        furniture.add(new FloorplanFurniture("Đảo bếp", "kitchen_counter", 0.55, round(midY + 0.6), round(Math.min(3.0, leftWidth - 1.1)), 0.65, 0, "#f97316"));
        furniture.add(new FloorplanFurniture("Bàn ăn", "dining", round(leftWidth / 2 - 0.95), round(midY + midDepth - 1.35), 1.9, 0.95, 0, "#a16207"));
        furniture.add(new FloorplanFurniture("Bậc thang", "stairs", round(coreX + 0.2), round(midY + 0.35), round(coreWidth - 0.4), round(stairDepth - 0.55), 0, "#38bdf8"));
        if (hasElevator) {
            furniture.add(new FloorplanFurniture("Cabin", "elevator", round(coreX + 0.35), round(midY + stairDepth + 0.18), round(coreWidth - 0.7), 1.15, 0, "#94a3b8"));
        }
        furniture.add(new FloorplanFurniture("Bồn cây", "planter", round(leftWidth + sideGardenWidth / 2 - 0.35), round(midY + midDepth / 2 - 0.35), 0.7, 0.7, 0, "#22c55e"));
        furniture.add(new FloorplanFurniture("Máy giặt", "washer", round(leftWidth + 0.45), round(rearY + 0.55), 0.7, 0.7, 0, "#64748b"));
        if (!multiFloor) {
            furniture.add(new FloorplanFurniture("Giường master", "bed", round(leftWidth / 2 - 1.05), round(rearY + 0.65), 2.1, 1.8, 0, "#c4b5fd"));
        }

        return floor(1, "Tầng 1", siteWidth, siteDepth, rooms, walls, doors, windows, furniture);
    }

    private FloorplanLevel createUpperFloor(
            double siteWidth,
            double siteDepth,
            int level,
            int floorCount,
            int bedrooms,
            int bathrooms
    ) {
        boolean topFloor = level == floorCount && floorCount >= 3;
        boolean hasElevator = floorCount >= 4;
        boolean wideSite = siteWidth >= 6.0;
        double balconyDepth = round(Math.min(wideSite ? 1.8 : 1.45, Math.max(0.9, siteDepth * 0.07)));
        double rearTerraceDepth = topFloor ? round(Math.min(2.2, Math.max(1.1, siteDepth * 0.08))) : 0;
        double bodyY = balconyDepth;
        double bodyDepth = round(siteDepth - balconyDepth - rearTerraceDepth);
        double frontDepth = round(Math.min(5.0, Math.max(3.6, bodyDepth * 0.34)));
        double coreDepth = round(Math.min(5.2, Math.max(3.6, bodyDepth * 0.3)));
        double rearDepth = round(bodyDepth - frontDepth - coreDepth);
        if (rearDepth < 3.2) {
            rearDepth = 3.2;
            coreDepth = round(bodyDepth - frontDepth - rearDepth);
        }
        double serviceWidth = round(Math.min(hasElevator ? 2.75 : 2.1, Math.max(hasElevator ? 2.2 : 1.45, siteWidth * (wideSite ? 0.34 : 0.42))));
        double mainWidth = round(siteWidth - serviceWidth);
        double serviceX = wideSite ? 0 : mainWidth;
        double mainX = wideSite ? serviceWidth : 0;
        double midY = round(bodyY + frontDepth);
        double rearY = round(midY + coreDepth);
        double stairDepth = round(Math.min(2.55, Math.max(1.85, coreDepth * 0.48)));
        double elevatorDepth = hasElevator ? 1.55 : 0;
        double wcY = round(midY + stairDepth + elevatorDepth);
        double wcDepth = round(Math.max(1.15, coreDepth - stairDepth - elevatorDepth));
        double studyWidth = round(Math.max(1.35, mainWidth * 0.36));
        double rearBedroomWidth = round(mainWidth - studyWidth);

        List<FloorplanRoom> rooms = new ArrayList<>();
        rooms.add(room("balcony_l" + level, topFloor ? "Sân thượng trước" : "Ban công lệch", "outdoor", mainX, 0, mainWidth, balconyDepth, "#bbf7d0"));
        rooms.add(room("front_room_l" + level, topFloor ? "Phòng thờ / đa năng" : "Phòng ngủ trước", topFloor ? "worship" : "bedroom", mainX, bodyY, mainWidth, frontDepth, topFloor ? "#f5d0fe" : "#ede9fe"));
        rooms.add(room("stairs_l" + level, hasElevator ? "Thang bộ" : "Thang", "stairs", serviceX, midY, serviceWidth, stairDepth, "#e0f2fe"));
        if (hasElevator) {
            rooms.add(room("elevator_l" + level, "Thang máy", "elevator", serviceX, round(midY + stairDepth), serviceWidth, elevatorDepth, "#f1f5f9"));
        }
        rooms.add(room("wc_l" + level, bathrooms >= level ? "WC tầng " + level : "Kho/WC", "bathroom", serviceX, wcY, serviceWidth, wcDepth, "#dbeafe"));
        rooms.add(room("family_l" + level, "Sinh hoạt + thông tầng", "living", mainX, midY, mainWidth, coreDepth, "#cffafe"));
        rooms.add(room("lightwell_l" + level, "Ô thoáng", "void", wideSite ? round(serviceWidth * 0.18) : round(mainX + mainWidth * 0.58), round(midY + 0.45), wideSite ? round(serviceWidth * 0.64) : round(mainWidth * 0.34), round(Math.max(1.35, coreDepth * 0.36)), "#ccfbf1"));
        if (topFloor) {
            rooms.add(room("laundry_l" + level, "Giặt phơi", "service", mainX, rearY, studyWidth, rearDepth, "#e2e8f0"));
            rooms.add(room("roof_garden_l" + level, "Vườn mái", "outdoor", round(mainX + studyWidth), rearY, rearBedroomWidth, rearDepth, "#bbf7d0"));
            if (rearTerraceDepth > 0) {
                rooms.add(room("rear_terrace_l" + level, "Sân phơi sau", "outdoor", 0, round(siteDepth - rearTerraceDepth), siteWidth, rearTerraceDepth, "#dcfce7"));
            }
        } else {
            rooms.add(room("rear_bedroom_l" + level, "Phòng ngủ sau", "bedroom", mainX, rearY, rearBedroomWidth, rearDepth, "#ddd6fe"));
            rooms.add(room("study_l" + level, "Góc học / thay đồ", "living", round(mainX + rearBedroomWidth), rearY, studyWidth, rearDepth, "#fef3c7"));
        }

        List<FloorplanWall> walls = createWalls(siteWidth, siteDepth, rooms);
        List<FloorplanDoor> doors = new ArrayList<>();
        doors.add(new FloorplanDoor("Cửa ban công", round(mainX + mainWidth / 2 - 0.8), balconyDepth, 1.6, "horizontal", "in"));
        doors.add(new FloorplanDoor("Cửa phòng trước", round(mainX + mainWidth / 2 - 0.45), bodyY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor("Lối thang", wideSite ? serviceWidth : mainWidth, round(midY + 0.35), 0.85, "vertical", "in"));
        doors.add(new FloorplanDoor("Cửa WC", wideSite ? serviceWidth : mainWidth, round(wcY + 0.2), 0.75, "vertical", "in"));
        doors.add(new FloorplanDoor(topFloor ? "Cửa giặt phơi" : "Cửa phòng sau", round(mainX + rearBedroomWidth / 2 - 0.45), rearY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor(topFloor ? "Cửa vườn mái" : "Cửa góc học", round(mainX + rearBedroomWidth), round(rearY + 0.6), 0.8, "vertical", "in"));

        List<FloorplanWindow> windows = new ArrayList<>();
        windows.add(new FloorplanWindow("Thoáng phòng trước", round(mainX + mainWidth * 0.18), balconyDepth, round(mainWidth * 0.45), "horizontal"));
        windows.add(new FloorplanWindow("Thoáng thông tầng", wideSite ? round(serviceWidth * 0.22) : round(mainX + mainWidth * 0.6), round(midY + 0.45), round(Math.min(1.8, coreDepth * 0.35)), wideSite ? "vertical" : "horizontal"));
        windows.add(new FloorplanWindow(topFloor ? "Thoáng vườn mái" : "Cửa sổ phòng sau", round(mainX + rearBedroomWidth * 0.18), siteDepth - rearTerraceDepth, round(Math.min(1.8, rearBedroomWidth * 0.48)), "horizontal"));
        windows.add(new FloorplanWindow("Thoáng WC", wideSite ? 0 : siteWidth, round(wcY + 0.25), 0.75, "vertical"));

        List<FloorplanFurniture> furniture = new ArrayList<>();
        furniture.add(new FloorplanFurniture(topFloor ? "Bàn thờ" : "Giường trước", topFloor ? "altar" : "bed", round(mainX + mainWidth / 2 - 1.05), round(bodyY + 0.75), 2.1, topFloor ? 0.75 : 1.8, 0, topFloor ? "#a855f7" : "#c4b5fd"));
        furniture.add(new FloorplanFurniture("Bậc thang", "stairs", round(serviceX + 0.2), round(midY + 0.35), round(serviceWidth - 0.4), round(stairDepth - 0.55), 0, "#38bdf8"));
        if (hasElevator) {
            furniture.add(new FloorplanFurniture("Cabin", "elevator", round(serviceX + 0.35), round(midY + stairDepth + 0.18), round(serviceWidth - 0.7), 1.15, 0, "#94a3b8"));
        }
        furniture.add(new FloorplanFurniture("Sofa nhỏ", "sofa", round(mainX + 0.45), round(midY + 0.8), round(Math.min(2.1, mainWidth * 0.5)), 0.75, 0, "#94a3b8"));
        furniture.add(new FloorplanFurniture("Lavabo", "sink", round(serviceX + serviceWidth * 0.25), round(wcY + Math.max(0.25, wcDepth - 0.85)), 0.5, 0.5, 0, "#60a5fa"));
        if (topFloor) {
            furniture.add(new FloorplanFurniture("Máy giặt", "washer", round(mainX + 0.45), round(rearY + 0.55), 0.7, 0.7, 0, "#64748b"));
            furniture.add(new FloorplanFurniture("Bồn cây", "planter", round(mainX + studyWidth + 0.55), round(rearY + 0.6), 0.9, 0.7, 0, "#22c55e"));
        } else {
            furniture.add(new FloorplanFurniture("Giường sau", "bed", round(mainX + rearBedroomWidth / 2 - 1.0), round(rearY + 0.6), 2.0, 1.8, 0, "#c4b5fd"));
            furniture.add(new FloorplanFurniture("Bàn học", "desk", round(mainX + rearBedroomWidth + 0.25), round(rearY + 0.55), round(Math.max(0.8, studyWidth - 0.5)), 0.55, 0, "#f59e0b"));
        }

        String label = "Tầng " + level;
        return floor(level, label, siteWidth, siteDepth, rooms, walls, doors, windows, furniture);
    }

    private FloorplanLevel floor(
            int level,
            String label,
            double siteWidth,
            double siteDepth,
            List<FloorplanRoom> rooms,
            List<FloorplanWall> walls,
            List<FloorplanDoor> doors,
            List<FloorplanWindow> windows,
            List<FloorplanFurniture> furniture
    ) {
        return new FloorplanLevel(
                level,
                label,
                siteWidth,
                siteDepth,
                rooms,
                walls,
                doors,
                windows,
                furniture,
                renderSvg(siteWidth, siteDepth, rooms, walls, doors, windows, furniture, label)
        );
    }

    private List<FloorplanWall> createWalls(double siteWidth, double siteDepth, List<FloorplanRoom> rooms) {
        List<FloorplanWall> walls = new ArrayList<>();
        walls.add(new FloorplanWall(0, 0, siteWidth, 0, 0.22, "external"));
        walls.add(new FloorplanWall(siteWidth, 0, siteWidth, siteDepth, 0.22, "external"));
        walls.add(new FloorplanWall(siteWidth, siteDepth, 0, siteDepth, 0.22, "external"));
        walls.add(new FloorplanWall(0, siteDepth, 0, 0, 0.22, "external"));

        for (FloorplanRoom room : rooms) {
            if ("outdoor".equals(room.type()) || "void".equals(room.type())) {
                continue;
            }
            walls.add(new FloorplanWall(room.x(), room.y(), room.x() + room.width(), room.y(), 0.12, "partition"));
            walls.add(new FloorplanWall(room.x() + room.width(), room.y(), room.x() + room.width(), room.y() + room.depth(), 0.12, "partition"));
            walls.add(new FloorplanWall(room.x() + room.width(), room.y() + room.depth(), room.x(), room.y() + room.depth(), 0.12, "partition"));
            walls.add(new FloorplanWall(room.x(), room.y() + room.depth(), room.x(), room.y(), 0.12, "partition"));
        }
        return walls;
    }

    private List<FloorplanDoor> createDoors(
            double siteWidth,
            double frontYardDepth,
            double livingY,
            double serviceY,
            double rearY,
            double leftWidth,
            double stairWidth,
            double serviceDepth,
            boolean multiFloor
    ) {
        List<FloorplanDoor> doors = new ArrayList<>();
        doors.add(new FloorplanDoor("Cửa chính", round(siteWidth / 2 - 0.65), frontYardDepth > 0 ? frontYardDepth : livingY, 1.3, "horizontal", "in"));
        doors.add(new FloorplanDoor("Cửa bếp", round(leftWidth / 2 - 0.45), serviceY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor("Cửa WC", leftWidth, round(serviceY + serviceDepth * 0.72), 0.75, "vertical", "in"));
        doors.add(new FloorplanDoor(multiFloor ? "Cửa sân sau" : "Cửa phòng ngủ", round(siteWidth / 2 - 0.45), rearY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor("Lối thang", leftWidth, round(serviceY + 0.35), Math.min(0.85, stairWidth * 0.65), "vertical", "in"));
        return doors;
    }

    private List<FloorplanWindow> createWindows(
            double siteWidth,
            double siteDepth,
            double frontYardDepth,
            double livingY,
            double serviceY,
            double rearY,
            double leftWidth,
            boolean multiFloor
    ) {
        List<FloorplanWindow> windows = new ArrayList<>();
        windows.add(new FloorplanWindow("Cửa sổ trước", round(siteWidth * 0.15), frontYardDepth > 0 ? frontYardDepth : livingY, round(siteWidth * 0.28), "horizontal"));
        windows.add(new FloorplanWindow("Cửa sổ trước", round(siteWidth * 0.57), frontYardDepth > 0 ? frontYardDepth : livingY, round(siteWidth * 0.28), "horizontal"));
        windows.add(new FloorplanWindow("Thông gió bếp", 0, round(rearY + 1.0), 1.2, "vertical"));
        windows.add(new FloorplanWindow("Giếng trời", round(leftWidth * 0.6), round(serviceY + 0.3), round(leftWidth * 0.28), "horizontal"));
        windows.add(new FloorplanWindow(multiFloor ? "Cửa thoáng sân sau" : "Cửa sổ phòng ngủ", round(siteWidth / 2 - 0.9), siteDepth, 1.8, "horizontal"));
        windows.add(new FloorplanWindow("Thoáng WC", siteWidth, round(serviceY + 2.9), 0.8, "vertical"));
        windows.add(new FloorplanWindow("Cửa mở sân sau", round(siteWidth / 2 - 0.7), rearY, 1.4, "horizontal"));
        return windows;
    }

    private List<FloorplanFurniture> createFurniture(
            double siteWidth,
            double siteDepth,
            double frontYardDepth,
            double livingY,
            double serviceY,
            double rearY,
            double leftWidth,
            double stairWidth,
            double serviceDepth,
            double kitchenDepth,
            boolean multiFloor
    ) {
        List<FloorplanFurniture> furniture = new ArrayList<>();
        furniture.add(new FloorplanFurniture("Sofa", "sofa", 0.45, round(livingY + 0.75), round(Math.min(2.5, siteWidth * 0.48)), 0.85, 0, "#94a3b8"));
        furniture.add(new FloorplanFurniture("TV", "media", round(siteWidth - 0.28), round(livingY + 1.0), 0.12, round(Math.min(1.8, siteWidth * 0.35)), 90, "#334155"));
        furniture.add(new FloorplanFurniture("Bếp chữ L", "kitchen_counter", 0.35, round(rearY + 0.45), round(Math.min(3.0, siteWidth - 0.7)), 0.62, 0, "#f97316"));
        furniture.add(new FloorplanFurniture("Bàn ăn", "dining", round(Math.max(0.55, siteWidth / 2 - 0.85)), round(rearY + Math.max(1.55, kitchenDepth - 1.55)), 1.7, 0.9, 0, "#a16207"));
        furniture.add(new FloorplanFurniture("Bồn cầu", "toilet", round(leftWidth + stairWidth * 0.25), round(serviceY + serviceDepth - 1.05), 0.48, 0.7, 0, "#60a5fa"));
        furniture.add(new FloorplanFurniture("Bậc thang", "stairs", round(leftWidth + 0.18), round(serviceY + 0.35), round(stairWidth - 0.36), 2.1, 0, "#38bdf8"));
        furniture.add(new FloorplanFurniture("Cây giếng trời", "plant", round(leftWidth * 0.62), round(serviceY + 0.8), 0.55, 0.55, 0, "#16a34a"));
        if (multiFloor) {
            furniture.add(new FloorplanFurniture("Máy giặt", "washer", 0.45, round(siteDepth - 1.15), 0.7, 0.7, 0, "#64748b"));
            furniture.add(new FloorplanFurniture("Bồn cây", "planter", round(siteWidth - 1.35), round(siteDepth - 1.2), 0.9, 0.7, 0, "#22c55e"));
        } else {
            furniture.add(new FloorplanFurniture("Giường", "bed", round(siteWidth / 2 - 1.05), round(rearY + kitchenDepth + 0.55), 2.1, 1.8, 0, "#c4b5fd"));
            furniture.add(new FloorplanFurniture("Tủ áo", "wardrobe", 0.35, round(siteDepth - 0.85), round(Math.min(2.2, siteWidth - 0.7)), 0.5, 0, "#8b5cf6"));
        }
        return furniture;
    }

    private String renderSvg(
            double siteWidth,
            double siteDepth,
            List<FloorplanRoom> rooms,
            List<FloorplanWall> walls,
            List<FloorplanDoor> doors,
            List<FloorplanWindow> windows,
            List<FloorplanFurniture> furniture,
            String floorLabel
    ) {
        int planWidth = toPixels(siteWidth);
        int planHeight = toPixels(siteDepth);
        int width = planWidth + MARGIN * 2;
        int height = planHeight + MARGIN * 2;
        int titleY = 20;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 ").append(width).append(' ').append(height).append("'>");
        svg.append("<defs>")
                .append("<filter id='softShadow' x='-15%' y='-15%' width='130%' height='130%'><feDropShadow dx='0' dy='2' stdDeviation='2.2' flood-color='#0f172a' flood-opacity='0.14'/></filter>")
                .append("<pattern id='paperGrid' width='34' height='34' patternUnits='userSpaceOnUse'><path d='M 34 0 L 0 0 0 34' fill='none' stroke='#d8d3c4' stroke-width='0.65'/></pattern>")
                .append("<pattern id='gardenDots' width='18' height='18' patternUnits='userSpaceOnUse'><circle cx='5' cy='5' r='2.2' fill='#65a30d' opacity='0.45'/><path d='M12 14q4-7-2-10q-4 5 2 10z' fill='#15803d' opacity='0.38'/></pattern>")
                .append("<pattern id='wetTile' width='12' height='12' patternUnits='userSpaceOnUse'><path d='M0 0h12v12H0z' fill='#dbeafe'/><path d='M0 12L12 0M-3 3L3 -3M9 15l6-6' stroke='#93c5fd' stroke-width='0.55' opacity='0.65'/></pattern>")
                .append("<pattern id='voidHatch' width='10' height='10' patternUnits='userSpaceOnUse'><path d='M0 10L10 0' stroke='#2dd4bf' stroke-width='1' opacity='0.45'/></pattern>")
                .append("<style>")
                .append(".room-label{font-family:Arial,sans-serif;font-size:11px;font-weight:700;fill:#1f2937}.room-meta{font-family:Arial,sans-serif;font-size:9px;fill:#64748b}.dim-text{font-family:Arial,sans-serif;font-size:9px;fill:#374151}.thin{vector-effect:non-scaling-stroke}")
                .append("</style>")
                .append("</defs>");
        svg.append("<rect width='100%' height='100%' fill='#efe9dc'/>");
        svg.append("<rect x='10' y='8' width='").append(width - 20).append("' height='").append(height - 16).append("' fill='none' stroke='#57534e' stroke-width='1'/>");
        svg.append("<text x='").append(width / 2).append("' y='").append(titleY).append("' text-anchor='middle' font-family='Arial, sans-serif' font-size='15' font-weight='800' fill='#111827'>")
                .append(escape("Mặt bằng nhà phố Việt Nam hiện đại & nhiệt đới - " + floorLabel)).append("</text>");
        svg.append("<text x='").append(width / 2).append("' y='").append(titleY + 14).append("' text-anchor='middle' font-family='Arial, sans-serif' font-size='9' font-style='italic' fill='#57534e'>")
                .append(escape("Vietnamese tropical townhouse floor plan · kích thước theo mét")).append("</text>");
        svg.append("<rect x='").append(MARGIN - 8).append("' y='").append(MARGIN - 8).append("' width='").append(planWidth + 16).append("' height='").append(planHeight + 16)
                .append("' rx='2' fill='#faf7ef' stroke='#a8a29e' stroke-width='1.1' filter='url(#softShadow)'/>");
        svg.append("<rect x='").append(MARGIN).append("' y='").append(MARGIN).append("' width='").append(planWidth).append("' height='").append(planHeight).append("' fill='url(#paperGrid)' stroke='#78716c' stroke-width='0.8'/>");

        appendDimensionChains(svg, rooms, siteWidth, siteDepth);
        for (FloorplanRoom room : rooms) {
            appendRoom(svg, room);
        }
        for (FloorplanRoom room : rooms) {
            appendRoomDecoration(svg, room);
        }
        for (FloorplanFurniture item : furniture) {
            appendFurniture(svg, item);
        }
        for (FloorplanWall wall : walls) {
            appendWall(svg, wall);
        }
        for (FloorplanWindow window : windows) {
            appendWindow(svg, window);
        }
        for (FloorplanDoor door : doors) {
            appendDoor(svg, door);
        }
        appendDimensions(svg, siteWidth, siteDepth);
        appendNorthArrow(svg, width, height);
        appendLegend(svg, width, height);

        svg.append("</svg>");
        return svg.toString();
    }

    private void appendRoom(StringBuilder svg, FloorplanRoom room) {
        int x = px(room.x());
        int y = px(room.y());
        int w = toPixels(room.width());
        int d = toPixels(room.depth());
        String fill = switch (room.type()) {
            case "outdoor" -> "url(#gardenDots)";
            case "bathroom" -> "url(#wetTile)";
            case "void" -> "url(#voidHatch)";
            default -> room.color();
        };
        String baseFill = "outdoor".equals(room.type()) ? "#dcfce7" : room.color();
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(d)
                .append("' fill='").append(baseFill).append("' stroke='#a8a29e' stroke-width='0.8'/>");
        svg.append("<rect x='").append(x + 2).append("' y='").append(y + 2).append("' width='").append(Math.max(0, w - 4)).append("' height='").append(Math.max(0, d - 4))
                .append("' fill='").append(fill).append("' fill-opacity='0.72' stroke='none'/>");
        svg.append("<text class='room-label' x='").append(x + 7).append("' y='").append(y + 16).append("'>")
                .append(escape(room.label())).append("</text>");
        svg.append("<text class='room-meta' x='").append(x + 7).append("' y='").append(y + 30).append("'>")
                .append(formatArea(room.width() * room.depth())).append(" m² · ").append(formatNumber(room.width())).append("×").append(formatNumber(room.depth())).append("m</text>");
    }

    private void appendRoomDecoration(StringBuilder svg, FloorplanRoom room) {
        int x = px(room.x());
        int y = px(room.y());
        int w = toPixels(room.width());
        int d = toPixels(room.depth());
        if (w < 36 || d < 32) {
            return;
        }
        switch (room.type()) {
            case "outdoor", "void" -> appendPlantCluster(svg, x + w / 2, y + d / 2, Math.min(w, d));
            case "stairs" -> appendStairTreads(svg, x + 8, y + 8, w - 16, d - 16);
            case "bathroom" -> appendBathroomFixtures(svg, x, y, w, d);
            case "kitchen" -> appendCounterLine(svg, x, y, w, d);
            default -> {
                if (w > 100 && d > 70) {
                    svg.append("<path d='M ").append(x + 10).append(' ').append(y + d - 16).append(" H ").append(x + w - 10)
                            .append("' stroke='#c4b5a5' stroke-width='1' stroke-dasharray='4 5' fill='none' opacity='0.8'/>");
                }
            }
        }
    }

    private void appendPlantCluster(StringBuilder svg, int cx, int cy, int size) {
        int radius = Math.max(5, Math.min(13, size / 7));
        svg.append("<g fill='none' stroke='#166534' stroke-width='1' opacity='0.78'>");
        for (int i = -1; i <= 1; i++) {
            int ox = i * radius;
            svg.append("<circle cx='").append(cx + ox).append("' cy='").append(cy + (i % 2) * radius / 2).append("' r='").append(radius).append("' fill='#86efac' stroke='#15803d'/>");
            svg.append("<path d='M ").append(cx + ox).append(' ').append(cy + radius).append(" C ").append(cx + ox - radius).append(' ').append(cy)
                    .append(' ').append(cx + ox + radius).append(' ').append(cy).append(' ').append(cx + ox).append(' ').append(cy - radius).append("'/>");
        }
        svg.append("</g>");
    }

    private void appendStairTreads(StringBuilder svg, int x, int y, int w, int d) {
        int steps = Math.max(4, Math.min(9, d / 12));
        svg.append("<g stroke='#0369a1' stroke-width='1' fill='none' opacity='0.9'>");
        for (int i = 0; i <= steps; i++) {
            int yy = y + (int) Math.round(i * (d / (double) steps));
            svg.append("<line x1='").append(x).append("' y1='").append(yy).append("' x2='").append(x + w).append("' y2='").append(yy).append("'/>");
        }
        svg.append("<path d='M ").append(x + 5).append(' ').append(y + d - 7).append(" L ").append(x + w - 5).append(' ').append(y + 7)
                .append("' stroke-dasharray='3 3'/></g>");
    }

    private void appendBathroomFixtures(StringBuilder svg, int x, int y, int w, int d) {
        svg.append("<g fill='#f8fafc' stroke='#2563eb' stroke-width='0.9' opacity='0.9'>");
        svg.append("<rect x='").append(x + w - 28).append("' y='").append(y + 10).append("' width='18' height='16' rx='4'/>");
        svg.append("<circle cx='").append(x + 18).append("' cy='").append(y + d - 18).append("' r='9'/>");
        svg.append("<rect x='").append(x + 8).append("' y='").append(y + 8).append("' width='20' height='10' rx='2'/>");
        svg.append("</g>");
    }

    private void appendCounterLine(StringBuilder svg, int x, int y, int w, int d) {
        svg.append("<g stroke='#b45309' stroke-width='3' fill='none' opacity='0.55'>");
        svg.append("<path d='M ").append(x + 8).append(' ').append(y + 10).append(" H ").append(x + w - 8).append("'/>");
        svg.append("<path d='M ").append(x + 10).append(' ').append(y + 10).append(" V ").append(y + Math.min(d - 8, 50)).append("'/>");
        svg.append("</g>");
    }

    private void appendFurniture(StringBuilder svg, FloorplanFurniture item) {
        int x = px(item.x());
        int y = px(item.y());
        int w = toPixels(item.width());
        int d = toPixels(item.depth());
        svg.append("<g>");
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(d)
                .append("' rx='4' fill='").append(item.color()).append("' fill-opacity='0.82' stroke='#44403c' stroke-width='0.8'/>");
        if ("dining".equals(item.type())) {
            int chairs = Math.max(2, Math.min(6, w / 18));
            for (int i = 0; i < chairs; i++) {
                int cx = x + 8 + i * Math.max(12, (w - 16) / chairs);
                svg.append("<circle cx='").append(cx).append("' cy='").append(y - 5).append("' r='3.5' fill='#a16207' opacity='0.72'/>");
                svg.append("<circle cx='").append(cx).append("' cy='").append(y + d + 5).append("' r='3.5' fill='#a16207' opacity='0.72'/>");
            }
        } else if ("bed".equals(item.type())) {
            svg.append("<rect x='").append(x + 4).append("' y='").append(y + 4).append("' width='").append(Math.max(8, w / 3)).append("' height='").append(Math.max(8, d - 8)).append("' rx='2' fill='#faf5ff' opacity='0.8'/>");
        } else if ("sofa".equals(item.type())) {
            svg.append("<path d='M ").append(x + 5).append(' ').append(y + d - 5).append(" H ").append(x + w - 5).append("' stroke='#475569' stroke-width='3' stroke-linecap='round'/>");
        }
        if (w > 30 && d > 18) {
            svg.append("<text x='").append(x + 5).append("' y='").append(y + Math.min(15, d - 4)).append("' font-family='Arial, sans-serif' font-size='8.5' font-weight='700' fill='#1f2937'>")
                    .append(escape(item.label())).append("</text>");
        }
        svg.append("</g>");
    }

    private void appendWall(StringBuilder svg, FloorplanWall wall) {
        double thickness = Math.max(2.2, wall.thickness() * SCALE);
        svg.append("<line x1='").append(px(wall.x1())).append("' y1='").append(px(wall.y1())).append("' x2='").append(px(wall.x2())).append("' y2='").append(px(wall.y2()))
                .append("' stroke='").append("external".equals(wall.type()) ? "#111827" : "#292524")
                .append("' stroke-width='").append(formatNumber(thickness)).append("' stroke-linecap='square'/>");
    }

    private void appendDoor(StringBuilder svg, FloorplanDoor door) {
        int x = px(door.x());
        int y = px(door.y());
        int w = toPixels(door.width());
        svg.append("<g stroke='#b45309' stroke-width='1.7' fill='none'>");
        if ("vertical".equals(door.orientation())) {
            svg.append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x).append("' y2='").append(y + w).append("'/>");
            svg.append("<path d='M ").append(x).append(' ').append(y).append(" Q ").append(x - w).append(' ').append(y + w / 2).append(' ').append(x).append(' ').append(y + w).append("'/>");
        } else {
            svg.append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x + w).append("' y2='").append(y).append("'/>");
            svg.append("<path d='M ").append(x).append(' ').append(y).append(" Q ").append(x + w / 2).append(' ').append(y + w).append(' ').append(x + w).append(' ').append(y).append("'/>");
        }
        svg.append("<text x='").append(x + 3).append("' y='").append(y - 3).append("' font-family='Arial, sans-serif' font-size='7' fill='#92400e'>")
                .append(escape(door.label())).append("</text></g>");
    }

    private void appendWindow(StringBuilder svg, FloorplanWindow window) {
        int x = px(window.x());
        int y = px(window.y());
        int w = toPixels(window.width());
        svg.append("<g stroke='#0284c7' stroke-width='3' stroke-linecap='round'>");
        if ("vertical".equals(window.orientation())) {
            svg.append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x).append("' y2='").append(y + w).append("'/>");
            svg.append("<line x1='").append(x - 4).append("' y1='").append(y).append("' x2='").append(x - 4).append("' y2='").append(y + w).append("' stroke-width='1.5'/>");
        } else {
            svg.append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x + w).append("' y2='").append(y).append("'/>");
            svg.append("<line x1='").append(x).append("' y1='").append(y - 4).append("' x2='").append(x + w).append("' y2='").append(y - 4).append("' stroke-width='1.5'/>");
        }
        svg.append("</g>");
    }

    private void appendDimensions(StringBuilder svg, double siteWidth, double siteDepth) {
        int x1 = MARGIN;
        int y1 = MARGIN;
        int x2 = MARGIN + toPixels(siteWidth);
        int y2 = MARGIN + toPixels(siteDepth);
        svg.append("<g stroke='#57534e' stroke-width='0.9' fill='#374151' font-family='Arial, sans-serif' font-size='10'>");
        svg.append("<line x1='").append(x1).append("' y1='").append(y1 - 24).append("' x2='").append(x2).append("' y2='").append(y1 - 24).append("'/>");
        svg.append("<line x1='").append(x1).append("' y1='").append(y1 - 30).append("' x2='").append(x1).append("' y2='").append(y1 - 14).append("'/>");
        svg.append("<line x1='").append(x2).append("' y1='").append(y1 - 30).append("' x2='").append(x2).append("' y2='").append(y1 - 14).append("'/>");
        svg.append("<text x='").append((x1 + x2) / 2).append("' y='").append(y1 - 29).append("' text-anchor='middle'>").append(formatNumber(siteWidth)).append("m</text>");
        svg.append("<line x1='").append(x2 + 24).append("' y1='").append(y1).append("' x2='").append(x2 + 24).append("' y2='").append(y2).append("'/>");
        svg.append("<line x1='").append(x2 + 16).append("' y1='").append(y1).append("' x2='").append(x2 + 30).append("' y2='").append(y1).append("'/>");
        svg.append("<line x1='").append(x2 + 16).append("' y1='").append(y2).append("' x2='").append(x2 + 30).append("' y2='").append(y2).append("'/>");
        svg.append("<text x='").append(x2 + 30).append("' y='").append((y1 + y2) / 2).append("' transform='rotate(90 ").append(x2 + 30).append(' ').append((y1 + y2) / 2).append(")'>").append(formatNumber(siteDepth)).append("m</text>");
        svg.append("</g>");
    }

    private void appendDimensionChains(StringBuilder svg, List<FloorplanRoom> rooms, double siteWidth, double siteDepth) {
        List<Double> xs = collectBreaks(rooms, true, siteWidth);
        List<Double> ys = collectBreaks(rooms, false, siteDepth);
        int topY = MARGIN - 7;
        int leftX = MARGIN - 26;
        svg.append("<g class='dim-text' stroke='#78716c' stroke-width='0.7' fill='#44403c'>");
        for (int i = 0; i < xs.size() - 1; i++) {
            int x1 = px(xs.get(i));
            int x2 = px(xs.get(i + 1));
            if (x2 - x1 < 18) continue;
            svg.append("<line x1='").append(x1).append("' y1='").append(topY).append("' x2='").append(x2).append("' y2='").append(topY).append("'/>");
            svg.append("<line x1='").append(x1).append("' y1='").append(topY - 4).append("' x2='").append(x1).append("' y2='").append(topY + 4).append("'/>");
            svg.append("<text x='").append((x1 + x2) / 2).append("' y='").append(topY - 3).append("' text-anchor='middle'>").append(formatNumber(xs.get(i + 1) - xs.get(i))).append("</text>");
        }
        for (int i = 0; i < ys.size() - 1; i++) {
            int y1 = px(ys.get(i));
            int y2 = px(ys.get(i + 1));
            if (y2 - y1 < 18) continue;
            svg.append("<line x1='").append(leftX).append("' y1='").append(y1).append("' x2='").append(leftX).append("' y2='").append(y2).append("'/>");
            svg.append("<line x1='").append(leftX - 4).append("' y1='").append(y1).append("' x2='").append(leftX + 4).append("' y2='").append(y1).append("'/>");
            svg.append("<text x='").append(leftX - 4).append("' y='").append((y1 + y2) / 2).append("' text-anchor='end'>").append(formatNumber(ys.get(i + 1) - ys.get(i))).append("</text>");
        }
        svg.append("</g>");
    }

    private List<Double> collectBreaks(List<FloorplanRoom> rooms, boolean horizontal, double max) {
        List<Double> values = new ArrayList<>();
        values.add(0.0);
        values.add(round(max));
        for (FloorplanRoom room : rooms) {
            values.add(horizontal ? room.x() : room.y());
            values.add(horizontal ? round(room.x() + room.width()) : round(room.y() + room.depth()));
        }
        values.sort(Double::compareTo);
        List<Double> compact = new ArrayList<>();
        for (Double value : values) {
            if (value < -0.001 || value > max + 0.001) {
                continue;
            }
            if (compact.isEmpty() || Math.abs(compact.get(compact.size() - 1) - value) > 0.2) {
                compact.add(round(value));
            }
        }
        return compact;
    }

    private void appendNorthArrow(StringBuilder svg, int width, int height) {
        svg.append("<g transform='translate(").append(width - 40).append(' ').append(height - 74).append(")' font-family='Arial, sans-serif' fill='#111827'>")
                .append("<circle cx='8' cy='20' r='18' fill='#faf7ef' stroke='#57534e'/>")
                .append("<path d='M 8 3 L 14 24 L 8 19 L 2 24 Z' fill='#111827'/>")
                .append("<text x='4' y='44' font-size='11' font-weight='700'>N</text>")
                .append("</g>");
    }

    private void appendLegend(StringBuilder svg, int width, int height) {
        int y = height - 26;
        int x = Math.max(48, width - 218);
        svg.append("<g font-family='Arial, sans-serif' font-size='8.5' fill='#44403c'>")
                .append("<rect x='").append(x - 8).append("' y='").append(y - 31).append("' width='200' height='38' fill='#faf7ef' stroke='#a8a29e'/>")
                .append("<text x='").append(x).append("' y='").append(y - 19).append("' font-weight='700'>LEGEND</text>")
                .append("<line x1='").append(x).append("' y1='").append(y - 10).append("' x2='").append(x + 22).append("' y2='").append(y - 10).append("' stroke='#111827' stroke-width='3'/>")
                .append("<text x='").append(x + 28).append("' y='").append(y - 7).append("'>tường bao</text>")
                .append("<line x1='").append(x + 88).append("' y1='").append(y - 10).append("' x2='").append(x + 111).append("' y2='").append(y - 10).append("' stroke='#b45309' stroke-width='2'/>")
                .append("<text x='").append(x + 116).append("' y='").append(y - 7).append("'>cửa</text>")
                .append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x + 44).append("' y2='").append(y).append("' stroke='#111827' stroke-width='2'/>")
                .append("<text x='").append(x + 50).append("' y='").append(y + 3).append("'>thước 0-1m</text>")
                .append("</g>");
    }

    private FloorplanRoom room(String name, String label, String type, double x, double y, double width, double depth, String color) {
        return new FloorplanRoom(name, label, type, round(x), round(y), round(width), round(depth), color);
    }

    private int px(double meters) {
        return MARGIN + toPixels(meters);
    }

    private int toPixels(double meters) {
        return (int) Math.round(meters * SCALE);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatArea(double value) {
        return formatNumber(round(value));
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private String escape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&apos;")
                .replace("\"", "&quot;");
    }
}
