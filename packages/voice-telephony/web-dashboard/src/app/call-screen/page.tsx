'use client';

import { useState, useEffect } from 'react';
import { Phone, PhoneCall, PhoneOff, Mic, MicOff, Brain, ArrowLeft, Clock, User, MapPin } from 'lucide-react';
import Link from 'next/link';

interface CallData {
  id: string;
  phoneNumber: string;
  callerName?: string;
  location?: string;
  callType: 'incoming' | 'outgoing';
  status: 'ringing' | 'active' | 'ended';
  startTime?: Date;
  duration: number;
}

interface AIAnalysis {
  intent: string;
  urgency: 'low' | 'medium' | 'high' | 'urgent';
  category: string;
  confidence: number;
  suggestedAction: string;
  keyTopics: string[];
  sentiment: 'positive' | 'neutral' | 'negative';
}

export default function CallScreenPage() {
  const [currentCall, setCurrentCall] = useState<CallData | null>(null);
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [aiAnalysis, setAiAnalysis] = useState<AIAnalysis | null>(null);
  const [callHistory, setCallHistory] = useState<CallData[]>([]);

  // Simulate incoming call
  useEffect(() => {
    const simulateCall = () => {
      const mockCall: CallData = {
        id: `call_${Date.now()}`,
        phoneNumber: '+1 (555) 123-4567',
        callerName: 'Sarah Johnson',
        location: 'Atlanta, GA',
        callType: 'incoming',
        status: 'ringing',
        duration: 0
      };
      setCurrentCall(mockCall);
    };

    // Simulate call after 3 seconds
    const timer = setTimeout(simulateCall, 3000);
    return () => clearTimeout(timer);
  }, []);

  // Update call duration
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (currentCall && currentCall.status === 'active') {
      interval = setInterval(() => {
        setCurrentCall(prev => prev ? { ...prev, duration: prev.duration + 1 } : null);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [currentCall?.status, currentCall]);

  const handleAnswerCall = async () => {
    if (currentCall) {
      setCurrentCall({
        ...currentCall,
        status: 'active',
        startTime: new Date()
      });
      setIsListening(true);
      
      // Simulate AI analysis after a few seconds
      setTimeout(() => {
        setTranscript("Hello, I'm interested in getting a quote for a kitchen renovation. The space is about 200 square feet and I'm looking to update everything including cabinets, countertops, and appliances. My budget is around $35,000. When would be a good time to schedule a consultation?");
        
        setAiAnalysis({
          intent: 'project_inquiry',
          urgency: 'medium',
          category: 'new_business',
          confidence: 0.92,
          suggestedAction: 'Schedule consultation appointment',
          keyTopics: ['kitchen renovation', 'budget $35,000', 'consultation scheduling'],
          sentiment: 'positive'
        });
      }, 5000);
    }
  };

  const handleEndCall = () => {
    if (currentCall) {
      const endedCall = {
        ...currentCall,
        status: 'ended' as const
      };
      setCallHistory(prev => [endedCall, ...prev]);
      setCurrentCall(null);
      setIsListening(false);
      setTranscript('');
      setAiAnalysis(null);
    }
  };

  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const getUrgencyColor = (urgency: string) => {
    switch (urgency) {
      case 'urgent': return 'text-red-600 bg-red-100';
      case 'high': return 'text-orange-600 bg-orange-100';
      case 'medium': return 'text-yellow-600 bg-yellow-100';
      case 'low': return 'text-green-600 bg-green-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Link href="/" className="mr-4">
                <ArrowLeft className="h-6 w-6 text-gray-600 hover:text-gray-900" />
              </Link>
              <Phone className="h-8 w-8 text-blue-600" />
              <h1 className="ml-2 text-xl font-bold text-gray-900">Smart Call Screen</h1>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-500 rounded-full mr-2"></div>
                <span className="text-sm text-gray-600">AI Assistant: Online</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Call Interface */}
          <div className="lg:col-span-2">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-8">
                {currentCall ? (
                  <div className="text-center">
                    {/* Call Status */}
                    <div className="mb-6">
                      <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                        currentCall.status === 'ringing' ? 'bg-blue-100 text-blue-800' :
                        currentCall.status === 'active' ? 'bg-green-100 text-green-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {currentCall.status === 'ringing' && 'Incoming Call'}
                        {currentCall.status === 'active' && 'Call Active'}
                        {currentCall.status === 'ended' && 'Call Ended'}
                      </div>
                    </div>

                    {/* Caller Info */}
                    <div className="mb-8">
                      <div className="w-24 h-24 bg-blue-500 rounded-full mx-auto mb-4 flex items-center justify-center">
                        <User className="h-12 w-12 text-white" />
                      </div>
                      <h2 className="text-2xl font-bold text-gray-900 mb-2">
                        {currentCall.callerName || 'Unknown Caller'}
                      </h2>
                      <p className="text-lg text-gray-600 mb-1">{currentCall.phoneNumber}</p>
                      {currentCall.location && (
                        <div className="flex items-center justify-center text-gray-500">
                          <MapPin className="h-4 w-4 mr-1" />
                          <span className="text-sm">{currentCall.location}</span>
                        </div>
                      )}
                    </div>

                    {/* Call Duration */}
                    {currentCall.status === 'active' && (
                      <div className="mb-6">
                        <div className="flex items-center justify-center text-gray-600">
                          <Clock className="h-5 w-5 mr-2" />
                          <span className="text-xl font-mono">{formatDuration(currentCall.duration)}</span>
                        </div>
                      </div>
                    )}

                    {/* Call Controls */}
                    <div className="flex justify-center space-x-4">
                      {currentCall.status === 'ringing' && (
                        <>
                          <button
                            onClick={handleAnswerCall}
                            className="bg-green-500 hover:bg-green-600 text-white rounded-full p-4 transition-colors"
                          >
                            <PhoneCall className="h-8 w-8" />
                          </button>
                          <button
                            onClick={handleEndCall}
                            className="bg-red-500 hover:bg-red-600 text-white rounded-full p-4 transition-colors"
                          >
                            <PhoneOff className="h-8 w-8" />
                          </button>
                        </>
                      )}
                      
                      {currentCall.status === 'active' && (
                        <>
                          <button
                            onClick={() => setIsListening(!isListening)}
                            className={`${isListening ? 'bg-red-500 hover:bg-red-600' : 'bg-gray-500 hover:bg-gray-600'} text-white rounded-full p-4 transition-colors`}
                          >
                            {isListening ? <Mic className="h-8 w-8" /> : <MicOff className="h-8 w-8" />}
                          </button>
                          <button
                            onClick={handleEndCall}
                            className="bg-red-500 hover:bg-red-600 text-white rounded-full p-4 transition-colors"
                          >
                            <PhoneOff className="h-8 w-8" />
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <Phone className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                    <h2 className="text-xl font-semibold text-gray-900 mb-2">Ready to Receive Calls</h2>
                    <p className="text-gray-600">AI-powered call screening is active and ready to assist.</p>
                  </div>
                )}
              </div>
            </div>

            {/* Call Transcript */}
            {transcript && (
              <div className="mt-6 bg-white shadow rounded-lg">
                <div className="px-6 py-4 border-b">
                  <h3 className="text-lg font-medium text-gray-900">Live Transcript</h3>
                </div>
                <div className="px-6 py-4">
                  <p className="text-gray-700 leading-relaxed">{transcript}</p>
                </div>
              </div>
            )}
          </div>

          {/* AI Analysis & Call History */}
          <div className="space-y-6">
            {/* AI Analysis */}
            {aiAnalysis && (
              <div className="bg-white shadow rounded-lg">
                <div className="px-6 py-4 border-b">
                  <div className="flex items-center">
                    <Brain className="h-5 w-5 text-purple-600 mr-2" />
                    <h3 className="text-lg font-medium text-gray-900">AI Analysis</h3>
                  </div>
                </div>
                <div className="px-6 py-4 space-y-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">Intent</label>
                    <p className="text-gray-900 capitalize">{aiAnalysis.intent.replace('_', ' ')}</p>
                  </div>
                  
                  <div>
                    <label className="text-sm font-medium text-gray-500">Urgency</label>
                    <div className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${getUrgencyColor(aiAnalysis.urgency)}`}>
                      {aiAnalysis.urgency.toUpperCase()}
                    </div>
                  </div>
                  
                  <div>
                    <label className="text-sm font-medium text-gray-500">Confidence</label>
                    <div className="flex items-center">
                      <div className="flex-1 bg-gray-200 rounded-full h-2 mr-2">
                        <div 
                          className="bg-green-500 h-2 rounded-full transition-all"
                          style={{ width: `${aiAnalysis.confidence * 100}%` }}
                        ></div>
                      </div>
                      <span className="text-sm text-gray-600">{Math.round(aiAnalysis.confidence * 100)}%</span>
                    </div>
                  </div>
                  
                  <div>
                    <label className="text-sm font-medium text-gray-500">Suggested Action</label>
                    <p className="text-gray-900">{aiAnalysis.suggestedAction}</p>
                  </div>
                  
                  <div>
                    <label className="text-sm font-medium text-gray-500">Key Topics</label>
                    <div className="flex flex-wrap gap-2 mt-1">
                      {aiAnalysis.keyTopics.map((topic, index) => (
                        <span key={index} className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs">
                          {topic}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Call History */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b">
                <h3 className="text-lg font-medium text-gray-900">Recent Calls</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {callHistory.length > 0 ? (
                  callHistory.slice(0, 5).map((call) => (
                    <div key={call.id} className="px-6 py-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-medium text-gray-900">{call.callerName || 'Unknown'}</p>
                          <p className="text-sm text-gray-600">{call.phoneNumber}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm text-gray-500">{formatDuration(call.duration)}</p>
                          <p className="text-xs text-gray-400">
                            {call.startTime?.toLocaleTimeString()}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="px-6 py-8 text-center text-gray-500">
                    <Phone className="h-8 w-8 mx-auto mb-2 opacity-50" />
                    <p>No recent calls</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}