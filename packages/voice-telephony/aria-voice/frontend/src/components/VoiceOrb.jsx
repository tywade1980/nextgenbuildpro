import { cn } from "@/lib/utils";
import { Mic, MicOff, Volume2 } from "lucide-react";

export const VoiceOrb = ({ 
  state = "idle", // idle, listening, speaking, disabled
  onClick,
  size = "lg"
}) => {
  const sizeClasses = {
    sm: "w-16 h-16",
    md: "w-24 h-24",
    lg: "w-32 h-32",
    xl: "w-40 h-40"
  };

  const iconSizes = {
    sm: "w-6 h-6",
    md: "w-8 h-8",
    lg: "w-10 h-10",
    xl: "w-12 h-12"
  };

  const getStateClass = () => {
    switch (state) {
      case "listening":
        return "orb-listening bg-primary/20 border-primary";
      case "speaking":
        return "orb-speaking bg-secondary/20 border-secondary";
      case "disabled":
        return "bg-muted/20 border-muted cursor-not-allowed";
      default:
        return "orb-idle bg-primary/10 border-primary/30 hover:border-primary/60";
    }
  };

  const getIcon = () => {
    switch (state) {
      case "listening":
        return <Mic className={cn(iconSizes[size], "text-primary")} />;
      case "speaking":
        return <Volume2 className={cn(iconSizes[size], "text-secondary")} />;
      case "disabled":
        return <MicOff className={cn(iconSizes[size], "text-muted-foreground")} />;
      default:
        return <Mic className={cn(iconSizes[size], "text-primary/70")} />;
    }
  };

  return (
    <div className="relative flex items-center justify-center">
      {/* Pulse rings for active states */}
      {(state === "listening" || state === "speaking") && (
        <>
          <div 
            className={cn(
              "absolute rounded-full pulse-ring",
              sizeClasses[size],
              state === "listening" ? "border-2 border-primary/50" : "border-2 border-secondary/50"
            )}
          />
          <div 
            className={cn(
              "absolute rounded-full pulse-ring",
              sizeClasses[size],
              state === "listening" ? "border-2 border-primary/30" : "border-2 border-secondary/30"
            )}
            style={{ animationDelay: "0.5s" }}
          />
        </>
      )}

      {/* Main orb */}
      <button
        onClick={onClick}
        disabled={state === "disabled"}
        className={cn(
          "relative rounded-full border-2 flex items-center justify-center transition-colors",
          sizeClasses[size],
          getStateClass()
        )}
        data-testid="voice-orb-btn"
        aria-label={state === "listening" ? "Stop listening" : "Start speaking"}
      >
        {getIcon()}
      </button>
    </div>
  );
};

export default VoiceOrb;
