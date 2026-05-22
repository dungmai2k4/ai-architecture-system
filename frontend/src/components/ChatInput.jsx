import { useState, useRef, useEffect } from "react";

export default function ChatInput({ onSend }) {
  const [text, setText] = useState("");
  const textareaRef = useRef(null);
  const maxLength = 1000; // Giới hạn 1000 ký tự

  // Tự động thay đổi chiều cao của khung nhập liệu theo độ dài văn bản
  useEffect(() => {
    const textarea = textareaRef.current;
    if (textarea) {
      textarea.style.height = "auto"; // Reset height
      textarea.style.height = `${Math.min(textarea.scrollHeight, 200)}px`; // Tối đa cao 200px rồi xuất hiện scrollbar nội bộ
    }
  }, [text]);

  const handleSubmit = (e) => {
    if (e) e.preventDefault();
    if (!text.trim()) return;
    onSend(text);
    setText("");
  };

  const handleKeyDown = (e) => {
    // Nhấn Enter để gửi, Nhấn Shift + Enter để xuống dòng (giống Gemini/ChatGPT)
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <div className="w-full flex flex-col gap-1">
      <form 
        onSubmit={handleSubmit}
        className="flex items-end gap-2 bg-gray-800/80 backdrop-blur-md border border-gray-700 rounded-2xl p-2 pl-4 focus-within:border-gray-500 transition-all shadow-xl"
      >
        <textarea
          ref={textareaRef}
          rows={1}
          maxLength={maxLength}
          className="flex-1 bg-transparent text-white outline-none text-[15px] placeholder-gray-400 py-2 resize-none max-h-[200px] scrollbar-thin"
          placeholder="Nhập yêu cầu thiết kế nhà chi tiết (tối đa 1000 ký tự)..."
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
        />

        <div className="flex flex-col justify-end h-full pb-1">
          <button
            type="submit"
            disabled={!text.trim()}
            className={`p-2.5 rounded-full transition-all ${
              text.trim() 
                ? "bg-blue-600 hover:bg-blue-500 text-white" 
                : "bg-gray-700 text-gray-500 cursor-not-allowed"
            }`}
          >
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-5 h-5">
              <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 10.5 12 3m0 0 7.5 7.5M12 3v18" />
            </svg>
          </button>
        </div>
      </form>
      
      {/* Hiển thị số ký tự còn lại ở góc dưới bên phải như các app chuyên nghiệp */}
      <div className="text-right text-[11px] text-gray-500 px-2">
        {text.length} / {maxLength}
      </div>
    </div>
  );
}