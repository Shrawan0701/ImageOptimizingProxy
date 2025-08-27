import React, { useState } from "react";
import "./App.css";

export default function App() {
  const [mode, setMode] = useState("url");
  const [url, setUrl] = useState("");
  const [file, setFile] = useState(null);
  const [w, setW] = useState("");
  const [h, setH] = useState("");
  const [fmt, setFmt] = useState("webp");
  const [q, setQ] = useState(75);
  const [preview, setPreview] = useState(null);

  const buildQs = () => {
    const p = new URLSearchParams();
    if (w) p.set("w", w);
    if (h) p.set("h", h);
    if (fmt) p.set("fmt", fmt);
    if (q) p.set("q", q);
    return p.toString();
  };

  const goUrl = () => {
    if (!url) return;
    const qs = buildQs();
    setPreview(`/api/image?url=${encodeURIComponent(url)}&${qs}`);
  };

  const upload = async () => {
    if (!file) return;
    const f = new FormData();
    f.append("file", file);
    if (w) f.append("w", w);
    if (h) f.append("h", h);
    if (fmt) f.append("fmt", fmt);
    if (q) f.append("q", q);
    const r = await fetch("/api/upload", { method: "POST", body: f });
    if (!r.ok) {
      alert("Upload failed");
      return;
    }
    const blob = await r.blob();
    const obj = URL.createObjectURL(blob);
    setPreview(obj);
  };

  return (
    <div className="container">
      <h1>Image Optimizing Proxy</h1>

      <div className="mode-switch">
        <button
          className={mode === "url" ? "active" : ""}
          onClick={() => setMode("url")}
        >
          From URL
        </button>
        <button
          className={mode === "upload" ? "active" : ""}
          onClick={() => setMode("upload")}
        >
          Upload File
        </button>
      </div>

      {mode === "url" ? (
        <div className="form">
          <input
            placeholder="Enter image URL..."
            value={url}
            onChange={(e) => setUrl(e.target.value)}
          />
          <button onClick={goUrl}>Preview</button>
        </div>
      ) : (
        <div className="form">
          <input
            type="file"
            onChange={(e) => setFile(e.target.files?.[0] || null)}
          />
          <button onClick={upload}>Upload</button>
        </div>
      )}

      <div className="options">
        <label>
          Width:
          <input value={w} onChange={(e) => setW(e.target.value)} />
        </label>
        <label>
          Height:
          <input value={h} onChange={(e) => setH(e.target.value)} />
        </label>
        <label>
          Format:
          <select value={fmt} onChange={(e) => setFmt(e.target.value)}>
            <option value="webp">WEBP</option>
            <option value="jpeg">JPEG</option>
            <option value="png">PNG</option>
          </select>
        </label>
        <label>
          Quality:
          <input
            type="number"
            min="1"
            max="100"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </label>
      </div>

      {preview && (
        <div className="preview">
          <img src={preview} alt="preview" />
          <a href={preview} download>
            ⬇ Download
          </a>
        </div>
      )}
    </div>
  );
}
