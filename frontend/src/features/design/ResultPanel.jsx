function ResultPanel({ result }) {
  const designBrief = result?.designBrief;
  const warnings = result?.ruleResult?.warnings ?? [];
  const layoutPlan = result?.layoutPlan;

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
