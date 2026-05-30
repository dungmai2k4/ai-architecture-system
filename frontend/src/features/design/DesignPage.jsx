import { useState } from "react";
import RequirementForm from "./RequirementForm";
import ResultPanel from "./ResultPanel";

const EXAMPLE_PROMPTS = [
  "Nhà phố 5x20m, 2 tầng, 3 phòng ngủ, có giếng trời và bếp mở.",
  "Biệt thự 8x18m, 3 tầng, 4 phòng ngủ, có gara và sân sau.",
  "Nhà ống hướng tây 4x16m, 2 tầng, cần chống nóng và thông gió tốt.",
];

function DesignPage() {
  const [requirement, setRequirement] = useState("");
  const [submittedRequirement, setSubmittedRequirement] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();

    const trimmedRequirement = requirement.trim();
    if (!trimmedRequirement) {
      setError("Vui lòng nhập yêu cầu thiết kế trước khi gửi.");
      setResult(null);
      return;
    }

    setError("");
    setResult(null);
    setSubmittedRequirement(trimmedRequirement);
    setRequirement("");
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/designs", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ requirement: trimmedRequirement }),
      });

      const data = await readJsonResponse(response);
      if (!response.ok) {
        throw new Error(data?.error || "Request failed");
      }

      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unexpected error");
    } finally {
      setLoading(false);
    }
  }

  function startNewConversation() {
    setRequirement("");
    setSubmittedRequirement("");
    setResult(null);
    setError("");
  }

  return (
    <main className="flex min-h-screen bg-[#212121] text-neutral-100">
      <aside className="hidden w-72 shrink-0 border-r border-white/10 bg-[#171717] p-3 lg:flex lg:flex-col">
        <button
          type="button"
          onClick={startNewConversation}
          className="flex w-full items-center gap-3 rounded-xl border border-white/10 px-3 py-3 text-left text-sm font-medium text-neutral-100 transition hover:bg-white/10"
        >
          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-white text-lg leading-none text-neutral-950">+</span>
          Cuộc trò chuyện mới
        </button>

        <div className="mt-6 space-y-2">
          <p className="px-2 text-xs font-semibold uppercase tracking-[0.18em] text-neutral-500">Gợi ý gần đây</p>
          {EXAMPLE_PROMPTS.map((prompt) => (
            <button
              key={prompt}
              type="button"
              onClick={() => setRequirement(prompt)}
              className="w-full truncate rounded-lg px-3 py-2 text-left text-sm text-neutral-300 transition hover:bg-white/10 hover:text-white"
            >
              {prompt}
            </button>
          ))}
        </div>

        <div className="mt-auto rounded-xl border border-white/10 bg-white/[0.03] p-3 text-xs leading-5 text-neutral-400">
          <p className="font-semibold text-neutral-200">ArchitectAI</p>
          <p>MVP tạo mặt bằng nhà ở Việt Nam bằng AI extraction + Java deterministic layout.</p>
        </div>
      </aside>

      <section className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-10 flex items-center justify-between border-b border-white/10 bg-[#212121]/90 px-4 py-3 backdrop-blur md:px-6">
          <div>
            <h1 className="text-base font-semibold md:text-lg">ArchitectAI</h1>
            <p className="text-xs text-neutral-400">Trợ lý thiết kế mặt bằng kiểu hội thoại</p>
          </div>
          <button
            type="button"
            onClick={startNewConversation}
            className="rounded-full border border-white/10 px-3 py-1.5 text-sm text-neutral-200 transition hover:bg-white/10 lg:hidden"
          >
            Chat mới
          </button>
        </header>

        <div className="flex-1 overflow-y-auto px-4 pb-44 pt-8 md:px-6">
          <div className="mx-auto flex w-full max-w-4xl flex-col gap-6">
            {!submittedRequirement && !loading && !result && !error && (
              <EmptyState onPickPrompt={setRequirement} />
            )}

            {submittedRequirement && <UserMessage content={submittedRequirement} />}

            {loading && (
              <AssistantShell>
                <div className="flex items-center gap-3 text-neutral-300">
                  <span className="flex gap-1">
                    <span className="h-2 w-2 animate-bounce rounded-full bg-neutral-400 [animation-delay:-0.2s]" />
                    <span className="h-2 w-2 animate-bounce rounded-full bg-neutral-400 [animation-delay:-0.1s]" />
                    <span className="h-2 w-2 animate-bounce rounded-full bg-neutral-400" />
                  </span>
                  Đang phân tích yêu cầu và tạo mặt bằng sơ bộ...
                </div>
              </AssistantShell>
            )}

            {error && (
              <AssistantShell tone="error">
                <p className="font-medium text-red-200">Không thể tạo thiết kế</p>
                <p className="mt-1 text-sm text-red-100/80">{error}</p>
              </AssistantShell>
            )}

            {result && (
              <AssistantShell>
                <ResultPanel result={result} />
              </AssistantShell>
            )}
          </div>
        </div>

        <div className="fixed inset-x-0 bottom-0 border-t border-white/10 bg-[#212121]/95 px-4 pb-4 pt-3 backdrop-blur lg:left-72 md:px-6">
          <div className="mx-auto max-w-4xl">
            <RequirementForm
              requirement={requirement}
              loading={loading}
              onRequirementChange={setRequirement}
              onSubmit={handleSubmit}
            />
            <p className="mt-2 text-center text-xs text-neutral-500">
              ArchitectAI có thể tạo phương án sơ bộ; hãy kiểm tra lại với kiến trúc sư trước khi thi công.
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}

function EmptyState({ onPickPrompt }) {
  return (
    <div className="mx-auto flex min-h-[48vh] max-w-3xl flex-col items-center justify-center text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-400 via-cyan-400 to-blue-500 text-2xl shadow-2xl shadow-cyan-950/40">
        ✨
      </div>
      <h2 className="mt-6 text-3xl font-semibold tracking-tight md:text-4xl">Bạn muốn thiết kế ngôi nhà như thế nào?</h2>
      <p className="mt-3 max-w-2xl text-sm leading-6 text-neutral-400 md:text-base">
        Nhập yêu cầu bằng tiếng Việt. ArchitectAI sẽ trích xuất brief, kiểm tra quy tắc, tạo layout, SVG và preview 3D trong cùng một cuộc trò chuyện.
      </p>
      <div className="mt-8 grid w-full gap-3 md:grid-cols-3">
        {EXAMPLE_PROMPTS.map((prompt) => (
          <button
            key={prompt}
            type="button"
            onClick={() => onPickPrompt(prompt)}
            className="rounded-2xl border border-white/10 bg-white/[0.04] p-4 text-left text-sm leading-5 text-neutral-300 transition hover:border-white/20 hover:bg-white/[0.08] hover:text-white"
          >
            {prompt}
          </button>
        ))}
      </div>
    </div>
  );
}

function UserMessage({ content }) {
  return (
    <div className="flex justify-end">
      <div className="max-w-[85%] rounded-3xl bg-[#2f2f2f] px-5 py-3 text-[15px] leading-7 text-neutral-100 shadow-lg md:max-w-[72%]">
        {content}
      </div>
    </div>
  );
}

function AssistantShell({ children, tone = "default" }) {
  const avatarClass = tone === "error"
    ? "bg-red-500 text-white"
    : "bg-gradient-to-br from-emerald-400 via-cyan-400 to-blue-500 text-neutral-950";

  return (
    <div className="flex gap-4">
      <div className={`mt-1 flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-bold ${avatarClass}`}>
        AI
      </div>
      <div className="min-w-0 flex-1">{children}</div>
    </div>
  );
}

async function readJsonResponse(response) {
  const rawBody = await response.text();
  if (!rawBody) {
    return null;
  }

  try {
    return JSON.parse(rawBody);
  } catch {
    return { error: rawBody };
  }
}

export default DesignPage;
