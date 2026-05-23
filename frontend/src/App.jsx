import { useState } from "react";

function App() {
  const [requirement, setRequirement] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setResult(null);
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/designs", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ requirement }),
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.error || "Request failed");
      }

      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen bg-slate-50 px-6 py-10 text-slate-900">
      <section className="mx-auto max-w-2xl">
        <h1 className="text-3xl font-semibold">ArchitectAI</h1>
        <form className="mt-8 space-y-4" onSubmit={handleSubmit}>
          <label className="block text-sm font-medium" htmlFor="requirement">
            Requirement
          </label>
          <textarea
            id="requirement"
            className="min-h-40 w-full resize-y rounded-md border border-slate-300 bg-white p-3 outline-none focus:border-slate-700"
            value={requirement}
            onChange={(event) => setRequirement(event.target.value)}
            placeholder="Enter the housing requirement..."
          />
          <button
            className="rounded-md bg-slate-900 px-4 py-2 font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-400"
            type="submit"
            disabled={loading}
          >
            {loading ? "Generating..." : "Generate Design"}
          </button>
        </form>

        {error && (
          <p className="mt-6 rounded-md border border-red-200 bg-red-50 p-3 text-red-700">
            {error}
          </p>
        )}

        {result && (
          <section className="mt-6 rounded-md border border-slate-200 bg-white p-4">
            <h2 className="text-lg font-semibold">Result</h2>
            <p className="mt-3">Project ID: {result.projectId}</p>
            <p>Status: {result.status}</p>
          </section>
        )}
      </section>
    </main>
  );
}

export default App;
