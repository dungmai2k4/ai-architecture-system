export default function Sidebar() {
  return (
    <div className="w-64 h-full bg-gray-950 p-4 flex flex-col justify-between hidden md:flex border-r border-gray-800">
      <div className="space-y-4">
        <button className="w-full py-3 px-4 bg-gray-900 hover:bg-gray-800 text-sm font-medium rounded-full flex items-center gap-2 border border-gray-700 transition">
          <span>+</span> Cuộc trò chuyện mới
        </button>
        
        <div className="space-y-1">
          <p className="text-xs text-gray-500 font-semibold px-2 uppercase tracking-wider">Gần đây</p>
          <div className="text-sm text-gray-300 hover:bg-gray-800 p-2 rounded-lg cursor-pointer truncate">
            Thiết kế nhà phố 2 tầng
          </div>
          <div className="text-sm text-gray-300 hover:bg-gray-800 p-2 rounded-lg cursor-pointer truncate">
            Ý tưởng phòng khách tân cổ điển
          </div>
        </div>
      </div>
      
      <div className="text-xs text-gray-500 border-t border-gray-800 pt-4">
        FPT University Project © 2026
      </div>
    </div>
  );
}