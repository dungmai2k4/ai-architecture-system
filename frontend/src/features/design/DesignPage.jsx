import { useState } from "react";
import RequirementForm from "./RequirementForm";
import ResultPanel from "./ResultPanel";

function DesignPage() {
  const [requirement, setRequirement] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();

    if (!requirement.trim()) {
      setError("Vui lòng nhập yêu cầu thiết kế trước khi gửi.");
      setResult(null);
      return;
    }

    setError("");
    setResult(null);
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/designs", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ requirement: requirement.trim() }),
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

  return (
    <main className="min-h-screen bg-slate-50 px-6 py-10 text-slate-900">
      <section className="mx-auto max-w-2xl">
        <h1 className="text-3xl font-semibold">ArchitectAI</h1>
        <RequirementForm
          requirement={requirement}
          loading={loading}
          onRequirementChange={setRequirement}
          onSubmit={handleSubmit}
        />

        {error && (
          <p className="mt-6 rounded-md border border-red-200 bg-red-50 p-3 text-red-700">
            {error}
          </p>
        )}

        {result && <ResultPanel result={result} />}
      </section>
    </main>
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
