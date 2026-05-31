import { useMemo, useState } from "react";
import { Canvas } from "@react-three/fiber";
import { Bounds, OrbitControls } from "@react-three/drei";

function ResultPanel({ result }) {
  const designBrief = result?.designBrief;
  const warnings = result?.ruleResult?.warnings ?? [];
  const layoutPlan = result?.layoutPlan;
  const floorplan = result?.floorplan;
  const designPackage = result?.architecturalDesignPackage;
  const renderPrompt = result?.renderPrompt;

  return (
    <section className="rounded-3xl border border-white/10 bg-[#2f2f2f] p-4 text-neutral-100 shadow-xl md:p-5">
      <h2 className="text-lg font-semibold">Phương án thiết kế sơ bộ</h2>
      <p className="mt-3 text-sm text-neutral-400">Project ID: {result.projectId}</p>
      <p className="text-sm text-neutral-400">Trạng thái: {result.status}</p>

      {result.error && (
        <p className="mt-4 rounded-2xl border border-red-400/30 bg-red-500/10 p-3 text-red-100">
          {result.error}
        </p>
      )}

      {!designBrief && (
        <div className="mt-4 rounded-2xl border border-yellow-400/30 bg-yellow-500/10 p-3 text-yellow-100">
          Chưa có thông tin thiết kế được trích xuất.
        </div>
      )}

      {designBrief && (
        <div className="mt-5 rounded-2xl border border-white/10 bg-white/[0.03] p-4">
          <h3 className="font-semibold">Thông tin thiết kế</h3>
          <dl className="mt-3 grid gap-2 text-sm">
            <KeyValue
              label="Kích thước lô đất"
              value={`${designBrief.siteWidthMeters}m x ${designBrief.siteDepthMeters}m`}
            />
            <KeyValue label="Số tầng" value={designBrief.floors} />
            <KeyValue label="Số phòng ngủ" value={designBrief.bedrooms} />
            <KeyValue label="Số WC" value={designBrief.bathrooms} />
            <KeyValue label="Phong cách" value={designBrief.style} />
            <KeyValue label="Hướng nhà" value={designBrief.orientation ?? "unknown"} />
            <KeyValue label="Nhu cầu bố cục" value={formatLayoutIntent(designBrief)} />
            <KeyValue label="Vị trí thang" value={designBrief.stairPreference ?? "unknown"} />
            <KeyValue label="Quan hệ phòng" value={formatList(designBrief.adjacencyPreferences)} />
            <KeyValue label="Yêu cầu theo tầng" value={formatFloorRequirements(designBrief.floorRequirements)} />
            <KeyValue label="Phòng yêu cầu" value={formatList(designBrief.rooms)} />
            <KeyValue label="Tùy chọn" value={formatList(designBrief.preferences)} />
          </dl>
        </div>
      )}

      {layoutPlan && (
        <div className="mt-4 rounded-2xl border border-emerald-400/20 bg-emerald-400/10 p-4">
          <h3 className="font-semibold text-emerald-100">Gợi ý bố trí sơ bộ</h3>
          <dl className="mt-2 grid gap-2 text-sm">
            <KeyValue label="Chiến lược" value={layoutPlan.strategy} />
            <KeyValue label="Phân khu" value={formatList(layoutPlan.zoning)} />
            <KeyValue label="Giao thông" value={formatList(layoutPlan.circulation)} />
            <KeyValue label="Ghi chú" value={formatList(layoutPlan.notes)} />
          </dl>
        </div>
      )}

      {designPackage && <ArchitecturalPackageSection designPackage={designPackage} />}

      {floorplan && <FloorplanSection floorplan={floorplan} />}

      {renderPrompt && <RenderPromptSection prompt={renderPrompt} designPackage={designPackage} imagePath={result.renderImagePath} />}

      {warnings.length > 0 && (
        <div className="mt-4 rounded-2xl border border-amber-400/20 bg-amber-400/10 p-4">
          <h3 className="font-semibold text-amber-100">Cảnh báo quy tắc</h3>
          <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-amber-100/90">
            {warnings.map((warning) => (
              <li key={warning}>{warning}</li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}


function ArchitecturalPackageSection({ designPackage }) {
  const typology = designPackage.typology;
  const climate = designPackage.climateAnalysis;
  const courtyard = designPackage.courtyardPlan;
  const roof = designPackage.roofPlan;
  const facade = designPackage.facadeComposition;
  const style = designPackage.exteriorStyle;
  const landscape = designPackage.landscapePlan;
  const sections = designPackage.buildingSections ?? [];

  return (
    <div className="mt-4 rounded-2xl border border-sky-400/20 bg-sky-400/10 p-4">
      <h3 className="font-semibold text-sky-100">Gói thiết kế kiến trúc Việt Nam</h3>
      <p className="mt-1 text-sm text-sky-100/80">
        Mở rộng từ mặt bằng sang typology, khí hậu, sân trong, mái, mặt cắt, mặt đứng và concept ngoại thất.
      </p>

      <div className="mt-3 grid gap-3 text-sm lg:grid-cols-2">
        <PackageCard title="Loại hình công trình">
          <KeyValue label="Typology" value={`${typology?.name ?? "-"} (${typology?.code ?? "-"})`} />
          <KeyValue label="Lý do chọn" value={formatList(typology?.fitReasons)} />
          <KeyValue label="Ưu tiên" value={formatList(typology?.planningPriorities)} />
        </PackageCard>

        <PackageCard title="Phân tích khí hậu">
          <KeyValue label="Vùng khí hậu" value={climate?.climateZone ?? "-"} />
          <KeyValue label="Che nắng" value={formatList(climate?.shadingStrategy)} />
          <KeyValue label="Thông gió" value={formatList(climate?.ventilationStrategy)} />
        </PackageCard>

        <PackageCard title="Sân trong / khoảng rỗng">
          <KeyValue label="Loại" value={courtyard?.type ?? "-"} />
          <KeyValue label="Kích thước" value={courtyard ? `${courtyard.width}m x ${courtyard.depth}m tại (${courtyard.x}, ${courtyard.y})` : "-"} />
          <KeyValue label="Cây + skylight" value={`${formatList(courtyard?.treePlacement)} · ${formatList(courtyard?.skylightPlacement)}`} />
        </PackageCard>

        <PackageCard title="Mái và mặt cắt">
          <KeyValue label="Mái" value={roof ? `${roof.roofType}, dốc ${roof.slopeDegrees}°, đua mái ${roof.overhangMeters}m` : "-"} />
          <KeyValue label="Thoát nước" value={formatList(roof?.drainageStrategy)} />
          <KeyValue label="Mặt cắt" value={formatList(sections.map((section) => section.name))} />
        </PackageCard>

        <PackageCard title="Mặt đứng">
          <KeyValue label="Bố cục" value={facade ? `${facade.compositionType}, ${facade.bays} nhịp` : "-"} />
          <KeyValue label="Ban công" value={formatList(facade?.balconies)} />
          <KeyValue label="Lam/screen" value={`${formatList(facade?.verticalFins)} · ${formatList(facade?.sunScreens)}`} />
        </PackageCard>

        <PackageCard title="Ngoại thất + cảnh quan">
          <KeyValue label="Phong cách" value={style?.name ?? "-"} />
          <KeyValue label="Vật liệu" value={formatList(style?.materialPalette)} />
          <KeyValue label="Cảnh quan" value={`${formatList(landscape?.frontLandscape)} · ${formatList(landscape?.courtyardLandscape)}`} />
        </PackageCard>
      </div>
    </div>
  );
}

function PackageCard({ title, children }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-black/10 p-3">
      <h4 className="mb-2 font-semibold text-sky-50">{title}</h4>
      <dl className="grid gap-2">{children}</dl>
    </div>
  );
}

function RenderPromptSection({ prompt, designPackage, imagePath }) {
  return (
    <div className="mt-4 rounded-2xl border border-violet-400/20 bg-violet-400/10 p-4">
      <h3 className="font-semibold text-violet-100">Prompt render gợi ý</h3>
      <p className="mt-1 text-sm text-violet-100/80">
        Dùng prompt này cho bước tạo ảnh phối cảnh/mood board sau khi đã duyệt mặt bằng.
      </p>
      <pre className="mt-3 whitespace-pre-wrap rounded-2xl border border-white/10 bg-black/20 p-3 text-xs leading-5 text-neutral-200">
        {prompt}
      </pre>
      {designPackage?.renderPrompts && (
        <div className="mt-3 grid gap-3 md:grid-cols-2">
          <PromptBlock title="Ngoại thất" value={designPackage.renderPrompts.exteriorPrompt} />
          <PromptBlock title="Sân trong" value={designPackage.renderPrompts.courtyardPrompt} />
          <PromptBlock title="Mặt đứng" value={designPackage.renderPrompts.facadePrompt} />
          <PromptBlock title="Mái + mặt cắt" value={designPackage.renderPrompts.roofAndSectionPrompt} />
        </div>
      )}
      {imagePath && (
        <p className="mt-2 text-sm text-violet-100/80">Ảnh render: {imagePath}</p>
      )}
    </div>
  );
}

function PromptBlock({ title, value }) {
  if (!value) return null;
  return (
    <div className="rounded-2xl border border-white/10 bg-black/10 p-3">
      <p className="text-xs font-semibold uppercase tracking-[0.16em] text-violet-100/70">{title}</p>
      <p className="mt-2 text-xs leading-5 text-neutral-200">{value}</p>
    </div>
  );
}

function FloorplanSection({ floorplan }) {
  const [activeTab, setActiveTab] = useState("svg");
  const [activeLevel, setActiveLevel] = useState(1);
  const tabs = [
    { id: "svg", label: "SVG" },
    { id: "json", label: "JSON" },
    { id: "preview3d", label: "3D" },
  ];
  const floors = floorplan.floors?.length > 0 ? floorplan.floors : [floorplan];
  const selectedFloor = floors.find((floor) => floor.level === activeLevel) ?? floors[0];

  return (
    <div className="mt-4 rounded-2xl border border-cyan-400/20 bg-cyan-400/10 p-4">
      <h3 className="font-semibold text-cyan-100">Mặt bằng sơ bộ {selectedFloor.label ?? `tầng ${selectedFloor.level ?? 1}`}</h3>
      <p className="mt-1 text-sm text-cyan-100/80">
        Kích thước: {selectedFloor.siteWidth}m x {selectedFloor.siteDepth}m · {selectedFloor.rooms?.length ?? 0} khu vực · {selectedFloor.doors?.length ?? 0} cửa · {selectedFloor.windows?.length ?? 0} cửa sổ/thoáng
      </p>

      {floors.length > 1 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {floors.map((floor) => (
            <button
              key={floor.level}
              type="button"
              onClick={() => setActiveLevel(floor.level)}
              className={`rounded border px-3 py-1 text-sm ${
                selectedFloor.level === floor.level
                  ? "border-white bg-white text-neutral-950"
                  : "border-white/10 bg-white/[0.04] text-neutral-200 hover:bg-white/10"
              }`}
            >
              {floor.label ?? `Tầng ${floor.level}`}
            </button>
          ))}
        </div>
      )}

      <div className="mt-3 flex gap-2">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id)}
            className={`rounded border px-3 py-1 text-sm ${
              activeTab === tab.id
                ? "border-white bg-white text-neutral-950"
                : "border-white/10 bg-white/[0.04] text-cyan-100 hover:bg-white/10"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === "svg" && (
        <div className="mt-3 overflow-auto rounded-2xl border border-white/10 bg-white p-2">
          <img
            src={toSvgDataUrl(selectedFloor.svg)}
            alt={`Mặt bằng sơ bộ ${selectedFloor.label ?? ""}`}
            className="mx-auto h-auto max-h-[70vh] max-w-none rounded-xl border border-neutral-200 bg-slate-50 object-contain"
          />
        </div>
      )}

      {activeTab === "json" && (
        <pre className="mt-3 max-h-72 overflow-auto rounded-2xl border border-white/10 bg-black/20 p-3 text-xs text-neutral-200">
          {JSON.stringify(selectedFloor, null, 2)}
        </pre>
      )}

      {activeTab === "preview3d" && (
        <div className="mt-3 h-80 overflow-hidden rounded-2xl border border-white/10 bg-slate-950">
          <Basic3DPreview floorplan={selectedFloor} />
        </div>
      )}
    </div>
  );
}

function Basic3DPreview({ floorplan }) {
  const scene = useMemo(() => buildPreviewScene(floorplan), [floorplan]);

  return (
    <Canvas camera={{ position: [0, 13, 14], fov: 48 }} shadows>
      <color attach="background" args={["#020617"]} />
      <ambientLight intensity={0.7} />
      <directionalLight position={[8, 16, 10]} intensity={1.15} castShadow />
      <gridHelper
        args={[Math.max(floorplan.siteWidth, floorplan.siteDepth) + 8, 28, "#334155", "#1e293b"]}
        position={[0, 0, 0]}
      />
      <Bounds fit clip observe margin={1.25}>
        <mesh position={[0, -0.04, 0]} rotation={[-Math.PI / 2, 0, 0]} receiveShadow>
          <planeGeometry args={[floorplan.siteWidth, floorplan.siteDepth]} />
          <meshStandardMaterial color="#0f172a" />
        </mesh>

        {scene.rooms.map((room) => (
          <mesh key={room.key} position={[room.x, 0.02, room.z]} receiveShadow>
            <boxGeometry args={[room.width, 0.04, room.depth]} />
            <meshStandardMaterial color={room.color} opacity={0.92} transparent />
          </mesh>
        ))}

        {scene.walls.map((wall) => (
          <mesh key={wall.key} position={[wall.x, 1.15, wall.z]} castShadow receiveShadow>
            <boxGeometry args={wall.args} />
            <meshStandardMaterial color={wall.color} />
          </mesh>
        ))}

        {scene.windows.map((window) => (
          <mesh key={window.key} position={[window.x, 1.35, window.z]}>
            <boxGeometry args={window.args} />
            <meshStandardMaterial color="#38bdf8" opacity={0.55} transparent />
          </mesh>
        ))}

        {scene.doors.map((door) => (
          <mesh key={door.key} position={[door.x, 0.8, door.z]}>
            <boxGeometry args={door.args} />
            <meshStandardMaterial color="#92400e" />
          </mesh>
        ))}

        {scene.furniture.map((item) => (
          <mesh key={item.key} position={[item.x, 0.18, item.z]} castShadow>
            <boxGeometry args={[item.width, 0.32, item.depth]} />
            <meshStandardMaterial color={item.color} />
          </mesh>
        ))}
      </Bounds>
      <OrbitControls enablePan enableZoom maxPolarAngle={Math.PI / 2.05} />
    </Canvas>
  );
}

function buildPreviewScene(floorplan) {
  const roomCenter = (item) => ({
    x: item.x + item.width / 2 - floorplan.siteWidth / 2,
    z: item.y + item.depth / 2 - floorplan.siteDepth / 2,
  });

  const rooms = (floorplan.rooms ?? []).map((room) => ({
    key: room.name,
    width: room.width,
    depth: room.depth,
    color: room.color ?? "#22d3ee",
    ...roomCenter(room),
  }));

  const walls = (floorplan.walls ?? []).map((wall, index) => {
    const horizontal = Math.abs(wall.x2 - wall.x1) >= Math.abs(wall.y2 - wall.y1);
    const length = horizontal ? Math.abs(wall.x2 - wall.x1) : Math.abs(wall.y2 - wall.y1);
    const thickness = Math.max(wall.thickness ?? 0.12, 0.08);
    return {
      key: `wall-${index}`,
      x: (wall.x1 + wall.x2) / 2 - floorplan.siteWidth / 2,
      z: (wall.y1 + wall.y2) / 2 - floorplan.siteDepth / 2,
      args: horizontal ? [length, 2.3, thickness] : [thickness, 2.3, length],
      color: wall.type === "external" ? "#f8fafc" : "#cbd5e1",
    };
  });

  const furniture = (floorplan.furniture ?? []).map((item, index) => ({
    key: `furniture-${index}-${item.type}`,
    width: item.width,
    depth: item.depth,
    color: item.color ?? "#94a3b8",
    ...roomCenter(item),
  }));

  const windows = (floorplan.windows ?? []).map((window, index) => toOpeningMesh(window, index, floorplan, 0.08));
  const doors = (floorplan.doors ?? []).map((door, index) => toOpeningMesh(door, index, floorplan, 0.12));

  return { rooms, walls, windows, doors, furniture };
}

function toOpeningMesh(opening, index, floorplan, thickness) {
  const horizontal = opening.orientation !== "vertical";
  const length = opening.width ?? 0.8;
  return {
    key: `${horizontal ? "h" : "v"}-opening-${index}-${opening.label}`,
    x: (opening.x ?? 0) + (horizontal ? length / 2 : 0) - floorplan.siteWidth / 2,
    z: (opening.y ?? 0) + (horizontal ? 0 : length / 2) - floorplan.siteDepth / 2,
    args: horizontal ? [length, 0.95, thickness] : [thickness, 0.95, length],
  };
}

function toSvgDataUrl(svg) {
  if (!svg) return "";
  const cleaned = svg
    .replace(/<\?xml[^>]*>/g, "")
    .replace(/\r?\n|\r/g, "")
    .trim();
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(cleaned)}`;
}

function KeyValue({ label, value }) {
  return (
    <div className="grid gap-1 border-b border-white/10 pb-2 sm:grid-cols-2">
      <dt className="font-medium text-neutral-400">{label}</dt>
      <dd className="text-neutral-100">{value}</dd>
    </div>
  );
}

function formatLayoutIntent(designBrief) {
  const labels = [
    designBrief.parkingRequired ? "cần chỗ để xe" : null,
    designBrief.lightwellRequired ? "cần giếng trời/sân trong" : null,
    designBrief.frontYardRequired ? "cần sân trước/hiên" : null,
    designBrief.rearGardenRequired ? "cần sân sau/vườn sau" : null,
    designBrief.openKitchen ? "bếp mở" : null,
  ].filter(Boolean);

  return labels.length > 0 ? labels.join(", ") : "-";
}

function formatFloorRequirements(value) {
  if (!Array.isArray(value) || value.length === 0) {
    return "-";
  }

  return value
    .map((floor) => `Tầng ${floor.level}: ${formatList(floor.rooms)}`)
    .join("; ");
}

function formatList(value) {
  return Array.isArray(value) && value.length > 0 ? value.join(", ") : "-";
}

export default ResultPanel;
