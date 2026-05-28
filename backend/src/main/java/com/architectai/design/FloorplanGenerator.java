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

        FloorplanLevel firstFloor = createFirstFloor(siteWidth, siteDepth, floorCount > 1);
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

    private FloorplanLevel createFirstFloor(double siteWidth, double siteDepth, boolean multiFloor) {
        double frontYardDepth = siteDepth >= 16 ? round(Math.min(2.2, Math.max(1.2, siteDepth * 0.09))) : 0;
        double livingDepth = round(Math.max(3.6, siteDepth * 0.23));
        double serviceDepth = round(Math.max(4.0, siteDepth * 0.24));
        double rearDepth = round(siteDepth - frontYardDepth - livingDepth - serviceDepth);
        if (rearDepth < 3.2) {
            rearDepth = 3.2;
            serviceDepth = round(siteDepth - frontYardDepth - livingDepth - rearDepth);
        }

        double stairWidth = round(Math.min(1.8, Math.max(1.15, siteWidth * 0.32)));
        double leftWidth = round(siteWidth - stairWidth);
        double livingY = frontYardDepth;
        double serviceY = round(livingY + livingDepth);
        double rearY = round(serviceY + serviceDepth);
        double stairDepth = round(serviceDepth * 0.55);
        double wcDepth = round(serviceDepth - stairDepth);

        List<FloorplanRoom> rooms = new ArrayList<>();
        if (frontYardDepth > 0) {
            rooms.add(room("front_yard", "Sân trước", "outdoor", 0, 0, siteWidth, frontYardDepth, "#dcfce7"));
        }
        rooms.add(room("living", "Phòng khách", "living", 0, livingY, siteWidth, livingDepth, "#fef3c7"));
        rooms.add(room("kitchen_dining", "Bếp + ăn", "kitchen", 0, serviceY, leftWidth, serviceDepth, "#fde68a"));
        rooms.add(room("stairs", "Thang", "stairs", leftWidth, serviceY, stairWidth, stairDepth, "#e0f2fe"));
        rooms.add(room("wc", "WC", "bathroom", leftWidth, round(serviceY + stairDepth), stairWidth, wcDepth, "#dbeafe"));
        rooms.add(room("lightwell", "Giếng trời", "void", round(leftWidth * 0.58), round(serviceY + 0.3), round(leftWidth * 0.34), round(Math.max(1.4, serviceDepth * 0.35)), "#ccfbf1"));

        if (multiFloor) {
            rooms.add(room("laundry_rear", "Sân sau / giặt", "service", 0, rearY, siteWidth, rearDepth, "#e2e8f0"));
        } else {
            rooms.add(room("bedroom", "Phòng ngủ", "bedroom", 0, rearY, siteWidth, rearDepth, "#ede9fe"));
        }

        List<FloorplanWall> walls = createWalls(siteWidth, siteDepth, rooms);
        List<FloorplanDoor> doors = createDoors(siteWidth, frontYardDepth, livingY, serviceY, rearY, leftWidth, stairWidth, serviceDepth, multiFloor);
        List<FloorplanWindow> windows = createWindows(siteWidth, siteDepth, frontYardDepth, livingY, serviceY, rearY, leftWidth, multiFloor);
        List<FloorplanFurniture> furniture = createFurniture(siteWidth, siteDepth, frontYardDepth, livingY, serviceY, rearY, leftWidth, stairWidth, serviceDepth, multiFloor);

        return new FloorplanLevel(
                1,
                "Tầng 1",
                siteWidth,
                siteDepth,
                rooms,
                walls,
                doors,
                windows,
                furniture,
                renderSvg(siteWidth, siteDepth, rooms, walls, doors, windows, furniture, "Tầng 1")
        );
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
        double balconyDepth = round(Math.min(1.5, Math.max(0.9, siteDepth * 0.07)));
        double frontDepth = round(Math.max(4.2, siteDepth * 0.28));
        double coreDepth = round(Math.max(4.4, siteDepth * 0.24));
        double rearDepth = round(siteDepth - balconyDepth - frontDepth - coreDepth);
        if (rearDepth < 3.4) {
            rearDepth = 3.4;
            coreDepth = round(siteDepth - balconyDepth - frontDepth - rearDepth);
        }

        double stairWidth = round(Math.min(1.8, Math.max(1.15, siteWidth * 0.32)));
        double leftWidth = round(siteWidth - stairWidth);
        double frontY = balconyDepth;
        double coreY = round(frontY + frontDepth);
        double rearY = round(coreY + coreDepth);
        double stairDepth = round(coreDepth * 0.58);
        double wcDepth = round(coreDepth - stairDepth);

        List<FloorplanRoom> rooms = new ArrayList<>();
        rooms.add(room("balcony_l" + level, topFloor ? "Sân thượng trước" : "Ban công", "outdoor", 0, 0, siteWidth, balconyDepth, "#bbf7d0"));
        rooms.add(room("front_bedroom_l" + level, topFloor ? "Phòng thờ / đa năng" : "Phòng ngủ trước", topFloor ? "worship" : "bedroom", 0, frontY, siteWidth, frontDepth, topFloor ? "#f5d0fe" : "#ede9fe"));
        rooms.add(room("family_l" + level, "Sinh hoạt chung", "living", 0, coreY, leftWidth, coreDepth, "#cffafe"));
        rooms.add(room("stairs_l" + level, "Thang", "stairs", leftWidth, coreY, stairWidth, stairDepth, "#e0f2fe"));
        rooms.add(room("wc_l" + level, bathrooms >= level ? "WC tầng " + level : "Kho/WC", "bathroom", leftWidth, round(coreY + stairDepth), stairWidth, wcDepth, "#dbeafe"));
        rooms.add(room("lightwell_l" + level, "Thông tầng", "void", round(leftWidth * 0.58), round(coreY + 0.3), round(leftWidth * 0.34), round(Math.max(1.4, coreDepth * 0.35)), "#ccfbf1"));
        rooms.add(room("rear_bedroom_l" + level, topFloor ? "Sân phơi sau" : "Phòng ngủ sau", topFloor ? "service" : "bedroom", 0, rearY, siteWidth, rearDepth, topFloor ? "#e2e8f0" : "#ddd6fe"));

        List<FloorplanWall> walls = createWalls(siteWidth, siteDepth, rooms);
        List<FloorplanDoor> doors = new ArrayList<>();
        doors.add(new FloorplanDoor("Cửa phòng trước", round(siteWidth / 2 - 0.45), frontY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor("Cửa sinh hoạt", round(leftWidth / 2 - 0.45), coreY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor("Cửa WC", leftWidth, round(coreY + coreDepth * 0.72), 0.75, "vertical", "in"));
        doors.add(new FloorplanDoor(topFloor ? "Cửa sân phơi" : "Cửa phòng sau", round(siteWidth / 2 - 0.45), rearY, 0.9, "horizontal", "in"));
        doors.add(new FloorplanDoor("Lối thang", leftWidth, round(coreY + 0.35), Math.min(0.85, stairWidth * 0.65), "vertical", "in"));

        List<FloorplanWindow> windows = new ArrayList<>();
        windows.add(new FloorplanWindow("Cửa ra ban công", round(siteWidth / 2 - 0.8), balconyDepth, 1.6, "horizontal"));
        windows.add(new FloorplanWindow("Thoáng phòng trước", round(siteWidth * 0.15), balconyDepth, round(siteWidth * 0.28), "horizontal"));
        windows.add(new FloorplanWindow("Thông tầng", round(leftWidth * 0.6), round(coreY + 0.3), round(leftWidth * 0.28), "horizontal"));
        windows.add(new FloorplanWindow(topFloor ? "Thoáng sân phơi" : "Cửa sổ phòng sau", round(siteWidth / 2 - 0.9), siteDepth, 1.8, "horizontal"));
        windows.add(new FloorplanWindow("Thoáng WC", siteWidth, round(coreY + 2.9), 0.8, "vertical"));

        List<FloorplanFurniture> furniture = new ArrayList<>();
        furniture.add(new FloorplanFurniture(topFloor ? "Bàn thờ" : "Giường trước", topFloor ? "altar" : "bed", round(siteWidth / 2 - 1.05), round(frontY + 0.8), 2.1, topFloor ? 0.75 : 1.8, 0, topFloor ? "#a855f7" : "#c4b5fd"));
        furniture.add(new FloorplanFurniture("Sofa nhỏ", "sofa", 0.45, round(coreY + 0.85), round(Math.min(2.0, leftWidth * 0.58)), 0.75, 0, "#94a3b8"));
        furniture.add(new FloorplanFurniture("Bậc thang", "stairs", round(leftWidth + 0.18), round(coreY + 0.35), round(stairWidth - 0.36), 2.1, 0, "#38bdf8"));
        furniture.add(new FloorplanFurniture("Lavabo", "sink", round(leftWidth + stairWidth * 0.25), round(coreY + coreDepth - 1.05), 0.5, 0.5, 0, "#60a5fa"));
        if (topFloor) {
            furniture.add(new FloorplanFurniture("Máy giặt", "washer", 0.45, round(siteDepth - 1.1), 0.7, 0.7, 0, "#64748b"));
            furniture.add(new FloorplanFurniture("Bồn cây", "planter", round(siteWidth - 1.25), round(siteDepth - 1.15), 0.85, 0.65, 0, "#22c55e"));
        } else {
            furniture.add(new FloorplanFurniture("Giường sau", "bed", round(siteWidth / 2 - 1.05), round(rearY + 0.65), 2.1, 1.8, 0, "#c4b5fd"));
            furniture.add(new FloorplanFurniture("Tủ áo", "wardrobe", 0.35, round(siteDepth - 0.85), round(Math.min(2.2, siteWidth - 0.7)), 0.5, 0, "#8b5cf6"));
        }

        String label = "Tầng " + level;
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
        windows.add(new FloorplanWindow("Thông gió bếp", 0, round(serviceY + 1.0), 1.2, "vertical"));
        windows.add(new FloorplanWindow("Giếng trời", round(leftWidth * 0.6), round(serviceY + 0.3), round(leftWidth * 0.28), "horizontal"));
        windows.add(new FloorplanWindow(multiFloor ? "Cửa thoáng sân sau" : "Cửa sổ phòng ngủ", round(siteWidth / 2 - 0.9), siteDepth, 1.8, "horizontal"));
        windows.add(new FloorplanWindow("Thoáng WC", siteWidth, round(serviceY + 2.9), 0.8, "vertical"));
        windows.add(new FloorplanWindow("Cửa sổ sau", round(siteWidth / 2 - 0.7), rearY, 1.4, "horizontal"));
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
            boolean multiFloor
    ) {
        List<FloorplanFurniture> furniture = new ArrayList<>();
        furniture.add(new FloorplanFurniture("Sofa", "sofa", 0.45, round(livingY + 0.75), round(Math.min(2.5, siteWidth * 0.48)), 0.85, 0, "#94a3b8"));
        furniture.add(new FloorplanFurniture("TV", "media", round(siteWidth - 0.28), round(livingY + 1.0), 0.12, round(Math.min(1.8, siteWidth * 0.35)), 90, "#334155"));
        furniture.add(new FloorplanFurniture("Bếp chữ I", "kitchen_counter", 0.35, round(serviceY + 0.45), round(Math.min(2.8, leftWidth - 0.7)), 0.62, 0, "#f97316"));
        furniture.add(new FloorplanFurniture("Bàn ăn", "dining", round(Math.max(0.5, leftWidth / 2 - 0.8)), round(serviceY + serviceDepth - 1.65), 1.6, 0.9, 0, "#a16207"));
        furniture.add(new FloorplanFurniture("Bồn cầu", "toilet", round(leftWidth + stairWidth * 0.25), round(serviceY + serviceDepth - 1.05), 0.48, 0.7, 0, "#60a5fa"));
        furniture.add(new FloorplanFurniture("Bậc thang", "stairs", round(leftWidth + 0.18), round(serviceY + 0.35), round(stairWidth - 0.36), 2.1, 0, "#38bdf8"));
        furniture.add(new FloorplanFurniture("Cây xanh", "plant", round(siteWidth * 0.72), round(serviceY + 0.65), 0.55, 0.55, 0, "#16a34a"));
        if (multiFloor) {
            furniture.add(new FloorplanFurniture("Máy giặt", "washer", 0.45, round(siteDepth - 1.15), 0.7, 0.7, 0, "#64748b"));
            furniture.add(new FloorplanFurniture("Bồn cây", "planter", round(siteWidth - 1.35), round(siteDepth - 1.2), 0.9, 0.7, 0, "#22c55e"));
        } else {
            furniture.add(new FloorplanFurniture("Giường", "bed", round(siteWidth / 2 - 1.05), round(rearY + 0.7), 2.1, 1.8, 0, "#c4b5fd"));
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
        int width = toPixels(siteWidth) + MARGIN * 2;
        int height = toPixels(siteDepth) + MARGIN * 2;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 ").append(width).append(' ').append(height).append("'>");
        svg.append("<defs>")
                .append("<filter id='softShadow' x='-10%' y='-10%' width='120%' height='120%'><feDropShadow dx='0' dy='2' stdDeviation='2' flood-color='#0f172a' flood-opacity='0.16'/></filter>")
                .append("<pattern id='grid' width='34' height='34' patternUnits='userSpaceOnUse'><path d='M 34 0 L 0 0 0 34' fill='none' stroke='#e2e8f0' stroke-width='0.7'/></pattern>")
                .append("</defs>");
        svg.append("<rect width='100%' height='100%' fill='#f8fafc'/>");
        svg.append("<text x='48' y='28' font-family='Arial, sans-serif' font-size='16' font-weight='700' fill='#0f172a'>").append(escape(floorLabel)).append("</text>");
        svg.append("<rect x='").append(MARGIN).append("' y='").append(MARGIN).append("' width='").append(toPixels(siteWidth)).append("' height='").append(toPixels(siteDepth)).append("' fill='url(#grid)'/>");

        for (FloorplanRoom room : rooms) {
            appendRoom(svg, room);
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
        appendNorthArrow(svg, width);
        appendLegend(svg, height);

        svg.append("</svg>");
        return svg.toString();
    }

    private void appendRoom(StringBuilder svg, FloorplanRoom room) {
        int x = px(room.x());
        int y = px(room.y());
        int w = toPixels(room.width());
        int d = toPixels(room.depth());
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(d)
                .append("' rx='4' fill='").append(room.color()).append("' stroke='#94a3b8' stroke-width='1' filter='url(#softShadow)'/>");
        svg.append("<text x='").append(x + 8).append("' y='").append(y + 18).append("' font-family='Arial, sans-serif' font-size='12' font-weight='700' fill='#0f172a'>")
                .append(escape(room.label())).append("</text>");
        svg.append("<text x='").append(x + 8).append("' y='").append(y + 34).append("' font-family='Arial, sans-serif' font-size='10' fill='#475569'>")
                .append(formatArea(room.width() * room.depth())).append(" m²</text>");
    }

    private void appendFurniture(StringBuilder svg, FloorplanFurniture item) {
        int x = px(item.x());
        int y = px(item.y());
        int w = toPixels(item.width());
        int d = toPixels(item.depth());
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(d)
                .append("' rx='5' fill='").append(item.color()).append("' fill-opacity='0.78' stroke='#334155' stroke-width='0.8'/>");
        if (w > 30 && d > 18) {
            svg.append("<text x='").append(x + 5).append("' y='").append(y + Math.min(16, d - 4)).append("' font-family='Arial, sans-serif' font-size='9' fill='#0f172a'>")
                    .append(escape(item.label())).append("</text>");
        }
    }

    private void appendWall(StringBuilder svg, FloorplanWall wall) {
        double thickness = Math.max(2.2, wall.thickness() * SCALE);
        svg.append("<line x1='").append(px(wall.x1())).append("' y1='").append(px(wall.y1())).append("' x2='").append(px(wall.x2())).append("' y2='").append(px(wall.y2()))
                .append("' stroke='").append("external".equals(wall.type()) ? "#0f172a" : "#334155")
                .append("' stroke-width='").append(formatNumber(thickness)).append("' stroke-linecap='square'/>");
    }

    private void appendDoor(StringBuilder svg, FloorplanDoor door) {
        int x = px(door.x());
        int y = px(door.y());
        int w = toPixels(door.width());
        svg.append("<g stroke='#92400e' stroke-width='2' fill='none'>");
        if ("vertical".equals(door.orientation())) {
            svg.append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x).append("' y2='").append(y + w).append("'/>");
            svg.append("<path d='M ").append(x).append(' ').append(y).append(" Q ").append(x - w).append(' ').append(y + w / 2).append(' ').append(x).append(' ').append(y + w).append("'/>");
        } else {
            svg.append("<line x1='").append(x).append("' y1='").append(y).append("' x2='").append(x + w).append("' y2='").append(y).append("'/>");
            svg.append("<path d='M ").append(x).append(' ').append(y).append(" Q ").append(x + w / 2).append(' ').append(y + w).append(' ').append(x + w).append(' ').append(y).append("'/>");
        }
        svg.append("</g>");
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
        svg.append("<g stroke='#475569' stroke-width='1' fill='#475569' font-family='Arial, sans-serif' font-size='11'>");
        svg.append("<line x1='").append(x1).append("' y1='").append(y1 - 18).append("' x2='").append(x2).append("' y2='").append(y1 - 18).append("'/>");
        svg.append("<text x='").append((x1 + x2) / 2 - 18).append("' y='").append(y1 - 24).append("'>").append(formatNumber(siteWidth)).append("m</text>");
        svg.append("<line x1='").append(x2 + 18).append("' y1='").append(y1).append("' x2='").append(x2 + 18).append("' y2='").append(y2).append("'/>");
        svg.append("<text x='").append(x2 + 23).append("' y='").append((y1 + y2) / 2).append("'>").append(formatNumber(siteDepth)).append("m</text>");
        svg.append("</g>");
    }

    private void appendNorthArrow(StringBuilder svg, int width) {
        svg.append("<g transform='translate(").append(width - 42).append(" 24)' font-family='Arial, sans-serif' fill='#0f172a'>")
                .append("<path d='M 0 22 L 8 0 L 16 22 L 8 17 Z' fill='#0f172a'/>")
                .append("<text x='3' y='36' font-size='12' font-weight='700'>N</text>")
                .append("</g>");
    }

    private void appendLegend(StringBuilder svg, int height) {
        int y = height - 22;
        svg.append("<g font-family='Arial, sans-serif' font-size='10' fill='#475569'>")
                .append("<text x='48' y='").append(y).append("'>Tường đậm = tường bao | Nâu = cửa | Xanh = cửa sổ/thoáng | Diện tích hiển thị theo m²</text>")
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
