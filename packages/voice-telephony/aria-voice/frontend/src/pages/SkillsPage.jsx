import { useState, useEffect } from "react";
import { Plus, Trash2, Power, Code, MessageSquare, Calendar, Database, Zap, Share2, Briefcase, Plug } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
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
import { Textarea } from "@/components/ui/textarea";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

const API = `${process.env.REACT_APP_BACKEND_URL}/api`;

const categoryOptions = [
  { value: "communication", label: "Communication", icon: MessageSquare },
  { value: "calendar", label: "Calendar", icon: Calendar },
  { value: "storage", label: "Storage", icon: Database },
  { value: "automation", label: "Automation", icon: Zap },
  { value: "social", label: "Social", icon: Share2 },
  { value: "productivity", label: "Productivity", icon: Briefcase },
  { value: "custom", label: "Custom", icon: Code },
];

const presetSkills = [
  {
    name: "Slack Connector",
    description: "Send messages, create channels, and manage Slack workspace",
    category: "communication",
    icon: "message-square"
  },
  {
    name: "Google Calendar",
    description: "Create events, check availability, and manage schedules",
    category: "calendar",
    icon: "calendar"
  },
  {
    name: "Discord Bot",
    description: "Manage Discord servers, send messages, and handle commands",
    category: "social",
    icon: "share-2"
  },
  {
    name: "Gmail Integration",
    description: "Read, send, and organize emails automatically",
    category: "communication",
    icon: "message-square"
  },
  {
    name: "Google Drive",
    description: "Upload, download, and manage files in Drive",
    category: "storage",
    icon: "database"
  },
  {
    name: "Notion API",
    description: "Create pages, update databases, and sync content",
    category: "productivity",
    icon: "briefcase"
  },
];

const iconMap = {
  "message-square": MessageSquare,
  "calendar": Calendar,
  "database": Database,
  "zap": Zap,
  "share-2": Share2,
  "briefcase": Briefcase,
  "code": Code,
  "plug": Plug,
};

export default function SkillsPage() {
  const [skills, setSkills] = useState([]);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("installed");
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    category: "custom",
    config: {}
  });

  useEffect(() => {
    fetchSkills();
  }, []);

  const fetchSkills = async () => {
    try {
      const { data } = await axios.get(`${API}/skills`);
      setSkills(data);
    } catch (error) {
      console.error("Failed to fetch skills:", error);
    }
  };

  const createSkill = async (skillData = formData) => {
    try {
      const { data } = await axios.post(`${API}/skills`, skillData);
      setSkills([...skills, data]);
      setIsCreateOpen(false);
      setFormData({ name: "", description: "", category: "custom", config: {} });
      toast.success("Skill created successfully");
    } catch (error) {
      toast.error("Failed to create skill");
    }
  };

  const toggleSkill = async (skillId) => {
    try {
      const { data } = await axios.put(`${API}/skills/${skillId}/toggle`);
      setSkills(skills.map(s => s.id === skillId ? { ...s, enabled: data.enabled } : s));
      toast.success(`Skill ${data.enabled ? "enabled" : "disabled"}`);
    } catch (error) {
      toast.error("Failed to toggle skill");
    }
  };

  const deleteSkill = async (skillId) => {
    try {
      await axios.delete(`${API}/skills/${skillId}`);
      setSkills(skills.filter(s => s.id !== skillId));
      toast.success("Skill deleted");
    } catch (error) {
      toast.error("Failed to delete skill");
    }
  };

  const installPreset = (preset) => {
    createSkill({
      name: preset.name,
      description: preset.description,
      category: preset.category,
      config: {}
    });
  };

  const getIcon = (iconName) => {
    const IconComponent = iconMap[iconName] || Plug;
    return IconComponent;
  };

  const getCategoryIcon = (category) => {
    const cat = categoryOptions.find(c => c.value === category);
    return cat ? cat.icon : Plug;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl lg:text-3xl font-heading font-semibold neon-text-purple">
            Skills & Connectors
          </h1>
          <p className="text-muted-foreground mt-1">
            Extend ARIA's capabilities with plugins and integrations
          </p>
        </div>
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogTrigger asChild>
            <Button className="bg-secondary hover:bg-secondary/90 neon-purple" data-testid="create-skill-btn">
              <Plus className="w-4 h-4 mr-2" />
              Create Skill
            </Button>
          </DialogTrigger>
          <DialogContent className="glass border-border/50" data-testid="create-skill-dialog">
            <DialogHeader>
              <DialogTitle className="font-heading">Create Custom Skill</DialogTitle>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label>Name</Label>
                <Input
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Skill name"
                  data-testid="skill-name-input"
                />
              </div>
              <div className="space-y-2">
                <Label>Description</Label>
                <Textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="What does this skill do?"
                  data-testid="skill-description-input"
                />
              </div>
              <div className="space-y-2">
                <Label>Category</Label>
                <Select
                  value={formData.category}
                  onValueChange={(value) => setFormData({ ...formData, category: value })}
                >
                  <SelectTrigger data-testid="skill-category-select">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {categoryOptions.map((opt) => (
                      <SelectItem key={opt.value} value={opt.value}>
                        <div className="flex items-center gap-2">
                          <opt.icon className="w-4 h-4" />
                          {opt.label}
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button
                onClick={() => createSkill()}
                className="w-full bg-secondary hover:bg-secondary/90"
                disabled={!formData.name || !formData.description}
                data-testid="save-skill-btn"
              >
                Create Skill
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="bg-card/50">
          <TabsTrigger value="installed" data-testid="installed-tab">
            Installed ({skills.length})
          </TabsTrigger>
          <TabsTrigger value="marketplace" data-testid="marketplace-tab">
            Marketplace
          </TabsTrigger>
        </TabsList>

        {/* Installed Skills */}
        <TabsContent value="installed" className="mt-6">
          <ScrollArea className="h-[calc(100vh-20rem)]">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {skills.map((skill) => {
                const IconComponent = getIcon(skill.icon);
                return (
                  <Card
                    key={skill.id}
                    className={cn(
                      "glass border-border/50 card-hover transition-all",
                      skill.enabled && "border-secondary/30"
                    )}
                    data-testid={`skill-card-${skill.id}`}
                  >
                    <CardHeader className="pb-2">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-3">
                          <div className={cn(
                            "w-10 h-10 rounded-lg flex items-center justify-center",
                            skill.enabled ? "bg-secondary/10" : "bg-muted"
                          )}>
                            <IconComponent className={cn(
                              "w-5 h-5",
                              skill.enabled ? "text-secondary" : "text-muted-foreground"
                            )} />
                          </div>
                          <div>
                            <CardTitle className="text-base font-heading">{skill.name}</CardTitle>
                            <Badge variant="outline" className="mt-1 text-xs">
                              {skill.category}
                            </Badge>
                          </div>
                        </div>
                        <Switch
                          checked={skill.enabled}
                          onCheckedChange={() => toggleSkill(skill.id)}
                          data-testid={`toggle-skill-${skill.id}`}
                        />
                      </div>
                    </CardHeader>
                    <CardContent className="pt-2">
                      <p className="text-sm text-muted-foreground line-clamp-2 mb-4">
                        {skill.description}
                      </p>
                      <div className="flex justify-end">
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => deleteSkill(skill.id)}
                          className="text-destructive hover:text-destructive"
                          data-testid={`delete-skill-${skill.id}`}
                        >
                          <Trash2 className="w-3 h-3 mr-1" />
                          Remove
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}

              {skills.length === 0 && (
                <Card className="col-span-full glass border-border/50 border-dashed">
                  <CardContent className="flex flex-col items-center justify-center py-12">
                    <Plug className="w-12 h-12 text-muted-foreground mb-4" />
                    <p className="text-muted-foreground text-center">
                      No skills installed. Check out the marketplace or create a custom skill.
                    </p>
                  </CardContent>
                </Card>
              )}
            </div>
          </ScrollArea>
        </TabsContent>

        {/* Marketplace */}
        <TabsContent value="marketplace" className="mt-6">
          <ScrollArea className="h-[calc(100vh-20rem)]">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {presetSkills.map((preset, idx) => {
                const IconComponent = getIcon(preset.icon);
                const isInstalled = skills.some(s => s.name === preset.name);
                return (
                  <Card
                    key={idx}
                    className="glass border-border/50 card-hover"
                    data-testid={`preset-${idx}`}
                  >
                    <CardHeader className="pb-2">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center">
                          <IconComponent className="w-5 h-5 text-muted-foreground" />
                        </div>
                        <div>
                          <CardTitle className="text-base font-heading">{preset.name}</CardTitle>
                          <Badge variant="outline" className="mt-1 text-xs">
                            {preset.category}
                          </Badge>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent className="pt-2">
                      <p className="text-sm text-muted-foreground line-clamp-2 mb-4">
                        {preset.description}
                      </p>
                      <Button
                        size="sm"
                        onClick={() => installPreset(preset)}
                        disabled={isInstalled}
                        className={cn(
                          "w-full",
                          isInstalled 
                            ? "bg-muted text-muted-foreground" 
                            : "bg-secondary hover:bg-secondary/90"
                        )}
                        data-testid={`install-preset-${idx}`}
                      >
                        {isInstalled ? "Installed" : "Install"}
                      </Button>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </ScrollArea>
        </TabsContent>
      </Tabs>
    </div>
  );
}
