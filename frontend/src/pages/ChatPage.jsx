import { useState } from "react";
import ChatMessage from "../components/ChatMessage";
import ChatInput from "../components/ChatInput";
import Sidebar from "../components/Sidebar";

export default function ChatPage() {
  const [messages, setMessages] = useState([
    { role: "ai", content: "Xin chào! Tôi có thể giúp gì cho ý tưởng thiết kế nhà của bạn hôm nay?" }
  ]);

  const handleSend = (text) => {
    if (!text.trim()) return;

    const newMsg = { role: "user", content: text };
    setMessages((prev) => [...prev, newMsg]);

    setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        { role: "ai", content: "Đang xử lý yêu cầu thiết kế của bạn. Hệ thống đang dựng layout phòng..." },
      ]);
    }, 1000);
  };

  return (
    <div className="h-screen w-screen flex bg-gray-900 text-gray-100 font-sans antialiased overflow-hidden">
      {/* Sidebar bên trái */}
      <Sidebar />

      {/* Khu vực Chat chính - QUAN TRỌNG: Thêm min-w-0 */}
      <div className="flex-1 min-w-0 flex flex-col h-full relative">

        {/* Header */}
        <header className="h-14 border-b border-gray-800 flex items-center px-6 shrink-0">
          <h1 className="text-lg font-semibold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            AI Architecture Assistant
          </h1>
        </header>

        {/* Vùng hiển thị tin nhắn */}
        <div className="flex-1 overflow-y-auto py-6 px-4 md:px-0 scrollbar-thin">
          <div className="max-w-3xl mx-auto space-y-6">
            {messages.map((msg, index) => (
              <ChatMessage key={index} msg={msg} />
            ))}
          </div>
        </div>

        {/* Khung Input ở đáy */}
        <div className="w-full max-w-3xl mx-auto px-4 pb-6 shrink-0">
          <ChatInput onSend={handleSend} />
          <p className="text-center text-[11px] text-gray-500 mt-2 tracking-wide">
            AI có thể đưa ra thông tin chưa chính xác, hãy kiểm tra lại các bản vẽ kỹ thuật.
          </p>
        </div>
      </div>
    </div>
  );
}