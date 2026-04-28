import "@/App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Toaster } from "@/components/ui/sonner";
import DashboardLayout from "@/components/DashboardLayout";
import ConversationPage from "@/pages/ConversationPage";
import SwarmPage from "@/pages/SwarmPage";
import SkillsPage from "@/pages/SkillsPage";
import DialerPage from "@/pages/DialerPage";
import SettingsPage from "@/pages/SettingsPage";

function App() {
  return (
    <BrowserRouter>
      <DashboardLayout>
        <Routes>
          <Route path="/" element={<ConversationPage />} />
          <Route path="/swarm" element={<SwarmPage />} />
          <Route path="/skills" element={<SkillsPage />} />
          <Route path="/dialer" element={<DialerPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Routes>
      </DashboardLayout>
      <Toaster position="bottom-right" theme="dark" />
    </BrowserRouter>
  );
}

export default App;
