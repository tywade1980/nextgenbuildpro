import { useState, useEffect } from "react";
import { Plus, Trash2, Play, Pause, Edit2, Save, X, Network, Zap, Bot } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";

const API = `${process.env.REACT_APP_BACKEND_URL}/api`;

const modelOptions = [
  { value: "gpt-5.2", label: "GPT-5.2 (OpenAI)" },
  { value: "gpt-4o", label: "GPT-4o (OpenAI)" },
  { value: "mistral-runpod", label: "Mistral (Runpod)" },
  { value: "claude-sonnet-4-5", label: "Claude Sonnet 4.5" },
];

export default function SwarmPage() {
  const [agents, setAgents] = useState([]);
  const [stats, setStats] = useState(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingAgent, setEditingAgent] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    model: "gpt-5.2",
    system_prompt: "You are a helpful assistant.",
    skills: [],
    status: "inactive"
  });

  useEffect(() => {
    fetchAgents();
    fetchStats();
  }, []);

  const fetchAgents = async () => {
    try {
      const { data } = await axios.get(`${API}/agents`);
      setAgents(data);
    } catch (error) {
      console.error("Failed to fetch agents:", error);
    }
  };

  const fetchStats = async () => {
    try {
      const { data } = await axios.get(`${API}/stats`);
      setStats(data);
    } catch (error) {
      console.error("Failed to fetch stats:", error);
    }
  };

  const createAgent = async () => {
    try {
      const { data } = await axios.post(`${API}/agents`, formData);
      setAgents([...agents, data]);
      setIsCreateOpen(false);
      resetForm();
      toast.success("Agent created successfully");
      fetchStats();
    } catch (error) {
      toast.error("Failed to create agent");
    }
  };

  const updateAgent = async () => {
    if (!editingAgent) return;
    try {
      await axios.put(`${API}/agents/${editingAgent.id}`, formData);
      setAgents(agents.map(a => a.id === editingAgent.id ? { ...a, ...formData } : a));
      setEditingAgent(null);
      resetForm();
      toast.success("Agent updated");
    } catch (error) {
      toast.error("Failed to update agent");
    }
  };

  const deleteAgent = async (agentId) => {
    try {
      await axios.delete(`${API}/agents/${agentId}`);
      setAgents(agents.filter(a => a.id !== agentId));
      toast.success("Agent deleted");
      fetchStats();
    } catch (error) {
      toast.error("Failed to delete agent");
    }
  };

  const toggleAgent = async (agentId) => {
    try {
      const { data } = await axios.post(`${API}/agents/${agentId}/toggle`);
      setAgents(agents.map(a => a.id === agentId ? { ...a, status: data.status } : a));
      toast.success(`Agent ${data.status === "active" ? "activated" : "deactivated"}`);
      fetchStats();
    } catch (error) {
      toast.error("Failed to toggle agent");
    }
  };

  const resetForm = () => {
    setFormData({
      name: "",
      description: "",
      model: "gpt-5.2",
      system_prompt: "You are a helpful assistant.",
      skills: [],
      status: "inactive"
    });
  };

  const startEditing = (agent) => {
    setEditingAgent(agent);
    setFormData({
      name: agent.name,
      description: agent.description,
      model: agent.model,
      system_prompt: agent.system_prompt,
      skills: agent.skills || [],
      status: agent.status
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl lg:text-3xl font-heading font-semibold neon-text-cyan">
            Agent Swarm
          </h1>
          <p className="text-muted-foreground mt-1">
            Orchestrate and manage your AI agent network
          </p>
        </div>
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogTrigger asChild>
            <Button className="bg-primary hover:bg-primary/90 neon-cyan" data-testid="create-agent-btn">
              <Plus className="w-4 h-4 mr-2" />
              Create Agent
            </Button>
          </DialogTrigger>
          <DialogContent className="glass border-border/50" data-testid="create-agent-dialog">
            <DialogHeader>
              <DialogTitle className="font-heading">Create New Agent</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label>Name</Label>
                <Input
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Agent name"
                  data-testid="agent-name-input"
                />
              </div>
              <div className="space-y-2">
                <Label>Description</Label>
                <Input
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="What does this agent do?"
                  data-testid="agent-description-input"
                />
              </div>
              <div className="space-y-2">
                <Label>Model</Label>
                <Select
                  value={formData.model}
                  onValueChange={(value) => setFormData({ ...formData, model: value })}
                >
                  <SelectTrigger data-testid="agent-model-select">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {modelOptions.map((opt) => (
                      <SelectItem key={opt.value} value={opt.value}>
                        {opt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>System Prompt</Label>
                <Textarea
                  value={formData.system_prompt}
                  onChange={(e) => setFormData({ ...formData, system_prompt: e.target.value })}
                  placeholder="Define the agent's personality and capabilities"
                  rows={4}
                  data-testid="agent-prompt-input"
                />
              </div>
              <Button
                onClick={createAgent}
                className="w-full bg-primary hover:bg-primary/90"
                disabled={!formData.name || !formData.description}
                data-testid="save-agent-btn"
              >
                Create Agent
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="glass border-border/50 card-hover">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Bot className="w-5 h-5 text-primary" />
              </div>
              <div>
                <p className="text-2xl font-heading font-semibold">{stats?.agents?.total || 0}</p>
                <p className="text-xs text-muted-foreground">Total Agents</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className="glass border-border/50 card-hover">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-green-500/10 flex items-center justify-center">
                <Zap className="w-5 h-5 text-green-500" />
              </div>
              <div>
                <p className="text-2xl font-heading font-semibold">{stats?.agents?.active || 0}</p>
                <p className="text-xs text-muted-foreground">Active</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className="glass border-border/50 card-hover">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-secondary/10 flex items-center justify-center">
                <Network className="w-5 h-5 text-secondary" />
              </div>
              <div>
                <p className="text-2xl font-heading font-semibold">{stats?.skills?.enabled || 0}</p>
                <p className="text-xs text-muted-foreground">Skills Active</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className="glass border-border/50 card-hover">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-orange-500/10 flex items-center justify-center">
                <Zap className="w-5 h-5 text-orange-500" />
              </div>
              <div>
                <p className="text-2xl font-heading font-semibold">{stats?.messages || 0}</p>
                <p className="text-xs text-muted-foreground">Messages</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Agents Grid */}
      <ScrollArea className="h-[calc(100vh-24rem)]">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {agents.map((agent) => (
            <Card
              key={agent.id}
              className={cn(
                "glass border-border/50 card-hover transition-all",
                agent.status === "active" && "border-green-500/30"
              )}
              data-testid={`agent-card-${agent.id}`}
            >
              <CardHeader className="pb-2">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className={cn(
                      "w-10 h-10 rounded-lg flex items-center justify-center",
                      agent.status === "active" ? "bg-green-500/10" : "bg-muted"
                    )}>
                      <Bot className={cn(
                        "w-5 h-5",
                        agent.status === "active" ? "text-green-500" : "text-muted-foreground"
                      )} />
                    </div>
                    <div>
                      <CardTitle className="text-base font-heading">{agent.name}</CardTitle>
                      <Badge
                        variant="outline"
                        className={cn(
                          "mt-1 text-xs",
                          agent.status === "active" ? "status-active" : "status-inactive"
                        )}
                      >
                        {agent.status}
                      </Badge>
                    </div>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="pt-2">
                <p className="text-sm text-muted-foreground line-clamp-2 mb-3">
                  {agent.description}
                </p>
                <div className="flex items-center gap-2 text-xs text-muted-foreground mb-4">
                  <span className="mono bg-muted/50 px-2 py-1 rounded">{agent.model}</span>
                </div>
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    variant={agent.status === "active" ? "destructive" : "default"}
                    onClick={() => toggleAgent(agent.id)}
                    className={agent.status === "inactive" ? "bg-green-600 hover:bg-green-700" : ""}
                    data-testid={`toggle-agent-${agent.id}`}
                  >
                    {agent.status === "active" ? (
                      <><Pause className="w-3 h-3 mr-1" /> Stop</>
                    ) : (
                      <><Play className="w-3 h-3 mr-1" /> Start</>
                    )}
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => startEditing(agent)}
                    data-testid={`edit-agent-${agent.id}`}
                  >
                    <Edit2 className="w-3 h-3" />
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => deleteAgent(agent.id)}
                    className="text-destructive hover:text-destructive"
                    data-testid={`delete-agent-${agent.id}`}
                  >
                    <Trash2 className="w-3 h-3" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}

          {agents.length === 0 && (
            <Card className="col-span-full glass border-border/50 border-dashed">
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Network className="w-12 h-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground text-center">
                  No agents created yet. Click "Create Agent" to get started.
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      </ScrollArea>

      {/* Edit Dialog */}
      <Dialog open={!!editingAgent} onOpenChange={(open) => !open && setEditingAgent(null)}>
        <DialogContent className="glass border-border/50">
          <DialogHeader>
            <DialogTitle className="font-heading">Edit Agent</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Name</Label>
              <Input
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                data-testid="edit-agent-name"
              />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                data-testid="edit-agent-description"
              />
            </div>
            <div className="space-y-2">
              <Label>Model</Label>
              <Select
                value={formData.model}
                onValueChange={(value) => setFormData({ ...formData, model: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {modelOptions.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>System Prompt</Label>
              <Textarea
                value={formData.system_prompt}
                onChange={(e) => setFormData({ ...formData, system_prompt: e.target.value })}
                rows={4}
                data-testid="edit-agent-prompt"
              />
            </div>
            <div className="flex gap-2">
              <Button
                onClick={updateAgent}
                className="flex-1 bg-primary hover:bg-primary/90"
                data-testid="update-agent-btn"
              >
                <Save className="w-4 h-4 mr-2" />
                Save Changes
              </Button>
              <Button
                variant="ghost"
                onClick={() => setEditingAgent(null)}
              >
                <X className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
