import { useMemo, useState } from "react";
import { Canvas } from "@react-three/fiber";
import { Bounds, OrbitControls } from "@react-three/drei";

function ResultPanel({ result }) {
  const designBrief = result?.designBrief;
  const warnings = result?.ruleResult?.warnings ?? [];
  const layoutPlan = result?.layoutPlan;
  const floorplan = result?.floorplan;

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

function FloorplanSection({ floorplan }) {
  const [activeTab, setActiveTab] = useState("svg");
  const tabs = [
    { id: "svg", label: "SVG" },
    { id: "json", label: "JSON" },
    { id: "preview3d", label: "3D" },
  ];

  return (
    <div className="mt-4 rounded-md border border-cyan-200 bg-cyan-50 p-4">
      <h3 className="font-semibold text-cyan-800">Mặt bằng sơ bộ tầng 1</h3>
      <p className="mt-1 text-sm text-cyan-900">
        Kích thước: {floorplan.siteWidth}m x {floorplan.siteDepth}m
      </p>

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
            src={toSvgDataUrl(floorplan.svg)}
            alt="Mặt bằng sơ bộ"
            className="mx-auto h-auto max-h-[70vh] w-full rounded border border-slate-100 bg-slate-50 object-contain"
          />
        </div>
      )}

      {activeTab === "json" && (
        <pre className="mt-3 max-h-72 overflow-auto rounded border border-cyan-100 bg-white p-3 text-xs text-slate-700">
          {JSON.stringify(floorplan, null, 2)}
        </pre>
      )}

      {activeTab === "preview3d" && (
        <div className="mt-3 h-80 overflow-hidden rounded border border-cyan-100 bg-slate-950">
          <Basic3DPreview floorplan={floorplan} />
        </div>
      )}
    </div>
  );
}

function Basic3DPreview({ floorplan }) {
  const roomMeshes = useMemo(
    () =>
      (floorplan.rooms ?? []).map((room) => ({
        key: room.name,
        width: room.width,
        depth: room.depth,
        x: room.x + room.width / 2 - floorplan.siteWidth / 2,
        z: room.y + room.depth / 2 - floorplan.siteDepth / 2,
      })),
    [floorplan],
  );

  return (
    <Canvas camera={{ position: [0, 12, 12], fov: 50 }}>
      <ambientLight intensity={0.65} />
      <directionalLight position={[8, 14, 10]} intensity={0.8} />
      <gridHelper
        args={[Math.max(floorplan.siteWidth, floorplan.siteDepth) + 8, 24, "#334155", "#1e293b"]}
        position={[0, 0, 0]}
      />
      <Bounds fit clip observe margin={1.2}>
        {roomMeshes.map((room) => (
          <mesh key={room.key} position={[room.x, 0.2, room.z]}>
            <boxGeometry args={[room.width, 0.4, room.depth]} />
            <meshStandardMaterial color="#22d3ee" opacity={0.7} transparent />
          </mesh>
        ))}
        <mesh position={[0, -0.05, 0]} rotation={[-Math.PI / 2, 0, 0]}>
          <planeGeometry args={[floorplan.siteWidth, floorplan.siteDepth]} />
          <meshStandardMaterial color="#0f172a" />
        </mesh>
      </Bounds>
      <OrbitControls enablePan enableZoom maxPolarAngle={Math.PI / 2.1} />
    </Canvas>
  );
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
