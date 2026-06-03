'use client'

import Link from "next/link";
import { useState } from "react";
import { Building, ArrowLeft, Calendar, DollarSign, MapPin, Users, Plus } from "lucide-react";

interface Project {
  id: string;
  name: string;
  description: string;
  status: 'planning' | 'active' | 'completed' | 'cancelled';
  budget: number;
  location: string;
  startDate: string;
  endDate?: string;
  client: {
    name: string;
  };
  _count: {
    tasks: number;
  };
}

// Mock data for demonstration
const mockProjects: Project[] = [
  {
    id: '1',
    name: 'Residential Complex A',
    description: 'Construction of a 50-unit residential complex',
    status: 'active',
    budget: 2500000,
    location: '789 Construction Blvd, Build City, BC 11111',
    startDate: '2024-01-15',
    endDate: '2024-12-31',
    client: { name: 'ABC Construction Corp' },
    _count: { tasks: 8 }
  },
  {
    id: '2',
    name: 'Office Building Renovation',
    description: 'Complete renovation of a 10-story office building',
    status: 'planning',
    budget: 1200000,
    location: '321 Office Plaza, Business District, BD 22222',
    startDate: '2024-03-01',
    endDate: '2024-08-30',
    client: { name: 'XYZ Development LLC' },
    _count: { tasks: 5 }
  },
  {
    id: '3',
    name: 'Shopping Center Expansion',
    description: 'Adding 20,000 sq ft to existing shopping center',
    status: 'active',
    budget: 850000,
    location: '555 Retail Way, Commerce City, CC 33333',
    startDate: '2024-02-01',
    endDate: '2024-09-15',
    client: { name: 'Retail Properties Inc' },
    _count: { tasks: 12 }
  },
  {
    id: '4',
    name: 'Warehouse Construction',
    description: 'New 40,000 sq ft distribution warehouse',
    status: 'completed',
    budget: 3200000,
    location: '100 Industrial Blvd, Logistics Park, LP 44444',
    startDate: '2023-06-01',
    endDate: '2024-01-15',
    client: { name: 'Logistics Solutions Ltd' },
    _count: { tasks: 15 }
  }
];

const statusColors = {
  planning: 'bg-yellow-100 text-yellow-800',
  active: 'bg-green-100 text-green-800',
  completed: 'bg-blue-100 text-blue-800',
  cancelled: 'bg-red-100 text-red-800'
};

export default function ProjectsPage() {
  const [projects] = useState<Project[]>(mockProjects);
  const [filter, setFilter] = useState<'all' | 'planning' | 'active' | 'completed' | 'cancelled'>('all');

  const filteredProjects = filter === 'all' 
    ? projects 
    : projects.filter(project => project.status === filter);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
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
              <Building className="h-8 w-8 text-blue-600" />
              <h1 className="ml-2 text-xl font-bold text-gray-900">Project Management</h1>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h2 className="text-3xl font-bold text-gray-900">Construction Projects</h2>
            <p className="text-gray-600 mt-2">Manage all your construction projects, timelines, and budgets</p>
          </div>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center hover:bg-blue-700">
            <Plus className="h-5 w-5 mr-2" />
            New Project
          </button>
        </div>

        {/* Filter Tabs */}
        <div className="mb-6">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {[
                { key: 'all', label: 'All Projects', count: projects.length },
                { key: 'planning', label: 'Planning', count: projects.filter(p => p.status === 'planning').length },
                { key: 'active', label: 'Active', count: projects.filter(p => p.status === 'active').length },
                { key: 'completed', label: 'Completed', count: projects.filter(p => p.status === 'completed').length },
              ].map((tab) => (
                <button
                  key={tab.key}
                  onClick={() => setFilter(tab.key as 'all' | 'planning' | 'active' | 'completed' | 'cancelled')}
                  className={`${
                    filter === tab.key
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm`}
                >
                  {tab.label} ({tab.count})
                </button>
              ))}
            </nav>
          </div>
        </div>

        {/* Projects Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {filteredProjects.map((project) => (
            <div key={project.id} className="bg-white rounded-lg shadow hover:shadow-md transition-shadow">
              <div className="p-6">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">{project.name}</h3>
                    <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${statusColors[project.status]}`}>
                      {project.status.charAt(0).toUpperCase() + project.status.slice(1)}
                    </span>
                  </div>
                  <Building className="h-6 w-6 text-gray-400" />
                </div>

                <p className="text-gray-600 mb-4 text-sm">{project.description}</p>

                <div className="space-y-3">
                  <div className="flex items-center text-sm text-gray-600">
                    <Users className="h-4 w-4 mr-2" />
                    <span>{project.client.name}</span>
                  </div>

                  <div className="flex items-center text-sm text-gray-600">
                    <MapPin className="h-4 w-4 mr-2" />
                    <span className="truncate">{project.location}</span>
                  </div>

                  <div className="flex items-center text-sm text-gray-600">
                    <DollarSign className="h-4 w-4 mr-2" />
                    <span>{formatCurrency(project.budget)}</span>
                  </div>

                  <div className="flex items-center text-sm text-gray-600">
                    <Calendar className="h-4 w-4 mr-2" />
                    <span>
                      {formatDate(project.startDate)} - {project.endDate ? formatDate(project.endDate) : 'Ongoing'}
                    </span>
                  </div>
                </div>

                <div className="mt-4 pt-4 border-t border-gray-200">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">{project._count.tasks} tasks</span>
                    <div className="flex space-x-2">
                      <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                        View Details
                      </button>
                      <button className="text-gray-600 hover:text-gray-800 text-sm font-medium">
                        Edit
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {filteredProjects.length === 0 && (
          <div className="text-center py-12">
            <Building className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No projects found</h3>
            <p className="text-gray-600">Create your first project to get started.</p>
          </div>
        )}
      </main>
    </div>
  );
}