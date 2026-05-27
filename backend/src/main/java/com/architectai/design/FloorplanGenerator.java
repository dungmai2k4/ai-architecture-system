package com.architectai.design;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FloorplanGenerator {

    public Floorplan generate(DesignBrief brief) {
        double siteWidth = brief.siteWidthMeters();
        double siteDepth = brief.siteDepthMeters();

        double frontDepth = round(siteDepth * 0.35);
        double middleDepth = round(siteDepth * 0.35);
        double backDepth = round(siteDepth - frontDepth - middleDepth);

        List<FloorplanRoom> rooms = new ArrayList<>();
        rooms.add(new FloorplanRoom("living", 0, 0, siteWidth, frontDepth));

        double wcWidth = round(Math.min(2.0, Math.max(1.6, siteWidth * 0.35)));
        rooms.add(new FloorplanRoom("kitchen_dining", 0, frontDepth, siteWidth - wcWidth, middleDepth));
        rooms.add(new FloorplanRoom("wc", siteWidth - wcWidth, frontDepth, wcWidth, middleDepth));

        double rearY = frontDepth + middleDepth;
        rooms.add(new FloorplanRoom("bedroom", 0, rearY, siteWidth, backDepth));

        return new Floorplan(siteWidth, siteDepth, rooms, renderSvg(siteWidth, siteDepth, rooms));
    }

    private String renderSvg(double siteWidth, double siteDepth, List<FloorplanRoom> rooms) {
        int scale = 24;
        int width = (int) Math.round(siteWidth * scale);
        int height = (int) Math.round(siteDepth * scale);

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 ").append(width).append(' ').append(height).append("'>");
        svg.append("<rect x='0' y='0' width='").append(width).append("' height='").append(height).append("' fill='#f8fafc' stroke='#0f172a' stroke-width='2'/>");

        for (FloorplanRoom room : rooms) {
            int x = (int) Math.round(room.x() * scale);
            int y = (int) Math.round(room.y() * scale);
            int w = (int) Math.round(room.width() * scale);
            int d = (int) Math.round(room.depth() * scale);
            svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(d)
                    .append("' fill='#e2e8f0' stroke='#334155' stroke-width='1.5'/>");
            svg.append("<text x='").append(x + 6).append("' y='").append(y + 18)
                    .append("' font-size='12' fill='#0f172a'>").append(room.name()).append("</text>");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
