package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ArchitecturalDrawingGenerator {

    private static final int WIDTH = 920;
    private static final int HEIGHT = 620;
    private static final int LEFT = 110;
    private static final int BASELINE = 500;

    public ArchitecturalDrawings generate(
            DesignBrief brief,
            RoofPlan roofPlan,
            List<BuildingSection> sections,
            FacadeComposition facade,
            ExteriorStyle style,
            CourtyardPlan courtyard
    ) {
        int floors = Math.max(1, brief.floors());
        double siteWidth = Math.max(3.5, brief.siteWidthMeters());
        double siteDepth = Math.max(8, brief.siteDepthMeters());
        double buildingHeight = floors * 3.35 + roofVisualHeight(roofPlan, siteWidth);

        ArchitecturalDrawings.DrawingSheet exterior = new ArchitecturalDrawings.DrawingSheet(
                "Phối cảnh ngoại thất sơ bộ",
                "exterior-perspective",
                "concept axonometric",
                renderExteriorPerspective(brief, roofPlan, facade, style, courtyard, floors, siteWidth, siteDepth),
                List.of(
                        "Khối nhà được dựng thành bản vẽ phối cảnh thay cho mô tả văn bản.",
                        "Thể hiện ban công, lam che nắng, cây xanh và vật liệu chính.",
                        "Dùng để duyệt ý tưởng ngoại thất trước khi triển khai 3D chi tiết."
                )
        );
        ArchitecturalDrawings.DrawingSheet roof = new ArchitecturalDrawings.DrawingSheet(
                "Bản vẽ mặt bằng mái",
                "roof-plan",
                "1:100 concept",
                renderRoofPlan(brief, roofPlan, courtyard, siteWidth, siteDepth),
                List.of(
                        roofPlan.roofType() + " dốc " + roofPlan.slopeDegrees() + "°.",
                        "Mái đua " + roofPlan.overhangMeters() + "m bao che mặt đứng.",
                        "Mũi tên chỉ hướng thoát nước và vị trí skylight/sân trong."
                )
        );
        ArchitecturalDrawings.DrawingSheet section = new ArchitecturalDrawings.DrawingSheet(
                "Mặt cắt khí hậu dọc nhà",
                "longitudinal-section",
                "1:100 concept",
                renderSection(brief, roofPlan, sections, courtyard, floors, siteDepth, buildingHeight),
                List.of(
                        "Có cao độ tầng, sàn, thang và giếng trời.",
                        "Mũi tên gió/nhiệt thể hiện thông gió thụ động.",
                        "Phù hợp kiểm tra quan hệ mái - sân trong - lõi thang."
                )
        );
        ArchitecturalDrawings.DrawingSheet elevation = new ArchitecturalDrawings.DrawingSheet(
                "Mặt đứng chính",
                "front-elevation",
                "1:100 concept",
                renderElevation(brief, roofPlan, facade, style, floors, siteWidth, buildingHeight),
                List.of(
                        facade.compositionType() + " với " + facade.bays() + " nhịp mặt đứng.",
                        "Thể hiện cửa, ban công, lam đứng và mảng xanh.",
                        "Có đường kích thước tổng chiều rộng và cao độ các tầng."
                )
        );

        return new ArchitecturalDrawings(
                exterior,
                roof,
                section,
                elevation,
                List.of(
                        "Các SVG là bản vẽ sơ bộ có hình học, nhãn và callout để thay thế phần mô tả thuần chữ.",
                        "Kích thước concept dựa trên brief; cần kiến trúc sư hiệu chỉnh trước hồ sơ kỹ thuật.",
                        "Có thể xuất trực tiếp từ JSON response để hiển thị hoặc lưu file SVG."
                )
        );
    }

    private String renderExteriorPerspective(
            DesignBrief brief,
            RoofPlan roofPlan,
            FacadeComposition facade,
            ExteriorStyle style,
            CourtyardPlan courtyard,
            int floors,
            double siteWidth,
            double siteDepth
    ) {
        StringBuilder svg = baseSvg("Phối cảnh ngoại thất", brief, "axonometric concept");
        int bodyW = 330;
        int floorH = Math.max(58, Math.min(88, 260 / floors));
        int bodyH = floorH * floors;
        int x = 280;
        int y = BASELINE - bodyH;
        int dx = 90;
        int dy = -55;
        svg.append("<polygon points='").append(x).append(',').append(y).append(' ').append(x + bodyW).append(',').append(y)
                .append(' ').append(x + bodyW + dx).append(',').append(y + dy).append(' ').append(x + dx).append(',').append(y + dy)
                .append("' fill='#e2e8f0' stroke='#334155' stroke-width='2'/>");
        svg.append("<polygon points='").append(x + bodyW).append(',').append(y).append(' ').append(x + bodyW + dx).append(',').append(y + dy)
                .append(' ').append(x + bodyW + dx).append(',').append(BASELINE + dy).append(' ').append(x + bodyW).append(',').append(BASELINE)
                .append("' fill='#cbd5e1' stroke='#334155' stroke-width='2'/>");
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(bodyW).append("' height='").append(bodyH)
                .append("' fill='#f8fafc' stroke='#334155' stroke-width='2'/>");

        for (int level = 0; level < floors; level++) {
            int fy = BASELINE - (level + 1) * floorH;
            svg.append("<line x1='").append(x).append("' y1='").append(fy).append("' x2='").append(x + bodyW).append("' y2='").append(fy).append("' stroke='#94a3b8' stroke-width='1'/>");
            appendFacadeOpenings(svg, x, fy + 10, bodyW, floorH, facade.bays(), level == 0, true);
            if (level > 0) {
                int balconyX = x + 34;
                svg.append("<rect x='").append(balconyX).append("' y='").append(fy + floorH - 24).append("' width='110' height='16' fill='#bae6fd' stroke='#0369a1'/>");
                svg.append("<line x1='").append(balconyX).append("' y1='").append(fy + floorH - 24).append("' x2='").append(balconyX).append("' y2='").append(fy + floorH - 42).append("' stroke='#0369a1'/>");
            }
        }
        appendRoofShape(svg, x - 20, y, bodyW + 40, roofPlan, true);
        appendVerticalFins(svg, x + bodyW - 74, y + 12, 52, bodyH - 24, 5);
        appendGreenery(svg, x + 20, BASELINE - 54, 84, 44);
        appendGreenery(svg, x + bodyW + dx - 40, BASELINE + dy - 52, 70, 42);
        appendCallout(svg, 120, 150, x + bodyW - 45, y + 55, "Lam & screen chống nắng");
        appendCallout(svg, 650, 150, x + bodyW / 2, y - 36, roofPlan.roofType());
        appendCallout(svg, 650, 420, x + 75, BASELINE - 42, "Ban công + cây xanh");
        appendLegendBlock(svg, style.materialPalette(), 112, 410);
        appendScaleBar(svg, siteWidth + "m mặt tiền · " + siteDepth + "m chiều sâu");
        return closeSvg(svg);
    }

    private String renderRoofPlan(DesignBrief brief, RoofPlan roofPlan, CourtyardPlan courtyard, double siteWidth, double siteDepth) {
        StringBuilder svg = baseSvg("Mặt bằng mái", brief, "roof plan");
        int x = 230;
        int y = 105;
        int w = 430;
        int h = 360;
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(h).append("' fill='#fefce8' stroke='#334155' stroke-width='2'/>");
        int overhang = (int) Math.round(roofPlan.overhangMeters() * 18);
        svg.append("<rect x='").append(x - overhang).append("' y='").append(y - overhang).append("' width='").append(w + overhang * 2).append("' height='").append(h + overhang * 2)
                .append("' fill='none' stroke='#f97316' stroke-width='2' stroke-dasharray='8 6'/>");
        if (roofPlan.roofType().contains("Gable") || roofPlan.roofType().contains("Thai") || roofPlan.roofType().contains("Hip") || roofPlan.roofType().contains("Japanese")) {
            svg.append("<line x1='").append(x + w / 2).append("' y1='").append(y).append("' x2='").append(x + w / 2).append("' y2='").append(y + h).append("' stroke='#b45309' stroke-width='3'/>");
            svg.append("<path d='M ").append(x).append(' ').append(y).append(" L ").append(x + w / 2).append(' ').append(y + h / 2).append(" L ").append(x + w).append(' ').append(y)
                    .append(" M ").append(x).append(' ').append(y + h).append(" L ").append(x + w / 2).append(' ').append(y + h / 2).append(" L ").append(x + w).append(' ').append(y + h)
                    .append("' fill='none' stroke='#92400e' stroke-width='1.5' stroke-dasharray='5 5'/>");
        } else {
            for (int i = 0; i < 9; i++) {
                int yy = y + 24 + i * 34;
                svg.append("<line x1='").append(x + 24).append("' y1='").append(yy).append("' x2='").append(x + w - 24).append("' y2='").append(yy - 26).append("' stroke='#b45309' stroke-width='1.4' opacity='0.7'/>");
            }
        }
        int courtX = x + (int) Math.round((courtyard.x() / siteWidth) * w);
        int courtY = y + (int) Math.round((courtyard.y() / siteDepth) * h);
        int courtW = Math.max(46, (int) Math.round((courtyard.width() / siteWidth) * w));
        int courtH = Math.max(50, (int) Math.round((courtyard.depth() / siteDepth) * h));
        svg.append("<rect x='").append(courtX).append("' y='").append(courtY).append("' width='").append(courtW).append("' height='").append(courtH)
                .append("' fill='#ccfbf1' stroke='#0f766e' stroke-width='2'/>");
        svg.append("<text x='").append(courtX + courtW / 2).append("' y='").append(courtY + courtH / 2).append("' text-anchor='middle' class='label'>Skylight</text>");
        appendArrow(svg, x + w / 2, y + h / 2, x + w - 36, y + h - 32, "thoát nước");
        appendArrow(svg, x + w / 2, y + h / 2, x + 34, y + h - 28, "thoát nước");
        appendCallout(svg, 116, 166, x - overhang, y - overhang, "Đường mái đua");
        appendCallout(svg, 680, 210, courtX + courtW, courtY + courtH / 2, "Skylight/sân trong");
        appendDimension(svg, x, y + h + 40, x + w, y + h + 40, format(siteWidth) + "m");
        appendDimension(svg, x - 42, y, x - 42, y + h, format(siteDepth) + "m");
        appendScaleBar(svg, roofPlan.roofType() + " · slope " + roofPlan.slopeDegrees() + "°");
        return closeSvg(svg);
    }

    private String renderSection(DesignBrief brief, RoofPlan roofPlan, List<BuildingSection> sections, CourtyardPlan courtyard, int floors, double siteDepth, double buildingHeight) {
        StringBuilder svg = baseSvg("Mặt cắt dọc", brief, "longitudinal section");
        int x = 145;
        int yBase = BASELINE;
        int w = 625;
        int floorH = Math.max(58, Math.min(82, 285 / floors));
        int bodyH = floorH * floors;
        int yTop = yBase - bodyH;
        svg.append("<rect x='").append(x).append("' y='").append(yTop).append("' width='").append(w).append("' height='").append(bodyH).append("' fill='#f8fafc' stroke='#334155' stroke-width='2'/>");
        int voidX = x + (int) Math.round(w * 0.44);
        int voidW = Math.max(70, (int) Math.round((courtyard.depth() / siteDepth) * w));
        svg.append("<rect x='").append(voidX).append("' y='").append(yTop).append("' width='").append(voidW).append("' height='").append(bodyH)
                .append("' fill='#ecfeff' stroke='#0891b2' stroke-width='1.8' stroke-dasharray='6 5'/>");
        svg.append("<text x='").append(voidX + voidW / 2).append("' y='").append(yTop + 28).append("' text-anchor='middle' class='label'>Giếng trời</text>");
        for (int level = 0; level < floors; level++) {
            int fy = yBase - (level + 1) * floorH;
            svg.append("<line x1='").append(x).append("' y1='").append(fy).append("' x2='").append(x + w).append("' y2='").append(fy).append("' stroke='#475569' stroke-width='2'/>");
            svg.append("<text x='").append(x - 16).append("' y='").append(fy + floorH / 2).append("' text-anchor='end' class='small'>Tầng ").append(level + 1).append("</text>");
            appendRoomMass(svg, x + 28, fy + 14, 165, floorH - 22, level == 0 ? "Khách/Bếp" : "Phòng ngủ");
            appendRoomMass(svg, x + w - 190, fy + 14, 150, floorH - 22, level == floors - 1 ? "Đa năng" : "Phòng chính");
        }
        appendStairSection(svg, voidX + voidW + 26, yTop + 12, 84, bodyH - 24, floors);
        appendRoofShape(svg, x - 20, yTop, w + 40, roofPlan, false);
        appendArrow(svg, voidX + voidW / 2, yBase - 20, voidX + voidW / 2, yTop - 65, "khí nóng thoát lên");
        appendArrow(svg, x + 20, yBase - 55, voidX + 8, yBase - 108, "gió vào");
        appendCallout(svg, 650, 145, voidX + voidW / 2, yTop + 58, "Skylight mở được");
        appendCallout(svg, 120, 170, x + 86, yBase - 36, "Không gian sinh hoạt");
        appendDimension(svg, x + w + 34, yBase, x + w + 34, yTop, format(buildingHeight) + "m tổng cao");
        if (!sections.isEmpty()) {
            appendNote(svg, 120, 540, sections.get(0).roofSection());
        }
        appendScaleBar(svg, "Cao tầng 1: 3.6m · tầng điển hình: 3.3m");
        return closeSvg(svg);
    }

    private String renderElevation(DesignBrief brief, RoofPlan roofPlan, FacadeComposition facade, ExteriorStyle style, int floors, double siteWidth, double buildingHeight) {
        StringBuilder svg = baseSvg("Mặt đứng chính", brief, "front elevation");
        int bodyW = 430;
        int floorH = Math.max(58, Math.min(82, 285 / floors));
        int bodyH = floorH * floors;
        int x = 245;
        int y = BASELINE - bodyH;
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(bodyW).append("' height='").append(bodyH)
                .append("' fill='#f8fafc' stroke='#334155' stroke-width='2.2'/>");
        int bayCount = Math.max(2, Math.min(4, facade.bays()));
        for (int b = 1; b < bayCount; b++) {
            int bx = x + b * bodyW / bayCount;
            svg.append("<line x1='").append(bx).append("' y1='").append(y).append("' x2='").append(bx).append("' y2='").append(BASELINE)
                    .append("' stroke='#cbd5e1' stroke-width='1' stroke-dasharray='5 5'/>");
        }
        for (int level = 0; level < floors; level++) {
            int fy = BASELINE - (level + 1) * floorH;
            svg.append("<line x1='").append(x).append("' y1='").append(fy).append("' x2='").append(x + bodyW).append("' y2='").append(fy).append("' stroke='#94a3b8'/>");
            appendFacadeOpenings(svg, x, fy + 8, bodyW, floorH, bayCount, level == 0, false);
            if (level > 0) {
                svg.append("<rect x='").append(x + 28).append("' y='").append(fy + floorH - 22).append("' width='138' height='16' fill='#bae6fd' stroke='#0369a1'/>");
                svg.append("<path d='M ").append(x + 40).append(' ').append(fy + floorH - 22).append(" v-18 M ").append(x + 78).append(' ').append(fy + floorH - 22)
                        .append(" v-18 M ").append(x + 116).append(' ').append(fy + floorH - 22).append(" v-18' stroke='#0369a1'/>");
            }
        }
        appendRoofShape(svg, x - 24, y, bodyW + 48, roofPlan, false);
        appendVerticalFins(svg, x + bodyW - 92, y + 20, 64, bodyH - 42, 6);
        appendGreenery(svg, x + 30, BASELINE - 48, 110, 38);
        appendCallout(svg, 120, 150, x + bodyW - 58, y + 70, "Lam đứng / sun screen");
        appendCallout(svg, 682, 175, x + bodyW / 2, y - 36, roofPlan.roofType());
        appendCallout(svg, 690, 402, x + 86, BASELINE - 38, "Mảng xanh ban công");
        appendDimension(svg, x, BASELINE + 42, x + bodyW, BASELINE + 42, format(siteWidth) + "m mặt tiền");
        appendDimension(svg, x + bodyW + 42, BASELINE, x + bodyW + 42, y, format(buildingHeight) + "m");
        appendLegendBlock(svg, style.materialPalette(), 112, 410);
        appendScaleBar(svg, facade.compositionType());
        return closeSvg(svg);
    }

    private StringBuilder baseSvg(String title, DesignBrief brief, String subtitle) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 ").append(WIDTH).append(' ').append(HEIGHT).append("'>");
        svg.append("<defs><style>")
                .append(".title{font-family:Arial,sans-serif;font-size:22px;font-weight:800;fill:#0f172a}")
                .append(".subtitle{font-family:Arial,sans-serif;font-size:12px;fill:#64748b}")
                .append(".label{font-family:Arial,sans-serif;font-size:12px;font-weight:700;fill:#0f172a}")
                .append(".small{font-family:Arial,sans-serif;font-size:10px;fill:#475569}")
                .append(".callout{font-family:Arial,sans-serif;font-size:11px;font-weight:700;fill:#1e293b}")
                .append("</style><marker id='arrow' markerWidth='10' markerHeight='10' refX='8' refY='3' orient='auto'><path d='M0,0 L0,6 L9,3 z' fill='#0f766e'/></marker>")
                .append("<pattern id='screenHatch' width='8' height='8' patternUnits='userSpaceOnUse'><path d='M0 8L8 0' stroke='#0ea5e9' stroke-width='1' opacity='0.5'/></pattern>")
                .append("</defs>");
        svg.append("<rect width='100%' height='100%' fill='#f8fafc'/>");
        svg.append("<rect x='18' y='18' width='").append(WIDTH - 36).append("' height='").append(HEIGHT - 36).append("' fill='none' stroke='#334155' stroke-width='1.2'/>");
        svg.append("<text x='42' y='54' class='title'>").append(escape(title)).append("</text>");
        svg.append("<text x='42' y='75' class='subtitle'>").append(escape(subtitle + " · " + brief.siteWidthMeters() + "m × " + brief.siteDepthMeters() + "m · " + brief.floors() + " tầng")).append("</text>");
        return svg;
    }

    private String closeSvg(StringBuilder svg) {
        svg.append("</svg>");
        return svg.toString();
    }

    private void appendRoofShape(StringBuilder svg, int x, int y, int w, RoofPlan roofPlan, boolean perspective) {
        int roofH = roofVisualHeightPx(roofPlan, w);
        if (roofPlan.roofType().contains("Gable") || roofPlan.roofType().contains("Thai") || roofPlan.roofType().contains("Hip") || roofPlan.roofType().contains("Japanese")) {
            svg.append("<polygon points='").append(x).append(',').append(y).append(' ').append(x + w / 2).append(',').append(y - roofH).append(' ').append(x + w).append(',').append(y)
                    .append("' fill='#b45309' stroke='#7c2d12' stroke-width='2'/>");
            svg.append("<line x1='").append(x + 20).append("' y1='").append(y - 4).append("' x2='").append(x + w / 2).append("' y2='").append(y - roofH + 8).append("' stroke='#fed7aa' opacity='0.6'/>");
        } else if (roofPlan.roofType().contains("Green")) {
            svg.append("<rect x='").append(x).append("' y='").append(y - 28).append("' width='").append(w).append("' height='28' fill='#86efac' stroke='#15803d' stroke-width='2'/>");
            for (int i = 0; i < 11; i++) {
                svg.append("<circle cx='").append(x + 18 + i * Math.max(18, w / 12)).append("' cy='").append(y - 14).append("' r='5' fill='#16a34a' opacity='0.75'/>");
            }
        } else {
            svg.append("<polygon points='").append(x).append(',').append(y - 12).append(' ').append(x + w).append(',').append(y - roofH).append(' ').append(x + w).append(',').append(y)
                    .append(' ').append(x).append(',').append(y).append("' fill='#64748b' stroke='#334155' stroke-width='2'/>");
        }
        if (perspective) {
            svg.append("<text x='").append(x + w + 16).append("' y='").append(y - Math.max(18, roofH / 2)).append("' class='small'>").append(escape(roofPlan.roofType())).append("</text>");
        }
    }

    private void appendFacadeOpenings(StringBuilder svg, int x, int y, int w, int h, int bays, boolean ground, boolean perspective) {
        int bayCount = Math.max(2, Math.min(4, bays));
        int bayW = w / bayCount;
        for (int i = 0; i < bayCount; i++) {
            int ox = x + i * bayW + Math.max(12, bayW / 5);
            int ow = Math.max(38, bayW / 2);
            int oh = ground && i == 0 ? h - 18 : Math.max(28, h / 2);
            int oy = ground && i == 0 ? y + 8 : y + 8;
            String fill = ground && i == 0 ? "#a16207" : "#bfdbfe";
            svg.append("<rect x='").append(ox).append("' y='").append(oy).append("' width='").append(ow).append("' height='").append(oh)
                    .append("' rx='2' fill='").append(fill).append("' stroke='#1e3a8a' stroke-width='1.4'/>");
            if (perspective) {
                svg.append("<line x1='").append(ox + ow).append("' y1='").append(oy).append("' x2='").append(ox + ow + 18).append("' y2='").append(oy - 10).append("' stroke='#94a3b8'/>");
            }
        }
    }

    private void appendVerticalFins(StringBuilder svg, int x, int y, int w, int h, int count) {
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(h).append("' fill='url(#screenHatch)' opacity='0.6'/>");
        for (int i = 0; i < count; i++) {
            int fx = x + 6 + i * Math.max(7, w / count);
            svg.append("<rect x='").append(fx).append("' y='").append(y).append("' width='5' height='").append(h).append("' fill='#0f766e' opacity='0.72'/>");
        }
    }

    private void appendGreenery(StringBuilder svg, int x, int y, int w, int h) {
        svg.append("<rect x='").append(x).append("' y='").append(y + h - 12).append("' width='").append(w).append("' height='12' fill='#854d0e'/>");
        for (int i = 0; i < 6; i++) {
            int cx = x + 10 + i * Math.max(12, w / 6);
            svg.append("<circle cx='").append(cx).append("' cy='").append(y + 14 + (i % 2) * 8).append("' r='").append(Math.max(8, h / 4)).append("' fill='#22c55e' opacity='0.82'/>");
        }
    }

    private void appendCallout(StringBuilder svg, int tx, int ty, int px, int py, String text) {
        svg.append("<path d='M ").append(tx).append(' ').append(ty + 4).append(" L ").append(px).append(' ').append(py).append("' stroke='#0f766e' stroke-width='1.4' marker-end='url(#arrow)' fill='none'/>");
        svg.append("<rect x='").append(tx - 6).append("' y='").append(ty - 14).append("' width='").append(Math.min(230, Math.max(90, text.length() * 7))).append("' height='24' rx='6' fill='#ecfeff' stroke='#67e8f9'/>");
        svg.append("<text x='").append(tx).append("' y='").append(ty + 2).append("' class='callout'>").append(escape(text)).append("</text>");
    }

    private void appendArrow(StringBuilder svg, int x1, int y1, int x2, int y2, String label) {
        svg.append("<path d='M ").append(x1).append(' ').append(y1).append(" C ").append((x1 + x2) / 2).append(' ').append(y1 - 40).append(' ')
                .append((x1 + x2) / 2).append(' ').append(y2 + 40).append(' ').append(x2).append(' ').append(y2)
                .append("' stroke='#0f766e' stroke-width='2' marker-end='url(#arrow)' fill='none'/>");
        svg.append("<text x='").append((x1 + x2) / 2).append("' y='").append((y1 + y2) / 2 - 8).append("' class='small'>").append(escape(label)).append("</text>");
    }

    private void appendDimension(StringBuilder svg, int x1, int y1, int x2, int y2, String text) {
        svg.append("<line x1='").append(x1).append("' y1='").append(y1).append("' x2='").append(x2).append("' y2='").append(y2).append("' stroke='#334155' stroke-width='1.2'/>");
        svg.append("<circle cx='").append(x1).append("' cy='").append(y1).append("' r='3' fill='#334155'/><circle cx='").append(x2).append("' cy='").append(y2).append("' r='3' fill='#334155'/>");
        svg.append("<text x='").append((x1 + x2) / 2).append("' y='").append((y1 + y2) / 2 - 8).append("' text-anchor='middle' class='small'>").append(escape(text)).append("</text>");
    }

    private void appendLegendBlock(StringBuilder svg, List<String> values, int x, int y) {
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='220' height='86' rx='10' fill='#ffffff' stroke='#cbd5e1'/>");
        svg.append("<text x='").append(x + 12).append("' y='").append(y + 22).append("' class='label'>Vật liệu chính</text>");
        List<String> list = values == null ? List.of() : values;
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            svg.append("<text x='").append(x + 14).append("' y='").append(y + 42 + i * 15).append("' class='small'>• ").append(escape(list.get(i))).append("</text>");
        }
    }

    private void appendNote(StringBuilder svg, int x, int y, String text) {
        svg.append("<text x='").append(x).append("' y='").append(y).append("' class='small'>").append(escape(text.length() > 110 ? text.substring(0, 107) + "..." : text)).append("</text>");
    }

    private void appendScaleBar(StringBuilder svg, String text) {
        svg.append("<rect x='42' y='574' width='836' height='22' fill='#e2e8f0' stroke='#cbd5e1'/>");
        svg.append("<text x='54' y='589' class='small'>").append(escape(text)).append("</text>");
        svg.append("<text x='860' y='589' text-anchor='end' class='small'>ArchitectAI · SVG drawing</text>");
    }

    private void appendRoomMass(StringBuilder svg, int x, int y, int w, int h, String label) {
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(h).append("' fill='#fef3c7' stroke='#d97706'/>");
        svg.append("<text x='").append(x + w / 2).append("' y='").append(y + h / 2 + 4).append("' text-anchor='middle' class='small'>").append(escape(label)).append("</text>");
    }

    private void appendStairSection(StringBuilder svg, int x, int y, int w, int h, int floors) {
        svg.append("<rect x='").append(x).append("' y='").append(y).append("' width='").append(w).append("' height='").append(h).append("' fill='#e0f2fe' stroke='#0284c7'/>");
        int steps = Math.max(9, floors * 6);
        for (int i = 0; i < steps; i++) {
            int sx = x + 8 + (i % 6) * (w - 18) / 6;
            int sy = y + h - 12 - i * Math.max(7, h / steps);
            svg.append("<path d='M ").append(sx).append(' ').append(sy).append(" h").append((w - 20) / 6).append(" v-6' stroke='#0369a1' fill='none'/>");
        }
        svg.append("<text x='").append(x + w / 2).append("' y='").append(y + 18).append("' text-anchor='middle' class='small'>Thang</text>");
    }

    private double roofVisualHeight(RoofPlan roofPlan, double siteWidth) {
        if (roofPlan.roofType().contains("Gable") || roofPlan.roofType().contains("Thai") || roofPlan.roofType().contains("Hip") || roofPlan.roofType().contains("Japanese")) {
            return Math.max(1.0, Math.tan(Math.toRadians(roofPlan.slopeDegrees())) * siteWidth / 2.0);
        }
        return Math.max(0.35, Math.tan(Math.toRadians(Math.max(2, roofPlan.slopeDegrees()))) * siteWidth);
    }

    private int roofVisualHeightPx(RoofPlan roofPlan, int width) {
        if (roofPlan.roofType().contains("Gable") || roofPlan.roofType().contains("Thai") || roofPlan.roofType().contains("Hip") || roofPlan.roofType().contains("Japanese")) {
            return Math.max(34, Math.min(92, (int) Math.round(Math.tan(Math.toRadians(roofPlan.slopeDegrees())) * width / 2.0)));
        }
        return Math.max(18, Math.min(48, (int) Math.round(Math.tan(Math.toRadians(Math.max(2, roofPlan.slopeDegrees()))) * width)));
    }

    private String format(double value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
