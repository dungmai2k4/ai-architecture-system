function RequirementForm({ requirement, loading, onRequirementChange, onSubmit }) {
  return (
    <form className="mt-8 space-y-4" onSubmit={onSubmit}>
      <label className="block text-sm font-medium" htmlFor="requirement">
        Yêu cầu thiết kế
      </label>
      <textarea
        id="requirement"
        className="min-h-40 w-full resize-y rounded-md border border-slate-300 bg-white p-3 outline-none focus:border-slate-700"
        value={requirement}
        onChange={(event) => onRequirementChange(event.target.value)}
        placeholder="VD: Nhà phố 5x20m, 2 tầng, 3 phòng ngủ, 2 WC, phong cách hiện đại..."
      />
      <button
        className="rounded-md bg-slate-900 px-4 py-2 font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-400"
        type="submit"
        disabled={loading}
      >
        {loading ? "Đang tạo..." : "Tạo thiết kế"}
      </button>
    </form>
  );
}

export default RequirementForm;
