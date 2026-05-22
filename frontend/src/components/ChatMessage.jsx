export default function ChatMessage({ msg }) {
  const isUser = msg.role === "user";

  return (
    <div className={`flex gap-4 ${isUser ? "justify-end" : "justify-start animate-fade-in"}`}>
      
      {/* Avatar cho AI */}
      {!isUser && (
        <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-600 via-purple-500 to-pink-500 flex items-center justify-center text-xs shadow-md shrink-0">
          ✨
        </div>
      )}

      {/* Nội dung tin nhắn */}
      <div
        className={`max-w-[85%] px-5 py-3 rounded-2xl text-[15px] leading-relaxed shadow-sm ${
          isUser
            ? "bg-gray-800 text-gray-100 rounded-tr-none border border-gray-700"
            : "bg-transparent text-gray-200"
        }`}
      >
        {msg.content}
      </div>

      {/* Avatar cho User */}
      {isUser && (
        <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-xs font-semibold shrink-0">
          U
        </div>
      )}
    </div>
  );
}