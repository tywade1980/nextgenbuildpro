'use client'

import Link from "next/link";
import { useState } from "react";
import { Phone, ArrowLeft, Clock, User, AlertCircle, CheckCircle, XCircle } from "lucide-react";

interface Call {
  id: string;
  phoneNumber: string;
  client?: {
    name: string;
  };
  status: 'incoming' | 'answered' | 'screened' | 'missed';
  duration?: number;
  transcript?: string;
  summary?: string;
  sentiment?: 'positive' | 'neutral' | 'negative';
  priority: 'low' | 'normal' | 'high' | 'urgent';
  createdAt: string;
}

// Mock data for demonstration
const mockCalls: Call[] = [
  {
    id: '1',
    phoneNumber: '+1-555-0123',
    client: { name: 'ABC Construction Corp' },
    status: 'answered',
    duration: 180,
    transcript: 'Client called regarding the residential complex project timeline. Discussed current progress and upcoming milestones.',
    summary: 'Project status update call - client satisfied with progress',
    sentiment: 'positive',
    priority: 'normal',
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: '2',
    phoneNumber: '+1-555-9999',
    status: 'screened',
    duration: 30,
    transcript: 'Automated sales call detected. Caller was asking about general construction services.',
    summary: 'Sales call - not a priority',
    sentiment: 'neutral',
    priority: 'low',
    createdAt: '2024-01-15T09:45:00Z'
  },
  {
    id: '3',
    phoneNumber: '+1-555-0456',
    client: { name: 'XYZ Development LLC' },
    status: 'answered',
    duration: 420,
    transcript: 'Emergency call about permit delays affecting the office building renovation project. Client expressed concern about timeline impact.',
    summary: 'Urgent: Permit delays discussion, requires immediate attention',
    sentiment: 'negative',
    priority: 'urgent',
    createdAt: '2024-01-15T08:15:00Z'
  },
  {
    id: '4',
    phoneNumber: '+1-555-7777',
    status: 'missed',
    priority: 'normal',
    createdAt: '2024-01-15T07:30:00Z'
  },
  {
    id: '5',
    phoneNumber: '+1-555-1234',
    status: 'screened',
    duration: 25,
    transcript: 'Telemarketing call for construction equipment financing. Automatically screened as low priority.',
    summary: 'Telemarketing - financing services',
    sentiment: 'neutral',
    priority: 'low',
    createdAt: '2024-01-15T07:00:00Z'
  }
];

const statusColors = {
  incoming: 'bg-blue-100 text-blue-800',
  answered: 'bg-green-100 text-green-800',
  screened: 'bg-yellow-100 text-yellow-800',
  missed: 'bg-red-100 text-red-800'
};

const priorityColors = {
  low: 'bg-gray-100 text-gray-800',
  normal: 'bg-blue-100 text-blue-800',
  high: 'bg-orange-100 text-orange-800',
  urgent: 'bg-red-100 text-red-800'
};

const sentimentColors = {
  positive: 'text-green-600',
  neutral: 'text-gray-600',
  negative: 'text-red-600'
};

const StatusIcon = ({ status }: { status: string }) => {
  switch (status) {
    case 'answered':
      return <CheckCircle className="h-4 w-4 text-green-600" />;
    case 'screened':
      return <AlertCircle className="h-4 w-4 text-yellow-600" />;
    case 'missed':
      return <XCircle className="h-4 w-4 text-red-600" />;
    default:
      return <Phone className="h-4 w-4 text-blue-600" />;
  }
};

export default function CallsPage() {
  const [calls] = useState<Call[]>(mockCalls);
  const [filter, setFilter] = useState<'all' | 'incoming' | 'answered' | 'screened' | 'missed'>('all');
  const [selectedCall, setSelectedCall] = useState<Call | null>(null);

  const filteredCalls = filter === 'all' 
    ? calls 
    : calls.filter(call => call.status === filter);

  const formatDuration = (seconds?: number) => {
    if (!seconds) return 'N/A';
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Link href="/" className="flex items-center text-gray-600 hover:text-gray-900">
                <ArrowLeft className="h-5 w-5 mr-2" />
                Back to Dashboard
              </Link>
            </div>
            <div className="flex items-center">
              <Phone className="h-8 w-8 text-blue-600" />
              <h1 className="ml-2 text-xl font-bold text-gray-900">Call Center</h1>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900">Smart Call Screening & Management</h2>
          <p className="text-gray-600 mt-2">AI-powered call analysis and automated screening for construction business inquiries</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Phone className="h-8 w-8 text-blue-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Calls Today</p>
                <p className="text-2xl font-bold text-gray-900">{calls.length}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <CheckCircle className="h-8 w-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Answered</p>
                <p className="text-2xl font-bold text-gray-900">{calls.filter(c => c.status === 'answered').length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <AlertCircle className="h-8 w-8 text-yellow-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Auto-Screened</p>
                <p className="text-2xl font-bold text-gray-900">{calls.filter(c => c.status === 'screened').length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <XCircle className="h-8 w-8 text-red-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Missed</p>
                <p className="text-2xl font-bold text-gray-900">{calls.filter(c => c.status === 'missed').length}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Call List */}
          <div className="lg:col-span-2">
            {/* Filter Tabs */}
            <div className="mb-6">
              <div className="border-b border-gray-200">
                <nav className="-mb-px flex space-x-8">
                  {[
                    { key: 'all', label: 'All Calls' },
                    { key: 'answered', label: 'Answered' },
                    { key: 'screened', label: 'Screened' },
                    { key: 'missed', label: 'Missed' },
                  ].map((tab) => (
                    <button
                      key={tab.key}
                      onClick={() => setFilter(tab.key as 'all' | 'incoming' | 'answered' | 'screened' | 'missed')}
                      className={`${
                        filter === tab.key
                          ? 'border-blue-500 text-blue-600'
                          : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                      } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm`}
                    >
                      {tab.label}
                    </button>
                  ))}
                </nav>
              </div>
            </div>

            {/* Call List */}
            <div className="space-y-4">
              {filteredCalls.map((call) => (
                <div 
                  key={call.id} 
                  className={`bg-white rounded-lg shadow p-4 cursor-pointer hover:shadow-md transition-shadow ${
                    selectedCall?.id === call.id ? 'ring-2 ring-blue-500' : ''
                  }`}
                  onClick={() => setSelectedCall(call)}
                >
                  <div className="flex justify-between items-start">
                    <div className="flex items-center space-x-3">
                      <StatusIcon status={call.status} />
                      <div>
                        <p className="font-medium text-gray-900">{call.phoneNumber}</p>
                        {call.client && (
                          <p className="text-sm text-gray-600">{call.client.name}</p>
                        )}
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm text-gray-500">{formatTime(call.createdAt)}</p>
                      <div className="flex space-x-2 mt-1">
                        <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${statusColors[call.status]}`}>
                          {call.status}
                        </span>
                        <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${priorityColors[call.priority]}`}>
                          {call.priority}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  {call.summary && (
                    <div className="mt-3">
                      <p className="text-sm text-gray-700">{call.summary}</p>
                    </div>
                  )}
                  
                  <div className="mt-3 flex justify-between items-center text-sm text-gray-500">
                    <div className="flex items-center space-x-4">
                      {call.duration && (
                        <div className="flex items-center">
                          <Clock className="h-4 w-4 mr-1" />
                          {formatDuration(call.duration)}
                        </div>
                      )}
                      {call.sentiment && (
                        <div className={`flex items-center ${sentimentColors[call.sentiment]}`}>
                          <span className="text-xs font-medium">
                            {call.sentiment.charAt(0).toUpperCase() + call.sentiment.slice(1)}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Call Details Panel */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow p-6 sticky top-8">
              {selectedCall ? (
                <div>
                  <div className="flex items-center mb-4">
                    <Phone className="h-6 w-6 text-blue-600" />
                    <h3 className="ml-2 text-lg font-semibold text-gray-900">Call Details</h3>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="text-sm font-medium text-gray-600">Phone Number</label>
                      <p className="text-gray-900">{selectedCall.phoneNumber}</p>
                    </div>

                    {selectedCall.client && (
                      <div>
                        <label className="text-sm font-medium text-gray-600">Client</label>
                        <p className="text-gray-900">{selectedCall.client.name}</p>
                      </div>
                    )}

                    <div>
                      <label className="text-sm font-medium text-gray-600">Status</label>
                      <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${statusColors[selectedCall.status]}`}>
                        {selectedCall.status}
                      </span>
                    </div>

                    <div>
                      <label className="text-sm font-medium text-gray-600">Priority</label>
                      <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${priorityColors[selectedCall.priority]}`}>
                        {selectedCall.priority}
                      </span>
                    </div>

                    {selectedCall.duration && (
                      <div>
                        <label className="text-sm font-medium text-gray-600">Duration</label>
                        <p className="text-gray-900">{formatDuration(selectedCall.duration)}</p>
                      </div>
                    )}

                    {selectedCall.sentiment && (
                      <div>
                        <label className="text-sm font-medium text-gray-600">Sentiment</label>
                        <p className={`${sentimentColors[selectedCall.sentiment]} font-medium`}>
                          {selectedCall.sentiment.charAt(0).toUpperCase() + selectedCall.sentiment.slice(1)}
                        </p>
                      </div>
                    )}

                    {selectedCall.summary && (
                      <div>
                        <label className="text-sm font-medium text-gray-600">AI Summary</label>
                        <p className="text-gray-900 text-sm">{selectedCall.summary}</p>
                      </div>
                    )}

                    {selectedCall.transcript && (
                      <div>
                        <label className="text-sm font-medium text-gray-600">Transcript</label>
                        <div className="mt-2 p-3 bg-gray-50 rounded text-sm text-gray-700">
                          {selectedCall.transcript}
                        </div>
                      </div>
                    )}

                    <div className="pt-4 border-t">
                      <button className="w-full bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 flex items-center justify-center">
                        <User className="h-4 w-4 mr-2" />
                        Call Back
                      </button>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center text-gray-500">
                  <Phone className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                  <p>Select a call to view details</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}