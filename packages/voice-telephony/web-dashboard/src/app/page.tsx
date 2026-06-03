'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { Phone, Building, Brain, BarChart3, Users, CheckCircle, ImageIcon, Calendar } from 'lucide-react';

interface DashboardStats {
  activeProjects: number;
  pendingCalls: number;
  aiModelsLoaded: number;
  totalClients: number;
}

export default function Home() {
  const [stats, setStats] = useState<DashboardStats>({
    activeProjects: 0,
    pendingCalls: 0,
    aiModelsLoaded: 0,
    totalClients: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simulate loading dashboard data
    const loadDashboardData = async () => {
      try {
        // In a real app, this would fetch from API
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        setStats({
          activeProjects: 12,
          pendingCalls: 3,
          aiModelsLoaded: 3,
          totalClients: 45
        });
      } catch (error) {
        console.error('Error loading dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading Construction AI Dashboard...</p>
        </div>
      </div>
    );
  }
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Building className="h-8 w-8 text-blue-600" />
              <h1 className="ml-2 text-xl font-bold text-gray-900">Construction AI</h1>
            </div>
            <nav className="flex space-x-4">
              <Link href="/dashboard" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                Dashboard
              </Link>
              <Link href="/projects" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                Projects
              </Link>
              <Link href="/clients" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                Clients
              </Link>
              <Link href="/call-screen" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                Call Screen
              </Link>
              <Link href="/calls" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                Call Center
              </Link>
              <Link href="/ai-models" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                AI Models
              </Link>
              <Link href="/image-generator" className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium">
                Image Generator
              </Link>
            </nav>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Section */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Construction Business Management Dashboard</h2>
          <p className="text-gray-600">
            Comprehensive construction business management solution with smart call screening, 
            AI-powered assistance, AI image generation, and integrated project management.
          </p>
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Building className="h-8 w-8 text-blue-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Active Projects</p>
                <p className="text-2xl font-bold text-gray-900">{stats.activeProjects}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Users className="h-8 w-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Clients</p>
                <p className="text-2xl font-bold text-gray-900">{stats.totalClients}</p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Phone className="h-8 w-8 text-purple-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Pending Calls</p>
                <p className="text-2xl font-bold text-gray-900">{stats.pendingCalls}</p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Brain className="h-8 w-8 text-yellow-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">AI Models</p>
                <p className="text-2xl font-bold text-gray-900">{stats.aiModelsLoaded}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Link href="/call-screen" className="block">
            <div className="bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg p-6 text-white hover:from-blue-600 hover:to-blue-700 transition-colors">
              <Phone className="h-12 w-12 mb-4" />
              <h3 className="text-xl font-bold mb-2">Smart Call Screen</h3>
              <p className="text-blue-100">AI-powered call screening and routing system for better customer service.</p>
            </div>
          </Link>

          <Link href="/projects" className="block">
            <div className="bg-gradient-to-r from-green-500 to-green-600 rounded-lg p-6 text-white hover:from-green-600 hover:to-green-700 transition-colors">
              <BarChart3 className="h-12 w-12 mb-4" />
              <h3 className="text-xl font-bold mb-2">Project Management</h3>
              <p className="text-green-100">Manage construction projects, track progress, and monitor costs.</p>
            </div>
          </Link>

          <Link href="/ai-models" className="block">
            <div className="bg-gradient-to-r from-purple-500 to-purple-600 rounded-lg p-6 text-white hover:from-purple-600 hover:to-purple-700 transition-colors">
              <Brain className="h-12 w-12 mb-4" />
              <h3 className="text-xl font-bold mb-2">AI Models</h3>
              <p className="text-purple-100">Manage and configure AI models for construction assistance.</p>
            </div>
          </Link>

          <Link href="/image-generator" className="block">
            <div className="bg-gradient-to-r from-orange-500 to-orange-600 rounded-lg p-6 text-white hover:from-orange-600 hover:to-orange-700 transition-colors">
              <ImageIcon className="h-12 w-12 mb-4" />
              <h3 className="text-xl font-bold mb-2">AI Image Generator</h3>
              <p className="text-orange-100">Generate endless variety of construction-themed images with AI.</p>
            </div>
          </Link>
        </div>

        {/* Main Features Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* AI Call Screening */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center mb-4">
              <Phone className="h-6 w-6 text-blue-600" />
              <h3 className="ml-2 text-lg font-semibold text-gray-900">Smart Call Screening</h3>
            </div>
            <p className="text-gray-600 mb-4">AI-powered call analysis and automated screening for construction business inquiries.</p>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Calls Screened Today</span>
                <span className="text-sm font-medium">8 of 12</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-blue-600 h-2 rounded-full" style={{ width: '67%' }}></div>
              </div>
            </div>
            <Link href="/calls" className="mt-4 inline-block text-blue-600 hover:text-blue-800 font-medium">
              View Call Center →
            </Link>
          </div>

          {/* AI Image Generation */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center mb-4">
              <ImageIcon className="h-6 w-6 text-orange-600" />
              <h3 className="ml-2 text-lg font-semibold text-gray-900">AI Image Generation</h3>
            </div>
            <p className="text-gray-600 mb-4">Generate construction-themed images with AI for proposals, marketing, and visualization.</p>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Images Generated</span>
                <span className="text-sm font-medium">24 today</span>
              </div>
              <div className="flex space-x-2">
                <span className="px-2 py-1 bg-orange-100 text-orange-800 text-xs rounded">DALL-E 3</span>
                <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">Ready</span>
              </div>
            </div>
            <Link href="/image-generator" className="mt-4 inline-block text-orange-600 hover:text-orange-800 font-medium">
              Generate Images →
            </Link>
          </div>

          {/* AI Models */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center mb-4">
              <Brain className="h-6 w-6 text-purple-600" />
              <h3 className="ml-2 text-lg font-semibold text-gray-900">AI Model Management</h3>
            </div>
            <p className="text-gray-600 mb-4">Manage multiple AI models for call analysis, project insights, and business intelligence.</p>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Active Models</span>
                <span className="text-sm font-medium">3 models</span>
              </div>
              <div className="flex space-x-2">
                <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">GPT-4</span>
                <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">Claude</span>
                <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">Whisper</span>
              </div>
            </div>
            <Link href="/ai-models" className="mt-4 inline-block text-purple-600 hover:text-purple-800 font-medium">
              Manage AI Models →
            </Link>
          </div>

          {/* Market Analysis */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center mb-4">
              <Calendar className="h-6 w-6 text-yellow-600" />
              <h3 className="ml-2 text-lg font-semibold text-gray-900">Market Intelligence</h3>
            </div>
            <p className="text-gray-600 mb-4">Real-time market data, labor rates, material costs, and construction trends.</p>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Labor Rate Trend</span>
                <span className="text-sm font-medium text-green-600">↑ 3.2%</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Material Costs</span>
                <span className="text-sm font-medium text-red-600">↑ 5.1%</span>
              </div>
            </div>
            <Link href="/market" className="mt-4 inline-block text-yellow-600 hover:text-yellow-800 font-medium">
              View Market Data →
            </Link>
          </div>
        </div>

        {/* Recent Activity */}
        <div className="bg-white rounded-lg shadow mb-8">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">Recent Activity</h3>
          </div>
          <div className="p-6">
            <div className="space-y-4">
              <div className="flex items-center space-x-3">
                <div className="flex-shrink-0">
                  <CheckCircle className="h-5 w-5 text-green-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-gray-900">Kitchen renovation project completed - Johnson Residence</p>
                  <p className="text-xs text-gray-500">2 hours ago</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-3">
                <div className="flex-shrink-0">
                  <Phone className="h-5 w-5 text-blue-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-gray-900">New call from ABC Construction Corp - Project status inquiry</p>
                  <p className="text-xs text-gray-500">4 hours ago</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-3">
                <div className="flex-shrink-0">
                  <ImageIcon className="h-5 w-5 text-orange-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-gray-900">Generated 15 construction images for project proposals</p>
                  <p className="text-xs text-gray-500">1 day ago</p>
                </div>
              </div>
              
              <div className="flex items-center space-x-3">
                <div className="flex-shrink-0">
                  <Brain className="h-5 w-5 text-purple-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-gray-900">AI model analysis completed for market trends report</p>
                  <p className="text-xs text-gray-500">2 days ago</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* System Status */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">System Status</h3>
          </div>
          <div className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
                <span className="text-sm text-gray-600">AI Models: Online</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
                <span className="text-sm text-gray-600">Call System: Active</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
                <span className="text-sm text-gray-600">Database: Connected</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
                <span className="text-sm text-gray-600">Image Generator: Ready</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
