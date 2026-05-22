import { BrowserRouter, Routes, Route } from 'react-router-dom'

import HomePage from '../pages/HomePage'
import DesignPage from '../pages/DesignPage'
import ResultPage from '../pages/ResultPage'
import Viewer3DPage from '../pages/Viewer3DPage'

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/design" element={<DesignPage />} />
        <Route path="/result" element={<ResultPage />} />
        <Route path="/viewer" element={<Viewer3DPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter