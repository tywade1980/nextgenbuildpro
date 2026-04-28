import { useState, useEffect } from "react";
import { Phone, PhoneIncoming, PhoneOutgoing, PhoneMissed, Clock, User, AlertTriangle } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

const API = `${process.env.REACT_APP_BACKEND_URL}/api`;

const dialPad = [
  ["1", "2", "3"],
  ["4", "5", "6"],
  ["7", "8", "9"],
  ["*", "0", "#"]
];

export default function DialerPage() {
  const [phoneNumber, setPhoneNumber] = useState("");
  const [calls, setCalls] = useState([]);
  const [isDialing, setIsDialing] = useState(false);

  useEffect(() => {
    fetchCalls();
  }, []);

  const fetchCalls = async () => {
    try {
      const { data } = await axios.get(`${API}/calls`);
      setCalls(data);
    } catch (error) {
      console.error("Failed to fetch calls:", error);
    }
  };

  const handleDigitPress = (digit) => {
    if (phoneNumber.length < 15) {
      setPhoneNumber(prev => prev + digit);
    }
  };

  const handleBackspace = () => {
    setPhoneNumber(prev => prev.slice(0, -1));
  };

  const handleCall = async () => {
    if (!phoneNumber) return;
    
    setIsDialing(true);
    try {
      const { data } = await axios.post(`${API}/calls/initiate`, null, {
        params: { phone_number: phoneNumber }
      });
      toast.info(data.message);
      await fetchCalls();
      setPhoneNumber("");
    } catch (error) {
      toast.error("Failed to initiate call");
    } finally {
      setIsDialing(false);
    }
  };

  const formatDuration = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const getCallIcon = (direction, status) => {
    if (status === "missed") return <PhoneMissed className="w-4 h-4 text-destructive" />;
    if (direction === "inbound") return <PhoneIncoming className="w-4 h-4 text-green-500" />;
    return <PhoneOutgoing className="w-4 h-4 text-primary" />;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl lg:text-3xl font-heading font-semibold neon-text-cyan">
          Call Center
        </h1>
        <p className="text-muted-foreground mt-1">
          Handle voice calls through ARIA
        </p>
      </div>

      {/* Carrier Integration Notice */}
      <Alert className="glass border-orange-500/30 bg-orange-500/5">
        <AlertTriangle className="h-4 w-4 text-orange-500" />
        <AlertTitle className="text-orange-500 font-heading">Carrier Integration Required</AlertTitle>
        <AlertDescription className="text-muted-foreground">
          To enable live call handling, you need to configure carrier-level permissions and provide
          audio stream access. Contact your telephony provider for API credentials.
        </AlertDescription>
      </Alert>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Dialer */}
        <Card className="glass border-border/50" data-testid="dialer-card">
          <CardHeader>
            <CardTitle className="font-heading flex items-center gap-2">
              <Phone className="w-5 h-5 text-primary" />
              Dialer
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Phone Number Display */}
            <div className="text-center">
              <Input
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value.replace(/[^0-9+*#]/g, ''))}
                placeholder="Enter number"
                className="text-center text-2xl font-mono h-14 bg-background/50"
                data-testid="phone-input"
              />
            </div>

            {/* Dial Pad */}
            <div className="grid grid-cols-3 gap-3">
              {dialPad.map((row, rowIdx) =>
                row.map((digit) => (
                  <Button
                    key={digit}
                    variant="outline"
                    onClick={() => handleDigitPress(digit)}
                    className="h-14 text-xl font-mono hover:bg-primary/10 hover:border-primary/50"
                    data-testid={`dial-${digit}`}
                  >
                    {digit}
                  </Button>
                ))
              )}
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              <Button
                variant="outline"
                onClick={handleBackspace}
                className="flex-1"
                disabled={!phoneNumber}
                data-testid="backspace-btn"
              >
                Delete
              </Button>
              <Button
                onClick={handleCall}
                disabled={!phoneNumber || isDialing}
                className="flex-1 bg-green-600 hover:bg-green-700"
                data-testid="call-btn"
              >
                <Phone className="w-4 h-4 mr-2" />
                {isDialing ? "Calling..." : "Call"}
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Call History */}
        <Card className="glass border-border/50" data-testid="call-history-card">
          <CardHeader>
            <CardTitle className="font-heading flex items-center gap-2">
              <Clock className="w-5 h-5 text-secondary" />
              Recent Calls
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-[400px]">
              <div className="space-y-2">
                {calls.map((call) => (
                  <div
                    key={call.id}
                    className="flex items-center justify-between p-3 rounded-lg bg-card/50 hover:bg-card transition-colors"
                    data-testid={`call-${call.id}`}
                  >
                    <div className="flex items-center gap-3">
                      {getCallIcon(call.direction, call.status)}
                      <div>
                        <p className="font-mono text-sm">{call.phone_number}</p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(call.created_at).toLocaleString()}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <Badge
                        variant="outline"
                        className={cn(
                          "text-xs",
                          call.status === "completed" && "status-active",
                          call.status === "missed" && "bg-destructive/10 text-destructive border-destructive/30",
                          call.status === "pending" && "status-pending"
                        )}
                      >
                        {call.status}
                      </Badge>
                      {call.duration > 0 && (
                        <p className="text-xs text-muted-foreground mt-1">
                          {formatDuration(call.duration)}
                        </p>
                      )}
                    </div>
                  </div>
                ))}

                {calls.length === 0 && (
                  <div className="flex flex-col items-center justify-center py-12 text-center">
                    <Phone className="w-12 h-12 text-muted-foreground mb-4" />
                    <p className="text-muted-foreground">
                      No call history yet
                    </p>
                  </div>
                )}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>

      {/* Audio Stream Placeholder */}
      <Card className="glass border-border/50 border-dashed" data-testid="audio-stream-card">
        <CardContent className="flex flex-col items-center justify-center py-8">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-2 h-2 rounded-full bg-orange-500 animate-pulse" />
            <span className="text-sm text-muted-foreground">Audio Stream: Waiting for connection</span>
          </div>
          <p className="text-xs text-muted-foreground text-center max-w-md">
            Once carrier permissions are granted, live audio streams will be processed here.
            ARIA will transcribe and respond to calls automatically.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
