'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { ArrowLeft, Building, Users, Calendar, DollarSign, Clock, AlertTriangle, Plus, Search } from 'lucide-react';

interface Project {
  id: string;
  name: string;
  type: string;
  status: string;
  estimatedCost?: number;
  actualCost?: number;
  startDate?: string;
  endDate?: string;
  clientName?: string;
}

interface Client {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  company?: string;
}

interface DashboardData {
  projects: Project[];
  clients: Client[];
  stats: {
    activeProjects: number;
    totalClients: number;
    pendingTasks: number;
    monthlyRevenue: number;
  };
}

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      // Initialize the database if needed
      await fetch('/api/init', { method: 'POST' });
      
      // In a real app, this would fetch from API
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const mockData: DashboardData = {
        projects: [
          {
            id: '1',
            name: 'Johnson Kitchen Renovation',
            type: 'residential',
            status: 'active',
            estimatedCost: 35000,
            startDate: '2024-02-01',
            endDate: '2024-03-15',
            clientName: 'Johnson Family'
          },
          {
            id: '2',
            name: 'Smith Office Building',
            type: 'commercial',
            status: 'planning',
            estimatedCost: 750000,
            startDate: '2024-03-01',
            endDate: '2024-12-31',
            clientName: 'Smith Construction LLC'
          },
          {
            id: '3',
            name: 'Davis Bathroom Renovation',
            type: 'residential',
            status: 'completed',
            estimatedCost: 18000,
            actualCost: 19500,
            startDate: '2024-01-15',
            endDate: '2024-02-28',
            clientName: 'Davis Residence'
          }
        ],
        clients: [
          {
            id: '1',
            name: 'Johnson Family',
            email: 'sarah.johnson@email.com',
            phone: '(555) 123-4567'
          },
          {
            id: '2',
            name: 'Smith Construction LLC',
            email: 'contact@smithconstruction.com',
            phone: '(555) 987-6543',
            company: 'Smith Construction LLC'
          },
          {
            id: '3',
            name: 'Davis Residence',
            email: 'mike.davis@email.com',
            phone: '(555) 456-7890'
          }
        ],
        stats: {
          activeProjects: 12,
          totalClients: 45,
          pendingTasks: 28,
          monthlyRevenue: 185000
        }
      };
      
      setData(mockData);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'text-green-600 bg-green-100';
      case 'planning': return 'text-blue-600 bg-blue-100';
      case 'completed': return 'text-gray-600 bg-gray-100';
      case 'on-hold': return 'text-yellow-600 bg-yellow-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const filteredProjects = data?.projects.filter(project =>
    project.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    project.clientName?.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading Dashboard...</p>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="h-16 w-16 text-red-500 mx-auto mb-4" />
          <p className="text-gray-600">Failed to load dashboard data</p>
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
              <Link href="/" className="mr-4">
                <ArrowLeft className="h-6 w-6 text-gray-600 hover:text-gray-900" />
              </Link>
              <Building className="h-8 w-8 text-blue-600" />
              <h1 className="ml-2 text-xl font-bold text-gray-900">Project Dashboard</h1>
            </div>
            <div className="flex items-center space-x-4">
              <button className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">
                <Plus className="h-4 w-4 mr-2" />
                New Project
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {/* Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Building className="h-8 w-8 text-blue-600" />
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-600">Active Projects</p>
                <p className="text-2xl font-bold text-gray-900">{data.stats.activeProjects}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Users className="h-8 w-8 text-green-600" />
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-600">Total Clients</p>
                <p className="text-2xl font-bold text-gray-900">{data.stats.totalClients}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Clock className="h-8 w-8 text-yellow-600" />
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-600">Pending Tasks</p>
                <p className="text-2xl font-bold text-gray-900">{data.stats.pendingTasks}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <DollarSign className="h-8 w-8 text-purple-600" />
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-600">Monthly Revenue</p>
                <p className="text-2xl font-bold text-gray-900">{formatCurrency(data.stats.monthlyRevenue)}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Projects List */}
          <div className="lg:col-span-2">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b">
                <div className="flex justify-between items-center">
                  <h2 className="text-lg font-medium text-gray-900">Recent Projects</h2>
                  <div className="relative">
                    <Search className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      placeholder="Search projects..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                </div>
              </div>
              
              <div className="divide-y divide-gray-200">
                {filteredProjects.map((project) => (
                  <div key={project.id} className="px-6 py-4 hover:bg-gray-50">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <h3 className="text-lg font-medium text-gray-900">{project.name}</h3>
                        <div className="flex items-center space-x-4 mt-1">
                          <span className="text-sm text-gray-600">Client: {project.clientName}</span>
                          <span className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(project.status)}`}>
                            {project.status}
                          </span>
                          <span className="text-sm text-gray-500 capitalize">{project.type}</span>
                        </div>
                        <div className="flex items-center space-x-4 mt-2 text-sm text-gray-600">
                          {project.startDate && (
                            <div className="flex items-center">
                              <Calendar className="h-4 w-4 mr-1" />
                              <span>Start: {formatDate(project.startDate)}</span>
                            </div>
                          )}
                          {project.endDate && (
                            <div className="flex items-center">
                              <Calendar className="h-4 w-4 mr-1" />
                              <span>End: {formatDate(project.endDate)}</span>
                            </div>
                          )}
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-lg font-medium text-gray-900">
                          {formatCurrency(project.actualCost || project.estimatedCost || 0)}
                        </p>
                        {project.actualCost && project.estimatedCost && (
                          <p className={`text-sm ${project.actualCost > project.estimatedCost ? 'text-red-600' : 'text-green-600'}`}>
                            {project.actualCost > project.estimatedCost ? '+' : ''}
                            {formatCurrency(project.actualCost - project.estimatedCost)}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Clients & Quick Actions */}
          <div className="space-y-6">
            {/* Recent Clients */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b">
                <h3 className="text-lg font-medium text-gray-900">Recent Clients</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {data.clients.slice(0, 5).map((client) => (
                  <div key={client.id} className="px-6 py-4">
                    <div className="flex items-center">
                      <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
                        <span className="text-white font-medium text-sm">
                          {client.name.charAt(0)}
                        </span>
                      </div>
                      <div className="ml-3">
                        <p className="font-medium text-gray-900">{client.name}</p>
                        <p className="text-sm text-gray-600">{client.phone}</p>
                        {client.company && (
                          <p className="text-xs text-gray-500">{client.company}</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Quick Actions */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b">
                <h3 className="text-lg font-medium text-gray-900">Quick Actions</h3>
              </div>
              <div className="px-6 py-4 space-y-3">
                <button className="w-full text-left px-4 py-3 rounded-md border border-gray-200 hover:bg-gray-50 flex items-center">
                  <Plus className="h-5 w-5 text-blue-600 mr-3" />
                  <span>Create New Project</span>
                </button>
                <button className="w-full text-left px-4 py-3 rounded-md border border-gray-200 hover:bg-gray-50 flex items-center">
                  <Users className="h-5 w-5 text-green-600 mr-3" />
                  <span>Add New Client</span>
                </button>
                <button className="w-full text-left px-4 py-3 rounded-md border border-gray-200 hover:bg-gray-50 flex items-center">
                  <Calendar className="h-5 w-5 text-purple-600 mr-3" />
                  <span>Schedule Appointment</span>
                </button>
                <button className="w-full text-left px-4 py-3 rounded-md border border-gray-200 hover:bg-gray-50 flex items-center">
                  <DollarSign className="h-5 w-5 text-yellow-600 mr-3" />
                  <span>Generate Invoice</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}