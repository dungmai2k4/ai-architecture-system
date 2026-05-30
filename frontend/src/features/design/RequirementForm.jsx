function RequirementForm({ requirement, loading, onRequirementChange, onSubmit }) {
  function handleKeyDown(event) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      event.currentTarget.form?.requestSubmit();
    }
  }

  return (
    <form className="rounded-[1.75rem] border border-white/10 bg-[#2f2f2f] p-2 shadow-2xl shadow-black/30" onSubmit={onSubmit}>
      <label className="sr-only" htmlFor="requirement">
        Yêu cầu thiết kế
      </label>
      <div className="flex items-end gap-2">
        <textarea
          id="requirement"
          rows={1}
          className="max-h-48 min-h-12 flex-1 resize-none bg-transparent px-4 py-3 text-[15px] leading-6 text-neutral-100 outline-none placeholder:text-neutral-500"
          value={requirement}
          onChange={(event) => onRequirementChange(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Nhắn ArchitectAI: Nhà phố 5x20m, 2 tầng, 3 phòng ngủ, có giếng trời..."
        />
        <button
          className="mb-1 flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-white text-neutral-950 transition hover:bg-neutral-200 disabled:cursor-not-allowed disabled:bg-neutral-600 disabled:text-neutral-400"
          type="submit"
          disabled={loading || !requirement.trim()}
          aria-label={loading ? "Đang tạo thiết kế" : "Gửi yêu cầu thiết kế"}
        >
          {loading ? (
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-neutral-400 border-t-transparent" />
          ) : (
            <svg aria-hidden="true" className="h-5 w-5" fill="none" viewBox="0 0 24 24" strokeWidth="2.4" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 12h14m0 0-6-6m6 6-6 6" />
            </svg>
          )}
        </button>
      </div>
    </form>
  );
}

export default RequirementForm;
