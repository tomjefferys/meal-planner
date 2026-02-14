import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import MealsPage from './pages/MealsPage';
import PeoplePage from './pages/PeoplePage';
import PlannerPage from './pages/PlannerPage';
import ShoppingPage from './pages/ShoppingPage';
import WeeklySummaryPage from './pages/WeeklySummaryPage';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Navigate to="/planner" replace />} />
        <Route path="/planner" element={<PlannerPage />} />
        <Route path="/meals" element={<MealsPage />} />
        <Route path="/people" element={<PeoplePage />} />
        <Route path="/shopping" element={<ShoppingPage />} />
        <Route path="/summary" element={<WeeklySummaryPage />} />
      </Route>
    </Routes>
  );
}
