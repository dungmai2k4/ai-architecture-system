import { useMemo, useState } from "react";
import { Canvas } from "@react-three/fiber";
import { Bounds, OrbitControls } from "@react-three/drei";

function ResultPanel({ result }) {
  const designBrief = result?.designBrief;
  const warnings = result?.ruleResult?.warnings ?? [];
  const layoutPlan = result?.layoutPlan;
  const floorplan = result?.floorplan;
  const renderPrompt = result?.renderPrompt;

  return (
    <section className="mt-6 rounded-md border border-slate-200 bg-white p-4">
      <h2 className="text-lg font-semibold">Kết quả</h2>
      <p className="mt-3">Project ID: {result.projectId}</p>
      <p>Trạng thái: {result.status}</p>

      {result.error && (
        <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-red-700">
          {result.error}
        </p>
      )}

      {!designBrief && (
        <div className="mt-4 rounded-md border border-yellow-300 bg-yellow-50 p-3 text-yellow-800">
          Chưa có thông tin thiết kế được trích xuất.
        </div>
      )}

      {designBrief && (
        <div className="mt-5 rounded-md border border-slate-200 p-4">
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
            <KeyValue label="Phòng yêu cầu" value={formatList(designBrief.rooms)} />
            <KeyValue label="Tùy chọn" value={formatList(designBrief.preferences)} />
          </dl>
        </div>
      )}

      {layoutPlan && (
        <div className="mt-4 rounded-md border border-emerald-200 bg-emerald-50 p-4">
          <h3 className="font-semibold text-emerald-800">Gợi ý bố trí sơ bộ</h3>
          <dl className="mt-2 grid gap-2 text-sm">
            <KeyValue label="Chiến lược" value={layoutPlan.strategy} />
            <KeyValue label="Phân khu" value={formatList(layoutPlan.zoning)} />
            <KeyValue label="Giao thông" value={formatList(layoutPlan.circulation)} />
            <KeyValue label="Ghi chú" value={formatList(layoutPlan.notes)} />
          </dl>
        </div>
      )}

      {floorplan && <FloorplanSection floorplan={floorplan} />}

      {renderPrompt && <RenderPromptSection prompt={renderPrompt} imagePath={result.renderImagePath} />}

      {warnings.length > 0 && (
        <div className="mt-4 rounded-md border border-amber-200 bg-amber-50 p-4">
          <h3 className="font-semibold text-amber-800">Cảnh báo quy tắc</h3>
          <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-amber-900">
            {warnings.map((warning) => (
              <li key={warning}>{warning}</li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}


function RenderPromptSection({ prompt, imagePath }) {
  return (
    <div className="mt-4 rounded-md border border-violet-200 bg-violet-50 p-4">
      <h3 className="font-semibold text-violet-800">Prompt render gợi ý</h3>
      <p className="mt-1 text-sm text-violet-900">
        Dùng prompt này cho bước tạo ảnh phối cảnh/mood board sau khi đã duyệt mặt bằng.
      </p>
      <pre className="mt-3 whitespace-pre-wrap rounded border border-violet-100 bg-white p-3 text-xs leading-5 text-slate-700">
        {prompt}
      </pre>
      {imagePath && (
        <p className="mt-2 text-sm text-violet-900">Ảnh render: {imagePath}</p>
      )}
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
    <div className="mt-4 rounded-md border border-cyan-200 bg-cyan-50 p-4">
      <h3 className="font-semibold text-cyan-800">Mặt bằng sơ bộ {selectedFloor.label ?? `tầng ${selectedFloor.level ?? 1}`}</h3>
      <p className="mt-1 text-sm text-cyan-900">
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
                  ? "border-slate-900 bg-slate-900 text-white"
                  : "border-cyan-200 bg-white text-slate-700"
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
                ? "border-cyan-700 bg-cyan-700 text-white"
                : "border-cyan-200 bg-white text-cyan-800"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === "svg" && (
        <div className="mt-3 overflow-auto rounded border border-cyan-100 bg-white p-2">
          <img
            src={toSvgDataUrl(selectedFloor.svg)}
            alt={`Mặt bằng sơ bộ ${selectedFloor.label ?? ""}`}
            className="mx-auto h-auto max-h-[70vh] w-full rounded border border-slate-100 bg-slate-50 object-contain"
          />
        </div>
      )}

      {activeTab === "json" && (
        <pre className="mt-3 max-h-72 overflow-auto rounded border border-cyan-100 bg-white p-3 text-xs text-slate-700">
          {JSON.stringify(selectedFloor, null, 2)}
        </pre>
      )}

      {activeTab === "preview3d" && (
        <div className="mt-3 h-80 overflow-hidden rounded border border-cyan-100 bg-slate-950">
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
    <div className="grid gap-1 border-b border-slate-100 pb-2 sm:grid-cols-2">
      <dt className="font-medium text-slate-600">{label}</dt>
      <dd className="text-slate-900">{value}</dd>
    </div>
  );
}

function formatList(value) {
  return Array.isArray(value) && value.length > 0 ? value.join(", ") : "-";
}

export default ResultPanel;
